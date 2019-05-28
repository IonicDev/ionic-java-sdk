package com.ionic.sdk.ks.request.updatekey.test;

import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysRequest;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysResponse;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Test ability to update the mutable attributes of Ionic symmetric keys, in the context of a
 * {@link com.ionic.sdk.key.KeyServices} request.
 */
public class CreateUpdateKeyTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Create new keys, and then update those keys with altered mutable attributes.
     *
     * @throws IonicException on SDK errors
     */
    @Test
    public final void testUpdateKey_CreateUpdate_Success() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        // build and run a single request for keys
        final CreateKeysRequest createKeysRequest = new CreateKeysRequest();
        final String refId1 = getClass().getSimpleName() + "1";
        final String refId2 = getClass().getSimpleName() + "2";
        final KeyAttributesMap keyAttributesQ1 = new KeyAttributesMap();
        final KeyAttributesMap keyAttributesQ2 = new KeyAttributesMap();
        keyAttributesQ1.put("attrC", Arrays.asList("0", "1"));
        keyAttributesQ2.put("attrC", Arrays.asList("0", "1"));
        final KeyAttributesMap mutableAttributesQ = new KeyAttributesMap();
        mutableAttributesQ.put("attrM", new ArrayList<String>(Arrays.asList("A", "B")));
        createKeysRequest.add(new CreateKeysRequest.Key(refId1, 1, keyAttributesQ1, mutableAttributesQ));
        createKeysRequest.add(new CreateKeysRequest.Key(refId2, 1, keyAttributesQ2, mutableAttributesQ));
        final CreateKeysResponse createKeysResponse = keyServices.createKeys(createKeysRequest);
        Assert.assertNotNull(createKeysResponse);
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, createKeysResponse.getHttpResponseCode());
        final Collection<CreateKeysResponse.Key> createKeysResponseKeys = createKeysResponse.getKeys();
        Assert.assertEquals(2, createKeysResponseKeys.size());
        // add an update for each key to an update request
        final UpdateKeysRequest updateKeysRequest = new UpdateKeysRequest();
        for (CreateKeysResponse.Key createKey : createKeysResponseKeys) {
            final String keyId = createKey.getId();
            final byte[] key = createKey.getKey();
            logger.info(String.format("KEY/[%s]=HEX[%s]", keyId, Transcoder.hex().encode(key)));
            Assert.assertNotNull(keyId);
            Assert.assertNotNull(key);
            Assert.assertEquals(AesCipher.KEY_BITS / Byte.SIZE, key.length);
            // immutable attributes
            final KeyAttributesMap keyAttributes = createKey.getAttributesMap();
            Assert.assertEquals(1, keyAttributes.size());
            final List<String> attrValueC1 = keyAttributes.get("attrC");
            Assert.assertEquals(2, attrValueC1.size());
            Assert.assertTrue(attrValueC1.contains("0"));
            Assert.assertTrue(attrValueC1.contains("1"));
            // mutable attributes
            final KeyAttributesMap mutableAttributes = createKey.getMutableAttributesMap();
            Assert.assertEquals(1, mutableAttributes.size());
            //final List<String> attrValueM = mutableAttributes.get("attrM");
            // mutate attributes: [A, B] -> [A, B, C, D]
            final UpdateKeysRequest.Key updateKeyU = new UpdateKeysRequest.Key(createKey, false);
            final KeyAttributesMap mutableAttributesU = updateKeyU.getMutableAttributesMap();
            final List<String> attrValueU = mutableAttributesU.get("attrM");
            attrValueU.add("C");
            attrValueU.add("D");
            updateKeysRequest.addKey(updateKeyU);
            // carry out update transaction
            final UpdateKeysResponse updateKeysResponse = keyServices.updateKeys(updateKeysRequest);
            // check update server response
            Assert.assertNotNull(updateKeysResponse);
            Assert.assertFalse(AgentTransactionUtil.isHttpErrorCode(updateKeysResponse.getHttpResponseCode()));
            Assert.assertEquals(1, updateKeysResponse.getKeys().size());
            // iterate through requested updates, and verify state
            final UpdateKeysResponse.Key updateKey = updateKeysResponse.getKey(createKey.getId());
            Assert.assertEquals(createKey.getId(), updateKey.getId());
            Assert.assertEquals(CryptoUtils.binToHex(createKey.getKey()), CryptoUtils.binToHex(updateKey.getKey()));
            Assert.assertEquals(createKey.getAttributesMap(), updateKey.getAttributesMap());
            Assert.assertEquals(
                    createKey.getAttributesSigBase64FromServer(),
                    updateKey.getAttributesSigBase64FromServer());
            Assert.assertNotEquals(createKey.getMutableAttributesMap(), updateKey.getMutableAttributesMap());
            Assert.assertNotEquals(
                    createKey.getMutableAttributesSigBase64FromServer(),
                    updateKey.getMutableAttributesSigBase64FromServer());
            Assert.assertEquals(createKey.getDeviceId(), updateKey.getDeviceId());
            Assert.assertEquals(createKey.getOrigin(), updateKey.getOrigin());
        }
    }
}
