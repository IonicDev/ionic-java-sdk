package com.ionic.sdk.httpclient;

/**
 * Constants associated with the ISHTTP module.
 */
public class Http {
    /**
     * Method names associated with the ISHTTP module.
     */
    public static class Method {
        /**
         * The "GET" HTTP method.
         */
        public static final String GET = "GET";

        /**
         * The "POST" HTTP method.
         */
        public static final String POST = "POST";
    }

    /**
     * Text names associated with the ISHTTP request/response headers.
     */
    public static class Header {

        /**
         * The HTTP request and response headers identify the allowed encoding of subsequent messages using this key.
         */
        public static final String ACCEPT_ENCODING = "Accept-Encoding";

        /**
         * Indicate to the server that it may deflate its json responses in order to save bandwidth.
         */
        public static final String ACCEPT_ENCODING_VALUE = "gzip,deflate";

        /**
         * The HTTP request and response headers identify the MIME type of the associated entity using this key.
         */
        public static final String CONTENT_TYPE = "Content-Type";

        /**
         * Indicate to the server that the request entity is syntactically valid json.
         */
        public static final String CONTENT_TYPE_VALUE = "application/json; charset=utf-8";

        /**
         * The response headers may contain this header, which identifies the application servicing the client request.
         */
        public static final String SERVER = "Server";

        /**
         * The request headers may contain this header, which identifies the application generating the client request.
         */
        public static final String USER_AGENT = "User-Agent";
    }

    /**
     * Text names associated with network-related system properties.
     */
    public static class Network {

        /**
         * This integer value determines the maximum number, for a given request, of HTTP redirects that will be
         * automatically followed by the protocol handler.
         *
         * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">
         * Networking Properties</a>
         */
        public static final String MAX_REDIRECTS = "http.maxRedirects";
    }
}
