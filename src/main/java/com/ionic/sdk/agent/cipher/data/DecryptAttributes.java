package com.ionic.sdk.agent.cipher.data;

import com.ionic.sdk.agent.data.MetadataHolder;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.SdkData;

/**
 * Base class for attributes / metadata associated with a decryption operation.
 */
public class DecryptAttributes extends MetadataHolder {

    /**
     * The Ionic cryptography key used in the decryption operation.
     */
    private GetKeysResponse.Key key;

    /**
     * The error response (if any) received from the server in the context of the get key request.
     */
    private AgentResponseBase serverErrorResponse;

    /**
     * Constructor.
     */
    public DecryptAttributes() {
        this.key = null;
        this.serverErrorResponse = null;
    }

    /**
     * @return the key used in the decryption operation
     */
    public final GetKeysResponse.Key getKeyResponse() {
        return key;
    }

    /**
     * @return the key used in the decryption operation
     * @deprecated Please migrate usages to the replacement {@link #getKeyResponse()} method.
     */
    @Deprecated
    public GetKeysResponse.Key getKey() {
        return key;
    }

    /**
     * Set the cryptography key used by the cipher's decryption operation.  This key is set by the SDK in the
     * context of the decrypt call from data received by the {@link com.ionic.sdk.key.KeyServices} implementation.
     * <p>
     * Ionic SDK clients should not call this function.  Any value set prior to the decryption operation will cause
     * an {@link com.ionic.sdk.error.IonicException} to be thrown by the operation.
     *
     * @param keyIn the key used in the decryption operation
     */
    @InternalUseOnly
    public final void setKeyResponse(final GetKeysResponse.Key keyIn) {
        SdkData.checkNotNullNPE(keyIn, GetKeysResponse.Key.class.getName());
        key = keyIn;
    }

    /**
     * @return the keyId String populated by the {@link #setKeyResponse(GetKeysResponse.Key)} call
     * during the cipher's decrypt operation
     */
    public final String getKeyId() {
        return ((key == null) ? "" : key.getId());
    }

    /**
     * @return the keyOrigin String populated by the {@link #setKeyResponse(GetKeysResponse.Key)} call
     * during the cipher's decrypt operation
     */
    public final String getKeyOrigin() {
        return ((key == null) ? "" : key.getOrigin());
    }

    /**
     * Get the fixed attributes associated with the key retrieved from the Ionic server.
     *
     * @return KeyAttributesMap attributes
     */
    public KeyAttributesMap getKeyAttributes() {
        return ((key == null) ? new KeyAttributesMap() : key.getAttributesMap());
    }

    /**
     * Get the mutable attributes associated with the key retrieved from the Ionic server.
     *
     * @return KeyAttributesMap attributes
     */
    public KeyAttributesMap getMutableKeyAttributes() {
        return ((key == null) ? new KeyAttributesMap() : key.getMutableAttributesMap());
    }

    /**
     * Get the mutable attributes associated with the key retrieved from the Ionic server.
     *
     * @return KeyAttributesMap attributes
     * @deprecated Please migrate usages to the replacement {@link #getMutableKeyAttributes()} method.
     */
    @Deprecated
    public KeyAttributesMap getMutableAttributes() {
        return ((key == null) ? new KeyAttributesMap() : key.getMutableAttributesMap());
    }

    /**
     * @return the error response (if any) received from the server in the context of the GetKey request
     */
    public final AgentResponseBase getServerErrorResponse() {
        return serverErrorResponse;
    }

    /**
     * Set the error received from the Ionic server in response to the GetKey request.  The value is set by the
     * SDK in the context of the decrypt call from data received by the {@link com.ionic.sdk.key.KeyServices}
     * implementation.
     * <p>
     * Ionic SDK clients should not call this function.  Any value set prior to the decryption operation will cause
     * an {@link com.ionic.sdk.error.IonicException} to be thrown by the operation.
     *
     * @param serverErrorResponseIn the error response (if any) received from the server in the context of the request
     */
    @InternalUseOnly
    public final void setServerErrorResponse(final AgentResponseBase serverErrorResponseIn) {
        serverErrorResponse = serverErrorResponseIn;
    }
}
