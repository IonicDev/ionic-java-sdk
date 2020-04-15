package com.ionic.sdk.cipher.rsa;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.cipher.rsa.model.RsaPrivateKey;
import com.ionic.sdk.cipher.rsa.model.RsaPublicKey;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * Ionic Machina Tools cipher implementation wrapping JCE-provided RSA algorithm.  This cipher object
 * implements RSA asymmetric encryption / decryption.
 * <p>
 * RSA is used internally by Machina in the context of the device enrollment operation.
 */
public final class RsaCipher {

    /**
     * The native Java cipher instance to wrap.
     */
    private final Cipher cipherInstance;

    /**
     * The native Java keypair used to perform cipher operations.
     */
    private KeyPair keypairInstance;

    /**
     * Class instance variable setter.
     *
     * @param keypairInstance the keypair to associate with this cipher instance
     */
    public void setKeypairInstance(final KeyPair keypairInstance) {
        this.keypairInstance = keypairInstance;
    }

    /**
     * Construct an instance of an Ionic RSA cipher.
     *
     * @throws IonicException on failure of platform preconditions for use of Ionic APIs.
     */
    public RsaCipher() throws IonicException {
        this.cipherInstance = AgentSdk.getCrypto().getCipherRsa();
        this.keypairInstance = new KeyPair(null, null);
    }

    /**
     * Encrypt a string and return the result as a byte array.
     *
     * @param plainText array of bytes to encrypt
     * @return array of bytes representing the ciphertext
     * @throws IonicException on cryptography errors
     */
    public byte[] encrypt(final String plainText) throws IonicException {
        return encryptInternal(Transcoder.utf8().decode(plainText));
    }

    /**
     * Encrypt a byte array and return the result as another byte array.
     *
     * @param plainText array of bytes to encrypt
     * @return array of bytes representing the ciphertext
     * @throws IonicException on cryptography errors
     */
    public byte[] encrypt(final byte[] plainText) throws IonicException {
        return encryptInternal(plainText);
    }

    /**
     * Encrypt a byte array and return the result as another byte array.
     *
     * @param plainText array of bytes to encrypt
     * @return array of bytes representing the ciphertext
     * @throws IonicException on cryptography errors, or invalid (null) parameters (pubkey, plainText)
     */
    private byte[] encryptInternal(final byte[] plainText) throws IonicException {
        try {
            SdkData.checkNotNull(keypairInstance, KeyPair.class.getName());
            SdkData.checkNotNull(keypairInstance.getPublic(), PublicKey.class.getName());
            SdkData.checkNotNull(plainText, getClass().getSimpleName());
            cipherInstance.init(Cipher.ENCRYPT_MODE, keypairInstance.getPublic());
            return cipherInstance.doFinal(plainText);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Decrypt a previously encrypted byte array and return the result as another byte array.
     *
     * @param cipherText array of bytes to decrypt
     * @return array of bytes representing the decrypted plaintext
     * @throws IonicException on cryptography errors
     */
    public byte[] decrypt(final byte[] cipherText) throws IonicException {
        return decryptInternal(cipherText);
    }

    /**
     * Decrypt a previously encrypted byte array and return the result as another byte array.
     *
     * @param cipherText array of bytes to decrypt
     * @return array of bytes representing the decrypted plaintext
     * @throws IonicException on cryptography errors, or invalid (null) parameters (privkey, cipherText)
     */
    private byte[] decryptInternal(final byte[] cipherText) throws IonicException {
        try {
            SdkData.checkNotNull(keypairInstance, KeyPair.class.getName());
            SdkData.checkNotNull(keypairInstance.getPrivate(), PrivateKey.class.getName());
            SdkData.checkNotNull(cipherText, getClass().getSimpleName());
            cipherInstance.init(Cipher.DECRYPT_MODE, keypairInstance.getPrivate());
            return cipherInstance.doFinal(cipherText);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Set the asymmetric cryptography public key to use in cryptography operations.
     *
     * @param publicKey the public key
     */
    public void setPublicKey(final RsaPublicKey publicKey) {
        keypairInstance = new KeyPair(publicKey.getPublicKey(), keypairInstance.getPrivate());
    }

    /**
     * Set the asymmetric cryptography private key to use in cryptography operations.
     *
     * @param privateKey the private key
     */
    public void setPrivateKey(final RsaPrivateKey privateKey) {
        keypairInstance = new KeyPair(keypairInstance.getPublic(), privateKey.getPrivateKey());
    }

    /**
     * Encrypt a byte array and return the result as a base64 encoded byte array.
     *
     * @param plainText array of bytes to encrypt
     * @return base64 representation of the ciphertext
     * @throws IonicException on cryptography errors
     */
    public String encryptToBase64(final byte[] plainText) throws IonicException {
        return Transcoder.base64().encode(encryptInternal(plainText));
    }

    /**
     * Encrypt a string and return the result as a base64 encoded byte array.  The plainText is converted to
     * UTF-8 to pass to the cryptography function.
     *
     * @param plainText string to encrypt
     * @return base64 representation of the ciphertext
     * @throws IonicException on cryptography errors
     */
    public String encryptToBase64(final String plainText) throws IonicException {
        return Transcoder.base64().encode(encryptInternal(Transcoder.utf8().decode(plainText)));
    }

    /**
     * Decrypt a base64 encoded string and return the result as a string.  The plainText is assumed to be valid
     * UTF-8 bytes; if not, the API {@link RsaCipher#decryptBase64(String)} should be used.
     *
     * @param cipherText base64 encoded string to decrypt
     * @return the plainText string
     * @throws IonicException on cryptography errors
     */
    public String decryptBase64ToString(final String cipherText) throws IonicException {
        return Transcoder.utf8().encode(decryptInternal(Transcoder.base64().decode(cipherText)));
    }

    /**
     * Decrypt a byte array and return the result as a string.  The plainText is assumed to be valid
     * UTF-8 bytes; if not, the API {@link RsaCipher#decrypt(byte[])} should be used.
     *
     * @param cipherText byte array to decrypt
     * @return the plainText string
     * @throws IonicException on cryptography errors
     */
    public String decryptToString(final byte[] cipherText) throws IonicException {
        return Transcoder.utf8().encode(decryptInternal(cipherText));
    }

    /**
     * Decrypt a base64 encoded string and return the result as the plainText byte array.
     *
     * @param cipherText base64 encoded string to decrypt
     * @return the plainText byte array
     * @throws IonicException on cryptography errors
     */
    public byte[] decryptBase64(final String cipherText) throws IonicException {
        return decryptInternal(Transcoder.base64().decode(cipherText));
    }

    /**
     * Returns the cryptographic signature of the input data.
     *
     * @param text the input data to be signed
     * @return the (base64 encoded) signature of the input data
     * @throws IonicException on cryptography errors, or invalid (null) parameters (text)
     */
    public String sign(final byte[] text) throws IonicException {
        try {
            SdkData.checkNotNull(text, getClass().getSimpleName());
            final Signature signature = AgentSdk.getCrypto().getSignatureRsa();
            signature.initSign(keypairInstance.getPrivate());
            signature.update(text);
            return CryptoUtils.binToBase64(signature.sign());
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Verifies the cryptographic signature of the input data.
     *
     * @param text the input data to be signed
     * @param sig  the input signature to verify
     * @return a boolean indicating the success or failure to verify the signature
     * @throws IonicException on cryptography errors, or invalid (null) parameters (text, sig)
     */
    public boolean verify(final byte[] text, final byte[] sig) throws IonicException {
        try {
            SdkData.checkNotNull(text, getClass().getSimpleName());
            SdkData.checkNotNull(sig, getClass().getSimpleName());
            final Signature signature = AgentSdk.getCrypto().getSignatureRsa();
            signature.initVerify(keypairInstance.getPublic());
            signature.update(text);
            return signature.verify(sig);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Length in bits of Ionic infrastructure RSA keys.
     */
    public static final int KEY_BITS = 3072;

    /**
     * Label for RSA algorithm.
     */
    public static final String ALGORITHM = "RSA";

    /**
     * Label for RSA algorithm, ECB transform (used in CreateDevice).
     */
    public static final String TRANSFORM_ECB = "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING";

    /**
     * Label for RSA signature algorithm (used in CreateDevice).
     */
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA/PSS";
}
