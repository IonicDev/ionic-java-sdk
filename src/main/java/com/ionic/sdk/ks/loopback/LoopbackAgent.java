package com.ionic.sdk.ks.loopback;

import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysRequest;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysRequest;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysResponse;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.date.DateTime;
import com.ionic.sdk.core.rng.CryptoRng;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;
import com.ionic.sdk.keyvault.KeyVaultKey;
import com.ionic.sdk.keyvault.impl.KeyVaultPassword;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * The "loopback agent" implements {@link KeyServices}, with a
 * {@link com.ionic.sdk.keyvault.KeyVaultBase} as the backing persistent store of key data.  The class
 * {@link CryptoRng} is used to generate keys.
 */
public final class LoopbackAgent implements KeyServices {

    /**
     * The source of randomness needed when generating new AES keys.
     */
    private final CryptoRng cryptoRng;

    /**
     * This virtual agent has a single implicit {@link DeviceProfile}, that is always active.
     */
    private final DeviceProfile activeProfile;

    /**
     * The backing location of the persisted data associated with this tenant.
     */
    private final KeyVaultPassword keyVault;

    /**
     * Constructor.
     *
     * @param file     the persistent store for the keys associated with the loopback agent instance
     * @param password the password used to protect the serialized byte stream containing key objects
     * @throws IonicException on failure to initialize the backing {@link com.ionic.sdk.keyvault.KeyVaultBase}
     */
    public LoopbackAgent(final File file, final String password) throws IonicException {
        cryptoRng = new CryptoRng();
        final long creationTimestamp = (System.currentTimeMillis() / DateTime.ONE_SECOND_MILLIS);
        final String uuid = UUID.randomUUID().toString();
        final String deviceId = Value.join(Value.DOT, Tenant.KEYSPACE, uuid.substring(0, 1), uuid);
        final byte[] bytesKey = new byte[AesCipher.KEY_BYTES];
        activeProfile = new DeviceProfile("", creationTimestamp, deviceId, Tenant.SERVER, bytesKey, bytesKey);
        keyVault = new KeyVaultPassword(file.getPath());
        keyVault.setPassword(password);
        keyVault.sync();
    }

    @Override
    public DeviceProfile getActiveProfile() {
        return activeProfile;
    }

    @Override
    public boolean hasActiveProfile() {
        return true;
    }

    @Override
    public CreateKeysResponse createKeys(final CreateKeysRequest createKeysRequest) throws IonicException {
        final CreateKeysResponse createKeysResponse = new CreateKeysResponse();
        final List<CreateKeysRequest.Key> keys = createKeysRequest.getKeys();
        for (CreateKeysRequest.Key requestKey : keys) {
            createKeyIntoResponse(requestKey, createKeysResponse);
        }
        return createKeysResponse;
    }

    @Override
    public CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                        final KeyAttributesMap mutableAttributes,
                                        final MetadataMap metadata) throws IonicException {
        final CreateKeysResponse createKeysResponse = new CreateKeysResponse();
        createKeyIntoResponse(new CreateKeysRequest.Key(IDC.Payload.REF), createKeysResponse);
        return createKeysResponse;
    }

    @Override
    public CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                        final KeyAttributesMap mutableAttributes) throws IonicException {
        final CreateKeysResponse createKeysResponse = new CreateKeysResponse();
        createKeyIntoResponse(new CreateKeysRequest.Key(IDC.Payload.REF), createKeysResponse);
        return createKeysResponse;
    }

    @Override
    public CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                        final MetadataMap metadata) throws IonicException {
        final CreateKeysResponse createKeysResponse = new CreateKeysResponse();
        createKeyIntoResponse(new CreateKeysRequest.Key(IDC.Payload.REF), createKeysResponse);
        return createKeysResponse;
    }

    @Override
    public CreateKeysResponse createKey(final KeyAttributesMap attributes) throws IonicException {
        final CreateKeysResponse createKeysResponse = new CreateKeysResponse();
        createKeyIntoResponse(new CreateKeysRequest.Key(IDC.Payload.REF), createKeysResponse);
        return createKeysResponse;
    }

    @Override
    public CreateKeysResponse createKey(final MetadataMap metadata) throws IonicException {
        final CreateKeysResponse createKeysResponse = new CreateKeysResponse();
        createKeyIntoResponse(new CreateKeysRequest.Key(IDC.Payload.REF), createKeysResponse);
        return createKeysResponse;
    }

    @Override
    public CreateKeysResponse createKey() throws IonicException {
        final CreateKeysResponse createKeysResponse = new CreateKeysResponse();
        createKeyIntoResponse(new CreateKeysRequest.Key(IDC.Payload.REF), createKeysResponse);
        return createKeysResponse;
    }

    @Override
    public GetKeysResponse getKeys(final GetKeysRequest getKeysRequest) {
        final GetKeysResponse getKeysResponse = new GetKeysResponse();
        final List<String> keyIds = getKeysRequest.getKeyIds();
        for (String keyId : keyIds) {
            getKeyIntoResponse(keyId, getKeysResponse);
        }
        return getKeysResponse;
    }

    @Override
    public GetKeysResponse getKey(final String keyId, final MetadataMap metadata) {
        final GetKeysResponse getKeysResponse = new GetKeysResponse();
        getKeyIntoResponse(keyId, getKeysResponse);
        return getKeysResponse;
    }

    @Override
    public GetKeysResponse getKey(final String keyId) {
        final GetKeysResponse getKeysResponse = new GetKeysResponse();
        getKeyIntoResponse(keyId, getKeysResponse);
        return getKeysResponse;
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
     * Generate a new AES key.  This key is added to the {@link com.ionic.sdk.keyvault.KeyVaultBase}, so it is
     * accessible in future {@link KeyServices} calls.
     *
     * @param requestKey         the request record, detailing desired attributes of keys to be created
     * @param createKeysResponse the response into which the key record(s) should be inserted
     * @throws IonicException on failure to access the VM entropy, or to persist the newly created key data
     */
    private void createKeyIntoResponse(final CreateKeysRequest.Key requestKey,
                                       final CreateKeysResponse createKeysResponse) throws IonicException {
        final int quantity = requestKey.getQuantity();
        for (int i = 0; (i < quantity); ++i) {
            final byte[] randomId = cryptoRng.rand(new byte[Tenant.LENGTH_KEY_ID]);
            // The "URL and Filename safe" Base 64 Alphabet, https://tools.ietf.org/html/rfc4648
            final String keyId = Tenant.KEYSPACE + Transcoder.base64().encode(randomId).
                    replace("+", "-").replace("/", "_").substring(0, Tenant.LENGTH_KEY_ID);  // magic_string_ok
            final byte[] keyBytes = cryptoRng.rand(new byte[AesCipher.KEY_BYTES]);
            final CreateKeysResponse.Key key = new CreateKeysResponse.Key(
                    requestKey.getRefId(), keyId, keyBytes, activeProfile.getDeviceId());
            final long issuedTime = (System.currentTimeMillis() / DateTime.ONE_SECOND_MILLIS);
            keyVault.setKey(new KeyVaultKey(key, issuedTime, issuedTime + Tenant.INTERVAL_EXPIRE));
            createKeysResponse.add(key);
        }
        keyVault.sync();
    }

    /**
     * Fetch a previously generated Ionic key record.
     *
     * @param keyId           the id associated with the desired key record
     * @param getKeysResponse the response into which the key record should be inserted
     */
    private void getKeyIntoResponse(final String keyId, final GetKeysResponse getKeysResponse) {
        final KeyVaultKey vaultKey = keyVault.getKey(keyId);
        if (vaultKey != null) {
            getKeysResponse.add(new GetKeysResponse.Key(
                    vaultKey.getKeyId(), vaultKey.getKeyBytes(), activeProfile.getDeviceId()));

        }
    }

    /**
     * Declarations used by this virtual tenant.
     */
    private static class Tenant {

        /**
         * The server URL hard-coded into this virtual tenant.  No HTTPS activity is occurring; this URL is only needed
         * to satisfy expectations that a {@link DeviceProfile} server URL is a valid URL.
         */
        private static final String SERVER = "https://localhost";

        /**
         * The tenant ID associated with the virtual tenant backing this {@link KeyServices} implementation.
         */
        private static final String KEYSPACE = "LPBK";

        /**
         * The number of bytes after the keyspace (4 characters) that contain the ID portion of an Ionic key ID.
         * A modified base64 alphabet is used to encode these characters.  As each byte contains 6 significant bits,
         * the available keyspace allows for 2^42 (~ 4 trillion) unique IDs.
         */
        private static final int LENGTH_KEY_ID = 7;

        /**
         * The amount of time (in seconds) that a newly created key should be retained by this {@link KeyServices}
         * implementation (P1Y).
         */
        private static final long INTERVAL_EXPIRE = (60 * 60 * 24 * 365);
    }
}
