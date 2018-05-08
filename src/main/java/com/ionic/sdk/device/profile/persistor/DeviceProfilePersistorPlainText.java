package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.PassThroughCipher;

/**
 * DeviceProfilePersistorPlainText is a persistor that uses a pass through
 * cipher.
 *
 */
public class DeviceProfilePersistorPlainText extends DeviceProfilePersistorBase {

    /**
     * constructor for DeviceProfilePersistorPlainText.
     *
     * @param filePath
     *            where the DeviceProfile file is stored.
     */
    public DeviceProfilePersistorPlainText(final String filePath) {
        super(filePath, new PassThroughCipher(null));
    }

    /**
     * Default constructor for DeviceProfilePersistorPlainText.
     */
    public DeviceProfilePersistorPlainText() {
        super(new PassThroughCipher(null));
    }
}
