package com.ionic.sdk.keyvault;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.error.IonicException;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Key vault abstract base class that implements all interfaces of KeyVaultInterface.
 *
 * This class serves as a convenient base class for key vaults in a way that
 * only requires the key vault derived class to implement load and save functions
 * (loadAllKeyRecords(), saveAllKeyRecords(), and cleanVaultStore()).
 *
 * With rare exception, a key vault class should implement KeyVaultBase instead
 * of KeyVaultInterface in order to take advantage of a wealth of well-tested
 * boiler-plate code.
 */
public abstract class KeyVaultBase extends KeyVaultInterface {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Security level of key vault.
     */
    protected static final int VAULT_SECURITY_LEVEL = 100;

    /**
     * A map of keyIds to key records.
     */
    @SuppressWarnings({"checkstyle:visibilitymodifier"})
    protected Map<String, KeyVaultKeyRecord> mapKeyRecords;

    /**
     * Last time key expiration times were checked.
     */
    private long lastExpirationSweepServerTimeUtcSeconds;

    /**
     * this value controls how often key expiration is checked, in seconds.
     * for example, key retrieval methods (e.g. getKey(), getKeys())
     * may get called with very high frequency, but we do not want to perform
     * key expiration sweep on every call for performance reasons.  the
     * expiration sweep interval controls how often the sweep will be performed
     * in situations like this.
     */
    private static final int EXPIRATION_SWEEP_INTERVAL_SECS = 10;

    /**
     * Default constructor.
     */
    public KeyVaultBase() {
        this.lastExpirationSweepServerTimeUtcSeconds = 0;
        this.mapKeyRecords = new TreeMap<String, KeyVaultKeyRecord>();
    }

    /**
     * Internal - Checks the key has an ID and is of the valid length.
     * @param key the key to check
     * @throws IonicException if the key fails those tests.
     */
    private void validateKey(final KeyVaultKey key) throws IonicException {
        if (key.getKeyId().length() == 0) {
            throw new IonicException(SdkError.ISKEYVAULT_INVALID_KEY, "Invalid protection key. Key ID is empty.");
        } else if (key.getKeyBytes().length != AesCipher.KEY_BYTES) {
            throw new IonicException(SdkError.ISKEYVAULT_INVALID_KEY,
                        String.format("Invalid protection key. Invalid key data length (expected %d, got %d).",
                                        AesCipher.KEY_BYTES, key.getKeyBytes().length));
        }
    }

    /**
     * Internal - Merge the records from the disk with records held in RAM.
     * @param mapKeyRecordsFromDisk the key records from the disk
     * @param mapKeyRecordsFromMemoryInOut the key records held in RAM - these will be updated
     * @return True if the RAM records are more up to date than the disk records.
     */
    private boolean mergeKeyRecords(final Map<String, KeyVaultKeyRecord> mapKeyRecordsFromDisk,
                                    final Map<String, KeyVaultKeyRecord> mapKeyRecordsFromMemoryInOut) {

        boolean bNeedsWriteToDiskOut = false;

        // iterate disk records to collect new and udpated keys from disk
        final Iterator<KeyVaultKeyRecord> diskKeyIter = mapKeyRecordsFromDisk.values().iterator();
        while (diskKeyIter.hasNext()) {
            final KeyVaultKeyRecord diskRecord = diskKeyIter.next();
            final KeyVaultKeyRecord memRecord = mapKeyRecordsFromMemoryInOut.get(diskRecord.getKeyId());
            if (memRecord != null) {

                // only consider updating our memory key from disk key if it is not marked for removal
                if (memRecord.isAlive()) {
                    // record from disk exists in our memory record map, so update
                    // our memory record if needed
                    if (memRecord.getIssuedServerTimeUtcSeconds()
                         == diskRecord.getIssuedServerTimeUtcSeconds()) {
                        // memory record is issued at the same time as disk record. just update our
                        // memory record state to ensure it is reflected as such.
                        memRecord.setState(KeyVaultKeyRecord.State.KR_STORED);
                    } else if (memRecord.getIssuedServerTimeUtcSeconds()
                                < diskRecord.getIssuedServerTimeUtcSeconds()) {
                        // memory record is issued before disk record, so its stale and we want to
                        // take definition from disk
                        mapKeyRecordsFromMemoryInOut.put(diskRecord.getKeyId(), new KeyVaultKeyRecord(diskRecord));
                    }
                }
            } else {
                // record from disk was added since last sync, add to our
                // memory record map
                mapKeyRecordsFromMemoryInOut.put(diskRecord.getKeyId(), new KeyVaultKeyRecord(diskRecord));
            }
        }

        // iterate memory records to trim out any that do not exist on disk.  simultaneously, determine
        // if there are any records that are not in ISKR_STORED state, which means we need to perform
        // a write to disk to reflect our in-memory changes.
        final Iterator<KeyVaultKeyRecord> memKeyIter = mapKeyRecordsFromMemoryInOut.values().iterator();
        while (memKeyIter.hasNext()) {
            final KeyVaultKeyRecord memRecord = memKeyIter.next();

            // if our memory record was not specifically added as a new key, then check
            // if there is a disk record for it.  if no disk record, then remove this key.
            if (memRecord.getState() != KeyVaultKeyRecord.State.KR_ADDED
                 && mapKeyRecordsFromDisk.get(memRecord.getKeyId()) == null) {
                // note the post-increment on the iterator, this is idiomatic map deletion in a loop
                memKeyIter.remove();
            } else {
                // if this memory record is not the same as what is on disk, then we need
                // to perform a write to disk to save the change
                if (memRecord.getState() != KeyVaultKeyRecord.State.KR_STORED) {
                    bNeedsWriteToDiskOut = true;
                }
            }
        }

        return bNeedsWriteToDiskOut;
    }

    // key management functions (required by ISKeyVaultInterface)
    // Documented in the Interface file.

    @Override
    protected final int setKeyInternal(final KeyVaultKey key, final boolean bAddIfNotFound) throws IonicException {
        logger.finest(String.format("key.getKeyId() = %s", key.getKeyId()));

        // make this entire function thread-safe
        synchronized (this) {

            // perform basic validation on the input key
            validateKey(key);

            // find the key and update it.  if the key is not found, then add the key only
            // if bAddIfNotFound is true.
            final KeyVaultKeyRecord record = mapKeyRecords.get(key.getKeyId());
            if (record != null && record.isAlive()) {
                // only update the key if its issue time is newer than the key we already have
                if (record.getIssuedServerTimeUtcSeconds() < key.getIssuedServerTimeUtcSeconds()) {

                    mapKeyRecords.put(key.getKeyId(), new KeyVaultKeyRecord(key, KeyVaultKeyRecord.State.KR_UPDATED));
                    return SdkError.ISKEYVAULT_OK;
                } else {
                    // update not needed
                    return SdkError.ISKEYVAULT_KEY_UPDATE_IGNORED;
                }
            } else if (bAddIfNotFound) {
                mapKeyRecords.put(key.getKeyId(), new KeyVaultKeyRecord(key, KeyVaultKeyRecord.State.KR_ADDED));
                return SdkError.ISKEYVAULT_OK;
            } else {
                logger.severe("An attempt to update a key was ignored because the key does not exist "
                              + "in the key vault and bAddIfNotFound = false.");
                return SdkError.ISKEYVAULT_KEY_NOT_FOUND;
            }
        }
    }

    @Override
    public final KeyVaultKey getKey(final String keyId) {
        logger.finest(String.format("keyId = %s", keyId));

        // make this entire function thread-safe
        synchronized (this) {

            // remove expired keys before possibly returning one
            expireKeysInternal(false);

            // perform key lookup
            final KeyVaultKeyRecord record = mapKeyRecords.get(keyId);
            if (record == null || !record.isAlive()) {
                return null;
            }

            // copy our key to the output object
            return new KeyVaultKey(record);
        }
    }

    @Override
    public final Vector<KeyVaultKey> getKeys(final Set<String> setKeyIds) {
        logger.finest(String.format("setKeyIds.size() = %d", setKeyIds.size()));

        // make this entire function thread-safe
        synchronized (this) {

            // remove expired keys before possibly returning any
            expireKeysInternal(false);

            // build local vector of keys
            final Vector<KeyVaultKey> vecKeysLocal = new Vector<KeyVaultKey>();
            for (String keyId : setKeyIds) {
                final KeyVaultKeyRecord record = mapKeyRecords.get(keyId);
                if (record != null && record.isAlive()) {
                    vecKeysLocal.add(new KeyVaultKey(record));
                }
            }

            return vecKeysLocal;
        }
    }

    @Override
    public final Set<String> getAllKeyIds() {

        // make this entire function thread-safe
        synchronized (this) {

            // remove expired keys before possibly returning any
            expireKeysInternal(false);

            final TreeSet<String> setKeyIdsOut = new TreeSet<String>();

            // build local set of key ids
            setKeyIdsOut.clear();
            for (KeyVaultKeyRecord record : mapKeyRecords.values()) {
                if (record.isAlive()) {
                    setKeyIdsOut.add(record.getKeyId());
                }
            }

            return setKeyIdsOut;
        }
    }

    @Override
    public final Vector<KeyVaultKey> getAllKeys() {

        // make this entire function thread-safe
        synchronized (this) {

            // remove expired keys before possibly returning any
            expireKeysInternal(false);

            final Vector<KeyVaultKey> vecKeysLocal = new Vector<KeyVaultKey>();
            for (KeyVaultKeyRecord record : mapKeyRecords.values()) {

                if (record.isAlive()) {

                    vecKeysLocal.add(new KeyVaultKey(record));
                }
            }

            return vecKeysLocal;
        }
    }

    @Override
    public final int getKeyCount() {

        // make this entire function thread-safe
        synchronized (this) {

            // remove expired keys before possibly returning any
            expireKeysInternal(false);

            int nCount = 0;
            for (KeyVaultKeyRecord record : mapKeyRecords.values()) {

                if (record.isAlive()) {

                    ++nCount;
                }
            }

            return nCount;
        }
    }

    @Override
    public final boolean hasKey(final String keyId) {

        // make this entire function thread-safe
        synchronized (this) {

            // remove expired keys before possibly returning any
            expireKeysInternal(false);

            final KeyVaultKeyRecord record = mapKeyRecords.get(keyId);
            return (record != null && record.isAlive());
        }
    }

    @Override
    public final boolean removeKey(final KeyVaultKey key) {

        // make this entire function thread-safe
        synchronized (this) {
            return removeKeyImpl(key.getKeyId());
        }
    }

    @Override
    public final boolean removeKey(final String keyId) {

        // make this entire function thread-safe
        synchronized (this) {
            return removeKeyImpl(keyId);
        }
    }

    @Override
    public final Set<String> removeKeys(final Set<String> keyIds) {

        // make this entire function thread-safe
        synchronized (this) {

            // clear the output set, if applicable
            final TreeSet<String> keyIdsNotFoundOptOut = new TreeSet<String>();

            // iterate each key and remove it
            for (String keyId : keyIds) {

                if (!removeKeyImpl(keyId)) {
                    keyIdsNotFoundOptOut.add(keyId);
                }
            }

            return keyIdsNotFoundOptOut;
        }
    }

    @Override
    public final void clearAllKeys() {

        // make this entire function thread-safe
        synchronized (this) {

            for (KeyVaultKeyRecord record : mapKeyRecords.values()) {

                if (record.isAlive()) {
                    // mark the key as removed. note that by doing this the key record
                    // will also modify the key data (clear key bytes, key attributes, etc).
                    record.setState(KeyVaultKeyRecord.State.KR_REMOVED);
                }
            }

            return;
        }
    }

    // key expiration / purging functions (required by ISKeyVaultInterface)
    @Override
    protected final void expireKeysInternal(final Set<String> keyIdsExpiredOptOut) {

        // make this entire function thread-safe
        synchronized (this) {

            // pass true in first parameter to force the expiration sweep
            expireKeysInternal(true, keyIdsExpiredOptOut);
        }
    }

    // persistence functions (required by ISKeyVaultInterface)
    @Override
    public final void sync() throws IonicException {

        // make this entire function thread-safe
        synchronized (this) {

            // remove expired keys, if any, before any sync logic
            expireKeysInternal(false);

            // keep track of whether we need to write any data back to permanent storage
            boolean bNeedsWriteToDisk = false;

            // load from permanent storage
            Map<String, KeyVaultKeyRecord> mapKeyRecordsFromDisk = null;
            try {
                mapKeyRecordsFromDisk = loadAllKeyRecords();
            } catch (IonicException e) {

                // check for errors that we can handle before returning with failure
                if (e.getReturnCode() == SdkError.ISKEYVAULT_RESOURCE_NOT_FOUND) {

                    // there is no storage record found, so there is nothing to load
                    // from disk.  this is not an error, but we in this case we need
                    // to force a write to disk in order to create the key vault resource
                    bNeedsWriteToDisk = true;
                } else if (e.getReturnCode() == SdkError.ISKEYVAULT_LOAD_NOT_NEEDED) {
                    // we only need to write to disk in this situation if there have
                    // been changes to our key records in memory
                    bNeedsWriteToDisk  = hasChangesInternal();
                } else {
                    throw e;
                }
            }

            if (mapKeyRecordsFromDisk != null) {

                // records were loaded from disk, so we need to perform merge of
                // disk records with memory records
                bNeedsWriteToDisk = mergeKeyRecords(mapKeyRecordsFromDisk, mapKeyRecords);
            }

            // write the final merged memory records back to permanent storage if needed
            if (bNeedsWriteToDisk) {
                logger.finest("Sync operation will now perform save operation.");

                saveAllKeyRecords(mapKeyRecords);

                // mark all live records as stored because we know that they all exist in
                // permanent storage. trim out all dead records.
                final Iterator<KeyVaultKeyRecord> itr = mapKeyRecords.values().iterator();
                while (itr.hasNext()) {
                    final KeyVaultKeyRecord record = itr.next();
                    if (record.isAlive()) {
                        record.setState(KeyVaultKeyRecord.State.KR_STORED);
                    } else {
                        itr.remove();
                    }
                }
            } else {
                logger.finest("Sync operation skipped save operation since there are no changes.");
            }

            return;
        }
    }

    @Override
    public final boolean hasChanges() {

        // make this entire function thread-safe
        synchronized (this) {

            return hasChangesInternal();
        }
    }

    /**
     * Internal Removes key by marking it KR_REMOVED.
     * @param keyId key to remove
     * @return True if an unremoved record is found and marked, false otherwise.
     */
    private boolean removeKeyImpl(final String keyId) {

        // perform key lookup
        final KeyVaultKeyRecord record = mapKeyRecords.get(keyId);
        if (record == null || !record.isAlive()) {

            return false;
        }

        // mark the key as removed. note that by doing this the key record
        // will also modify the key data (clear key bytes, key attributes, etc).
        record.setState(KeyVaultKeyRecord.State.KR_REMOVED);

        return true;
    }

    // pure virtual serialization interfaces that must be implemented by sub-classes
    /**
     * Abstract interface function subclasses must override.
     *
     * Function should move a map of key records from some form of persistent storage
     * (usually this means an encrypted file) into the returned map.
     * @return Map of key id to key records where saved records should be stored
     * @throws IonicException If the load runs into IO errors.
     */
    protected abstract Map<String, KeyVaultKeyRecord> loadAllKeyRecords() throws IonicException;

    /**
     * Abstract interface function subclasses must override.
     *
     * Function takes a map of key records and should move them into some form of persistent storage
     * (usually this means an encrypted file)
     * @param  mapKeyRecords Map of key id to key records that should be saved
     * @return ISKEYVAULT_OK on success, or some other non-zero error code.
     * @throws IonicException If the load runs into IO errors.
     */
    protected abstract int saveAllKeyRecords(Map<String, KeyVaultKeyRecord> mapKeyRecords) throws IonicException;

    /**
     * Internal Checks for keys that have expired and marks them KR_REMOVED.
     * Does nothing if EXPIRATION_SWEEP_INTERVAL_SECS have not passed and bForceSweep is false.
     * @param bForceSweep Perform the expiration check regardless of how recently it was done.
     * @param keyIdsExpiredOptOut Optional, if not null, the function will fill this Set with
     * the keyID's of the keys that expired.
     */
    private void expireKeysInternal(final boolean bForceSweep, final Set<String> keyIdsExpiredOptOut) {
        // get current server time UTC seconds to compare all key expiration times against
        final long currentServerTimeUtcSeconds = KeyVaultTimeUtil.getCurrentServerTimeUtcSeconds();

        // if an expiration sweep has happened in the last EXPIRATION_SWEEP_INTERVAL_SECS
        // seconds, then ignore this request
        if (!bForceSweep && currentServerTimeUtcSeconds > lastExpirationSweepServerTimeUtcSeconds
                                                             + EXPIRATION_SWEEP_INTERVAL_SECS) {
            return;
        }

        // store current server time as our last expiration sweep time
        lastExpirationSweepServerTimeUtcSeconds = currentServerTimeUtcSeconds;

        for (KeyVaultKeyRecord record : mapKeyRecords.values()) {

            if (record.isAlive() && record.isExpired(currentServerTimeUtcSeconds)) {
                // if output key ID set is provided then add this key to it
                if (keyIdsExpiredOptOut != null) {
                    keyIdsExpiredOptOut.add(record.getKeyId());
                }

                // mark the key as removed. note that by doing this the key record
                // will also modify the key data (clear key bytes, key attributes, etc).
                record.setState(KeyVaultKeyRecord.State.KR_REMOVED);
            }
        }
    }

    /**
     * Remove all keys which have expired.
     * @param bForceSweep Run the check regardless of whether the EXPIRATION_SWEEP_INTERVAL_SECS has passed.
     */
    private void expireKeysInternal(final boolean bForceSweep) {
        expireKeysInternal(bForceSweep, null);
    }

    /**
     * Internal check to see if any records need serialization to disk.
     * @return true if any record is found not in the KR_STORED state, false otherwise.
     */
    protected final boolean hasChangesInternal() {

        // look for any key record which is not in STORED state
        for (KeyVaultKeyRecord record : mapKeyRecords.values()) {

            if (record.getState() != KeyVaultKeyRecord.State.KR_STORED) {
                return true;
            }
        }
        return false;
    }
}
