package com.ionic.sdk.crypto.secretshare;

/**
 * Container for a cryptography secret, intended to be derived from (and protected by) the environment of
 * a JVM instance.
 */
public class SecretShareKey {

    /**
     * The hexadecimal representation of an AES 256 key.
     */
    private final String key;

    /**
     * The JSON representation of the aggregation of Shamir share data derived from the environment of a JVM instance.
     */
    private final String shares;

    /**
     * Constructor.
     *
     * @param key    an AES 256 key
     * @param shares the share data derived from the JVM environment
     */
    public SecretShareKey(final String key, final String shares) {
        this.key = key;
        this.shares = shares;
    }

    /**
     * @return the hexadecimal representation of an AES 256 key.
     */
    public final String getKey() {
        return key;
    }

    /**
     * @return the Shamir share data derived from the JVM environment
     */
    public final String getShares() {
        return shares;
    }
}
