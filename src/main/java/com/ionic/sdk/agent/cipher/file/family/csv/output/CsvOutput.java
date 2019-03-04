package com.ionic.sdk.agent.cipher.file.family.csv.output;

import com.ionic.sdk.agent.cipher.file.CsvFileCipher;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesInterface;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Wrap an output stream with logic to manage the Ionic augmentation of the content (header, cipher blocks).
 */
@InternalUseOnly
public class CsvOutput {

    /**
     * The raw output data stream that is to contain the protected file content.  A reference to this object is
     * retained in order to enable querying the output for bytes written.
     */
    private final DataOutputStream dataOutputStream;

    /**
     * The length of the resource to be encrypted.
     */
    private final long sizeInput;

    /**
     * The buffered output data stream that is to contain the protected file content.  Output is written to this
     * stream for efficiency.
     */
    private final BufferedOutputStream targetStream;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * Cover page services implementation; used to substitute cover pages to display on failure to access crypto key.
     */
    private final FileCryptoCoverPageServicesInterface coverPageServices;

    /**
     * The cipher family implementation for managing the file body content for the specified version.
     */
    private CsvBodyOutput bodyOutput;

    /**
     * The length of the Ionic CSV file header.  We cache this position so we can seek in the content to the
     * correct position at which the wrapped content is found.
     */
    private int headerLength;

    /**
     * @return the {@link ByteBuffer} allocated to hold a plaintext block for this cryptography operation
     */
    public ByteBuffer getPlainText() {
        return bodyOutput.getPlainText();
    }

    /**
     * Constructor.
     *
     * @param outputStream      the raw output data that will contain the protected file content
     * @param sizeInput         the length of the resource to be encrypted
     * @param agent             the key services implementation; used to provide keys for cryptography operations
     * @param coverPageServices the cover page services implementation
     */
    public CsvOutput(final DataOutputStream outputStream, final long sizeInput, final KeyServices agent,
                     final FileCryptoCoverPageServicesInterface coverPageServices) {
        this.dataOutputStream = outputStream;
        this.sizeInput = sizeInput;
        this.targetStream = new BufferedOutputStream(outputStream);
        this.agent = agent;
        this.coverPageServices = coverPageServices;
    }

    /**
     * Initialize this object for processing the body of an Ionic-protected file.
     *
     * @param encryptAttributes a container for applying desired configuration to the operation,
     *                          and receiving status of the operation
     * @throws IOException    on failure writing to the stream
     * @throws IonicException on:
     *                        <ul>
     *                        <li>incorrect / missing Ionic file version</li>
     *                        <li>cover page fetch failure</li>
     *                        <li>key creation failure</li>
     *                        <li>failure writing to output stream</li>
     *                        </ul>
     */
    public void init(final FileCryptoEncryptAttributes encryptAttributes) throws IonicException, IOException {
        encryptAttributes.setFamily(CipherFamily.FAMILY_CSV);
        encryptAttributes.setVersion(CsvFileCipher.VERSION_LATEST);
        final String version = encryptAttributes.getVersion();
        SdkData.checkTrue(!Value.isEmpty(version), SdkError.ISFILECRYPTO_MISSINGVALUE);
        targetStream.write(coverPageServices.getCoverPage(FileType.FILETYPE_CSV));
        // quote replicates core SDK behavior
        targetStream.write(Transcoder.utf8().decode(FileCipher.Csv.V10.QUOTE_CHAR));
        targetStream.write(Transcoder.utf8().decode(FileCipher.Csv.V10.LINE_SEPARATOR));
        targetStream.write(Transcoder.utf8().decode(new CsvHeaderOutput().write(version)));
        if (FileCipher.Csv.V10.LABEL.equals(version)) {
            // alternate CsvBodyOutput implementation can be substituted here
            bodyOutput = new Csv10BodyOutput(targetStream, sizeInput, agent, encryptAttributes);
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
        bodyOutput.init();
        headerLength = dataOutputStream.size();
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
     * @throws IOException    on failure writing to the stream
     * @throws IonicException on failure to encrypt the block, or calculate the block signature
     */
    public void write(final ByteBuffer byteBuffer) throws IOException, IonicException {
        if (bodyOutput == null) {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        } else {
            bodyOutput.write(byteBuffer);
        }
    }

    /**
     * Finish processing of the output stream.
     *
     * @throws IOException on failure flushing the stream
     */
    public void doFinal() throws IOException {
        bodyOutput.doFinal();
        // quote replicates core SDK behavior
        targetStream.write(Transcoder.utf8().decode(FileCipher.Csv.V10.QUOTE_CHAR));
        targetStream.write(Transcoder.utf8().decode(FileCipher.Csv.V10.LINE_SEPARATOR));
        targetStream.flush();
    }

    /**
     * The length of the Ionic file header.  We cache this position so we can seek in the content to the
     * correct position at which the file signature should be written.
     *
     * @return the number of bytes in the Ionic file header
     */
    public int getHeaderLengthCsv() {
        return headerLength;
    }

    /**
     * The length of the Ionic file header.  We cache this position so we can seek in the content to the
     * correct position at which the file signature should be written.
     *
     * @return the number of bytes in the Ionic file header
     */
    public int getHeaderLengthWrapped() {
        return bodyOutput.getHeaderLengthWrapped();
    }

    /**
     * Retrieve the calculated file signature for the output.  This is inserted into the file content immediately
     * after the Ionic file header.
     *
     * @return the Ionic-protected signature bytes associated with the output
     * @throws IonicException on failure to calculate the file signature (if present)
     */
    public byte[] getSignatureWrapped() throws IonicException {
        return bodyOutput.getSignatureWrapped();
    }
}
