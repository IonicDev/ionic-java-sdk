package com.ionic.sdk.core.hash;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provide ability to calculate message digest of arbitrary data.
 */
public final class Hash {

    /**
     * Calculate hash of input byte stream.
     *
     * @param value input to the hash function
     * @return the SHA-256 hash of the input, as a raw byte array
     */
    public byte[] sha256(final byte[] value) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
            return messageDigest.digest(value);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Calculate hash of input byte stream.
     *
     * @param is input to the hash function
     * @return the SHA-256 hash of the input, as a raw byte array
     * @throws IOException on failure reading from the stream
     */
    public byte[] sha256(final InputStream is) throws IOException {
        try {
            final byte[] bytes = new byte[HASH_BITS * HASH_BITS];
            final MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
            while (is.available() > 0) {
                final int count = is.read(bytes);
                messageDigest.update(bytes, 0, count);
            }
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Calculate hash of input byte stream.
     *
     * @param value input to the hash function
     * @return the SHA-512 hash of the input, as a raw byte array
     */
    public byte[] sha512(final byte[] value) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM_512);
            return messageDigest.digest(value);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Length in bits of default hash.
     */
    private static final int HASH_BITS = 256;

    /**
     * Label for hashing algorithm used by SDK.
     */
    public static final String ALGORITHM = "SHA-256";

    /**
     * Label for alternate hashing algorithm used by SDK.
     */
    private static final String ALGORITHM_512 = "SHA-512";
}
