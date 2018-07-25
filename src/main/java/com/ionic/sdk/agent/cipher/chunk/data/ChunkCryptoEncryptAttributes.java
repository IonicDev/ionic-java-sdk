package com.ionic.sdk.agent.cipher.chunk.data;

import com.ionic.sdk.agent.data.MetadataHolder;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;

/**
 * On an SDK encrypt operation, this class provides the ability to specify cryptography key attributes (fixed
 * and mutable) and request metadata that should be sent to the server along with the cryptography key request.
 */
public class ChunkCryptoEncryptAttributes extends MetadataHolder {

    /**
     * The key attributes.
     */
    private final KeyAttributesMap keyAttributes;

    /**
     * The mutable attributes.
     */
    private final KeyAttributesMap mutableAttributes;

    /**
     * The identifier for the cipher used in the encryption operation.
     */
    private String cipherId;

    /**
     * The key used in the encryption operation.
     */
    private CreateKeysResponse.Key key;

    /**
     * The error response (if any) received from the server in the context of the create key request.
     */
    private AgentResponseBase serverErrorResponse;

    /**
     * Constructor.
     */
    public ChunkCryptoEncryptAttributes() {
        this.keyAttributes = new KeyAttributesMap();
        this.mutableAttributes = new KeyAttributesMap();
        this.cipherId = "";
        this.key = null;
        this.serverErrorResponse = null;
    }

    /**
     * Constructor.
     *
     * @param keyAttributes the key attributes
     */
    public ChunkCryptoEncryptAttributes(final KeyAttributesMap keyAttributes) {
        this.keyAttributes = new KeyAttributesMap(keyAttributes);
        this.mutableAttributes = new KeyAttributesMap();
        this.cipherId = "";
        this.key = null;
        this.serverErrorResponse = null;
    }

    /**
     * Constructor.
     *
     * @param keyAttributes     the key attributes
     * @param mutableAttributes the mutable attributes
     */
    public ChunkCryptoEncryptAttributes(
            final KeyAttributesMap keyAttributes, final KeyAttributesMap mutableAttributes) {
        this.keyAttributes = new KeyAttributesMap(keyAttributes);
        this.mutableAttributes = new KeyAttributesMap(mutableAttributes);
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
     * Set the key attributes to be associated with keys generated this chunk encrypt operation.
     *
     * @param keyAttributes the key attributes for any keys generated during this operation
     */
    public final void setKeyAttributes(final KeyAttributesMap keyAttributes) {
        this.keyAttributes.clear();
        this.keyAttributes.putAll(keyAttributes);
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
     * Set the mutable key attributes to be associated with keys generated this chunk encrypt operation.
     *
     * @param mutableKeyAttributes the mutable key attributes for any keys generated during this operation
     */
    public final void setMutableKeyAttributes(final KeyAttributesMap mutableKeyAttributes) {
        this.mutableAttributes.clear();
        this.mutableAttributes.putAll(mutableKeyAttributes);
    }

    /**
     * @return the identifier for the cipher used in the encryption operation
     */
    public final String getCipherId() {
        return cipherId;
    }

    /**
     * @return the key used in the encryption operation
     */
    public final CreateKeysResponse.Key getKey() {
        return key;
    }

    /**
     * @return the key used in the encryption operation
     */
    public final CreateKeysResponse.Key getKeyResponse() {
        return key;
    }

    /**
     * @return the id of the key used in the encryption operation
     */
    public final String getKeyId() {
        return ((key == null) ? "" : key.getId());
    }

    /**
     * @return the origin of the key used in the encryption operation
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
     * Set the cipher identifier associated with the encryption.
     *
     * @param cipherId the identifier for the cipher used in the encryption operation
     */
    public final void setCipherId(final String cipherId) {
        this.cipherId = cipherId;
    }

    /**
     * Set the cryptography key from the response.
     *
     * @param key the key used in the encryption operation
     */
    public final void setKey(final CreateKeysResponse.Key key) {
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
