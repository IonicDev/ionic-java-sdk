package com.ionic.sdk.core.codec;

/**
 * A utility class to perform base64 encoding and decoding.
 *
 * @deprecated Please migrate usages to the replacement {@link Transcoder#base64()} factory method.
 */
public final class Base64 implements BytesTranscoder {

    /**
     * Transform byte stream into equivalent base64 string representation.
     *
     * @param bytes an array of bytes
     * @return string containing base64 representation of input bytes
     * @deprecated Please migrate usages to the replacement {@link Transcoder#base64()} encode API.
     */
    @Deprecated
    @Override
    public String encode(final byte[] bytes) {
        return Transcoder.base64().encode(bytes);
    }

    /**
     * Transform base64 string into equivalent raw byte array.
     *
     * @param base64Text base64 string representation of raw byte array
     * @return byte array containing decoded representation of input
     * @deprecated Please migrate usages to the replacement {@link Transcoder#base64()} decode API.
     */
    @Deprecated
    @Override
    public byte[] decode(final String base64Text) {
        return Transcoder.base64().decode(base64Text);
    }
}
