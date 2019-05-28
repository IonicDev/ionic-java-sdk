package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.IonicException;

import java.io.InputStream;
import java.net.URL;

/**
 * DeviceProfilePersistorPassword is a persistor that uses the AesGcmCipher with a user-supplied password
 * hashed with pbkdf2.
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
 */
public class DeviceProfilePersistorPassword extends DeviceProfilePersistorBase {

    /**
     * The AesGcmCipher cast of the mCipher.
     */
    private final AesGcmCipher cipherCast;

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
     * @throws IonicException on cryptography initialization failures; bad input; cryptography operation failures
     */
    public final void setPassword(final String password) throws IonicException {
        // derive a key from the password using PBKDF2 (mimic current C++ behavior)
        final int iterations = 2000;
        cipherCast.setKey(CryptoUtils.pbkdf2ToBytes(
                Transcoder.utf8().decode(password), new byte[0], iterations, AesCipher.KEY_BYTES));
        // set a hard-coded, known auth data
        cipherCast.setAuthData(Transcoder.utf8().decode(IONIC_AUTH_DATA));
    }
}
