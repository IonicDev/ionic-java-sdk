package com.ionic.sdk.core.rng;

import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Utility functions relating to the JRE random number generator facility.
 */
public class CryptoRng {

    /**
     * The source of randomness for this object.
     */
    private final SecureRandom random;

    /**
     * Constructor.
     */
    public CryptoRng() {
        this.random = new SecureRandom();
    }

    /**
     * Generate a random byte within a specified range.
     * <p>
     * This function is backed by {@link SecureRandom#nextInt(int)}.
     *
     * @param min The minimum random value.
     * @param max The maximum random value.
     * @return Returns random byte.
     * @throws IonicException on invalid input
     */
    public final byte randByte(final byte min, final byte max) throws IonicException {
        final int i = randInternal(max - min);
        return (byte) (i + min);
    }

    /**
     * Generate a random short within a specified range.
     * <p>
     * This function is backed by {@link SecureRandom#nextInt(int)}.
     *
     * @param min The minimum random value.
     * @param max The maximum random value.
     * @return Returns a random 16-bit signed integer value.
     * @throws IonicException on invalid input
     */
    public final short randInt16(final short min, final short max) throws IonicException {
        final int i = randInternal(max - min);
        return (short) (i + min);
    }

    /**
     * Generate a random int within a specified range.
     * <p>
     * This function is backed by {@link SecureRandom#nextInt(int)}.
     *
     * @param min The minimum random value.
     * @param max The maximum random value.
     * @return Returns a random 32-bit signed integer value.
     * @throws IonicException on invalid input
     */
    public final int randInt32(final int min, final int max) throws IonicException {
        final int i = randInternal(max - min);
        return (i + min);
    }

    /**
     * Generate a random int within a specified range.
     * <p>
     * This function is backed by {@link SecureRandom#nextInt(int)}.
     *
     * @param bound The difference between the minimum and maximum random values.
     * @return Returns a random 32-bit signed integer value.
     * @throws IonicException on invalid input
     */
    private int randInternal(final int bound) throws IonicException {
        if (bound <= 0) {
            throw new IonicException(SdkError.ISCRYPTO_BAD_INPUT);
        } else {
            return random.nextInt(bound);
        }
    }

    /**
     * Generate a random long within a specified range.
     * <p>
     * This function is backed by {@link SecureRandom#nextBytes(byte[])}.
     * <p>
     * See {@link BigInteger#longValue()}.
     *
     * @param min The minimum random value.
     * @param max The maximum random value.
     * @return Returns a random 64-bit signed integer value.
     * @throws IonicException on invalid input
     */
    public final long randInt64(final long min, final long max) throws IonicException {
        final long bound = max - min;
        if (bound <= 0) {
            throw new IonicException(SdkError.ISCRYPTO_BAD_INPUT);
        } else {
            // generate 128-bit random number
            final byte[] bytes = new byte[(Long.SIZE + FIPS_A_5_3_EXTRA_BITS) / Byte.SIZE];
            random.nextBytes(bytes);
            // perform modulo operation on 128-bit number
            final BigInteger randomBI = new BigInteger(bytes).mod(BigInteger.valueOf(bound));
            // discard most significant bits of result
            final long i = randomBI.longValue();
            return (i + min);
        }
    }

    /**
     * The number of extra bits needed to generate a FIPS cryptographically secure random number, as described
     * <a href='https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-90Ar1.pdf' target='_blank'>here</a>.
     * <p>
     * A.5.3 - The Simple Modular Method
     */
    private static final int FIPS_A_5_3_EXTRA_BITS = 64;

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
