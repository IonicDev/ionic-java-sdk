package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.PassThroughCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Logical data container for information stored in an Ionic Secure Enrollment Profile (SEP).  This data includes 1..n
 * {@link DeviceProfile} objects, and an indicator of the "active" device profile.
 * <p>
 * Each {@link DeviceProfile} object contains configuration specifying the Ionic key server to use for key requests, as
 * well as data to identify the client making the key requests.
 */
public class DeviceProfiles {

    /**
     * The list of profiles loaded into memory.
     */
    private final List<DeviceProfile> profiles;

    /**
     * The id of the active device profile.
     */
    private final String activeProfileId;

    /**
     * Constructor.
     *
     * @param json the json serialization of the enrollment information associated with a device
     * @throws IonicException on failure to parse the serialized json
     */
    public DeviceProfiles(final String json) throws IonicException {
        final InputStream is = new ByteArrayInputStream(Transcoder.utf8().decode(json));
        final DeviceProfilePersistorPlainText persistor = new DeviceProfilePersistorPlainText(is);
        final String[] activeProfile = new String[1];
        this.profiles = persistor.loadAllProfiles(activeProfile);
        this.activeProfileId = activeProfile[0];
    }

    /**
     * Constructor.
     *
     * @param deviceProfile a single logical device enrollment
     */
    public DeviceProfiles(final DeviceProfile deviceProfile) {
        this.profiles = Collections.singletonList(deviceProfile);
        this.activeProfileId = deviceProfile.getDeviceId();
    }

    /**
     * @return the loaded device enrollment profiles
     */
    public List<DeviceProfile> getProfiles() {
        return profiles;
    }

    /**
     * @return the device id of the active device enrollment profile
     */
    public String getActiveProfileId() {
        return activeProfileId;
    }

    /**
     * Serialize the contained device enrollment data to json format.
     *
     * @return a json string
     * @throws IonicException on failure to serialize the device enrollment data
     */
    public String toJson() throws IonicException {
        return Transcoder.utf8().encode(DeviceProfilePersistorPlainText.saveAllProfilesToJson(
                profiles, activeProfileId, new PassThroughCipher(null)));
    }
}
