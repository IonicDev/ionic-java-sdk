package com.ionic.sdk.cipher.rsa;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.CryptoErrorModuleConstants;
import com.ionic.sdk.error.IonicException;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Signature;

/**
 * Cipher that implements RSA encryption / decryption.
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
     * @throws IonicException on cryptography errors
     */
    public RsaCipher() throws IonicException {
        this.cipherInstance = getCipherInstance();
    }

    /**
     * Construct an instance of a Java cipher.
     *
     * @return the Cipher to be wrapped
     * @throws IonicException if Cipher cannot be instantiated
     */
    private static Cipher getCipherInstance() throws IonicException {
        try {
            AgentSdk.initialize(null);
            return Cipher.getInstance(TRANSFORM_ECB);
        } catch (GeneralSecurityException e) {
            throw new IonicException(CryptoErrorModuleConstants.ISCRYPTO_ERROR.value(), e);
        }
    }

    /**
     * Encrypt a byte array and return the result as another byte array.
     *
     * @param plainText array of bytes to encrypt
     * @return array of bytes representing the ciphertext
     * @throws IonicException on cryptography errors
     */
    public byte[] encrypt(final byte[] plainText) throws IonicException {
        try {
            cipherInstance.init(Cipher.ENCRYPT_MODE, keypairInstance.getPublic());
            return cipherInstance.doFinal(plainText);
        } catch (GeneralSecurityException e) {
            throw new IonicException(e);
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
        try {
            cipherInstance.init(Cipher.DECRYPT_MODE, keypairInstance.getPrivate());
            return cipherInstance.doFinal(cipherText);
        } catch (GeneralSecurityException e) {
            throw new IonicException(e);
        }
    }

    /**
     * Returns the cryptographic signature of the input data.
     *
     * @param text the input data to be signed
     * @return the (base64 encoded) signature of the input data
     * @throws IonicException on cryptography errors
     */
    public String sign(final byte[] text) throws IonicException {
        try {
            AgentSdk.initialize(null);
            final Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(keypairInstance.getPrivate());
            signature.update(text);
            return CryptoUtils.binToBase64(signature.sign());
        } catch (GeneralSecurityException e) {
            throw new IonicException(e);
        }
    }

    /**
     * Verifies the cryptographic signature of the input data.
     *
     * @param text the input data to be signed
     * @param sig  the input signature to verify
     * @return a boolean indicating the success or failure to verify the signature
     * @throws IonicException on cryptography errors
     */
    public boolean verify(final byte[] text, final byte[] sig) throws IonicException {
        try {
            AgentSdk.initialize(null);
            final Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(keypairInstance.getPublic());
            signature.update(text);
            return signature.verify(sig);
        } catch (GeneralSecurityException e) {
            throw new IonicException(e);
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
