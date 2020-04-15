package com.ionic.sdk.httpclient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class containing various useful functions for working with http data.
 */
public final class HttpUtils {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private HttpUtils() {
    }

    /**
     * @param value the input to be encoded
     * @return the URL encoded representation of the input
     * @throws UnsupportedEncodingException should never happen (as is noted in {@link StandardCharsets}, support for
     *                                      the "UTF-8" charset is guaranteed to be available on every implementation
     *                                      of the Java platform)
     */
    public static String urlEncode(final String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.displayName());
    }
}
