package com.ionic.sdk.cipher;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.agent.data.MetadataHolder;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Base class for all encryption/decryption ciphers.
 */
public abstract class CipherAbstract extends MetadataHolder {

    /**
     * The native Java cipher instance to wrap.
     */
    private final Cipher cipherInstance;

    /**
     * The native Java key used to perform the cipher operation.
     */
    private Key keyInstance;

    /**
     * Construct and initialize an object of this type.
     *
     * @param cipher the native Java cipher instance to wrap
     */
    public CipherAbstract(final Cipher cipher) {
        this.cipherInstance = cipher;
    }

    /**
     * Retrieve the Cipher crossplatform class ID.
     *
     * @return The cipher crossplatform class ID string
     */
    public abstract String getId();

    /**
     * Retrieve the Cipher crossplatform class name.
     *
     * @return The cipher crossplatform class name string
     */
    public abstract String getLabel();

    /**
     * Set the key for this cipher.
     *
     * @param key the native Java key used to perform the cipher operation.
     */
    protected final void setKeyNative(final Key key) {
        this.keyInstance = key;
    }

    /**
     * Compute a MAC (message authentication code) for the input value, using the member key.
     *
     * @param message the message for which the MAC should be generated
     * @return the message authentication code
     * @throws IonicException on cryptography errors
     */
    public byte[] hmacSHA256(final byte[] message) throws IonicException {
        try {
            final Mac hmacSHA256 = AgentSdk.getCrypto().getHmacSha256();
            hmacSHA256.init(keyInstance);
            return hmacSHA256.doFinal(message);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Encrypt a byte array and return the result as another byte array.
     *
     * @param plainText array of bytes to encrypt
     * @return array of bytes representing the ciphertext
     * @throws IonicException on cryptography errors
     */
    public abstract byte[] encrypt(byte[] plainText) throws IonicException;

    /**
     * Encrypt a string and return the result as a byte array.
     *
     * @param plainText Plaintext String to encrypt.
     * @return An array of bytes representing the ciphertext.
     * @throws IonicException on cryptography errors
     */
    public abstract byte[] encrypt(String plainText) throws IonicException;

    /**
     * Encrypt a byte array and return the result as a base64-encoded string.
     *
     * @param plainText Array of bytes to encrypt.
     * @return A base64-encoded String representing the ciphertext.
     * @throws IonicException on cryptography errors
     */
    public final String encryptToBase64(final byte[] plainText) throws IonicException {
        return CryptoUtils.binToBase64(encrypt(plainText));
    }

    /**
     * Encrypt a string and return the result as a byte array.
     *
     * @param plainText Plaintext String to encrypt.
     * @return An array of bytes representing the ciphertext.
     * @throws IonicException on cryptography errors
     */
    public final byte[] encryptString(final String plainText) throws IonicException {
        return encrypt(Transcoder.utf8().decode(plainText));
    }

    /**
     * Encrypt a string and return the result as a base64-encoded string.
     *
     * @param plainText String to encrypt.
     * @return A base64-encoded String representing the ciphertext.
     * @throws IonicException on cryptography errors
     */
    public final String encryptToBase64(final String plainText) throws IonicException {
        return CryptoUtils.binToBase64(encrypt(Transcoder.utf8().decode(plainText)));
    }

    /**
     * Decrypt a previously encrypted byte array and return the result as another byte array.
     *
     * @param cipherText array of bytes to decrypt
     * @return array of bytes representing the decrypted plaintext
     * @throws IonicException on cryptography errors
     */
    public abstract byte[] decrypt(byte[] cipherText) throws IonicException;

    /**
     * Decrypt a previously-encrypted byte array and return the result as a string.
     *
     * @param cipherText Array of bytes to decrypt.
     * @return String representing the decrypted plaintext.
     * @throws IonicException on cryptography errors
     */
    public final String decryptToString(final byte[] cipherText) throws IonicException {
        return Transcoder.utf8().encode(decrypt(cipherText));
    }

    /**
     * Decrypt a previously-encrypted string and return the result as a byte array.
     *
     * @param cipherText String to decrypt.
     * @return Array of bytes representing the decrypted plaintext.
     * @throws IonicException on cryptography errors
     */
    public final byte[] decryptBase64(final String cipherText) throws IonicException {
        return decrypt(CryptoUtils.base64ToBin(cipherText));
    }

    /**
     * Decrypt a previously-encrypted string and return the result as another string.
     *
     * @param cipherText Base64-encoded String to decrypt.
     * @return String representing the decrypted plaintext.
     * @throws IonicException on cryptography errors
     */
    public final String decryptBase64ToString(final String cipherText) throws IonicException {
        return Transcoder.utf8().encode(decrypt(CryptoUtils.base64ToBin(cipherText)));
    }

    /**
     * Encrypt a byte array and return the result as another byte array.
     *
     * @param plainText     array of bytes to encrypt
     * @param authData      additional authenticated data used by some ciphers in crypto operations
     * @param parameterSpec additional configuration specific to some ciphers
     * @return an array of bytes representing the ciphertext
     * @throws IonicException on cryptography errors, or invalid (null) parameters (key, plainText)
     */
    protected final byte[] encrypt(final byte[] plainText, final byte[] authData,
                                   final AlgorithmParameterSpec parameterSpec) throws IonicException {
        SdkData.checkNotNull(keyInstance, Key.class.getName());
        SdkData.checkNotNull(plainText, getClass().getSimpleName());
        try {
            return encryptInner(plainText, authData, parameterSpec);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Encrypt a byte array and return the result as another byte array.
     *
     * @param plainText     array of bytes to encrypt
     * @param authData      additional authenticated data used by some ciphers in crypto operations
     * @param parameterSpec additional configuration specific to some ciphers
     * @return an array of bytes representing the ciphertext
     * @throws GeneralSecurityException on cryptography errors
     */
    private byte[] encryptInner(final byte[] plainText, final byte[] authData,
                                final AlgorithmParameterSpec parameterSpec) throws GeneralSecurityException {
        // set cipher parameters
        if (parameterSpec == null) {
            cipherInstance.init(Cipher.ENCRYPT_MODE, keyInstance);
        } else {
            cipherInstance.init(Cipher.ENCRYPT_MODE, keyInstance, parameterSpec);
        }
        // set aad
        if (authData != null) {
            cipherInstance.updateAAD(authData);
        }
        // encrypt
        return cipherInstance.doFinal(plainText);
    }

    /**
     * Encrypt a byte buffer.  This API makes use of the JRE
     * {@link Cipher#doFinal(ByteBuffer, ByteBuffer)} API, which uses the parameter <code>ByteBuffer</code>
     * objects internally instead of allocating new buffers.
     *
     * @param plainText     ByteBuffer containing bytes to encrypt
     * @param cipherText    ByteBuffer to receive the result of the cryptography operation
     * @param authData      additional authenticated data used by some ciphers in crypto operations
     * @param parameterSpec additional configuration specific to some ciphers
     * @return the number of bytes stored in ciphertext
     * @throws IonicException on cryptography errors, or invalid (null) parameters (key, plainText)
     */
    protected final int encrypt(final ByteBuffer plainText, final ByteBuffer cipherText, final byte[] authData,
                                final AlgorithmParameterSpec parameterSpec) throws IonicException {
        SdkData.checkNotNull(keyInstance, Key.class.getName());
        SdkData.checkNotNull(plainText, getClass().getSimpleName());
        try {
            return encryptInner(plainText, cipherText, authData, parameterSpec);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Encrypt a byte buffer.
     *
     * @param plainText     ByteBuffer containing bytes to encrypt
     * @param cipherText    ByteBuffer to receive the result of the cryptography operation
     * @param authData      additional authenticated data used by some ciphers in crypto operations
     * @param parameterSpec additional configuration specific to some ciphers
     * @return the number of bytes stored in ciphertext
     * @throws GeneralSecurityException on cryptography errors
     */
    private int encryptInner(final ByteBuffer plainText, final ByteBuffer cipherText, final byte[] authData,
                             final AlgorithmParameterSpec parameterSpec) throws GeneralSecurityException {
        // set cipher parameters
        if (parameterSpec == null) {
            cipherInstance.init(Cipher.ENCRYPT_MODE, keyInstance);
        } else {
            cipherInstance.init(Cipher.ENCRYPT_MODE, keyInstance, parameterSpec);
        }
        // set aad
        if (authData != null) {
            cipherInstance.updateAAD(authData);
        }
        // encrypt
        return cipherInstance.doFinal(plainText, cipherText);
    }

    /**
     * Decrypt a previously encrypted byte array and return the result as another byte array.
     *
     * @param cipherText    array of bytes to decrypt
     * @param authData      additional authenticated data used by some ciphers in crypto operations
     * @param parameterSpec additional configuration specific to some ciphers
     * @return array of bytes representing the decrypted plaintext
     * @throws IonicException on cryptography errors, or invalid (null) parameters (key, cipherText)
     */
    protected final byte[] decrypt(final byte[] cipherText, final byte[] authData,
                                   final AlgorithmParameterSpec parameterSpec) throws IonicException {
        SdkData.checkNotNull(keyInstance, Key.class.getName());
        SdkData.checkNotNull(cipherText, getClass().getSimpleName());
        try {
            return decryptInner(cipherText, authData, parameterSpec);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Decrypt a previously encrypted byte array and return the result as another byte array.
     *
     * @param cipherText    array of bytes to decrypt
     * @param authData      additional authenticated data used by some ciphers in crypto operations
     * @param parameterSpec additional configuration specific to some ciphers
     * @return array of bytes representing the decrypted plaintext
     * @throws GeneralSecurityException on cryptography errors
     */
    private byte[] decryptInner(final byte[] cipherText, final byte[] authData,
                                final AlgorithmParameterSpec parameterSpec) throws GeneralSecurityException {
        // set cipher parameters
        if (parameterSpec == null) {
            cipherInstance.init(Cipher.DECRYPT_MODE, keyInstance);
        } else {
            cipherInstance.init(Cipher.DECRYPT_MODE, keyInstance, parameterSpec);
        }
        // set aad
        if (authData != null) {
            cipherInstance.updateAAD(authData);
        }
        // decrypt
        final int inputLen = cipherText.length - AesCipher.SIZE_IV;
        return cipherInstance.doFinal(cipherText, AesCipher.SIZE_IV, inputLen);
    }

    /**
     * Decrypt a previously encrypted byte buffer.  This API makes use of the JRE
     * {@link Cipher#doFinal(ByteBuffer, ByteBuffer)} API, which uses the parameter <code>ByteBuffer</code>
     * objects internally instead of allocating new buffers.
     *
     * @param plainText     ByteBuffer to receive the result of the cryptography operation
     * @param cipherText    ByteBuffer containing bytes to decrypt
     * @param authData      additional authenticated data used by some ciphers in crypto operations
     * @param parameterSpec additional configuration specific to some ciphers
     * @return the number of bytes stored in plaintext
     * @throws IonicException on cryptography errors
     */
    protected final int decrypt(final ByteBuffer plainText, final ByteBuffer cipherText, final byte[] authData,
                                final AlgorithmParameterSpec parameterSpec) throws IonicException {
        SdkData.checkNotNull(keyInstance, Key.class.getName());
        SdkData.checkNotNull(plainText, getClass().getSimpleName());
        SdkData.checkNotNull(cipherText, getClass().getSimpleName());
        try {
            return decryptInner(plainText, cipherText, authData, parameterSpec);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Decrypt a previously encrypted byte buffer.
     *
     * @param plainText     ByteBuffer to receive the result of the cryptography operation
     * @param cipherText    ByteBuffer containing bytes to decrypt
     * @param authData      additional authenticated data used by some ciphers in crypto operations
     * @param parameterSpec additional configuration specific to some ciphers
     * @return the number of bytes stored in plaintext
     * @throws GeneralSecurityException on cryptography errors
     */
    private int decryptInner(final ByteBuffer plainText, final ByteBuffer cipherText, final byte[] authData,
                             final AlgorithmParameterSpec parameterSpec) throws GeneralSecurityException {
        // set cipher parameters
        if (parameterSpec == null) {
            cipherInstance.init(Cipher.DECRYPT_MODE, keyInstance);
        } else {
            cipherInstance.init(Cipher.DECRYPT_MODE, keyInstance, parameterSpec);
        }
        // set aad
        if (authData != null) {
            cipherInstance.updateAAD(authData);
        }
        // decrypt
        return cipherInstance.doFinal(cipherText, plainText);
    }
}
