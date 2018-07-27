package com.ionic.sdk.cipher.rsa.model;

import java.security.PublicKey;

/**
 * The Ionic implementation of an RSA public key.  This class wraps the JRE public key implementation.
 */
public final class RsaPublicKey {

    /**
     * The platform asymmetric public key implementation.
     */
    private PublicKey publicKey;

    /**
     * Constructor.
     */
    public RsaPublicKey() {
        this.publicKey = null;
    }

    /**
     * Constructor.
     *
     * @param publicKey the platform public key implementation being wrapped.
     */
    public RsaPublicKey(final PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @return the platform public key implementation
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Set the platform public key implementation.
     *
     * @param publicKey the platform public key implementation
     */
    public void setPublicKey(final PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @return true iff the wrapped object is present
     */
    public boolean isLoaded() {
        return (publicKey != null);
    }
}
