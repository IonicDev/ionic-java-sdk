package com.ionic.sdk.cipher.aes;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.core.rng.RNG;
import com.ionic.sdk.error.CryptoErrorModuleConstants;
import com.ionic.sdk.error.IonicException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Cipher that implements AES GCM mode encryption / decryption.
 */
public class AesCtrCipher extends AesCipherAbstract {

    /**
     * Construct an instance of an Ionic AES GCM-mode cipher.
     *
     * @throws IonicException on cryptography errors
     */
    public AesCtrCipher() throws IonicException {
        super(getCipherInstance());
        setKey(null);
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
            return Cipher.getInstance(AesCipher.TRANSFORM_CTR);
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
    @Override
    public final byte[] encrypt(final byte[] plainText) throws IonicException {
        // cipher configuration
        final byte[] iv = RNG.fill(new byte[AesCipher.SIZE_IV]);
        final IvParameterSpec parameterSpec = new IvParameterSpec(iv);
        // encrypt
        final byte[] cipherText = super.encrypt(plainText, null, parameterSpec);
        // package result
        final byte[] cipherTextIonic = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, cipherTextIonic, 0, iv.length);
        System.arraycopy(cipherText, 0, cipherTextIonic, iv.length, cipherText.length);
        return cipherTextIonic;
    }

    /**
     * Decrypt a previously encrypted byte array and return the result as another byte array.
     *
     * @param cipherText array of bytes to decrypt
     * @return array of bytes representing the decrypted plaintext
     * @throws IonicException on cryptography errors
     */
    @Override
    public final byte[] decrypt(final byte[] cipherText) throws IonicException {
        // cipher configuration
        final byte[] iv = Arrays.copyOfRange(cipherText, 0, AesCipher.SIZE_IV);
        final byte[] cipherTextIonic = Arrays.copyOfRange(cipherText, AesCipher.SIZE_IV, cipherText.length);
        final IvParameterSpec parameterSpec = new IvParameterSpec(iv);
        // decrypt
        return super.decrypt(cipherTextIonic, null, parameterSpec);
    }
}
