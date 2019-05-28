package com.ionic.sdk.agent;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.crypto.jce.CryptoAbstract;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Constructor;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point into the Ionic SDK.
 */
public final class AgentSdk {

    /**
     * Exception which occurred on initialize call.
     */
    private final IonicException exceptionInitialize;

    /**
     * The pass-through object that brokers access to JCE primitives.
     */
    private final CryptoAbstract cryptoAbstract;

    /**
     * Constructor.
     *
     * @param provider the provider implementation that should be used to satisfy requests for cryptography objects
     */
    private AgentSdk(final Provider provider) {
        final Logger logger = Logger.getLogger(AgentSdk.class.getName());
        CryptoAbstract cryptoAbstractCtor = null;
        IonicException exception = null;
        try {
            final Provider providerUse = (provider == null) ? createProviderBouncyCastle() : provider;
            cryptoAbstractCtor = new CryptoAbstract(providerUse);
            checkForUnlimitedStrength(cryptoAbstractCtor);
            logger.log(Level.FINE, "initialize() = OK");
        } catch (IonicException e) {
            exception = e;
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        this.exceptionInitialize = exception;
        this.cryptoAbstract = cryptoAbstractCtor;
    }

    /**
     * @return the pass-through object that brokers access to JCE primitives
     */
    private CryptoAbstract getCryptoAbstract() {
        return cryptoAbstract;
    }

    /**
     * @return the {@link CryptoAbstract} singleton for the process
     * @throws IonicException on cryptography initialization failures
     */
    public static CryptoAbstract getCrypto() throws IonicException {
        final AgentSdk agentSdk = SingletonHelper.getInstance();
        final IonicException exceptionInitialize = agentSdk.exceptionInitialize;
        if (exceptionInitialize == null) {
            return agentSdk.getCryptoAbstract();
        } else {
            throw exceptionInitialize;
        }
    }

    /**
     * Initialize the Ionic SDK for usage.  In Java, this implementation:
     * <ol>
     * <li>checks the active JRE for the policy jurisdiction files needed to work with AES 256 bit keys,</li>
     * <li>adds the Bouncycastle provider (in JRE 7) so that necessary cryptography primitives are available.</li>
     * </ol>
     * <p>
     * https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples
     *
     * @param applicationContext the platform-specific context needed by the underlying platform
     * @return the per-process singleton of this object
     * @throws IonicException on cryptography initialization failures
     */
    public static AgentSdk initialize(final Object applicationContext) throws IonicException {
        final AgentSdk agentSdk = SingletonHelper.getInstance();
        final IonicException exceptionInitialize = agentSdk.exceptionInitialize;
        if (exceptionInitialize == null) {
            return agentSdk;
        } else {
            throw exceptionInitialize;
        }
    }

    /**
     * Alternate API allowing for the specification of a particular {@link Provider}.  When the Ionic SDK is
     * initialized using a particular provider, subsequent requests for cryptography primitives will pass the
     * request through to this provider.  In versions of the SDK prior to 2.5, all registered providers are
     * used to satisfy the request.
     *
     * @param provider the provider implementation that should be used to satisfy requests for cryptography objects
     * @return the initialized {@link AgentSdk} object
     * @throws IonicException on cryptography initialization failures
     */
    public static AgentSdk initialize(final Provider provider) throws IonicException {
        final AgentSdk agentSdk = SingletonHelper.initialize(provider);
        final IonicException exceptionInitialize = agentSdk.exceptionInitialize;
        if (exceptionInitialize == null) {
            return agentSdk;
        } else {
            throw exceptionInitialize;
        }
    }

    /**
     * Reset the state of the {@link AgentSdk} singleton.
     */
    public static void deinitialize() {
        SingletonHelper.deinitialize();
    }

    /**
     * Initialize the Ionic SDK for usage.  In Java, this implementation:
     * <ol>
     * <li>checks the active JRE for the policy jurisdiction files needed to work with AES 256 bit keys,</li>
     * <li>adds the Bouncycastle provider (in JRE 7) so that necessary cryptography primitives are available.</li>
     * </ol>
     * <p>
     * https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples
     *
     * @throws IonicException on cryptography initialization failures
     */
    public static void initialize() throws IonicException {
        final AgentSdk agentSdk = SingletonHelper.getInstance();
        final IonicException exceptionInitialize = agentSdk.exceptionInitialize;
        if (exceptionInitialize != null) {
            throw exceptionInitialize;
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + DELIMITER_AT + Integer.toHexString(hashCode());
    }

    /**
     * Delimiter that can be used when joining strings together.
     */
    private static final String DELIMITER_AT = "@";

    /**
     * Helper to guard against double init.
     * <p>
     * http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
     */
    private static class SingletonHelper {

        /**
         * The per-process singleton of this object.  The first fetch of this object should trigger its implicit
         * initialization.  Subsequent fetches will return the cached value.
         */
        private static volatile AgentSdk instance;

        /**
         * @return the per-process singleton of this object
         */
        private static AgentSdk getInstance() {
            if (instance == null) {
                synchronized (SingletonHelper.class) {
                    if (instance == null) {
                        instance = new AgentSdk(null);
                    }
                }
            }
            return instance;
        }

        /**
         * Alternate API allowing for the specification of a particular {@link Provider}.  When the Ionic SDK is
         * initialized using a particular provider, subsequent requests for cryptography primitives will pass the
         * request through to this provider.  In versions of the SDK prior to 2.5, all registered providers are
         * used to satisfy the request.
         *
         * @param provider the provider implementation that should be used to satisfy requests for cryptography objects
         * @return the initialized {@link AgentSdk} object
         */
        private static AgentSdk initialize(final Provider provider) {
            if (instance == null) {
                synchronized (SingletonHelper.class) {
                    if (instance == null) {
                        instance = new AgentSdk(provider);
                    }
                }
            }
            return instance;
        }

        /**
         * Reset the state of the {@link AgentSdk} singleton.
         */
        private static void deinitialize() {
            instance = null;
        }
    }

    /**
     * Attempt to instantiate a 256-bit AES key, and a cipher initialized with that key.  If the "JCE Unlimited
     * Strength Jurisdiction Policy Files" are not installed in the active JRE, this operation will fail.
     * <p>
     * Instructions to upgrade a JRE: https://support.ca.com/us/knowledge-base-articles.tec1698523.html
     * <p>
     * Download binary from: http://www.oracle.com/technetwork/java/javase/downloads/index.html
     * <p>
     * Search page for: "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files"
     *
     * @param cryptoAbstract the pass-through object that brokers access to JCE primitives
     * @throws IonicException if the javax.crypto.Cipher object cannot be initialized
     */
    private static void checkForUnlimitedStrength(final CryptoAbstract cryptoAbstract) throws IonicException {
        try {
            final byte[] bytes = new byte[AesCipher.KEY_BITS / Byte.SIZE];
            Arrays.fill(bytes, (byte) 0);
            final SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, AesCipher.ALGORITHM);
            final Cipher cipher = cryptoAbstract.getCipherAesCtr();
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISAGENT_INIT_FAILED_KEY_SIZE, e);
        }
    }

    /**
     * Using reflection, add BouncyCastle provider to provider list.
     *
     * @return the provider implementation that should be used to satisfy requests for cryptography objects
     * @throws IonicException if BouncyCastle provider is not in classpath or available
     */
    private static Provider createProviderBouncyCastle() throws IonicException {
        final String className = CLASSNAME_BC_PROVIDER;
        try {
            final Class<?> c = Class.forName(className);
            final Constructor<?> ctor = c.getConstructor();
            final Object object = ctor.newInstance();
            if (object instanceof Provider) {
                Security.addProvider((Provider) object);
                return (Provider) object;
            } else {
                throw new IonicException(SdkError.ISCRYPTO_ERROR, new GeneralSecurityException(className));
            }
        } catch (ReflectiveOperationException e) {
            throw new IonicException(SdkError.ISAGENT_RESOURCE_NOT_FOUND, e);
        }
    }

    /**
     * Class name for BouncyCastle Security Provider.  When running in JRE less than or equal 7, BouncyCastle provides
     * implementation of AES/GCM transform (needed for communications with ionic.com).
     */
    private static final String CLASSNAME_BC_PROVIDER = "org.bouncycastle.jce.provider.BouncyCastleProvider";
}
