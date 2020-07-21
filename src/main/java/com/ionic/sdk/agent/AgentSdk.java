package com.ionic.sdk.agent;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.crypto.jce.CryptoAbstract;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point into the Ionic SDK.
 * <p>
 * The API {@link #initialize(Provider)} may be used to specify the {@link Provider} to use when cryptography
 * primitives are needed.  By default, SDK 2.6+ uses the <b>SunJCE</b> provider built into the JRE.  To use a different
 * cryptography provider, call the API {@link #initialize(Provider)} prior to any other usage of the Ionic SDK.
 * <p>
 * An implicit call to {@link #initialize(Provider)} is made upon first usage of Ionic cryptography APIs.
 * <p>
 * The cryptography {@link Provider} specified in the first call to {@link #initialize(Provider)} will remain in effect
 * for the lifetime of the hosting process.  In order to enable accountability of the cryptography provider, any
 * subsequent calls to {@link #initialize(Provider)} will be ignored.
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
            // ensure that specified provider is registered in JCE
            if ((provider != null)) {
                Security.addProvider(provider);
            }
            final Provider providerUse = (provider == null) ? Security.getProvider(PROVIDER_SUNJCE) : provider;
            cryptoAbstractCtor = new CryptoAbstract(providerUse);
            checkForUnlimitedStrength();
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
        final AgentSdk agentSdk = SingletonHelper.getInstance();  // JCE-ok; internals of AgentSdk
        final IonicException exceptionInitialize = agentSdk.exceptionInitialize;
        if (exceptionInitialize == null) {
            return agentSdk.getCryptoAbstract();
        } else {
            throw exceptionInitialize;
        }
    }

    /**
     * Initialize the Ionic SDK for usage.  In Java, this implementation checks the active JRE for the policy
     * jurisdiction files needed to work with AES 256 bit keys.
     * <p>
     * See this
     * <a href='https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples'
     * target='_blank'>link</a> for
     * some background on singleton initialization.
     *
     * @param applicationContext the platform-specific context needed by the underlying platform
     * @return the per-process singleton of this object
     * @throws IonicException on cryptography initialization failures
     */
    public static AgentSdk initialize(final Object applicationContext) throws IonicException {
        final AgentSdk agentSdk = SingletonHelper.getInstance();  // JCE-ok; internals of AgentSdk
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
     * Initialize the Ionic SDK for usage.  In Java, this implementation checks the active JRE for the policy
     * jurisdiction files needed to work with AES 256 bit keys.
     * <p>
     * See this
     * <a href='https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples'
     * target='_blank'>link</a> for
     * some background on singleton initialization.
     *
     * @throws IonicException on cryptography initialization failures
     */
    public static void initialize() throws IonicException {
        final AgentSdk agentSdk = SingletonHelper.getInstance();  // JCE-ok; internals of AgentSdk
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
        private static AgentSdk getInstance() {  // JCE-ok; internals of AgentSdk
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
     * @throws IonicException if the javax.crypto.Cipher object cannot be initialized
     */
    private static void checkForUnlimitedStrength() throws IonicException {
        try {
            final byte[] bytes = new byte[AesCipher.KEY_BITS / Byte.SIZE];
            Arrays.fill(bytes, (byte) 0);
            final SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, AesCipher.ALGORITHM);
            final Cipher cipher = Cipher.getInstance(AesCipher.TRANSFORM_CTR);  // JCE-ok (maven-shade workaround)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISAGENT_INIT_FAILED_KEY_SIZE, e);
        }
    }

    /**
     * Provider name for built-in JCE implementation.
     */
    private static final String PROVIDER_SUNJCE = "SunJCE";
}
