package com.ionic.sdk.core.hash;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.error.IonicException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

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
            final MessageDigest messageDigest = AgentSdk.getCrypto().getMessageDigestSha256();
            return messageDigest.digest(value);
        } catch (IonicException e) {
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
            final MessageDigest messageDigest = AgentSdk.getCrypto().getMessageDigestSha256();
            while (is.available() > 0) {
                final int count = is.read(bytes);
                messageDigest.update(bytes, 0, count);
            }
            return messageDigest.digest();
        } catch (IonicException e) {
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
            final MessageDigest messageDigest = AgentSdk.getCrypto().getMessageDigestSha512();
            return messageDigest.digest(value);
        } catch (IonicException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Length in bits of default hash.
     */
    private static final int HASH_BITS = 256;

    /**
     * Length in bytes of default hash.
     */
    public static final int HASH_BYTES = HASH_BITS / Byte.SIZE;

    /**
     * Label for hashing algorithm used by SDK.
     */
    public static final String ALGORITHM = "SHA-256";

    /**
     * Label for alternate hashing algorithm used by SDK.
     */
    public static final String ALGORITHM_512 = "SHA-512";
}
