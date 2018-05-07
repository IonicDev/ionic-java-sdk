package com.ionic.sdk.core.codec8;

import com.ionic.sdk.core.codec.BytesTranscoder;

/**
 * Isolating the usage of "DatatypeConverter" to this class, as we may have to do different things on certain platforms.
 */
public final class Base64 implements BytesTranscoder {

    /**
     * Transform byte stream into equivalent base64 string representation.
     *
     * @param bytes an array of bytes
     * @return string containing base64 representation of input bytes, or null if input is null
     */
    @Override
    public String encode(final byte[] bytes) {
        return (bytes == null) ? null : java.util.Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Transform base64 string into equivalent raw byte array.
     *
     * @param base64Text base64 string representation of raw byte array
     * @return byte array containing decoded representation of input, or null if input is null
     */
    @Override
    public byte[] decode(final String base64Text) {
        return (base64Text == null) ? null : java.util.Base64.getDecoder().decode(base64Text);
    }
}
