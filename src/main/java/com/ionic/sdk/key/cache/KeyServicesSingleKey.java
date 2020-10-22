package com.ionic.sdk.key.cache;

import com.ionic.sdk.agent.data.MetadataHolder;
import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysRequest;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysRequest;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysResponse;
import com.ionic.sdk.core.annotation.Experimental;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

/**
 * Implementation of {@link KeyServices} intended for use in situations where deterministic encryption is
 * desired.  This manner of encryption provides for key reuse and a deterministic IV generation algorithm.  The
 * goal is to always output the same ciphertext, given the same plaintext input.
 * <p>
 * Intended for use within applications where deterministic behavior is desirable.  For example, in database
 * applications, this behavior facilitates searches of encrypted content.
 * <p>
 * Sample:
 * <pre>
 * public void testChunkCipherAuto_DeterministicIv() throws IonicException {
 *     // create Machina-backed KeyServices providing access to a single key
 *     final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
 *     final CreateKeysRequest createKeysRequest = new CreateKeysRequest(new CreateKeysRequest.Key());
 *     final KeyServicesSingleKey keyServicesWrapper = new KeyServicesSingleKey(keyServices, createKeysRequest);
 *     // configure chunk cipher to use deterministic IV generation
 *     final String plainText = "Hello Machina!";
 *     final ChunkCryptoEncryptAttributes attributes = new ChunkCryptoEncryptAttributes();
 *     attributes.setMetadata("ionic-iv-algorithm", "HmacSHA256");
 *     // verify functionality
 *     final ChunkCipherAbstract chunkCipher1 = new ChunkCipherAuto(keyServicesWrapper);
 *     final String cipherText1 = chunkCipher1.encrypt(plainText, new ChunkCryptoEncryptAttributes(attributes));
 *     final ChunkCipherAbstract chunkCipher2 = new ChunkCipherAuto(keyServicesWrapper);
 *     final String cipherText2 = chunkCipher2.encrypt(plainText, new ChunkCryptoEncryptAttributes(attributes));
 *     Assert.assertEquals(cipherText1, cipherText2);
 * }
 * </pre>
 * <p>
 * Most of the interface APIs are purposefully left unimplemented.  This implementation is intended for use only
 * within the confines of the experimental deterministic encryption implementation.
 * <p>
 * This {@link KeyServices} implementation is _not_ intended for broad usage.
 */
@Experimental
public class KeyServicesSingleKey extends MetadataHolder implements KeyServices {

    /**
     * The Machina key to serve for any implemented KeyServices request.
     */
    private final AgentKey key;

    /**
     * Constructor.
     *
     * @param keyServices the source of the cached key to be used for future key operations
     * @param request     the request to be submitted to the source {@link KeyServices}
     * @throws IonicException on key request failure
     */
    public KeyServicesSingleKey(final KeyServices keyServices, final CreateKeysRequest request) throws IonicException {
        final CreateKeysResponse response = keyServices.createKeys(request);
        this.key = response.getFirstKey();
    }

    /**
     * Constructor.
     *
     * @param keyServices the source of the cached key to be used for future key operations
     * @param request     the request to be submitted to the source {@link KeyServices}
     * @throws IonicException on key request failure
     */
    public KeyServicesSingleKey(final KeyServices keyServices, final GetKeysRequest request) throws IonicException {
        final GetKeysResponse response = keyServices.getKeys(request);
        this.key = response.getFirstKey();
    }

    @Override
    public final DeviceProfile getActiveProfile() {
        return null;
    }

    @Override
    public final boolean hasActiveProfile() {
        return false;
    }

    @Override
    public final CreateKeysResponse createKeys(final CreateKeysRequest request) {
        return new CreateKeysResponse(new CreateKeysResponse.Key(key));
    }

    @Override
    public final CreateKeysResponse createKey(
            final KeyAttributesMap attributes, final KeyAttributesMap mutableAttributes,
            final MetadataMap metadata) {
        return new CreateKeysResponse(new CreateKeysResponse.Key(key));
    }

    @Override
    public final CreateKeysResponse createKey(
            final KeyAttributesMap attributes, final KeyAttributesMap mutableAttributes) throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }

    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                              final MetadataMap metadata) throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }

    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes) throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }

    @Override
    public final CreateKeysResponse createKey(final MetadataMap metadata) throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }

    @Override
    public final CreateKeysResponse createKey() throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }

    @Override
    public final GetKeysResponse getKeys(final GetKeysRequest request) {
        return new GetKeysResponse(new GetKeysResponse.Key(key));
    }

    @Override
    public final GetKeysResponse getKey(final String keyId, final MetadataMap metadata) {
        return new GetKeysResponse(new GetKeysResponse.Key(key));
    }

    @Override
    public final GetKeysResponse getKey(final String keyId) throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }

    @Override
    public final UpdateKeysResponse updateKeys(final UpdateKeysRequest request) throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }

    @Override
    public final UpdateKeysResponse updateKey(final UpdateKeysRequest.Key key,
                                              final MetadataMap metadata) throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
    }
}
