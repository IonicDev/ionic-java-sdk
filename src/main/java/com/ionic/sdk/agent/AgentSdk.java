package com.ionic.sdk.agent;

import com.ionic.sdk.cipher.aes.AesCipher;
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
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private AgentSdk() {
        final Logger logger = Logger.getLogger(AgentSdk.class.getName());
        IonicException exception = null;
        try {
            checkForUnlimitedStrength();
            addBCAndCheckForCipherAESGCM();  // we have changed to explicitly require BouncyCastle
            logger.log(Level.FINE, "initialize() = OK");
        } catch (IonicException e) {
            exception = e;
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        this.exceptionInitialize = exception;
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
     * @throws IonicException on cryptography errors
     */
    public static AgentSdk initialize(final Object applicationContext) throws IonicException {
        final AgentSdk agentSdk = SingletonHelper.INSTANCE;
        final IonicException exceptionInitialize = agentSdk.exceptionInitialize;
        if (exceptionInitialize == null) {
            return agentSdk;
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
     * @throws IonicException on cryptography errors
     */
    public static void initialize() throws IonicException {
        final AgentSdk agentSdk = SingletonHelper.INSTANCE;
        final IonicException exceptionInitialize = agentSdk.exceptionInitialize;
        if (exceptionInitialize == null) {
            return;
        } else {
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
    public static final String DELIMITER_AT = "@";

    /**
     * Helper to guard against double init.
     * <p>
     * http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
     */
    private static class SingletonHelper {

        /**
         * The per-process singleton of this object.
         * <p>
         * This object is only being used at present to guard against multiple initializations.  If any state is added
         * to it in the future, where there are different lifecycle expectations, then that code will need to take this
         * present need into account.
         */
        private static final AgentSdk INSTANCE = new AgentSdk();
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
    private void checkForUnlimitedStrength() throws IonicException {
        try {
            final byte[] bytes = new byte[AesCipher.KEY_BITS / Byte.SIZE];
            Arrays.fill(bytes, (byte) 0);
            final SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, AesCipher.ALGORITHM);
            final Cipher cipher = Cipher.getInstance(AesCipher.TRANSFORM_CTR);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISAGENT_INIT_FAILED_KEY_SIZE, e);
        }
    }

    /**
     * Verify that the AES/GCM cipher can be instantiated.  This is available in JRE 8+.  For JRE 7, we require the
     * inclusion of BouncyCastle provider implementation on the classpath, and use reflection to dynamically add the
     * provider.
     *
     * @throws IonicException if the AES/GCM cipher is not available, either from JRE or from BouncyCastle
     */
    private void checkForCipherAESGCM() throws IonicException {
        try {
            Cipher.getInstance(AesCipher.TRANSFORM_GCM);
        } catch (GeneralSecurityException e) {
            addBCAndCheckForCipherAESGCM();
        }
    }

    /**
     * Using reflection, add BouncyCastle provider to provider list.  Then, make a second attempt to instantiate the
     * cipher.
     *
     * @throws IonicException if BouncyCastle provider is not in classpath, or if cipher cannot be instantiated
     */
    private void addBCAndCheckForCipherAESGCM() throws IonicException {
        try {
            addProviderBouncyCastle();
            Cipher.getInstance(AesCipher.TRANSFORM_GCM);
        } catch (ReflectiveOperationException e) {
            throw new IonicException(SdkError.ISAGENT_RESOURCE_NOT_FOUND, e);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISAGENT_INIT_FAILED_PLATFORM, e);
        }
    }

    /**
     * Using reflection, add BouncyCastle provider to provider list.
     *
     * @throws ReflectiveOperationException if BouncyCastle provider is not in classpath
     * @throws GeneralSecurityException     if provider not available
     */
    private void addProviderBouncyCastle() throws ReflectiveOperationException, GeneralSecurityException {
        final String className = CLASSNAME_BC_PROVIDER;
        final Class<?> c = Class.forName(className);
        final Constructor<?> ctor = c.getConstructor();
        final Object object = ctor.newInstance();
        if (object instanceof Provider) {
            Security.addProvider((Provider) object);
        } else {
            throw new GeneralSecurityException(className);
        }
    }

    /**
     * Class name for BouncyCastle Security Provider.  When running in JRE less than or equal 7, BouncyCastle provides
     * implementation of AES/GCM transform (needed for communications with ionic.com).
     */
    private static final String CLASSNAME_BC_PROVIDER = "org.bouncycastle.jce.provider.BouncyCastleProvider";
}
