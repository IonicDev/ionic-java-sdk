package com.ionic.sdk.cipher;

import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.CryptoErrorModuleConstants;
import com.ionic.sdk.error.IonicException;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Base class for all encryption/decryption ciphers.
 */
public abstract class CipherAbstract {

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
     * Set the key for this cipher.
     *
     * @param key the native Java key used to perform the cipher operation.
     */
    protected final void setKeyNative(final Key key) {
        this.keyInstance = key;
    }

    /**
     * Encrypt a byte array and return the result as another byte array.
     *
     * @param plainText array of bytes to encrypt
     * @return array of bytes representing the ciphertext
     * @throws IonicException on cryptography errors
     */
    public abstract byte[] encrypt(final byte[] plainText) throws IonicException;

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
    public abstract byte[] decrypt(final byte[] cipherText) throws IonicException;

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
     * @throws IonicException on cryptography errors
     */
    protected final byte[] encrypt(final byte[] plainText, final byte[] authData,
                                   final AlgorithmParameterSpec parameterSpec) throws IonicException {
        try {
            return encryptInner(plainText, authData, parameterSpec);
        } catch (GeneralSecurityException e) {
            throw new IonicException(CryptoErrorModuleConstants.ISCRYPTO_ERROR.value(), e);
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
     * Decrypt a previously encrypted byte array and return the result as another byte array.
     *
     * @param cipherText    array of bytes to decrypt
     * @param authData      additional authenticated data used by some ciphers in crypto operations
     * @param parameterSpec additional configuration specific to some ciphers
     * @return array of bytes representing the decrypted plaintext
     * @throws IonicException on cryptography errors
     */
    protected final byte[] decrypt(final byte[] cipherText, final byte[] authData,
                                   final AlgorithmParameterSpec parameterSpec) throws IonicException {
        try {
            return decryptInner(cipherText, authData, parameterSpec);
        } catch (GeneralSecurityException e) {
            throw new IonicException(CryptoErrorModuleConstants.ISCRYPTO_ERROR.value(), e);
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
        return cipherInstance.doFinal(cipherText);
    }
}
