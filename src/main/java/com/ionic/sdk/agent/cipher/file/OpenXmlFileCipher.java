package com.ionic.sdk.agent.cipher.file;

import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesDefault;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesInterface;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCipherUtils;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.agent.cipher.file.family.openxml.input.OpenXmlInput;
import com.ionic.sdk.agent.cipher.file.family.openxml.input.OpenXmlPortionMarkInput;
import com.ionic.sdk.agent.cipher.file.family.openxml.OpenXmlUtils;
import com.ionic.sdk.agent.cipher.file.family.openxml.output.OpenXmlOutput;

import com.ionic.sdk.core.datastructures.Tuple;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Ionic Machina Tools OpenXML (MS Office files) file crypto implementation.  This object can be used to perform
 * encryption and decryption operations on filesystem files that are in the OpenXML format.
 * <p>
 * An instance of {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher} has the following format versions:
 * <ul>
 * <li>version 1.0 files use AES-CTR encryption, and encode the file data using base64, and so are appropriate for
 * applications where the file data must traverse a medium where binary data causes problems</li>
 * <li>version 1.1 files use AES-CTR encryption, and encode the file data as binary, minimizing the storage
 * requirement</li>
 * </ul>
 * <p>
 * {@link OpenXmlFileCipher} provides APIs to perform file encryption on filesystem files, and also on in-memory
 * byte arrays.
 * <p>
 * Sample (byte[] API):
 * <pre>
 * public final void testFileCipherPdf_EncryptDecryptBytes() throws IonicException {
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final byte[] plainText = new FileCryptoCoverPageServicesDefault().getCoverPage(FileType.FILETYPE_DOCX);
 *     final FileCipherAbstract fileCipher = new OpenXmlFileCipher(keyServices);
 *     final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
 *     final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
 *     final byte[] cipherText = fileCipher.encrypt(plainText, encryptAttributes);
 *     final byte[] plainTextRecover = fileCipher.decrypt(cipherText, decryptAttributes);
 *     Assert.assertArrayEquals(plainText, plainTextRecover);
 * }
 * </pre>
 * <p>
 * Sample (file path API):
 * <pre>
 * public final void testFileCipherPdf_EncryptDecryptFile() throws IonicException {
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final byte[] plainText = new FileCryptoCoverPageServicesDefault().getCoverPage(FileType.FILETYPE_DOCX);
 *     final File filePlainText = new File("plainText.docx");
 *     final File fileCipherText = new File("cipherText.docx");
 *     final File filePlainTextRecover = new File("plainText.recover.docx");
 *     DeviceUtils.write(filePlainText, plainText);
 *     final FileCipherAbstract fileCipher = new OpenXmlFileCipher(keyServices);
 *     final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
 *     final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
 *     fileCipher.encrypt(filePlainText.getPath(), fileCipherText.getPath(), encryptAttributes);
 *     fileCipher.decrypt(fileCipherText.getPath(), filePlainTextRecover.getPath(), decryptAttributes);
 *     final String sha256PlainText = CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainText));
 *     final String sha256Recover = CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainTextRecover));
 *     Assert.assertEquals(sha256PlainText, sha256Recover);
 * }
 * </pre>
 * <p>
 * See <a href='https://dev.ionic.com/sdk/formats/file-crypto-openxml' target='_blank'>Machina Developers</a> for
 * more information on the different file crypto data formats.
 */
public final class OpenXmlFileCipher extends FileCipherAbstract {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * File format family OpenXML.
     */
    public static final String FAMILY = FileCipher.OpenXml.FAMILY;

    /**
     * File format family OpenXML, version 1.0.
     */
    public static final String VERSION_1_0 = FileCipher.OpenXml.V10.LABEL;

    /**
     * File format family OpenXML, version 1.1.
     */
    public static final String VERSION_1_1 = FileCipher.OpenXml.V11.LABEL;

    /**
     * File format family OpenXML, default version.
     */
    public static final String VERSION_DEFAULT = VERSION_1_1;

    /**
     * File format family OpenXML, latest version.
     */
    public static final String VERSION_LATEST = VERSION_1_1;

    /**
     * The versions of the encryption format supported by this FileCipher implementation.
     */
    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList(VERSION_1_0, VERSION_1_1);

    /**
     * Constructor.
     *
     * @param agent the key services implementation; used to provide keys for cryptography operations
     */
    public OpenXmlFileCipher(final KeyServices agent) {
        super(agent, new FileCryptoCoverPageServicesDefault());
    }

    /**
     * Constructor.
     *
     * @param agent             the key services implementation; used to provide keys for cryptography operations
     * @param coverPageServices the cover page services implementation
     */
    public OpenXmlFileCipher(final KeyServices agent, final FileCryptoCoverPageServicesInterface coverPageServices) {
        super(agent, coverPageServices);
    }

    @Deprecated
    @Override
    public String getFamily() {
        return FAMILY;
    }

    @Override
    public String getFamilyString() {
        return FAMILY;
    }

    @Override
    public CipherFamily getCipherFamily() {
        return CipherFamily.FAMILY_OPENXML;
    }

    @Override
    public List<String> getVersions() {
        return new ArrayList<String>(SUPPORTED_VERSIONS);
    }

    @Override
    public String getDefaultVersion() {
        return VERSION_1_1;
    }

    @Override
    public boolean isVersionSupported(final String version) {
        return SUPPORTED_VERSIONS.contains(version);
    }

    @Override
    public FileCryptoFileInfo getFileInfo(final String filePath) throws IonicException {
        final FileCryptoFileInfo fileInfo = new FileCryptoFileInfo();
        final File file = new File(filePath);
        if (file.exists() && file.length() > 0) {
            try {
                try (FileInputStream is = new FileInputStream(file)) {
                    try {
                        OpenXmlInput.getFileInfo(is, fileInfo);
                    } catch (IonicException e) {
                        logger.fine(e.getMessage()); // if file cannot be parsed as cipherText, default to plainText
                    }
                }
            } catch (IOException e) {
                throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
            }
        }
        return fileInfo;
    }

    @Override
    public FileCryptoFileInfo getFileInfo(final byte[] text) throws IonicException {
        final FileCryptoFileInfo fileInfo = new FileCryptoFileInfo();
        if (text.length > 0) {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(text);
            try {
                OpenXmlInput.getFileInfo(inputStream, fileInfo);
            } catch (IonicException e) {
                logger.fine(e.getMessage()); // if file cannot be parsed as Ionic cipherText, default to plainText
            }
        }
        return fileInfo;
    }

    @Override
    protected byte[] encryptInternal(final byte[] plainText,
                                     final FileCryptoEncryptAttributes attributes) throws IonicException {
        SdkData.checkTrue((plainText != null), SdkError.ISFILECRYPTO_NULL_INPUT, byte[].class.getName());
        SdkData.checkTrue((plainText.length > 0), SdkError.ISFILECRYPTO_EOF);
        SdkData.checkTrue((attributes != null), SdkError.ISFILECRYPTO_NULL_INPUT,
                FileCryptoEncryptAttributes.class.getName());

        final ByteArrayInputStream is = new ByteArrayInputStream(plainText);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        final boolean isVersion10 = FileCipher.OpenXml.V10.LABEL.equals(attributes.getVersion());
        FileType fileType = FileType.FILETYPE_UNKNOWN;
        byte[] customPropFile = null;
        if (!isVersion10) {         // Version 1.0 doesn't care what the version is, so leave it unknown.
            final Tuple<FileType, byte[]> typeAndProps = OpenXmlUtils.doFirstPassThrough(is,
                                                        attributes.getShouldCopyCustomProps());
            fileType = typeAndProps.first();
            customPropFile = typeAndProps.second();
            is.reset();
        }
        encryptInternal(is, bos, attributes, fileType, customPropFile, null);
        return bos.toByteArray();
    }

    @Override
    protected byte[] decryptInternal(final byte[] cipherText,
                                     final FileCryptoDecryptAttributes attributes) throws IonicException {
        SdkData.checkTrue((cipherText != null), SdkError.ISFILECRYPTO_NULL_INPUT);
        SdkData.checkTrue((cipherText.length > 0), SdkError.ISFILECRYPTO_BAD_INPUT);
        SdkData.checkTrue((attributes != null), SdkError.ISFILECRYPTO_NULL_INPUT,
                FileCryptoDecryptAttributes.class.getName());
        SdkData.checkTrue(attributes.getKeyId().isEmpty(), SdkError.ISFILECRYPTO_INVALIDVALUE);
        SdkData.checkTrue(attributes.getFamily() == CipherFamily.FAMILY_UNKNOWN, SdkError.ISFILECRYPTO_INVALIDVALUE);
        SdkData.checkTrue(attributes.getVersion().isEmpty(), SdkError.ISFILECRYPTO_INVALIDVALUE);

        ByteArrayInputStream is = new ByteArrayInputStream(cipherText);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream os = new BufferedOutputStream(bos);
        decryptInternal(is, os, attributes);

        byte[] decryptOutput = bos.toByteArray();

        final OpenXmlPortionMarkInput portionMark = new OpenXmlPortionMarkInput(getServices(), attributes);
        is = new ByteArrayInputStream(decryptOutput);
        if (portionMark.findPortionMarkedSections(is)) {

            bos = new ByteArrayOutputStream();
            os = new BufferedOutputStream(bos);
            is.reset();

            portionMark.decryptPortionMarkedSections(is, os);
            decryptOutput = bos.toByteArray();
        }
        return decryptOutput;
    }

    @Override
    protected void encryptInternal(final File sourceFile, final File targetFile,
                                   final FileCryptoEncryptAttributes attributes) throws IonicException {
        SdkData.checkTrue((attributes != null), SdkError.ISFILECRYPTO_NULL_INPUT,
                FileCryptoEncryptAttributes.class.getName());
        final String version = attributes.getVersion();
        final boolean versionOK = (SUPPORTED_VERSIONS.contains(version) || Value.isEmpty(version));
        SdkData.checkTrue(versionOK, SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);

        final boolean isVersion10 = FileCipher.OpenXml.V10.LABEL.equals(attributes.getVersion());
        FileType fileType = FileType.FILETYPE_UNKNOWN;
        byte[] customPropFile = null;
        if (!isVersion10) {         // Version 1.0 doesn't care what the version is, so leave it unknown.
            final Tuple<FileType, byte[]> typeAndProps = OpenXmlUtils.doFirstPassThrough(sourceFile,
                                                        attributes.getShouldCopyCustomProps());
            fileType = typeAndProps.first();
            customPropFile = typeAndProps.second();
        }
        final File tempFile = FileCipherUtils.generateTempFile(sourceFile);
        try {
            encryptInternal(sourceFile, targetFile, attributes, fileType, customPropFile, tempFile);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        }
        try {
            Files.deleteIfExists(tempFile.toPath());
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        }
    }

    /**
     * Encrypts an input file into an output file.
     *
     * @param sourceFile     the input file
     * @param targetFile     the output file
     * @param attributes     the attributes to be used in the context of the encrypt operation
     * @param fileType       the specific OpenXML file type discovered in an early step
     * @param customPropFile an optional custom properties file to include in the coverpage file
     * @param tempFile       an optional filename to use as a temp file during the process
     *                       if this param is null, the process will use RAM buffer.
     * @throws IOException    on stream read / write failures
     * @throws IonicException on cryptography failures
     */
    private void encryptInternal(final File sourceFile, final File targetFile,
                                 final FileCryptoEncryptAttributes attributes, final FileType fileType,
                                 final byte[] customPropFile, final File tempFile) throws IonicException, IOException {
        final FileInputStream is = new FileInputStream(sourceFile);
        try {
            encryptInternal(is, targetFile, attributes, fileType, customPropFile, tempFile);
        } finally {
            is.close();
        }
    }

    /**
     * Encrypts an input stream into an output file.
     *
     * @param is             the input stream
     * @param targetFile     the output file
     * @param attributes     the attributes to be used in the context of the encrypt operation
     * @param fileType       the specific OpenXML file type discovered in an early step
     * @param customPropFile an optional custom properties file to include in the coverpage file
     * @param tempFile       an optional filename to use as a temp file during the process
     *                       if this param is null, the process will use RAM buffer.
     * @throws IOException    on stream read / write failures
     * @throws IonicException on cryptography failures
     */
    private void encryptInternal(final InputStream is, final File targetFile,
                                 final FileCryptoEncryptAttributes attributes, final FileType fileType,
                                 final byte[] customPropFile, final File tempFile) throws IonicException, IOException {
        final FileOutputStream os = new FileOutputStream(targetFile);
        try {
            encryptInternal(is, os, attributes, fileType, customPropFile, tempFile);
        } finally {
            os.close();
        }
    }

    @Override
    protected void decryptInternal(final File sourceFile, final File targetFile,
                                   final FileCryptoDecryptAttributes attributes) throws IonicException {
        SdkData.checkTrue((attributes != null), SdkError.ISFILECRYPTO_NULL_INPUT,
                FileCryptoDecryptAttributes.class.getName());
        SdkData.checkTrue(attributes.getKeyId().isEmpty(), SdkError.ISFILECRYPTO_INVALIDVALUE);
        SdkData.checkTrue(attributes.getFamily() == CipherFamily.FAMILY_UNKNOWN, SdkError.ISFILECRYPTO_INVALIDVALUE);
        SdkData.checkTrue(attributes.getVersion().isEmpty(), SdkError.ISFILECRYPTO_INVALIDVALUE);
        SdkData.checkTrue(sourceFile.exists(), SdkError.ISFILECRYPTO_RESOURCE_NOT_FOUND);


        try {
            // working around "spotbugs" issue; until it is fixed, avoid "try with resources"
            // https://github.com/spotbugs/spotbugs/issues/293
            // https://github.com/spotbugs/spotbugs/issues/493
            final FileInputStream isSourceFile = new FileInputStream(sourceFile);
            try {
                try (FileOutputStream osTargetFile = new FileOutputStream(targetFile)) {
                    decryptInternal(isSourceFile, new BufferedOutputStream(osTargetFile), attributes);
                }
            } finally {
                isSourceFile.close();
            }
            // Portion Marking post process:
            final OpenXmlPortionMarkInput portionMark = new OpenXmlPortionMarkInput(getServices(), attributes);
            boolean foundPortions = false;
            // working around "spotbugs" issue; until it is fixed, avoid "try with resources"
            // https://github.com/spotbugs/spotbugs/issues/293
            // https://github.com/spotbugs/spotbugs/issues/493
            final FileInputStream isTargetFile = new FileInputStream(targetFile);
            try {
                foundPortions = portionMark.findPortionMarkedSections(isTargetFile);
            } finally {
                isTargetFile.close();
            }
            if (foundPortions) {
                boolean decrypted = false;
                final File tempFile = FileCipherUtils.generateTempFile(targetFile);
                final FileInputStream isTargetFile2 = new FileInputStream(targetFile);
                try {
                    try (OutputStream osTempFile = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                        decrypted = portionMark.decryptPortionMarkedSections(isTargetFile2, osTempFile);
                    }
                } finally {
                    isTargetFile2.close();
                }
                if (decrypted) {
                    FileCipherUtils.renameFile(tempFile, targetFile);
                } else {
                    Files.delete(tempFile.toPath());
                }
            }
        } catch (FileNotFoundException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_IOSTREAM_ERROR, e);
        }
    }

    /**
     * Common utility function for implementing encryption of various stream formats.
     *
     * @param plainText         the input stream presenting the binary plain text input buffer
     * @param cipherText        the output stream presenting the binary cipher text output buffer
     * @param encryptAttributes the attributes to be used in the context of the encrypt operation
     * @param fileType          the speific OpenXML file type discovered in an early step
     * @param customPropFile    an optional custom properties file to include in the coverpage file
     * @param tempFile          an optional filename to use as a temp file during the process
     *                          if this param is null, the process will use RAM buffer.
     * @return internal state associated with the encryption operation; this is needed to insert
     * the file signature after the stream is written
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    private OpenXmlOutput encryptInternal(final InputStream plainText, final OutputStream cipherText,
                                          final FileCryptoEncryptAttributes encryptAttributes,
                                          final FileType fileType,
                                          final byte[] customPropFile,
                                          final File tempFile) throws IonicException {
        try {
            final OpenXmlOutput ionicOutput = new OpenXmlOutput(cipherText,
                                                                getServices(),
                                                                getCoverPageServices(),
                                                                fileType,
                                                                customPropFile,
                                                                tempFile);
            ionicOutput.init(encryptAttributes);
            ionicOutput.doEncryption(plainText);
            cipherText.flush();
            return ionicOutput;
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_STREAM_WRITE, e);
        }
    }

    /**
     * Common utility function for implementing decryption of various stream formats.
     *
     * @param cipherText the input stream presenting the binary cipher text input buffer
     * @param plainText  the output stream presenting the binary plain text output buffer
     * @param attributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    private void decryptInternal(final InputStream cipherText, final OutputStream plainText,
                                 final FileCryptoDecryptAttributes attributes) throws IonicException {
        final FileCryptoFileInfo fileInfo = new FileCryptoFileInfo();
        try {
            final WritableByteChannel plainChannel = Channels.newChannel(plainText);
            final OpenXmlInput ionicInput = new OpenXmlInput(cipherText, getServices());
            ionicInput.init(fileInfo, attributes);
            while (ionicInput.available() > 0) {
                plainChannel.write(ionicInput.read());
            }
            ionicInput.doFinal();
            plainText.flush();
        } catch (IonicException e) {

            // Need to handle access denied in a very custom manner as we need to determine the specific
            // type of OpenXML file at this point and our v10 files do not support cover pages at all.
            final boolean isAcccessDenied = SdkError.ISAGENT_KEY_DENIED == e.getReturnCode();
            final boolean shouldProvidePage = attributes.shouldProvideAccessDeniedPage();
            if (isAcccessDenied && shouldProvidePage
                && fileInfo.getCipherVersion().equals(FileCipher.OpenXml.V11.LABEL)) {
                final FileType fileType = getDecryptingFileType(cipherText);
                attributes.setAccessDeniedPageOut(getCoverPageServices().getAccessDeniedPage(fileType));
            }
            throw e;

        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_BAD_ZIP, e);
        }
    }

    /**
     * Retrieves the specific file type (doc, xls, ppt) for an Access Deined cover page from a failed decrytion.
     * @param cipherText Stream to a failed decryption attempt (function will reset position)
     * @return The FileType or FILETYPE_UNKNOWN on any exception
     */
    private FileType getDecryptingFileType(final InputStream cipherText) {
        try {
            // sourceStream should be a FileInputStream or a ByteArrayInputStream
            // The first can be reset by its channel, the second will reset with reset()
            // Any other inputStream type will attempt reset() and throw an exception if it can't.
            try {
                final FileInputStream fs = (FileInputStream) cipherText;
                fs.getChannel().position(0);
            } catch (ClassCastException e) {
                cipherText.reset();
            }
            final Tuple<FileType, byte[]> fileTypeTuple = OpenXmlUtils.doFirstPassThrough(cipherText, false);
            return fileTypeTuple.first();
        } catch (IOException e) {
            return FileType.FILETYPE_UNKNOWN;
        } catch (IonicException e) {
            return FileType.FILETYPE_UNKNOWN;
        }
    }
}
