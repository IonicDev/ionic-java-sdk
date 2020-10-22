package com.ionic.sdk.cipher.aes;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Ionic Machina Tools cipher implementation wrapping JCE-provided AES-CTR algorithm.  This cipher object
 * implements AES CTR mode encryption / decryption.
 * <p>
 * AES-CTR (Counter Mode) is a streaming cipher variant of AES where the next key stream block is calculated by
 * encrypting increasing values of a "counter".
 * <p>
 * Class API variants are available to encrypt input strings into either raw byte arrays, or into the
 * base64-encoded string representation of a raw byte array.
 * <p>
 * Sample:
 * <pre>
 * public final void testAesCtrCipher_EncryptDecryptStringToBytes() throws IonicException {
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final CreateKeysResponse.Key key = keyServices.createKey().getFirstKey();
 *     final String plainText = "Hello, Machina!";
 *     final AesCtrCipher cipher = new AesCtrCipher();
 *     cipher.setKey(key.getSecretKey());
 *     final byte[] cipherText = cipher.encryptString(plainText);
 *     final String plainTextRecover = cipher.decryptToString(cipherText);
 *     Assert.assertEquals(plainText, plainTextRecover);
 * }
 * </pre>
 * <p>
 * Sample:
 * <pre>
 * public final void testAesCtrCipher_EncryptDecryptStringToString() throws IonicException {
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final CreateKeysResponse.Key key = keyServices.createKey().getFirstKey();
 *     final String plainText = "Hello, Machina!";
 *     final AesCipherAbstract cipher = new AesCtrCipher();
 *     cipher.setKey(key.getSecretKey());
 *     final String cipherText = cipher.encryptToBase64(plainText);
 *     final String plainTextRecover = cipher.decryptBase64ToString(cipherText);
 *     Assert.assertEquals(plainText, plainTextRecover);
 * }
 * </pre>
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/crypto-aes-ctr' target='_blank'>Machina Developers</a> for
 * more information on this cryptography implementation.
 */
public class AesCtrCipher extends AesCipherAbstract {

    /**
     * ID for AesCtrCipher class cipher.
     */
    private static final String ID = "aes_ctr";
    /**
     * Label for AesCtrCipher class cipher.
     */
    private static final String LABEL = "AES CTR Cipher";

    /**
     * Construct an instance of an Ionic AES CTR-mode cipher.
     *
     * @throws IonicException on cryptography errors
     */
    public AesCtrCipher() throws IonicException {
        this((byte[]) null);
    }

    /**
     * Construct an instance of an Ionic AES CTR-mode cipher.
     *
     * @param cipherKey the raw bytes of the key
     * @throws IonicException on cryptography errors
     */
    public AesCtrCipher(final byte[] cipherKey) throws IonicException {
        super(getCipherInstance());
        setKey(cipherKey);
    }

    /**
     * Construct an instance of an Ionic AES CTR-mode cipher.
     *
     * @param secretKey the JCE object representation of the key
     * @throws IonicException on cryptography errors
     */
    public AesCtrCipher(final SecretKey secretKey) throws IonicException {
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
        return AgentSdk.getCrypto().getCipherAesCtr();
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
        // cipher configuration
        final byte[] iv = getIV(plainText);
        final IvParameterSpec parameterSpec = new IvParameterSpec(iv);
        // encrypt
        final byte[] cipherText = super.encrypt(plainText, null, parameterSpec);
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
        // cipher configuration
        final byte[] iv = getIV(plainText.duplicate().array());
        final IvParameterSpec parameterSpec = new IvParameterSpec(iv);
        // encrypt
        cipherText.put(iv);
        return iv.length + super.encrypt(plainText, cipherText, null, parameterSpec);
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
                             final IvParameterSpec parameterSpec) throws IonicException {
        SdkData.checkNotNull(plainText, ERR_LABEL);
        SdkData.checkNotNull(cipherText, ERR_LABEL);
        SdkData.checkNotNull(parameterSpec, ERR_LABEL);
        // encrypt
        cipherText.clear();
        return super.encrypt(plainText, cipherText, null, parameterSpec);
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
        // cipher configuration
        final IvParameterSpec parameterSpec = new IvParameterSpec(cipherText, 0, AesCipher.SIZE_IV);
        // decrypt
        return super.decrypt(cipherText, null, parameterSpec);
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
        final byte[] iv = new byte[AesCipher.SIZE_IV];
        cipherText.get(iv);
        final IvParameterSpec parameterSpec = new IvParameterSpec(iv, 0, iv.length);
        plainText.clear();
        return super.decrypt(plainText, cipherText, null, parameterSpec);
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
                             final IvParameterSpec parameterSpec) throws IonicException {
        SdkData.checkNotNull(plainText, ERR_LABEL);
        SdkData.checkNotNull(cipherText, ERR_LABEL);
        SdkData.checkNotNull(parameterSpec, ERR_LABEL);
        // decrypt
        plainText.clear();
        return super.decrypt(plainText, cipherText, null, parameterSpec);
    }

    /**
     * Label for API call validity check failure.
     */
    private static final String ERR_LABEL = AesCtrCipher.class.getSimpleName();
}
