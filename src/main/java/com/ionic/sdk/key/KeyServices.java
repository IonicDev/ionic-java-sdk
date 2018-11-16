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
 */
public interface KeyServices {

    /**
     * Get the current device profile of the agent.
     *
     * @return the active device profile object
     */
    DeviceProfile getActiveProfile();

    /**
     * Determine if any device profile is active.
     *
     * @return an indication that the source Secure Enrollment Profile has a profile marked as active
     */
    boolean hasActiveProfile();

    /**
     * Creates one or more protection keys through Ionic.com.
     *
     * @param request The protection key request input data object.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    CreateKeysResponse createKeys(CreateKeysRequest request) throws IonicException;

    /**
     * Creates a single protection key with **immutable** key attributes through Ionic.com.
     *
     * @param attributes        The **immutable** protection key attributes to use for creating the protection key.
     * @param mutableAttributes The mutable protection key attributes to use for creating the protection key.
     * @param metadata          The metadata properties to send along with the HTTP request.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    CreateKeysResponse createKey(KeyAttributesMap attributes, KeyAttributesMap mutableAttributes,
                                 MetadataMap metadata) throws IonicException;

    /**
     * Creates a single protection key with **immutable** key attributes through Ionic.com.
     *
     * @param attributes        The **immutable** protection key attributes to use for creating the protection key.
     * @param mutableAttributes The mutable protection key attributes to use for creating the protection key.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    CreateKeysResponse createKey(
            KeyAttributesMap attributes, KeyAttributesMap mutableAttributes) throws IonicException;

    /**
     * Creates a single protection key with **immutable** key attributes through Ionic.com.
     *
     * @param attributes The **immutable** protection key attributes to use for creating the protection key.
     * @param metadata   The metadata properties to send along with the HTTP request.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    CreateKeysResponse createKey(KeyAttributesMap attributes, MetadataMap metadata) throws IonicException;

    /**
     * Creates a single protection key with **immutable** key attributes through Ionic.com.
     *
     * @param attributes The **immutable** protection key attributes to use for creating the protection key.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    CreateKeysResponse createKey(KeyAttributesMap attributes) throws IonicException;

    /**
     * Creates a single protection key without any key attributes through Ionic.com.
     *
     * @param metadata The metadata properties to send along with the HTTP request.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    CreateKeysResponse createKey(MetadataMap metadata) throws IonicException;

    /**
     * Creates a single protection key without any key attributes through Ionic.com.
     *
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    CreateKeysResponse createKey() throws IonicException;

    /**
     * Gets protection keys from Ionic.com.
     *
     * @param request The protection key request input data object.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    GetKeysResponse getKeys(GetKeysRequest request) throws IonicException;

    /**
     * Gets a single protection key from Ionic.com.
     *
     * @param keyId    The protection key ID to fetch.
     * @param metadata The metadata properties to send along with the HTTP request.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    GetKeysResponse getKey(String keyId, MetadataMap metadata) throws IonicException;

    /**
     * Gets a single protection key from Ionic.com.
     *
     * @param keyId The protection key ID to fetch.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    GetKeysResponse getKey(String keyId) throws IonicException;

    /**
     * Updates one or more protection keys through Ionic.com.
     *
     * @param request The protection key request input data object.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    UpdateKeysResponse updateKeys(UpdateKeysRequest request) throws IonicException;

    /**
     * Updates a single protection key from Ionic.com.
     *
     * @param key      key to update
     * @param metadata The metadata properties to send along with the HTTP request.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    UpdateKeysResponse updateKey(UpdateKeysRequest.Key key, MetadataMap metadata) throws IonicException;
}
