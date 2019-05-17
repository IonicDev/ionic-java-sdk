package com.ionic.sdk.core.io;

/**
 * Utility function to find a byte sequence embedded within another byte sequence.
 */
public final class BytePattern {

    /**
     * Constructor. http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private BytePattern() {
    }

    /**
     * Find a byte sequence embedded within another byte sequence.
     *
     * @param bytes   the byte sequence to search
     * @param start   the position at which to start the search of the byte sequence
     * @param pattern the byte sequence to find within <code>bytes</code>
     * @return the starting index of the matching sequence, or -1 if not found
     */
    public static int findIn(final byte[] bytes, final int start, final byte[] pattern) {
        final int indexFinish = bytes.length - pattern.length;
        int indexBytes = start;
        int indexPattern = 0;
        while ((indexBytes <= indexFinish) && (indexPattern < pattern.length)) {
            if (bytes[indexBytes + indexPattern] == pattern[indexPattern]) {
                ++indexPattern;
            } else {
                ++indexBytes;
                indexPattern = 0;
            }
        }
        return (indexPattern == pattern.length) ? indexBytes : -1;
    }
}
