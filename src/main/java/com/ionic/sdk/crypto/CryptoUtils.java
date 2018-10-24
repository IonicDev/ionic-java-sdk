package com.ionic.sdk.crypto;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.hash.Hash;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

/**
 * Utility class containing various useful functions for a cryptographic programming context.
 */
public final class CryptoUtils {

    /**
     * SecretKeyFactory Algorithm name for PBKDF2.  Leave this available in case we figure out a generic solution.
     */
    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Algorithm name for message authentication code.
     */
    public static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Length in bytes of SHA256 hashes.
     */
    public static final int SHA256_DIGEST_SIZE = 32;

    /**
     * Length in bytes of SHA512 hashes.
     */
    public static final int SHA512_DIGEST_SIZE = 64;

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private CryptoUtils() {
    }

    /**
     * Decode a Base64-encoded buffer into a binary data buffer. This function decodes a Base64-encoded buffer into a
     * binary data buffer.
     *
     * @param buffer Base64-encoded input buffer.
     * @return The binary output buffer.
     * @throws IonicException Throws SdkException with an error code from CryptoErrorModuleConstants on error.
     */
    public static byte[] base64ToBin(final String buffer) throws IonicException {
        try {
            return Transcoder.base64().decode(buffer);
        } catch (RuntimeException e) {
            throw new IonicException(SdkError.ISCRYPTO_BAD_INPUT, e);
        }
    }

    /**
     * Base64-encode a binary data buffer. This function Base64-encodes a binary data buffer into a string buffer.
     *
     * @param buffer The input binary data buffer.
     * @return The Base64-encoded output buffer.
     */
    public static String binToBase64(final byte[] buffer) {
        return Transcoder.base64().encode(buffer);
    }

    /**
     * Hex-encode a binary data buffer. This function Hex-encodes a binary data buffer into a string buffer.
     *
     * @param buffer The input binary data buffer.
     * @return The Hex-encoded output buffer.
     */
    public static String binToHex(final byte[] buffer) {
        return Transcoder.hex().encode(buffer);
    }

    /**
     * Computes a PBKDF2 (SHA-256) hash of the input value as a binary data buffer, optionally including salt value.
     * <p>
     * JCE version does not work, as it uses PKCS12 encoding.  So we must use the BC classes explicitly.
     *
     * @param value      The input value to be hashed.
     * @param salt       The input salt value to be used in the hash. Can be empty to avoid using salt.
     * @param iterations Number of PBKDF2 iterations to perform.
     * @param hashLength The desired length in bytes of the hash to produce.
     * @return The hash output byte buffer.
     */
    private static byte[] pbkdf2ToBytesInternal(final byte[] value, final byte[] salt,
                                                final long iterations, final long hashLength) {
        //AgentSdk.initialize(null);  // if we switch back to use of JCE interface, call initialize()
        final PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        generator.init(value, salt, (int) iterations);
        return ((KeyParameter) generator.generateDerivedParameters((int) hashLength * Byte.SIZE)).getKey();
    }

    /**
     * Computes a PBKDF2 (SHA-256) hash of the input value as a binary data buffer, optionally including salt value.
     *
     * @param value      The input value to be hashed.
     * @param salt       The input salt value to be used in the hash. Can be empty to avoid using salt.
     * @param iterations Number of PBKDF2 iterations to perform.
     * @param hashLength The desired length in bytes of the hash to produce.
     * @return The hash output byte buffer.
     */
    public static byte[] pbkdf2ToBytes(final byte[] value, final byte[] salt,
                                       final long iterations, final long hashLength) {
        return CryptoUtils.pbkdf2ToBytesInternal(value, salt, iterations, hashLength);
        // NoSuchAlgorithmException should never happen since PBKDF2_ALGORITHM is correct
    }

    /**
     * Computes a PBKDF2 (SHA-256) hash of the input value as a Hex-encoded buffer, optionally including salt value.
     *
     * @param value      The input value to be hashed.
     * @param salt       The input salt value to be used in the hash.  Can be empty to avoid using salt.
     * @param iterations Number of PBKDF2 iterations to perform.
     * @param hashLength The desired length in bytes of the hash to produce.
     * @return The hash output as a Hex-encoded buffer.
     * @throws IonicException Throws SdkException with an error code from CryptoErrorModuleConstants on error.
     */
    public static String pbkdf2ToHexString(final byte[] value, final byte[] salt,
                                           final long iterations, final long hashLength) throws IonicException {
        return Transcoder.hex().encode(pbkdf2ToBytesInternal(value, salt, iterations, hashLength));
    }

    /**
     * Computes a PBKDF2 (SHA-256) hash of the input value as a Base64-encoded buffer, optionally including salt value.
     *
     * @param value      The input value to be hashed.
     * @param salt       The input salt value to be used in the hash.  Can be empty to avoid using salt.
     * @param iterations Number of PBKDF2 iterations to perform.
     * @param hashLength The desired length in bytes of the hash to produce.
     * @return The hash output as a Base64-encoded buffer.
     * @throws IonicException Throws SdkException with an error code from CryptoErrorModuleConstants on error.
     */
    public static String pbkdf2ToBase64String(final byte[] value, final byte[] salt,
                                              final long iterations, final long hashLength) throws IonicException {
        return Transcoder.base64().encode(pbkdf2ToBytesInternal(value, salt, iterations, hashLength));
    }

    /**
     * Decode a Hex-encoded buffer into a binary data buffer. This function decodes a Hex-encoded buffer into a binary
     * data buffer.
     *
     * @param buffer Hex-encoded input buffer.
     * @return The binary output buffer.
     * @throws IonicException Throws SdkException with an error code from CryptoErrorModuleConstants on error.
     */
    public static byte[] hexToBin(final String buffer) throws IonicException {
        try {
            return Transcoder.hex().decode(buffer);
        } catch (IllegalArgumentException e) {
            throw new IonicException(SdkError.ISCRYPTO_BAD_INPUT, e);
        }
    }

    /**
     * Compute a MAC (message authentication code) for the input value, given the input key.
     *
     * @param message the message for which the MAC should be generated
     * @param key     the key bytes used to generate the MAC
     * @return the message authentication code
     * @throws IonicException on failure to generate the MAC
     */
    private static byte[] hmacSHA256Internal(final byte[] message, final byte[] key) throws IonicException {
        try {
            AgentSdk.initialize();
            final Mac hmacSHA256 = Mac.getInstance(HMAC_ALGORITHM);
            final SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_ALGORITHM);
            hmacSHA256.init(keySpec);
            return hmacSHA256.doFinal(message);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * Compute a MAC (message authentication code) for the input value, given the input key.
     *
     * @param message the message for which the MAC should be generated
     * @param key     the key bytes used to generate the MAC
     * @return the message authentication code
     * @throws IonicException on failure to generate the MAC
     */
    public static byte[] hmacSHA256(final byte[] message, final byte[] key) throws IonicException {
        return hmacSHA256Internal(message, key);
    }

    /**
     * Compute the base64 representation of the MAC (message authentication code) for the input value, given the input
     * key.
     *
     * @param message the message for which the MAC should be generated
     * @param key     the key bytes used to generate the MAC
     * @return the message authentication code
     * @throws IonicException on failure to generate the MAC
     */
    public static String hmacSHA256Base64(final byte[] message, final byte[] key) throws IonicException {
        return Transcoder.base64().encode(hmacSHA256Internal(message, key));
    }

    /**
     * Calculate hash of input byte stream.
     *
     * @param message input to the hash function
     * @return the SHA-256 hash of the input, as a raw byte array
     */
    public static byte[] sha256ToBytes(final byte[] message) {
        return new Hash().sha256(message);
    }

    /**
     * Calculate hash of input byte stream.
     *
     * @param message input to the hash function
     * @return the SHA-256 hash of the input, as the hex representation of the hash byte array
     */
    public static String sha256ToHexString(final byte[] message) {
        return Transcoder.hex().encode(new Hash().sha256(message));
    }

    /**
     * Calculate hash of input byte stream.
     *
     * @param message input to the hash function
     * @return the SHA-512 hash of the input, as a raw byte array
     */
    public static byte[] sha512ToBytes(final byte[] message) {
        return new Hash().sha512(message);
    }

    /**
     * Calculate hash of input byte stream.
     *
     * @param message input to the hash function
     * @return the SHA-512 hash of the input, as the hex representation of the hash byte array
     */
    public static String sha512ToHexString(final byte[] message) {
        return Transcoder.hex().encode(new Hash().sha512(message));
    }
}
