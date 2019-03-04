package com.ionic.sdk.agent.cipher.file.family.openxml.output;

import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesInterface;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Wrap an output stream with logic to manage the Ionic augmentation of the content (header, cipher blocks).
 */
@InternalUseOnly
public final class OpenXmlOutput {

    /**
     * The raw output data stream that is to contain the protected file content.
     */
    private final OutputStream targetStream;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * The cipher family implementation for managing the file body content for the specified version.
     */
    private OpenXmlBodyOutput bodyOutput;

    /**
     * Cover page services implementation; used to broker key transactions and crypto operations.
     */
    private FileCryptoCoverPageServicesInterface coverpage;

    /**
     * The speific OpenXML file type discovered in an early step.
     */
    private final FileType fileType;

    /**
     * An optional custom properties file to include in the coverpage file.
     */
    private final byte[] customPropFile;

    /**
     * an optional filename to use as a temp file during the process.  Can be null.
     */
    private final File tempFile;

    /**
     * Constructor.
     *
     * @param outputStream  the raw output data that will contain the protected file content
     * @param agent         the key services implementation; used to provide keys for cryptography operations
     * @param coverpage     the cover page services implementation
     * @param fileType      the speific OpenXML file type discovered in an early step
     * @param customPropFile an optional custom properties file to include in the coverpage file
     * @param tempFile      an optional filename to use as a temp file during the process
     *                      if this param is null, the process will use RAM buffer.
     */
    public OpenXmlOutput(final OutputStream outputStream,
                         final KeyServices agent,
                         final FileCryptoCoverPageServicesInterface coverpage,
                         final FileType fileType,
                         final byte[] customPropFile,
                         final File tempFile) {
        this.targetStream = outputStream;
        this.agent = agent;
        this.coverpage = coverpage;
        this.fileType = fileType;
        if (customPropFile != null) {
            // have to make a copy to pass code quality testing.
            this.customPropFile = customPropFile.clone();
        } else {
            this.customPropFile = null;
        }
        this.tempFile = tempFile;
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
        encryptAttributes.setFamily(CipherFamily.FAMILY_OPENXML);
        encryptAttributes.setVersion(Value.defaultOnEmpty(
                encryptAttributes.getVersion(), OpenXmlFileCipher.VERSION_LATEST));
        final String version = encryptAttributes.getVersion();
        SdkData.checkTrue(!Value.isEmpty(version), SdkError.ISFILECRYPTO_MISSINGVALUE);

        if (FileCipher.OpenXml.V10.LABEL.equals(version)) {
            bodyOutput = new OpenXml10BodyOutput(targetStream, agent, encryptAttributes);
        } else if (FileCipher.OpenXml.V11.LABEL.equals(version)) {
            bodyOutput = new OpenXml11BodyOutput(targetStream, agent,
                                                 coverpage, encryptAttributes,
                                                 fileType, customPropFile, tempFile);
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
        bodyOutput.init();
    }

    /**
     * Finish processing the input stream into the output stream.
     *
     * @param plainText the output stream presenting the binary plain text output buffer
     * @throws IOException on failure flushing the stream
     * @throws IonicException on failure to encrypt the block, or calculate the block signature
     */
    public void doEncryption(final InputStream plainText) throws IOException, IonicException {
        bodyOutput.doEncryption(plainText);
    }
}
