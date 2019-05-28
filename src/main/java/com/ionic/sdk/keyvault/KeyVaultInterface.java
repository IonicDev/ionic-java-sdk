package com.ionic.sdk.keyvault;

import com.ionic.sdk.error.IonicException;

import java.util.Set;
import java.util.Vector;

/**
 * Pure interface class that defines every required interface of a key vault.
 * This interface defines every required method that a key vault must implement.
 */
public abstract class KeyVaultInterface {

    /**
     * Get the unique key vault type identifier.
     * @return Returns an identification string that is unique amongst all key vault implementations.
     */
    public abstract String getId();

    /**
     * Get the human readable version of the key vault identifier ({@link KeyVaultInterface#getId()}).
     * @return Returns an identification string that is unique amongst all key vault implementations.
     */
    public abstract String getLabel();

    //------------------------------------------------------
    // key management functions
    //------------------------------------------------------

    /**
     * Add or update a key into the key vault.  (See {@link #setKey(KeyVaultKey, boolean)} for details.)
     *
     * @param key The data protection key.
     * @param bAddIfNotFound Determines if the key should be added in the case that it is not found.
     * @return Returns a code from {@link com.ionic.sdk.error.KeyVaultErrorModuleConstants}.
     * See details section for all error code possibilities of this function.
     * @throws IonicException on errors listed in the description
     */
    protected abstract int setKeyInternal(KeyVaultKey key, boolean bAddIfNotFound) throws IonicException;

    /**
     * Add or update a key into the key vault.
     *
     * Attempts to update the provided key into the key vault and returns
     *   ISKEYVAULT_OK on success.
     *
     *   If the key does not exist then the key will be added to the vault.
     *
     *   If the key is found, but its 'issued' time ({@link KeyVaultKey#getIssuedServerTimeUtcSeconds()})
     *   is unchanged, then the key will NOT be updated and ISKEYVAULT_KEY_UPDATE_IGNORED will be returned.
     *
     *   If some aspect of the key itself is invalid, such as the key ID being empty,
     *   the key data not being 32 bytes in size, etc. then ISKEYVAULT_INVALID_KEY will be
     *   returned.
     *
     * @param key The data protection key.
     * @param bAddIfNotFound Determines if the key should be added in the case that it is not found.
     * @return Returns a code from {@link com.ionic.sdk.error.KeyVaultErrorModuleConstants}. See details section for
     * all error code possibilities of this function.
     * @throws IonicException on errors listed in the description
     */
    public int setKey(final KeyVaultKey key, final boolean bAddIfNotFound) throws IonicException {
        return setKeyInternal(key, bAddIfNotFound);
    }

    /**
     * Add or update a key into the key vault.
     *
     * Attempts to update the provided key into the key vault and returns
     *   ISKEYVAULT_OK on success.
     *
     *   If the key does not exist then the key will be added to the vault.
     *
     *   If the key is found, but its 'issued' time ({@link KeyVaultKey#getIssuedServerTimeUtcSeconds()})
     *   is unchanged, then the key will NOT be updated and ISKEYVAULT_KEY_UPDATE_IGNORED will be returned.
     *
     *   If some aspect of the key itself is invalid, such as the key ID being empty,
     *   the key data not being 32 bytes in size, etc. then ISKEYVAULT_INVALID_KEY will be
     *   returned.
     *
     * @param key The data protection key.
     * @return Returns a code from {@link com.ionic.sdk.error.KeyVaultErrorModuleConstants}. See details section for
     * all error code possibilities of this function.
     * @throws IonicException on errors listed in the description
     */
    public int setKey(final KeyVaultKey key) throws IonicException {
        return setKeyInternal(key, true);
    }

    /**
     * Get a single key from the key vault.
     *
     * Searches for a key identified by keyId.  On success, the found key will be
     * returned. If the key is not found, then null will be returned.
     * @param keyId The data protection key ID (also known as the key tag).
     * @return The retrieved key, if it exists, otherwise null.
     */
    public abstract KeyVaultKey getKey(String keyId);

    /**
     * Get one or more keys from the key vault.
     *
     * Searches for all the key IDs specified in setKeyIds. The return value vector will be
     * populated with all the keys which were found.
     * There is no failure condition for this function.
     * @param setKeyIds The set of data protection key IDs (also known as the key tag).
     * @return Vector of key objects that is populated with the retrieved keys.
     */
    public abstract Vector<KeyVaultKey> getKeys(Set<String> setKeyIds);

    /**
     * Get the set of all key IDs in the key vault.
     *
     * Returns set with all data protection key IDs that are contained in the key vault.
     * @return Output set of all the data protection key IDs in the key vault.
     */
    public abstract Set<String> getAllKeyIds();

    /**
     * Get the set of all key objects in the key vault.
     *
     * Returns all data protection keys that are contained in the key vault.
     * @return Output Vector of all the data protection keys in the key vault.
     */
    public abstract Vector<KeyVaultKey> getAllKeys();

    /**
     * Get the number of keys in the key vault.
     * @return  Returns the number of keys in the key vault.
     */
    public abstract int getKeyCount();

    /**
     * Determine if a key exists in the key vault.
     * @param keyId The data protection key ID to look for.
     * @return  Returns true if a key with the specified ID exists.  Otherwise, returns false.
     */
    public abstract boolean hasKey(String keyId);

    /**
     * Remove a single key from the key vault.
     *
     * The key ID of the provided key object is used in order to lookup the
     * key for removal. Calling this function is exactly equivalent to calling
     * {@link #removeKey(String)} with the key ID of the input
     * key object. This is a convenience function.
     * @param key The key object to remove.
     * @return  Returns true on success. Otherwise, returns false
     *          if the specified key was not found.
     * @throws IonicException if the key isn't found
     */
    public abstract boolean removeKey(KeyVaultKey key) throws IonicException;

    /**
     * Remove a single key from the key vault.
     * @param keyId The data protection key ID to remove.
     * @return  Returns true on success. Otherwise, returns false
     *          if the specified key was not found.
     */
    public abstract boolean removeKey(String keyId);

    /**
     * Remove one or more keys from the key vault.
     * @param setKeyIds The set of data protection key IDs to remove.
     * @return Optional output set of the key IDs which were not found.  Can be NULL.
     */
    public abstract Set<String> removeKeys(Set<String> setKeyIds);

    /**
     * Remove all keys from the key vault.
     */
    public abstract void clearAllKeys();

    //------------------------------------------------------
    // key expiration / purging functions
    //------------------------------------------------------

    /**
     * Remove all keys which have expired.
     * @param pKeyIdsExpiredOptOut Optional output set of the key IDs which were removed due to
     *        expiration. Can be NULL.
     */
    protected abstract void expireKeysInternal(Set<String> pKeyIdsExpiredOptOut);

    /**
     * Remove all keys which have expired.
     * @param pKeyIdsExpiredOptOut Optional output set of the key IDs which were removed due to
     *        expiration. Can be NULL.
     */
    public final void expireKeys(final Set<String> pKeyIdsExpiredOptOut) {
        expireKeysInternal(pKeyIdsExpiredOptOut);
    }

    /**
     * Remove all keys which have expired.
     */
    public final void expireKeys() {
        expireKeysInternal(null);
    }

    //------------------------------------------------------
    // persistence functions
    //------------------------------------------------------

    /**
     * Perform synchronization to permanent storage.
     *
     * This function first loads any detected changes to the key vault from
     * permanent storage, then merges those changes (if any) with the key vault
     * in memory, and finally saves the merged changes to permanent storage.
     *
     * This synchronization is both process-safe and thread-safe to ensure that
     * no changes are lost, and more importantly that the permanent storage is
     * never corrupted.
     * @throws IonicException on error
     */
    public abstract void sync() throws IonicException;

    /**
     * Determine if there are any changes to the key vault in memory that necessitate a {@link #sync()}.
     * @return  Returns true if changes have been made to the key vault in memory that have
     *          not yet been put into permanent storage via {@link #sync()}. For example, if
     *          a key is added, update, or removed, then a call to {@link #sync()} is needed
     *          in order to reflect the relevant change(s) to permanent storage.
     */
    public abstract boolean hasChanges();

    /**
     * Delete the current key vault storage (file, keychain, whatever).
     *
     * If the loadAllKeyRecords function fails, it may mean something is wrong or corrupt in the file
     * itself. When this happens, the sync function will early out and the key vault can never repair
     * itself. This function is designed to do that by simply deleting the corrupt storage.
     */
    public abstract void cleanVaultStore();

    //------------------------------------------------------
    // security level
    //------------------------------------------------------

    /**
     * Determine the relative security level of the key vault permanent storage model.
     * @return  Returns a positive integer between 0 and 10,000 to indicate the relative security
     *          level offered by the key vault's permanent storage model. The lower the number is, the
     *          lower the security level is.
     *
     *          The key vaults provided by Ionic all
     *          use security level near or at 100. Custom key vault implementations may use higher
     *          or lower security levels to indicate that they are trusted more or less than
     *          the Ionic-provided key vaults, for example.
     */
    public abstract int getSecurityLevel();
}
