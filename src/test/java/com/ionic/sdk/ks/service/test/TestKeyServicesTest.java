package com.ionic.sdk.ks.service.test;

import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysRequest;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.ks.service.TestKeyServices;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.logging.Logger;

/**
 * Test cases for {@link TestKeyServices} implementation.
 */
public class TestKeyServicesTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Attempt creation of a single key.  Check implementation accessors.
     *
     * @throws IonicException on data expectation failures
     */
    @Test
    public void testKeyServices_CreateKey() throws IonicException {
        final String keyspaceKeyServices = "Java";
        final TestKeyServices keyServices = new TestKeyServices(keyspaceKeyServices);
        final DeviceProfile deviceProfile = keyServices.getActiveProfile();
        Assert.assertNotNull(deviceProfile);
        final String keyspace = deviceProfile.getKeySpace();
        logger.info(keyspace);
        Assert.assertEquals(keyspaceKeyServices, keyspace);
        final CreateKeysResponse createKeysResponse = keyServices.createKey();
        Assert.assertNotNull(createKeysResponse);
        final List<CreateKeysResponse.Key> keys = createKeysResponse.getKeys();
        Assert.assertEquals(1, keys.size());
        final CreateKeysResponse.Key key = createKeysResponse.getFirstKey();
        Assert.assertTrue(key.getId().startsWith(keyspace));
    }

    /**
     * Attempt creation of multiple key types.
     */
    @Test
    public void testKeyServices_CreateKeys() {
        final CreateKeysRequest.Key request1 = new CreateKeysRequest.Key("1", 1);
        final CreateKeysRequest.Key request2 = new CreateKeysRequest.Key("2", 2);
        final CreateKeysRequest createKeysRequest = new CreateKeysRequest(request1, request2);
        final TestKeyServices keyServices = new TestKeyServices("Java");
        final CreateKeysResponse createKeysResponse = keyServices.createKeys(createKeysRequest);
        Assert.assertEquals(1 + 2, createKeysResponse.getKeys().size());
        Assert.assertEquals(1, createKeysResponse.findKeysByRef("1").size());
        Assert.assertEquals(2, createKeysResponse.findKeysByRef("2").size());
    }

    /**
     * Attempt fetch scenarios for a single key.
     */
    @Test
    public void testKeyServices_GetKey_FailureCases() {
        final TestKeyServices keyServices = new TestKeyServices("Java");
        try {
            keyServices.getKey(null);  // null input
        } catch (IonicException e) {
            Assert.assertEquals(SdkError.ISAGENT_NULL_INPUT, e.getReturnCode());
        }
        try {
            keyServices.getKey("ABCD1234567");  // invalid keyspace
        } catch (IonicException e) {
            Assert.assertEquals(SdkError.ISAGENT_KEY_DENIED, e.getReturnCode());
        }
        try {
            keyServices.getKey("Java123456");  // invalid length
        } catch (IonicException e) {
            Assert.assertEquals(SdkError.ISAGENT_KEY_DENIED, e.getReturnCode());
        }
    }

    /**
     * Attempt fetch of a single key.
     *
     * @throws IonicException on data expectation failures
     */
    @Test
    public void testKeyServices_GetKey_SuccessCases() throws IonicException {
        final TestKeyServices keyServices = new TestKeyServices("Java");
        final GetKeysResponse getKeysResponse = keyServices.getKey("Java1234567");
        Assert.assertEquals(1, getKeysResponse.getKeys().size());
    }

    /**
     * Attempt fetch of a multiple keys.
     */
    @Test
    public void testKeyServices_GetKeys() {
        final TestKeyServices keyServices = new TestKeyServices("Java");
        final GetKeysRequest getKeysRequest = new GetKeysRequest("Java1234567", "Java1234568");
        final GetKeysResponse getKeysResponse = keyServices.getKeys(getKeysRequest);
        Assert.assertEquals(2, getKeysResponse.getKeys().size());
    }
}
