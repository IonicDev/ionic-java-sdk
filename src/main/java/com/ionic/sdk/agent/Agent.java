package com.ionic.sdk.agent;

import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.data.MetadataHolder;
import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.agent.hfp.Fingerprint;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createassertion.CreateIdentityAssertionRequest;
import com.ionic.sdk.agent.request.createassertion.CreateIdentityAssertionResponse;
import com.ionic.sdk.agent.request.createassertion.CreateIdentityAssertionTransaction;
import com.ionic.sdk.agent.request.createassertion.IdentityAssertionValidator;
import com.ionic.sdk.agent.request.createassertion.data.AssertionUtils;
import com.ionic.sdk.agent.request.createassertion.data.IdentityAssertion;
import com.ionic.sdk.agent.request.createassertion.data.PubkeyResolver;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceRequest;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceResponse;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceTransaction;
import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.createkey.CreateKeysTransaction;
import com.ionic.sdk.agent.request.getkey.GetKeysRequest;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysTransaction;
import com.ionic.sdk.agent.request.getkeyspace.GetKeyspaceRequest;
import com.ionic.sdk.agent.request.getkeyspace.GetKeyspaceResponse;
import com.ionic.sdk.agent.request.getkeyspace.GetKeyspaceTransaction;
import com.ionic.sdk.agent.request.getresources.GetResourcesRequest;
import com.ionic.sdk.agent.request.getresources.GetResourcesResponse;
import com.ionic.sdk.agent.request.getresources.GetResourcesTransaction;
import com.ionic.sdk.agent.request.logmessage.LogMessagesRequest;
import com.ionic.sdk.agent.request.logmessage.LogMessagesResponse;
import com.ionic.sdk.agent.request.logmessage.LogMessagesTransaction;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysRequest;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysResponse;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysTransaction;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.core.date.DateTime;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.device.create.saml.DeviceEnrollment;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.device.profile.persistor.DeviceProfiles;
import com.ionic.sdk.device.profile.persistor.ProfilePersistor;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.IonicServerException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.util.ArrayList;
import java.util.List;

/**
 * The main point of interaction with the Ionic Machina Tools SDK.  {@link Agent} instances provide APIs to perform
 * the following Machina operations:
 * <ul>
 * <li>Create Key</li>
 * <li>Get Key</li>
 * <li>Update Key</li>
 * <li>Get Resources</li>
 * <li>Log Messages</li>
 * <li>Create Device</li>
 * </ul>
 * <p>
 * For this {@link KeyServices} implementation, these operations involve a request to a remote Machina key
 * server.  On failure of this operation, an {@link IonicException} is thrown, which contains additional data
 * about the failure.
 * <p>
 * {@link Agent} objects may be instantiated in a few different ways.
 * <ul>
 * <li>{@link Agent#Agent()} creates a fresh Agent instance with default configuration (uninitialized).</li>
 * <li>{@link Agent#Agent(AgentConfig)} creates a fresh Agent instance with custom configuration (uninitialized).</li>
 * <li>{@link Agent#Agent(ProfilePersistor)} creates a fresh Agent instance with default configuration, and using
 * the {@link DeviceProfile} objects associated with the specified {@link ProfilePersistor} (initialized)</li>
 * <li>{@link Agent#Agent(DeviceProfiles)} creates a fresh Agent instance from a set of
 * in-memory {@link DeviceProfile} objects (initialized)</li>
 * </ul>
 * <p>
 * Uninitialized {@link Agent} instances should be initialized using {@link Agent#initialize()} before attempting any
 * service interactions.
 * <p>
 * {@link Agent} instances interact with Machina via associated {@link DeviceProfile} objects.  These are loaded into
 * the Agent during construction, or through the {@link #loadProfiles(ProfilePersistor)} API.  DeviceProfile
 * objects may be added to the working set of the Agent through the {@link #addProfile(DeviceProfile)} API.  Updates to
 * the DeviceProfile working set may be saved through the {@link #saveProfiles(ProfilePersistor)} API.
 * <p>
 * Implementations of {@link ProfilePersistor} are typically backed by a file on a filesystem available to
 * the {@link Agent} process.  The agent maintains an in-memory cache of {@link DeviceProfile} objects for use during
 * cryptography operations.  The API {@link #loadProfiles(ProfilePersistor)} causes the agent cache
 * of DeviceProfile objects to be replaced with those retrieved from the file.  The
 * API {@link #saveProfiles(ProfilePersistor)} causes the serialized ProfilePersistor file to be replaced
 * with the records from the agent cache.  No internal reference to a ProfilePersistor is maintained by
 * the agent.
 * <p>
 * Machina Tools SDK for Java does not have the concept of a default ProfilePersistor, so use of
 * the {@link #loadProfiles()} and {@link #saveProfiles()} APIs will cause an {@link IonicException} to be thrown.
 * <p>
 * Sample (simple agent instantiation and usage):
 * <pre>
 * public final void testAgent_Initialize_CreateKey_GetKey() throws IonicException {
 *     final ProfilePersistor profilePersistor = IonicTestEnvironment.getInstance().getProfilePersistor();
 *     final Agent agent = new Agent(profilePersistor);
 *     final CreateKeysResponse createKeysResponse = agent.createKey();
 *     final AgentKey keyCreate = createKeysResponse.getFirstKey();
 *     final GetKeysResponse getKeysResponse = agent.getKey(keyCreate.getId());
 *     final AgentKey keyGet = getKeysResponse.getKeys().iterator().next();
 *     Assert.assertEquals(keyCreate.getId(), keyGet.getId());
 * }
 * </pre>
 * <p>
 * Sample (set custom DeviceProfile):
 * <pre>
 * public final void testAgent_InitializeFromDeviceProfile() throws IonicException {
 *     final ProfilePersistor profilePersistor = IonicTestEnvironment.getInstance().getProfilePersistor();
 *     final Agent agentLoadProfiles = new Agent(profilePersistor);
 *     final DeviceProfile activeProfile = agentLoadProfiles.getActiveProfile();
 *     // instantiate blank Agent
 *     final Agent agent = new Agent();
 *     agent.initializeWithoutProfiles();
 *     agent.addProfile(activeProfile, true);
 *     final CreateKeysResponse createKeysResponse = agent.createKey();
 *     final AgentKey keyCreate = createKeysResponse.getFirstKey();
 *     final GetKeysResponse getKeysResponse = agent.getKey(keyCreate.getId());
 *     final AgentKey keyGet = getKeysResponse.getKeys().iterator().next();
 *     Assert.assertEquals(keyCreate.getId(), keyGet.getId());
 * }
 * </pre>
 * <p>
 * Sample (set custom HTTP "User-Agent" header):
 * <pre>
 * public final void testAgent_InitializeFromAgentConfig() throws IonicException {
 *     final AgentConfig agentConfig = new AgentConfig();
 *     agentConfig.setUserAgent("Custom Machina HTTP User Agent");
 *     final Agent agent = new Agent(agentConfig);
 *     final ProfilePersistor profilePersistor = IonicTestEnvironment.getInstance().getProfilePersistor();
 *     agent.initialize(profilePersistor);
 *     final CreateKeysResponse createKeysResponse = agent.createKey();
 *     Assert.assertEquals(1, createKeysResponse.getKeys().size());
 * }
 * </pre>
 * <p>
 * Keyspace Name Service (KNS), based on DNS, allows Machina clients to resolve API endpoint information by
 * providing a valid four-character keyspace ID.  KNS can be used to update existing {@link DeviceProfile} records (for
 * instance, in the case of a Machina tenant migration).  Updates may be persisted to the filesystem via a subsequent
 * call to {@link Agent#saveProfiles(ProfilePersistor)}.
 * <p>
 * Sample (update in-memory copy of active profile's server string, loaded from filesystem):
 * <pre>
 * public final void testAgent_KNS_UpdateActiveProfile_FromProfilePersistor() throws IonicException {
 *     final ProfilePersistor profilePersistor = IonicTestEnvironment.getInstance().getProfilePersistor();
 *     final Agent agent = new Agent(profilePersistor);
 *     final String deviceId = agent.getActiveProfile().getDeviceId();
 *     final String server = agent.getActiveProfile().getServer();
 *     agent.updateProfileFromKNS(deviceId, "https://api.ionic.com");
 *     final String serverUpdate = agent.getActiveProfile().getServer();
 *     Assert.assertEquals(server, serverUpdate);  // url should be the same
 *     Assert.assertNotSame(server, serverUpdate);  // the string reference should be updated
 * }
 * </pre>
 * <p>
 * Sample (update in-memory copy of active profile's server string, loaded from string):
 * <pre>
 * public final void testAgent_KNS_UpdateActiveProfile_FromJsonString() throws IonicException, IOException {
 *     final File file = IonicTestEnvironment.getInstance().getFileProfilePersistor();
 *     Assert.assertNotNull(file);
 *     final String json = Transcoder.utf8().encode(Stream.read(file));
 *     final Agent agent = new Agent(new DeviceProfiles(json));
 *     final String deviceId = agent.getActiveProfile().getDeviceId();
 *     final String server = agent.getActiveProfile().getServer();
 *     agent.updateProfileFromKNS(deviceId, "https://api.ionic.com");
 *     final String serverUpdate = agent.getActiveProfile().getServer();
 *     Assert.assertEquals(server, serverUpdate);  // url should be the same
 *     Assert.assertNotSame(server, serverUpdate);  // the string reference should be updated
 * }
 * </pre>
 * <p>
 * Sample (persist update of active profile's server string to filesystem):
 * <pre>
 * public final void testAgent_KNS_UpdateActiveProfile_PersistUpdate() throws IonicException {
 *     final ProfilePersistor profilePersistor = IonicTestEnvironment.getInstance().getProfilePersistor();
 *     final Agent agent = new Agent(profilePersistor);
 *     final String deviceId = agent.getActiveProfile().getDeviceId();
 *     agent.updateProfileFromKNS(deviceId, "https://api.ionic.com");
 *     agent.saveProfiles(profilePersistor);
 * }
 * </pre>
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
        this(new AgentConfig());
    }

    /**
     * Copy constructor.
     * <p>
     * Copy the internal state of an {@link Agent}, so that the SEP file I/O is unnecessary.  This will greatly
     * increase the throughput of Agent object instantiation.
     *
     * @param agent the object from which the loaded state should be copied
     */
    public Agent(final Agent agent) {
        this(agent.getConfig());
        initialized = agent.initialized;
        deviceProfiles.addAll(agent.deviceProfiles);
        activeProfile = agent.activeProfile;
        fingerprint = agent.fingerprint;
        setMetadata(agent.getMetadata());
    }

    /**
     * Convenience constructor that allows for instantiation of an uninitialized Agent with configuration.
     *
     * @param agentConfig the configuration of this agent
     */
    public Agent(final AgentConfig agentConfig) {
        this.initialized = false;
        this.deviceProfiles = new ArrayList<DeviceProfile>();
        this.agentConfig = agentConfig;
    }

    /**
     * Convenience constructor that allows for instantiation of an initialized Agent in a single step.
     *
     * @param persistor The device profile persistor.
     * @throws IonicException on errors
     */
    public Agent(final ProfilePersistor persistor) throws IonicException {
        this(new AgentConfig());
        initializeInternal(agentConfig, persistor, new MetadataMap(), new Fingerprint(null));
    }

    /**
     * Convenience constructor that allows for instantiation of an initialized Agent in a single step.
     *
     * @param deviceProfiles The device profiles with which to initialize the Agent.
     * @throws IonicException on errors
     */
    public Agent(final DeviceProfiles deviceProfiles) throws IonicException {
        this(new AgentConfig());
        initializeInternal(agentConfig, deviceProfiles, new MetadataMap(), new Fingerprint(null));
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
        return (activeProfile == null) ? new DeviceProfile() : activeProfile;
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
            if (Value.isEqual(deviceProfile.getDeviceId(), deviceId)) {
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
        final boolean found = setActiveProfileInternal(deviceId);
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
     * Return the {@link DeviceProfile} associated with the parameter input.
     *
     * @param deviceId the ID of the {@link DeviceProfile} to retrieve
     * @return the {@link DeviceProfile} if found; otherwise null
     */
    private DeviceProfile getProfileInternal(final String deviceId) {
        DeviceProfile deviceProfile = null;
        for (DeviceProfile deviceProfileIt : this.deviceProfiles) {
            if (Value.isEqual(deviceProfileIt.getDeviceId(), deviceId)) {
                deviceProfile = deviceProfileIt;
                break;
            }
        }
        return deviceProfile;
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
     * Update the device profile with specified deviceId using KNS (Keyspace Name Service).  KNS, based on DNS, allows
     * Machina clients to resolve endpoint information by providing a valid four-character keyspace ID.
     * <p>
     * The keyspace API URL is updated based on current information provided by KNS.  Use
     * {@link #saveProfiles(ProfilePersistor)} to persist the update.
     * <p>
     * This {@link Agent} API is intended to facilitate the migration of a Machina key server to a new DNS name.
     *
     * @param deviceId the device ID of the profile to update; if empty, the active profile is updated
     * @param knsUrl   the KNS endpoint URL to use for the request; if empty, the default endpoint URL
     *                 'https://api.ionic.com/' is used
     * @throws IonicException on bad input; lookup failure (API endpoint access)
     */
    public final void updateProfileFromKNS(final String deviceId, final String knsUrl) throws IonicException {
        final DeviceProfile deviceProfile = Value.isEmpty(deviceId) ? activeProfile : getProfileInternal(deviceId);
        SdkData.checkTrue((deviceProfile != null), SdkError.ISAGENT_NO_DEVICE_PROFILE);
        final String keySpace = deviceProfile.getKeySpace();
        SdkData.checkTrue((keySpace != null), SdkError.ISAGENT_MISSINGVALUE);
        final String knsUrlCall = (Value.isEmpty(knsUrl) ? GetKeyspaceRequest.API_BASE_URL : knsUrl);
        final GetKeyspaceResponse response = getKeyspaceInternal(new GetKeyspaceRequest(keySpace, knsUrlCall));
        final List<String> apiURLs = response.getApiURLs();
        deviceProfile.setServer(apiURLs.isEmpty()
                ? new String(deviceProfile.getServer().toCharArray()) : apiURLs.iterator().next());
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
    public final void loadProfiles(final ProfilePersistor persistor) throws IonicException {
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
    private void loadProfilesInternal(final ProfilePersistor persistor) throws IonicException {
        final String[] activeProfileParam = new String[1];
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
        loadProfilesInternal(deviceProfilesInit, activeProfileParam[0]);
    }

    /**
     * Initialize this agent's device profile set with the supplied information.  This function facilitates SDK usages
     * in cloud contexts, where {@link ProfilePersistor} objects are inappropriate.
     *
     * @param deviceProfiles  list of {@link DeviceProfile} for use by the agent
     * @param activeProfileId the device id of the {@link DeviceProfile} designated as active
     * @throws IonicException on errors
     */
    private void loadProfilesInternal(
            final List<DeviceProfile> deviceProfiles, final String activeProfileId) throws IonicException {
        for (DeviceProfile deviceProfile : deviceProfiles) {
            try {
                deviceProfile.isValid();
            } catch (IonicException e) {
                throw new IonicException(SdkError.ISAGENT_LOAD_PROFILES_FAILED, e);
            }
        }
        this.deviceProfiles = deviceProfiles;
        setActiveProfileInternal(activeProfileId);
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
    public final void saveProfiles(final ProfilePersistor persistor) throws IonicException {
        final String activeDeviceId = (this.activeProfile == null) ? null : this.activeProfile.getDeviceId();
        persistor.saveAllProfiles(this.deviceProfiles, activeDeviceId);
    }

    /**
     * Resolve the {@link DeviceProfile} to be used in the context of this {@link GetKeysRequest}.
     * <p>
     * To determine the {@link DeviceProfile} to use for this request, the {@link AgentConfig} boolean
     * property  "autoselectprofile" is examined.
     * <ul>
     * <li>If set to "true", the most recently created device profile with a matching keyspace is returned.</li>
     * <li>Otherwise, the agent's active profile is used.</li>
     * </ul>
     * <p>
     * Note that only one server request is issued for a given GetKeys transaction.  In particular, it is not valid to
     * request keys from multiple key servers in a single GetKeys request.  To request keys from multiple keyspaces,
     * one GetKeys request should be made for each unique keyspace.
     *
     * @param keyId The protection key ID to fetch.
     * @return the relevant {@link DeviceProfile} record for the request
     */
    @SuppressWarnings("unused")  // used by JavaJNI tests
    public final DeviceProfile getDeviceProfileForKeyId(final String keyId) {
        return getDeviceProfileForKeyIdInternal(keyId);
    }

    /**
     * Resolve the {@link DeviceProfile} to be used in the context of this {@link GetKeysRequest}.
     *
     * @param keyId The protection key ID to fetch.
     * @return the relevant {@link DeviceProfile} record for the request
     */
    private DeviceProfile getDeviceProfileForKeyIdInternal(final String keyId) {
        final boolean autoProfile = Boolean.parseBoolean(agentConfig.getProperty(
                AgentConfig.Key.AUTOSELECT_PROFILE, Boolean.TRUE.toString()));
        final boolean disableAutoProfile = Value.isEmpty(keyId);
        return (autoProfile && !disableAutoProfile)
                ? AgentTransactionUtil.getProfileForKeyId(deviceProfiles, keyId) : activeProfile;
    }

    /**
     * Dynamically resolve the server metadata associated with a four-character Machina keyspace.
     *
     * @param request The request input data object.
     * @return The response output data object, containing the keyspace access metadata.
     * @throws IonicException if an error occurs
     */
    public final GetKeyspaceResponse getKeyspace(final GetKeyspaceRequest request) throws IonicException {
        return getKeyspaceInternal(request);
    }

    /**
     * Dynamically resolve the server metadata associated with a four-character Machina keyspace.
     *
     * @param keyspace The four character Machina keyspace to lookup.
     * @return The response output data object, containing the keyspace access metadata.
     * @throws IonicException if an error occurs
     */
    public final GetKeyspaceResponse getKeyspace(final String keyspace) throws IonicException {
        return getKeyspaceInternal(new GetKeyspaceRequest(keyspace));
    }

    /**
     * Dynamically resolve the server metadata associated with a four-character Machina keyspace.
     *
     * @param keyspace The four character Machina keyspace to lookup.
     * @param url      The API URL used to perform the request.
     * @return The response output data object, containing the keyspace access metadata.
     * @throws IonicException if an error occurs
     */
    public final GetKeyspaceResponse getKeyspace(final String keyspace, final String url) throws IonicException {
        return getKeyspaceInternal(new GetKeyspaceRequest(keyspace, url));
    }

    /**
     * Dynamically resolve the server metadata associated with a four-character Machina keyspace.
     *
     * @param request The request input data object.
     * @return The response output data object, containing the keyspace access metadata.
     * @throws IonicException if an error occurs
     */
    private GetKeyspaceResponse getKeyspaceInternal(final GetKeyspaceRequest request) throws IonicException {
        final GetKeyspaceResponse response = new GetKeyspaceResponse();
        final GetKeyspaceTransaction transaction = new GetKeyspaceTransaction(new VbeProtocol(this), request, response);
        transaction.run();
        return response;
    }

    /**
     * Creates a new device record for use with subsequent requests to Ionic.com.
     *
     * @param assertion        a (pre-generated) SAML assertion (proof of validated authentication to keyspace)
     * @param keyspace         the four character Machina keyspace that is the target of the enrollment
     * @param makeDeviceActive assign the newly created device as the active device for the agent
     * @return the response output data object, containing the created device profile
     * @throws IonicException if an error occurs
     */
    public final CreateDeviceResponse createDevice(
            final byte[] assertion, final String keyspace, final boolean makeDeviceActive) throws IonicException {
        final GetKeyspaceResponse getKeyspaceResponse = getKeyspaceInternal(new GetKeyspaceRequest(keyspace));
        final String enrollmentURL = getKeyspaceResponse.getFirstEnrollmentURL();
        final DeviceEnrollment deviceEnrollment = new DeviceEnrollment(DeviceUtils.toUrl(enrollmentURL));
        final CreateDeviceResponse createDeviceResponse = deviceEnrollment.enroll(assertion);
        addProfileInternal(createDeviceResponse.getDeviceProfile(), makeDeviceActive);
        return createDeviceResponse;
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
        final CreateDeviceTransaction transaction =
                new CreateDeviceTransaction(new VbeProtocol(this), request, response);
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
        final CreateDeviceTransaction transaction =
                new CreateDeviceTransaction(new VbeProtocol(this), request, response);
        transaction.run();
        addProfileInternal(response.getDeviceProfile(), makeDeviceActive);
        return response;
    }

    /**
     * Creates one or more protection keys in the Machina service, and returns those keys to the caller.
     * <p>
     * Each protection key may contain caller-specified **immutable** key attributes, which describe the context
     * of the intended key usage.  Each key may also contain caller-specified **mutable** key attributes, which
     * may be updated after creation via the API {@link #updateKeys(UpdateKeysRequest)}.
     * <p>
     * The {@link CreateKeysRequest} parameter may contain caller-specified metadata, which provides contextual
     * information about the device making the request.
     * <p>
     * NOTE: please limit to 1,000 keys per request, otherwise the server will return an error.
     *
     * @param request the protection key request input data object
     * @return the protection key response output data object
     * @throws IonicException on failure to create the requested protection key(s)
     */
    @Override
    public final CreateKeysResponse createKeys(final CreateKeysRequest request) throws IonicException {
        return createKeysInternal(request, new VbeProtocol(this));
    }

    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                              final KeyAttributesMap mutableAttributes,
                                              final MetadataMap metadata) throws IonicException {
        return createKeyInternal(attributes, mutableAttributes, metadata, new VbeProtocol(this));
    }

    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                              final KeyAttributesMap mutableAttributes) throws IonicException {
        return createKeyInternal(attributes, mutableAttributes, new MetadataMap(), new VbeProtocol(this));
    }

    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes,
                                              final MetadataMap metadata) throws IonicException {
        return createKeyInternal(attributes, new KeyAttributesMap(), metadata, new VbeProtocol(this));
    }

    @Override
    public final CreateKeysResponse createKey(final KeyAttributesMap attributes) throws IonicException {
        return createKeyInternal(attributes, new KeyAttributesMap(), new MetadataMap(), new VbeProtocol(this));
    }

    @Override
    public final CreateKeysResponse createKey(final MetadataMap metadata) throws IonicException {
        return createKeyInternal(
                new KeyAttributesMap(), new KeyAttributesMap(), metadata, new VbeProtocol(this));
    }

    @Override
    public final CreateKeysResponse createKey() throws IonicException {
        return createKeyInternal(
                new KeyAttributesMap(), new KeyAttributesMap(), new MetadataMap(), new VbeProtocol(this));
    }

    /**
     * Creates one or more protection keys in the Machina service, and returns those keys to the caller.
     *
     * @param request  the protection key request input data object
     * @param protocol the implementation of the semantics for the targeted service endpoint
     * @return the protection key response output data object
     * @throws IonicException on failure to create the requested protection key(s)
     */
    protected CreateKeysResponse createKeysInternal(final CreateKeysRequest request,
                                                    final ServiceProtocol protocol) throws IonicException {
        final CreateKeysResponse response = new CreateKeysResponse();
        final CreateKeysTransaction transaction = new CreateKeysTransaction(protocol, request, response);
        transaction.run();
        return response;
    }

    /**
     * Creates one or more protection keys in the Machina service, and returns those keys to the caller.
     *
     * @param attributes        the **immutable** key attributes to associate with the key
     * @param mutableAttributes the mutable key attributes to associate with the key
     * @param metadata          the metadata properties to associate with the request
     * @param protocol          the implementation of the semantics for the targeted service endpoint
     * @return the protection key response output data object
     * @throws IonicException on failure to create the requested protection key
     */
    protected CreateKeysResponse createKeyInternal(final KeyAttributesMap attributes,
                                                   final KeyAttributesMap mutableAttributes,
                                                   final MetadataMap metadata,
                                                   final ServiceProtocol protocol) throws IonicException {
        final CreateKeysRequest request = new CreateKeysRequest();
        request.setMetadata(metadata);
        request.add(new CreateKeysRequest.Key(IDC.Payload.REF, 1, attributes, mutableAttributes));
        final CreateKeysResponse response = new CreateKeysResponse();
        final CreateKeysTransaction transaction = new CreateKeysTransaction(protocol, request, response);
        transaction.run();
        // response validation (reference implementation symmetry)
        if (response.getKeys().isEmpty()) {
            throw new IonicException(SdkError.ISAGENT_KEY_DENIED,
                    new IonicServerException(SdkError.ISAGENT_MISSINGVALUE, response.getConversationId(), response));
        }
        return response;
    }

    /**
     * Retrieves a set of protection keys (by key tag, or by external id) from the Machina service.
     * <p>
     * The {@link GetKeysRequest} parameter may contain caller-specified metadata, which provides contextual
     * information about the device making the request.
     * <p>
     * NOTE: please limit to 1,000 keys per request, otherwise the server will return an error.
     *
     * @param request the protection key request input data object
     * @return the protection key response output data object
     * @throws IonicException on failure to retrieve the requested protection key(s)
     */
    @Override
    public final GetKeysResponse getKeys(final GetKeysRequest request) throws IonicException {
        final String firstKeyId = (request.getKeyIds().isEmpty() ? "" : request.getKeyIds().iterator().next());
        return getKeysInternal(request, new VbeProtocol(this, getDeviceProfileForKeyIdInternal(firstKeyId)));
    }

    @Override
    public final GetKeysResponse getKey(final String keyId, final MetadataMap metadata) throws IonicException {
        return getKeyInternal(keyId, metadata, new VbeProtocol(this, getDeviceProfileForKeyIdInternal(keyId)));
    }

    @Override
    public final GetKeysResponse getKey(final String keyId) throws IonicException {
        return getKeyInternal(keyId, new MetadataMap(),
                new VbeProtocol(this, getDeviceProfileForKeyIdInternal(keyId)));
    }

    /**
     * Retrieves a single protection key (by key tag, or by external id) from the Machina service.
     *
     * @param request  the protection key request input data object
     * @param protocol the implementation of the semantics for the targeted service endpoint
     * @return the protection key response output data object
     * @throws IonicException on failure to retrieve the requested protection key(s)
     */
    protected GetKeysResponse getKeysInternal(final GetKeysRequest request,
                                              final ServiceProtocol protocol) throws IonicException {
        final GetKeysResponse response = new GetKeysResponse();
        final GetKeysTransaction transaction = new GetKeysTransaction(protocol, request, response);
        transaction.run();
        return response;
    }

    /**
     * Retrieves a single protection key (by key tag) from the Machina service.
     * <p>
     * By convention, failures to retrieve a single {@link com.ionic.sdk.agent.key.AgentKey} specified by key tag
     * should result in an {@link IonicException} with the error code {@link SdkError#ISAGENT_KEY_DENIED}.
     *
     * @param keyId    the protection key tag to fetch
     * @param metadata the metadata properties to associate with the request
     * @param protocol the implementation of the semantics for the targeted service endpoint
     * @return the protection key response output data object
     * @throws IonicException on failure of the remote request; on failure to fetch the specified protection key
     */
    protected GetKeysResponse getKeyInternal(final String keyId, final MetadataMap metadata,
                                             final ServiceProtocol protocol) throws IonicException {
        final GetKeysRequest request = new GetKeysRequest();
        request.setMetadata(metadata);
        request.add(keyId);
        final GetKeysResponse response = new GetKeysResponse();
        final GetKeysTransaction transaction = new GetKeysTransaction(protocol, request, response);
        transaction.run();
        // response validation (reference implementation symmetry), SDK-3105
        if (response.getKeys().isEmpty()) {
            throw new IonicException(SdkError.ISAGENT_KEY_DENIED,
                    new IonicServerException(SdkError.ISAGENT_MISSINGVALUE, response.getConversationId(), response));
        }
        return response;
    }

    /**
     * Updates a set of protection keys in the Machina service.
     *
     * @param request the protection key request input data object
     * @return the protection key response output data object
     * @throws IonicException on failure of the remote request
     */
    private UpdateKeysResponse updateKeysInternal(final UpdateKeysRequest request) throws IonicException {
        final UpdateKeysResponse response = new UpdateKeysResponse();
        final UpdateKeysTransaction transaction =
                new UpdateKeysTransaction(new VbeProtocol(this), request, response);
        transaction.run();
        return response;
    }

    @Override
    public final UpdateKeysResponse updateKeys(final UpdateKeysRequest request) throws IonicException {
        return updateKeysInternal(request);
    }

    @Override
    public final UpdateKeysResponse updateKey(
            final UpdateKeysRequest.Key key, final MetadataMap metadata) throws IonicException {
        final UpdateKeysRequest request = new UpdateKeysRequest();
        request.setMetadata(metadata);
        request.addKey(new UpdateKeysRequest.Key(key));
        return updateKeysInternal(request);
    }

    /**
     * Updates a single protection key in the Machina service.
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
     * @param key key to update
     * @return the protection key response output data object
     * @throws IonicException on failure of the remote request
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
     * the output response (guaranteed to exist) and check the error field
     * ({@link com.ionic.sdk.agent.request.getresources.GetResourcesResponse.Resource#getError()}) to see if an
     * error occurred.  If the error field is empty, then the data field
     * ({@link com.ionic.sdk.agent.request.getresources.GetResourcesResponse.Resource#getData()}) should be expected
     * to contain the resource.
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
     * field ({@link com.ionic.sdk.agent.request.getresources.GetResourcesResponse.Resource#getError()}) of each output
     * resource object to see if an error occurred.
     * If the error field is empty, then the data field
     * ({@link com.ionic.sdk.agent.request.getresources.GetResourcesResponse.Resource#getData()}) should be expected
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
        final GetResourcesTransaction transaction =
                new GetResourcesTransaction(new VbeProtocol(this), request, response);
        transaction.run();
        return response;
    }

    /**
     * Logs one or more messages to Ionic.com. This method makes an HTTP call to
     * Ionic.com to post one or more log messages.
     *
     * @param request the message request input data object
     * @return the response output data object
     * @throws IonicException if an error occurs during servicing of the request
     */
    public final LogMessagesResponse logMessages(final LogMessagesRequest request) throws IonicException {
        return logMessageInternal(request);
    }

    /**
     * Logs one or more messages to Ionic.com. This method makes an HTTP call to
     * Ionic.com to post one or more log messages.
     *
     * @param message  the message request input data object
     * @param metadata the metadata properties to send along with the HTTP request
     * @return the response output data object
     * @throws IonicException if an error occurs during servicing of the request
     */
    public final LogMessagesResponse logMessage(final LogMessagesRequest.Message message, final MetadataMap metadata)
            throws IonicException {
        final LogMessagesRequest request = new LogMessagesRequest();
        request.setMetadata(metadata);
        request.add(message);
        return logMessageInternal(request);
    }

    /**
     * Logs one or more messages to Ionic.com. This method makes an HTTP call to
     * Ionic.com to post one or more log messages.
     *
     * @param message the message request input data object
     * @return the response output data object
     * @throws IonicException if an error occurs during servicing of the request
     */
    public final LogMessagesResponse logMessage(final LogMessagesRequest.Message message) throws IonicException {
        final LogMessagesRequest request = new LogMessagesRequest();
        request.add(message);
        return logMessageInternal(request);
    }

    /**
     * Logs one or more messages to Ionic.com. This method makes an HTTP call to Ionic.com to post one or more
     * log messages.
     *
     * @param request the log message request input data object
     * @return the response output data object
     * @throws IonicException if an error occurs during servicing of the request
     */
    private LogMessagesResponse logMessageInternal(final LogMessagesRequest request) throws IonicException {
        final LogMessagesResponse response = new LogMessagesResponse();
        final LogMessagesTransaction transaction =
                new LogMessagesTransaction(new VbeProtocol(this), request, response);
        transaction.run();
        return response;
    }

    /**
     * Creates an Identity Assertion using the active {@link DeviceProfile}.  Assertions are used as proof of
     * membership in an Ionic keyspace.  They can be created by Ionic devices, and validated by any other system.
     *
     * @param request the identity assertion request input data object
     * @return the identity assertion response output data object
     * @throws IonicException if an error occurs during servicing of the request
     */
    public CreateIdentityAssertionResponse createIdentityAssertion(
            final CreateIdentityAssertionRequest request) throws IonicException {
        final CreateIdentityAssertionResponse response = new CreateIdentityAssertionResponse();
        final CreateIdentityAssertionTransaction transaction =
                new CreateIdentityAssertionTransaction(new VbeProtocol(this), request, response);
        transaction.run();
        return response;
    }

    /**
     * Verify the validity of an Identity Assertion.  This may be used to verify that the assertion was
     * generated by a previously enrolled Ionic Machina device.
     *
     * @param pubkeyBase64    the public key component of the RSA keypair used to generate the identity assertion;
     *                        if null, the key is retrieved via a Keyspace Name Service (KNS) lookup
     * @param assertionBase64 the container for the supplied Identity Assertion data
     * @param nonce           the single-use token incorporated into the canonical form / digest of the assertion
     * @return a container object holding the data from the unwrapped assertionBase64 input
     * @throws IonicException on failure to validate the specified assertion
     */
    public IdentityAssertion validateIdentityAssertion(
            final String pubkeyBase64, final String assertionBase64, final String nonce) throws IonicException {
        final IdentityAssertion assertion = new IdentityAssertion(assertionBase64);
        final String keyspace = AssertionUtils.toKeyspace(assertion.getSigner());
        final IdentityAssertionValidator validator = new IdentityAssertionValidator(
                (pubkeyBase64 == null) ? new PubkeyResolver(this).getPublicKeyKeyspace(keyspace) : pubkeyBase64);
        return validator.validate(assertionBase64, null, null, nonce);
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
    public final void initialize(final ProfilePersistor persistor) throws IonicException {
        initializeInternal(agentConfig, persistor, new MetadataMap(), new Fingerprint(null));
    }

    /**
     * Initialize the agent with a configuration object and specified profile loader. The profile loader is used to
     * load device profiles during initialization.
     *
     * @param agentConfig Configuration object.
     * @param persistor   Device profile loader object.
     * @throws IonicException if an error occurs
     */
    public final void initialize(final AgentConfig agentConfig, final ProfilePersistor persistor)
            throws IonicException {
        initializeInternal(agentConfig, persistor, new MetadataMap(), new Fingerprint(null));
    }

    /**
     * Initialize the agent with default configuration.  This API may be used when no profile loader is available.
     *
     * @throws IonicException if an error occurs
     */
    public final void initializeWithoutProfiles() throws IonicException {
        initializeWithoutProfilesInternal(agentConfig);
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
        initializeInternal(agentConfig, (ProfilePersistor) null, new MetadataMap(), new Fingerprint(null));
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
    private void initializeInternal(final AgentConfig agentConfig, final ProfilePersistor persistor,
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
     * Initialize the agent with a configuration object and specified profile set.
     *
     * @param agentConfig    Configuration object.
     * @param deviceProfiles Device profile information for use by the Agent.
     * @param metadata       Device server metadata object.
     * @param fingerprint    Device fingerprint object.
     * @throws IonicException if an error occurs
     */
    private void initializeInternal(final AgentConfig agentConfig, final DeviceProfiles deviceProfiles,
                                    final MetadataMap metadata, final Fingerprint fingerprint) throws IonicException {
        if (initialized) {
            throw new IonicException(SdkError.ISAGENT_DOUBLEINIT);
        }
        AgentSdk.initialize();
        loadProfilesInternal(deviceProfiles.getProfiles(), deviceProfiles.getActiveProfileId());
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
     * Set by calibrateServerTimeOffsetMillis and tracks the basic difference between local UTC and the server UTC.
     */
    private static long serverTimeOffsetMillis;

    /**
     * Calibrate the server offset. Called when the server returns a timestamp denied error,
     * Agent will calibrate here and retry
     *
     * @param serverTimeMillis The server time returned from the timestamp denied response
     */
    public static void calibrateServerTimeOffsetMillis(final long serverTimeMillis) {

        // get current time on device with millisecond precision
        final long deviceTimeMillis = System.currentTimeMillis();

        // update server time offset to be the difference between server time millis
        // and our own device time millis
        serverTimeOffsetMillis = serverTimeMillis - deviceTimeMillis;
    }

    /**
     * Get the current server time UTC seconds.
     * <p>
     * This method returns the current server time UTC seconds.  See getServerTimeUtcMillis()
     * for more information about how this time value is determined and why it is useful.
     *
     * @return Server time in seconds
     * @see com.ionic.sdk.core.date.DateTime
     */
    public static long getServerTimeUtcSecs() {
        return ((System.currentTimeMillis() + serverTimeOffsetMillis) / DateTime.ONE_SECOND_MILLIS);
    }

    /**
     * Get the current server time UTC milliseconds.
     * <p>
     * This method returns the current server time UTC milliseconds based on information received
     * from any communication with Ionic.com. The server time is typically very similar or even
     * identical to local UTC time, but in some cases it can be substantially different if/when
     * the local clock is wrong. The server UTC time is a more reliable source of the absolute
     * real world UTC time than the local device UTC time.
     *
     * @return Server time in milliseconds
     * @see com.ionic.sdk.core.date.DateTime
     */
    public static long getServerTimeUtcMillis() {
        // get current UTC time on device with millisecond precision
        final long deviceTimeMillis = System.currentTimeMillis();

        // add server time offset to device UTC time and return
        return deviceTimeMillis + serverTimeOffsetMillis;
    }

    /**
     * Get the server offset UTC milliseconds.
     *
     * @return The server offset in milliseconds
     */
    public static long getServerTimeOffsetMillis() {
        return serverTimeOffsetMillis;
    }

    /**
     * Copy the internal state of an {@link Agent}, so that the SEP file I/O is unnecessary.  This will greatly
     * increase the throughput of Agent object instantiation.
     *
     * @param agent the object from which the loaded state should be copied
     * @return a new Agent instance, with configuration copied from original
     * @deprecated please migrate usages to the replacement {@link Agent#Agent(Agent)}
     */
    @Deprecated
    public static Agent clone(final Agent agent) {
        final Agent agentClone = new Agent();
        agentClone.initialized = agent.initialized;
        agentClone.deviceProfiles = new ArrayList<DeviceProfile>(agent.deviceProfiles);
        agentClone.activeProfile = agent.activeProfile;
        agentClone.agentConfig = new AgentConfig(agent.getConfig());
        agentClone.fingerprint = agent.fingerprint;
        agentClone.setMetadata(agent.getMetadata());
        return agentClone;
    }

    /**
     * This string constant represents the key origin ID for keys that originate
     * from an Ionic key server. Outside of advanced use cases by an SDK consumer, this is
     * the only key origin string that will ever be used.
     */
    public static final String KEYORIGIN_IONIC_KEYSERVER = IDC.Metadata.KEYORIGIN_IONIC;
}
