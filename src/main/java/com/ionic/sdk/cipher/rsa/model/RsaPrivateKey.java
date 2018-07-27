package com.ionic.sdk.cipher.rsa.model;

import java.security.PrivateKey;

/**
 * The Ionic implementation of an RSA private key.  This class wraps the JRE private key implementation.
 */
public final class RsaPrivateKey {

    /**
     * The platform asymmetric private key implementation.
     */
    private PrivateKey privateKey;

    /**
     * Constructor.
     */
    public RsaPrivateKey() {
        this.privateKey = null;
    }

    /**
     * Constructor.
     *
     * @param privateKey the platform private key implementation being wrapped.
     */
    public RsaPrivateKey(final PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * @return the platform private key implementation
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Set the platform private key implementation.
     *
     * @param privateKey the platform private key implementation
     */
    public void setPrivateKey(final PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * @return true iff the wrapped object is present
     */
    public boolean isLoaded() {
        return (privateKey != null);
    }
}
