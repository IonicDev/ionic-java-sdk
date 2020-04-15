package com.ionic.sdk.ks.device.persist.test;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.crypto.secretshare.SecretShareBucket;
import com.ionic.sdk.crypto.secretshare.SecretShareData;
import com.ionic.sdk.crypto.secretshare.SecretSharePersistor;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorAesGcm;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorPassword;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorPlainText;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorSecretShare;
import com.ionic.sdk.device.profile.persistor.ProfilePersistor;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Iterate through {@link ProfilePersistor} implementations available in main SDK code base.  For each, save a stock
 * profile, then load it.
 */
public class DevicePersistorTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Iterate through available {@link ProfilePersistor} implementations.  For each, save a sample
     * {@link DeviceProfile}.  Then iterate through each persistor, and load the freshly committed profile.
     *
     * @throws IonicException on test environment issues; cryptography failures
     */
    @Test
    public final void testDevicePersistor_SaveLoadProfiles_Success() throws IonicException {
        final DeviceProfile deviceProfile = new DeviceProfile("faux-persistor", 0L,
                "faux.D.513ddebb-c5af-435e-ad7e-8d810daffb9f", "https://localhost",
                new byte[AesCipher.KEY_BYTES], new byte[AesCipher.KEY_BYTES]);
        final List<DeviceProfile> deviceProfiles = Collections.singletonList(deviceProfile);
        final File folder = IonicTestEnvironment.getInstance().getFolderTestOutputsMkdir();
        final Collection<ProfilePersistor> persistors = new ArrayList<ProfilePersistor>();
        // DeviceProfilePersistorAesGcm
        final DeviceProfilePersistorAesGcm persistorAesGcm =
                new DeviceProfilePersistorAesGcm(generateFilePath(folder, "aesgcm", "sep"));
        persistorAesGcm.setKey(CryptoUtils.hexToBin(Value.generate("00", AesCipher.KEY_BYTES)));
        persistorAesGcm.setAuthData(Transcoder.utf8().decode("some authentication data"));
        persistors.add(persistorAesGcm);
        // DeviceProfilePersistorPassword
        final DeviceProfilePersistorPassword persistorPassword =
                new DeviceProfilePersistorPassword(generateFilePath(folder, "password", "sep"));
        persistorPassword.setPassword("some password");
        persistors.add(persistorPassword);
        // DeviceProfilePersistorPlainText
        final DeviceProfilePersistorPlainText persistorPlainText =
                new DeviceProfilePersistorPlainText(generateFilePath(folder, "plaintext", "sep"));
        persistors.add(persistorPlainText);
        // DeviceProfilePersistorSecretShare
        final SecretShareData secretShareData = new SecretShareDataTest();
        final SecretSharePersistor secretSharePersistor =
                new SecretSharePersistor(generateFilePath(folder, "secretshare", "ss"), secretShareData);
        final DeviceProfilePersistorSecretShare persistorSecretShare =
                new DeviceProfilePersistorSecretShare(secretSharePersistor);
        persistorSecretShare.setFilePath(generateFilePath(folder, "secretshare", "sep"));
        persistors.add(persistorSecretShare);
        // serialize all persistors
        for (ProfilePersistor persistor : persistors) {
            persistor.saveAllProfiles(deviceProfiles, deviceProfile.getDeviceId());
        }
        for (ProfilePersistor persistor : persistors) {
            final String[] activeProfile = new String[1];
            final List<DeviceProfile> deviceProfilesLoad = persistor.loadAllProfiles(activeProfile);
            logger.info(activeProfile[0]);
            Assert.assertEquals(1, deviceProfilesLoad.size());
            Assert.assertEquals(deviceProfile.getDeviceId(), activeProfile[0]);
        }
    }

    /**
     * Utility function to generate a test file path for commit of persistor data to filesystem.
     *
     * @param folder the folder in which the file content should be written
     * @param name   descriptor for name of file
     * @param type   descriptor for type of file
     * @return a file path in $folder, suitable for commit of data
     */
    private String generateFilePath(final File folder, final String name, final String type) {
        return new File(folder, String.format("%s.%s.%s", getClass().getSimpleName(), name, type)).getPath();
    }

    /**
     * Test implementation of {@link SecretShareData}.  This implementation specifies that two of the three properties
     * "key1", "key2", and "key3" must match in order to recover the secret.
     */
    private static class SecretShareDataTest implements SecretShareData {

        @Override
        public Properties getData() {
            final Properties properties = new Properties();
            properties.setProperty("key1", "value1");
            properties.setProperty("key2", "value2");
            properties.setProperty("key3", "value3");
            properties.setProperty("key4", "value4");
            properties.setProperty("key5", "value5");
            return properties;
        }

        @Override
        public Collection<SecretShareBucket> getBuckets() {
            final Collection<SecretShareBucket> buckets = new ArrayList<SecretShareBucket>();
            buckets.add(new SecretShareBucket(Arrays.asList("key1", "key2", "key3"), 2));
            return buckets;
        }
    }
}
