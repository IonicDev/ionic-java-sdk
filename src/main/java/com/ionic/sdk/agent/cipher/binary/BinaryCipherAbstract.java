package com.ionic.sdk.agent.cipher.binary;

import com.ionic.sdk.agent.cipher.data.DecryptAttributes;
import com.ionic.sdk.agent.cipher.data.EncryptAttributes;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.BytePattern;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.nio.ByteBuffer;

/**
 * Cipher implementation specialized for dealing with binary data.  As with
 * {@link com.ionic.sdk.agent.cipher.chunk.ChunkCipherAbstract} implementations, the Machina key tag is incorporated
 * into the output ciphertext.  This makes the plaintext recoverable, given only the ciphertext as input.
 */
public abstract class BinaryCipherAbstract {

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices keyServices;

    /**
     * Constructor.
     *
     * @param keyServices the key services implementation
     */
    public BinaryCipherAbstract(final KeyServices keyServices) {
        this.keyServices = keyServices;
    }

    /**
     * @return the key services implementation; used to broker key transactions and crypto operations
     */
    public final KeyServices getKeyServices() {
        return keyServices;
    }

    /**
     * Encrypt the input plaintext using the provided {@link EncryptAttributes}.
     *
     * @param plainText         the plaintext to be encrypted
     * @param encryptAttributes (in/out) the parameters to be used for the encryption; the encryption result parameters
     * @return the encoded ciphertext for the given input
     * @throws IonicException on cryptography failures
     */
    public final byte[] encrypt(final byte[] plainText, final EncryptAttributes encryptAttributes)
            throws IonicException {
        // check input
        SdkData.checkNotNull(keyServices, KeyServices.class.getName());
        SdkData.checkNotNull(plainText, byte[].class.getName());
        SdkData.checkNotNull(encryptAttributes, EncryptAttributes.class.getName());
        // obtain Machina key
        final CreateKeysResponse createKeysResponse = keyServices.createKey(encryptAttributes.getKeyAttributes(),
                encryptAttributes.getKeyAttributes(), encryptAttributes.getMetadata());
        final CreateKeysResponse.Key createKey = createKeysResponse.getFirstKey();  // one key expected
        encryptAttributes.setKeyResponse(createKey);
        final ByteBuffer plainBuffer = ByteBuffer.wrap(plainText);
        // perform implementation-specific encryption
        return encryptInternal(plainBuffer, encryptAttributes);
    }

    /**
     * Implementation-specific algorithm for encryption of input plaintext.
     *
     * @param plainBuffer       the plaintext to be encrypted
     * @param encryptAttributes (in/out) the parameters to be used for the encryption; the encryption result parameters
     * @return the encoded ciphertext for the given input
     * @throws IonicException on cryptography failures
     */
    abstract byte[] encryptInternal(ByteBuffer plainBuffer, EncryptAttributes encryptAttributes) throws IonicException;

    /**
     * Decrypt the input ciphertext.
     *
     * @param cipherText        the ciphertext to be decrypted
     * @param decryptAttributes (in/out) the parameters to be used in the decryption; the decryption result parameters
     * @return the original plaintext associated with the given ciphertext input
     * @throws IonicException on cryptography failures
     */
    public final byte[] decrypt(final byte[] cipherText, final DecryptAttributes decryptAttributes)
            throws IonicException {
        final byte[] delimiterExpected = Transcoder.utf8().decode(DELIMITER);
        // check input
        SdkData.checkNotNull(keyServices, KeyServices.class.getName());
        SdkData.checkNotNull(cipherText, byte[].class.getName());
        SdkData.checkNotNull(decryptAttributes, DecryptAttributes.class.getName());
        // setup for cryptography (allocate space)
        final ByteBuffer cipherBuffer = ByteBuffer.wrap(cipherText);
        final int offsetDelimiter = BytePattern.findIn(cipherText, 0, delimiterExpected);
        final byte[] keyTagBytes = new byte[offsetDelimiter];
        cipherBuffer.get(keyTagBytes);
        final String keyTag = Transcoder.utf8().encode(keyTagBytes);
        final byte delimiter = cipherBuffer.get();
        SdkData.checkTrue(delimiter == delimiterExpected[0], SdkError.ISAGENT_INVALIDVALUE);
        // obtain Machina key
        final GetKeysResponse getKeysResponse = keyServices.getKey(keyTag, decryptAttributes.getMetadata());
        final GetKeysResponse.Key key = getKeysResponse.getFirstKey();
        decryptAttributes.setKeyResponse(key);
        SdkData.checkTrue(key.getId().equals(keyTag), SdkError.ISAGENT_BADRESPONSE);
        // perform implementation-specific decryption
        return decryptInternal(cipherBuffer, decryptAttributes);
    }

    /**
     * Implementation-specific algorithm for decryption of input ciphertext.
     *
     * @param cipherBuffer      the ciphertext to be decrypted
     * @param decryptAttributes (in/out) the parameters to be used in the decryption; the decryption result parameters
     * @return the original plaintext associated with the given ciphertext input
     * @throws IonicException on cryptography failures
     */
    abstract byte[] decryptInternal(ByteBuffer cipherBuffer, DecryptAttributes decryptAttributes) throws IonicException;

    /**
     * The defined delimiter for this cipher type.
     */
    public static final String DELIMITER = "~";
}
