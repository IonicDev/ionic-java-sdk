package com.ionic.sdk.agent.cipher.raw;

import com.ionic.sdk.agent.cipher.data.DecryptAttributes;
import com.ionic.sdk.agent.cipher.data.EncryptAttributes;
import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.nio.ByteBuffer;

/**
 * An implementation of an {@link AesCtrCipher} wrapper, with a user-provided {@link KeyServices} implementation
 * used to broker cryptography key creates and fetches.
 */
public class RawCipherAesCtr {

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * Constructor.
     *
     * @param agent the key services implementation
     */
    public RawCipherAesCtr(final KeyServices agent) {
        this.agent = agent;
    }

    /**
     * Encrypt some plain text with a newly created key provided by the {@link KeyServices} implementation.
     *
     * @param plainText         the plain text input to be encrypted
     * @param encryptAttributes the Ionic key attributes to be associated with the newly created key
     * @return the cipher text resulting from the encryption operation
     * @throws IonicException on key create failure, cryptography operation failure
     */
    public final byte[] encrypt(final byte[] plainText, final EncryptAttributes encryptAttributes)
            throws IonicException {
        SdkData.checkNotNull(agent, KeyServices.class.getName());
        SdkData.checkNotNull(plainText, byte[].class.getName());
        SdkData.checkNotNull(encryptAttributes, EncryptAttributes.class.getName());
        final byte[] cipherText = new byte[plainText.length + AesCipher.SIZE_IV];
        final ByteBuffer plainBuffer = ByteBuffer.wrap(plainText);
        final ByteBuffer cipherBuffer = ByteBuffer.wrap(cipherText);
        final CreateKeysResponse createKeysResponse = agent.createKey(encryptAttributes.getKeyAttributes(),
                encryptAttributes.getMutableKeyAttributes(), encryptAttributes.getMetadata());
        final CreateKeysResponse.Key key = createKeysResponse.getFirstKey();
        encryptAttributes.setKeyResponse(key);
        final AesCtrCipher cipher = new AesCtrCipher(key.getSecretKey());
        final int count = cipher.encrypt(plainBuffer, cipherBuffer);
        SdkData.checkTrue(cipherText.length == count, SdkError.ISAGENT_ERROR);
        return cipherText;
    }

    /**
     * Decrypt some cipher text, previously encrypted using this class, with a key fetched by the
     * {@link KeyServices} implementation.
     *
     * @param cipherText        the cipher text to be decrypted
     * @param keyId             the id associated with the key used to encrypt the input data
     * @param decryptAttributes the Ionic key attributes associated with the fetched key
     * @return the plain text resulting from the decryption operation
     * @throws IonicException on key fetch failure, cryptography operation failure
     */
    public final byte[] decrypt(final byte[] cipherText, final String keyId, final DecryptAttributes decryptAttributes)
            throws IonicException {
        SdkData.checkNotNull(agent, KeyServices.class.getName());
        SdkData.checkNotNull(cipherText, byte[].class.getName());
        SdkData.checkNotNull(keyId, AgentKey.class.getName());
        SdkData.checkNotNull(decryptAttributes, DecryptAttributes.class.getName());
        final byte[] plainText = new byte[cipherText.length - AesCipher.SIZE_IV];
        final ByteBuffer cipherBuffer = ByteBuffer.wrap(cipherText);
        final ByteBuffer plainBuffer = ByteBuffer.wrap(plainText);
        final GetKeysResponse getKeysResponse = agent.getKey(keyId, decryptAttributes.getMetadata());
        final GetKeysResponse.Key key = getKeysResponse.getFirstKey();
        decryptAttributes.setKeyResponse(key);
        final AesCtrCipher cipher = new AesCtrCipher(key.getSecretKey());
        final int count = cipher.decrypt(plainBuffer, cipherBuffer);
        SdkData.checkTrue(plainText.length == count, SdkError.ISAGENT_ERROR);
        return plainText;
    }
}
