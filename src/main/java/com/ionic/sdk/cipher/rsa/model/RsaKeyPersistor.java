package com.ionic.sdk.cipher.rsa.model;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Ionic Machina Tools serialization implementation wrapping JCE-provided RSA key pair.  This object
 * implements the ability to persist RSA asymmetric cryptography key pairs.
 * <p>
 * RSA is used internally by Machina in the context of the device enrollment operation.
 */
public final class RsaKeyPersistor {

    /**
     * Constructor.
     *
     * @throws IonicException on failure of platform preconditions for use of Ionic APIs.
     */
    public RsaKeyPersistor() throws IonicException {
        AgentSdk.initialize();
    }

    /**
     * Serialize the public key using X509EncodedKeySpec, and base64 encode the result.
     *
     * @param keyHolder the Ionic container for the asymmetric keypair
     * @return the base64 representation of the serialized public key
     */
    public String toBase64Public(final RsaKeyHolder keyHolder) {
        final PublicKey publicKey = keyHolder.getPublicKey();
        final X509EncodedKeySpec x509KeySpec = (publicKey == null)
                ? null : new X509EncodedKeySpec(publicKey.getEncoded());
        return (x509KeySpec == null) ? null : Transcoder.base64().encode(x509KeySpec.getEncoded());
    }

    /**
     * Serialize the private key using PKCS8EncodedKeySpec, and base64 encode the result.
     *
     * @param keyHolder the Ionic container for the asymmetric keypair
     * @return the base64 representation of the serialized private key
     */
    public String toBase64Private(final RsaKeyHolder keyHolder) {
        final PrivateKey privateKey = keyHolder.getPrivateKey();
        final PKCS8EncodedKeySpec pkcs8KeySpec = (privateKey == null)
                ? null : new PKCS8EncodedKeySpec(privateKey.getEncoded());
        return (pkcs8KeySpec == null) ? null : Transcoder.base64().encode(pkcs8KeySpec.getEncoded());
    }

    /**
     * Reconstitute the serialized keypair components into a keypair.
     *
     * @param base64Public  the base64 representation of the serialized public key
     * @param base64Private the base64 representation of the serialized private key
     * @return the keypair object for the input key data
     * @throws IonicException on cryptography errors
     */
    public RsaKeyHolder fromBase64(final String base64Public, final String base64Private) throws IonicException {
        try {
            final KeyFactory keyFactory = AgentSdk.getCrypto().getKeyFactoryRsa();
            final PublicKey publicKey = (base64Public == null) ? null : toPublicKey(keyFactory, base64Public);
            final PrivateKey privateKey = (base64Private == null) ? null : toPrivateKey(keyFactory, base64Private);
            return new RsaKeyHolder(new KeyPair(publicKey, privateKey));
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Convert the base64 string into a {@link PublicKey}.
     *
     * @param keyFactory   the key factory to use when creating the key
     * @param base64Public the base64 representation of the serialized public key
     * @return the deserialized {@link PublicKey}
     * @throws InvalidKeySpecException if the given key specification is inappropriate for this key factory
     */
    private static PublicKey toPublicKey(final KeyFactory keyFactory, final String base64Public)
            throws InvalidKeySpecException {
        final X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Transcoder.base64().decode(base64Public));
        return keyFactory.generatePublic(x509KeySpec);
    }

    /**
     * Convert the base64 string into a {@link PrivateKey}.
     *
     * @param keyFactory    the key factory to use when creating the key
     * @param base64Private the base64 representation of the serialized private key
     * @return the deserialized {@link PrivateKey}
     * @throws InvalidKeySpecException if the given key specification is inappropriate for this key factory
     */
    private static PrivateKey toPrivateKey(final KeyFactory keyFactory, final String base64Private)
            throws InvalidKeySpecException {
        final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Transcoder.base64().decode(base64Private));
        return keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * Serialize the private key using PKCS8EncodedKeySpec.
     *
     * @param rsaPrivateKey the Ionic wrapper for the asymmetric private key
     * @return the byte array representation of the serialized private key
     */
    public byte[] savePrivateKeyDer(final RsaPrivateKey rsaPrivateKey) {
        if (rsaPrivateKey == null) {
            return null;
        }
        final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(
                rsaPrivateKey.getPrivateKey().getEncoded());
        return pkcs8KeySpec.getEncoded();
    }

    /**
     * Reconstitute the serialized private key.
     *
     * @param privateKeyBytes the byte array representation of the serialized private key
     * @return an Ionic wrapper for the private key
     * @throws IonicException on cryptography errors
     */
    public RsaPrivateKey loadPrivateKeyBer(final byte[] privateKeyBytes) throws IonicException {
        if (privateKeyBytes == null) {
            return null;
        }
        try {
            final KeyFactory keyFactory = AgentSdk.getCrypto().getKeyFactoryRsa();
            final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            final PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            return new RsaPrivateKey(privateKey);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Serialize the public key using X509EncodedKeySpec.
     *
     * @param rsaPublicKey the Ionic wrapper for the asymmetric public key
     * @return the byte array representation of the serialized public key
     */
    public byte[] savePublicKeyDer(final RsaPublicKey rsaPublicKey) {
        if (rsaPublicKey == null) {
            return null;
        }
        final X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(rsaPublicKey.getPublicKey().getEncoded());
        return x509KeySpec.getEncoded();
    }

    /**
     * Reconstitute the serialized public key.
     *
     * @param publicKeyBytes the byte array representation of the serialized public key
     * @return an Ionic wrapper for the public key
     * @throws IonicException on cryptography errors
     */
    public RsaPublicKey loadPublicKeyBer(final byte[] publicKeyBytes) throws IonicException {
        if (publicKeyBytes == null) {
            return null;
        }
        try {
            final KeyFactory keyFactory = AgentSdk.getCrypto().getKeyFactoryRsa();
            final X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
            final PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
            return new RsaPublicKey(publicKey);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }
}
