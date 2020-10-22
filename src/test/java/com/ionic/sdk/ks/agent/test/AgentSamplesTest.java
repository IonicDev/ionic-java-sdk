package com.ionic.sdk.ks.agent.test;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.device.profile.persistor.DeviceProfiles;
import com.ionic.sdk.device.profile.persistor.ProfilePersistor;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Concise JUnit tests incorporated into JavaDoc.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AgentSamplesTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Verify simple agent CreateKey / GetKey operation.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testAgent_Initialize_CreateKey_GetKey() throws IonicException {
        final ProfilePersistor profilePersistor = IonicTestEnvironment.getInstance().getProfilePersistor();
        final Agent agent = new Agent(profilePersistor);
        final CreateKeysResponse createKeysResponse = agent.createKey();
        final AgentKey keyCreate = createKeysResponse.getFirstKey();
        final GetKeysResponse getKeysResponse = agent.getKey(keyCreate.getId());
        final AgentKey keyGet = getKeysResponse.getKeys().iterator().next();
        Assert.assertEquals(keyCreate.getId(), keyGet.getId());
        logger.info(keyCreate.getId());
    }

    /**
     * Verify simple agent CreateKey / GetKey operation.  {@link Agent} is initialized to use a
     * single {@link DeviceProfile}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testAgent_InitializeFromDeviceProfile() throws IonicException {
        final ProfilePersistor profilePersistor = IonicTestEnvironment.getInstance().getProfilePersistor();
        final Agent agentLoadProfiles = new Agent(profilePersistor);
        final DeviceProfile activeProfile = agentLoadProfiles.getActiveProfile();
        // instantiate blank Agent
        final Agent agent = new Agent();
        agent.initializeWithoutProfiles();
        agent.addProfile(activeProfile, true);
        final CreateKeysResponse createKeysResponse = agent.createKey();
        final AgentKey keyCreate = createKeysResponse.getFirstKey();
        final GetKeysResponse getKeysResponse = agent.getKey(keyCreate.getId());
        final AgentKey keyGet = getKeysResponse.getKeys().iterator().next();
        Assert.assertEquals(keyCreate.getId(), keyGet.getId());
        logger.info(keyCreate.getId());
    }

    /**
     * Verify ability to instantiate agent using custom {@link AgentConfig}.  This AgentConfig sets a custom
     * HTTP user agent in each of its communications with the key service.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testAgent_InitializeFromAgentConfig() throws IonicException {
        final AgentConfig agentConfig = new AgentConfig();
        agentConfig.setUserAgent("Custom Machina HTTP User Agent");
        final Agent agent = new Agent(agentConfig);
        final ProfilePersistor profilePersistor = IonicTestEnvironment.getInstance().getProfilePersistor();
        agent.initialize(profilePersistor);
        final CreateKeysResponse createKeysResponse = agent.createKey();
        Assert.assertEquals(1, createKeysResponse.getKeys().size());
    }

    /**
     * Verify ability to update in-memory copy of active profile (loaded from file using
     * {@link Agent#initialize(ProfilePersistor)}) with updated API URL retrieved from KNS HTTPS endpoint.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testAgent_KNS_UpdateActiveProfile_FromProfilePersistor() throws IonicException {
        final ProfilePersistor profilePersistor = IonicTestEnvironment.getInstance().getProfilePersistor();
        final Agent agent = new Agent(profilePersistor);
        Assume.assumeFalse("no KNS record, on premise test tenant",
                agent.getActiveProfile().getKeySpace().equals("PDs3"));
        final String deviceId = agent.getActiveProfile().getDeviceId();
        final String server = agent.getActiveProfile().getServer();
        agent.updateProfileFromKNS(deviceId, "https://api.ionic.com");
        final String serverUpdate = agent.getActiveProfile().getServer();
        Assert.assertEquals(server, serverUpdate);  // url should be the same
        Assert.assertNotSame(server, serverUpdate);  // the string reference should be updated
    }

    /**
     * Verify ability to update in-memory copy of active profile (loaded from JSON string using
     * {@link Agent#Agent(DeviceProfiles)}) with updated API URL retrieved from KNS HTTPS endpoint.
     *
     * @throws IonicException on initialization failure, operation failure
     * @throws IOException    on filesystem read failure
     */
    @Test
    public final void testAgent_KNS_UpdateActiveProfile_FromJsonString() throws IonicException, IOException {
        final File file = IonicTestEnvironment.getInstance().getFileProfilePersistor();
        Assert.assertNotNull(file);
        final String json = Transcoder.utf8().encode(Stream.read(file));
        final Agent agent = new Agent(new DeviceProfiles(json));
        Assume.assumeFalse("no KNS record, on premise test tenant",
                agent.getActiveProfile().getKeySpace().equals("PDs3"));
        final String deviceId = agent.getActiveProfile().getDeviceId();
        final String server = agent.getActiveProfile().getServer();
        agent.updateProfileFromKNS(deviceId, "https://api.ionic.com");
        final String serverUpdate = agent.getActiveProfile().getServer();
        Assert.assertEquals(server, serverUpdate);  // url should be the same
        Assert.assertNotSame(server, serverUpdate);  // the string reference should be updated
    }

    /**
     * Verify ability to persist update of active profile API URL to filesystem using
     * {@link Agent#saveProfiles(ProfilePersistor)}.
     * <p>
     * Since this test case writes to the filesystem, any error in the
     * {@link Agent#updateProfileFromKNS(String, String)} operation will impact subsequent test cases
     * within the run of the test suite.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testAgent_KNS_UpdateActiveProfile_PersistUpdate() throws IonicException {
        final ProfilePersistor profilePersistor = IonicTestEnvironment.getInstance().getProfilePersistor();
        final Agent agent = new Agent(profilePersistor);
        Assume.assumeFalse("no KNS record, on premise test tenant",
                agent.getActiveProfile().getKeySpace().equals("PDs3"));
        final String deviceId = agent.getActiveProfile().getDeviceId();
        agent.updateProfileFromKNS(deviceId, "https://api.ionic.com");
        agent.saveProfiles(profilePersistor);
    }
}
