package com.ionic.sdk.agent.cipher.file.family.csv.input;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Wrap an input stream with logic to manage the Ionic augmentation of the content (header, content representation).
 */
@InternalUseOnly
public final class CsvInput {

    /**
     * The raw input data stream containing the protected file content.
     */
    private final BufferedInputStream sourceStream;

    /**
     * The length of the resource to be decrypted.
     */
    private final long sizeInput;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * The cipher family implementation for managing the file body content for the specified version.
     */
    private CsvBodyInput bodyInput;

    /**
     * Constructor.
     *
     * @param inputStream the raw input data containing the protected file content
     * @param sizeInput   the length of the resource to be decrypted
     * @param agent       the key services implementation; used to provide keys for cryptography operations
     */
    public CsvInput(final InputStream inputStream, final long sizeInput, final KeyServices agent) {
        this.sourceStream = new BufferedInputStream(inputStream);
        this.sizeInput = sizeInput;
        this.agent = agent;
    }

    /**
     * Initialize this object for processing an Ionic-protected file.  The file is expected to begin with a text
     * header, indicating the content type associated with the file.
     *
     * @param fileInfo          the structure into which data about the Ionic state of the file should be written
     * @param decryptAttributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on failure to load or parse header, or specification of an unsupported file format, or
     *                        cipher initialization
     * @throws IOException    on failure reading from the stream
     */
    public void init(final FileCryptoFileInfo fileInfo,
                     final FileCryptoDecryptAttributes decryptAttributes) throws IonicException, IOException {
        final String ionicHeader = new CsvHeaderInput().read(sourceStream);
        if (ionicHeader.contains(FileCipher.Csv.V10.VERSION_1_0_STRING)) {
            // alternate CsvBodyOutput implementation can be substituted here
            bodyInput = new Csv10BodyInput(sourceStream, sizeInput, agent, fileInfo, decryptAttributes);
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
        bodyInput.init();
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream
     * without blocking by the next invocation of a method for this input stream.
     *
     * @return an estimate of the number of bytes that can be read (or skipped over) from this input stream
     * without blocking
     * @throws IonicException on specification of an unsupported file format
     * @throws IOException    if this input stream has been closed, or an I/O error occurs
     */
    public int available() throws IonicException, IOException {
        if (bodyInput == null) {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        } else {
            return bodyInput.available();
        }
    }

    /**
     * Read the Ionic-protected content from the input resource body.
     *
     * @return the content extracted from the stream
     * @throws IOException    on failure reading from the stream
     * @throws IonicException on failure to decrypt the content
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
     * @throws IonicException on failure to finalize the wrapped stream
     */
    public void doFinal() throws IonicException {
        if (bodyInput == null) {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        } else {
            bodyInput.doFinal();
        }
    }
}
