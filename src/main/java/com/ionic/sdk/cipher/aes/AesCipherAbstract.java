package com.ionic.sdk.cipher.aes;

import com.ionic.sdk.cipher.CipherAbstract;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Base class for all AES ciphers.
 */
public abstract class AesCipherAbstract extends CipherAbstract {

    /**
     * Construct and initialize an object of this type.
     *
     * @param cipher the native Java cipher instance to wrap
     */
    public AesCipherAbstract(final Cipher cipher) {
        super(cipher);
    }

    /**
     * Set the key for this cipher.
     *
     * @param cipherKey the raw bytes of the key
     */
    public final void setKey(final byte[] cipherKey) {
        if (!Value.isEmpty(cipherKey)) {
            setKeyNative(new SecretKeySpec(cipherKey, AesCipher.ALGORITHM));
        }
    }

    /**
     * Set the key for this cipher.
     *
     * @param secretKey the {@link SecretKey} representation of the key
     * @throws IonicException on NULL input
     */
    public final void setKey(final SecretKey secretKey) throws IonicException {
        SdkData.checkNotNull(secretKey, SecretKey.class.getName());
        setKeyNative(secretKey);
    }

    /**
     * Set the key for this cipher.
     *
     * @param cipherKey the hex-encoded representation of the key bytes
     */
    public final void setKeyHex(final String cipherKey) {
        if (!Value.isEmpty(cipherKey)) {
            setKeyNative(new SecretKeySpec(Transcoder.hex().decode(cipherKey), AesCipher.ALGORITHM));
        }
    }

    /**
     * Get the required length of the AES cipher (in bytes).
     *
     * @return The required length of the AES cipher. Providing a key of any other length will result in an error
     * being returned during any encryption / decryption operation.
     */
    public static int getCipherKeyLen() {
        return AesCipher.KEY_BYTES;
    }
}
