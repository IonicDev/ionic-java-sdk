package com.ionic.sdk.cipher.aes;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Ionic Machina Tools cipher implementation wrapping JCE-provided AES-GCM algorithm.  This cipher object
 * implements AES GCM mode encryption / decryption.
 * <p>
 * AES-GCM (Galois/Counter Mode) is a streaming cipher variant of AES with authenticated data (ADATA).  It provides
 * for both data confidentiality and integrity, due to the use of the authenticated encryption Galois/Counter
 * Mode (GCM).  While GCM guarantees data integrity, it requires additional space to store the integrity check.
 * <p>
 * Class API variants are available to encrypt input strings into either raw byte arrays, or into the
 * base64-encoded string representation of a raw byte array.
 * <p>
 * Sample:
 * <pre>
 * public final void testAesGcmCipher_EncryptDecryptStringToBytes() throws IonicException {
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final CreateKeysResponse.Key key = keyServices.createKey().getFirstKey();
 *     final String plainText = "Hello, Machina!";
 *     final AesGcmCipher cipher = new AesGcmCipher();
 *     cipher.setKey(key.getSecretKey());
 *     cipher.setAuthData(Transcoder.utf8().decode(key.getId()));
 *     final byte[] cipherText = cipher.encryptString(plainText);
 *     final String plainTextRecover = cipher.decryptToString(cipherText);
 *     Assert.assertEquals(plainText, plainTextRecover);
 * }
 * </pre>
 * <p>
 * Sample:
 * <pre>
 * public final void testAesGcmCipher_EncryptDecryptStringToString() throws IonicException {
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final CreateKeysResponse.Key key = keyServices.createKey().getFirstKey();
 *     final String plainText = "Hello, Machina!";
 *     final AesGcmCipher cipher = new AesGcmCipher();
 *     cipher.setKey(key.getSecretKey());
 *     cipher.setAuthData(Transcoder.utf8().decode(key.getId()));
 *     final String cipherText = cipher.encryptToBase64(plainText);
 *     final String plainTextRecover = cipher.decryptBase64ToString(cipherText);
 *     Assert.assertEquals(plainText, plainTextRecover);
 * }
 * </pre>
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/crypto-aes-gcm' target='_blank'>Machina Developers</a> for
 * more information on this cryptography implementation.
 */
public class AesGcmCipher extends AesCipherAbstract {

    /**
     * ID for AesGcmCipher class cipher.
     */
    private static final String ID = "aes_gcm";
    /**
     * Label for AesGcmCipher class cipher.
     */
    private static final String LABEL = "AES GCM Cipher";

    /**
     * The additional authenticated data used by the GCM cipher.
     */
    private byte[] authData;

    /**
     * Construct an instance of an Ionic AES GCM-mode cipher.
     *
     * @throws IonicException on cryptography errors
     */
    public AesGcmCipher() throws IonicException {
        this((byte[]) null);
    }

    /**
     * Construct an instance of an Ionic AES GCM-mode cipher.
     *
     * @param cipherKey the raw bytes of the key
     * @throws IonicException on cryptography errors
     */
    public AesGcmCipher(final byte[] cipherKey) throws IonicException {
        super(getCipherInstance());
        setKey(cipherKey);
    }

    /**
     * Construct an instance of an Ionic AES GCM-mode cipher.
     *
     * @param secretKey the JCE object representation of the key
     * @throws IonicException on cryptography errors
     */
    public AesGcmCipher(final SecretKey secretKey) throws IonicException {
        super(getCipherInstance());
        setKey(secretKey);
    }

    /**
     * Construct an instance of a Java cipher.
     *
     * @return the Cipher to be wrapped
     * @throws IonicException if Cipher cannot be instantiated
     */
    private static Cipher getCipherInstance() throws IonicException {
        return AgentSdk.getCrypto().getCipherAesGcm();
    }

    @Override
    public final String getId() {
        return ID;
    }

    @Override
    public final String getLabel() {
        return LABEL;
    }

    /**
     * Set the additional authenticated data used by the GCM cipher.
     *
     * @param authDataIn auth data byte array
     */
    public final void setAuthData(final byte[] authDataIn) {
        this.authData = (authDataIn == null) ? null : Arrays.copyOf(authDataIn, authDataIn.length);
    }

    /**
     * Encrypt a byte array and return the result as another byte array.
     *
     * @param plainText array of bytes to encrypt
     * @return array of bytes representing the ciphertext
     * @throws IonicException on cryptography errors
     */
    @Override
    public final byte[] encrypt(final byte[] plainText) throws IonicException {
        return encryptInternal(plainText);
    }

    /**
     * Encrypt a string and return the result as a byte array.
     *
     * @param plainText array of bytes to encrypt
     * @return array of bytes representing the ciphertext
     * @throws IonicException on cryptography errors
     */
    @Override
    public final byte[] encrypt(final String plainText) throws IonicException {
        return encryptInternal(Transcoder.utf8().decode(plainText));
    }

    /**
     * Encrypt a byte array and return the result as another byte array.
     *
     * @param plainText array of bytes to encrypt
     * @return array of bytes representing the ciphertext
     * @throws IonicException on cryptography errors, or invalid (null) parameters (plainText)
     */
    private byte[] encryptInternal(final byte[] plainText) throws IonicException {
        SdkData.checkNotNull(plainText, ERR_LABEL);
        SdkData.checkTrue(!Value.isEmpty(authData), SdkError.ISCRYPTO_BAD_INPUT, ERR_AAD);
        // cipher configuration
        final byte[] iv = getIV(plainText);
        final GCMParameterSpec parameterSpec = new GCMParameterSpec(AesCipher.SIZE_ATAG * Byte.SIZE, iv);
        // encrypt
        final byte[] cipherText = super.encrypt(plainText, authData, parameterSpec);
        // package result
        final byte[] cipherTextIonic = Arrays.copyOf(iv, (iv.length + cipherText.length));
        System.arraycopy(cipherText, 0, cipherTextIonic, iv.length, cipherText.length);
        return cipherTextIonic;
    }

    /**
     * Encrypt a byte buffer.  This API makes use of the JRE
     * {@link Cipher#doFinal(ByteBuffer, ByteBuffer)} API, which uses the parameter <code>ByteBuffer</code>
     * objects internally instead of allocating new buffers.
     *
     * @param plainText  ByteBuffer containing bytes to encrypt
     * @param cipherText ByteBuffer to receive the result of the cryptography operation
     * @return the number of bytes stored in ciphertext
     * @throws IonicException on cryptography errors, or invalid (null) parameters (key, plainText)
     */
    public final int encrypt(final ByteBuffer plainText, final ByteBuffer cipherText) throws IonicException {
        SdkData.checkNotNull(plainText, ERR_LABEL);
        SdkData.checkNotNull(cipherText, ERR_LABEL);
        SdkData.checkTrue(!Value.isEmpty(authData), SdkError.ISCRYPTO_BAD_INPUT, ERR_AAD);
        // cipher configuration
        final byte[] iv = getIV(plainText.duplicate().array());
        final GCMParameterSpec parameterSpec = new GCMParameterSpec(
                AesCipher.SIZE_ATAG * Byte.SIZE, iv, 0, AesCipher.SIZE_IV);
        // encrypt
        cipherText.put(iv);
        return iv.length + super.encrypt(plainText, cipherText, authData, parameterSpec);
    }

    /**
     * Encrypt a byte buffer.  This API makes use of the JRE
     * {@link Cipher#doFinal(ByteBuffer, ByteBuffer)} API, which uses the parameter <code>ByteBuffer</code>
     * objects internally instead of allocating new buffers.
     *
     * @param plainText     ByteBuffer containing bytes to encrypt
     * @param cipherText    ByteBuffer to receive the result of the cryptography operation
     * @param parameterSpec additional cipher configuration
     * @return the number of bytes stored in ciphertext
     * @throws IonicException on cryptography errors, or invalid (null) parameters (key, plainText)
     */
    public final int encrypt(final ByteBuffer plainText, final ByteBuffer cipherText,
                             final GCMParameterSpec parameterSpec) throws IonicException {
        SdkData.checkNotNull(plainText, ERR_LABEL);
        SdkData.checkNotNull(cipherText, ERR_LABEL);
        SdkData.checkNotNull(parameterSpec, ERR_LABEL);
        SdkData.checkTrue(!Value.isEmpty(authData), SdkError.ISCRYPTO_BAD_INPUT, ERR_AAD);
        // encrypt
        cipherText.clear();
        return super.encrypt(plainText, cipherText, authData, parameterSpec);
    }

    /**
     * Decrypt a previously encrypted byte array and return the result as another byte array.
     *
     * @param cipherText array of bytes to decrypt
     * @return array of bytes representing the decrypted plaintext
     * @throws IonicException on cryptography errors, or invalid (null) parameters (cipherText)
     */
    @Override
    public final byte[] decrypt(final byte[] cipherText) throws IonicException {
        SdkData.checkNotNull(cipherText, ERR_LABEL);
        SdkData.checkTrue(!Value.isEmpty(authData), SdkError.ISCRYPTO_BAD_INPUT, ERR_AAD);
        // cipher configuration
        final GCMParameterSpec parameterSpec = new GCMParameterSpec(
                AesCipher.SIZE_ATAG * Byte.SIZE, cipherText, 0, AesCipher.SIZE_IV);
        // decrypt
        return super.decrypt(cipherText, authData, parameterSpec);
    }

    /**
     * Decrypt a previously encrypted byte buffer.  This API makes use of the JRE
     * {@link Cipher#doFinal(ByteBuffer, ByteBuffer)} API, which uses the parameter <code>ByteBuffer</code>
     * objects internally instead of allocating new buffers.
     *
     * @param plainText  ByteBuffer to receive the result of the cryptography operation
     * @param cipherText ByteBuffer containing bytes to decrypt
     * @return the number of bytes stored in plaintext
     * @throws IonicException on cryptography errors
     */
    public final int decrypt(final ByteBuffer plainText, final ByteBuffer cipherText) throws IonicException {
        SdkData.checkNotNull(plainText, ERR_LABEL);
        SdkData.checkNotNull(cipherText, ERR_LABEL);
        SdkData.checkTrue(!Value.isEmpty(authData), SdkError.ISCRYPTO_BAD_INPUT, ERR_AAD);
        final byte[] iv = new byte[AesCipher.SIZE_IV];
        cipherText.get(iv);
        final GCMParameterSpec parameterSpec = new GCMParameterSpec(
                AesCipher.SIZE_ATAG * Byte.SIZE, iv, 0, AesCipher.SIZE_IV);
        return super.decrypt(plainText, cipherText, authData, parameterSpec);
    }

    /**
     * Decrypt a previously encrypted byte buffer.  This API makes use of the JRE
     * {@link Cipher#doFinal(ByteBuffer, ByteBuffer)} API, which uses the parameter <code>ByteBuffer</code>
     * objects internally instead of allocating new buffers.
     *
     * @param plainText     ByteBuffer to receive the result of the cryptography operation
     * @param cipherText    ByteBuffer containing bytes to decrypt
     * @param parameterSpec additional cipher configuration
     * @return the number of bytes stored in plaintext
     * @throws IonicException on cryptography errors
     */
    public final int decrypt(final ByteBuffer plainText, final ByteBuffer cipherText,
                             final GCMParameterSpec parameterSpec) throws IonicException {
        SdkData.checkNotNull(plainText, ERR_LABEL);
        SdkData.checkNotNull(cipherText, ERR_LABEL);
        SdkData.checkNotNull(parameterSpec, ERR_LABEL);
        SdkData.checkTrue(!Value.isEmpty(authData), SdkError.ISCRYPTO_BAD_INPUT, ERR_AAD);
        // decrypt
        plainText.clear();
        return super.decrypt(plainText, cipherText, authData, parameterSpec);
    }

    /**
     * Length in bytes of authentication tag used to detect data tampering using AES/GCM.
     *
     * @deprecated please migrate usages to the replacement {@link AesCipher#SIZE_ATAG}
     */
    @Deprecated
    public static final int SIZE_AUTH_TAG = 16;

    /**
     * Label for API call validity check failure.
     */
    private static final String ERR_LABEL = AesGcmCipher.class.getSimpleName();

    /**
     * Label for GCM Additional Authenticated Data (AAD).
     */
    private static final String ERR_AAD = "Additional Authenticated Data";
}
