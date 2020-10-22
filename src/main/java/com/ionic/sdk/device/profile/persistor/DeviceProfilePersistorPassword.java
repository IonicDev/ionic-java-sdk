package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.rng.CryptoRng;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.json.JsonTarget;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * DeviceProfilePersistorPassword provides for serialization to and deserialization from an accessible filesystem
 * file of a set of {@link com.ionic.sdk.device.profile.DeviceProfile}.  These DeviceProfile objects allow for service
 * interactions with 1..n Ionic Machina service keyspaces, which broker authenticated cryptography key transactions.
 * <p>
 * DeviceProfilePersistorPassword is a persistor that uses the AesGcmCipher with a user-supplied password
 * hashed with pbkdf2.  The minimum password length is 6 characters; use of a null password, or of a password of
 * insufficient length, will cause an {@link IonicException} to be thrown.
 * <p>
 * The constructor {@link #DeviceProfilePersistorPassword(InputStream)} may be used to load the serialized store of
 * {@link com.ionic.sdk.device.profile.DeviceProfile} objects from an InputStream.  The content is cached in this
 * persistor after construction, so the InputStream reference may be discarded.  The content would then be
 * deserialized in the context of the {@link #loadAllProfiles(String[])} API.  Before calling the
 * {@link #saveAllProfiles(java.util.List, String)} API, the save file path must be set using the
 * {@link #setFilePath(String)} API.
 * <p>
 * The constructor {@link #DeviceProfilePersistorPassword(URL)} may be used to load the serialized store of
 * {@link com.ionic.sdk.device.profile.DeviceProfile} objects from a URL.  The content would then be
 * deserialized in the context of the {@link #loadAllProfiles(String[])} API.  Before calling the
 * {@link #saveAllProfiles(java.util.List, String)} API, the save file path must be set using the
 * {@link #setFilePath(String)} API.
 * <p>
 * Sample:
 * <pre>
 * public final void testProfilePersistorPassword_SaveLoadProfiles() throws IonicException {
 *     final String password = Long.toString(System.currentTimeMillis());
 *     final ProfilePersistor profilePersistorTest = IonicTestEnvironment.getInstance().getProfilePersistor();
 *     final Agent agent1 = new Agent(profilePersistorTest);
 *     // persist the DeviceProfile information to a new file
 *     final File folderUserDir = new File(System.getProperty("user.dir"));
 *     final File filePersistor = new File(folderUserDir, getClass().getSimpleName() + ".password.sep");
 *     final DeviceProfilePersistorPassword profilePersistor1 =
 *             new DeviceProfilePersistorPassword(filePersistor.getPath());
 *     profilePersistor1.setPassword(password);
 *     agent1.saveProfiles(profilePersistor1);
 *     // load the DeviceProfile information from the new file
 *     final DeviceProfilePersistorPassword profilePersistor2 =
 *             new DeviceProfilePersistorPassword(filePersistor.getPath());
 *     profilePersistor2.setPassword(password);
 *     final Agent agent2 = new Agent(profilePersistor2);
 *     Assert.assertEquals(agent1.getActiveProfile().getDeviceId(), agent2.getActiveProfile().getDeviceId());
 * }
 * </pre>
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/initialize-agent-with-password-persistor'
 * target='_blank'>Machina Developers</a> for
 * more information on this ProfilePersistor.
 */
public class DeviceProfilePersistorPassword extends DeviceProfilePersistorBase {

    /**
     * The AesGcmCipher cast of the mCipher.
     */
    private final AesGcmCipher cipherCast;

    /**
     * The password used to generate the key protecting the file data.
     */
    private String password;

    /**
     * The minimum acceptable password length.
     */
    private static final int MINIMUM_PASSWORD_LENGTH = 6;

    /**
     * The Ionic auth string.
     */
    private static final String IONIC_AUTH_DATA = "Ionic Security Inc";

    /**
     * Constructor.
     *
     * @param filePath path to a filesystem file containing serialized DeviceProfile objects
     * @throws IonicException on failure of the underlying runtime environment cipher to initialize
     */
    public DeviceProfilePersistorPassword(final String filePath) throws IonicException {
        super(filePath, new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Constructor.
     *
     * @param url location of a resource containing serialized DeviceProfile objects
     * @throws IonicException on failure of the underlying runtime environment cipher to initialize
     */
    public DeviceProfilePersistorPassword(final URL url) throws IonicException {
        super(url, new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Constructor.
     *
     * @param inputStream stream containing serialized DeviceProfile objects
     * @throws IonicException on failure of the underlying runtime environment cipher to initialize; read failure
     */
    public DeviceProfilePersistorPassword(final InputStream inputStream) throws IonicException {
        super(inputStream, new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Constructor.
     *
     * @throws IonicException on failure of the underlying runtime environment cipher to initialize
     */
    public DeviceProfilePersistorPassword() throws IonicException {
        super(new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Provide password used to protect the serialized byte stream containing DeviceProfile objects.
     *
     * @param password the client-supplied string used to protect the DeviceProfile objects on serialization
     * @throws IonicException on null password, or on insufficient password length (minimum 6 characters)
     */
    public final void setPassword(final String password) throws IonicException {
        final boolean isPasswordOk = ((password != null) && (password.length() >= MINIMUM_PASSWORD_LENGTH));
        SdkData.checkTrue(isPasswordOk, SdkError.ISAGENT_INVALIDVALUE, FORMAT_PASSWORD);
        this.password = password;
    }

    @Override
    protected final String getFormat() {
        return FORMAT_PASSWORD;
    }

    @Override
    protected final void initializeCipher(final String jsonHeader) throws IonicException {
        final boolean isPasswordOk = ((password != null) && (password.length() >= MINIMUM_PASSWORD_LENGTH));
        SdkData.checkTrue(isPasswordOk, SdkError.ISAGENT_INVALIDVALUE, FORMAT_PASSWORD);
        byte[] salt = new byte[0];
        if (jsonHeader != null) {
            final JsonObject jsonObject = JsonIO.readObject(Transcoder.utf8().decode(jsonHeader));
            final JsonObject extra = JsonSource.getJsonObjectNullable(jsonObject, EXTRA);
            salt = Transcoder.base64().decode(JsonSource.getString(extra, SALT));
        }
        // derive a key from the password using PBKDF2 (mimic current C++ behavior)
        final int iterations = 2000;
        cipherCast.setKey(CryptoUtils.pbkdf2ToBytes(
                Transcoder.utf8().decode(password), salt, iterations, AesCipher.KEY_BYTES));
        // set a hard-coded, known auth data
        cipherCast.setAuthData(Transcoder.utf8().decode(IONIC_AUTH_DATA));
    }

    @Override
    public final void saveAllProfiles(
            final List<DeviceProfile> profiles, final String activeProfile) throws IonicException {
        // generate new salt for this encryption
        final JsonObjectBuilder extraBuilder = Json.createObjectBuilder();
        final int saltBytes = 32;
        JsonTarget.addNotNull(extraBuilder, SALT,
                Transcoder.base64().encode(new CryptoRng().rand(new byte[saltBytes])));
        setExtra(JsonIO.write(extraBuilder.build(), false));
        // delegate file write
        super.saveAllProfiles(profiles, activeProfile);
    }

    /**
     * Ionic Secure Enrollment Profile type header field value.
     */
    public static final String FORMAT_PASSWORD = "password";
}
