package com.ionic.sdk.keyvault;

/**
 * An extension of the basic {@link KeyVaultKey} that includes update state.
 */
public class KeyVaultKeyRecord extends KeyVaultKey {

    /**
     * Serialized state, which lets the serializing agent know which records to update.
     */
    public enum State {

        /**
         * Represents an unknown / invalid state.
         */
        KR_INVALID,

        /**
         * Record has been added since the last serialization.
         */
        KR_ADDED,

        /**
         * Record has been removed since the last serialization.
         */
        KR_REMOVED,

        /**
         * Record has been updated since the last serialization.
         */
        KR_UPDATED,

        /**
         * Record has been serialized more recently than any other activity.
         */
        KR_STORED,
    }

    /**
     * Serialized state enum, which lets the serializing agent know which records to update.
     */
    private State eState;

    /**
     * Check whether a record has been removed and is hanging around waiting for that fact to be serialized.
     * @return True if the record has not been removed, false otherwise.
     */
    public boolean isAlive() {
        return (eState != State.KR_REMOVED);
    }

    /**
     * Initializes the key record empty and invalid.
     */
    public KeyVaultKeyRecord() {
        eState = State.KR_INVALID;
    }

    /**
     * Initializes the key record.
     * @param key KeyVaultKey record that needs serialization
     * @param eState current state of the record
     */
    public KeyVaultKeyRecord(final KeyVaultKey key, final State eState) {
        super(key);
        this.eState = eState;
    }

    /**
     * Copy constructor.
     * @param key KeyVaultKeyRecord to copy
     */
    public KeyVaultKeyRecord(final KeyVaultKeyRecord key) {
        super(key);
        this.eState = key.eState;
    }

    /**
     * Getter for the record state.
     * @return The record state
     */
    public State getState() {
        return eState;
    }

    /**
     * Setter for the record state.
     * @param eState new record state
     */
    public void setState(final State eState) {
        this.eState = eState;
        if (eState == State.KR_REMOVED) {
            // wipe all sensitive key data from memory.  leave the key ID, issued time,
            // and expiration time around.
            setKeyBytes(null);
            clearAllAttributesAndObligations();
        }
    }

    /**
     * Override for equals because code quality forces this to be here even though we want the super class version.
     * @param keyRecObj KeyVaultKey or KeyVaultKeyRecord to compare to
     * @return True if all the KeyVaultKey members equal the ones in the Object
     */
    @SuppressWarnings("PMD.UselessOverridingMethod")  // spotbugs versus pmd; document behavior
    @Override
    public boolean equals(final Object keyRecObj) {
        return super.equals(keyRecObj);
    }

    /**
     * Override for hashCode because code quality requires it.
     * @return hash of all the things in the super class.
     */
    @SuppressWarnings("PMD.UselessOverridingMethod")  // spotbugs versus pmd; document behavior
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
