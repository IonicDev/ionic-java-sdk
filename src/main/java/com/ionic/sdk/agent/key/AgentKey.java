package com.ionic.sdk.agent.key;

import java.util.Objects;

/**
 * Class that defines an abstract basic key container object used by Ionic.
 * Other classes may subclass AgentKey.
 */
public class AgentKey implements KeyBase, KeyMetadata {

    /**
     * A String denoting the id of the key.
     */
    private String keyId;

    /**
     * The key byte array.
     */
    private byte[] keyBytes;

    /**
     * The key attributes.
     */
    private KeyAttributesMap keyAttributes;

    /**
     * The (server-supplied) hash of the key attributes.
     */
    private String attributesSigBase64FromServer;

    /**
     * The mutable attributes.
     */
    private KeyAttributesMap mutableAttributes;

    /**
     * The mutable attributes (save copy to assist with possible future merge.
     */
    private KeyAttributesMap mutableAttributesFromServer;

    /**
     * The (server-supplied) hash of the mutable key attributes.
     */
    private String mutableAttributesSigBase64FromServer;

    /**
     * The key obligations.
     */
    private KeyObligationsMap keyObligations;

    /**
     * Constructs an empty AgentKey.
     */
    public AgentKey() {
        this("", new byte[0]);
    }

    /**
     * Copy constructor.
     *
     * @param key
     *      The key from which to copy attributes.
     */
    public AgentKey(final AgentKey key) {
        this(key.keyId, key.keyBytes, key.keyAttributes, key.mutableAttributes, key.keyObligations);
        this.attributesSigBase64FromServer = key.attributesSigBase64FromServer;
        this.mutableAttributesSigBase64FromServer = key.mutableAttributesSigBase64FromServer;
    }

    /**
     * Constructs a new AgentKey with supplied arguments.
     *
     * @param keyId
     *      The specified key id to initialize with.
     * @param keyBytes
     *      The specified key data to initialize with.
     * @throws NullPointerException
     *      When keyId or keyBytes are null.
     */
    public AgentKey(final String keyId, final byte[] keyBytes) throws NullPointerException {
        this(keyId, keyBytes, new KeyAttributesMap(), new KeyObligationsMap());
    }

    /**
     * Constructs a new AgentKey with supplied arguments.
     *
     * @param keyId
     *      The specified key id to initialize with.
     * @param keyBytes
     *      The specified key data to initialize with.
     * @param keyAttributes
     *      The specified key attributes to initialize with.
     * @param keyObligations
     *      The specified key obligations to initialize with.
     * @throws NullPointerException
     *      When keyId or keyBytes are null.
     */
    public AgentKey(final String keyId, final byte[] keyBytes,
        final KeyAttributesMap keyAttributes, final KeyObligationsMap keyObligations)
        throws NullPointerException {
        this(keyId, keyBytes, keyAttributes, new KeyAttributesMap(), keyObligations);
    }

    /**
     * Constructs a new AgentKey with supplied arguments.
     *
     * @param keyId
     *      The specified key id to initialize with.
     * @param keyBytes
     *      The specified key data to initialize with.
     * @param keyAttributes
     *      The specified key attributes to initialize with.
     * @param mutableAttributes
     *      The specified mutable attributes to initialize with.
     * @param keyObligations
     *      The specified key obligations to initialize with.
     * @throws NullPointerException
     *      When keyId or keyBytes are null.
     */
    public AgentKey(final String keyId, final byte[] keyBytes,
                    final KeyAttributesMap keyAttributes, final KeyAttributesMap mutableAttributes,
                    final KeyObligationsMap keyObligations) throws NullPointerException {

        if (keyBytes == null) {
            throw new NullPointerException("cannot have null keyBytes");
        }
        this.keyId = Objects.requireNonNull(keyId, "keyId must not be null");
        this.keyBytes = Objects.requireNonNull(keyBytes.clone(), "keyBytes must not be null");
        this.keyAttributes = new KeyAttributesMap(keyAttributes);
        this.mutableAttributes = new KeyAttributesMap(mutableAttributes);
        this.mutableAttributesFromServer = new KeyAttributesMap(mutableAttributes);
        this.keyObligations = new KeyObligationsMap(keyObligations);
        this.attributesSigBase64FromServer = "";
        this.mutableAttributesSigBase64FromServer = "";
    }

    /**
     * Get the id of the key.
     *
     * @return String key id.
     */
    @Override
    public final String getId() {
        return this.keyId;
    }

    /**
     * Set the id of the key.
     *
     * @param keyId
     *      The key id.
     * @throws NullPointerException
     *      When keyId is null.
     */
    @Override
    public final void setId(final String keyId) throws NullPointerException {
        this.keyId = Objects.requireNonNull(keyId, "keyId must not be null");
    }

    /**
     * Get the key data.
     *
     * @return byte[] key bytes.
     */
    @Override
    public final byte[] getKey() {
        return this.keyBytes.clone();
    }

    /**
     * Set the key data.
     *
     * @param keyBytes
     *      The key bytes.
     * @throws NullPointerException
     *      When keyBytes is null.
     */
    @Override
    public final void setKey(final byte[] keyBytes) throws NullPointerException {
        this.keyBytes = Objects.requireNonNull(keyBytes.clone(), "keyBytes must not be null");
    }

    /**
     * Get the key attributes.
     *
     * @return KeyAttributesMap attributes.
     */
    @Override
    public final KeyAttributesMap getAttributesMap() {
        return this.keyAttributes;
    }

    /**
     * Set the key attributes map.
     *
     * @param keyAttributes
     *      The key attributes map.
     */
    @Override
    public final void setAttributesMap(final KeyAttributesMap keyAttributes) {
        this.keyAttributes = keyAttributes;
    }

    /**
     * Get the mutable attributes map.
     *
     * @return KeyAttributesMap attributes.
     */
    @Override
    public final KeyAttributesMap getMutableAttributes() {
        return this.mutableAttributes;
    }

    /**
     * Set the mutable attributes map.
     *
     * @param mutableAttributes
     *      The key attributes map.
     */
    @Override
    public final void setMutableAttributes(final KeyAttributesMap mutableAttributes) {
        this.mutableAttributes = mutableAttributes;
    }

    /**
     * Get the copy of the mutable attributes map.
     *
     * @return KeyAttributesMap attributes.
     */
    public final KeyAttributesMap getMutableAttributesFromServer() {
        return this.mutableAttributesFromServer;
    }

    /**
     * Set the copy of the mutable attributes map.
     *
     * @param mutableAttributesFromServer
     *      The key attributes map.
     */
    public final void setMutableAttributesFromServer(final KeyAttributesMap mutableAttributesFromServer) {
        this.mutableAttributesFromServer = mutableAttributesFromServer;
    }

    /**
     * Get the key obligations.
     *
     * @return KeyObligationsMap obligations.
     */
    @Override
    public final KeyObligationsMap getObligationsMap() {
        return this.keyObligations;
    }

    /**
     * Set the key obligations map.
     *
     * @param keyObligations
     *      The key obligations map.
     */
    @Override
    public final void setObligationsMap(final KeyObligationsMap keyObligations) {
        this.keyObligations = keyObligations;
    }

    /**
     * Get the (server-supplied) hash of the key attributes.
     *
     * @return the (server-supplied) hash of the key attributes
     */
    public final String getAttributesSigBase64FromServer() {
        return attributesSigBase64FromServer;
    }

    /**
     * Set the (server-supplied) hash of the key attributes.
     *
     * @param attributesSigBase64FromServer the (server-supplied) hash of the key attributes
     */
    public final void setAttributesSigBase64FromServer(final String attributesSigBase64FromServer) {
        this.attributesSigBase64FromServer = attributesSigBase64FromServer;
    }

    /**
     * Get the (server-supplied) hash of the mutable key attributes.
     *
     * @return the (server-supplied) hash of the mutable key attributes
     */
    public final String getMutableAttributesSigBase64FromServer() {
        return mutableAttributesSigBase64FromServer;
    }

    /**
     * Set the (server-supplied) hash of the mutable key attributes.
     *
     * @param mutableAttributesSigBase64FromServer the (server-supplied) hash of the mutable key attributes
     */
    public final void setMutableAttributesSigBase64FromServer(final String mutableAttributesSigBase64FromServer) {
        this.mutableAttributesSigBase64FromServer = mutableAttributesSigBase64FromServer;
    }
}
