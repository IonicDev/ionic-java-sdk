package com.ionic.sdk.core.value;

/**
 * Utility class for derivation of integral values from an arbitrary byte array.
 */
public final class BytesReader {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private BytesReader() {
    }

    /**
     * Read an "int" value from the parameter byte array, between the parameter boundaries.
     *
     * @param bytes the byte array from which to read the integer
     * @param from  the starting offset (inclusive) in the input array from which to read
     * @param to    the ending offset (exclusive) in the input array at which to terminate the read
     * @return the integer value associated with the input data
     */
    public static int readInt(final byte[] bytes, final int from, final int to) {
        int value = 0;
        for (int i = from; (i < to); ++i) {
            value = (value << Byte.SIZE);
            value += (bytes[i] & MASK);
        }
        return value;
    }

    /**
     * Bit mask used to target significant bits of incoming byte data.
     */
    private static final int MASK = 0xff;
}
