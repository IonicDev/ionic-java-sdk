package com.ionic.sdk.agent.cipher.file;

import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesDefault;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesInterface;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipherUtils;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.File;
import java.util.List;

/**
 * Ionic Machina Tools file crypto abstract implementation.  Subclasses of FileCipherAbstract implement the
 * different Machina file data formats.
 * <p>
 * See <a href='https://dev.ionic.com/sdk/features' target='_blank'>Machina Developers</a> for more information
 * on the different file crypto data formats.
 */
public abstract class FileCipherAbstract {

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private KeyServices agent;

    /**
     * Cover page services implementation; used to substitute cover pages to display on failure to access crypto key.
     */
    private FileCryptoCoverPageServicesInterface coverPageServices;

    /**
     * Constructor.
     *
     * @param agent the key services implementation; used to provide keys for cryptography operations
     */
    public FileCipherAbstract(final KeyServices agent) {
        this(agent, new FileCryptoCoverPageServicesDefault());
    }

    /**
     * Constructor.
     *
     * @param agent             the key services implementation; used to provide keys for cryptography operations
     * @param coverPageServices the cover page services implementation
     */
    public FileCipherAbstract(final KeyServices agent, final FileCryptoCoverPageServicesInterface coverPageServices) {
        this.agent = agent;
        this.coverPageServices = coverPageServices;
    }

    /**
     * @return the key services implementation
     */
    public KeyServices getServices() {
        return agent;
    }

    /**
     * @return the cover page services implementation
     */
    public FileCryptoCoverPageServicesInterface getCoverPageServices() {
        return coverPageServices;
    }

    /**
     * @return the file cipher format family label
     * @deprecated Please migrate usages to the replacement {@link #getFamilyString()} method.
     */
    @Deprecated
    public abstract String getFamily();

    /**
     * @return the file cipher format family label
     */
    public abstract String getFamilyString();

    /**
     * @return the file cipher format family
     */
    public abstract CipherFamily getCipherFamily();

    /**
     * @return the versions of the encryption format supported by this FileCipher implementation
     */
    public abstract List<String> getVersions();

    /**
     * @return the default version of the encryption format supported by this FileCipher implementation
     */
    public abstract String getDefaultVersion();

    /**
     * @param version the cipher family version to be checked for support
     * @return true, iff the specified version of the encryption format is supported by this implementation
     */
    public abstract boolean isVersionSupported(String version);

    /**
     * Parse the first bytes of a file, to determine whether it is Ionic encrypted, its format, and other
     * interesting metadata about it.
     *
     * @param filePath the path of the file to be examined
     * @return a {@link FileCryptoFileInfo} object containing metadata about the Ionic state of the file
     * @throws IonicException on data read / write failures
     */
    public abstract FileCryptoFileInfo getFileInfo(String filePath) throws IonicException;

    /**
     * Parse the first bytes of a file, to determine whether it is Ionic encrypted, its format, and other
     * interesting metadata about it.
     *
     * @param text the first bytes of file data
     * @return a {@link FileCryptoFileInfo} object containing metadata about the Ionic state of the file
     * @throws IonicException on data read / write failures
     */
    public abstract FileCryptoFileInfo getFileInfo(byte[] text) throws IonicException;

    /**
     * Encrypts an input byte buffer into an Ionic-encrypted output byte buffer.
     * <p>
     * This API operates on the entire input byte buffer, and returns the entire output buffer.  The memory settings
     * of the in-use JVM should be tuned to accommodate very large byte buffers, if needed.
     * <p>
     * See also:
     * <a href='https://docs.oracle.com/cd/E21764_01/web.1111/e13814/jvm_tuning.htm'
     * target='_blank'>Tuning Java Virtual Machines</a>
     *
     * @param plainText  the binary plain text input buffer
     * @param attributes the attributes to be used in the context of the encrypt operation
     * @return the binary cipher text output buffer
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public byte[] encrypt(final byte[] plainText,
                          final FileCryptoEncryptAttributes attributes) throws IonicException {
        return encryptInternal(plainText, attributes);
    }

    /**
     * Encrypts an input byte buffer into an Ionic-encrypted output byte buffer.
     * <p>
     * This API operates on the entire input byte buffer, and returns the entire output buffer.  The memory settings
     * of the in-use JVM should be tuned to accommodate very large byte buffers, if needed.
     * <p>
     * See also:
     * <a href='https://docs.oracle.com/cd/E21764_01/web.1111/e13814/jvm_tuning.htm'
     * target='_blank'>Tuning Java Virtual Machines</a>
     *
     * @param plainText the binary plain text input buffer
     * @return the binary cipher text output buffer
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public byte[] encrypt(final byte[] plainText) throws IonicException {
        return encryptInternal(plainText, new FileCryptoEncryptAttributes());
    }

    /**
     * Encrypts an input file into an output file.
     * <p>
     * This API uses a block cipher encoding scheme with the following block sizes:
     * <ul>
     * <li>v1.1 block size is 8,192 bytes</li>
     * <li>v1.2 block size is 10,000,000 bytes</li>
     * </ul>
     * <p>
     * As these sizes are within the bounds of the default JVM memory allocation, there should be no need to
     * tune the memory settings of the in-use JVM.
     *
     * @param sourcePath the input file path
     * @param targetPath the output file path
     * @param attributes the attributes to be used in the context of the encrypt operation
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public void encrypt(final String sourcePath, final String targetPath,
                        final FileCryptoEncryptAttributes attributes) throws IonicException {
        SdkData.checkTrue((sourcePath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        SdkData.checkTrue((targetPath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        encryptInternal(new File(sourcePath), new File(targetPath), attributes);
    }

    /**
     * Encrypts an input file into an output file.
     * <p>
     * This API uses a block cipher encoding scheme with the following block sizes:
     * <ul>
     * <li>v1.1 block size is 8,192 bytes</li>
     * <li>v1.2 block size is 10,000,000 bytes</li>
     * </ul>
     * <p>
     * As these sizes are within the bounds of the default JVM memory allocation, there should be no need to
     * tune the memory settings of the in-use JVM.
     *
     * @param sourcePath the input file path
     * @param targetPath the output file path
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public void encrypt(final String sourcePath, final String targetPath) throws IonicException {
        SdkData.checkTrue((sourcePath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        SdkData.checkTrue((targetPath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        encryptInternal(new File(sourcePath), new File(targetPath), new FileCryptoEncryptAttributes());
    }

    /**
     * Encrypts an input file in place.
     * <p>
     * This function performs in-place file encryption, which means that the file specified by the filePath
     * parameter will be overwritten with the resulting encrypted ciphertext.  If encryption fails, it is
     * guaranteed that the original plaintext file will not be modified in any way.
     * <p>
     * You must have write access to the file specified by the filePath parameter.
     * <p>
     * This API uses a block cipher encoding scheme with the following block sizes:
     * <ul>
     * <li>v1.1 block size is 8,192 bytes</li>
     * <li>v1.2 block size is 10,000,000 bytes</li>
     * </ul>
     * <p>
     * As these sizes are within the bounds of the default JVM memory allocation, there should be no need to
     * tune the memory settings of the in-use JVM.
     *
     * @param filePath   the path of the file to be encrypted in place
     * @param attributes the attributes to be used in the context of the encrypt operation
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public void encrypt(final String filePath,
                        final FileCryptoEncryptAttributes attributes) throws IonicException {
        SdkData.checkTrue((filePath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        final File originalFile = new File(filePath);
        final File tempFile = FileCipherUtils.generateTempFile(originalFile);
        encryptInternal(originalFile, tempFile, attributes);
        FileCipherUtils.renameFile(tempFile, originalFile);
    }

    /**
     * Encrypts an input file in place.
     * <p>
     * This function performs in-place file encryption, which means that the file specified by the filePath
     * parameter will be overwritten with the resulting encrypted ciphertext.  If encryption fails, it is
     * guaranteed that the original plaintext file will not be modified in any way.
     * <p>
     * You must have write access to the file specified by the filePath parameter.
     * <p>
     * This API uses a block cipher encoding scheme with the following block sizes:
     * <ul>
     * <li>v1.1 block size is 8,192 bytes</li>
     * <li>v1.2 block size is 10,000,000 bytes</li>
     * </ul>
     * <p>
     * As these sizes are within the bounds of the default JVM memory allocation, there should be no need to
     * tune the memory settings of the in-use JVM.
     *
     * @param filePath the path of the file to be encrypted in place
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public void encrypt(final String filePath) throws IonicException {
        SdkData.checkTrue((filePath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        final File originalFile = new File(filePath);
        final File tempFile = FileCipherUtils.generateTempFile(originalFile);
        encryptInternal(originalFile, tempFile, new FileCryptoEncryptAttributes());
        FileCipherUtils.renameFile(tempFile, originalFile);
    }

    /**
     * Decrypts an Ionic-encrypted input byte buffer into an output byte buffer.
     * <p>
     * This API operates on the entire input byte buffer, and returns the entire output buffer.  The memory settings
     * of the in-use JVM should be tuned to accommodate very large byte buffers, if needed.
     * <p>
     * See also:
     * <a href='https://docs.oracle.com/cd/E21764_01/web.1111/e13814/jvm_tuning.htm'
     * target='_blank'>Tuning Java Virtual Machines</a>
     *
     * @param cipherText the binary cipher text input buffer
     * @param attributes the attributes used in the context of the decrypt operation
     * @return the binary plain text output buffer
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public byte[] decrypt(final byte[] cipherText,
                          final FileCryptoDecryptAttributes attributes) throws IonicException {
        return decryptInternal(cipherText, attributes);
    }

    /**
     * Decrypts an Ionic-encrypted input byte buffer into an output byte buffer.
     * <p>
     * This API operates on the entire input byte buffer, and returns the entire output buffer.  The memory settings
     * of the in-use JVM should be tuned to accommodate very large byte buffers, if needed.
     * <p>
     * See also:
     * <a href='https://docs.oracle.com/cd/E21764_01/web.1111/e13814/jvm_tuning.htm'
     * target='_blank'>Tuning Java Virtual Machines</a>
     *
     * @param cipherText the binary cipher text input buffer
     * @return the binary plain text output buffer
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public byte[] decrypt(final byte[] cipherText) throws IonicException {
        return decryptInternal(cipherText, new FileCryptoDecryptAttributes());
    }

    /**
     * Decrypts an input file into an output file.
     * <p>
     * This API uses a block cipher encoding scheme with the following block sizes:
     * <ul>
     * <li>v1.1 block size is 8,192 bytes</li>
     * <li>v1.2 block size is 10,000,000 bytes</li>
     * </ul>
     * <p>
     * As these sizes are within the bounds of the default JVM memory allocation, there should be no need to
     * tune the memory settings of the in-use JVM.
     *
     * @param sourcePath the input file path
     * @param targetPath the output file path
     * @param attributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public void decrypt(final String sourcePath, final String targetPath,
                        final FileCryptoDecryptAttributes attributes) throws IonicException {
        SdkData.checkTrue((sourcePath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        SdkData.checkTrue((targetPath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        decryptInternal(new File(sourcePath), new File(targetPath), attributes);
    }

    /**
     * Decrypts an input file into an output file.
     * <p>
     * This API uses a block cipher encoding scheme with the following block sizes:
     * <ul>
     * <li>v1.1 block size is 8,192 bytes</li>
     * <li>v1.2 block size is 10,000,000 bytes</li>
     * </ul>
     * <p>
     * As these sizes are within the bounds of the default JVM memory allocation, there should be no need to
     * tune the memory settings of the in-use JVM.
     *
     * @param sourcePath the input file path
     * @param targetPath the output file path
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public void decrypt(final String sourcePath, final String targetPath) throws IonicException {
        SdkData.checkTrue((sourcePath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        SdkData.checkTrue((targetPath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        decryptInternal(new File(sourcePath), new File(targetPath), new FileCryptoDecryptAttributes());
    }

    /**
     * Decrypts an input file in place.
     * <p>
     * This function performs in-place file decryption, which means that the file specified by the filePath
     * parameter will be overwritten with the resulting decrypted plaintext.  If decryption fails, it is
     * guaranteed that the original ciphertext file will not be modified in any way.
     * <p>
     * You must have write access to the file specified by the filePath parameter.
     * <p>
     * This API uses a block cipher encoding scheme with the following block sizes:
     * <ul>
     * <li>v1.1 block size is 8,192 bytes</li>
     * <li>v1.2 block size is 10,000,000 bytes</li>
     * </ul>
     * <p>
     * As these sizes are within the bounds of the default JVM memory allocation, there should be no need to
     * tune the memory settings of the in-use JVM.
     *
     * @param filePath   the path of the file to be decrypted in place
     * @param attributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public void decrypt(final String filePath,
                        final FileCryptoDecryptAttributes attributes) throws IonicException {
        SdkData.checkTrue((filePath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        final File originalFile = new File(filePath);
        final File tempFile = FileCipherUtils.generateTempFile(originalFile);
        decryptInternal(originalFile, tempFile, attributes);
        FileCipherUtils.renameFile(tempFile, originalFile);
    }

    /**
     * Decrypts an input file in place.
     * <p>
     * This function performs in-place file decryption, which means that the file specified by the filePath
     * parameter will be overwritten with the resulting decrypted plaintext.  If decryption fails, it is
     * guaranteed that the original ciphertext file will not be modified in any way.
     * <p>
     * You must have write access to the file specified by the filePath parameter.
     * <p>
     * This API uses a block cipher encoding scheme with the following block sizes:
     * <ul>
     * <li>v1.1 block size is 8,192 bytes</li>
     * <li>v1.2 block size is 10,000,000 bytes</li>
     * </ul>
     * <p>
     * As these sizes are within the bounds of the default JVM memory allocation, there should be no need to
     * tune the memory settings of the in-use JVM.
     *
     * @param filePath the path of the file to be decrypted in place
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    public void decrypt(final String filePath) throws IonicException {
        SdkData.checkTrue((filePath != null), SdkError.ISFILECRYPTO_NULL_INPUT, File.class.getName());
        final File originalFile = new File(filePath);
        final File tempFile = FileCipherUtils.generateTempFile(originalFile);
        decryptInternal(originalFile, tempFile, new FileCryptoDecryptAttributes());
        FileCipherUtils.renameFile(tempFile, originalFile);
    }

    /**
     * Encrypts an input byte buffer into an Ionic-encrypted output byte buffer.
     * <p>
     * This API operates on the entire input byte buffer, and returns the entire output buffer.  The memory settings
     * of the in-use JVM should be tuned to accommodate very large byte buffers, if needed.
     * <p>
     * See also:
     * <a href='https://docs.oracle.com/cd/E21764_01/web.1111/e13814/jvm_tuning.htm'
     * target='_blank'>Tuning Java Virtual Machines</a>
     *
     * @param plainText  the binary plain text input buffer
     * @param attributes the attributes to be used in the context of the encrypt operation
     * @return the binary cipher text output buffer
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    protected abstract byte[] encryptInternal(byte[] plainText,
                                              FileCryptoEncryptAttributes attributes) throws IonicException;

    /**
     * Decrypts an Ionic-encrypted input byte buffer into an output byte buffer.
     * <p>
     * This API operates on the entire input byte buffer, and returns the entire output buffer.  The memory settings
     * of the in-use JVM should be tuned to accommodate very large byte buffers, if needed.
     * <p>
     * See also:
     * <a href='https://docs.oracle.com/cd/E21764_01/web.1111/e13814/jvm_tuning.htm'
     * target='_blank'>Tuning Java Virtual Machines</a>
     *
     * @param cipherText the binary cipher text input buffer
     * @param attributes the attributes used in the context of the decrypt operation
     * @return the binary plain text output buffer
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    protected abstract byte[] decryptInternal(byte[] cipherText,
                                              FileCryptoDecryptAttributes attributes) throws IonicException;

    /**
     * Encrypts an input file into an output file.
     *
     * @param sourceFile the input file
     * @param targetFile the output file
     * @param attributes the attributes to be used in the context of the encrypt operation
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    protected abstract void encryptInternal(File sourceFile, File targetFile,
                                            FileCryptoEncryptAttributes attributes) throws IonicException;

    /**
     * Decrypts an input file into an output file.
     *
     * @param sourceFile the input file
     * @param targetFile the output file
     * @param attributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on cryptography failures; or stream read / write failures
     */
    protected abstract void decryptInternal(File sourceFile, File targetFile,
                                            FileCryptoDecryptAttributes attributes) throws IonicException;


    /**
     * Core SDK behavior is to supply access denied page for appropriate file type in the
     * {@link FileCryptoDecryptAttributes} of the operation.
     *
     * @param e                 the thrown exception
     * @param fileType          the file type associated with the cryptography operation
     * @param decryptAttributes the out value used by the SDK to supply results of the operation to the caller
     * @throws IonicException unconditionally
     */
    protected final void handleFileCryptoException(final IonicException e, final FileType fileType,
                                                   final FileCryptoDecryptAttributes decryptAttributes)
            throws IonicException {
        final boolean isAcccessDenied = SdkError.ISAGENT_KEY_DENIED == e.getReturnCode();
        final boolean shouldProvidePage = decryptAttributes.shouldProvideAccessDeniedPage();
        if (isAcccessDenied && shouldProvidePage) {
            decryptAttributes.setAccessDeniedPageOut(coverPageServices.getAccessDeniedPage(fileType));
        }
        throw e;
    }
}
