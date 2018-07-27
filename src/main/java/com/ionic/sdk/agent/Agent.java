package com.ionic.sdk.agent;

import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.data.MetadataHolder;
import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.agent.hfp.Fingerprint;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceRequest;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceResponse;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceTransaction;
import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.createkey.CreateKeysTransaction;
import com.ionic.sdk.agent.request.getkey.GetKeysRequest;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysTransaction;
import com.ionic.sdk.agent.request.getresources.GetResourcesRequest;
import com.ionic.sdk.agent.request.getresources.GetResourcesResponse;
import com.ionic.sdk.agent.request.getresources.GetResourcesTransaction;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysRequest;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysResponse;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysTransaction;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.device.profile.persistor.DeviceProfilePersistorBase;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.IonicServerException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.util.ArrayList;
import java.util.List;

/**
 * The main point of interaction with the Ionic SDK. This class performs all client/server communications with
 * Ionic.com.
 */
public class Agent extends MetadataHolder implements KeyServices {

    /**
     * Flag indicating initialization state of this object.
     */
    private boolean initialized;

    /**
     * The collection of {@link DeviceProfile} read from the SEP when loaded.
     */
    private List<DeviceProfile> deviceProfiles;

    /**
     * The current device profile of the agent.
     */
    private DeviceProfile activeProfile;

    /**
     * The configuration container object for the agent.
     */
    private AgentConfig agentConfig;

    /**
     * The (externally provided) fingerprint associated with this agent instance.
     */
    private Fingerprint fingerprint;

    /**
     * Default constructor.
     */
    public Agent() {
        initialized = false;
        deviceProfiles = new ArrayList<DeviceProfile>();
        agentConfig = new AgentConfig();
    }

    /**
     * Convenience constructor that allows for instantiation of an initialized Agent in a single step.
     *
     * @param persistor The device profile persistor.
     * @throws IonicException on errors
     */
    public Agent(final DeviceProfilePersistorBase persistor) throws IonicException {
        this();
        initializeInternal(new AgentConfig(), persistor, new MetadataMap(), new Fingerprint(null));
    }

    /**
     * @return the fingerprint associated with this agent instance
     */
    public final Fingerprint getFingerprint() {
        return fingerprint;
    }

    /**
     * Set the fingerprint associated with this agent instance.
     *
     * @param hfp a text representation of a fingerprint to associate with this agent instance
     */
    public final void setFingerprint(final String hfp) {
        this.fingerprint = new Fingerprint(hfp);
    }

    /**
     * Get the current configuration of the agent. Configuration cannot be changed directly through this object.
     * Configuration is done through the initialization functions and cannot be changed after the agent is initialized.
     *
     * @return AgentConfig instance representing this Agent's current configuration.
     */
    public final AgentConfig getConfig() {
        return new AgentConfig(agentConfig);
    }

    /**
     * Determine if any device profiles are loaded.
     *
     * @return True if there is at least one profile associated with this Agent instance; false otherwise.
     */
    public final boolean hasAnyProfiles() {
        return !this.deviceProfiles.isEmpty();
    }

    /**
     * Get the current device profile of the agent.
     *
     * @return Returns the active device profile object. If no active profile is set, then the object is uninitialized
     * and calling isLoaded() on the object will return false. You can also call hasActiveProfile() to determine if
     * there is an active profile.
     */
    @Override
    public final DeviceProfile getActiveProfile() {
        return activeProfile;
    }

    /**
     * Determine if any device profile is active.
     *
     * @return True iff this Agent has an active device profile.
     */
    @Override
    public final boolean hasActiveProfile() {
        return (activeProfile != null);
    }

    /**
     * Set the current device profile of the agent.
     *
     * @param deviceId The device ID of the profile to become active.
     * @return Returns true if the profile was found. Returns false if the profile was not found.
     */
    public final boolean setActiveProfile(final String deviceId) {
        return setActiveProfileInternal(deviceId);
    }

    /**
     * Set the current device profile of the agent.
     *
     * @param deviceId The device ID of the profile to become active.
     * @return Returns true if the profile was found. Returns false if the profile was not found.
     */
    private boolean setActiveProfileInternal(final String deviceId) {
        boolean found = false;
        for (DeviceProfile deviceProfile : this.deviceProfiles) {
            if (deviceProfile.getDeviceId().equals(deviceId)) {
                found = true;
                this.activeProfile = deviceProfile;
            }
        }
        return found;
    }

    /**
     * Set all available device profiles.
     * <p>
     * This method discards all previous profiles (if any) and adds all the provided profiles. The previous active
     * profile (if any) will be retained if it exists in the provided profiles.
     *
     * @param profiles The list of device profiles.
     */
    public final void setAllProfiles(final List<DeviceProfile> profiles) {
        this.deviceProfiles.clear();
        this.deviceProfiles.addAll(profiles);
        final String deviceId = this.activeProfile == null ? null : this.activeProfile.getDeviceId();
        boolean found = setActiveProfileInternal(deviceId);
        if (!found) {
            this.activeProfile = null;
        }
    }

    /**
     * Get all available device profiles.
     *
     * @return Returns a List of all known device profile objects.
     */
    public final List<DeviceProfile> getAllProfiles() {
        return new ArrayList<DeviceProfile>(this.deviceProfiles);
    }

    /**
     * Add a device profile to the agent device profile collection.
     *
     * @param profile    The device profile object.
     * @param makeActive If true, then this profile will be set as the active profile.
     */
    public final void addProfile(final DeviceProfile profile, final boolean makeActive) {
        addProfileInternal(profile, makeActive);
    }

    /**
     * Add a device profile to the agent device profile collection.
     *
     * @param profile The device profile object.
     */
    public final void addProfile(final DeviceProfile profile) {
        addProfileInternal(profile, false);
    }

    /**
     * Add a device profile to the agent device profile collection.
     *
     * @param profile    The device profile object.
     * @param makeActive A flag indicating whether the added profile should become the active profile.
     */
    private void addProfileInternal(final DeviceProfile profile, final boolean makeActive) {
        if (profile != null) {
            this.deviceProfiles.add(profile);
            if (makeActive) {
                setActiveProfileInternal(profile.getDeviceId());
            }
        }
    }

    /**
     * Remove a device profile from the agent device profile collection.
     *
     * @param deviceId The device ID of the profile.
     * @return Returns true if the profile was found. Returns false if the profile was not found.
     */
    public final boolean removeProfile(final String deviceId) {
        DeviceProfile deviceProfileToRemove = null;
        for (final DeviceProfile deviceProfile : deviceProfiles) {
            if (deviceProfile.getDeviceId().equals(deviceId)) {
                deviceProfileToRemove = deviceProfile;
            }
        }
        return deviceProfiles.remove(deviceProfileToRemove);
    }

    /**
     * Load device profiles using the default profile persistor. Attempts to load device profiles using the default
     * profile persistor.
     *
     * @throws IonicException always, as the Java 2.0 SDK does not implement a (platform-specific) default persistor
     */
    public final void loadProfiles() throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NO_DEVICE_PROFILE);
    }

    /**
     * Load device profiles using the specified profile persistor. Attempts to load device profiles using the specified
     * profile persistor. If loading is successful, all existing profiles associated with the agent will be discarded
     * in favor of the newly loaded profiles. The active profile provided by the persistor will replace the previous
     * active profile (if any). It is also possible that the persistor does not provide an active profile, in which
     * case the agent object will be left without an active profile. If loading is not successful, then no changes are
     * made to the state of the agent.
     *
     * @param persistor The device profile persistor.
     * @throws IonicException on errors
     */
    public final void loadProfiles(final DeviceProfilePersistorBase persistor) throws IonicException {
        loadProfilesInternal(persistor);
    }

    /**
     * Load device profiles using the specified profile persistor. Attempts to load device profiles using the specified
     * profile persistor. If loading is successful, all existing profiles associated with the agent will be discarded
     * in favor of the newly loaded profiles. The active profile provided by the persistor will replace the previous
     * active profile (if any). It is also possible that the persistor does not provide an active profile, in which
     * case the agent object will be left without an active profile. If loading is not successful, then no changes are
     * made to the state of the agent.
     *
     * @param persistor The device profile persistor.
     * @throws IonicException on errors
     */
    private void loadProfilesInternal(final DeviceProfilePersistorBase persistor) throws IonicException {
        String[] activeProfileParam = new String[1];
        List<DeviceProfile> deviceProfilesInit = new ArrayList<DeviceProfile>();
        try {
            deviceProfilesInit = (persistor == null)
                    ? new ArrayList<DeviceProfile>()
                    : persistor.loadAllProfiles(activeProfileParam);
        } catch (IonicException e) {
            // load to local stack data.  if loading failed due to resource not found
            // (i.e. the storage file or other resource does not exist) then we do not
            // consider this an error. per the c++ sdk (ISAgent.cpp#617)
            if (e.getReturnCode() != SdkError.ISAGENT_RESOURCE_NOT_FOUND) {
                throw e;
            }
        }
        final String deviceId = activeProfileParam[0];
        this.deviceProfiles = deviceProfilesInit;
        setActiveProfileInternal(deviceId);
    }

    /**
     * Save device profiles using the default profile persistor. Attempts to save device profiles using the default
     * profile persistor.
     *
     * @throws IonicException always, as the Java 2.0 SDK does not implement a (platform-specific) default persistor
     */
    public final void saveProfiles() throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NO_DEVICE_PROFILE);
    }

    /**
     * Save device profiles using the specified profile persistor. Attempts to save device profiles using the specified
     * profile persistor.
     *
     * @param persistor The device profile persistor.
     * @throws IonicException on errors
     */
    public final void saveProfiles(final DeviceProfilePersistorBase persistor) throws IonicException {
        final String activeDeviceId = (this.activeProfile == null) ? null : this.activeProfile.getDeviceId();
        persistor.saveAllProfiles(this.deviceProfiles, activeDeviceId);
    }

    /**
     * Determine which device profile is associated with the provided key ID.
     *
     * @param keyId The key ID for which to retrieve the associated device profile.
     * @return Returns the device profile associated with the provided key ID.
     * If no device profile is found for the key, then null is returned.
     */
    public final DeviceProfile getDeviceProfileForKeyId(final String keyId) {
        final int sizeofTenantId = 4;
        DeviceProfile deviceProfileFound = null;
        for (DeviceProfile deviceProfileIt : this.deviceProfiles) {
            if (deviceProfileIt.getDeviceId().startsWith(keyId.substring(0, sizeofTenantId))) {
                deviceProfileFound = deviceProfileIt;
                break;
            }
        }
        return deviceProfileFound;
    }

    /**
     * Creates a new device record for use with subsequent requests to Ionic.com.
     *
     * @param request The request input data object.
     * @return The response output data object, containing the created device profile.
     * @throws IonicException if an error occurs
     */
    public final CreateDeviceResponse createDevice(
            final CreateDeviceRequest request) throws IonicException {
        final CreateDeviceResponse response = new CreateDeviceResponse();
        final CreateDeviceTransaction transaction = new CreateDeviceTransaction(this, request, response);
        transaction.run();
        addProfileInternal(response.getDeviceProfile(), true);
        return response;
    }

    /**
     * Creates a new device record for use with subsequent requests to Ionic.com.
     *
     * @param request          The request input data object.
     * @param makeDeviceActive Assign the newly created device as the active device.
     * @return The response output data object, containing the created device profile.
     * @throws IonicException if an error occurs
     */
    public final CreateDeviceResponse createDevice(
            final CreateDeviceRequest request, final boolean makeDeviceActive) throws IonicException {
        final CreateDeviceResponse response = new CreateDeviceResponse();
        final CreateDeviceTransaction transaction = new CreateDeviceTransaction(this, request, response);
        transaction.run();
        addProfileInternal(response.getDeviceProfile(), makeDeviceActive);
        return response;
    }

    /**
     * Creates one or more protection keys through Ionic.com.
     *
     * @param request The protection key request input data object.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final CreateKeysResponse createKeys(final CreateKeysRequest request) throws IonicException {
        final CreateKeysResponse response = new CreateKeysResponse();
        final CreateKeysTransaction transaction = new CreateKeysTransaction(this, request, response);
        transaction.run();
        return response;
    }

    /**
     * Creates a single protection key with **immutable** and mutable key attributes through Ionic.com.
     *
     * @param attributes        The **immutable** protection key attributes to use for creating the protection key.
     * @param mutableAttributes The mutable protection key attributes to use for creating the protection key.
     * @param metadata          The metadata properties to send along with the HTTP request.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                              final KeyAttributesMap mutableAttributes,
                                              final MetadataMap metadata) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.setMetadata(metadata);
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, attributes, mutableAttributes));
        return createKeyInternal(request);
    }

    /**
     * Creates a single protection key with **immutable** and mutable key attributes through Ionic.com.
     *
     * @param attributes        The **immutable** protection key attributes to use for creating the protection key.
     * @param mutableAttributes The mutable protection key attributes to use for creating the protection key.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                              final KeyAttributesMap mutableAttributes) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, attributes, mutableAttributes));
        return createKeyInternal(request);
    }

    /**
     * Creates a single protection key with **immutable** key attributes through Ionic.com.
     *
     * @param attributes The **immutable** protection key attributes to use for creating the protection key.
     * @param metadata   The metadata properties to send along with the HTTP request.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                              final MetadataMap metadata) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.setMetadata(metadata);
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, attributes, new KeyAttributesMap()));
        return createKeyInternal(request);
    }

    /**
     * Creates a single protection key with **immutable** key attributes through Ionic.com.
     *
     * @param attributes The **immutable** protection key attributes to use for creating the protection key.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, attributes, new KeyAttributesMap()));
        return createKeyInternal(request);
    }

    /**
     * Creates a single protection key without key attributes through Ionic.com.
     *
     * @param metadata The metadata properties to send along with the HTTP request.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final CreateKeysResponse createKey(final MetadataMap metadata) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.setMetadata(metadata);
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, new KeyAttributesMap(), new KeyAttributesMap()));
        return createKeyInternal(request);
    }

    /**
     * Creates a single protection key without key attributes through Ionic.com.
     *
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final CreateKeysResponse createKey() throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, new KeyAttributesMap(), new KeyAttributesMap()));
        return createKeyInternal(request);
    }

    /**
     * Creates one or more protection keys through Ionic.com.
     *
     * @param request The protection key request input data object.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    private CreateKeysResponse createKeyInternal(final CreateKeysRequest request) throws IonicException {
        final CreateKeysResponse response = new CreateKeysResponse();
        final CreateKeysTransaction transaction = new CreateKeysTransaction(this, request, response);
        transaction.run();
        // response validation (reference implementation symmetry)
        if (response.getKeys().isEmpty()) {
            throw new IonicException(SdkError.ISAGENT_KEY_DENIED,
                    new IonicServerException(SdkError.ISAGENT_MISSINGVALUE, response.getConversationId(), response));
        }
        return response;
    }

    /**
     * Gets protection keys from Ionic.com.
     *
     * @param request The protection key request input data object.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final GetKeysResponse getKeys(final GetKeysRequest request) throws IonicException {
        return getKeysInternal(request);
    }

    /**
     * Gets protection keys from Ionic.com.
     *
     * @param request The protection key request input data object.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    private GetKeysResponse getKeysInternal(final GetKeysRequest request) throws IonicException {
        final GetKeysResponse response = new GetKeysResponse();
        final GetKeysTransaction transaction = new GetKeysTransaction(this, request, response);
        transaction.run();
        return response;
    }

    /**
     * Gets a single protection key from Ionic.com.
     *
     * @param keyId    The protection key ID to fetch.
     * @param metadata The metadata properties to send along with the HTTP request.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final GetKeysResponse getKey(final String keyId, final MetadataMap metadata) throws IonicException {
        final GetKeysRequest request = new GetKeysRequest();
        request.setMetadata(metadata);
        request.add(keyId);
        return getKeysInternal(request);
    }

    /**
     * Gets a single protection key from Ionic.com.
     *
     * @param keyId The protection key ID to fetch.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final GetKeysResponse getKey(final String keyId) throws IonicException {
        final GetKeysRequest request = new GetKeysRequest();
        request.add(keyId);
        return getKeysInternal(request);
    }

    /**
     * Updates one or more protection keys through Ionic.com.
     *
     * @param request The protection key request input data object.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    private UpdateKeysResponse updateKeysInternal(final UpdateKeysRequest request) throws IonicException {
        final UpdateKeysResponse response = new UpdateKeysResponse();
        final UpdateKeysTransaction transaction = new UpdateKeysTransaction(this, request, response);
        transaction.run();
        return response;
    }

    /**
     * Updates one or more protection keys through Ionic.com.
     *
     * @param request The protection key request input data object.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final UpdateKeysResponse updateKeys(final UpdateKeysRequest request) throws IonicException {
        return updateKeysInternal(request);
    }

    /**
     * Updates a single protection key from Ionic.com.
     *
     * @param key      key to update
     * @param metadata The metadata properties to send along with the HTTP request.
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    @Override
    public final UpdateKeysResponse updateKey(
            final UpdateKeysRequest.Key key, final MetadataMap metadata) throws IonicException {
        final UpdateKeysRequest request = new UpdateKeysRequest();
        request.setMetadata(metadata);
        request.addKey(new UpdateKeysRequest.Key(key));
        return updateKeysInternal(request);
    }

    /**
     * Updates a single protection key from Ionic.com.
     *
     * @param key key to update
     * @return The protection key response output data object.
     * @throws IonicException if an error occurs
     */
    public final UpdateKeysResponse updateKey(final UpdateKeysRequest.Key key) throws IonicException {
        final UpdateKeysRequest request = new UpdateKeysRequest();
        request.addKey(new UpdateKeysRequest.Key(key));
        return updateKeysInternal(request);
    }

    /**
     * Gets a single generic resource from Ionic.com. This method makes an HTTP call to Ionic.com to request a generic
     * resource.
     *
     * @param resource The generic request resource input object.
     * @return The generic resource response output data object. It is important to note that even if the function
     * succeeds, it does NOT mean that the requested resource was provided. The caller can look at the first object in
     * the output response (guaranteed to exist) and check the error field (GetResourcesResponse.Resource::getError())
     * to see if an error occurred.  If the error field is empty, then the data field
     * (GetResourcesResponse.Resource::getData()) should be expected to contain the resource.
     * @throws IonicException if an error occurs
     */
    public final GetResourcesResponse getResource(final GetResourcesRequest.Resource resource) throws IonicException {
        final GetResourcesRequest request = new GetResourcesRequest();
        request.setMetadata(getMetadata());
        request.add(resource);
        return getResourcesInternal(request);
    }

    /**
     * Gets one or more generic resource(s) from Ionic.com. This method makes an HTTP call to Ionic.com to request
     * generic resource(s).
     *
     * @param request The generic resource batch request input object.
     * @return The generic resource response output data object. It is important to note that even if the function
     * succeeds, it does NOT mean that any or all of the requested resources were provided. The caller can iterate
     * through the objects in the output response to see which ones were successful.  The caller should check the error
     * field (GetResourcesResponse.Resource::getError()) of each output resource object to see if an error occurred.
     * If the error field is empty, then the data field (GetResourcesResponse.Resource::getData()) should be expected
     * to contain the resource.
     * @throws IonicException if an error occurs
     */
    public final GetResourcesResponse getResources(final GetResourcesRequest request) throws IonicException {
        return getResourcesInternal(request);
    }

    /**
     * Gets one or more generic resource(s) from Ionic.com. This method makes an HTTP call to Ionic.com to request
     * generic resource(s).
     *
     * @param request The generic resource batch request input object.
     * @return The generic resource response output data object.
     * @throws IonicException if an error occurs
     */
    private GetResourcesResponse getResourcesInternal(final GetResourcesRequest request) throws IonicException {
        final GetResourcesResponse response = new GetResourcesResponse();
        final GetResourcesTransaction transaction = new GetResourcesTransaction(this, request, response);
        transaction.run();
        return response;
    }

    /**
     * Initialize the agent with default configuration and default profile loader.
     *
     * @throws IonicException always, as the Java 2.0 SDK does not implement a (platform-specific) default persistor
     */
    public final void initialize() throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NO_DEVICE_PROFILE);
    }

    /**
     * Initialize the agent with a configuration object and default profile loader.
     *
     * @param agentConfig Configuration object.
     * @throws IonicException always, as the Java 2.0 SDK does not implement a (platform-specific) default persistor
     */
    public final void initialize(final AgentConfig agentConfig) throws IonicException {
        throw new IonicException(SdkError.ISAGENT_NO_DEVICE_PROFILE);
    }

    /**
     * Initialize the agent with default configuration and specified profile loader. The profile loader is used to
     * load device profiles during initialization.
     *
     * @param persistor Device profile loader object.
     * @throws IonicException if an error occurs
     */
    public final void initialize(final DeviceProfilePersistorBase persistor) throws IonicException {
        initializeInternal(new AgentConfig(), persistor, new MetadataMap(), new Fingerprint(null));
    }

    /**
     * Initialize the agent with a configuration object and specified profile loader. The profile loader is used to
     * load device profiles during initialization.
     *
     * @param agentConfig Configuration object.
     * @param persistor   Device profile loader object.
     * @throws IonicException if an error occurs
     */
    public final void initialize(final AgentConfig agentConfig, final DeviceProfilePersistorBase persistor)
            throws IonicException {
        initializeInternal(agentConfig, persistor, new MetadataMap(), new Fingerprint(null));
    }

    /**
     * Initialize the agent with default configuration.  This API may be used when no profile loader is available.
     *
     * @throws IonicException if an error occurs
     */
    public final void initializeWithoutProfiles() throws IonicException {
        initializeWithoutProfilesInternal(new AgentConfig());
    }

    /**
     * Initialize the agent with a configuration object.  This API may be used when no profile loader is available.
     *
     * @param agentConfig Configuration object.
     * @throws IonicException if an error occurs
     */
    public final void initializeWithoutProfiles(final AgentConfig agentConfig) throws IonicException {
        initializeWithoutProfilesInternal(agentConfig);
    }

    /**
     * Initialize the agent with a configuration object.  This API may be used when no profile loader is available.
     *
     * @param agentConfig Configuration object.
     * @throws IonicException if an error occurs
     */
    private void initializeWithoutProfilesInternal(final AgentConfig agentConfig) throws IonicException {
        initializeInternal(agentConfig, null, new MetadataMap(), new Fingerprint(null));
    }

    /**
     * Initialize the agent with a configuration object and specified profile loader. The profile loader is used to
     * load device profiles during initialization.
     *
     * @param agentConfig Configuration object.
     * @param persistor   Device profile loader object.
     * @param metadata    Device server metadata object.
     * @param fingerprint Device fingerprint object.
     * @throws IonicException if an error occurs
     */
    private void initializeInternal(final AgentConfig agentConfig, final DeviceProfilePersistorBase persistor,
                                    final MetadataMap metadata, final Fingerprint fingerprint) throws IonicException {
        if (initialized) {
            throw new IonicException(SdkError.ISAGENT_DOUBLEINIT);
        }
        AgentSdk.initialize();
        loadProfilesInternal(persistor);
        this.initialized = true;
        this.agentConfig = agentConfig;
        setMetadata(metadata);
        setMetadata(IDC.Metadata.IONIC_AGENT, SdkVersion.getAgentString());
        this.fingerprint = fingerprint;
    }

    /**
     * Determine if the agent is initialized and ready for use.
     *
     * @return True if the agent is initialized; false otherwise.
     */
    public final boolean isInitialized() {
        return initialized;
    }

    /**
     * This string constant represents the key origin ID for keys that originate
     * from an Ionic key server. Outside of advanced use cases by an SDK consumer, this is
     * the only key origin string that will ever be used.
     */
    public static final String KEYORIGIN_IONIC_KEYSERVER = IDC.Metadata.KEYORIGIN_IONIC;
}
