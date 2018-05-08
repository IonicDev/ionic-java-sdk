package com.ionic.sdk.device.profile.persistor;

import java.util.List;

import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;

/**
 * ProfilePersistor is an interface that enforces the base requirements of a
 * persistor.
 *
 * @author Ionic Security
 */
public interface ProfilePersistor {

    /**
     * Method required to implement in order to retrieve DeviceProfiles from a
     * persistor.
     *
     * @param activeProfile
     *            - this is an output parameter that will fetch the active profile.
     * @return List of DeviceProfiles is returned with all Device profiles retrieved
     *         from disk.
     * @throws IonicException
     *             - File IO, parsing, and decryption related exceptions are
     *             possible.
     */
    List<DeviceProfile> loadAllProfiles(String[] activeProfile) throws IonicException;

    /**
     * Method required to save data to files to qualify as a persistor.
     *
     * @param profiles
     *            - Change the list of available profiles to this input parameter.
     * @param activeProfile
     *            - Change the active device profile to this input parameter.
     * @throws IonicException
     *             - File IO and encryption related exceptions are possible.
     */
    void saveAllProfiles(List<DeviceProfile> profiles, String activeProfile) throws IonicException;
}
