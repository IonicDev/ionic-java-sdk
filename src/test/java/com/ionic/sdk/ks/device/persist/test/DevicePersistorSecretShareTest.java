package com.ionic.sdk.ks.device.persist.test;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.crypto.secretshare.SecretShareBucket;
import com.ionic.sdk.crypto.secretshare.SecretShareData;
import com.ionic.sdk.crypto.secretshare.SecretSharePersistor;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorSecretShare;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test cases for {@link DeviceProfilePersistorSecretShare} scenarios.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DevicePersistorSecretShareTest {

    /**
     * Class scoped logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DevicePersistorSecretShareTest.class.getName());

    /**
     * Filesystem folder for storage of {@link DeviceProfilePersistorSecretShare} data.
     */
    private static final File FOLDER_TEMP = new File(System.getProperty("java.io.tmpdir"));

    /**
     * Filesystem folder for storage of {@link DeviceProfilePersistorSecretShare} data.
     */
    private static final File FILE_SS_NUMBER = new File(FOLDER_TEMP, "secretshare.number.ss");

    /**
     * Filesystem folder for storage of {@link DeviceProfilePersistorSecretShare} data.
     */
    private static final File FILE_SS_LETTER = new File(FOLDER_TEMP, "secretshare.letter.ss");

    /**
     * Filesystem folder for storage of {@link DeviceProfilePersistorSecretShare} data.
     */
    private static final File FILE_SEP_NUMBER = new File(FOLDER_TEMP, "secretshare.number.sep");

    /**
     * Filesystem folder for storage of {@link DeviceProfilePersistorSecretShare} data.
     */
    private static final File FILE_SEP_LETTER = new File(FOLDER_TEMP, "secretshare.letter.sep");

    /**
     * Initialize filesystem state for this test class.  Any leftover data files should be discarded.
     */
    @BeforeClass
    public static void beforeClass() {
        LOGGER.info(String.format("INITIALIZING FILE [EXISTS=%s]", FILE_SS_NUMBER.delete()));
        LOGGER.info(String.format("INITIALIZING FILE [EXISTS=%s]", FILE_SS_LETTER.delete()));
        LOGGER.info(String.format("INITIALIZING FILE [EXISTS=%s]", FILE_SEP_NUMBER.delete()));
        LOGGER.info(String.format("INITIALIZING FILE [EXISTS=%s]", FILE_SEP_LETTER.delete()));
    }

    /**
     * Restore filesystem state from this test class.  Any leftover data files should be discarded.
     */
    @AfterClass
    public static void afterClass() {
        LOGGER.info(String.format("REMOVING FILE [EXISTS=%s]", FILE_SS_NUMBER.delete()));
        LOGGER.info(String.format("REMOVING FILE [EXISTS=%s]", FILE_SS_LETTER.delete()));
        LOGGER.info(String.format("REMOVING FILE [EXISTS=%s]", FILE_SEP_NUMBER.delete()));
        LOGGER.info(String.format("REMOVING FILE [EXISTS=%s]", FILE_SEP_LETTER.delete()));
    }

    /**
     * Check preconditions for test cases in this test class.
     */
    @Test
    public final void testDevicePersistor_0_CheckPreconditions() {
        Assert.assertFalse(FILE_SS_NUMBER.exists());
        Assert.assertFalse(FILE_SS_LETTER.exists());
        Assert.assertFalse(FILE_SEP_NUMBER.exists());
        Assert.assertFalse(FILE_SEP_LETTER.exists());
    }

    /**
     * Create and save a {@link SecretSharePersistor} file /
     * {@link com.ionic.sdk.device.profile.persistor.ProfilePersistor} file combo.  Uses {@link SecretShareDataNumber}
     * implementation (internal class).
     *
     * @throws IonicException on persistence failures
     */
    @Test
    public final void testDevicePersistor_1A_SaveProfilePersistorNumber() throws IonicException {
        // generate/persist SecretShareData
        final SecretShareData secretShareData = new SecretShareDataNumber();
        final SecretSharePersistor secretSharePersistor =
                new SecretSharePersistor(FILE_SS_NUMBER.getPath(), secretShareData);
        // generate/persist DeviceProfile data
        final DeviceProfilePersistorSecretShare profilePersistor =
                new DeviceProfilePersistorSecretShare(secretSharePersistor);
        profilePersistor.setFilePath(FILE_SEP_NUMBER.getPath());
        final DeviceProfile deviceProfile = new DeviceProfile("persistor1", 0L,
                "513ddebb-c5af-435e-ad7e-8d810daffb9f", "https://localhost",
                new byte[AesCipher.KEY_BYTES], new byte[AesCipher.KEY_BYTES]);
        final List<DeviceProfile> deviceProfiles = Collections.singletonList(deviceProfile);
        profilePersistor.saveAllProfiles(deviceProfiles, deviceProfile.getDeviceId());
    }

    /**
     * Create and save a {@link SecretSharePersistor} file /
     * {@link com.ionic.sdk.device.profile.persistor.ProfilePersistor} file combo.  Uses {@link SecretShareDataNumber}
     * implementation (internal class).
     *
     * @throws IonicException on persistence failures
     */
    @Test
    public final void testDevicePersistor_1B_SaveProfilePersistorLetter() throws IonicException {
        // generate/persist SecretShareData
        final SecretShareData secretShareData = new SecretShareDataLetter();
        final SecretSharePersistor secretSharePersistor =
                new SecretSharePersistor(FILE_SS_LETTER.getPath(), secretShareData);
        // generate/persist DeviceProfile data
        final DeviceProfilePersistorSecretShare profilePersistor =
                new DeviceProfilePersistorSecretShare(secretSharePersistor);
        profilePersistor.setFilePath(FILE_SEP_LETTER.getPath());
        final DeviceProfile deviceProfile = new DeviceProfile("persistor1", 0L,
                "513ddebb-c5af-435e-ad7e-8d810daffb9f", "https://localhost",
                new byte[AesCipher.KEY_BYTES], new byte[AesCipher.KEY_BYTES]);
        final List<DeviceProfile> deviceProfiles = Collections.singletonList(deviceProfile);
        profilePersistor.saveAllProfiles(deviceProfiles, deviceProfile.getDeviceId());
    }

    /**
     * Recover serialized {@link com.ionic.sdk.device.profile.persistor.ProfilePersistor}.
     *
     * @throws IonicException on deserialization failures
     */
    @Test
    public final void testDevicePersistor_2_LoadProfilePersistor_ExpectSuccess() throws IonicException {
        // retrieve SecretShareData
        final SecretShareData secretShareData = new SecretShareDataNumber();
        final SecretSharePersistor secretSharePersistor =
                new SecretSharePersistor(FILE_SS_NUMBER.getPath(), secretShareData);
        // retrieve ProfilePersistor data
        final DeviceProfilePersistorSecretShare profilePersistor =
                new DeviceProfilePersistorSecretShare(secretSharePersistor);
        profilePersistor.setFilePath(FILE_SEP_NUMBER.getPath());
        final String[] activeProfile = new String[1];
        final List<DeviceProfile> deviceProfilesLoad = profilePersistor.loadAllProfiles(activeProfile);
        // verify expectations
        Assert.assertEquals("513ddebb-c5af-435e-ad7e-8d810daffb9f", activeProfile[0]);
        Assert.assertEquals(1, deviceProfilesLoad.size());
    }

    /**
     * Recover serialized {@link com.ionic.sdk.device.profile.persistor.ProfilePersistor}.  Tamper with environment
     * data (within tolerance).
     *
     * @throws IonicException on deserialization failures
     */
    @Test
    public final void testDevicePersistor_3_LoadProfilePersistorTamper1_ExpectSuccess() throws IonicException {
        // retrieve SecretShareData
        final SecretShareDataNumber secretShareData = new SecretShareDataNumber();
        secretShareData.tamper("key2", "value2Tamper");
        final SecretSharePersistor secretSharePersistor =
                new SecretSharePersistor(FILE_SS_NUMBER.getPath(), secretShareData);
        // retrieve ProfilePersistor data
        final DeviceProfilePersistorSecretShare profilePersistor =
                new DeviceProfilePersistorSecretShare(secretSharePersistor);
        profilePersistor.setFilePath(FILE_SEP_NUMBER.getPath());
        final String[] activeProfile = new String[1];
        final List<DeviceProfile> deviceProfilesLoad = profilePersistor.loadAllProfiles(activeProfile);
        // verify expectations
        Assert.assertEquals("513ddebb-c5af-435e-ad7e-8d810daffb9f", activeProfile[0]);
        Assert.assertEquals(1, deviceProfilesLoad.size());
    }

    /**
     * Recover serialized {@link com.ionic.sdk.device.profile.persistor.ProfilePersistor}.  Tamper with environment
     * data (outside tolerance).
     */
    @Test
    public final void testDevicePersistor_4_LoadProfilePersistorTamper2_ExpectFailure() {
        // retrieve SecretShareData
        final SecretShareDataNumber secretShareData = new SecretShareDataNumber();
        secretShareData.tamper("key2", "value2Tamper");
        secretShareData.tamper("key4", "value4Tamper");
        final SecretSharePersistor secretSharePersistor =
                new SecretSharePersistor(FILE_SS_NUMBER.getPath(), secretShareData);
        try {
            // retrieve ProfilePersistor data
            final DeviceProfilePersistorSecretShare profilePersistor =
                    new DeviceProfilePersistorSecretShare(secretSharePersistor);
            LOGGER.info(profilePersistor.toString());
            Assert.fail("exception expected");
            profilePersistor.setFilePath(FILE_SEP_NUMBER.getPath());
            final String[] activeProfile = new String[1];
            final List<DeviceProfile> deviceProfilesLoad = profilePersistor.loadAllProfiles(activeProfile);
            // verify expectations
            Assert.assertEquals("513ddebb-c5af-435e-ad7e-8d810daffb9f", activeProfile[0]);
            Assert.assertEquals(1, deviceProfilesLoad.size());
        } catch (IonicException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            Assert.assertEquals(SdkError.ISCRYPTO_ERROR, e.getReturnCode());
        }
    }

    /**
     * Recover serialized {@link com.ionic.sdk.device.profile.persistor.ProfilePersistor}.  Use mismatched
     * SecretSharePersistor / ProfilePersistor.
     *
     * @throws IonicException on persistence failures
     */
    @Test
    public final void testDevicePersistor_5_LoadProfilePersistor_MixData_ExpectFailure() throws IonicException {
        // retrieve SecretShareData
        final SecretShareDataLetter secretShareData = new SecretShareDataLetter();
        secretShareData.tamper("keyB", "valueBTamper");
        final SecretSharePersistor secretSharePersistor =
                new SecretSharePersistor(FILE_SS_LETTER.getPath(), secretShareData);
        // retrieve ProfilePersistor data
        final DeviceProfilePersistorSecretShare profilePersistor =
                new DeviceProfilePersistorSecretShare(secretSharePersistor);
        profilePersistor.setFilePath(FILE_SEP_LETTER.getPath());

        final Agent agent = new Agent();
        try {
            agent.initialize(profilePersistor);
        } catch (IonicException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            Assert.assertEquals(SdkError.ISCRYPTO_ERROR, e.getReturnCode());
        }
    }

    /**
     * Test implementation of {@link SecretShareData}.  This implementation specifies that four of the five properties
     * "key1", "key2", "key3", "key4", "key5" must match in order to recover the secret.
     */
    private static class SecretShareDataNumber implements SecretShareData {

        /**
         * Properties are collected at construction and retained for lifetime of object.
         */
        private final Properties properties;

        /**
         * Constructor.
         */
        SecretShareDataNumber() {
            this.properties = new Properties();
            properties.setProperty("key1", "value1");
            properties.setProperty("key2", "value2");
            properties.setProperty("key3", "value3");
            properties.setProperty("key4", "value4");
            properties.setProperty("key5", "value5");
        }

        /**
         * Tamper with a value previously collected from the environment.
         *
         * @param key   the property key
         * @param value the updated property value
         */
        public final void tamper(final String key, final String value) {
            properties.setProperty(key, value);
        }

        @Override
        public Properties getData() {
            return properties;
        }

        @Override
        public Collection<SecretShareBucket> getBuckets() {
            final int minMatch = 4;
            final Collection<SecretShareBucket> buckets = new ArrayList<SecretShareBucket>();
            buckets.add(new SecretShareBucket(Arrays.asList("key1", "key2", "key3", "key4", "key5"), minMatch));
            return buckets;
        }
    }

    /**
     * Test implementation of {@link SecretShareData}.  This implementation specifies that four of the five properties
     * "keyA", "keyB", "keyC", "keyD", "keyE" must match in order to recover the secret.
     */
    private static class SecretShareDataLetter implements SecretShareData {

        /**
         * Properties are collected at construction and retained for lifetime of object.
         */
        private final Properties properties;

        /**
         * Constructor.
         */
        SecretShareDataLetter() {
            this.properties = new Properties();
            properties.setProperty("keyA", "valueA");
            properties.setProperty("keyB", "valueB");
            properties.setProperty("keyC", "valueC");
            properties.setProperty("keyD", "valueD");
            properties.setProperty("keyE", "valueE");
        }

        /**
         * Tamper with a value previously collected from the environment.
         *
         * @param key   the property key
         * @param value the updated property value
         */
        public final void tamper(final String key, final String value) {
            properties.setProperty(key, value);
        }

        @Override
        public Properties getData() {
            return properties;
        }

        @Override
        public Collection<SecretShareBucket> getBuckets() {
            final int minMatch = 4;
            final Collection<SecretShareBucket> buckets = new ArrayList<SecretShareBucket>();
            buckets.add(new SecretShareBucket(Arrays.asList("keyA", "keyB", "keyC", "keyD", "keyE"), minMatch));
            return buckets;
        }
    }
}
