package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.BytesTranscoder;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.rng.RNG;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.IonicException;

/**
 *
 * @author Ionic Security DeviceProfilePersistorPassword class that uses the
 *         AesGcmCipher with a password hashed with pbkdf2.
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
     * takes an input to a device profile, decrypts it and stores it into memory.
     *
     * @param filePath
     *            to a device profile
     * @throws IonicException
     *             when AesGcmCipher fails to initialize.
     */
    public DeviceProfilePersistorPassword(final String filePath) throws IonicException {
        super(filePath, new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * Default constructor for DeviceProfilePersistorPassword.
     *
     * @throws IonicException
     *             when AesGcmCipher fails to initialize.
     */
    public DeviceProfilePersistorPassword() throws IonicException {
        super(new AesGcmCipher());
        cipherCast = (AesGcmCipher) getCipher();
    }

    /**
     * setPassword hashes an inputed password to use as a key.
     *
     * @param password
     *            we want to hash
     * @throws IonicException
     *             Exception thrown if the hash fails.
     */
    public final void setPassword(final String password) throws IonicException {
        final BytesTranscoder utConverter = Transcoder.utf8();

        final byte[] salt = RNG.fill(new byte[CryptoUtils.SALT_BYTES]);
        final int iterations = 2000;

        // derive a key from the password using PBKDF2
        final byte[] hashBytes = CryptoUtils.pbkdf2ToBytes(
                utConverter.decode(password), salt, iterations, CryptoUtils.HASH_BYTES);

        cipherCast.setKey(hashBytes);
        // set a hard-coded, known auth data
        cipherCast.setAuthData(utConverter.decode(IONIC_AUTH_DATA));
    }
}
