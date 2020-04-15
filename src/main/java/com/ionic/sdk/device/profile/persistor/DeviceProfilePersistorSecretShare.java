package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.crypto.secretshare.SecretSharePersistor;
import com.ionic.sdk.error.IonicException;

/**
 * DeviceProfilePersistorSecretShare provides for serialization to and deserialization from an accessible filesystem
 * file of a set of {@link com.ionic.sdk.device.profile.DeviceProfile}.  These DeviceProfile objects allow for service
 * interactions with 1..n Ionic Machina service keyspaces, which broker authenticated cryptography key transactions.
 * <p>
 * DeviceProfilePersistorSecretShare is a persistor that uses the AesGcmCipher, with an AES key derived from the
 * process environment.  It allows for secure storage of {@link ProfilePersistor} data without the need of a separately
 * persisted password or AES key.  The derivation of the AES key may incorporate any data available to the Java
 * process.  This might include:
 * <ul>
 * <li>the Java Runtime Environment system property set</li>
 * <li>the process environment</li>
 * <li>the OS host name</li>
 * <li>the OS network interfaces</li>
 * <li>the OS filesystem (including file content)</li>
 * </ul>
 * <p>
 * See <a href='https://github.com/IonicDev/sample-tomcat-password-2-secretshare'
 * target='_blank'>github.com/IonicDev</a> for a
 * code sample using DeviceProfilePersistorSecretShare.
 * <p>
 * See <a href='https://dev.ionic.com/sdk/features' target='_blank'>Machina Developers</a> for
 * more information on the available {@link ProfilePersistor} implementations.
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

    @Override
    protected final String getFormat() {
        return FORMAT_SECRET_SHARE;
    }

    /**
     * Ionic Secure Enrollment Profile type header field value.
     */
    public static final String FORMAT_SECRET_SHARE = "secretshare";
}
