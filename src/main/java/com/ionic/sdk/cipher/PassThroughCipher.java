package com.ionic.sdk.cipher;

import javax.crypto.Cipher;

import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;

/**
 * PassThroughCipher class.
 * @author Ionic Security
 */
public class PassThroughCipher extends CipherAbstract {

    /**
     * ID for PassThroughCipher class cipher.
     */
    private static final String ID = "plaintext";
    /**
     * Label for PassThroughCipher class cipher.
     */
    private static final String LABEL = "Plaintext Pass-Through Cipher";

    /**
     * Cipher that does nothing.
     * @param cipher ignored.
     */
    public PassThroughCipher(final Cipher cipher) {
        super(cipher);
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
     * @param plainText - The bytes we want encrypted.
     * @return returns the input.
     * @throws IonicException - thrown if an encrypt fails.
     */
    @Override
    public final byte[] encrypt(final byte[] plainText) throws IonicException {
        return plainText;
    }

    /**
     * @param plainText - The bytes we want encrypted.
     * @return returns the input.
     * @throws IonicException - thrown if an encrypt fails.
     */
    @Override
    public final byte[] encrypt(final String plainText) throws IonicException {
        return Transcoder.utf8().decode(plainText);
    }

    /**
     * @param cipherText - The bytes we want decrypted.
     * @return returns the input.
     * @throws IonicException thrown if decrypt fails.
     */
    @Override
    public final byte[] decrypt(final byte[] cipherText) throws IonicException {
        return cipherText;
    }
}
