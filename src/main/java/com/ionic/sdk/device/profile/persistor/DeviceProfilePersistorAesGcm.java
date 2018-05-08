package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.error.IonicException;

/**
 * @author Ionic Security DeviceProfilePersistorAesGcm class.
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
     * Takes an input to a device profile, decrypts it and stores it into memory.
     *
     * @param filePath
     *            to a device profile.
     * @throws IonicException
     *             - exception that is thrown if AesGcmCipher fails to initialize.
     */
    public DeviceProfilePersistorAesGcm(final String filePath) throws IonicException {
        super(filePath, new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Default constructor for DeviceProfilePersistorAesGcm.
     *
     * @throws IonicException
     *             - exception that is thrown if AesGcmCipher fails to initialize.
     */
    public DeviceProfilePersistorAesGcm() throws IonicException {
        super(new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Getter for the Authentication data.
     *
     * @return the Authentication data.
     */
    public final byte[] getAuthData() {
        return mAuthData.clone();
    }

    /**
     * Setter for the authentication data.
     *
     * @param authData
     *            input parameter to set the new authData bytes.
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
     * @param key
     *            input parameter to set the new key bytes.
     */
    public final void setKey(final byte[] key) {
        mKeyData = key.clone();
        cipherCast.setKey(mKeyData);
    }

}
