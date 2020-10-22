package com.ionic.sdk.agent.cipher.chunk;

import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoChunkInfo;
import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoEncryptAttributes;
import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;

/**
 * Ionic Machina Tools chunk crypto implementation, version 2.  This wrapper object can be used to abstract Machina
 * cryptography operations.  This format is an AES-CTR based encryption scheme, encoded in Base64, and using
 * delimiters that are shorter than the "legacy" ones in {@link ChunkCipherV1}.
 * <p>
 * AES-CTR (Counter Mode) is a streaming cipher variant of AES where the next key stream block is calculated by
 * encrypting increasing values of a "counter".
 * <p>
 * ChunkCipherV2 is the default cipher used by the {@link ChunkCipherAuto} utility class.
 * <p>
 * Sample:
 * <pre>
 * public final void testChunkCipherV2_EncryptDecryptString() throws IonicException {
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final String plainText = "Hello, Machina!";
 *     final ChunkCipherAbstract chunkCipher = new ChunkCipherV2(keyServices);
 *     final String cipherText = chunkCipher.encrypt(plainText);
 *     final String plainTextRecover = chunkCipher.decrypt(cipherText);
 *     Assert.assertEquals(plainText, plainTextRecover);
 * }
 * </pre>
 * <p>
 * See <a href='https://dev.ionic.com/sdk/formats/chunk' target='_blank'>Machina Developers</a> for more information
 * on the different chunk crypto data formats.
 */
public class ChunkCipherV2 extends ChunkCipherAbstract {

    /**
     * Constructor.
     *
     * @param agent the key services implementation
     */
    public ChunkCipherV2(final KeyServices agent) {
        super(agent);
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
    public static final String ID = "ChunkV2";

    /**
     * The defined delimiter for the start of the ciphertext used by this chunk cipher type.
     */
    private static final String DELIMITER_CIPHERTEXT_START = "!";

    /**
     * The defined delimiter for the end of the ciphertext used by this chunk cipher type.
     */
    private static final String DELIMITER_CIPHERTEXT_END = "!";

    /**
     * The defined delimiter for the start of the key tag used by this chunk cipher type.
     */
    private static final String DELIMITER_KEYTAG_START = "~!2!";

    /**
     * @return The token used to mark the start of the ciphertext for a ChunkCipherV2 encrypted string.
     */
    @Override
    protected final String getDelimiterCiphertextStart() {
        return DELIMITER_CIPHERTEXT_START;
    }

    /**
     * @return The token used to mark the end of the ciphertext for a ChunkCipherV2 encrypted string.
     */
    @Override
    protected final String getDelimiterCiphertextEnd() {
        return DELIMITER_CIPHERTEXT_END;
    }

    /**
     * @return The token used to mark the start of the key tag for a ChunkCipherV2 encrypted string.
     */
    // ${IONIC_REPO_ROOT}\IonicAgents\SDK\ISAgentSDK\ISChunkCryptoLib\ISChunkCryptoCipherV2.cpp
    @Override
    protected final String getDelimiterKeyTagStart() {
        return DELIMITER_KEYTAG_START;
    }

    /**
     * Inspect the parameter data to determine the relevant Ionic chunk cipher used to encrypt it.
     *
     * @param data the text data to be inspected
     * @return an info object which may be used to decrypt the parameter data; or null if the data is not understood
     */
    @Override
    public final ChunkCryptoChunkInfo getChunkInfo(final String data) {
        return getChunkInfoInternal(data);
    }

    /**
     * Encrypt some text, using Ionic infrastructure to abstract away the key management and cryptography.
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
        return encryptInternal(Transcoder.utf8().decode(plainText), encryptAttributes);
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
     * @param plainText         some bytes to be encrypted
     * @param encryptAttributes the attributes to pass along to the key created by the operation
     * @return the Ionic encoded encrypted representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final String encrypt(
            final byte[] plainText, final ChunkCryptoEncryptAttributes encryptAttributes) throws IonicException {
        return encryptInternal(plainText, encryptAttributes);
    }

    @Override
    protected final String encryptInternal(final AgentKey key, final byte[] plainText,
                                           final ChunkCryptoEncryptAttributes encryptAttributes) throws IonicException {
        final AesCtrCipher cipher = new AesCtrCipher();
        cipher.setMetadata(encryptAttributes.getMetadata());
        cipher.setKey(key.getKey());
        return cipher.encryptToBase64(plainText);
    }

    /**
     * Decrypt some text, using Ionic infrastructure to abstract away the key management and cryptography.
     *
     * @param cipherText some text (previously encrypted with an instance of this agent) to be decrypted
     * @return the plainText representation of the input
     * @throws IonicException on cryptography errors
     */
    @Override
    public final String decrypt(final String cipherText) throws IonicException {
        return Transcoder.utf8().encode(decryptInternal(cipherText, new ChunkCryptoDecryptAttributes()));
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
        return Transcoder.utf8().encode(decryptInternal(cipherText, decryptAttributes));
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
        return Transcoder.utf8().encode(decryptInternal(
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
        return Transcoder.utf8().encode(decryptInternal(Transcoder.utf8().encode(cipherText), decryptAttributes));
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
        return decryptInternal(cipherText, new ChunkCryptoDecryptAttributes());
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
        return decryptInternal(cipherText, decryptAttributes);
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
        final AesCtrCipher cipher = new AesCtrCipher();
        cipher.setKey(key.getKey());
        return cipher.decryptBase64(cipherTextBase64);
    }
}
