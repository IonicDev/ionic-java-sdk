package com.ionic.sdk.agent.cipher.chunk;

/**
 * Data class used to describe attributes of a data chunk.
 *
 * @deprecated Please migrate usages to the drop-in replacement class
 * {@link com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoChunkInfo}.
 */
@Deprecated
public class ChunkCryptoChunkInfo {

    /**
     * Determines whether the chunk is encrypted or not.
     */
    private final boolean isEncrypted;

    /**
     * Gets the key ID that was used to encrypt the data chunk.
     */
    private final String keyId;

    /**
     * Determines the cipher ID of the data chunk.
     */
    private final String cipherId;

    /**
     * Gets the starting index of the payload section of the data chunk.
     */
    private final long payloadStart;

    /**
     * Gets the size of the payload section of the data chunk.
     */
    private final long payloadSize;

    /**
     * Constructor.
     *
     * @param isEncrypted  the chunk encryption status
     * @param keyId        the id of the Ionic crypto key
     * @param cipherId     the type of cipher used to encrypt the chunk
     * @param payloadStart the payload starting index
     * @param payloadSize  the payload length in characters
     */
    public ChunkCryptoChunkInfo(final boolean isEncrypted, final String keyId, final String cipherId,
                                final long payloadStart, final long payloadSize) {
        this.isEncrypted = isEncrypted;
        this.keyId = keyId;
        this.cipherId = cipherId;
        this.payloadStart = payloadStart;
        this.payloadSize = payloadSize;
    }

    /**
     * Determines whether the chunk is encrypted or not.
     *
     * @return the chunk encryption status
     */
    public final boolean isEncrypted() {
        return isEncrypted;
    }

    /**
     * Gets the key ID that was used to encrypt the data chunk.
     *
     * @return the id of the Ionic crypto key
     */
    public final String getKeyId() {
        return keyId;
    }

    /**
     * Determines the cipher ID of the data chunk.
     *
     * @return the type of cipher used to encrypt the chunk
     */
    public final String getCipherId() {
        return cipherId;
    }

    /**
     * Gets the starting index of the payload section of the data chunk.
     *
     * @return the payload starting index
     */
    public final long getPayloadStart() {
        return payloadStart;
    }

    /**
     * Gets the size of the payload section of the data chunk.
     *
     * @return the payload size
     */
    public final long getPayloadSize() {
        return payloadSize;
    }
}
