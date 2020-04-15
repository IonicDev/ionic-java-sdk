package com.ionic.sdk.ks.device.create.test;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceResponse;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.cipher.rsa.model.RsaKeyHolder;
import com.ionic.sdk.core.vm.VM;
import com.ionic.sdk.device.create.EnrollSAML;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.device.profile.persistor.DeviceProfiles;
import com.ionic.sdk.device.profile.persistor.ProfilePersistor;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test mechanics of CreateDevice SAML transaction using SDK API class.
 */
public class CreateDeviceAPITest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Enrollment test cases to be run on Java 8 need Ionic cryptography initialized with BouncyCastle provider.
     *
     * @throws IonicException on cryptography initialization failure
     */
    @BeforeClass
    public static void setUp() throws IonicException {
        AgentSdk.initialize(new BouncyCastleProvider());
    }

    /**
     * Test usage of API class to enroll a new device record on the server.
     */
    @Test
    public final void testCreateDeviceSAML() {
        final IonicTestEnvironment ionicTestEnvironment = IonicTestEnvironment.getInstance();
        Assume.assumeTrue("don't register when using existing SEP", ionicTestEnvironment.isNewProfile());
        final String url = System.getenv("TEST_REGISTRATION_URL");
        Assume.assumeFalse("only use with internal tenants", url.contains("https://enrollment.ionic.com/"));
        try {
            final boolean useRsaExisting = System.getProperty(VM.Sys.OS_ARCH).equals("arm");
            final RsaKeyHolder rsaKeyHolder = useRsaExisting ? ionicTestEnvironment.getTestRsaKeyHolder() : null;
            final EnrollSAML enrollSAML = new EnrollSAML(url, new Agent(), rsaKeyHolder);
            final String user = System.getenv("TEST_REGISTRATION_USER");
            final String pass = System.getenv("TEST_REGISTRATION_PASS");
            logger.info(String.format("ENROLL, URL=[%s], USER=[%s]", url, user));
            final CreateDeviceResponse createDeviceResponse = enrollSAML.enroll(user, pass, getClass().getName());
            final DeviceProfile deviceProfile = createDeviceResponse.getDeviceProfile();
            final String deviceId = deviceProfile.getDeviceId();
            logger.info(String.format("ENROLL SUCCESS, URL=[%s], USER=[%s], DEVICEID=[%s]", url, user, deviceId));
            // unconditionally persist this newly created SEP (overwrite existing) (needed for all of the other tests)
            final ProfilePersistor persistor = ionicTestEnvironment.getProfilePersistor();
            final Agent agent = new Agent((ProfilePersistor) null);
            agent.addProfile(deviceProfile);
            agent.setActiveProfile(deviceProfile.getDeviceId());
            agent.saveProfiles(persistor);
            logger.info(new DeviceProfiles(deviceProfile).toJson());
            // demonstrate that this newly created agent can successfully create a tenant key
            final CreateKeysResponse keysResponse = agent.createKey();
            Assert.assertEquals(1, keysResponse.getKeys().size());
            final CreateKeysResponse.Key key = keysResponse.getKeys().iterator().next();
            Assert.assertNotNull(key);
            logger.info(key.getId());
        } catch (IonicException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }
}
