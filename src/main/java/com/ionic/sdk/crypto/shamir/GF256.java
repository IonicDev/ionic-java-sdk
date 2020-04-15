package com.ionic.sdk.crypto.shamir;

import com.ionic.sdk.core.codec.Transcoder;

import java.util.Random;

/**
 * An implementation of GF(256).  Uses AES field polynomial and generator.
 * <p>
 * <a href="https://research.swtch.com/field" target="_blank">Finite Field Arithmetic and Reed-Solomon Coding</a>
 * <p>
 * <a href="http://www.cs.utsa.edu/~wagner/laws/FFM.html"
 * target="_blank">The Laws of Cryptography: The Finite Field GF(256)</a>
 * <p>
 * <a href="http://www.cs.utsa.edu/~wagner/laws/AFFMultTables.html" target="_blank">
 * The Laws of Cryptography:  Generate Multiplication Tables</a>
 * <p>
 * <a href="https://github.com/codahale/shamir/blob/master/src/main/java/com/codahale/shamir/GF256.java"
 * target="_blank">
 * codahale / shamir</a>
 */
public final class GF256 {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private GF256() {
    }

    /**
     * Generated from code derived from <a href="http://www.cs.utsa.edu/~wagner/laws/AFFMultTables.html"
     * target="_blank">
     * Generate Multiplication Tables</a>.
     *
     * The first two bytes are inverted from the output of FiniteFieldGeneratorTest
     * at the direction of jburns@ionic.
     *
     * stash.in ... /projects/A2/repos/ionicagents/pull-requests/2129/overview?commentId=86548
     */
    private static final byte[] LOG = Transcoder.hex().decode(""
            + "ff00190132021ac64bc71b6833eedf036404e00e348d81ef4c7108c8f8691cc1"
            + "7dc21db5f9b9276a4de4a6729ac90978652f8a05210fe12412f082453593da8e"
            + "968fdbbd36d0ce94135cd2f14046833866ddfd30bf068b62b325e29822889110"
            + "7e6e48c3a3b61e423a6b2854fa853dba2b790a159b9f5eca4ed4ace5f373a757"
            + "af58a850f4ead6744faee9d5e7e6ade82cd7757aeb160bf559cb5fb09ca951a0"
            + "7f0cf66f17c449ecd8431f2da4767bb7ccbb3e5afb60b1863b52a16caa55299d"
            + "97b2879061bedcfcbc95cfcd373f5bd15339843c41a26d47142a9e5d56f2d3ab"
            + "441192d923202e89b47cb8267799e3a5674aeddec531fe180d638c80c0f77007");

    /**
     * Generated from code derived from <a href="http://www.cs.utsa.edu/~wagner/laws/AFFMultTables.html"
     * target="_blank">
     * Generate Multiplication Tables</a>.
     */
    private static final byte[] EXP = Transcoder.hex().decode(""
            + "0103050f113355ff1a2e7296a1f813355fe13848d87395a4f702060a1e2266aa"
            + "e5345ce43759eb266abed97090abe63153f5040c143c44cc4fd168b8d36eb2cd"
            + "4cd467a9e03b4dd762a6f10818287888839eb9d06bbddc7f8198b3ce49db769a"
            + "b5c457f9103050f00b1d2769bbd661a3fe192b7d8792adec2f7193aee92060a0"
            + "fb163a4ed26db7c25de73256fa153f41c35ee23d47c940c05bed2c749cbfda75"
            + "9fbad564acef2a7e829dbcdf7a8e89809bb6c158e82365afea256fb1c843c554"
            + "fc1f2163a5f407091b2d7799b0cb46ca45cf4ade798b8691a8e33e42c651f30e"
            + "12365aee297b8d8c8f8a8594a7f20d17394bdd7c8497a2fd1c246cb4c752f601"
            + "03050f113355ff1a2e7296a1f813355fe13848d87395a4f702060a1e2266aae5"
            + "345ce43759eb266abed97090abe63153f5040c143c44cc4fd168b8d36eb2cd4c"
            + "d467a9e03b4dd762a6f10818287888839eb9d06bbddc7f8198b3ce49db769ab5"
            + "c457f9103050f00b1d2769bbd661a3fe192b7d8792adec2f7193aee92060a0fb"
            + "163a4ed26db7c25de73256fa153f41c35ee23d47c940c05bed2c749cbfda759f"
            + "bad564acef2a7e829dbcdf7a8e89809bb6c158e82365afea256fb1c843c554fc"
            + "1f2163a5f407091b2d7799b0cb46ca45cf4ade798b8691a8e33e42c651f30e12"
            + "365aee297b8d8c8f8a8594a7f20d17394bdd7c8497a2fd1c246cb4c752f60103");

    /**
     * Perform the addition transform on the field value.  Addition is just XOR.
     *
     * @param a the current field state
     * @param b the additional data to be integrated into the field
     * @return the result of the operation
     */
    private static byte add(final byte a, final byte b) {
        return (byte) (a ^ b);
    }

    /**
     * Perform the subtraction transform on the field value.  Delegate to add transform.
     *
     * @param a the current field state
     * @param b the additional data to be integrated into the field
     * @return the result of the operation
     */
    private static byte sub(final byte a, final byte b) {
        return add(a, b);
    }

    /**
     * Perform the multiplication transform on the field value.
     *
     * @param a the current field state
     * @param b the additional data to be integrated into the field
     * @return the result of the operation
     */
    private static byte mul(final byte a, final byte b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        final byte logA = LOG[toUnsignedInt(a)];
        final byte logB = LOG[toUnsignedInt(b)];
        return EXP[toUnsignedInt(logA) + toUnsignedInt(logB)];
    }

    /**
     * Perform the division transform on the field value.
     *
     * @param a the current field state
     * @param b the additional data to be integrated into the field
     * @return the result of the operation
     */
    private static byte div(final byte a, final byte b) {
        // multiply by the inverse of b
        final byte logB = LOG[toUnsignedInt(b)];
        return mul(a, EXP[(LOG.length - 1) - toUnsignedInt(logB)]);
    }

    /**
     * Evaluate the polynomial for the given x coordinate.
     *
     * @param p the polynomial to be evaluated
     * @param x the x coordinate of the desired point
     * @return the y coordinate of the desired point
     */
    public static byte eval(final byte[] p, final byte x) {
        // https://en.wikipedia.org/wiki/Horner%27s_method
        byte result = 0;
        for (int i = p.length - 1; i >= 0; i--) {
            final byte mul = mul(result, x);
            result = add(mul, p[i]);
        }
        return result;
    }

    /**
     * Calculate the degree of the input polynomial.
     *
     * @param p the polynomial to be evaluated
     * @return the degree of the polynomial
     */
    private static int degree(final byte[] p) {
        for (int i = p.length - 1; i >= 1; i--) {
            if (p[i] != 0) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Generate a polynomial of the desired degree.
     *
     * @param random a source of randomness to use in the generation
     * @param degree the desired degree of the polynomial to be generated
     * @param x      the y intercept of the generated polynomial
     * @return the generated polynomial
     */
    public static byte[] generate(final Random random, final int degree, final byte x) {
        final byte[] p = new byte[degree + 1];

        // generate random polynomials until we find one of the given degree
        do {
            random.nextBytes(p);
        } while (degree(p) != degree);

        // set y intercept
        p[0] = x;

        return p;
    }

    /**
     * Recover the original byte from the polynomial components.
     *
     * @param points a collection of points intersecting the polynomial
     * @return the original byte
     */
    public static byte interpolate(final byte[][] points) {
        // calculate f(0) of the given points using Lagrangian interpolation
        final byte x = 0;
        byte y = 0;
        for (int i = 0; i < points.length; i++) {
            final byte aX = points[i][0];
            final byte aY = points[i][1];
            byte li = 1;
            for (int j = 0; j < points.length; j++) {
                final byte bX = points[j][0];
                if (i != j) {
                    li = mul(li, div(sub(x, bX), sub(aX, bX)));
                }
            }
            y = add(y, mul(li, aY));
        }
        return y;
    }

    /**
     * Converts the argument to an int by an unsigned conversion.
     *
     * @param x the value to convert to an unsigned int
     * @return the argument converted to int by an unsigned conversion
     */
    private static int toUnsignedInt(final byte x) {
        return ((int) x) & ((1 << Byte.SIZE) - 1);
    }
}
