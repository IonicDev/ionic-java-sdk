package com.ionic.sdk.agent.cipher.chunk.data;

/**
 * Data class used to encapsulate and describe the data formatting attributes of a chunk of Ionic
 * Machina-encrypted text.
 */
public class ChunkCryptoChunkInfo {

    /**
     * Determines whether the chunk is encrypted or not.
     */
    private boolean isEncrypted;

    /**
     * Gets the key ID that was used to encrypt the data chunk.
     */
    private String keyId;

    /**
     * Determines the cipher ID of the data chunk.
     */
    private String cipherId;

    /**
     * Gets the starting index of the payload section of the data chunk.
     */
    private long payloadStart;

    /**
     * Gets the size of the payload section of the data chunk.
     */
    private long payloadSize;

    /**
     * Constructor.
     */
    public ChunkCryptoChunkInfo() {
        this.isEncrypted = false;
        this.keyId = "";
        this.cipherId = "";
        this.payloadStart = 0;
        this.payloadSize = 0;
    }

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
     * Sets whether the chunk is encrypted or not.
     *
     * @param encrypted the chunk encryption status
     */
    public final void setEncrypted(final boolean encrypted) {
        isEncrypted = encrypted;
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
     * Sets the key ID that was used to encrypt the data chunk.
     *
     * @param keyId the id of the Ionic crypto key
     */
    public final void setKeyId(final String keyId) {
        this.keyId = keyId;
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
     * Determines the cipher ID of the data chunk.
     *
     * @param cipherId the type of cipher used to encrypt the chunk
     */
    public final void setCipherId(final String cipherId) {
        this.cipherId = cipherId;
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
     * Sets the starting index of the payload section of the data chunk.
     *
     * @param payloadStart the payload starting index
     */
    public final void setPayloadStart(final long payloadStart) {
        this.payloadStart = payloadStart;
    }

    /**
     * Gets the size of the payload section of the data chunk.
     *
     * @return the payload size
     */
    public final long getPayloadSize() {
        return payloadSize;
    }

    /**
     * Sets the size of the payload section of the data chunk.
     *
     * @param payloadSize the payload size
     */
    public final void setPayloadSize(final long payloadSize) {
        this.payloadSize = payloadSize;
    }
}
