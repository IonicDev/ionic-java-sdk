package com.ionic.sdk.cipher.aes;

import com.ionic.sdk.cipher.CipherAbstract;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.rng.CryptoRng;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.crypto.jce.CryptoAbstract;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

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

    /**
     * Allow alternate IV generation algorithms (default is to use random data).
     *
     * @param plainText input to IV generation
     * @return the IV to be used
     * @throws IonicException on cryptography errors
     */
    protected final byte[] getIV(final byte[] plainText) throws IonicException {
        final String ivAlgorithm = getMetadata(AesCipher.IV_ALGORITHM);
        return (CryptoAbstract.HMAC_ALGORITHM.equals(ivAlgorithm))
                ? Arrays.copyOf(getHmacIV(plainText), AesCipher.SIZE_IV)
                : new CryptoRng().rand(new byte[AesCipher.SIZE_IV]);
    }

    /**
     * Generate IV using SQL Server deterministic encryption IV generation strategy.
     *
     * @param plainText input to IV generation
     * @return the IV to be used
     * @throws IonicException on cryptography errors
     */
    private byte[] getHmacIV(final byte[] plainText) throws IonicException {
        // https://docs.microsoft.com/en-us/sql/relational-databases/security/encryption/always-encrypted-cryptography
        final String input = String.format(AesCipher.PATTERN_HMAC, CryptoAbstract.HMAC_ALGORITHM, AesCipher.KEY_BITS);
        final byte[] ivKey = hmacSHA256(Transcoder.utf8().decode(input));
        return CryptoUtils.hmacSHA256(plainText, ivKey);
    }
}
