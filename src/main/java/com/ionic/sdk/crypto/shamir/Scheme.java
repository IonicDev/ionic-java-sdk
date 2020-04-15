package com.ionic.sdk.crypto.shamir;

import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of <a href="https://en.wikipedia.org/wiki/Shamir%27s_Secret_Sharing"
 * target="_blank">Shamir's Secret Sharing</a>,
 * allowing for the splitting of a secret into multiple parts, some or all of which are needed to recover the original
 * secret.
 */
public final class Scheme {

    /**
     * The number of parts into which the input secret should be split.
     */
    private final int n;

    /**
     * The number of parts from which the input secret should be recoverable.
     */
    private final int k;

    /**
     * A source of randomness to use in the generation of the split polynomial.
     */
    private final SecureRandom random;

    /**
     * Construct an object to be used in the splitting and recovery of a secret.
     * <p>
     * The variable k must be at least 1, and the variable n must be at least as large as k.  Additionally, n
     * is limited to a maximum of 255, for performance.
     *
     * @param n the number of parts into which the input secret should be split
     * @param k the number of parts from which the input secret should be recoverable
     * @throws IonicException on illegal inputs
     */
    public Scheme(final int n, final int k) throws IonicException {
        final boolean invalid = ((k < 1) || (n < k) || (n > MAX_N));
        if (invalid) {
            throw new IonicException(SdkError.ISAGENT_INVALIDVALUE,
                    new IllegalArgumentException(String.format("N = %d, K = %d", n, k)));
        }
        this.n = n;
        this.k = k;
        this.random = new SecureRandom();
    }

    /**
     * Split a secret into multiple parts.
     *
     * @param secret the original secret
     * @return a set of polynomial components, which may be used at a later time to recover the secret
     */
    public List<byte[]> split(final byte[] secret) {
        // generate part values
        final byte[][] values = new byte[n][secret.length];
        for (int i = 0; i < secret.length; ++i) {
            // for each byte, generate a random polynomial, p
            final byte[] p = GF256.generate(random, k - 1, secret[i]);
            for (int x = 1; x <= n; ++x) {
                // each part's byte is p(partId)
                values[x - 1][i] = GF256.eval(p, (byte) x);
            }
        }
        // return as a set of objects
        final List<byte[]> parts = new ArrayList<byte[]>(n);
        for (int i = 0; i < values.length; ++i) {
            final byte[] value = values[i];
            final byte[] valueWrapped = new byte[value.length + CRYPTOPP_OFFSET];
            ByteBuffer.wrap(valueWrapped, 0, CRYPTOPP_OFFSET).putInt(i + 1);
            System.arraycopy(value, 0, valueWrapped, CRYPTOPP_OFFSET, value.length);
            parts.add(valueWrapped);
        }
        return Collections.unmodifiableList(parts);
    }

    /**
     * Recover the original secret from the polynomial components.
     *
     * @param parts  the polynomial components
     * @param length the size of the secret to be reconstituted
     * @return the original secret, reconstituted from the input components
     */
    public byte[] join(final Collection<byte[]> parts, final int length) {
        //java.util.logging.Logger.getLogger(getClass().getName()).finest(
        //        String.format("JOIN %d PARTS, LENGTH %d", parts.size(), length));
        //final int[] lengths = getLengths(parts.values());
        final byte[] secret = new byte[length];
        for (int i = 0; i < secret.length; i++) {
            final byte[][] points = new byte[parts.size()][2];
            int j = 0;
            for (final byte[] value : parts) {
                // here is where we will strip off the cryptopp padding
                final int channel = ByteBuffer.wrap(value, 0, CRYPTOPP_OFFSET).getInt();
                final byte[] valueUnwrapped = new byte[value.length - CRYPTOPP_OFFSET];
                System.arraycopy(value, CRYPTOPP_OFFSET, valueUnwrapped, 0, valueUnwrapped.length);
                points[j][0] = Integer.valueOf(channel).byteValue();
                points[j][1] = valueUnwrapped[i];
                j++;
            }
            secret[i] = GF256.interpolate(points);
        }
        return secret;
    }

    /*
     * Calculate the lengths of the input value collection.
     *
     * @param values the collection of byte arrays representing the polynomial components
     * @return a corresponding array containing the calculated lengths of the input components
     */
/*
    private int[] getLengths(final Collection<byte[]> values) {
        final Set<Integer> lengths = new TreeSet<Integer>();
        for (final byte[] value : values) {
            lengths.add(value.length);
        }
        final int[] lengthsArray = new int[lengths.size()];
        int index = -1;
        for (final Integer length : lengths) {
            lengthsArray[++index] = length;
        }
        return lengthsArray;
    }
*/

    /**
     * The "cryptopp" implementation prepends the share data with a 4-byte block containing the channel number.
     */
    private static final int CRYPTOPP_OFFSET = 4;

    /**
     * Limit the computational complexity of our implementation.
     */
    private static final int MAX_N = 255;
}
