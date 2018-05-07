package com.ionic.sdk.core.codec7;

import com.ionic.sdk.core.codec.BytesTranscoder;

import javax.xml.bind.DatatypeConverter;
import java.util.Locale;

/**
 * Isolating the usage of "DatatypeConverter" to this class, as we may have to do different things on certain platforms.
 */
public final class Hex implements BytesTranscoder {

    /**
     * Transform byte stream into equivalent hex string representation.
     *
     * @param bytes an array of bytes
     * @return string containing hex representation of input bytes, or null if input is null
     */
    @Override
    public String encode(final byte[] bytes) {
        // Found "toLowerCase()" during interop testing.  Once we need "toLowerCase", it is safer to use a hard-coded
        // locale, to avoid breaking in unexpected ways.  Looks like "Locale.ROOT" is available, so we will use that.
        // https://garygregory.wordpress.com/2015/11/03/java-lowercase-conversion-turkey/
        return (bytes == null) ? null : DatatypeConverter.printHexBinary(bytes).toLowerCase(Locale.ROOT);
    }

    /**
     * Transform hex string into equivalent raw byte array.
     *
     * @param hexText hex string representation of raw byte array
     * @return byte array containing decoded representation of input, or null if input is null
     */
    @Override
    public byte[] decode(final String hexText) {
        return (hexText == null) ? null : DatatypeConverter.parseHexBinary(hexText);
    }
}
