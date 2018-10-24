package com.ionic.sdk.agent.cipher.file.family.generic.output;

import com.ionic.sdk.agent.cipher.file.GenericFileCipher;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

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
     * Constructor.
     *
     * @param outputStream the raw output data that will contain the protected file content
     * @param agent        the key services implementation; used to provide keys for cryptography operations
     */
    public GenericOutput(final OutputStream outputStream, final KeyServices agent) {
        this.targetStream = new BufferedOutputStream(outputStream);
        this.agent = agent;
    }

    /**
     * Initialize this object for processing the body of an Ionic-protected file.
     *
     * @param encryptAttributes a container for applying desired configuration to the operation,
     *                          and receiving status  of the operation
     * @throws IOException    on failure reading from the stream
     * @throws IonicException on failure to decrypt the file signature (if present)
     */
    public void init(final FileCryptoEncryptAttributes encryptAttributes) throws IonicException, IOException {
        encryptAttributes.setFamily(CipherFamily.FAMILY_GENERIC);
        encryptAttributes.setVersion(Value.defaultOnEmpty(
                encryptAttributes.getVersion(), GenericFileCipher.VERSION_LATEST));
        final String version = encryptAttributes.getVersion();
        SdkData.checkTrue(!Value.isEmpty(version), SdkError.ISFILECRYPTO_MISSINGVALUE);
        final CreateKeysResponse keysResponse = agent.createKey(
                encryptAttributes.getKeyAttributes(), encryptAttributes.getMutableKeyAttributes());
        encryptAttributes.setKeyResponse(keysResponse.getFirstKey());
        final CreateKeysResponse.Key key = encryptAttributes.getKeyResponse();
        final AesCtrCipher cipher = new AesCtrCipher(key.getKey());
        final String headerText = new GenericHeaderOutput().write(
                version, agent.getActiveProfile().getServer(), key.getId());
        if (FileCipher.Generic.V11.LABEL.equals(version)) {
            bodyOutput = new Generic11BodyOutput(targetStream, cipher);
        } else if (FileCipher.Generic.V12.LABEL.equals(version)) {
            bodyOutput = new Generic12BodyOutput(targetStream, cipher, key);
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
        final byte[] header = Transcoder.utf8().decode(headerText);
        headerLength = header.length;
        targetStream.write(header);
        bodyOutput.init();
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
     * @param block the next plainText block to be written to the stream
     * @throws IOException    on failure writing to the stream
     * @throws IonicException on failure to encrypt the block, or calculate the block signature
     */
    public void write(final byte[] block) throws IOException, IonicException {
        if (bodyOutput == null) {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        } else {
            bodyOutput.write(block);
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
}
