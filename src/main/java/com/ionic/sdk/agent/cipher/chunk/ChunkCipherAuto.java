package com.ionic.sdk.agent.cipher.chunk;

import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoChunkInfo;
import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoEncryptAttributes;
import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

/**
 * Ionic Machina Tools chunk crypto implementation.  This wrapper object can be used to abstract Ionic
 * cryptography operations.  Instances of this class use underlying Machina chunk cipher implementations to perform
 * operations:
 * <ul>
 * <li>encryption operations are performed using the default cipher: {@link ChunkCipherV2},</li>
 * <li>decryption operations are performed using the cipher format used to produce the ciphertext.</li>
 * </ul>
 * <p>
 * Sample:
 * <pre>
 * public final void testChunkCipherAuto_EncryptDecryptString() throws IonicException {
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final String plainText = "Hello, Machina!";
 *     final ChunkCipherAbstract chunkCipher = new ChunkCipherAuto(keyServices);
 *     final String cipherText = chunkCipher.encrypt(plainText);
 *     final ChunkCryptoChunkInfo chunkInfo = chunkCipher.getChunkInfo(cipherText);
 *     Assert.assertEquals(ChunkCipherV2.ID, chunkInfo.getCipherId());
 *     final String plainTextRecover = chunkCipher.decrypt(cipherText);
 *     Assert.assertEquals(plainText, plainTextRecover);
 * }
 * </pre>
 * <p>
 * See <a href='https://dev.ionic.com/sdk/formats/chunk' target='_blank'>Machina Developers</a> for more information
 * on the different chunk crypto data formats.
 */
public class ChunkCipherAuto extends ChunkCipherAbstract {

    /**
     * The chunk ciphers which can be detected using this object.
     */
    private final ChunkCipherAbstract[] chunkCiphers;

    /**
     * The default chunk cipher implementation.
     */
    private final ChunkCipherAbstract chunkCipherDefault;

    /**
     * Constructor.
     *
     * @param agent the key services implementation
     */
    public ChunkCipherAuto(final KeyServices agent) {
        super(agent);
        final ChunkCipherV1 chunkCipherV1 = new ChunkCipherV1(agent);
        final ChunkCipherV2 chunkCipherV2 = new ChunkCipherV2(agent);
        final ChunkCipherV3 chunkCipherV3 = new ChunkCipherV3(agent);
        this.chunkCiphers = new ChunkCipherAbstract[]{
                chunkCipherV1, chunkCipherV2, chunkCipherV3,
        };
        this.chunkCipherDefault = chunkCipherV2;
    }

    /**
     * @return The text id of this cipher.
     */
    @Override
    public final String getId() {
        return ID;
    }

    /**
     * @return The text label of this cipher.
     */
    @Override
    public final String getLabel() {
        return ID;
    }

    /**
     * The text id of this cipher.
     */
    public static final String ID = "ChunkAuto";

    /**
     * Encrypt some text, using Ionic infrastructure to abstract away the key management and cryptography.
     * <p>
     * ChunkCipherAuto uses ChunkV2 by default to encrypt data.
     *
     * @param plainText some text to be encrypted
     * @return the Ionic encoded encrypted representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final String encrypt(final String plainText) throws IonicException {
        return encryptInternal(Transcoder.utf8().decode(plainText), new ChunkCryptoEncryptAttributes());
    }

    /**
     * Encrypt some text, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param plainText         some text to be encrypted
     * @param encryptAttributes the attributes to pass along to the key created by the operation
     * @return the Ionic encoded encrypted representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final String encrypt(
            final String plainText, final ChunkCryptoEncryptAttributes encryptAttributes) throws IonicException {
        return chunkCipherDefault.encryptInternal(Transcoder.utf8().decode(plainText), encryptAttributes);
    }

    /**
     * Encrypt some bytes, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param plainText some bytes to be encrypted
     * @return the Ionic encoded encrypted representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final String encrypt(final byte[] plainText) throws IonicException {
        return encryptInternal(plainText, new ChunkCryptoEncryptAttributes());
    }

    /**
     * Encrypt some bytes, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param plainText some bytes to be encrypted
     * @param encryptAttributes the attributes to pass along to the key created by the operation
     * @return the Ionic encoded encrypted representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final String encrypt(
            final byte[] plainText, final ChunkCryptoEncryptAttributes encryptAttributes) throws IonicException {
        return chunkCipherDefault.encryptInternal(plainText, encryptAttributes);
    }

    @Override
    protected final String encryptInternal(final AgentKey key, final byte[] plainText,
                                           final ChunkCryptoEncryptAttributes encryptAttributes) throws IonicException {
        return chunkCipherDefault.encryptInternal(key, plainText, encryptAttributes);
    }

    /**
     * Decrypt some text, using Ionic infrastructure to abstract away the key management and cryptography.
     * <p>
     * ChunkCipherAuto attempts to determine the cipher used to encrypt the data, and decrypts using that cipher type.
     *
     * @param cipherText some text (previously encrypted with an instance of this agent) to be decrypted
     * @return the plainText representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final String decrypt(final String cipherText) throws IonicException {
        return Transcoder.utf8().encode(decryptAuto(cipherText, new ChunkCryptoDecryptAttributes()));
    }

    /**
     * Decrypt some text, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param cipherText        some text (previously encrypted with an instance of this agent) to be decrypted
     * @param decryptAttributes the attributes to pass along from the key fetched by the operation
     * @return the plainText representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final String decrypt(
            final String cipherText, final ChunkCryptoDecryptAttributes decryptAttributes) throws IonicException {
        return Transcoder.utf8().encode(decryptAuto(cipherText, decryptAttributes));
    }

    /**
     * Decrypt some bytes, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param cipherText some bytes (previously encrypted with an instance of this agent) to be decrypted
     * @return the plainText representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final String decrypt(final byte[] cipherText) throws IonicException {
        return Transcoder.utf8().encode(decryptAuto(
                Transcoder.utf8().encode(cipherText), new ChunkCryptoDecryptAttributes()));
    }

    /**
     * Decrypt some bytes, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param cipherText        some bytes (previously encrypted with an instance of this agent) to be decrypted
     * @param decryptAttributes the attributes to pass along from the key fetched by the operation
     * @return the plainText representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final String decrypt(
            final byte[] cipherText, final ChunkCryptoDecryptAttributes decryptAttributes) throws IonicException {
        return Transcoder.utf8().encode(decryptAuto(Transcoder.utf8().encode(cipherText), decryptAttributes));
    }

    /**
     * Decrypt some text, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param cipherText some text (previously encrypted with an instance of this agent) to be decrypted
     * @return the plainText representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final byte[] decryptToBytes(final String cipherText) throws IonicException {
        return decryptAuto(cipherText, new ChunkCryptoDecryptAttributes());
    }

    /**
     * Decrypt some text, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param cipherText        some text (previously encrypted with an instance of this agent) to be decrypted
     * @param decryptAttributes the container for the key attributes from the Ionic server
     * @return the plainText representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final byte[] decryptToBytes(
            final String cipherText, final ChunkCryptoDecryptAttributes decryptAttributes) throws IonicException {
        return decryptAuto(cipherText, decryptAttributes);
    }

    /**
     * Decrypt some text, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param cipherText        some text (previously encrypted with an instance of this agent) to be decrypted
     * @param decryptAttributes the container for the key attributes from the Ionic server
     * @return the plainText representation of the input
     * @throws IonicException on cryptography errors
     */
    private byte[] decryptAuto(
            final String cipherText, final ChunkCryptoDecryptAttributes decryptAttributes) throws IonicException {
        final ChunkCryptoChunkInfo chunkInfo = getChunkInfoAuto(cipherText);
        // decrypt using the correct cipher out of those that are available
        for (ChunkCipherAbstract chunkCipher : chunkCiphers) {
            if (chunkInfo.getCipherId().equals(chunkCipher.getId())) {
                return chunkCipher.decryptToBytes(cipherText, decryptAttributes);
            }
        }
        // no cipher found that understands this data
        final int errorCode = SdkError.ISAGENT_INVALIDVALUE;
        throw new IonicException(errorCode, new IonicException(errorCode, cipherText));
    }

    /**
     * Decrypt some text, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param key              the Ionic key associated with the ciphertext
     * @param cipherTextBase64 some text (previously encrypted with an instance of this agent) to be decrypted
     * @return the plainText representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    protected final byte[] decryptInternal(final AgentKey key, final String cipherTextBase64) throws IonicException {
        // this function must be implemented in order to satisfy the inheritance contract, but is not valid
        // in context (implemented only in versioned chunk ciphers)
        final int errorCode = SdkError.ISCHUNKCRYPTO_ERROR;
        throw new IonicException(errorCode, new IonicException(errorCode, cipherTextBase64));
    }

    /**
     * Inspect the parameter data to determine the relevant Ionic chunk cipher used to encrypt it.
     *
     * @param data the text data to be inspected
     * @return an info object which may be used to decrypt the parameter data; or null if the data is not understood
     */
    @Override
    public final ChunkCryptoChunkInfo getChunkInfo(final String data) {
        return getChunkInfoAuto(data);
    }

    /**
     * Inspect the parameter data to determine the relevant Ionic chunk cipher used to encrypt it.
     *
     * @param data the text data to be inspected
     * @return an info object which may be used to decrypt the parameter data; or null if the data is not understood
     */
    private ChunkCryptoChunkInfo getChunkInfoAuto(final String data) {
        ChunkCryptoChunkInfo chunkInfo = new ChunkCryptoChunkInfo();
        for (final ChunkCipherAbstract chunkCipher : chunkCiphers) {
            final ChunkCryptoChunkInfo chunkInfoIt = chunkCipher.getChunkInfoInternal(data);
            if (chunkInfoIt != null) {
                chunkInfo = chunkInfoIt;
                break;
            }
        }
        return chunkInfo;
    }

    /**
     * @return The token used to mark the start of the ciphertext for a ChunkCipherV2 encrypted string.
     */
    @Override
    protected final String getDelimiterCiphertextStart() {
        return chunkCipherDefault.getDelimiterCiphertextStart();
    }

    /**
     * @return The token used to mark the end of the ciphertext for a ChunkCipherV2 encrypted string.
     */
    @Override
    protected final String getDelimiterCiphertextEnd() {
        return chunkCipherDefault.getDelimiterCiphertextEnd();
    }

    /**
     * @return The token used to mark the start of the key tag for a ChunkCipherV2 encrypted string.
     */
    // ${IONIC_REPO_ROOT}\IonicAgents\SDK\ISAgentSDK\ISChunkCryptoLib\ISChunkCryptoCipherV2.cpp
    @Override
    protected final String getDelimiterKeyTagStart() {
        return chunkCipherDefault.getDelimiterKeyTagStart();
    }
}
