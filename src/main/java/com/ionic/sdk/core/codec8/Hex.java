package com.ionic.sdk.core.codec8;

import com.ionic.sdk.core.codec.BytesTranscoder;

/**
 * Implementation of hex encoding of arbitrary byte streams.  Package "javax.xml.bind" has been removed from default
 * classpath in JRE 9.  New class "java.util.Base64" is available starting in JRE 8, but no built-in hex equivalent is
 * available.  So we've implemented this class to fill the gap.
 * <p>
 * Lower case alpha chars are being used, per previous implementation.
 */
public final class Hex implements BytesTranscoder {

    /**
     * Transform byte stream into equivalent hex string representation.
     * <p>
     * https://stackoverflow.com/questions/332079/
     *
     * @param bytes an array of bytes
     * @return string containing hex representation of input bytes, or null if input is null
     */
    @Override
    public String encode(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final char[] charsOut = new char[bytes.length * 2];
        int cursor = -1;
        for (byte b : bytes) {
            final int i = (b & HEX_BYTE);
            charsOut[++cursor] = ALPHABET_CHARS[i / RADIX];
            charsOut[++cursor] = ALPHABET_CHARS[i % RADIX];
        }
        return new String(charsOut);
    }

    /**
     * Transform hex string into equivalent raw byte array.
     * <p>
     * https://stackoverflow.com/questions/140131/
     *
     * @param hexText hex string representation of raw byte array
     * @return byte array containing decoded representation of input, or null if input is null
     * @throws IllegalArgumentException on illegal input (input length is expected to be a multiple of 2)
     */
    @Override
    public byte[] decode(final String hexText) {
        if (hexText == null) {
            return null;
        }
        final int lengthIn = hexText.length();
        if ((lengthIn % 2) != 0) {
            throw new IllegalArgumentException(hexText);
        }
        final byte[] bytesOut = new byte[lengthIn / 2];
        final char[] charsIn = hexText.toCharArray();
        int cursor = 0;
        boolean highNibble = true;
        for (char c : charsIn) {
            final int index = ALPHABET.indexOf(c);
            bytesOut[cursor] += (byte) (index << (highNibble ? (Byte.SIZE / 2) : 0));
            cursor += (highNibble ? 0 : 1);
            highNibble = !highNibble;
        }
        return bytesOut;
    }

    /**
     * Enumeration of possible hexadecimal values.
     */
    private static final String ALPHABET = "0123456789abcdef";

    /**
     * Enumeration of possible hexadecimal values.
     */
    private static final char[] ALPHABET_CHARS = ALPHABET.toCharArray();

    /**
     * Define low eight bits enabled of an integer, in order to perform bitwise operations.
     */
    private static final int HEX_BYTE = (1 << Byte.SIZE) - 1;

    /**
     * Hexadecimal (also base 16, or hex) is a positional numeral system with a radix, or base, of 16.
     */
    private static final int RADIX = 16;
}
