package com.ionic.sdk.agent.cipher.file;

import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesDefault;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesInterface;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.agent.cipher.file.family.csv.input.CsvInput;
import com.ionic.sdk.agent.cipher.file.family.csv.output.CsvOutput;
import com.ionic.sdk.agent.cipher.file.family.csv.sig.CsvSignerBytes;
import com.ionic.sdk.agent.cipher.file.family.csv.sig.CsvSignerFile;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Ionic Machina Tools CSV (comma-separated variable) file crypto implementation.  This object can be used to perform
 * encryption and decryption operations on filesystem files that are in the CSV format.
 * <p>
 * {@link CsvFileCipher} provides APIs to perform file encryption on filesystem files, and also on in-memory
 * byte arrays.
 * <p>
 * Sample (byte[] API):
 * <pre>
 * public final void testFileCipherCsv_EncryptDecryptBytes() throws IonicException {
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final byte[] plainText = Transcoder.utf8().decode("a,b,c\n1,2,3\n");
 *     final FileCipherAbstract fileCipher = new CsvFileCipher(keyServices);
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
 * public final void testFileCipherCsv_EncryptDecryptFile() throws IonicException {
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final byte[] plainText = Transcoder.utf8().decode("a,b,c\n1,2,3\n");
 *     final File filePlainText = new File("csv.plainText.txt");
 *     final File fileCipherText = new File("csv.cipherText.txt");
 *     final File filePlainTextRecover = new File("csv.plainText.recover.txt");
 *     DeviceUtils.write(filePlainText, plainText);
 *     final FileCipherAbstract fileCipher = new CsvFileCipher(keyServices);
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
 * See <a href='https://dev.ionic.com/sdk/formats/file-crypto-csv' target='_blank'>Machina Developers</a> for
 * more information on the different file crypto data formats.
 */
public final class CsvFileCipher extends FileCipherAbstract {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * File format family CSV (comma-separated variable).
     */
    public static final String FAMILY = FileCipher.Csv.FAMILY;

    /**
     * File format family CSV (comma-separated variable), version 1.0.
     */
    public static final String VERSION_1_0 = "1.0";

    /**
     * File format family CSV (comma-separated variable), latest version.
     */
    public static final String VERSION_LATEST = VERSION_1_0;

    /**
     * Constructor.
     *
     * @param agent the key services implementation; used to provide keys for cryptography operations
     */
    public CsvFileCipher(final KeyServices agent) {
        super(agent, new FileCryptoCoverPageServicesDefault());
    }

    /**
     * Constructor.
     *
     * @param agent             the key services implementation; used to provide keys for cryptography operations
     * @param coverPageServices the cover page services implementation
     */
    public CsvFileCipher(final KeyServices agent, final FileCryptoCoverPageServicesInterface coverPageServices) {
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
        return CipherFamily.FAMILY_CSV;
    }

    @Override
    public List<String> getVersions() {
        return new ArrayList<String>(SUPPORTED_VERSIONS);
    }

    @Override
    public String getDefaultVersion() {
        return VERSION_1_0;
    }

    @Override
    public boolean isVersionSupported(final String version) {
        return SUPPORTED_VERSIONS.contains(version);
    }

    /**
     * The versions of the encryption format supported by this FileCipher implementation.
     */
    private static final List<String> SUPPORTED_VERSIONS = Collections.singletonList(VERSION_1_0);

    @Override
    public FileCryptoFileInfo getFileInfo(final String filePath) throws IonicException {
        final FileCryptoFileInfo fileInfo = new FileCryptoFileInfo();
        final File file = new File(filePath);
        final long length = file.length();
        if (file.exists() && file.length() > 0) {
            try {
                try (FileInputStream is = new FileInputStream(file)) {
                    final CsvInput csvInput = new CsvInput(is, length, getServices());
                    final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
                    try {
                        csvInput.init(fileInfo, decryptAttributes);
                        csvInput.read();  // this "read()" call triggers consumption of first block of generic data
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
            final CsvInput csvInput = new CsvInput(inputStream, text.length, null);
            final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
            try {
                csvInput.init(fileInfo, decryptAttributes);
                csvInput.read();  // this "read()" call triggers the consumption of the first block of generic data
            } catch (IonicException e) {
                logger.fine(e.getMessage()); // if file cannot be parsed as Ionic cipherText, default to plainText
            } catch (IOException e) {
                throw new IonicException(SdkError.ISFILECRYPTO_EOF);
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

        final BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(plainText));
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);
        final CsvOutput ionicOutput = encryptInternal(is, plainText.length, dos, attributes);
        final byte[] bytes = bos.toByteArray();
        final byte[] signature = ionicOutput.getSignatureWrapped();
        if (signature != null) {
            final CsvSignerBytes signer = new CsvSignerBytes(bytes);
            signer.apply(signature, ionicOutput.getHeaderLengthCsv(), ionicOutput.getHeaderLengthWrapped());
        }
        return bytes;
    }

    @Override
    protected byte[] decryptInternal(final byte[] cipherText,
                                     final FileCryptoDecryptAttributes attributes) throws IonicException {
        SdkData.checkTrue((cipherText != null), SdkError.ISFILECRYPTO_NULL_INPUT);
        SdkData.checkTrue((cipherText.length > 0), SdkError.ISFILECRYPTO_NULL_INPUT);
        SdkData.checkTrue((attributes != null), SdkError.ISFILECRYPTO_NULL_INPUT,
                FileCryptoDecryptAttributes.class.getName());
        SdkData.checkTrue(attributes.getKeyId().isEmpty(), SdkError.ISFILECRYPTO_INVALIDVALUE);
        SdkData.checkTrue(attributes.getFamily() == CipherFamily.FAMILY_UNKNOWN, SdkError.ISFILECRYPTO_INVALIDVALUE);
        SdkData.checkTrue(attributes.getVersion().isEmpty(), SdkError.ISFILECRYPTO_INVALIDVALUE);
        final ByteArrayInputStream is = new ByteArrayInputStream(cipherText);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final BufferedOutputStream os = new BufferedOutputStream(bos);
        decryptInternal(is, cipherText.length, os, attributes);
        return bos.toByteArray();
    }

    @Override
    protected void encryptInternal(final File sourceFile, final File targetFile,
                                   final FileCryptoEncryptAttributes attributes) throws IonicException {
        SdkData.checkTrue((attributes != null), SdkError.ISFILECRYPTO_NULL_INPUT,
                FileCryptoEncryptAttributes.class.getName());
        final String version = attributes.getVersion();
        final boolean versionOK = (SUPPORTED_VERSIONS.contains(version) || Value.isEmpty(version));
        SdkData.checkTrue(versionOK, SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        try {
            encryptInternal2(sourceFile, targetFile, attributes);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        }
    }

    /**
     * Encrypts an input file into an output file.
     *
     * @param sourceFile the input file
     * @param targetFile the output file
     * @param attributes the attributes to be used in the context of the encrypt operation
     * @throws IonicException on cryptography failures
     * @throws IOException    on stream read / write failures
     */
    private void encryptInternal2(final File sourceFile, final File targetFile,
                                  final FileCryptoEncryptAttributes attributes) throws IonicException, IOException {
        final FileInputStream is = new FileInputStream(sourceFile);
        try {
            encryptInternal3(is, sourceFile.length(), targetFile, attributes);
        } finally {
            is.close();
        }
        // working around "spotbugs" issue; until it is fixed, avoid "try with resources"
        // https://github.com/spotbugs/spotbugs/issues/293
        // https://github.com/spotbugs/spotbugs/issues/493
/*
        try (FileInputStream is = new FileInputStream(sourceFile)) {
            encryptInternal3(is, targetFile, attributes);
        }
*/
    }

    /**
     * Encrypts an input stream into an output file.
     *
     * @param is         the input stream
     * @param sizeInput  the length of the resource to be encrypted
     * @param targetFile the output file
     * @param attributes the attributes to be used in the context of the encrypt operation
     * @throws IonicException on cryptography failures
     * @throws IOException    on stream read / write failures
     */
    private void encryptInternal3(final InputStream is, final long sizeInput, final File targetFile,
                                  final FileCryptoEncryptAttributes attributes) throws IonicException, IOException {
        final RandomAccessFile randomAccessFile = new RandomAccessFile(
                targetFile, FileCipher.Generic.OPEN_MODE_ENCRYPT);

        try (FileOutputStream fos = new FileOutputStream(randomAccessFile.getFD())) {
            final DataOutputStream dos = new DataOutputStream(fos);
            final CsvOutput ionicOutput = encryptInternal(is, sizeInput, dos, attributes);
            final byte[] signature = ionicOutput.getSignatureWrapped();
            if (signature != null) {
                final CsvSignerFile signer = new CsvSignerFile(randomAccessFile.getChannel());
                signer.apply(signature, ionicOutput.getHeaderLengthCsv(), ionicOutput.getHeaderLengthWrapped());
            }
            dos.close();
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
        try {
            decryptInternal2(sourceFile, targetFile, attributes);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        }
    }

    /**
     * Decrypts an input file into an output file.
     *
     * @param sourceFile the input file
     * @param targetFile the output file
     * @param attributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on cryptography failures
     * @throws IOException    on stream read / write failures
     */
    private void decryptInternal2(final File sourceFile, final File targetFile,
                                  final FileCryptoDecryptAttributes attributes) throws IonicException, IOException {
        final FileInputStream is = new FileInputStream(sourceFile);
        try {
            decryptInternal3(is, sourceFile.length(), targetFile, attributes);
        } finally {
            is.close();
        }
        // working around "spotbugs" issue; until it is fixed, avoid "try with resources"
        // https://github.com/spotbugs/spotbugs/issues/293
        // https://github.com/spotbugs/spotbugs/issues/493
/*
        try (FileInputStream is = new FileInputStream(sourceFile)) {
            decryptInternal3(is, targetFile, attributes);
        }
*/
    }

    /**
     * Decrypts an input stream into an output file.
     *
     * @param is         the input stream
     * @param sizeInput  the length of the resource to be decrypted
     * @param targetFile the output file
     * @param attributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on cryptography failures
     * @throws IOException    on stream read / write failures
     */
    private void decryptInternal3(final InputStream is, final long sizeInput, final File targetFile,
                                  final FileCryptoDecryptAttributes attributes) throws IonicException, IOException {
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            try (BufferedOutputStream os = new BufferedOutputStream(fos)) {
                decryptInternal(is, sizeInput, os, attributes);
            }
        }
    }

    /**
     * Common utility function for implementing encryption of various stream formats.
     *
     * @param plainText         the input stream presenting the binary plain text input buffer
     * @param sizeInput         the length of the resource to be encrypted
     * @param cipherText        the output stream presenting the binary cipher text output buffer
     * @param encryptAttributes the attributes to be used in the context of the encrypt operation
     * @return internal state associated with the encryption operation; this is needed to insert
     * the file signature after the stream is written
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    private CsvOutput encryptInternal(final InputStream plainText, final long sizeInput,
                                      final DataOutputStream cipherText,
                                      final FileCryptoEncryptAttributes encryptAttributes) throws IonicException {
        try {
            final ReadableByteChannel plainChannel = Channels.newChannel(plainText);
            final CsvOutput ionicOutput = new CsvOutput(cipherText, sizeInput, getServices(), getCoverPageServices());
            ionicOutput.init(encryptAttributes);
            final ByteBuffer bufferPlainText = ionicOutput.getPlainText();
            while (plainText.available() > 0) {
                bufferPlainText.clear();
                plainChannel.read(bufferPlainText);
                bufferPlainText.limit(bufferPlainText.position());
                bufferPlainText.position(0);
                ionicOutput.write(bufferPlainText);
            }
            ionicOutput.doFinal();
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
     * @param sizeInput  the length of the resource to be decrypted
     * @param plainText  the output stream presenting the binary plain text output buffer
     * @param attributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    private void decryptInternal(final InputStream cipherText, final long sizeInput, final OutputStream plainText,
                                 final FileCryptoDecryptAttributes attributes) throws IonicException {
        try {
            final WritableByteChannel plainChannel = Channels.newChannel(plainText);
            final CsvInput ionicInput = new CsvInput(cipherText, sizeInput, getServices());
            final FileCryptoFileInfo fileInfo = new FileCryptoFileInfo();
            ionicInput.init(fileInfo, attributes);
            while (ionicInput.available() > 0) {
                plainChannel.write(ionicInput.read());
            }
            ionicInput.doFinal();
            plainText.flush();
        } catch (IonicException e) {
            handleFileCryptoException(e, FileType.FILETYPE_CSV, attributes);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_STREAM_WRITE, e);
        }
    }
}
