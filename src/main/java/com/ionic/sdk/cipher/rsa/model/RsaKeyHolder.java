package com.ionic.sdk.cipher.rsa.model;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Ionic Machina Tools key implementation wrapping JCE-provided RSA primitives.  This object
 * contains an RSA asymmetric cryptography key pair.
 * <p>
 * RSA is used internally by Machina in the context of the device enrollment operation.
 */
public final class RsaKeyHolder implements java.io.Serializable {

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

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = 6154420231017369862L;
}
