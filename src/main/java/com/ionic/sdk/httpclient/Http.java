package com.ionic.sdk.httpclient;

import java.net.HttpURLConnection;

/**
 * Constants associated with the ISHTTP module.
 */
public final class Http {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Http() {
    }

    /**
     * Method names associated with the ISHTTP module.
     */
    public static final class Method {

        /** Checkstyle / FinalClass. */
        private Method() {
        }

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
     * Status values associated with the ISHTTP module.
     */
    public static final class Status {

        /** Checkstyle / FinalClass. */
        private Status() {
        }

        /**
         * The code associated with a successful HTTP call.
         */
        public static final int OK = HttpURLConnection.HTTP_OK;
    }

    /**
     * States associated with an HTTP exchange.
     */
    public static final class State {

        /** Checkstyle / FinalClass. */
        private State() {
        }

        /**
         * The request sent by the client to the server.
         */
        public static final String REQUEST = "q";

        /**
         * The response sent by the server to the client.
         */
        public static final String RESPONSE = "p";
    }

    /**
     * Text names associated with the ISHTTP request/response headers.
     */
    public static final class Header {

        /** Checkstyle / FinalClass. */
        private Header() {
        }

        /**
         * The HTTP request and response headers identify the allowed encoding of subsequent messages using this key.
         */
        public static final String ACCEPT_ENCODING = "Accept-Encoding";

        /**
         * Indicate to the server that it may deflate its json responses in order to save bandwidth.
         */
        public static final String ACCEPT_ENCODING_VALUE = "gzip,deflate";

        /**
         * HTTP connection provides mechanism to signal connection lifecycle from either client or service.
         */
        public static final String CONNECTION = "Connection";

        /**
         * The HTTP request and response headers identify the MIME type of the associated entity using this key.
         */
        public static final String CONTENT_TYPE = "Content-Type";

        /**
         * Indicate to the server that the request entity is syntactically valid json.
         */
        public static final String CONTENT_TYPE_CLIENT = "application/json; charset=utf-8";

        /**
         * Expected content type specified in server response.  (Charset is implied to be 'utf-8').
         */
        public static final String CONTENT_TYPE_SERVER = "application/json";

        /**
         * The response headers may contain this header, which identifies the application servicing the client request.
         */
        public static final String SERVER = "Server";

        /**
         * The request headers may contain this header, which identifies the application generating the client request.
         */
        public static final String USER_AGENT = "User-Agent";

        /**
         * CreateDevice 2.3 API calls for the inclusion of a conversation ID header (value of type UUID).
         */
        public static final String X_CONVERSATION_ID = "X-Conversation-ID";
    }

    /**
     * Text values associated with HTTP headers.
     */
    public static final class Value {

        /** Checkstyle / FinalClass. */
        private Value() {
        }

        /**
         * HTTP connection provides mechanism to signal connection lifecycle from either client or service.
         */
        public static final String CLOSE = "close";
    }

    /**
     * Text names associated with network-related system properties.
     */
    public static final class Network {

        /** Checkstyle / FinalClass. */
        private Network() {
        }

        /**
         * Indicates if persistent connections should be supported.
         *
         * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html#MiscHTTP"
         * target="_blank">Networking Properties</a>
         */
        public static final String KEEP_ALIVE = "http.keepalive";

        /**
         * This integer value determines the maximum number, for a given request, of HTTP redirects that will be
         * automatically followed by the protocol handler.
         *
         * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html#MiscHTTP"
         * target="_blank">Networking Properties</a>
         */
        public static final String MAX_REDIRECTS = "http.maxRedirects";
    }
}
