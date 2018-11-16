package com.ionic.sdk.agent.cipher.file;

import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesDefault;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesInterface;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.agent.cipher.file.family.generic.input.GenericInput;
import com.ionic.sdk.agent.cipher.file.family.generic.output.GenericOutput;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Cipher that supports generic file encryption / decryption.
 */
public final class GenericFileCipher extends FileCipherAbstract {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * File format family generic.
     */
    public static final String FAMILY = FileCipher.Generic.FAMILY;

    /**
     * File format family generic, version 1.1.
     */
    public static final String VERSION_1_1 = FileCipher.Generic.V11.LABEL;

    /**
     * File format family generic, version 1.2.
     */
    public static final String VERSION_1_2 = FileCipher.Generic.V12.LABEL;

    /**
     * File format family generic, latest version.
     */
    public static final String VERSION_LATEST = FileCipher.Generic.V12.LABEL;

    /**
     * Constructor.
     *
     * @param agent the key services implementation; used to provide keys for cryptography operations
     */
    public GenericFileCipher(final KeyServices agent) {
        super(agent, new FileCryptoCoverPageServicesDefault());
    }

    /**
     * Constructor.
     *
     * @param agent             the key services implementation; used to provide keys for cryptography operations
     * @param coverPageServices the cover page services implementation
     */
    public GenericFileCipher(final KeyServices agent, final FileCryptoCoverPageServicesInterface coverPageServices) {
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
        return CipherFamily.FAMILY_GENERIC;
    }

    @Override
    public List<String> getVersions() {
        return new ArrayList<String>(SUPPORTED_VERSIONS);
    }

    @Override
    public boolean isVersionSupported(final String version) {
        return SUPPORTED_VERSIONS.contains(version);
    }

    /**
     * The versions of the encryption format supported by this FileCipher implementation.
     */
    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList(VERSION_1_1, VERSION_1_2);

    @Override
    public FileCryptoFileInfo getFileInfo(final String filePath) throws IonicException {
        final FileCryptoFileInfo fileInfo = new FileCryptoFileInfo();
        final File file = new File(filePath);
        if (file.exists() && file.length() > 0) {
            try {
                try (FileInputStream is = new FileInputStream(file)) {
                    final GenericInput genericInput = new GenericInput(is, getServices());
                    final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
                    try {
                        genericInput.init(fileInfo, decryptAttributes);

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
            final GenericInput genericInput = new GenericInput(inputStream, null);
            final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
            try {
                genericInput.init(fileInfo, decryptAttributes);

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

        final BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(plainText));
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final BufferedOutputStream os = new BufferedOutputStream(bos);
        final GenericOutput ionicOutput = encryptInternal(is, os, attributes);
        final byte[] bytes = bos.toByteArray();
        final byte[] signature = ionicOutput.getSignature();
        if (signature != null) {
            System.arraycopy(signature, 0, bytes, ionicOutput.getHeaderLength(), signature.length);
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
        decryptInternal(is, os, attributes);
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
            encryptInternal3(is, targetFile, attributes);
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
     * @param targetFile the output file
     * @param attributes the attributes to be used in the context of the encrypt operation
     * @throws IonicException on cryptography failures
     * @throws IOException    on stream read / write failures
     */
    private void encryptInternal3(final InputStream is, final File targetFile,
                                  final FileCryptoEncryptAttributes attributes) throws IonicException, IOException {
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            final BufferedOutputStream os = new BufferedOutputStream(fos);
            final GenericOutput ionicOutput = encryptInternal(is, os, attributes);
            final byte[] signature = ionicOutput.getSignature();
            if (signature != null) {
                fos.getChannel().position(ionicOutput.getHeaderLength());
                fos.write(signature);
            }
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
            decryptInternal3(is, targetFile, attributes);
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
     * @param targetFile the output file
     * @param attributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on cryptography failures
     * @throws IOException    on stream read / write failures
     */
    private void decryptInternal3(final InputStream is, final File targetFile,
                                  final FileCryptoDecryptAttributes attributes) throws IonicException, IOException {
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            final BufferedOutputStream os = new BufferedOutputStream(fos);
            decryptInternal(is, os, attributes);
            os.close();
        }
    }


    /**
     * Common utility function for implementing encryption of various stream formats.
     *
     * @param plainText  the input stream presenting the binary plain text input buffer
     * @param cipherText the output stream presenting the binary cipher text output buffer
     * @param attributes the attributes to be used in the context of the encrypt operation
     * @return internal state associated with the encryption operation; this is needed for v1.2 to insert
     * the file signature after the stream is written
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    private GenericOutput encryptInternal(final InputStream plainText, final OutputStream cipherText,
                                          final FileCryptoEncryptAttributes attributes) throws IonicException {
        try {
            final GenericOutput ionicOutput = new GenericOutput(cipherText, getServices());
            ionicOutput.init(attributes);
            final byte[] block = new byte[ionicOutput.getBlockLengthPlain()];
            while (plainText.available() > 0) {
                final int length = plainText.read(block);
                ionicOutput.write(Arrays.copyOf(block, length));
            }
            ionicOutput.doFinal();
            return ionicOutput;
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_STREAM_WRITE, e);
        }
    }

    /**
     * Common utility function for implementing decryption of various stream formats.
     *
     * @param cipherText        the input stream presenting the binary cipher text input buffer
     * @param plainText         the output stream presenting the binary plain text output buffer
     * @param decryptAttributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    private void decryptInternal(final InputStream cipherText, final OutputStream plainText,
                                 final FileCryptoDecryptAttributes decryptAttributes) throws IonicException {
        try {
            final GenericInput ionicInput = new GenericInput(cipherText, getServices());
            final FileCryptoFileInfo fileInfo = new FileCryptoFileInfo();
            ionicInput.init(fileInfo, decryptAttributes);
            while (ionicInput.available() > 0) {
                plainText.write(ionicInput.read());
            }
            ionicInput.doFinal();
            plainText.flush();
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_STREAM_WRITE, e);
        }
    }
}
