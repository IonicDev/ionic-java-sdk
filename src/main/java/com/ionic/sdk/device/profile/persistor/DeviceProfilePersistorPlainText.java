package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.PassThroughCipher;
import com.ionic.sdk.error.IonicException;

import java.io.InputStream;
import java.net.URL;

/**
 * DeviceProfilePersistorPlainText provides for serialization to and deserialization from an accessible filesystem
 * file of a set of {@link com.ionic.sdk.device.profile.DeviceProfile}.  These DeviceProfile objects allow for service
 * interactions with 1..n Ionic Machina service keyspaces, which broker authenticated cryptography key transactions.
 * <p>
 * DeviceProfilePersistorPlainText is a persistor that uses a pass through cipher.  As the device credentials are
 * stored in a plaintext JSON representation, production usage is discouraged.
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
 * <p>
 * Sample:
 * <pre>
 * public final void testProfilePersistorPlaintext_SaveLoadProfiles() throws IonicException {
 *     final ProfilePersistor profilePersistorTest = IonicTestEnvironment.getInstance().getProfilePersistor();
 *     final Agent agent1 = new Agent(profilePersistorTest);
 *     // persist the DeviceProfile information to a new file
 *     final File folderUserDir = new File(System.getProperty("user.dir"));
 *     final File filePersistor = new File(folderUserDir, getClass().getSimpleName() + ".plaintext.txt");
 *     final DeviceProfilePersistorPlainText profilePersistor1 =
 *             new DeviceProfilePersistorPlainText(filePersistor.getPath());
 *     agent1.saveProfiles(profilePersistor1);
 *     // load the DeviceProfile information from the new file
 *     final DeviceProfilePersistorPlainText profilePersistor2 =
 *             new DeviceProfilePersistorPlainText(filePersistor.getPath());
 *     final Agent agent2 = new Agent(profilePersistor2);
 *     Assert.assertEquals(agent1.getActiveProfile().getDeviceId(), agent2.getActiveProfile().getDeviceId());
 * }
 * </pre>
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/initialize-agent-with-plaintext-persistor'
 * target='_blank'>Machina Developers</a> for
 * more information on this ProfilePersistor.
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


    @Override
    protected final String getFormat() {
        return FORMAT_PLAINTEXT;
    }

    /**
     * Ionic Secure Enrollment Profile type header field value.
     */
    public static final String FORMAT_PLAINTEXT = "plaintext";
}
