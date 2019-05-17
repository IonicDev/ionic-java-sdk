package com.ionic.sdk.ks.project.test;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.core.vm.VM;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

/**
 * This test checks the necessary preconditions for the Ionic SDK tests to run.  Its intent is to enable a developer
 * to quickly build the SDK code, given access to the code and (optionally) to an Ionic server infrastructure instance.
 */
public class BuildEnvironmentTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Test that the expected system environment variables are set in the containing environment.
     * <p>
     * The JavaSDK can be run in any of three modes:
     * <ul>
     * <li>Environment variables "TEST_REGISTRATION_URL", "TEST_REGISTRATION_USER", "TEST_REGISTRATION_PASS"
     * are defined to variables needed to perform a new device enrollment.</li>
     * <li>Environment variable "TEST_PROFILE" defined to be the location of a classpath resource containing
     * a valid Secure Enrollment Profile.</li>
     * <li>If none of these environment variables are defined, a test <code>KeyServices</code> implementation is
     * used.</li>
     * </ul>
     * In the first mode, a device enrollment is performed, and that device registration is used for subsequent tests.
     * <p>
     * In the second mode, no device enrollment is performed, and the preexisting device registration is used.
     * <p>
     * In the third mode, the test <code>KeyServices</code> implementation manages access to a local store of key data.
     *
     * @throws IonicException if the URL environment variable format cannot be parsed
     */
    @Test
    public final void testExpectedEnvironmentVariables() throws IonicException {
        // IONIC_REPO_ROOT
        final String repoRoot = System.getenv("IONIC_REPO_ROOT");
        logger.info("IONIC_REPO_ROOT=" + repoRoot);
        Assert.assertNotNull("IONIC_REPO_ROOT environment variable should point to "
                + "folder containing JavaSDK git repo", repoRoot);
        final File folderIonicRepoRoot = new File(repoRoot);
        logger.info(folderIonicRepoRoot.getPath());
        Assert.assertTrue("git repo root folder should exist", folderIonicRepoRoot.exists());
        final File folderJavaSDK = new File(folderIonicRepoRoot, "JavaSDK");
        logger.info(folderJavaSDK.getPath());
        Assert.assertTrue("git repo JavaSDK should exist", folderJavaSDK.exists());
        final IonicTestEnvironment ionicTestEnvironment = IonicTestEnvironment.getInstance();
        if (ionicTestEnvironment.isNewProfile()) {
            // TEST_REGISTRATION_URL
            final String urlString = System.getenv("TEST_REGISTRATION_URL");
            logger.info("TEST_REGISTRATION_URL=" + urlString);
            final URL url = AgentTransactionUtil.getProfileUrl(urlString);
            logger.info("TEST_REGISTRATION_URL (as URL)=" + url.toExternalForm());
            // TEST_REGISTRATION_USER
            final String user = System.getenv("TEST_REGISTRATION_USER");
            logger.info("TEST_REGISTRATION_USER=" + user);
            Assert.assertNotNull("Ionic tenant test user environment variable should be set", user);
            // TEST_REGISTRATION_PASS
            final String password = System.getenv("TEST_REGISTRATION_PASS");
            logger.info("TEST_REGISTRATION_PASS=" + password);
            Assert.assertNotNull("Ionic tenant test user password environment variable should be set", password);
        } else if (ionicTestEnvironment.isExistingProfile()) {
            // TEST_PROFILE
            final String profile = System.getenv("TEST_PROFILE");
            logger.info("TEST_PROFILE=" + profile);
            Assert.assertNotNull("Ionic tenant test profile variable should be set", profile);
        }
    }

    /**
     * Test access to the SDK source tree.
     */
    @Test
    public final void testFilesystemSDKSource() {
        // there are a few tests that examine the SDK source code
        final String repoRoot = System.getenv("IONIC_REPO_ROOT");
        final File folderRepoRoot = new File(repoRoot);
        Assert.assertTrue(folderRepoRoot.exists());
        final File folderRepo = new File(folderRepoRoot, "JavaSDK");
        Assert.assertTrue(folderRepo.exists());
        final File folderJVM = new File(folderRepo, "SDK/ISAgentSDKJVM/AgentSDKJVM");
        Assert.assertTrue(folderJVM.exists());
    }

    /**
     * Test that the filesystem contains a folder for storage of a test Secure Enrollment Profile (SEP).
     *
     * @throws IonicException on failure to locate the file
     */
    @Test
    public final void testFilesystemStorageForTestSEP() throws IonicException {
        final IonicTestEnvironment ionicTestEnvironment = IonicTestEnvironment.getInstance();
        if (ionicTestEnvironment.isNewProfile()) {
            // we need a folder into which the test Secure Enrollment Profile can be persisted
            final File fileTestPersistor = ionicTestEnvironment.getFileProfilePersistor();
            final File folderTestPersistor = fileTestPersistor.getParentFile();
            logger.info("TEST_PERSISTOR_FOLDER=" + folderTestPersistor.getPath());
            Assert.assertTrue(folderTestPersistor.exists());
            Assert.assertTrue(folderTestPersistor.isDirectory());
            // check SEP file
            logger.info("TEST_PERSISTOR_FILE=" + fileTestPersistor.getPath());
            logger.info("TEST_PERSISTOR_FILE (exists)=" + fileTestPersistor.exists());
        } else if (ionicTestEnvironment.isExistingProfile()) {
            final File fileTestPersistor = ionicTestEnvironment.getFileProfilePersistor();
            Assert.assertTrue(fileTestPersistor.exists());
            Assert.assertTrue(fileTestPersistor.isFile());
        }
    }

    /**
     * Dump information about the running JVM.
     */
    @Test
    public final void testDumpJavaVersion() {
        logger.info("JAVA_HOME=" + System.getProperty(VM.Sys.JAVA_HOME));
        logger.info("JAVA_VERSION=" + System.getProperty(VM.Sys.JAVA_VERSION));
    }

    /**
     * Ensure the correct installation of the
     * Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files.
     *
     * @throws IonicException on failure of JRE prerequisites (AES-256, BouncyCastle)
     */
    @Test
    public final void testUnlimitedStrengthJurisdictionPolicyFiles() throws IonicException {
        logger.info(AgentSdk.initialize(null).toString());
    }
}
