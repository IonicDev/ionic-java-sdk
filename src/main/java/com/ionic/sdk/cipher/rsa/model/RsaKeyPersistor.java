package com.ionic.sdk.cipher.rsa.model;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.cipher.rsa.RsaCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.CryptoErrorModuleConstants;
import com.ionic.sdk.error.IonicException;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Class encapsulating capability to serialize asymmetric cryptography keypairs.
 */
public final class RsaKeyPersistor {

    /**
     * Serialize the public key using X509EncodedKeySpec, and base 64 encode the result.
     *
     * @param keyHolder the Ionic container for the asymmetric keypair
     * @return the base64 representation of the serialized public key
     */
    public String toBase64Public(final RsaKeyHolder keyHolder) {
        final X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyHolder.getPublicKey().getEncoded());
        return Transcoder.base64().encode(x509KeySpec.getEncoded());
    }

    /**
     * Serialize the private key using PKCS8EncodedKeySpec, and base 64 encode the result.
     *
     * @param keyHolder the Ionic container for the asymmetric keypair
     * @return the base64 representation of the serialized private key
     */
    public String toBase64Private(final RsaKeyHolder keyHolder) {
        final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyHolder.getPrivateKey().getEncoded());
        return Transcoder.base64().encode(pkcs8KeySpec.getEncoded());
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
            AgentSdk.initialize(null);
            final KeyFactory keyFactory = KeyFactory.getInstance(RsaCipher.ALGORITHM);
            final PublicKey publicKey = (base64Public == null) ? null : toPublicKey(keyFactory, base64Public);
            final PrivateKey privateKey = (base64Private == null) ? null : toPrivateKey(keyFactory, base64Private);
            return new RsaKeyHolder(new KeyPair(publicKey, privateKey));
        } catch (GeneralSecurityException e) {
            throw new IonicException(CryptoErrorModuleConstants.ISCRYPTO_ERROR.value(), e);
        }
    }

    /**
     * Convert the base 64 string into a {@link PublicKey}.
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
     * Convert the base 64 string into a {@link PrivateKey}.
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
}
