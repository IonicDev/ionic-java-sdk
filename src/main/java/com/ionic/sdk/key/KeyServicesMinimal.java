package com.ionic.sdk.key;

import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysRequest;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysRequest;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysResponse;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;

/**
 * A partial implementation of the {@link KeyServices} interface, with default methods.  Implementations extending
 * this class may do so by providing a minimal number of methods.
 * <ul>
 * <li>{@link KeyServices#getActiveProfile()}</li>
 * <li>{@link KeyServices#createKeys(CreateKeysRequest)}</li>
 * <li>{@link KeyServices#getKeys(GetKeysRequest)}</li>
 * <li>{@link KeyServices#updateKeys(UpdateKeysRequest)}</li>
 * </ul>
 */
public abstract class KeyServicesMinimal implements KeyServices {

    @Override
    public abstract DeviceProfile getActiveProfile();

    @Override
    public final boolean hasActiveProfile() {
        return (getActiveProfile() != null);
    }

    @Override
    public abstract CreateKeysResponse createKeys(CreateKeysRequest request) throws IonicException;

    @Override
    public final CreateKeysResponse createKey(
            final KeyAttributesMap attributes, final KeyAttributesMap mutableAttributes,
            final MetadataMap metadata) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.setMetadata(metadata);
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, attributes, mutableAttributes));
        return createKeys(request);
    }

    @Override
    public final CreateKeysResponse createKey(
            final KeyAttributesMap attributes, final KeyAttributesMap mutableAttributes) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, attributes, mutableAttributes));
        return createKeys(request);
    }

    @Override
    public final CreateKeysResponse createKey(
            final KeyAttributesMap attributes, final MetadataMap metadata) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.setMetadata(metadata);
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, attributes));
        return createKeys(request);
    }

    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, attributes));
        return createKeys(request);
    }

    @Override
    public final CreateKeysResponse createKey(final MetadataMap metadata) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.setMetadata(metadata);
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1));
        return createKeys(request);
    }

    @Override
    public final CreateKeysResponse createKey() throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1));
        return createKeys(request);
    }

    @Override
    public abstract GetKeysResponse getKeys(GetKeysRequest request) throws IonicException;

    @Override
    public final GetKeysResponse getKey(final String keyId, final MetadataMap metadata) throws IonicException {
        final GetKeysRequest request = new GetKeysRequest();
        request.setMetadata(metadata);
        request.add(keyId);
        return getKeys(request);
    }

    @Override
    public final GetKeysResponse getKey(final String keyId) throws IonicException {
        final GetKeysRequest request = new GetKeysRequest();
        request.add(keyId);
        return getKeys(request);
    }

    @Override
    public abstract UpdateKeysResponse updateKeys(UpdateKeysRequest request) throws IonicException;

    @Override
    public final UpdateKeysResponse updateKey(
            final UpdateKeysRequest.Key key, final MetadataMap metadata) throws IonicException {
        final UpdateKeysRequest request = new UpdateKeysRequest();
        request.setMetadata(metadata);
        request.addKey(new UpdateKeysRequest.Key(key));
        return updateKeys(request);
    }
}
