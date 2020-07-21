package com.ionic.sdk.key;

import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysRequest;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysRequest;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysResponse;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;

/**
 * The interface that defines the contract that {@link com.ionic.sdk.agent.Agent} implements, with regard to access to
 * cryptography keys.
 * <p>
 * The subclasses of {@link com.ionic.sdk.agent.request.base.AgentResponseBase} that are returned by KeyServices
 * implementations are constructed with empty fields.  Implementations should populate instances of these fields as
 * appropriate.
 * <p>
 * On operation failures, implementations should throw {@link IonicException} with an appropriate error code.  If the
 * failure is due to an underlying platform exception, {@link IonicException} should wrap that exception.  Unchecked
 * exceptions (like {@link RuntimeException}) should not be intentionally thrown from KeyServices operations.
 */
public interface KeyServices {

    /**
     * Get the active {@link DeviceProfile} of the {@link KeyServices} implementation.  This profile specifies data
     * needed to perform key operations (which might include communication parameters in the case of a remote
     * data store).
     *
     * @return the active {@link DeviceProfile} object
     */
    DeviceProfile getActiveProfile();

    /**
     * Determine if any device profile is active.
     *
     * @return an indication that the operant Secure Enrollment Profile has a device profile marked as active
     */
    boolean hasActiveProfile();

    /**
     * Creates one or more protection keys in the associated data store, and returns those keys to the caller.
     * <p>
     * Each protection key may contain caller-specified **immutable** key attributes, which describe the context
     * of the intended key usage.  Each key may also contain caller-specified **mutable** key attributes, which
     * may be updated after creation via the API {@link #updateKeys(UpdateKeysRequest)}.
     * <p>
     * The {@link CreateKeysRequest} parameter may contain caller-specified metadata, which provides contextual
     * information about the device making the request.
     *
     * @param request the protection key request input data object
     * @return the protection key response output data object
     * @throws IonicException on failure to create the requested protection key(s)
     */
    CreateKeysResponse createKeys(CreateKeysRequest request) throws IonicException;

    /**
     * Creates a single protection key in the associated data store, and returns that key to the caller.
     * <p>
     * The protection key may contain caller-specified **immutable** key attributes, which describe the context
     * of the intended key usage.  The key may also contain caller-specified **mutable** key attributes, which
     * may be updated after creation via the API {@link #updateKeys(UpdateKeysRequest)}.
     * <p>
     * The call may contain caller-specified metadata, which provides contextual
     * information about the device making the request.
     *
     * @param attributes        the **immutable** key attributes to associate with the key
     * @param mutableAttributes the mutable key attributes to associate with the key
     * @param metadata          the metadata properties to associate with the request
     * @return the protection key response output data object
     * @throws IonicException on failure to create the requested protection key
     */
    CreateKeysResponse createKey(KeyAttributesMap attributes, KeyAttributesMap mutableAttributes,
                                 MetadataMap metadata) throws IonicException;

    /**
     * Creates a single protection key in the associated data store, and returns that key to the caller.
     * <p>
     * Each protection key may contain caller-specified **immutable** key attributes, which describe the context
     * of the intended key usage.  Each key may also contain caller-specified **mutable** key attributes, which
     * may be updated after creation via the API {@link #updateKeys(UpdateKeysRequest)}.
     *
     * @param attributes        the **immutable** key attributes to associate with the key
     * @param mutableAttributes the mutable key attributes to associate with the key
     * @return the protection key response output data object
     * @throws IonicException on failure to create the requested protection key
     */
    CreateKeysResponse createKey(
            KeyAttributesMap attributes, KeyAttributesMap mutableAttributes) throws IonicException;

    /**
     * Creates a single protection key in the associated data store, and returns that key to the caller.
     * <p>
     * The protection key may contain caller-specified **immutable** key attributes, which describe the context
     * of the intended key usage.
     * <p>
     * The call may contain caller-specified metadata, which provides contextual
     * information about the device making the request.
     *
     * @param attributes the **immutable** key attributes to associate with the key
     * @param metadata   the metadata properties to associate with the request
     * @return the protection key response output data object
     * @throws IonicException on failure to create the requested protection key
     */
    CreateKeysResponse createKey(KeyAttributesMap attributes, MetadataMap metadata) throws IonicException;

    /**
     * Creates a single protection key in the associated data store, and returns that key to the caller.
     * <p>
     * The protection key may contain caller-specified **immutable** key attributes, which describe the context
     * of the intended key usage.
     *
     * @param attributes the **immutable** key attributes to associate with the key
     * @return the protection key response output data object
     * @throws IonicException on failure to create the requested protection key
     */
    CreateKeysResponse createKey(KeyAttributesMap attributes) throws IonicException;

    /**
     * Creates a single protection key without attributes in the associated data store, and returns that key
     * to the caller.
     * <p>
     * The call may contain caller-specified metadata, which provides contextual
     * information about the device making the request.
     *
     * @param metadata the metadata properties to associate with the request
     * @return the protection key response output data object
     * @throws IonicException on failure to create the requested protection key
     */
    CreateKeysResponse createKey(MetadataMap metadata) throws IonicException;

    /**
     * Creates a single protection key without attributes in the associated data store, and returns that key
     * to the caller.
     *
     * @return the protection key response output data object
     * @throws IonicException on failure to create the requested protection key
     */
    CreateKeysResponse createKey() throws IonicException;

    /**
     * Retrieves a set of protection keys (by key tag, or by external id) from the associated data store.
     * <p>
     * The {@link GetKeysRequest} parameter may contain caller-specified metadata, which provides contextual
     * information about the device making the request.
     *
     * @param request the protection key request input data object
     * @return the protection key response output data object
     * @throws IonicException on failure to retrieve the requested protection key
     */
    GetKeysResponse getKeys(GetKeysRequest request) throws IonicException;

    /**
     * Retrieves a single protection key (by key tag) from the associated data store.
     * <p>
     * The call may contain caller-specified metadata, which provides contextual
     * information about the device making the request.
     * <p>
     * By convention, failures to retrieve a single {@link com.ionic.sdk.agent.key.AgentKey} specified by key tag
     * should result in an {@link IonicException} with the error code
     * {@link com.ionic.sdk.error.SdkError#ISAGENT_KEY_DENIED}.
     *
     * @param keyId    the protection key tag to fetch
     * @param metadata the metadata properties to associate with the request
     * @return the protection key response output data object
     * @throws IonicException on failure to retrieve the requested protection key
     */
    GetKeysResponse getKey(String keyId, MetadataMap metadata) throws IonicException;

    /**
     * Retrieves a single protection key (by key tag) from the associated data store.
     * <p>
     * By convention, failures to retrieve a single {@link com.ionic.sdk.agent.key.AgentKey} specified by key tag
     * should result in an {@link IonicException} with the error code
     * {@link com.ionic.sdk.error.SdkError#ISAGENT_KEY_DENIED}.
     *
     * @param keyId the protection key tag to fetch
     * @return the protection key response output data object
     * @throws IonicException on failure to retrieve the requested protection key
     */
    GetKeysResponse getKey(String keyId) throws IonicException;

    /**
     * Updates a set of protection keys in the associated data store.
     * <p>
     * MutableAttributes will be modified on the server, as specified in the request. Since
     * changes to immutable attributes are prohibited, the value for Attributes (if specified)
     * will be ignored and the Attributes will remain unchanged on the server. The keys returned
     * in the Response object will copy the Attributes of the keys provided in the Request object
     * for convenience, but will not reflect the values on the server if changes were made.
     *
     * @param request the protection key request input data object
     * @return the protection key response output data object
     * @throws IonicException on failure of the remote request
     */
    UpdateKeysResponse updateKeys(UpdateKeysRequest request) throws IonicException;

    /**
     * Updates a single protection key in the associated data store.
     * <p>
     * The call may contain caller-specified metadata, which provides contextual
     * information about the device making the request.
     * <p>
     * MutableAttributes will be modified on the server, as specified in the request. Since
     * changes to immutable attributes are prohibited, the value for Attributes (if specified)
     * will be ignored and the Attributes will remain unchanged on the server. The key returned
     * in the Response object will copy the Attributes of the key provided in the Request object
     * for convenience, but will not reflect the values on the server if changes were made.
     *
     * @param key      key to update
     * @param metadata the metadata properties to associate with the request
     * @return the protection key response output data object
     * @throws IonicException on failure of the remote request
     */
    UpdateKeysResponse updateKey(UpdateKeysRequest.Key key, MetadataMap metadata) throws IonicException;
}
