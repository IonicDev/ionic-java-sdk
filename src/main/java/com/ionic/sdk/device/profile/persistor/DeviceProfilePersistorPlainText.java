package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.PassThroughCipher;
import com.ionic.sdk.error.IonicException;

import java.io.InputStream;
import java.net.URL;

/**
 * DeviceProfilePersistorPlainText is a persistor that uses a pass through cipher.
 * <p>
 * The constructor {@link #DeviceProfilePersistorPlainText(InputStream)} may be used to load the serialized store of
 * {@link com.ionic.sdk.device.profile.DeviceProfile} objects from an InputStream.  The content is cached in this
 * persistor after construction, so the InputStream reference may be discarded.  The content would then be
 * deserialized in the context of the {@link #loadAllProfiles(String[])} API.  Before calling the
 * {@link #saveAllProfiles(java.util.List, String)} API, the save file path must be set using the
 * {@link #setFilePath(String)} API.
 * <p>
 * The constructor {@link #DeviceProfilePersistorPlainText(URL)} may be used to load the serialized store of
 * {@link com.ionic.sdk.device.profile.DeviceProfile} objects from a URL.  The content would then be
 * deserialized in the context of the {@link #loadAllProfiles(String[])} API.  Before calling the
 * {@link #saveAllProfiles(java.util.List, String)} API, the save file path must be set using the
 * {@link #setFilePath(String)} API.
 */
public class DeviceProfilePersistorPlainText extends DeviceProfilePersistorBase {

    /**
     * Constructor.
     *
     * @param filePath path to a filesystem file containing serialized DeviceProfile objects
     */
    public DeviceProfilePersistorPlainText(final String filePath) {
        super(filePath, new PassThroughCipher(null));
    }

    /**
     * Constructor.
     *
     * @param url location of a resource containing serialized DeviceProfile objects
     */
    public DeviceProfilePersistorPlainText(final URL url) {
        super(url, new PassThroughCipher(null));
    }

    /**
     * Constructor.
     *
     * @param inputStream stream containing serialized DeviceProfile objects
     * @throws IonicException on stream read failure
     */
    public DeviceProfilePersistorPlainText(final InputStream inputStream) throws IonicException {
        super(inputStream, new PassThroughCipher(null));
    }

    /**
     * Constructor.
     */
    public DeviceProfilePersistorPlainText() {
        super(new PassThroughCipher(null));
    }
}
