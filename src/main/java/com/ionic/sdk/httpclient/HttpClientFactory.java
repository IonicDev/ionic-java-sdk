package com.ionic.sdk.httpclient;

/**
 * Encapsulate the assembly of a new http client, given a string specifying the requested type.
 */
public final class HttpClientFactory {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private HttpClientFactory() {
    }

    /**
     * Create a new http client for use in making key services requests.
     *
     * @param httpImpl        the type of implementation to create
     * @param httpTimeoutSecs the number of seconds to wait after making a server request for a response
     * @param maxRedirects    the maximum number of HTTP redirects
     * @param protocol        the protocol to be checked for proxy configuration (e.g. "http", "https")
     * @return an HttpClient of the requested type
     */
    public static HttpClient create(final String httpImpl, final int httpTimeoutSecs,
                                    final int maxRedirects, final String protocol) {
        final HttpClient httpClient;
        if (HttpClientTrustAll.class.getSimpleName().equals(httpImpl)) {
            httpClient = new HttpClientTrustAll(httpTimeoutSecs, maxRedirects, protocol);
        } else {
            httpClient = new HttpClientDefault(httpTimeoutSecs, maxRedirects, protocol);
        }
        return httpClient;
    }
}
