package com.ionic.sdk.core.codec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utilities to provide conversions between UTF-8 encoded byte arrays and Java strings.
 */
public final class UTF8 implements BytesTranscoder {

    /**
     * Encode a UTF-8 byte[] as a string.
     *
     * @param bytes the input byte array (assumed to be encoded using the UTF-8 charset)
     * @return the equivalent Java string
     * @deprecated Please migrate usages to the replacement {@link Transcoder#utf8()} encode API.
     */
    @Deprecated
    @Override
    public String encode(final byte[] bytes) {
        return ((bytes == null) ? null : new String(bytes, UTF8));
    }

    /**
     * Encode a string as a UTF-8 byte[].
     *
     * @param string the input string
     * @return the UTF-8 encoded byte array
     * @deprecated Please migrate usages to the replacement {@link Transcoder#utf8()} decode API.
     */
    @Deprecated
    @Override
    public byte[] decode(final String string) {
        return ((string == null) ? null : string.getBytes(UTF8));
    }

    /**
     * Local reference to JVM definition of UTF-8 charset.
     */
    @SuppressWarnings("PMD.UnusedPrivateField")  // false positive
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    /**
     * Declaration of JRE default Charset name (use constant instead of string declaration).
     */
    public static final String NAME = UTF8.name();
}
