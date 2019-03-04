package com.ionic.sdk.agent.cipher.file.family.generic.output;

import com.ionic.sdk.agent.cipher.file.GenericFileCipher;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Wrap an output stream with logic to manage the Ionic augmentation of the content (header, cipher blocks).
 */
@InternalUseOnly
public final class GenericOutput {

    /**
     * The raw output data stream that is to contain the protected file content.
     */
    private final BufferedOutputStream targetStream;

    /**
     * The length of the resource to be encrypted.
     */
    private final long sizeInput;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * The buffer to hold a plaintext block (source buffer for encryption, target buffer for decryption).
     */
    private ByteBuffer plainText;

    /**
     * The buffer to hold a ciphertext block (source buffer for decryption, target buffer for encryption).
     */
    private ByteBuffer cipherText;

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
     * The cipher family implementation for managing the file body content for the specified version.
     */
    private GenericBodyOutput bodyOutput;

    /**
     * The length of the Ionic file header.  We cache this position so we can seek in the content to the
     * correct position at which the file signature should be written.
     */
    private int headerLength;

    /**
     * The length of the output of the encryption operation.  This is tracked so that it may be available to
     * wrapping ciphers (in particular, {@link com.ionic.sdk.agent.cipher.file.PdfFileCipher}).
     */
    private int outputLength;

    /**
     * Constructor.
     *
     * @param outputStream the raw output data that will contain the protected file content
     * @param sizeInput    the length of the resource to be encrypted
     * @param agent        the key services implementation; used to provide keys for cryptography operations
     */
    public GenericOutput(final OutputStream outputStream, final long sizeInput, final KeyServices agent) {
        this.targetStream = new BufferedOutputStream(outputStream);
        this.sizeInput = sizeInput;
        this.agent = agent;
    }

    /**
     * Initialize this object for processing the body of an Ionic-protected file.
     *
     * @param encryptAttributes a container for applying desired configuration to the operation,
     *                          and receiving status  of the operation
     * @return the number of bytes written in the context of this call
     * @throws IOException    on failure reading from the stream
     * @throws IonicException on failure to decrypt the file signature (if present)
     */
    public int init(final FileCryptoEncryptAttributes encryptAttributes) throws IonicException, IOException {
        encryptAttributes.validateInput();
        final String version = Value.defaultOnEmpty(
                encryptAttributes.getVersion(), GenericFileCipher.VERSION_LATEST);
        final CreateKeysResponse keysResponse = agent.createKey(
                encryptAttributes.getKeyAttributes(), encryptAttributes.getMutableKeyAttributes());
        encryptAttributes.setFamily(CipherFamily.FAMILY_GENERIC);
        encryptAttributes.setVersion(version);
        final CreateKeysResponse.Key createKey = keysResponse.getFirstKey();
        createKey.setAttributesMap(encryptAttributes.getKeyAttributes());
        createKey.setMutableAttributesMap(encryptAttributes.getMutableKeyAttributes());
        encryptAttributes.setKeyResponse(createKey);
        final AesCtrCipher cipher = new AesCtrCipher(createKey.getKey());
        final String headerText = new GenericHeaderOutput().write(
                version, agent.getActiveProfile().getServer(), createKey.getId());
        if (FileCipher.Generic.V11.LABEL.equals(version)) {
            plainText = ByteBuffer.allocate(FileCipher.Generic.V11.BLOCK_SIZE_PLAIN);
            cipherText = ByteBuffer.allocate(FileCipher.Generic.V11.BLOCK_SIZE_PLAIN + AesCipher.SIZE_IV);
            bodyOutput = new Generic11BodyOutput(targetStream, cipher);
        } else if (FileCipher.Generic.V12.LABEL.equals(version)) {
            final int sizeBlock = (int) Math.min(sizeInput, FileCipher.Generic.V12.BLOCK_SIZE_PLAIN);
            plainText = ByteBuffer.allocate(sizeBlock);
            cipherText = ByteBuffer.allocate(sizeBlock + AesCipher.SIZE_IV);
            bodyOutput = new Generic12BodyOutput(targetStream, cipher, createKey, plainText, cipherText);
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
        final byte[] header = Transcoder.utf8().decode(headerText);
        headerLength = header.length;
        targetStream.write(header);
        final int countBodyInit = bodyOutput.init();
        outputLength += (headerLength + countBodyInit);
        return headerLength + countBodyInit;
    }

    /**
     * Each generic file format specifies a plain text block length.  This is the amount of plain text that
     * constitutes a block on which an encrypt operation is performed.
     *
     * @return the amount of plain text that should be converted to cipher text in a single operation
     */
    public int getBlockLengthPlain() {
        return bodyOutput.getBlockLengthPlain();
    }

    /**
     * Write the next Ionic-protected block to the output resource.
     *
     * @param byteBuffer the next plainText block to be written to the stream
     * @return the number of cipherText bytes written in the context of this call
     * @throws IOException    on failure writing to the stream
     * @throws IonicException on failure to encrypt the block, or calculate the block signature
     */
    public int write(final ByteBuffer byteBuffer) throws IOException, IonicException {
        if (bodyOutput == null) {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        } else {
            final int write = bodyOutput.write(byteBuffer);
            outputLength += write;
            return write;
        }
    }

    /**
     * Finish processing of the output stream.
     *
     * @throws IOException on failure flushing the stream
     */
    public void doFinal() throws IOException {
        bodyOutput.doFinal();
        targetStream.flush();
    }

    /**
     * The length of the Ionic file header.  We cache this position so we can seek in the content to the
     * correct position at which the file signature should be written.
     *
     * @return the number of bytes in the Ionic file header
     */
    public int getHeaderLength() {
        return headerLength;
    }

    /**
     * Retrieve the calculated file signature for the output.  This is inserted into the file content immediately
     * after the Ionic file header.
     *
     * @return the Ionic-protected signature bytes associated with the output
     * @throws IonicException on failure to calculate the file signature (if present)
     */
    public byte[] getSignature() throws IonicException {
        return bodyOutput.getSignature();
    }

    /**
     * @return the position in the generic output stream at which the file signature should be written
     */
    public int getSignatureOffset() {
        return headerLength;
    }

    /**
     * @return the length of the output of the encryption operation
     */
    public int getOutputLength() {
        return outputLength;
    }
}
