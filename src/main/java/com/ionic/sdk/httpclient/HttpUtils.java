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
     * @throws UnsupportedEncodingException should never happen {@link StandardCharsets}
     */
    public static String urlEncode(final String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.displayName());
    }
}
