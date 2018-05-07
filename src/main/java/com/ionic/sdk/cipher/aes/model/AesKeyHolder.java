package com.ionic.sdk.cipher.aes.model;

import java.security.Key;

/**
 * Container for an instance of a symmetric cryptography key.
 */
public final class AesKeyHolder {

    /**
     * The native Java keypair used to perform cryptographic operations.
     */
    private final Key key;

    /**
     * Constructor.
     *
     * @param key the native Java keypair used to perform cryptographic operations
     */
    public AesKeyHolder(final Key key) {
        this.key = key;
    }

    /**
     * @return the native Java public key used to perform cryptographic operations
     */
    public Key getKey() {
        return key;
    }
}
