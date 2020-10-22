package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.CipherAbstract;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.datastructures.Tuple;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.core.vm.VM;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.device.profile.DeviceFields;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.json.JsonTarget;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DeviceProfilePersistorBase is an abstract class. It is used to do the core
 * serialize, deserialize, encrypt, decrypt, and json parsing needed to load
 * DeviceProfiles into memory.
 *
 * @author Ionic Security
 */
public abstract class DeviceProfilePersistorBase implements ProfilePersistor {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The list of profiles loaded into memory from disk.
     */
    private ArrayList<DeviceProfile> mProfiles;

    /**
     * The id of the active device profile.
     */
    private String activeDeviceProfileId = "";

    /**
     * The cipher used to encrypt and decrypt a device profile file.
     */
    private final CipherAbstract mCipher;

    /**
     * The file path of a loaded device profile.
     */
    private String mFilePath = "";

    /**
     * The URL of a device profile resource.
     */
    private URL mUrl;

    /**
     * The data version of the payload data in the file.  Version "1.1" includes a json header with metadata
     * describing the file content.
     */
    private String version;

    /**
     * Additional settings associated with individual instances of {@link ProfilePersistor} data files.
     */
    private String extra;

    /**
     * The bytes cached from an input stream if the {@link InputStream} constructor is used.  These are cached to:
     * <ol>
     * <li>immediately use and release the InputStream reference</li>
     * <li>preserve the existing semantics of construction and the "loadAllProfiles()" API call</li>
     * </ol>
     * <p>
     * They are preserved when the API {@link #loadAllProfiles(String[])} is called, and may be loaded multiple
     * times.  On call of the API {@link #saveAllProfiles(List, String)}, these bytes are cleared, and the filesystem
     * path becomes the persistence location of this {@link ProfilePersistor}.
     */
    private byte[] inputStreamBytes;

    /**
     * boolean to track if profiles need to be updated.
     */
    private boolean shouldUpdateProfiles = true;

    /**
     * Constructor.
     *
     * @param filePath path to a filesystem file containing serialized DeviceProfile objects
     * @param cipher   the cipher used to protect the serialized form of the DeviceProfile objects
     */
    public DeviceProfilePersistorBase(final String filePath, final CipherAbstract cipher) {
        mFilePath = filePath;
        mUrl = null;
        inputStreamBytes = null;
        mCipher = cipher;
    }

    /**
     * Constructor.
     *
     * @param url    location of a resource containing serialized DeviceProfile objects
     * @param cipher the cipher used to protect the serialized form of the DeviceProfile objects
     */
    public DeviceProfilePersistorBase(final URL url, final CipherAbstract cipher) {
        mFilePath = null;
        mUrl = url;
        inputStreamBytes = null;
        mCipher = cipher;
    }

    /**
     * Constructor.
     *
     * @param inputStream stream containing serialized DeviceProfile objects
     * @param cipher      the cipher used to protect the serialized form of the DeviceProfile objects
     * @throws IonicException on stream read failure
     */
    public DeviceProfilePersistorBase(final InputStream inputStream, final CipherAbstract cipher)
            throws IonicException {
        mFilePath = null;
        mUrl = null;
        SdkData.checkTrue((inputStream != null), SdkError.ISAGENT_NULL_INPUT);
        inputStreamBytes = DeviceUtils.read(inputStream);
        mCipher = cipher;
    }

    /**
     * Constructor.
     *
     * @param cipher the cipher used to protect the serialized form of the DeviceProfile objects
     */
    public DeviceProfilePersistorBase(final CipherAbstract cipher) {
        mCipher = cipher;
    }

    /**
     * Getter for the cipher used to encrypt and decrypt a device profile file.
     *
     * @return the cipher object
     */
    protected final CipherAbstract getCipher() {
        return mCipher;
    }

    /**
     * @return the format type name of the {@link ProfilePersistor} in use
     */
    protected abstract String getFormat();

    /**
     * @return the format version of the persisted data
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the format version of the persisted data.
     *
     * @param version the format version of the persisted data
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * Set the extra parameters associated with the persisted data.  (Some format types require
     * additional configuration.)
     *
     * @param extra the extra configuration parameters associated with the persisted data, in JSON format
     */
    protected void setExtra(final String extra) {
        this.extra = extra;
    }

    /**
     * Initialize the cipher associated with the {@link ProfilePersistor}.
     *
     * @param json (optional) JSON content prepended to the document, containing cipher configuration parameters
     * @throws IonicException on cryptography initialization failures; bad input; cipher expectation failures;
     * cryptography operation failures
     */
    protected void initializeCipher(final String json) throws IonicException {
    }

    /**
     * Update the filesystem location of the serialized DeviceProfile objects. This method also sets an internal flag
     * indicating the need to reload the active device profile and device profile list from the the new file path.
     *
     * @param path the new file path for load / save of device profiles
     * @throws IonicException on null parameter input
     */
    public final void setFilePath(final String path) throws IonicException {
        if (path == null) {
            throw new IonicException(SdkError.ISAGENT_NULL_INPUT);
        }
        if ((mFilePath == null) || (!mFilePath.equals(path))) {
            mFilePath = path;
            shouldUpdateProfiles = true;
        }
    }

    /**
     * Getter for the current file path.
     *
     * @return the current file path
     */
    public final String getFilePath() {
        return mFilePath;
    }

    /**
     * Interface method used as a means of retrieving the serialized DeviceProfile objects. Also retrieves the
     * active device profile id as an out parameter.
     *
     * @param activeProfile an out parameter that will provide the active profile of the persistor
     * @return the list of available device profiles on the persistor
     * @throws IonicException decrypt or json parsing can throw a sdk exception, expect
     *                        ISAGENT_PARSEFAILED, ISAGENT_MISSINGVALUE, ISAGENT_RESOURCE_NOT_FOUND,
     *                        or ISCRYPTO_ERROR
     */
    @Override
    @SuppressWarnings({"checkstyle:designforextension"})  // extended in Ionic/addon/dpapi
    public List<DeviceProfile> loadAllProfiles(final String[] activeProfile) throws IonicException {
        final File f = Value.isEmpty(mFilePath) ? null : new File(mFilePath);
        // if file is defined, it must exist on the filesystem
        SdkData.checkTrue((f == null) || f.exists(), SdkError.ISAGENT_RESOURCE_NOT_FOUND);
        // must have a source for the serialized DeviceProfile bytes
        SdkData.checkTrue((f != null) || (mUrl != null) || (inputStreamBytes != null),
                SdkError.ISAGENT_RESOURCE_NOT_FOUND);
        if (shouldUpdateProfiles) {
            final Tuple<List<DeviceProfile>, String> profiles = (f != null)
                    ? loadAllProfilesFromFile(mFilePath) : (mUrl != null)
                    ? loadAllProfilesFromURL(mUrl) : (inputStreamBytes != null)
                    ? loadAllProfilesFromJson(InputStream.class.getSimpleName(), inputStreamBytes, mCipher)
                    : null;
            SdkData.checkNotNull(profiles, DeviceProfile.class.getName());
            mProfiles = new ArrayList<DeviceProfile>(profiles.first());
            activeDeviceProfileId = profiles.second();
            shouldUpdateProfiles = false;
        }
        if (activeProfile != null && activeProfile.length >= 1) {
            activeProfile[0] = activeDeviceProfileId;
        }

        return new ArrayList<DeviceProfile>(mProfiles);
    }

    /**
     * Function that saves input parameters in memory as well as disk.
     *
     * @param profiles      change the list of available profiles to this input parameter
     * @param activeProfile change the active device profile to this input parameter
     * @throws IonicException write to disk can throw a ISAGENT_OPENFILE exception
     *                        saveAllProfilesToJson can throw an ISCRYPTO_ERROR on encrypt
     */
    @Override
    @SuppressWarnings({"checkstyle:designforextension"})  // extended in Ionic/addon/dpapi
    public void saveAllProfiles(final List<DeviceProfile> profiles, final String activeProfile) throws IonicException {
        mProfiles = new ArrayList<DeviceProfile>(profiles);
        activeDeviceProfileId = activeProfile;

        saveAllProfilesToFile(mProfiles, activeDeviceProfileId, mFilePath, mCipher);
        inputStreamBytes = null;  // inputStreamBytes are discarded after being persisted to filesystem
    }

    /**
     * Function that saves input parameters in memory as well as disk.
     *
     * @param profiles      the device profiles we will serialize, encrypt, and write to disk
     * @param activeProfile the active profile we will serialize, encrypt, and write to disk
     * @param filePath      the file path to write a device profile file
     * @param cipher        the cipher used to encrypt the deviceProfile
     * @throws IonicException write to disk can throw a ISAGENT_OPENFILE exception
     *                        saveAllProfilesToJson can throw an ISCRYPTO_ERROR on encrypt
     */
    private void saveAllProfilesToFile(
            final List<DeviceProfile> profiles, final String activeProfile,
            final String filePath, final CipherAbstract cipher) throws IonicException {
        // in order to save, file path must be set
        SdkData.checkTrue(!Value.isEmpty(filePath), SdkError.ISAGENT_OPENFILE);
        // in order to save, file path parent folder must exist
        final File folderParent = new File(filePath).getParentFile();
        final File folder = (folderParent == null) ? new File(System.getProperty(VM.Sys.USER_DIR)) : folderParent;
        SdkData.checkTrue((folder.exists() || folder.mkdirs()), SdkError.ISAGENT_OPENFILE);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (VERSION_1_1.equals(version)) {
            final String json = createPersistor11JsonHeader();
            DeviceUtils.write(os, Transcoder.utf8().decode(json));
            DeviceUtils.write(os, Transcoder.utf8().decode(DeviceProfileSerializer.HEADER_JSON_DELIMITER));
            initializeCipher(json);
        } else {
            initializeCipher(null);
        }
        DeviceUtils.write(os, saveAllProfilesToJson(profiles, activeProfile, cipher));
        Stream.writeToDisk(filePath, os.toByteArray());
    }

    /**
     * Fabricate the JSON header to be persisted with the Secure Enrollment Profile data.  Version 1.1 of
     * the {@link ProfilePersistor} data formats include a JSON header that specifies metadata associated with the
     * file content.
     *
     * @return a String containing serialized JSON, specifying metadata associated with the file
     * @throws IonicException on failure constructing the JSON header
     */
    private String createPersistor11JsonHeader() throws IonicException {
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        JsonTarget.addNotNull(objectBuilder, EXTRA,
                JsonIO.readObjectNotNull(Transcoder.utf8().decode(this.extra)));
        JsonTarget.addNotNull(objectBuilder, FILE_TYPE_ID, FILE_TYPE_DEVICE_PROFILES);
        JsonTarget.addNotNull(objectBuilder, FORMAT, getFormat());
        JsonTarget.addNotNull(objectBuilder, VERSION, VERSION_1_1);
        return JsonIO.write(objectBuilder.build(), false);
    }

    /**
     * Save all profiles to json.
     *
     * @param profiles      the device profiles we will serialize and encrypt
     * @param activeProfile the active profile we will serialize and encrypt
     * @param cipher        the cipher used to encrypt the deviceProfile
     * @return the encrypted bytes
     * @throws IonicException can throw an ISCRYPTO_ERROR on encrypt
     */
    protected static byte[] saveAllProfilesToJson(final List<DeviceProfile> profiles, final String activeProfile,
                                                  final CipherAbstract cipher) throws IonicException {

        final JsonObjectBuilder devicePersistor = Json.createObjectBuilder();
        JsonTarget.addNotNull(devicePersistor, DeviceFields.FIELD_ACTIVE_DEVICE_ID, activeProfile);
        final JsonArrayBuilder deviceProfiles = Json.createArrayBuilder();

        for (final DeviceProfile profile : profiles) {
            final String keyHexIDC = CryptoUtils.binToHex(profile.getAesCdIdcProfileKey());
            final String keyHexEI = CryptoUtils.binToHex(profile.getAesCdEiProfileKey());
            final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            JsonTarget.addNotNull(objectBuilder, DeviceFields.FIELD_NAME, profile.getName());
            JsonTarget.addNotNull(objectBuilder, DeviceFields.FIELD_DEVICE_ID, profile.getDeviceId());
            JsonTarget.addNotNull(objectBuilder, DeviceFields.FIELD_SERVER, profile.getServer());
            JsonTarget.addNotNull(objectBuilder, DeviceFields.FIELD_AES_CD_IDC_KEY, keyHexIDC);
            JsonTarget.addNotNull(objectBuilder, DeviceFields.FIELD_AES_CD_EI_KEY, keyHexEI);
            JsonTarget.add(objectBuilder, DeviceFields.FIELD_CREATION_TIMESTAMP, profile.getCreationTimestampSecs());
            JsonTarget.addNotNull(deviceProfiles, objectBuilder.build());
        }
        JsonTarget.addNotNull(devicePersistor, DeviceFields.FIELD_PROFILES, deviceProfiles.build());
        final JsonObject jobject = devicePersistor.build();

        return cipher.encrypt(Transcoder.utf8().decode(JsonIO.write(jobject, false)));
    }

    /**
     * Loads a Device profile from the file system, decrypts it and parses the json
     * into memory. Then we use the deserialized json to create a list of Device Profile
     * objects.
     *
     * @param filePath the file path of a device profile file
     * @return a Tuple that holds the list of Device profiles and the id of the active profile
     * @throws IonicException decrypt or json parsing can throw a sdk exception, expect
     *                        ISAGENT_PARSEFAILED, ISAGENT_MISSINGVALUE, or ISCRYPTO_ERROR
     */
    private Tuple<List<DeviceProfile>, String> loadAllProfilesFromFile(final String filePath) throws IonicException {
        final byte[] cipherText = Stream.loadFileIntoMemory(filePath);
        return loadAllProfilesFromJson(filePath, cipherText, mCipher);
    }

    /**
     * Loads a Device profile from a URL, decrypts it and parses the json
     * into memory. Then we use the deserialized json to create a list of Device Profile
     * objects.
     *
     * @param url the URL of a device profile resource
     * @return a Tuple that holds the list of Device profiles and the id of the active profile
     * @throws IonicException decrypt or json parsing can throw a sdk exception, expect
     *                        ISAGENT_PARSEFAILED, ISAGENT_MISSINGVALUE, or ISCRYPTO_ERROR
     */
    private Tuple<List<DeviceProfile>, String> loadAllProfilesFromURL(final URL url) throws IonicException {
        final byte[] cipherText = DeviceUtils.read(url);
        return loadAllProfilesFromJson(url.toExternalForm(), cipherText, mCipher);
    }

    /**
     * A method used to decrypt and parse a json object in order to create Device
     * profiles.
     *
     * @param resource   The label to associate with the input
     * @param cipherText The input we want to decrypt
     * @param cipher     The cipher we will use to decrypt the inputted bytes
     * @return a Tuple that holds the list of Device profiles and the id of the active profile
     * @throws IonicException decrypt or json parsing can throw a sdk exception, expect
     *                        ISAGENT_PARSEFAILED or ISCRYPTO_ERROR
     */
    protected Tuple<List<DeviceProfile>, String> loadAllProfilesFromJson(final String resource,
            final byte[] cipherText, final CipherAbstract cipher) throws IonicException {
        SdkData.checkTrue(cipherText != null, SdkError.ISAGENT_NULL_INPUT, resource);
        logger.fine(String.format("ProfilePersistor, resource=[%s], hash=[%s], size=[%d]",
                resource, CryptoUtils.sha256ToHexString(cipherText), cipherText.length));
        // handle ProfilePersistor v1.1 JSON header
        final DeviceProfileSerializer serializer = new DeviceProfileSerializer(cipherText);
        initializeCipher(serializer.getHeader());
        final List<DeviceProfile> profiles = new ArrayList<DeviceProfile>();
        String activeDeviceId = null;
        final byte[] json = cipher.decrypt(serializer.getBody());

        final JsonObject jsonObj = JsonIO.readObject(json);

        try {
            activeDeviceId = JsonSource.getString(jsonObj, DeviceFields.FIELD_ACTIVE_DEVICE_ID);

        } catch (final NullPointerException npe) {
            // do NOT fail here since this field is optional
            logger.warning("JSON is missing a field "
                    + DeviceFields.FIELD_ACTIVE_DEVICE_ID + ". It has been skipped since it is optional.");
        }

        // read device profiles array
        final JsonArray jsonProfiles = JsonSource.getJsonArray(jsonObj, DeviceFields.FIELD_PROFILES);
        for (JsonValue jsonProfile : jsonProfiles) {
            final JsonObject value = JsonSource.toJsonObject(jsonProfile, DeviceFields.FIELD_PROFILES);
            final DeviceProfile profile = new DeviceProfile();

            final String deviceName;
            final String deviceId;
            final String server;
            final String aesCdIdcKeyHex;
            final String aesCdEiKeyHex;
            final int timeStamp;
            try {
                deviceName = JsonSource.getString(value, DeviceFields.FIELD_NAME);
                deviceId = JsonSource.getString(value, DeviceFields.FIELD_DEVICE_ID);
                server = JsonSource.getString(value, DeviceFields.FIELD_SERVER);
                aesCdIdcKeyHex = JsonSource.getString(value, DeviceFields.FIELD_AES_CD_IDC_KEY);
                aesCdEiKeyHex = JsonSource.getString(value, DeviceFields.FIELD_AES_CD_EI_KEY);
                timeStamp = JsonSource.getInt(value, DeviceFields.FIELD_CREATION_TIMESTAMP);
            } catch (final NullPointerException npe) {
                // NOTE: do NOT fail here, just skip the loading of this invalid profile and
                // keep moving
                logger.warning("JSON device profile object is missing one or more fields. Profile has been skipped.");
                continue;

            }

            profile.setName(deviceName);
            profile.setDeviceId(deviceId);
            profile.setServer(server);
            profile.setAesCdEiProfileKey(CryptoUtils.hexToBin(aesCdEiKeyHex));
            profile.setAesCdIdcProfileKey(CryptoUtils.hexToBin(aesCdIdcKeyHex));
            profile.setCreationTimestampSecs(timeStamp);
            try {
                profile.isValid();
            } catch (IonicException e) {
                throw new IonicException(SdkError.ISAGENT_LOAD_PROFILES_FAILED, e);
            }
            profiles.add(profile);
        }

        return new Tuple<List<DeviceProfile>, String>(profiles, activeDeviceId);
    }

    /**
     * Ionic Secure Enrollment Profile type header field value.
     */
    public static final String VERSION_1_0 = "1.0";

    /**
     * Ionic Secure Enrollment Profile type header field value.
     */
    public static final String VERSION_1_1 = "1.1";

    /**
     * Ionic Secure Enrollment Profile type header field name.
     */
    public static final String EXTRA = "extra";

    /**
     * Ionic Secure Enrollment Profile type header field name.
     */
    public static final String FILE_TYPE_ID = "fileTypeId";

    /**
     * Ionic Secure Enrollment Profile type header field value.
     */
    public static final String FILE_TYPE_DEVICE_PROFILES = "ionic-device-profiles";

    /**
     * Ionic Secure Enrollment Profile type header field name.
     */
    public static final String FORMAT = "format";

    /**
     * Ionic Secure Enrollment Profile type header field name.
     */
    public static final String SALT = "salt";

    /**
     * Ionic Secure Enrollment Profile type header field name.
     */
    public static final String VERSION = "version";
}
