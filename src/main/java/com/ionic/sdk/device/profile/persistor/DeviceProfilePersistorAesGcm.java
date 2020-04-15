package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.error.IonicException;

import java.io.InputStream;
import java.net.URL;

/**
 * DeviceProfilePersistorAesGcm provides for serialization to and deserialization from an accessible filesystem
 * file of a set of {@link com.ionic.sdk.device.profile.DeviceProfile}.  These DeviceProfile objects allow for service
 * interactions with 1..n Ionic Machina service keyspaces, which broker authenticated cryptography key transactions.
 * <p>
 * DeviceProfilePersistorAesGcm is a persistor that uses the AesGcmCipher with a user-supplied key and authData.
 * <p>
 * The constructor {@link #DeviceProfilePersistorAesGcm(InputStream)} may be used to load the serialized store of
 * {@link com.ionic.sdk.device.profile.DeviceProfile} objects from an InputStream.  The content is cached in this
 * persistor after construction, so the InputStream reference may be discarded.  The content would then be
 * deserialized in the context of the {@link #loadAllProfiles(String[])} API.  Before calling the
 * {@link #saveAllProfiles(java.util.List, String)} API, the save file path must be set using the
 * {@link #setFilePath(String)} API.
 * <p>
 * The constructor {@link #DeviceProfilePersistorAesGcm(URL)} may be used to load the serialized store of
 * {@link com.ionic.sdk.device.profile.DeviceProfile} objects from a URL.  The content would then be
 * deserialized in the context of the {@link #loadAllProfiles(String[])} API.  Before calling the
 * {@link #saveAllProfiles(java.util.List, String)} API, the save file path must be set using the
 * {@link #setFilePath(String)} API.
 * <p>
 * Sample:
 * <pre>
 * public final void testProfilePersistorAesGcm_SaveLoadProfiles() throws IonicException {
 *     final byte[] keyBytes = new byte[AesCipher.KEY_BYTES];
 *     final byte[] aad = new byte[AesCipher.KEY_BYTES];
 *     final ProfilePersistor profilePersistorTest = IonicTestEnvironment.getInstance().getProfilePersistor();
 *     final Agent agent1 = new Agent(profilePersistorTest);
 *     // persist the DeviceProfile information to a new file
 *     final File folderUserDir = new File(System.getProperty("user.dir"));
 *     final File filePersistor = new File(folderUserDir, getClass().getSimpleName() + ".aesgcm.sep");
 *     final DeviceProfilePersistorAesGcm profilePersistor1 =
 *             new DeviceProfilePersistorAesGcm(filePersistor.getPath());
 *     profilePersistor1.setKey(keyBytes);
 *     profilePersistor1.setAuthData(aad);
 *     agent1.saveProfiles(profilePersistor1);
 *     // load the DeviceProfile information from the new file
 *     final DeviceProfilePersistorAesGcm profilePersistor2 =
 *             new DeviceProfilePersistorAesGcm(filePersistor.getPath());
 *     profilePersistor2.setKey(keyBytes);
 *     profilePersistor2.setAuthData(aad);
 *     final Agent agent2 = new Agent(profilePersistor2);
 *     Assert.assertEquals(agent1.getActiveProfile().getDeviceId(), agent2.getActiveProfile().getDeviceId());
 * }
 * </pre>
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/initialize-agent-with-aes-persistor'
 * target='_blank'>Machina Developers</a> for
 * more information on this ProfilePersistor.
 */
public class DeviceProfilePersistorAesGcm extends DeviceProfilePersistorBase {

    /**
     * Auth data used in the AesGcmCipher cipher.
     */
    private byte[] mAuthData;

    /**
     * Key data used in the AesGcmCipher cipher.
     */
    private byte[] mKeyData;

    /**
     * Cast of the mCipher.
     */
    private final AesGcmCipher cipherCast;

    /**
     * Constructor.
     *
     * @param filePath path to a filesystem file containing serialized DeviceProfile objects
     * @throws IonicException on failure of the underlying runtime environment cipher to initialize
     */
    public DeviceProfilePersistorAesGcm(final String filePath) throws IonicException {
        super(filePath, new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Constructor.
     *
     * @param url location of a resource containing serialized DeviceProfile objects
     * @throws IonicException on failure of the underlying runtime environment cipher to initialize
     */
    public DeviceProfilePersistorAesGcm(final URL url) throws IonicException {
        super(url, new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Constructor.
     *
     * @param inputStream stream containing serialized DeviceProfile objects
     * @throws IonicException on failure of the underlying runtime environment cipher to initialize; read failure
     */
    public DeviceProfilePersistorAesGcm(final InputStream inputStream) throws IonicException {
        super(inputStream, new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Constructor.
     *
     * @throws IonicException on failure of the underlying runtime environment cipher to initialize
     */
    public DeviceProfilePersistorAesGcm() throws IonicException {
        super(new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Getter for the Authentication data.
     *
     * @return the additional authentication data used by the GCM cipher
     */
    public final byte[] getAuthData() {
        return mAuthData.clone();
    }

    /**
     * Setter for the authentication data.
     *
     * @param authData the additional authentication data to be used by the GCM cipher
     */
    public final void setAuthData(final byte[] authData) {
        mAuthData = authData.clone();
        cipherCast.setAuthData(mAuthData);
    }

    /**
     * Getter for the encryption key.
     *
     * @return key byte array
     */
    public final byte[] getKey() {
        return mKeyData.clone();
    }

    /**
     * Setter for the encryption key.
     *
     * @param key input parameter to set the new key bytes
     */
    public final void setKey(final byte[] key) {
        mKeyData = key.clone();
        cipherCast.setKey(mKeyData);
    }

    @Override
    protected final String getFormat() {
        return FORMAT_AESGCM;
    }

    /**
     * Ionic Secure Enrollment Profile type header field value.
     */
    public static final String FORMAT_AESGCM = "aesgcm";
}
