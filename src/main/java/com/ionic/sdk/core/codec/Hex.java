package com.ionic.sdk.core.codec;

/**
 * A utility class to perform hex encoding and decoding.
 *
 * @deprecated Please migrate usages to the replacement {@link Transcoder#hex()} factory method.
 */
public final class Hex implements BytesTranscoder {

    /**
     * Transform byte stream into equivalent hex string representation.
     *
     * @param bytes an array of bytes
     * @return string containing hex representation of input bytes
     * @deprecated Please migrate usages to the replacement {@link Transcoder#hex()} encode API.
     */
    @Deprecated
    @Override
    public String encode(final byte[] bytes) {
        return Transcoder.hex().encode(bytes);
    }

    /**
     * Transform hex string into equivalent raw byte array.
     *
     * @param hexText hex string representation of raw byte array
     * @return byte array containing decoded representation of input
     * @deprecated Please migrate usages to the replacement {@link Transcoder#hex()} decode API.
     */
    @Deprecated
    @Override
    public byte[] decode(final String hexText) {
        return Transcoder.hex().decode(hexText);
    }
}
