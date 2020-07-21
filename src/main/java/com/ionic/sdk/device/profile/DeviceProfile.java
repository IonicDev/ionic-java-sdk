package com.ionic.sdk.device.profile;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.io.Serializable;
import java.security.InvalidKeyException;

/**
 * This class represents the device profile of the machine we are on.
 */
public class DeviceProfile implements Serializable {

    /**
     * The device profile name.
     */
    private String deviceName = "";

    /**
     * The Ionic.com server associated with this device profile.
     */
    private String serverName = "";

    /**
     * The device profile ID.
     */
    private String deviceProfileId = "";

    /**
     * The creation time in UTC seconds.
     */
    private long timestamp;

    /**
     * The raw AES key bytes.
     */
    private byte[] aesCdIdcProfileKey;

    /**
     * The private AES key shared between client and EI.
     */
    private byte[] aesCdEiProfileKey;

    /**
     * Initializes the object to be empty. Creation time in seconds is initialized
     * to be zero.
     */
    public DeviceProfile() {
        timestamp = 0;
    }

    /**
     * Copy constructor.
     *
     * @param profile the existing DeviceProfile to replicate
     */
    public DeviceProfile(final DeviceProfile profile) {
        this(profile.getName(), profile.getCreationTimestampSecs(), profile.getDeviceId(),
                profile.getServer(), profile.getAesCdIdcProfileKey(), profile.getAesCdEiProfileKey());
    }

    /**
     * Constructor.
     *
     * @param name              name for device
     * @param creationTimestamp timestamp at which this device was created
     * @param deviceId          unique identifier for device
     * @param server            base URL of server for this profile
     * @param aesCdIdcKey       session key for session between device and ionic.com
     * @param aesCdEiKey        session key for session between device and Ionic enterprise infrastructure
     */
    public DeviceProfile(final String name, final long creationTimestamp, final String deviceId,
                         final String server, final byte[] aesCdIdcKey, final byte[] aesCdEiKey) {
        this.deviceName = name;
        this.timestamp = creationTimestamp;
        this.deviceProfileId = deviceId;
        this.serverName = server;
        this.aesCdIdcProfileKey = (aesCdIdcKey == null ? null : aesCdIdcKey.clone());
        this.aesCdEiProfileKey = (aesCdEiKey == null ? null : aesCdEiKey.clone());
    }

    /**
     * Determine if this profile has data.
     *
     * @return the status of loaded keys
     */
    public final boolean isLoaded() {
        return !deviceProfileId.isEmpty() && aesCdIdcProfileKey.length != 0 && aesCdEiProfileKey.length != 0;
    }

    /**
     * Determine if this profile is valid.  Any validation checks of member variables should be done here.
     *
     * @return the validity state of the {@link DeviceProfile} data
     * @throws IonicException on invalid {@link DeviceProfile} data
     */
    public final boolean isValid() throws IonicException {
        if ((aesCdIdcProfileKey == null) || (aesCdEiProfileKey == null)) {
            throw new IonicException(SdkError.ISAGENT_INVALID_KEY, new InvalidKeyException((String) null));
        }
        if (aesCdIdcProfileKey.length != AesCipher.KEY_BYTES) {
            throw new IonicException(SdkError.ISAGENT_INVALID_KEY,
                    new InvalidKeyException(Integer.toString(aesCdIdcProfileKey.length)));
        }
        if (aesCdEiProfileKey.length != AesCipher.KEY_BYTES) {
            throw new IonicException(SdkError.ISAGENT_INVALID_KEY,
                    new InvalidKeyException(Integer.toString(aesCdEiProfileKey.length)));
        }
        return true;
    }

    /**
     * Get the name associated with this device profile.
     *
     * @return The name associated with this device profile.
     */
    public final String getName() {
        return deviceName;
    }

    /**
     * Set the name associated with this device profile.
     *
     * @param name The name associated with this device profile.
     */
    public final void setName(final String name) {
        deviceName = name;
    }

    /**
     * Get the time at which this profile was created.
     *
     * @return Returns creation time in UTC seconds since January 1, 1970.
     * @see com.ionic.sdk.core.date.DateTime
     */
    public final long getCreationTimestampSecs() {
        return timestamp;
    }

    /**
     * Set the time at which this profile was created.
     *
     * @param timestamp Creation time must be specified in UTC seconds since January 1,
     *                  1970.
     * @see com.ionic.sdk.core.date.DateTime
     */
    public final void setCreationTimestampSecs(final long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the Ionic.com server associated with this device profile.
     *
     * @return The Ionic.com server associated with this device profile.
     */
    public final String getServer() {
        return serverName;
    }

    /**
     * Set the Ionic.com server associated with this device profile.
     *
     * @param server The Ionic.com server associated with this device profile.
     */
    public final void setServer(final String server) {
        serverName = server;
    }

    /**
     * Get the device profile ID. This device ID is generated by Ionic.com during
     * registration that is performed by calling
     * {@link com.ionic.sdk.agent.Agent#createDevice(com.ionic.sdk.agent.request.createdevice.CreateDeviceRequest)}.
     *
     * @return The device profile ID.
     */
    public final String getDeviceId() {
        return deviceProfileId;
    }

    /**
     * Set the device profile ID. This device ID is generated by Ionic.com during
     * registration that is performed by calling
     * {@link com.ionic.sdk.agent.Agent#createDevice(com.ionic.sdk.agent.request.createdevice.CreateDeviceRequest)}.
     * When the
     * device profile ID is set, the key space is also implicitly set by parsing it
     * out of the ID.
     *
     * @param deviceId The device profile ID.
     */
    public final void setDeviceId(final String deviceId) {
        deviceProfileId = deviceId;
    }

    /**
     * Get the private AES key shared between client and Ionic.com.
     *
     * @return The private AES key shared between client and Ionic.com.
     */
    public final byte[] getAesCdIdcProfileKey() {
        return aesCdIdcProfileKey == null ? null : aesCdIdcProfileKey.clone();
    }

    /**
     * Set the private AES key shared between client and Ionic.com.
     *
     * @param keyBytes The raw AES key bytes.
     */
    public final void setAesCdIdcProfileKey(final byte[] keyBytes) {
        if (keyBytes != null) {
            aesCdIdcProfileKey = keyBytes.clone();
        }
    }

    /**
     * Get the private AES key shared between client and EI (Enterprise
     * Infrastructure).
     *
     * @return The private AES key shared between client and EI (Enterprise
     * Infrastructure).
     */
    public final byte[] getAesCdEiProfileKey() {
        return aesCdEiProfileKey == null ? null : aesCdEiProfileKey.clone();
    }

    /**
     * Set the private AES key shared between client and EI (Enterprise
     * Infrastructure).
     *
     * @param keyBytes The raw AES key bytes.
     */
    public final void setAesCdEiProfileKey(final byte[] keyBytes) {
        if (keyBytes != null) {
            aesCdEiProfileKey = keyBytes.clone();
        }
    }

    /**
     * Get the Key space by parsing the Device id before the delimiter.
     *
     * @return the keySpace.
     */
    public final String getKeySpace() {
        return ((deviceProfileId == null) ? "" : deviceProfileId.split(REGEX_TOKEN_DOT, -1)[0]);
    }

    /**
     * Regular expression token used to split device ID into its constituent parts.
     */
    private static final String REGEX_TOKEN_DOT = "\\.";

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = -8004857920029153448L;
}
