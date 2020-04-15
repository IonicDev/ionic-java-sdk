package com.ionic.sdk.ks.device.persist.test;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorAesGcm;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorPassword;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorPlainText;
import com.ionic.sdk.device.profile.persistor.ProfilePersistor;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.logging.Logger;

/**
 * Concise JUnit tests incorporated into JavaDoc.
 */
public class ProfilePersistorSamplesTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Reference to test suite output folder.
     */
    private File folderOutput = null;

    /**
     * Get reference to test suite output folder.
     *
     * @throws IonicException on failure to get reference to output folder
     */
    @Before
    public void setUp() throws IonicException {
        folderOutput = IonicTestEnvironment.getInstance().getFolderTestOutputsMkdir();
        Assert.assertTrue(folderOutput.exists());
    }

    /**
     * Verify simple {@link ProfilePersistor} save / load operation using {@link DeviceProfilePersistorPlainText}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testProfilePersistorPlaintext_SaveLoadProfiles() throws IonicException {
        final ProfilePersistor profilePersistorTest = IonicTestEnvironment.getInstance().getProfilePersistor();
        final Agent agent1 = new Agent(profilePersistorTest);
        // persist the DeviceProfile information to a new file
        final File filePersistor = new File(folderOutput, getClass().getSimpleName() + ".plaintext.sep");
        final DeviceProfilePersistorPlainText profilePersistor1 =
                new DeviceProfilePersistorPlainText(filePersistor.getPath());
        agent1.saveProfiles(profilePersistor1);
        // load the DeviceProfile information from the new file
        final DeviceProfilePersistorPlainText profilePersistor2 =
                new DeviceProfilePersistorPlainText(filePersistor.getPath());
        final Agent agent2 = new Agent(profilePersistor2);
        Assert.assertEquals(agent1.getActiveProfile().getDeviceId(), agent2.getActiveProfile().getDeviceId());
        logger.info(agent1.getActiveProfile().getDeviceId());
    }

    /**
     * Verify simple {@link ProfilePersistor} save / load operation using {@link DeviceProfilePersistorPassword}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testProfilePersistorPassword_SaveLoadProfiles() throws IonicException {
        final String password = Long.toString(System.currentTimeMillis());
        logger.info(String.format("PASSWORD = %s", password));
        final ProfilePersistor profilePersistorTest = IonicTestEnvironment.getInstance().getProfilePersistor();
        final Agent agent1 = new Agent(profilePersistorTest);
        // persist the DeviceProfile information to a new file
        final File filePersistor = new File(folderOutput, getClass().getSimpleName() + ".password.sep");
        final DeviceProfilePersistorPassword profilePersistor1 =
                new DeviceProfilePersistorPassword(filePersistor.getPath());
        profilePersistor1.setPassword(password);
        agent1.saveProfiles(profilePersistor1);
        // load the DeviceProfile information from the new file
        final DeviceProfilePersistorPassword profilePersistor2 =
                new DeviceProfilePersistorPassword(filePersistor.getPath());
        profilePersistor2.setPassword(password);
        final Agent agent2 = new Agent(profilePersistor2);
        Assert.assertEquals(agent1.getActiveProfile().getDeviceId(), agent2.getActiveProfile().getDeviceId());
        logger.info(agent1.getActiveProfile().getDeviceId());
    }

    /**
     * Verify simple {@link ProfilePersistor} save / load operation using {@link DeviceProfilePersistorAesGcm}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testProfilePersistorAesGcm_SaveLoadProfiles() throws IonicException {
        final byte[] keyBytes = new byte[AesCipher.KEY_BYTES];
        final byte[] aad = new byte[AesCipher.KEY_BYTES];
        final ProfilePersistor profilePersistorTest = IonicTestEnvironment.getInstance().getProfilePersistor();
        final Agent agent1 = new Agent(profilePersistorTest);
        // persist the DeviceProfile information to a new file
        final File filePersistor = new File(folderOutput, getClass().getSimpleName() + ".aesgcm.sep");
        final DeviceProfilePersistorAesGcm profilePersistor1 =
                new DeviceProfilePersistorAesGcm(filePersistor.getPath());
        profilePersistor1.setKey(keyBytes);
        profilePersistor1.setAuthData(aad);
        agent1.saveProfiles(profilePersistor1);
        // load the DeviceProfile information from the new file
        final DeviceProfilePersistorAesGcm profilePersistor2 =
                new DeviceProfilePersistorAesGcm(filePersistor.getPath());
        profilePersistor2.setKey(keyBytes);
        profilePersistor2.setAuthData(aad);
        final Agent agent2 = new Agent(profilePersistor2);
        Assert.assertEquals(agent1.getActiveProfile().getDeviceId(), agent2.getActiveProfile().getDeviceId());
        logger.info(agent1.getActiveProfile().getDeviceId());
    }
}
