package com.ionic.sdk.keyvault;

import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.key.KeyObligationsMap;

import java.util.Arrays;
import java.util.Objects;

/**
 * Key vault key data definition class.
 *
 * This class completely defines a key vault key and is an in-memory data
 * storage class only.
*/
public class KeyVaultKey {

    /**
     * The key tag.
     */
    private String keyId;

    /**
     * The key itself as raw bytes.
     */
    private byte[] keyBytes;

    /**
     * Immutabe attributes associated with this key.
     */
    private KeyAttributesMap mapKeyAttributes;

    /**
     * Mutabe attributes associated with this key.
     */
    private KeyAttributesMap mapMutableKeyAttributes;

    /**
     * Obligations associated with this key.
     */
    private KeyObligationsMap mapKeyObligations;

    /**
     * Time at which the key was issued from the server.
     */
    private long issuedServerTimeUtcSeconds;

    /**
     * Time at which the key will expire.
     */
    private long expirationServerTimeUtcSeconds;

    /**
     * Initializes the key object to be empty.
     */
    public KeyVaultKey() {
        this.issuedServerTimeUtcSeconds = 0;
        this.expirationServerTimeUtcSeconds = 0;
        this.mapKeyAttributes = new KeyAttributesMap();
        this.mapMutableKeyAttributes = new KeyAttributesMap();
        this.mapKeyObligations = new KeyObligationsMap();
    }

    /**
     * Initializes the key object with provided inputs.
     *
     * To know the current time on the server in UTC seconds, which is required to
     * populate the issuedServerTimeUtcSeconds and expirationServerTimeUtcSeconds fields,
     * the {@link KeyVaultTimeUtil#getCurrentServerTimeUtcSeconds()} should be used.
     * @param keyData The key data object (typically produced via {@link com.ionic.sdk.key.KeyServices#createKey()},
     * {@link com.ionic.sdk.key.KeyServices#getKey(String)}, and variants).
     * @param issuedServerTimeUtcSeconds The time at which this key was issued, measured in UTC seconds according
     * to server time.
     * @param expirationServerTimeUtcSeconds The time at which this key expires, measured in UTC seconds according
     * to server time.
     */
    public KeyVaultKey(final AgentKey keyData,
                       final long issuedServerTimeUtcSeconds,
                       final long expirationServerTimeUtcSeconds) {
        this.keyId = keyData.getId();
        this.keyBytes = keyData.getKey();
        this.mapKeyAttributes = keyData.getAttributesMap();
        this.mapMutableKeyAttributes = keyData.getMutableAttributesMap();
        this.mapKeyObligations = keyData.getObligationsMap();
        this.issuedServerTimeUtcSeconds = issuedServerTimeUtcSeconds;
        this.expirationServerTimeUtcSeconds = expirationServerTimeUtcSeconds;
    }

    /**
     * Initializes the key object with provided inputs.
     *
     * To know the current time on the server in UTC seconds, which is required to
     * populate the issuedServerTimeUtcSeconds and expirationServerTimeUtcSeconds fields,
     * the {@link KeyVaultTimeUtil#getCurrentServerTimeUtcSeconds()} should be used.
     * @param keyId The key ID (also known as the key tag).
     * @param keyBytes The raw key bytes. It must be exactly 32 bytes in length.
     * @param issuedServerTimeUtcSeconds The time at which this key was issued, measured in UTC seconds according
     * to server time.
     * @param expirationServerTimeUtcSeconds The time at which this key expires, measured in UTC seconds according
     * to server time.
     */
    public KeyVaultKey(final String keyId,
                       final byte[] keyBytes,
                       final long issuedServerTimeUtcSeconds,
                       final long expirationServerTimeUtcSeconds) {
        this.keyId = keyId;
        this.keyBytes = keyBytes.clone();
        this.issuedServerTimeUtcSeconds = issuedServerTimeUtcSeconds;
        this.expirationServerTimeUtcSeconds = expirationServerTimeUtcSeconds;
        this.mapKeyAttributes = new KeyAttributesMap();
        this.mapMutableKeyAttributes = new KeyAttributesMap();
        this.mapKeyObligations = new KeyObligationsMap();
    }

    /**
     * Initializes the key object with provided inputs.
     *
     * To know the current time on the server in UTC seconds, which is required to
     * populate the issuedServerTimeUtcSeconds and expirationServerTimeUtcSeconds fields,
     * the {@link KeyVaultTimeUtil#getCurrentServerTimeUtcSeconds()} should be used.
     * @param keyId The key ID (also known as the key tag).
     * @param keyBytes The raw key bytes. It must be exactly 32 bytes in length.
     * @param mapKeyAttributes The key attributes (see KeyAttributesMap).
     * @param mapMutableKeyAttributes The key attributes (see KeyAttributesMap).
     * @param mapKeyObligations The key obligations (see {@link KeyObligationsMap} and
     * {@link AgentKey#getObligationsMap()}).
     * @param issuedServerTimeUtcSeconds The time at which this key was issued, measured in UTC seconds according
     * to server time.
     * @param expirationServerTimeUtcSeconds The time at which this key expires, measured in UTC seconds according
     * to server time.
     */
    public KeyVaultKey(final String keyId,
                       final byte[] keyBytes,
                       final KeyAttributesMap mapKeyAttributes,
                       final KeyAttributesMap mapMutableKeyAttributes,
                       final KeyObligationsMap mapKeyObligations,
                       final long issuedServerTimeUtcSeconds,
                       final long expirationServerTimeUtcSeconds) {
        this.keyId = keyId;
        this.keyBytes = keyBytes.clone();
        this.mapKeyAttributes = new KeyAttributesMap(mapKeyAttributes);
        this.mapMutableKeyAttributes = new KeyAttributesMap(mapMutableKeyAttributes);
        this.mapKeyObligations = new KeyObligationsMap(mapKeyObligations);
        this.issuedServerTimeUtcSeconds = issuedServerTimeUtcSeconds;
        this.expirationServerTimeUtcSeconds = expirationServerTimeUtcSeconds;
    }

    /**
     * Copy Constructor.
     * @param key key to copy.
     */
    KeyVaultKey(final KeyVaultKey key) {
        this.keyId = key.keyId;
        this.keyBytes = key.getKeyBytes();
        this.mapKeyAttributes = new KeyAttributesMap(key.getKeyAttributes());
        this.mapMutableKeyAttributes = new KeyAttributesMap(key.getMutableKeyAttributes());
        this.mapKeyObligations = new KeyObligationsMap(key.getKeyObligations());
        this.issuedServerTimeUtcSeconds = key.issuedServerTimeUtcSeconds;
        this.expirationServerTimeUtcSeconds = key.expirationServerTimeUtcSeconds;
    }

    /**
     * Get the ID of the key, also known as the key tag (const version).
     * @return  The key ID string.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Set the ID of the key, also known as the key tag.
     * @param keyId The key ID.
     * @throws NullPointerException When keyBytes is null.
     */
    public void setKeyId(final String keyId) throws NullPointerException {
        this.keyId = Objects.requireNonNull(keyId, "keyId must not be null");
    }

    /**
     * Get the raw binary key bytes (non-const version).
     * @return The raw binary key bytes.
     */
    public byte[] getKeyBytes() {
        return keyBytes.clone();
    }

    /**
     * Set the raw binary key bytes.
     * @param keyBytes The raw binary key bytes (must be exactly 32 bytes in length).
     */
    public void setKeyBytes(final byte[] keyBytes) {
        this.keyBytes = ((keyBytes == null) ? new byte[0] : keyBytes.clone());
    }

    /**
     * Get the **immutable** key attributes (non-const version).
     *
     * See KeyAttributesMap for more information on what key attributes are.
     * @return  The **immutable** key attributes.
     */
    public KeyAttributesMap getKeyAttributes() {
        return mapKeyAttributes;
    }

    /**
     * Get the **immutable** key attributes.
     *
     * See KeyAttributesMap for more information on what key attributes are.
     * @param mapKeyAttributes the new immutable key attributes
     */
    public void setKeyAttributes(final KeyAttributesMap mapKeyAttributes) {
        this.mapKeyAttributes = ((mapKeyAttributes == null) ? new KeyAttributesMap() : mapKeyAttributes);
    }

    /**
     * Get the **mutable** key attributes (non-const version).
     *
     * See KeyAttributesMap for more information on what **mutable** key attributes are.
     * @return  The **mutable** key attributes.
     */
    public KeyAttributesMap getMutableKeyAttributes() {
        return mapMutableKeyAttributes;
    }

    /**
     * Set the **mutable** key attributes.
     *
     * See KeyAttributesMap for more information on what **mutable** key attributes are.
     * @param mapMutableKeyAttributes The **mutable** key attributes.
     */
    public void setMutableKeyAttributes(final KeyAttributesMap mapMutableKeyAttributes) {
        this.mapMutableKeyAttributes = ((mapMutableKeyAttributes == null)
                                         ? new KeyAttributesMap()
                                         : mapMutableKeyAttributes);
    }

    /**
     * Get the key obligations.
     *
     * See {@link KeyObligationsMap} and {@link AgentKey#getObligationsMap()} for more information on what key
     * obligations are.
     * @return The key obligations.
     */
    public KeyObligationsMap getKeyObligations() {
        return mapKeyObligations;
    }

    /**
     * Set the key obligations.
     *
     * See {@link KeyObligationsMap} and {@link AgentKey#getObligationsMap()} for more information on what key
     * obligations are.
     * @param mapKeyObligations the key obligations.
     */
    public void setKeyObligations(final KeyObligationsMap mapKeyObligations) {
        this.mapKeyObligations = ((mapKeyObligations == null) ? new KeyObligationsMap() : mapKeyObligations);
    }

    /**
     * Internal - when a record is lazy deleted, use this function to clear out RAM use.
     */
    protected void clearAllAttributesAndObligations() {
        mapKeyAttributes.clear();
        mapMutableKeyAttributes.clear();
        mapKeyObligations.clear();
    }

    /**
     * Get the issued time of the key measured in UTC seconds according to server time.
     * @return  The issued time of the key measured in UTC seconds according to server time.
     * @see com.ionic.sdk.core.date.DateTime
     */
    public long getIssuedServerTimeUtcSeconds() {
        return issuedServerTimeUtcSeconds;
    }

    /**
     * Set the issued time of the key measured in UTC seconds according to server time.
     * @param issuedServerTimeUtcSeconds The issued time of the key measured in UTC seconds according
     * to server time.
     * @see com.ionic.sdk.core.date.DateTime
     */
    public void setIssuedServerTimeUtcSeconds(final long issuedServerTimeUtcSeconds) {
        this.issuedServerTimeUtcSeconds = issuedServerTimeUtcSeconds;
    }

    /**
     * Get the expiration time of the key measured in UTC seconds according to server time.
     * @return The expiration time of the key measured in UTC seconds according to server time.
     * @see com.ionic.sdk.core.date.DateTime
     */
    public long getExpirationServerTimeUtcSeconds() {
        return expirationServerTimeUtcSeconds;
    }

    /**
     * Set the expiration time of the key measured in UTC seconds according to server time.
     * @param expirationServerTimeUtcSeconds The expiration time of the key measured in UTC seconds according
     * to server time.
     * @see com.ionic.sdk.core.date.DateTime
     */
    public void setExpirationServerTimeUtcSeconds(final long expirationServerTimeUtcSeconds) {
        this.expirationServerTimeUtcSeconds = expirationServerTimeUtcSeconds;
    }

    /**
     * Determine if the key is expired given an optional server time.
     * @param currentServerTimeUtcSeconds The current time measured in UTC seconds according to server time.
     *  Optionally, zero can be provided, in which case this function will calculate the current server time
     *  automatically via {@link KeyVaultTimeUtil#getCurrentServerTimeUtcSeconds()}.
     * @return True if the current time if greater than or equal to the expiration time, false otherwise
     * @see com.ionic.sdk.core.date.DateTime
     */
    public boolean isExpired(final long currentServerTimeUtcSeconds) {
        if (currentServerTimeUtcSeconds <= 0) {
            return (KeyVaultTimeUtil.getCurrentServerTimeUtcSeconds() >= expirationServerTimeUtcSeconds);
        } else {
            return (currentServerTimeUtcSeconds >= expirationServerTimeUtcSeconds);
        }
    }

    /**
     * Determine if the key is expired using the current server time
     * via {@link KeyVaultTimeUtil#getCurrentServerTimeUtcSeconds()}.
     * @return True if the current time if greater than or equal to the expiration time, false otherwise
     * @see com.ionic.sdk.core.date.DateTime
     */
    public boolean isExpired() {
        return (KeyVaultTimeUtil.getCurrentServerTimeUtcSeconds() >= expirationServerTimeUtcSeconds);
    }

    /**
     * Override for equals for KeyVaultKey's so it can be compared meaningfully to KeyVaultKeyRecord's.
     * @param keyRecObj KeyVaultKey or KeyVaultKeyRecord to compare to
     * @return True if all the KeyVaultKey members equal the ones in the KeyVaultKeyRecord
     */
    @Override
    public boolean equals(final Object keyRecObj) {

        // If the object is compared with itself then return true
        if (keyRecObj == this) {
            return true;
        }

        /* Check if keyRecObj is an instance of KeyVaultKey or not
          "null instanceof [type]" also returns false */
        if (!(keyRecObj instanceof KeyVaultKey)) {
            return false;
        }

        // typecast keyRecObj to KeyVaultKey so that we can compare data members
        final KeyVaultKey keyRec = (KeyVaultKey) keyRecObj;

        final KeyAttributesMap recAttrMap = keyRec.getKeyAttributes();
        if (mapKeyAttributes == null || mapKeyAttributes.size() == 0) {
            if (recAttrMap != null && recAttrMap.size() > 0) {
                return false;
            }
        } else {
            if (recAttrMap == null || !mapKeyAttributes.equals(recAttrMap)) {
                return false;
            }
        }

        final KeyAttributesMap recMutMap = keyRec.getMutableKeyAttributes();
        if (mapMutableKeyAttributes == null || mapMutableKeyAttributes.size() == 0) {
            if (recMutMap != null && recMutMap.size() > 0) {
                return false;
            }
        } else {
            if (recMutMap == null || !mapMutableKeyAttributes.equals(recMutMap)) {
                return false;
            }
        }

        final KeyObligationsMap recOblMap = keyRec.getKeyObligations();
        if (mapKeyObligations == null || mapKeyObligations.size() == 0) {
            if (recOblMap != null && recOblMap.size() > 0) {
                return false;
            }
        } else {
            if (recOblMap == null || !mapKeyObligations.equals(recOblMap)) {
                return false;
            }
        }

        return keyId.equals(keyRec.getKeyId())
               && Arrays.equals(keyBytes, keyRec.getKeyBytes())
               && issuedServerTimeUtcSeconds == keyRec.getIssuedServerTimeUtcSeconds()
               && expirationServerTimeUtcSeconds == keyRec.getExpirationServerTimeUtcSeconds();
    }

    /**
     * Override for hashCode because code quality requires it.
     * @return hash of all the things.
     */
    @Override
    public int hashCode() {
        return Objects.hash(keyId, Arrays.hashCode(keyBytes),
                            mapKeyAttributes,
                            mapMutableKeyAttributes,
                            mapKeyObligations,
                            issuedServerTimeUtcSeconds,
                            expirationServerTimeUtcSeconds);
    }
}
