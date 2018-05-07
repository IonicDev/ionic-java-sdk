package com.ionic.sdk.core.rng;

import java.security.SecureRandom;

/**
 * Utility functions relating to the JRE random number generator facility.
 */
public final class RNG {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private RNG() {
    }

    /**
     * Generate some random bytes, to be used in cryptographic operations.
     * <p>
     * The empty argument constructor is used in preference to any particular implementation.  This defers the choice
     * of random number generator to the system.
     * <p>
     * Once support for JRE 1.7 is dropped (JRE 1.8+), we may choose to migrate to use
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/security/SecureRandom.html#getInstanceStrong--">
     * getInstanceStrong</a>.
     *
     * @param bytes the byte array to be filled with random data
     * @return the parameter byte array, populated with secure random data
     * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html#SecureRandomImp">
     * SecureRandomImpl (oracle.com)</a>
     * @see <a href="https://stackoverflow.com/questions/27622625/securerandom-with-nativeprng-vs-sha1prng">
     * Stack Overflow</a>
     * @see <a href="https://android-developers.googleblog.com/2016/06/security-crypto-provider-deprecated-in.html">
     * googleblog.com</a>
     */
    public static byte[] fill(final byte[] bytes) {
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
