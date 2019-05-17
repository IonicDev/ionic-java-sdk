package com.ionic.sdk.cipher.aes;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.rng.CryptoRng;
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
 * Cipher that implements AES GCM mode encryption / decryption.
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
        SdkData.checkNotNull(plainText, getClass().getSimpleName());
        SdkData.checkTrue(!Value.isEmpty(authData), SdkError.ISCRYPTO_BAD_INPUT, AAD);
        // cipher configuration
        final byte[] iv = new CryptoRng().rand(new byte[AesCipher.SIZE_IV]);
        final GCMParameterSpec parameterSpec = new GCMParameterSpec(SIZE_AUTH_TAG * Byte.SIZE, iv);
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
        SdkData.checkNotNull(plainText, getClass().getSimpleName());
        SdkData.checkNotNull(cipherText, getClass().getSimpleName());
        // cipher configuration
        final byte[] iv = new CryptoRng().rand(new byte[AesCipher.SIZE_IV]);
        final GCMParameterSpec parameterSpec = new GCMParameterSpec(
                SIZE_AUTH_TAG * Byte.SIZE, iv, 0, AesCipher.SIZE_IV);
        // encrypt
        cipherText.clear();
        cipherText.put(iv);
        return iv.length + super.encrypt(plainText, cipherText, authData, parameterSpec);
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
        SdkData.checkNotNull(cipherText, getClass().getSimpleName());
        SdkData.checkTrue(!Value.isEmpty(authData), SdkError.ISCRYPTO_BAD_INPUT, AAD);
        // cipher configuration
        final GCMParameterSpec parameterSpec = new GCMParameterSpec(
                SIZE_AUTH_TAG * Byte.SIZE, cipherText, 0, AesCipher.SIZE_IV);
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
        final byte[] iv = new byte[AesCipher.SIZE_IV];
        cipherText.get(iv);
        SdkData.checkTrue(!Value.isEmpty(authData), SdkError.ISCRYPTO_BAD_INPUT, AAD);
        final GCMParameterSpec parameterSpec = new GCMParameterSpec(
                SIZE_AUTH_TAG * Byte.SIZE, iv, 0, AesCipher.SIZE_IV);
        return super.decrypt(plainText, cipherText, authData, parameterSpec);
    }

    /**
     * Length in bytes of authentication tag used to detect data tampering using AES/GCM.
     */
    public static final int SIZE_AUTH_TAG = 16;

    /**
     * Label for GCM Additional Authenticated Data (AAD).
     */
    private static final String AAD = "Additional Authenticated Data";
}
