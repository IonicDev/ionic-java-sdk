package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.crypto.secretshare.SecretSharePersistor;
import com.ionic.sdk.error.IonicException;

/**
 * DeviceProfilePersistorThreshold uses an AesGcmCipher with a password derived from external data.
 *
 * @author Ionic Security
 */
public class DeviceProfilePersistorSecretShare extends DeviceProfilePersistorBase {

    /**
     * The AesGcmCipher cast of the mCipher.
     */
    private final AesGcmCipher cipherCast;

    /**
     * The Ionic auth string.
     */
    private static final String IONIC_AUTH_DATA = "Ionic Security Inc";

    /**
     * Default constructor for DeviceProfilePersistorPassword.
     *
     * @param secretSharePersistor the encapsulation of logic to maintain the share file
     * @throws IonicException when AesGcmCipher fails to initialize.
     */
    public DeviceProfilePersistorSecretShare(final SecretSharePersistor secretSharePersistor) throws IonicException {
        super(new AesGcmCipher());
        this.cipherCast = (AesGcmCipher) getCipher();
        cipherCast.setKey(CryptoUtils.hexToBin(secretSharePersistor.generateKey()));
        cipherCast.setAuthData(Transcoder.utf8().decode(IONIC_AUTH_DATA));
    }
}
