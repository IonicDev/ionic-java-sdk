package com.ionic.sdk.cipher.rsa.model;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * A digital signature is a mathematical scheme for presenting the authenticity of digital messages or documents.  The
 * Ionic implementation makes use of an RSA asymmetric key pair to perform sign and verify operations.
 */
public final class RsaSignatureProcessor {

    /**
     * The asymmetric cryptography public key to use in sign and verify operations.
     */
    private RsaPublicKey rsaPublicKey;

    /**
     * The asymmetric cryptography private key to use in sign and verify operations.
     */
    private RsaPrivateKey rsaPrivateKey;

    /**
     * Construct an instance of an Ionic RSA cipher.
     *
     * @throws IonicException on failure of platform preconditions for use of Ionic APIs.
     */
    public RsaSignatureProcessor() throws IonicException {
        AgentSdk.initialize();
        rsaPublicKey = null;
        rsaPrivateKey = null;
    }

    /**
     * Set the asymmetric cryptography public key to use in cryptography operations.
     *
     * @param publicKey the public key
     */
    public void setPublicKey(final RsaPublicKey publicKey) {
        rsaPublicKey = publicKey;
    }

    /**
     * Set the asymmetric cryptography private key to use in cryptography operations.
     *
     * @param privateKey the private key
     */
    public void setPrivateKey(final RsaPrivateKey privateKey) {
        rsaPrivateKey = privateKey;
    }

    /**
     * Sign a byte array and return the resulting signature as a byte array.
     *
     * @param data array of bytes to sign
     * @return the signature byte array
     * @throws IonicException on cryptography errors
     */
    public byte[] sign(final byte[] data) throws IonicException {
        return signInternal(data);
    }

    /**
     * Sign a string and return the resulting signature as a byte array.
     *
     * @param data the string to sign
     * @return the signature byte array
     * @throws IonicException on cryptography errors
     */
    public byte[] sign(final String data) throws IonicException {
        return signInternal(Transcoder.utf8().decode(data));
    }

    /**
     * Verify the authenticity and integrity of the input byte array, using the supplied signature.
     *
     * @param data      array of data bytes
     * @param signature array of signature bytes
     * @throws IonicException on signature verify failure
     */
    public void verify(final byte[] data, final byte[] signature) throws IonicException {
        verifyInternal(data, signature);
    }

    /**
     * Verify the authenticity and integrity of the input string, using the supplied signature.
     *
     * @param data      the source data to verify
     * @param signature array of signature bytes
     * @throws IonicException on signature verify failure
     */
    public void verify(final String data, final byte[] signature) throws IonicException {
        verifyInternal(Transcoder.utf8().decode(data), signature);
    }

    /**
     * Sign a byte array and return the resulting signature as a base64 encoded byte array.
     *
     * @param data array of bytes to sign
     * @return base64 representation of the signature
     * @throws IonicException on cryptography errors
     */
    public String signToBase64(final String data) throws IonicException {
        return Transcoder.base64().encode(signInternal(Transcoder.utf8().decode(data)));
    }

    /**
     * Verify the authenticity and integrity of the input data, using the supplied base64 encoded signature.
     *
     * @param data            the source data to verify
     * @param signatureBase64 the base64 representation of the signature bytes
     * @throws IonicException on signature verify failure
     */
    public void verifyFromBase64(final byte[] data, final String signatureBase64) throws IonicException {
        verifyInternal(data, Transcoder.base64().decode(signatureBase64));
    }

    /**
     * Verify the authenticity and integrity of the input data, using the supplied base64 encoded signature.
     *
     * @param data            the source data to verify
     * @param signatureBase64 the base64 representation of the signature bytes
     * @throws IonicException on signature verify failure
     */
    public void verifyFromBase64(final String data, final String signatureBase64) throws IonicException {
        verifyInternal(Transcoder.utf8().decode(data), Transcoder.base64().decode(signatureBase64));
    }

    /**
     * Returns the cryptographic signature of the input data.
     *
     * @param data the input data to be signed
     * @return the signature of the input data
     * @throws IonicException on cryptography errors, or invalid (null) parameters (privkey)
     */
    private byte[] signInternal(final byte[] data) throws IonicException {
        try {
            final Signature spi = AgentSdk.getCrypto().getSignatureRsa();
            SdkData.checkNotNull(rsaPrivateKey, RsaPrivateKey.class.getName());
            SdkData.checkNotNull(rsaPrivateKey.getPrivateKey(), PrivateKey.class.getName());
            spi.initSign(rsaPrivateKey.getPrivateKey());
            spi.update(data);
            return spi.sign();
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Verifies the cryptographic signature of the input data.
     *
     * @param data      the input data to be signed
     * @param signature the input signature to verify
     * @throws IonicException on signature verify failure, or invalid (null) parameters (pubkey, data)
     */
    private void verifyInternal(final byte[] data, final byte[] signature) throws IonicException {
        try {
            final Signature spi = AgentSdk.getCrypto().getSignatureRsa();
            SdkData.checkNotNull(rsaPublicKey, RsaPublicKey.class.getName());
            SdkData.checkNotNull(rsaPublicKey.getPublicKey(), PublicKey.class.getName());
            spi.initVerify(rsaPublicKey.getPublicKey());
            spi.update(data);
            SdkData.checkTrue(spi.verify(signature), SdkError.ISCRYPTO_BAD_SIGNATURE, RsaPublicKey.class.getName());
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }
}
