package com.ionic.sdk.core.hash;

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
     * Label for hashing algorithm used by SDK.
     */
    public static final String ALGORITHM = "SHA-256";

    /**
     * Label for alternate hashing algorithm used by SDK.
     */
    private static final String ALGORITHM_512 = "SHA-512";
}
