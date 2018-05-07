package com.ionic.sdk.device.profile.persistor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import com.ionic.sdk.cipher.CipherAbstract;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.datastructures.Tuple;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.profile.DeviceFields;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.AgentErrorModuleConstants;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.json.JsonU;

/**
 * DeviceProfilePersistorBase is an abstract class. It is used to do the core
 * serialize, deserialize, encrypt, decrypt, and json parsing needed to load
 * DeviceProfiles into memory.
 *
 * @author Ionic Security
 */
public abstract class DeviceProfilePersistorBase implements ProfilePersistor {

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
     * boolean to track if profiles need to be updated.
     */
    private boolean shouldUpdateProfiles = true;

    /**
     * Abstract constructor used to initialize Device profiles from disk.
     *
     * @param filePath
     *            - the file path of a device profile file.
     * @param cipher
     *            - the cipher used to encrypt and decrypt a device profile.
     */
    public DeviceProfilePersistorBase(final String filePath, final CipherAbstract cipher) {
        mFilePath = filePath;
        mCipher = cipher;
    }

    /**
     * Abstract constructor used to initialize Device profiles from disk.
     *
     * @param cipher
     *            the cipher
     */
    public DeviceProfilePersistorBase(final CipherAbstract cipher) {
        mCipher = cipher;
    }

    /**
     * Getter for the cipher used to encrypt and decrypt a device profile file.
     *
     * @return mCipher
     */
    protected final CipherAbstract getCipher() {
        return mCipher;
    }

    /**
     * changes the file path of a device profile. This method has a sid3 effect of
     * reloading the active device profile and device profile list from the the new
     * file path.
     *
     * @param path
     *            the new file path to load device profiles from.
     * @throws IonicException on null parameter
     */
    public final void setFilePath(final String path) throws IonicException {
        if (path == null) {
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_NULL_INPUT.value());
        }
        if (!mFilePath.equals(path)) {
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
     * Interface method used as a means of retrieving the profiles available in
     * memory. Also retrieves the active device profile id as an out parameter.
     *
     * @param activeProfile
     *            an out parameter that will provide the active profile of the
     *            persistor.
     * @return the list of available device profiles on the persistor.
     * @throws IonicException
     *             decrypt or json parsing can throw a sdk exception, expect
     *             ISAGENT_PARSEFAILED, ISAGENT_MISSINGVALUE, ISAGENT_RESOURCE_NOT_FOUND,
     *             or ISCRYPTO_ERROR
     */
    @Override
    public final List<DeviceProfile> loadAllProfiles(final String[] activeProfile) throws IonicException {

        final File f = new File(mFilePath);
        if (!f.exists()) {
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_RESOURCE_NOT_FOUND.value());
        }
        if (shouldUpdateProfiles) {
            final Tuple<List<DeviceProfile>, String> profiles = loadAllProfilesFromFile(mFilePath);
            if (profiles != null) {
                mProfiles = new ArrayList<DeviceProfile>(profiles.first());
                activeDeviceProfileId = profiles.second();
            }
            shouldUpdateProfiles = false;
        }
        if (activeProfile != null && activeProfile.length >= 1) {
            activeProfile[0] = activeDeviceProfileId;
        }

        return new ArrayList<DeviceProfile>(mProfiles);
    }

    /**
     * Function that saves input parameters in memory aswell as disk.
     *
     * @param profiles
     *            - change the list of available profiles to this input parameter.
     * @param activeProfile
     *            - change the active device profile to this input parameter.
     * @throws IonicException
     *             write to disk can throw a ISAGENT_OPENFILE exception
     *             saveAllProfilesToJson can throw an ISCRYPTO_ERROR on encrypt.
     */
    @Override
    public final void saveAllProfiles(final List<DeviceProfile> profiles, final String activeProfile)
            throws IonicException {
        mProfiles = new ArrayList<DeviceProfile>(profiles);
        activeDeviceProfileId = activeProfile;

        saveAllProfilesToFile(mProfiles, activeDeviceProfileId, mFilePath, mCipher);
    }

    /**
     * Function that saves input parameters in memory as well as disk.
     *
     * @param profiles
     *            the device profiles we will serialize, encrypt, and write to disk.
     * @param activeProfile
     *            the active profile we will serialize, encrypt, and write to disk.
     * @param filePath
     *            - the file path to write a device profile file.
     * @param cipher
     *            - the cipher used to encrypt the deviceProfile.
     * @throws IonicException
     *             write to disk can throw a ISAGENT_OPENFILE exception
     *             saveAllProfilesToJson can throw an ISCRYPTO_ERROR on encrypt.
     */
    private static void saveAllProfilesToFile(final List<DeviceProfile> profiles, final String activeProfile,
            final String filePath, final CipherAbstract cipher) throws IonicException {
        final File folder = new File(filePath).getParentFile();
        if ((!folder.exists()) && (!folder.mkdirs())) {
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_OPENFILE.value());
        }
        Stream.writeToDisk(filePath, saveAllProfilesToJson(profiles, activeProfile, cipher));
    }

    /**
     * Save all profiles to json.
     *
     * @param profiles
     *            the device profiles we will serialize and encrypt
     * @param activeProfile
     *            the active profile we will serialize and encrypt
     * @param cipher
     *            - the cipher used to encrypt the deviceProfile.
     * @return the encrypted bytes
     * @throws IonicException
     *             can throw an ISCRYPTO_ERROR on encrypt.
     */
    private static byte[] saveAllProfilesToJson(final List<DeviceProfile> profiles, final String activeProfile,
            final CipherAbstract cipher) throws IonicException {

        final JsonObjectBuilder devicePersistor = Json.createObjectBuilder();
        JsonU.addNotNull(devicePersistor, DeviceFields.FIELD_ACTIVE_DEVICE_ID, activeProfile);
        final JsonArrayBuilder deviceProfiles = Json.createArrayBuilder();

        for (final DeviceProfile profile : profiles) {

            deviceProfiles.add(Json.createObjectBuilder().add(DeviceFields.FIELD_NAME, profile.getName())
                    .add(DeviceFields.FIELD_DEVICE_ID, profile.getDeviceId())
                    .add(DeviceFields.FIELD_SERVER, profile.getServer())
                    .add(DeviceFields.FIELD_AES_CD_IDC_KEY, CryptoUtils.binToHex(profile.getAesCdIdcProfileKey()))
                    .add(DeviceFields.FIELD_AES_CD_EI_KEY, CryptoUtils.binToHex(profile.getAesCdEiProfileKey()))
                    .add(DeviceFields.FIELD_CREATION_TIMESTAMP, profile.getCreationTimestampSecs()).build());
        }
        devicePersistor.add(DeviceFields.FIELD_PROFILES, deviceProfiles.build());
        final JsonObject jobject = devicePersistor.build();

        return cipher.encrypt(Transcoder.utf8().decode(jobject.toString()));
    }

    /**
     * Loads a Device profile from the file system, decrypts it and parsers the json
     * into memory. Then we use the deserialzed json to create a list Device Profile
     * objects.
     *
     * @param filePath
     *            - the file path of a device profile file.
     * @return a Tuple that holds the list of Device profiles and the id of the
     *         active profile.
     * @throws IonicException
     *             decrypt or json parsing can throw a sdk exception, expect
     *             ISAGENT_PARSEFAILED, ISAGENT_MISSINGVALUE, or ISCRYPTO_ERROR
     */
    private Tuple<List<DeviceProfile>, String> loadAllProfilesFromFile(final String filePath) throws IonicException {
        return loadAllProfilesFromFile(filePath, mCipher);
    }

    /**
     * Load all profiles from file.
     *
     * @param filePath
     *            - The file path of a device profile.
     * @param cipher
     *            - the cipher used to decrypt the deviceProfile.
     * @return a tuple of loaded profiles from disk.
     * @throws IonicException
     *             decrypt or json parsing can throw a sdk exception, expect
     *             ISAGENT_PARSEFAILED, ISAGENT_MISSINGVALUE, or ISCRYPTO_ERROR
     */
    private static Tuple<List<DeviceProfile>, String> loadAllProfilesFromFile(final String filePath,
            final CipherAbstract cipher) throws IonicException {
        final byte[] loadedFile = Stream.loadFileIntoMemory(filePath);
        return loadAllProfilesFromJson(loadedFile, cipher);
    }

    @SuppressWarnings({"checkstyle:javadocmethod"})
    /**
     * A method used to decrypt and parse a json object in order to create Device
     * profiles.
     *
     * @param cipherText
     *            The input we want to decrypt.
     * @param cipher
     *            The cipher we will use to decrypt the inputed bytes.
     * @return a Tuple that holds the list of Device profiles and the id of the
     *         active profile.
     * @throws IonicException
     *             decrypt or json parsing can throw a sdk exception, expect
     *             ISAGENT_PARSEFAILED or ISCRYPTO_ERROR
     * @throws javax.json.JsonException on problems parsing the persistor bytes
     */
    private static Tuple<List<DeviceProfile>, String> loadAllProfilesFromJson(final byte[] cipherText,
            final CipherAbstract cipher) throws IonicException {
        final List<DeviceProfile> profiles = new ArrayList<DeviceProfile>();
        String activeDeviceId = null;
        final byte[] json = cipher.decrypt(cipherText);

        final JsonObject jsonObj = JsonU.getJsonObject(Transcoder.utf8().encode(json));

        if (jsonObj.getValueType() != ValueType.OBJECT) {
            Logger.getLogger(DeviceProfilePersistorBase.class.getName()).log(Level.SEVERE,
                    "JSON does not represent an object");
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_PARSEFAILED.value());
        }

        try {
            activeDeviceId = JsonU.getString(jsonObj, DeviceFields.FIELD_ACTIVE_DEVICE_ID);

        } catch (final NullPointerException npe) {
            // do NOT fail here since this field is optional
            Logger.getLogger(DeviceProfilePersistorBase.class.getName()).log(Level.WARNING, "JSON is missing a field "
                    + DeviceFields.FIELD_ACTIVE_DEVICE_ID + ". It has been skipped since it is optional.");
        }

        // read device profiles array
        final JsonValue jsonProfilesArray = jsonObj.get(DeviceFields.FIELD_PROFILES);
        if (jsonProfilesArray.getValueType() != ValueType.ARRAY) {
            Logger.getLogger(DeviceProfilePersistorBase.class.getName()).log(Level.SEVERE,
                    "JSON is missing a field " + DeviceFields.FIELD_PROFILES + ".");
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_MISSINGVALUE.value());
        }

        final JsonArray jsonProfiles = (JsonArray) jsonProfilesArray;
        for (final JsonObject value : jsonProfiles.getValuesAs(JsonObject.class)) {
            final DeviceProfile profile = new DeviceProfile();

            String deviceName;
            String deviceId;
            String server;
            String aesCdIdcKeyHex;
            String aesCdEiKeyHex;
            int timeStamp;
            try {
                deviceName = JsonU.getString(value, DeviceFields.FIELD_NAME);
                deviceId = JsonU.getString(value, DeviceFields.FIELD_DEVICE_ID);
                server = JsonU.getString(value, DeviceFields.FIELD_SERVER);
                aesCdIdcKeyHex = JsonU.getString(value, DeviceFields.FIELD_AES_CD_IDC_KEY);
                aesCdEiKeyHex = JsonU.getString(value, DeviceFields.FIELD_AES_CD_EI_KEY);
                timeStamp = JsonU.getInt(value, DeviceFields.FIELD_CREATION_TIMESTAMP);
            } catch (final NullPointerException npe) {
                // NOTE: do NOT fail here, just skip the loading of this invalid profile and
                // keep moving
                Logger.getLogger(DeviceProfilePersistorBase.class.getName()).log(Level.WARNING,
                        "JSON device profile object is missing one or more fields. Profile has been skipped.");
                continue;

            }

            profile.setName(deviceName);
            profile.setDeviceId(deviceId);
            profile.setServer(server);
            profile.setAesCdEiProfileKey(CryptoUtils.hexToBin(aesCdEiKeyHex));
            profile.setAesCdIdcProfileKey(CryptoUtils.hexToBin(aesCdIdcKeyHex));
            profile.setCreationTimestampSecs(timeStamp);
            profiles.add(profile);
        }

        return new Tuple<List<DeviceProfile>, String>(profiles, activeDeviceId);
    }
}
