package com.ionic.sdk.crypto.jce;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.rsa.RsaCipher;
import com.ionic.sdk.core.hash.Hash;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Ionic Java SDK provides pass-through access to various JCE facilities implemented in either the base Java
 * Runtime Environment, or in third-party libraries (for example, <a href='http://bouncycastle.org/'
 * target='_blank'>Bouncy Castle</a>).
 * <p>
 * This interface is intended to enable Ionic SDK users to substitute their own cryptography
 * library into an Ionic SDK-enabled process.  If so configured, the SDK will use only the facilities provided by that
 * library.  If a facility is not provided, then it is expected that the {@link CryptoAbstract} implementation will
 * throw an IonicException noting this failure.
 * <p>
 * If an alternate cryptography provider is used, it must:
 * <ul>
 * <li>expose itself as a {@link java.security.Provider} via the API
 * {@link java.security.Security#getProvider(String)}</li>
 * <li>expose the cipher algorithm "AES/CTR/NoPadding" via the API {@link Cipher#getInstance(String)}</li>
 * <li>expose the cipher algorithm "AES/GCM/NoPadding" via the API {@link Cipher#getInstance(String)}</li>
 * <li>expose the key generator algorithm "AES" via the API {@link KeyGenerator#getInstance(String)}</li>
 * <li>expose the cipher algorithm "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING" via the API
 * {@link Cipher#getInstance(String)}</li>
 * <li>expose the signature algorithm "SHA256withRSA/PSS" via the API {@link Signature#getInstance(String)}</li>
 * <li>expose the key pair generator "RSA" via the API {@link KeyPairGenerator#getInstance(String)}</li>
 * <li>expose the key factory algorithm "RSA" via the API {@link KeyFactory#getInstance(String)}</li>
 * <li>expose the message digest algorithm "SHA-256" via the API {@link MessageDigest#getInstance(String)}</li>
 * <li>expose the message digest algorithm "SHA-512" via the API {@link MessageDigest#getInstance(String)}</li>
 * <li>expose the message authentication code algorithm "HmacSHA256" via the API {@link Mac#getInstance(String)}</li>
 * <li>expose the secret key factory algorithm "PBKDF2WithHmacSHA256" via the API
 * {@link SecretKeyFactory#getInstance(String)}</li>
 * </ul>
 * <p>
 * If any algorithm listed above is not implemented and available, use of Ionic APIs depending on the algorithm will
 * fail, throwing an {@link IonicException} noting the failure.
 */
public final class CryptoAbstract {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The pass-through object that brokers access to JCE primitives.
     */
    private final Provider provider;

    /**
     * Constructor.
     *
     * @param provider the provider implementation that should be used to satisfy requests for cryptography objects
     */
    public CryptoAbstract(final Provider provider) {
        this.provider = provider;
    }

    /**
     * Return an instance of an AES/CTR cipher.
     *
     * @return an instance of {@link Cipher}, using AES 256-bit keys and the CTR cipher
     * @throws IonicException if the AES/CTR facility is not provided by the configured CryptoAbstract implementation
     */
    public Cipher getCipherAesCtr() throws IonicException {
        try {
            return (provider == null)
                    ? Cipher.getInstance(AesCipher.TRANSFORM_CTR)
                    : Cipher.getInstance(AesCipher.TRANSFORM_CTR, provider);
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Return an instance of an AES/GCM cipher.
     *
     * @return an instance of {@link Cipher}, using AES 256-bit keys and the GCM cipher
     * @throws IonicException if the AES/GCM facility is not provided by the configured CryptoAbstract implementation
     */
    public Cipher getCipherAesGcm() throws IonicException {
        try {
            return (provider == null)
                    ? Cipher.getInstance(AesCipher.TRANSFORM_GCM)
                    : Cipher.getInstance(AesCipher.TRANSFORM_GCM, provider);
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Return an instance of an AES key generator.
     *
     * @return an instance of an AES key generator
     * @throws IonicException if the AES facility is not provided by the configured CryptoAbstract implementation
     */
    public KeyGenerator getKeyGeneratorAes() throws IonicException {
        try {
            return (provider == null)
                    ? KeyGenerator.getInstance(AesCipher.ALGORITHM)
                    : KeyGenerator.getInstance(AesCipher.ALGORITHM, provider);
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Return an instance of an RSA cipher.
     *
     * @return an instance of {@link Cipher}, using an RSA 3072-bit keypair and the ECB cipher
     * @throws IonicException if the RSA facility is not provided by the configured CryptoAbstract implementation
     */
    public Cipher getCipherRsa() throws IonicException {
        try {
            return (provider == null)
                    ? Cipher.getInstance(RsaCipher.TRANSFORM_ECB)
                    : Cipher.getInstance(RsaCipher.TRANSFORM_ECB, provider);
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Return an instance of an RSA signature facility.
     * <p>
     * Reference <a href="https://bugs.openjdk.java.net/browse/JDK-8190180" target="_blank">JDK Bug System</a>
     * Reference <a href="https://stackoverflow.com/questions/48803100/" target="_blank">RSA-PSS</a>
     *
     * @return an instance of {@link Signature}, suitable for signing / verifying a byte stream
     * @throws IonicException if the signature facility is not provided by the configured CryptoAbstract implementation
     */
    public Signature getSignatureRsa() throws IonicException {
        try {
            if (provider == null) {
                return Signature.getInstance(RsaCipher.SIGNATURE_ALGORITHM);
            } else if (PROVIDER_SUNJCE.equals(provider.getName())) {
                // works on JRE >= 11, NoSuchAlgorithmException on previous versions
                final Signature signature = Signature.getInstance(ALGORITHM_RSASIGN, PROVIDER_RSASIGN);
                signature.setParameter(new PSSParameterSpec(
                        Hash.ALGORITHM, MASK_GENERATION_RSASIGN, MGF1ParameterSpec.SHA256, Hash.HASH_BYTES, 1));
                return signature;
            } else {
                return Signature.getInstance(RsaCipher.SIGNATURE_ALGORITHM, provider);
            }
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Return an instance of an RSA keypair generator.
     *
     * @return an instance of an RSA keypair generator
     * @throws IonicException if the RSA facility is not provided by the configured CryptoAbstract implementation
     */
    public KeyPairGenerator getKeyPairGeneratorRsa() throws IonicException {
        try {
            if (provider == null) {
                return KeyPairGenerator.getInstance(RsaCipher.ALGORITHM);
            } else if (PROVIDER_SUNJCE.equals(provider.getName())) {
                return KeyPairGenerator.getInstance(RsaCipher.ALGORITHM, PROVIDER_SUNJSSE);
            } else {
                return KeyPairGenerator.getInstance(RsaCipher.ALGORITHM, provider);
            }
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Return an instance of an RSA key factory.
     *
     * @return an instance of an RSA key factory
     * @throws IonicException if the RSA facility is not provided by the configured CryptoAbstract implementation
     */
    public KeyFactory getKeyFactoryRsa() throws IonicException {
        try {
            if (provider == null) {
                return KeyFactory.getInstance(RsaCipher.ALGORITHM);
            } else if (PROVIDER_SUNJCE.equals(provider.getName())) {
                return KeyFactory.getInstance(RsaCipher.ALGORITHM, PROVIDER_SUNJSSE);
            } else {
                return KeyFactory.getInstance(RsaCipher.ALGORITHM, provider);
            }
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Return a MessageDigest object instance using the SHA-256 hash algorithm.
     *
     * @return a MessageDigest object instance using the SHA-256 hash algorithm
     * @throws IonicException if the facility is not provided by the configured CryptoAbstract implementation
     */
    public MessageDigest getMessageDigestSha256() throws IonicException {
        try {
            if (provider == null) {
                return MessageDigest.getInstance(Hash.ALGORITHM);
            } else if (PROVIDER_SUNJCE.equals(provider.getName())) {
                return MessageDigest.getInstance(Hash.ALGORITHM, PROVIDER_SUN);
            } else {
                return MessageDigest.getInstance(Hash.ALGORITHM, provider);
            }
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Return a MessageDigest object instance using the SHA-512 hash algorithm.
     *
     * @return a MessageDigest object instance using the SHA-512 hash algorithm
     * @throws IonicException if the facility is not provided by the configured CryptoAbstract implementation
     */
    public MessageDigest getMessageDigestSha512() throws IonicException {
        try {
            if (provider == null) {
                return MessageDigest.getInstance(Hash.ALGORITHM_512);
            } else if (PROVIDER_SUNJCE.equals(provider.getName())) {
                return MessageDigest.getInstance(Hash.ALGORITHM_512, PROVIDER_SUN);
            } else {
                return MessageDigest.getInstance(Hash.ALGORITHM_512, provider);
            }
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Return an SSLContext object instance using the specified protocol.
     *
     * @param protocol the {@link SSLContext} protocol to use
     * @return an SSLContext object instance using the specified protocol
     * @throws IonicException if the facility is not provided by the configured CryptoAbstract implementation
     */
    public SSLContext getSSLContext(final String protocol) throws IonicException {
        try {
            if (provider == null) {
                return SSLContext.getInstance(protocol);
            } else if (PROVIDER_SUNJCE.equals(provider.getName())) {
                return SSLContext.getInstance(protocol, PROVIDER_SUNJSSE);
            } else {
                return SSLContext.getInstance(protocol, provider);
            }
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Return a Message Authentication Code (MAC) object instance using the HMAC algorithm.
     *
     * @return a Message Authentication Code (MAC) object instance using the HMAC algorithm
     * @throws IonicException if the facility is not provided by the configured CryptoAbstract implementation
     */
    public Mac getHmacSha256() throws IonicException {
        try {
            return (provider == null)
                    ? Mac.getInstance(HMAC_ALGORITHM)
                    : Mac.getInstance(HMAC_ALGORITHM, provider);
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Algorithm name for message authentication code.
     */
    public static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Return a SecretKeyFactory object instance using the Password-Based Key Derivation Function (PBKDF2) algorithm.
     *
     * @return a SecretKeyFactory object instance using the Password-Based Key Derivation Function (PBKDF2) algorithm
     * @throws IonicException if the facility is not provided by the configured CryptoAbstract implementation
     */
    public SecretKeyFactory getSecretKeyFactoryPBKDF2() throws IonicException {
        try {
            return (provider == null)
                    ? SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
                    : SecretKeyFactory.getInstance(PBKDF2_ALGORITHM, provider);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * SecretKeyFactory Algorithm name for PBKDF2.
     */
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Provider name for built-in JCE implementation.
     */
    private static final String PROVIDER_SUNJCE = "SunJCE";

    /**
     * Provider name for built-in JCE implementation.
     */
    private static final String PROVIDER_SUN = "SUN";

    /**
     * Provider name for built-in JCE implementation.
     */
    private static final String PROVIDER_SUNJSSE = "SunJSSE";

    /**
     * Algorithm name for built-in JCE implementation.
     */
    private static final String ALGORITHM_RSASIGN = "RSASSA-PSS";

    /**
     * Provider name for built-in JCE implementation.
     */
    private static final String PROVIDER_RSASIGN = "SunRsaSign";

    /**
     * The algorithm name of the mask generation function.
     */
    private static final String MASK_GENERATION_RSASIGN = "MGF1";
}
