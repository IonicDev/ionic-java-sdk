package com.ionic.sdk.cipher.aes;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.core.rng.RNG;
import com.ionic.sdk.error.CryptoErrorModuleConstants;
import com.ionic.sdk.error.IonicException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Cipher that implements AES GCM mode encryption / decryption.
 */
public class AesGcmCipher extends AesCipherAbstract {

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
            return Cipher.getInstance(AesCipher.TRANSFORM_GCM);
        } catch (GeneralSecurityException e) {
            throw new IonicException(CryptoErrorModuleConstants.ISCRYPTO_ERROR.value(), e);
        }
    }

    /**
     * Set the additional authenticated data used by the GCM cipher.
     *
     * @param authDataIn auth data byte array
     */
    public final void setAuthData(final byte[] authDataIn) {
        this.authData = (authDataIn == null) ? null : Arrays.copyOfRange(authDataIn, 0, authDataIn.length);
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
        final GCMParameterSpec parameterSpec = new GCMParameterSpec(SIZE_AUTH_TAG * Byte.SIZE, iv);
        // encrypt
        final byte[] cipherText = super.encrypt(plainText, authData, parameterSpec);
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
        final GCMParameterSpec parameterSpec = new GCMParameterSpec(SIZE_AUTH_TAG * Byte.SIZE, iv);
        // decrypt
        return super.decrypt(cipherTextIonic, authData, parameterSpec);
    }

    /**
     * Length in bytes of authentication tag used to detect data tampering using AES/GCM.
     */
    public static final int SIZE_AUTH_TAG = 16;
}
