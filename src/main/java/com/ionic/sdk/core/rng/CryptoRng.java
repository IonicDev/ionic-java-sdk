package com.ionic.sdk.core.rng;

import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Utility functions relating to the JRE random number generator facility.
 */
public class CryptoRng {

    /**
     * The source of randomness for this object.
     */
    private final Random random;

    /**
     * Constructor.
     */
    public CryptoRng() {
        this.random = new SecureRandom();
    }

    /**
     * Generate a random byte within a specified range. This function returns a random byte value produced by the
     * crypto random number generator within the range of nMin and nMax, inclusive. An SdkException is thrown in the
     * event of an error.
     *
     * @param min The minimum random value.
     * @param max The maximum random value.
     * @return Returns random byte.
     */
    public final byte randByte(final byte min, final byte max) {
        final int i = random.nextInt(max - min);
        return (byte) (i + min);
    }

    /**
     * Generate a random 16-bit signed integer within a specified range. This function returns a random 16-bit signed
     * integer value produced by the crypto random number generator within the range of nMin and nMax, inclusive. An
     * SdkException is thrown in the event of an error.
     *
     * @param min The minimum random value.
     * @param max The maximum random value.
     * @return Returns a random 16-bit signed integer value.
     */
    public final short randInt16(final short min, final short max) {
        final int i = random.nextInt(max - min);
        return (short) (i + min);
    }

    /**
     * Generate a random 32-bit signed integer within a specified range. This function returns a random 32-bit signed
     * integer value produced by the crypto random number generator within the range of nMin and nMax, inclusive. An
     * SdkException is thrown in the event of an error.
     *
     * @param min The minimum random value.
     * @param max The maximum random value.
     * @return Returns a random 32-bit signed integer value.
     */
    public final int randInt32(final int min, final int max) {
        final int i = random.nextInt(max - min);
        return (i + min);
    }

    /**
     * Generate a random 64-bit signed integer within a specified range. This function returns a random 64-bit signed
     * integer value produced by the crypto random number generator within the range of nMin and nMax, inclusive. An
     * SdkException is thrown in the event of an error.
     *
     * @param min The minimum random value.
     * @param max The maximum random value.
     * @return Returns a random 64-bit signed integer value.
     */
    public final long randInt64(final long min, final long max) {
        final long i = (random.nextLong() & Long.MAX_VALUE) % (max - min);
        return (i + min);
    }

    /**
     * Generate a random byte array.
     *
     * @param bytes A pre-allocated input buffer, which will be populated with random bytes.
     * @return Returns a reference to the input parameter (client code optimization).
     * @throws IonicException on null input
     */
    public final byte[] rand(final byte[] bytes) throws IonicException {
        if (bytes == null) {
            throw new IonicException(SdkError.ISCRYPTO_NULL_INPUT);
        } else {
            random.nextBytes(bytes);
            return bytes;
        }
    }
}
