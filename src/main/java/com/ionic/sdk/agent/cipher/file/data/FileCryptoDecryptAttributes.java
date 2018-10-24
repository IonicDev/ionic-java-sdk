package com.ionic.sdk.agent.cipher.file.data;

import com.ionic.sdk.agent.data.MetadataHolder;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

/**
 * On an Ionic SDK file decrypt operation, this class provides the ability to receive parameters
 * relevant to the operation.
 * <p>
 * The usage pattern for this object is to supply a fresh instance to each API call.  The Ionic SDK will reject
 * any <code>decrypt()</code> operation that is attempted using a previously used {@link FileCryptoDecryptAttributes}
 * object.
 */
public final class FileCryptoDecryptAttributes extends MetadataHolder {

    /**
     * After decryption, parameters applied to operation.
     */
    private GetKeysResponse.Key key;

    /**
     * The identifier for the file family used in the decryption operation.
     */
    private CipherFamily cipherFamily;

    /**
     * The identifier for the file family version used in the decryption operation.
     */
    private String version;

    /**
     * Value indicating whether denied page object should be populated if an access denied error is encountered.
     */
    private boolean shouldProvideAccessDeniedPage;

    /**
     * Constructor.
     */
    public FileCryptoDecryptAttributes() {
        this.key = new GetKeysResponse.Key();
        this.cipherFamily = CipherFamily.FAMILY_UNKNOWN;
        this.version = "";
        this.shouldProvideAccessDeniedPage = false;
    }

    /**
     * @return the id of the key used in the decryption operation
     */
    public String getKeyId() {
        return key.getId();
    }

    /**
     * @return the origin of the key used in the decryption operation
     */
    public String getKeyOrigin() {
        return key.getOrigin();
    }

    /**
     * Setter.
     *
     * @param version the file family version to be used in the decryption operation
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * @return the file family version to be used in the decryption operation
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setter.
     *
     * @param cipherFamily the file family to be used in the decryption operation
     */
    public void setFamily(final CipherFamily cipherFamily) {
        this.cipherFamily = cipherFamily;
    }

    /**
     * @return the file family to be used in the decryption operation
     */
    public CipherFamily getFamily() {
        return cipherFamily;
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
     * Get the mutable attributes map.
     *
     * @return KeyAttributesMap attributes
     */
    public KeyAttributesMap getMutableKeyAttributes() {
        return this.key.getMutableAttributesMap();
    }

    /**
     * Get the value indicating whether denied page object should be populated if
     * an access denied error is encountered.
     *
     * @return access denied page enablement option
     */
    public boolean shouldProvideAccessDeniedPage() {
        return shouldProvideAccessDeniedPage;
    }

    /**
     * Indicate whether access denied page object should be populated in the event that
     * an access denied error is encountered.
     *
     * @param state access denied page enablement option
     */
    public void setShouldProvideAccessDeniedPage(final boolean state) {
        shouldProvideAccessDeniedPage = state;
    }

    /**
     * Get the byte vector containing the access denied page for the appropriately formatted document.
     *
     * @return access denied page bytes
     * @throws IonicException unconditionally, as this functionality is to be implemented
     */
    public byte[] getAccessDeniedPageOut() throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }

    /**
     * Set the cryptography key from the response.
     *
     * @param key the key used in the decryption operation
     */
    public void setKeyResponse(final GetKeysResponse.Key key) {
        this.key = key;
    }

    /**
     * @return the key used in the decryption operation
     */
    public GetKeysResponse.Key getKeyResponse() {
        return this.key;
    }
}
