package com.ionic.sdk.ks.service;

import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysRequest;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysRequest;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysResponse;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Non-network implementation of {@link KeyServices}, for use with test suite.
 * <p>
 * Any key request returns a fixed 256-bit zero-fill key.
 */
public final class TestKeyServices implements KeyServices {

    /**
     * Stub {@link DeviceProfile} record for this {@link KeyServices} implementation.
     */
    private final DeviceProfile deviceProfile;

    /**
     * Regular expression describing a valid key id for this {@link KeyServices} implementation.
     */
    private final Pattern patternKeyId;

    /**
     * Internal ordinal used to track the number of keys issued by this {@link KeyServices} implementation.
     */
    private int counter;

    /**
     * Constructor.
     *
     * @param keyspace the four-character keyspace for the {@link KeyServices} instance
     */
    public TestKeyServices(final String keyspace) {
        final String deviceId = String.format("%s.A.%s", keyspace, UUID.randomUUID().toString());
        this.deviceProfile = new DeviceProfile("", 0L, deviceId, "https://localhost", TEST_KEY, TEST_KEY);
        this.patternKeyId = Pattern.compile(keyspace + "\\d{7}");
        this.counter = 0;
    }

    @Override
    public DeviceProfile getActiveProfile() {
        return deviceProfile;
    }

    @Override
    public boolean hasActiveProfile() {
        return true;
    }

    @Override
    public CreateKeysResponse createKeys(final CreateKeysRequest request) {
        final CreateKeysResponse createKeysResponse = new CreateKeysResponse();
        for (CreateKeysRequest.Key item : request.getKeys()) {
            for (int i = 0; (i < item.getQuantity()); ++i) {
                createKeysResponse.add(createKeyInternal(item.getRefId()));
            }
        }
        return createKeysResponse;
    }

    @Override
    public CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                        final KeyAttributesMap mutableAttributes,
                                        final MetadataMap metadata) {
        return new CreateKeysResponse(createKeyInternal(null));
    }

    @Override
    public CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                        final KeyAttributesMap mutableAttributes) {
        return new CreateKeysResponse(createKeyInternal(null));
    }

    @Override
    public CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                        final MetadataMap metadata) {
        return new CreateKeysResponse(createKeyInternal(null));
    }

    @Override
    public CreateKeysResponse createKey(final KeyAttributesMap attributes) {
        return new CreateKeysResponse(createKeyInternal(null));
    }

    @Override
    public CreateKeysResponse createKey(final MetadataMap metadata) {
        return new CreateKeysResponse(createKeyInternal(null));
    }

    @Override
    public CreateKeysResponse createKey() {
        return new CreateKeysResponse(createKeyInternal(null));
    }

    /**
     * Assemble a key, given the input reference id.
     *
     * @param refId the reference identifier for the request
     * @return a valid {@link CreateKeysResponse.Key} object
     */
    private CreateKeysResponse.Key createKeyInternal(final String refId) {
        final String keyId = String.format("%s%07d", deviceProfile.getKeySpace(), ++counter);
        return new CreateKeysResponse.Key(refId, keyId, TEST_KEY, "");
    }

    @Override
    public GetKeysResponse getKeys(final GetKeysRequest request) {
        final GetKeysResponse getKeysResponse = new GetKeysResponse();
        for (String keyId : request.getKeyIds()) {
            if (patternKeyId.matcher(keyId).matches()) {
                getKeysResponse.add(getKeyInternal(keyId));
            }
        }
        return getKeysResponse;
    }

    @Override
    public GetKeysResponse getKey(final String keyId, final MetadataMap metadata) throws IonicException {
        SdkData.checkTrue(keyId != null, SdkError.ISAGENT_NULL_INPUT);
        SdkData.checkTrue(patternKeyId.matcher(keyId).matches(), SdkError.ISAGENT_KEY_DENIED);
        return new GetKeysResponse(getKeyInternal(keyId));
    }

    @Override
    public GetKeysResponse getKey(final String keyId) throws IonicException {
        SdkData.checkTrue(keyId != null, SdkError.ISAGENT_NULL_INPUT);
        SdkData.checkTrue(patternKeyId.matcher(keyId).matches(), SdkError.ISAGENT_KEY_DENIED);
        return new GetKeysResponse(getKeyInternal(keyId));
    }

    /**
     * Assemble a key, given the input key id.
     *
     * @param keyId the identifier for the requested key
     * @return a valid {@link GetKeysResponse.Key} object
     */
    private GetKeysResponse.Key getKeyInternal(final String keyId) {
        return new GetKeysResponse.Key(keyId, TEST_KEY, "");
    }

    @Override
    public UpdateKeysResponse updateKeys(final UpdateKeysRequest request) throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }

    @Override
    public UpdateKeysResponse updateKey(final UpdateKeysRequest.Key key,
                                        final MetadataMap metadata) throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }

    /**
     * Static zero-fill test AES key for use by this {@link KeyServices} implementation.
     */
    private static final byte[] TEST_KEY = Transcoder.hex().decode(Value.generate("00", AesCipher.KEY_BYTES));
}
