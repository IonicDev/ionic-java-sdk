package com.ionic.sdk.agent.cipher.file.data;

import com.ionic.sdk.agent.data.MetadataHolder;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.error.SdkData;

/**
 * On an Ionic SDK file encrypt operation, this class provides the ability to specify parameters
 * to be used by the operation.
 */
public final class FileCryptoEncryptAttributes extends MetadataHolder {

    /**
     * Before encryption, container for attributes to be applied to operation.  After encryption, parameters applied
     * to operation.
     */
    private CreateKeysResponse.Key key;

    /**
     * The identifier for the file family to be used in the encryption operation.
     */
    private CipherFamily cipherFamily;

    /**
     * The identifier for the file family version to be used in the encryption operation.
     */
    private String version;

    /**
     * Constructor.
     */
    public FileCryptoEncryptAttributes() {
        this.key = new CreateKeysResponse.Key();
        this.cipherFamily = CipherFamily.FAMILY_UNKNOWN;
        this.version = "";
    }

    /**
     * Constructor.
     *
     * @param version the file family version to be used in the encryption operation
     */
    public FileCryptoEncryptAttributes(final String version) {
        this();
        this.version = version;
    }

    /**
     * Constructor.
     *
     * @param attributes the immutable attributes to be applied to the key created for the encryption operation
     */
    public FileCryptoEncryptAttributes(final KeyAttributesMap attributes) {
        this();
        this.key.setAttributesMap(attributes);
    }

    /**
     * Constructor.
     *
     * @param attributes        the immutable attributes to be applied to the key created for the encryption operation
     * @param mutableAttributes the mutable attributes to be applied to the key created for the encryption operation
     */
    public FileCryptoEncryptAttributes(final KeyAttributesMap attributes, final KeyAttributesMap mutableAttributes) {
        this();
        this.key.setAttributesMap(attributes);
        this.key.setMutableAttributesMap(mutableAttributes);
    }

    /**
     * Constructor.
     *
     * @param version    the file family version to be used in the encryption operation
     * @param attributes the immutable attributes to be applied to the key created for the encryption operation
     */
    public FileCryptoEncryptAttributes(final String version, final KeyAttributesMap attributes) {
        this();
        this.version = version;
        this.key.setAttributesMap(attributes);
    }

    /**
     * Constructor.
     *
     * @param version           the file family version to be used in the encryption operation
     * @param attributes        the immutable attributes to be applied to the key created for the encryption operation
     * @param mutableAttributes the mutable attributes to be applied to the key created for the encryption operation
     */
    public FileCryptoEncryptAttributes(final String version, final KeyAttributesMap attributes,
                                       final KeyAttributesMap mutableAttributes) {
        this();
        this.version = version;
        this.key.setAttributesMap(attributes);
        this.key.setMutableAttributesMap(mutableAttributes);
    }

    /**
     * @return the id of the key used in the encryption operation
     */
    public String getKeyId() {
        return key.getId();
    }

    /**
     * @return the origin of the key used in the encryption operation
     */
    public String getKeyOrigin() {
        return key.getOrigin();
    }

    /**
     * Setter.
     *
     * @param cipherFamily the file family to be used in the encryption operation
     */
    public void setFamily(final CipherFamily cipherFamily) {
        this.cipherFamily = cipherFamily;
    }

    /**
     * @return the file family to be used in the encryption operation
     */
    public CipherFamily getFamily() {
        return cipherFamily;
    }

    /**
     * Setter.
     *
     * @param version the file family version to be used in the encryption operation
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * @return the file family version to be used in the encryption operation
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the key attributes map for the encryption operation.
     *
     * @param keyAttributes the key attributes map
     */
    public void setKeyAttributes(final KeyAttributesMap keyAttributes) {
        this.key.setAttributesMap(keyAttributes);
    }

    /**
     * Get the key attributes.
     *
     * @return KeyAttributesMap attributes
     */
    public KeyAttributesMap getKeyAttributes() {
        return this.key.getAttributesMap();
    }

    /**
     * Set the mutable attributes map.
     *
     * @param keyAttributes the mutable attributes map
     */
    public void setMutableKeyAttributes(final KeyAttributesMap keyAttributes) {
        this.key.setMutableAttributesMap(keyAttributes);
    }

    /**
     * Get the mutable attributes map.
     *
     * @return KeyAttributesMap attributes
     */
    public KeyAttributesMap getMutableKeyAttributes() {
        return this.key.getMutableAttributesMap();
    }

    /**
     * Set the cryptography key from the response.
     *
     * @param key the key used in the encryption operation
     */
    public void setKeyResponse(final CreateKeysResponse.Key key) {
        SdkData.checkNotNullNPE(key, CreateKeysResponse.Key.class.getName());
        this.key = key;
    }

    /**
     * @return the key used in the encryption operation
     */
    public CreateKeysResponse.Key getKeyResponse() {
        return this.key;
    }
}
