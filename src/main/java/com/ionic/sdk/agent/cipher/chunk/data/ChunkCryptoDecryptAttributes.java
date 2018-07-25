package com.ionic.sdk.agent.cipher.chunk.data;

import com.ionic.sdk.agent.data.MetadataHolder;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;

/**
 * On an SDK decrypt operation, this class provides the ability to specify request metadata that should be sent
 * to the server along with the cryptography key request.
 * <p>
 * The key used for the decryption might have associated cryptography key attributes (fixed and mutable).  This
 * information is communicated back to the SDK caller in this object.
 */
public class ChunkCryptoDecryptAttributes extends MetadataHolder {

    /**
     * The key attributes.
     */
    private KeyAttributesMap keyAttributes;

    /**
     * The mutable attributes.
     */
    private KeyAttributesMap mutableAttributes;

    /**
     * The identifier for the cipher used in the decryption operation.
     */
    private String cipherId;

    /**
     * The key used in the decryption operation.
     */
    private GetKeysResponse.Key key;

    /**
     * The error response (if any) received from the server in the context of the get key request.
     */
    private AgentResponseBase serverErrorResponse;

    /**
     * Constructor.
     */
    public ChunkCryptoDecryptAttributes() {
        this.keyAttributes = new KeyAttributesMap();
        this.mutableAttributes = new KeyAttributesMap();
        this.cipherId = "";
        this.key = null;
        this.serverErrorResponse = null;
    }

    /**
     * @return the key attributes
     */
    public final KeyAttributesMap getKeyAttributes() {
        return keyAttributes;
    }

    /**
     * @return the mutable attributes
     * @deprecated
     *      Please migrate usages to the replacement {@link #getMutableKeyAttributes()}
     *      method (Ionic SDK 1.x API compatibility).
     */
    @Deprecated
    public final KeyAttributesMap getMutableAttributes() {
        return mutableAttributes;
    }

    /**
     * @return the mutable attributes
     */
    public final KeyAttributesMap getMutableKeyAttributes() {
        return mutableAttributes;
    }

    /**
     * @return the identifier for the cipher used in the decryption operation
     */
    public final String getCipherId() {
        return cipherId;
    }

    /**
     * @return the key used in the decryption operation
     */
    public final GetKeysResponse.Key getKey() {
        return key;
    }

    /**
     * @return the key used in the decryption operation
     */
    public final GetKeysResponse.Key getKeyResponse() {
        return key;
    }

    /**
     * @return the id of the key used in the decryption operation
     */
    public final String getKeyId() {
        return ((key == null) ? "" : key.getId());
    }

    /**
     * @return the origin of the key used in the decryption operation
     */
    public final String getKeyOrigin() {
        return ((key == null) ? "" : key.getOrigin());
    }

    /**
     * @return the error response (if any) received from the server in the context of the request
     */
    public final AgentResponseBase getServerErrorResponse() {
        return serverErrorResponse;
    }

    /**
     * Set the immutable attributes associated with the decryption key.
     *
     * @param keyAttributes the key attributes associated with the decryption key
     */
    public final void setKeyAttributes(final KeyAttributesMap keyAttributes) {
        this.keyAttributes = keyAttributes;
    }

    /**
     * Set the mutable attributes associated with the decryption key.
     *
     * @param mutableAttributes the mutable attributes associated with the decryption key
     */
    public final void setMutableAttributes(final KeyAttributesMap mutableAttributes) {
        this.mutableAttributes = mutableAttributes;
    }

    /**
     * Set the cipher identifier associated with the decryption.
     *
     * @param cipherId the identifier for the cipher used in the decryption operation
     */
    public final void setCipherId(final String cipherId) {
        this.cipherId = cipherId;
    }

    /**
     * Set the cryptography key from the response.
     *
     * @param key the key used in the decryption operation
     */
    public final void setKey(final GetKeysResponse.Key key) {
        this.key = key;
    }

    /**
     * Set the server error received in the response.
     *
     * @param serverErrorResponse the error response (if any) received from the server in the context of the request
     */
    public final void setServerErrorResponse(final AgentResponseBase serverErrorResponse) {
        this.serverErrorResponse = serverErrorResponse;
    }
}
