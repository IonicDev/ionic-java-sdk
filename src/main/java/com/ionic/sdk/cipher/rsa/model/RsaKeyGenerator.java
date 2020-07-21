package com.ionic.sdk.cipher.rsa.model;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * Ionic Machina Tools key generator implementation wrapping JCE-provided RSA algorithm.  It encapsulates the
 * capability to generate asymmetric cryptography keypairs.
 * <p>
 * RSA is used internally by Machina in the context of the device enrollment operation.
 * <p>
 * On systems containing older CPUs, this operation can take a long time.  On these systems, the use of
 * pre-existing key pairs is preferred to generating new key pairs.
 */
public final class RsaKeyGenerator {

    /**
     * Constructor.
     *
     * @throws IonicException on failure of platform preconditions for use of Ionic APIs.
     */
    public RsaKeyGenerator() throws IonicException {
        AgentSdk.initialize();
    }

    /**
     * Creates a new cryptographic keypair.
     *
     * @param keySize length in bits of the RSA keys
     * @return a {@link RsaKeyHolder} object containing the newly created keypair
     * @throws IonicException on cryptography errors
     */
    public RsaKeyHolder generate(final int keySize) throws IonicException {
        final KeyPairGenerator keygen = AgentSdk.getCrypto().getKeyPairGeneratorRsa();
        keygen.initialize(keySize);
        return new RsaKeyHolder(keygen.generateKeyPair());
    }

    /**
     * Creates a new asymmetric cryptography private key.
     *
     * @param keySize length in bits of the RSA key
     * @return a {@link RsaPrivateKey} object wrapping the newly created private key
     * @throws IonicException on cryptography errors
     */
    public RsaPrivateKey generatePrivateKey(final long keySize) throws IonicException {
        final KeyPairGenerator keygen = AgentSdk.getCrypto().getKeyPairGeneratorRsa();
        keygen.initialize((int) keySize);
        return new RsaPrivateKey(keygen.generateKeyPair().getPrivate());
    }

    /**
     * Given an existing asymmetric private key, derive the associated public key.
     *
     * @param rsaPrivateKey the private key that is the source of the derivation
     * @return the corresponding public key
     * @throws IonicException on using an unsupported private key format
     */
    public RsaPublicKey generatePublicKey(final RsaPrivateKey rsaPrivateKey) throws IonicException {
        final PrivateKey privateKey = rsaPrivateKey.getPrivateKey();
        if (privateKey instanceof RSAPrivateCrtKey) {
            return generatePublicKeyInternal((RSAPrivateCrtKey) privateKey);
        } else {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, privateKey.getClass().getName());
        }
    }

    /**
     * Given an existing asymmetric private key, derive the associated public key.
     *
     * @param privkey the private key that is the source of the derivation
     * @return the corresponding public key
     * @throws IonicException on using an unsupported private key format
     */
    private RsaPublicKey generatePublicKeyInternal(final RSAPrivateCrtKey privkey) throws IonicException {
        try {
            final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                    privkey.getModulus(), privkey.getPublicExponent());
            final KeyFactory keyFactory = AgentSdk.getCrypto().getKeyFactoryRsa();
            return new RsaPublicKey(keyFactory.generatePublic(publicKeySpec));
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }
}
