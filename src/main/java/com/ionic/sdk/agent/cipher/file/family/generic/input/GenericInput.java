package com.ionic.sdk.agent.cipher.file.family.generic.input;

import com.ionic.sdk.agent.cipher.file.GenericFileCipher;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.key.KeyServices;

import javax.json.JsonObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Wrap an input stream with logic to manage the Ionic augmentation of the content (header, cipher blocks).
 */
@InternalUseOnly
public final class GenericInput {

    /**
     * The raw input data stream containing the protected file content.
     */
    private final BufferedInputStream sourceStream;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * The buffer to hold a plaintext block (source buffer for encryption, target buffer for decryption).
     */
    private final ByteBuffer plainText;

    /**
     * The buffer to hold a ciphertext block (source buffer for decryption, target buffer for encryption).
     */
    private final ByteBuffer cipherText;

    /**
     * The cipher family implementation for managing the file body content for the specified version.
     */
    private GenericBodyInput bodyInput;

    /**
     * @return the {@link ByteBuffer} allocated to hold a plaintext block for this cryptography operation
     */
    public ByteBuffer getPlainText() {
        return plainText;
    }

    /**
     * @return the {@link ByteBuffer} allocated to hold a ciphertext block for this cryptography operation
     */
    public ByteBuffer getCipherText() {
        return cipherText;
    }

    /**
     * Constructor.
     *
     * @param inputStream the raw input data containing the protected file content
     * @param sizeInput   the length of the resource to be decrypted
     * @param agent       the key services implementation; used to provide keys for cryptography operations
     */
    public GenericInput(final InputStream inputStream, final long sizeInput, final KeyServices agent) {
        this.sourceStream = new BufferedInputStream(inputStream);
        this.agent = agent;
        final int sizeBlockCipher = Math.max(AesCipher.SIZE_IV,
                (int) Math.min(sizeInput, FileCipher.Generic.V12.BLOCK_SIZE_CIPHER));
        this.plainText = ByteBuffer.allocate(sizeBlockCipher - AesCipher.SIZE_IV);
        this.cipherText = ByteBuffer.allocate(sizeBlockCipher);
    }

    /**
     * Initialize this object for processing an Ionic-protected file.  The file is expected to begin with a JSON
     * header, describing metadata associated with the file.
     *
     * @param fileInfo          the structure into which data about the Ionic state of the file should be written
     * @param decryptAttributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on failure to load or parse header, or specification of an unsupported file format, or
     *                        cipher initialization
     */
    public void init(final FileCryptoFileInfo fileInfo,
                     final FileCryptoDecryptAttributes decryptAttributes) throws IonicException {
        // deserialize Ionic generic file family JSON header
        final JsonObject jsonHeader;
        try {
            final String ionicHeader = new GenericHeaderInput().read(sourceStream);
            jsonHeader = JsonIO.readObject(ionicHeader, SdkError.ISFILECRYPTO_PARSEFAILED);
        } catch (IonicException e) {
            fileInfo.setCipherFamily(CipherFamily.FAMILY_GENERIC);
            fileInfo.setCipherVersion(GenericFileCipher.VERSION_DEFAULT);
            throw e;
        }
        // parse Ionic generic file family JSON header
        final String family = Value.defaultOnEmpty(
                JsonSource.getString(jsonHeader, FileCipher.Header.FAMILY), FileCipher.Generic.FAMILY);
        SdkData.checkTrue(FileCipher.Generic.FAMILY.equals(family), SdkError.ISFILECRYPTO_UNRECOGNIZED);
        final String version = JsonSource.getString(jsonHeader, FileCipher.Header.VERSION);
        SdkData.checkTrue(!Value.isEmpty(version), SdkError.ISFILECRYPTO_MISSINGVALUE);
        final String tag = JsonSource.getString(jsonHeader, FileCipher.Header.TAG);
        final String server = JsonSource.getString(jsonHeader, FileCipher.Header.SERVER);
        // input file validation
        final boolean isV11 = FileCipher.Generic.V11.LABEL.equals(version);
        final boolean isV12 = FileCipher.Generic.V12.LABEL.equals(version);
        final boolean isV13 = FileCipher.Generic.V13.LABEL.equals(version);
        final boolean isTagged = !Value.isEmpty(tag);
        SdkData.checkTrue(((isV11 || isV12 || isV13) && isTagged), SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        // set FileCryptoFileInfo members
        fileInfo.setEncrypted(true);
        fileInfo.setCipherFamily(CipherFamily.FAMILY_GENERIC);
        fileInfo.setCipherVersion(version);
        fileInfo.setKeyId(tag);
        fileInfo.setServer(server);
        // guard against FileCryptoDecryptAttributes reuse
        decryptAttributes.validateInput();
        // set FileCryptoDecryptAttributes members
        decryptAttributes.setFamily(CipherFamily.FAMILY_GENERIC);
        decryptAttributes.setVersion(version);
        // perform server transaction ("getFileInfo()" does not supply an agent instance)
        final GetKeysResponse.Key key = (agent == null) ? null : agent.getKey(tag).getFirstKey();
        if (isV11) {
            bodyInput = new Generic11BodyInput(sourceStream, key);
        } else if (isV12) {
            bodyInput = new Generic12BodyInput(sourceStream, key, plainText, cipherText);
        } else if (isV13) {
            final int blockSize = JsonSource.getInt(jsonHeader, FileCipher.Generic.BLOCK_SIZE);
            final int metaSize = JsonSource.getInt(jsonHeader, FileCipher.Generic.META_SIZE);
            bodyInput = new Generic13BodyInput(sourceStream, agent, key, blockSize, metaSize, plainText, cipherText);
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
        if (key != null) {
            decryptAttributes.setKeyResponse(key);
        }
        // prime the decryption buffer
        bodyInput.init();
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream
     * without blocking by the next invocation of a method for this input stream.
     *
     * @return an estimate of the number of bytes that can be read (or skipped over) from this input stream
     * without blocking
     * @throws IOException if this input stream has been closed, or an I/O error occurs
     */
    public int available() throws IOException {
        return sourceStream.available();
    }

    /**
     * Read the next Ionic-protected block from the input resource body.
     *
     * @return the next plainText block extracted from the stream, wrapped in a {@link ByteBuffer} object
     * @throws IOException    on failure reading from the stream
     * @throws IonicException on failure to decrypt the block, or calculate the block signature
     */
    public ByteBuffer read() throws IOException, IonicException {
        if (bodyInput == null) {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        } else {
            return bodyInput.read();
        }
    }

    /**
     * Finish processing of the input stream.
     *
     * @throws IonicException on failure to verify the file signature (if present)
     */
    public void doFinal() throws IonicException {
        bodyInput.doFinal();
    }
}
