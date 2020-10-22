package com.ionic.sdk.agent.cipher.data;

import com.ionic.sdk.agent.data.MetadataHolder;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.SdkData;

import java.util.Properties;

/**
 * Base class for attributes / metadata associated with an encryption operation.
 * <p>
 * On an Ionic SDK encrypt operation, this class provides the ability to specify cryptography key attributes (fixed
 * and mutable) and request metadata that should be sent to the server along with the cryptography key request.
 */
public class EncryptAttributes extends MetadataHolder {

    /**
     * The fixed attributes to be associated with the newly created key at the Ionic server.
     */
    private final KeyAttributesMap keyAttributes;

    /**
     * The mutable attributes to be associated with the newly created key at the Ionic server.
     */
    private final KeyAttributesMap mutableAttributes;

    /**
     * Arbitrary name-value pairs associated with this EncryptAttributes.
     */
    private final Properties properties;

    /**
     * Before encryption, container for attributes to be applied to operation.  After encryption, parameters applied
     * to operation.
     */
    private CreateKeysResponse.Key key;

    /**
     * The error response (if any) received from the server in the context of the create key request.
     */
    private AgentResponseBase serverErrorResponse;

    /**
     * Constructor.
     */
    public EncryptAttributes() {
        this.keyAttributes = new KeyAttributesMap();
        this.mutableAttributes = new KeyAttributesMap();
        this.properties = new Properties();
        this.key = null;
        this.serverErrorResponse = null;
    }

    /**
     * Constructor.
     *
     * @param encryptAttributes an existing object with data to populate this new object
     */
    public EncryptAttributes(final EncryptAttributes encryptAttributes) {
        this.keyAttributes = encryptAttributes.keyAttributes;
        this.mutableAttributes = encryptAttributes.mutableAttributes;
        this.properties = encryptAttributes.properties;
        super.setMetadata(encryptAttributes.getMetadata());
        this.key = null;
        this.serverErrorResponse = null;
    }

    /**
     * @return the cryptography key used by the cipher's encryption operation
     */
    public final CreateKeysResponse.Key getKeyResponse() {
        return key;
    }

    /**
     * @return the cryptography key used by the cipher's encryption operation
     * @deprecated Please migrate usages to the replacement {@link #getKeyResponse()} method.
     */
    @Deprecated
    public CreateKeysResponse.Key getKey() {
        return key;
    }

    /**
     * Set the cryptography key used by the cipher's encryption operation.  This key is set by the SDK in the
     * context of the encrypt call from data received by the {@link com.ionic.sdk.key.KeyServices} implementation.
     * <p>
     * Ionic SDK clients should not call this function.  Any value set prior to the encryption operation will cause
     * an {@link com.ionic.sdk.error.IonicException} to be thrown by the operation.
     *
     * @param keyIn the key used in the encryption operation
     */
    @InternalUseOnly
    public final void setKeyResponse(final CreateKeysResponse.Key keyIn) {
        SdkData.checkNotNullNPE(keyIn, CreateKeysResponse.Key.class.getName());
        key = keyIn;
    }

    /**
     * @return the keyId String populated by the {@link #setKeyResponse(CreateKeysResponse.Key)} call
     * during the cipher's encrypt operation
     */
    public final String getKeyId() {
        return (key == null) ? "" : key.getId();
    }

    /**
     * @return the keyOrigin String populated by the {@link #setKeyResponse(CreateKeysResponse.Key)} call
     * during the cipher's encrypt operation
     */
    public final String getKeyOrigin() {
        return (key == null) ? "" : key.getOrigin();
    }

    /**
     * Set the fixed attributes to be associated with the newly created key at the Ionic server.
     *
     * @param keyAttributesIn the key attributes map
     */
    public void setKeyAttributes(final KeyAttributesMap keyAttributesIn) {
        keyAttributes.clear();
        if (keyAttributesIn != null) {
            keyAttributes.putAll(keyAttributesIn);
        }
    }

    /**
     * Get the fixed attributes to be associated with the newly created key at the Ionic server.
     *
     * @return KeyAttributesMap attributes
     */
    public KeyAttributesMap getKeyAttributes() {
        return keyAttributes;
    }

    /**
     * Set the mutable attributes to be associated with the newly created key at the Ionic server.
     *
     * @param mutableAttributesIn the mutable attributes map
     */
    public void setMutableKeyAttributes(final KeyAttributesMap mutableAttributesIn) {
        mutableAttributes.clear();
        if (mutableAttributesIn != null) {
            mutableAttributes.putAll(mutableAttributesIn);
        }
    }

    /**
     * Set the mutable attributes to be associated with the newly created key at the Ionic server.
     *
     * @param mutableAttributesIn the mutable attributes map
     * @deprecated Please migrate usages to the replacement {@link #setMutableKeyAttributes(KeyAttributesMap)} method.
     */
    @Deprecated
    public void setMutableAttributes(final KeyAttributesMap mutableAttributesIn) {
        mutableAttributes.clear();
        if (mutableAttributesIn != null) {
            mutableAttributes.putAll(mutableAttributesIn);
        }
    }

    /**
     * Get the mutable attributes to be associated with the newly created key at the Ionic server.
     *
     * @return KeyAttributesMap attributes
     */
    public KeyAttributesMap getMutableKeyAttributes() {
        return mutableAttributes;
    }

    /**
     * Get the mutable attributes to be associated with the newly created key at the Ionic server.
     *
     * @return KeyAttributesMap attributes
     * @deprecated Please migrate usages to the replacement {@link #getMutableKeyAttributes()} method.
     */
    @Deprecated
    public KeyAttributesMap getMutableAttributes() {
        return mutableAttributes;
    }

    /**
     * Set a configuration property string by name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     */
    public final void setProperty(final String name, final String value) {
        properties.setProperty(name, value);
    }

    /**
     * Get a configuration property string by name.
     *
     * @param name The parameter name.
     * @return The value of the named configuration property.
     */
    public final String getProperty(final String name) {
        return properties.getProperty(name);
    }

    /**
     * @return the error response (if any) received from the server in the context of the request
     */
    public final AgentResponseBase getServerErrorResponse() {
        return serverErrorResponse;
    }

    /**
     * Set the error received from the Ionic server in response to the CreateKey request.  The value is set by the
     * SDK in the context of the encrypt call from data received by the {@link com.ionic.sdk.key.KeyServices}
     * implementation.
     * <p>
     * Ionic SDK clients should not call this function.  Any value set prior to the encryption operation will cause
     * an {@link com.ionic.sdk.error.IonicException} to be thrown by the operation.
     *
     * @param serverErrorResponseIn the error response (if any) received from the server in the context of the request
     */
    @InternalUseOnly
    public final void setServerErrorResponse(final AgentResponseBase serverErrorResponseIn) {
        serverErrorResponse = serverErrorResponseIn;
    }
}
