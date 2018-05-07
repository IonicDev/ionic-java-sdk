package com.ionic.sdk.cipher.rsa.model;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Container for an instance of an asymmetric cryptography key pair.
 */
public final class RsaKeyHolder {

    /**
     * The native Java keypair used to perform cryptographic operations.
     */
    private final KeyPair keypair;

    /**
     * Constructor.
     *
     * @param keypair the native Java keypair used to perform cryptographic operations
     */
    public RsaKeyHolder(final KeyPair keypair) {
        this.keypair = keypair;
    }

    /**
     * @return the native Java keypair used to perform cryptographic operations
     */
    public KeyPair getKeypair() {
        return keypair;
    }

    /**
     * @return the native Java public key used to perform cryptographic operations
     */
    public PublicKey getPublicKey() {
        return ((keypair == null) ? null : keypair.getPublic());
    }

    /**
     * @return the native Java private key used to perform cryptographic operations
     */
    public PrivateKey getPrivateKey() {
        return ((keypair == null) ? null : keypair.getPrivate());
    }
}
