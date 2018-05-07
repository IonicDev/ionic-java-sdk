package com.ionic.sdk.crypto.secretshare;

import java.util.Collection;

/**
 * Container for configuration instructing the generator how to fold together the environment information
 * to derive the protection JSON.
 */
public class SecretShareBucket {

    /**
     * The names of the property data elements to associate with this bucket.
     */
    private final Collection<String> keys;

    /**
     * The number of property data values which must match the original values to unlock the secret.
     */
    private final int threshold;

    /**
     * Constructor.
     *
     * @param keys      the names of the property data elements to associate with this bucket
     * @param threshold the number of property data values which must match the original values to unlock the secret
     */
    public SecretShareBucket(final Collection<String> keys, final int threshold) {
        this.keys = keys;
        this.threshold = threshold;
    }

    /**
     * @return the names of the property data elements to associate with this bucket
     */
    public final Collection<String> getKeys() {
        return keys;
    }

    /**
     * @return the number of property data values which must match the original values to unlock the secret
     */
    public final int getThreshold() {
        return threshold;
    }
}
