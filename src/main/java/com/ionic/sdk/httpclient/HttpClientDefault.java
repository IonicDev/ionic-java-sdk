package com.ionic.sdk.httpclient;

import com.ionic.sdk.core.date.DateTime;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.httpclient.proxy.ProxyManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The object that provides the ability to communicate with an HTTP server.
 */
public final class HttpClientDefault implements HttpClient {

    /**
     * The number of seconds to wait after making a server request for a response.
     */
    private final int httpTimeoutSecs;

    /**
     * The maximum number of HTTP redirects.
     */
    private final int maxRedirects;

    /**
     * The proxy configured for this client object (if no configuration, Proxy.NO_PROXY).
     */
    private final Proxy proxy;

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     *
     * @param httpTimeoutSecs the number of seconds to wait after making a server request for a response
     * @param maxRedirects    the maximum number of HTTP redirects
     * @param protocol        the protocol to be checked for proxy configuration (e.g. "http", "https")
     */
    public HttpClientDefault(final int httpTimeoutSecs, final int maxRedirects, final String protocol) {
        this.httpTimeoutSecs = httpTimeoutSecs;
        this.maxRedirects = maxRedirects;
        this.proxy = ProxyManager.getProxy(protocol);
    }

    /**
     * Send a request to the specified HTTP server.
     *
     * @param httpRequest the data associated with the client request
     * @return the response received from the server
     * @throws IOException if an I/O error occurs
     */
    @Override
    public HttpResponse execute(final HttpRequest httpRequest) throws IOException {
        // https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html
        System.setProperty(Http.Network.MAX_REDIRECTS, Integer.toString(maxRedirects));
        final URL url = httpRequest.getUrl();
        final String resource = httpRequest.getResource();
        final URL urlRequest = new URL(url.getProtocol(), url.getHost(), url.getPort(), resource);
        logger.finest(String.format("URL = %s", urlRequest.toExternalForm()));
        final HttpURLConnection connection = (HttpURLConnection) urlRequest.openConnection(proxy);
        connection.setConnectTimeout(httpTimeoutSecs * (int) DateTime.ONE_SECOND_MILLIS);
        connection.setReadTimeout(httpTimeoutSecs * (int) DateTime.ONE_SECOND_MILLIS);
        logger.finest(String.format("HttpURLConnection = %s", connection.toString()));
        return executeInternal(connection, httpRequest);
    }

    /**
     * Send a request to the specified HTTP server connection.
     *
     * @param connection  the open connection associated with the client request
     * @param httpRequest the data associated with the client request
     * @return the response received from the server
     * @throws IOException if an I/O error occurs
     */
    private HttpResponse executeInternal(final HttpURLConnection connection, final HttpRequest httpRequest)
            throws IOException {
        final ByteArrayInputStream entity = httpRequest.getEntity();
        connection.setRequestMethod(httpRequest.getMethod());
        connection.setDoInput(true);
        connection.setDoOutput(entity != null);
        for (final HttpHeader httpHeader : httpRequest.getHttpHeaders()) {
            connection.setRequestProperty(httpHeader.getName(), httpHeader.getValue());
        }
        logger.finest(String.format("ready to connect, HttpURLConnection = %s", connection));
        connection.connect();
        logger.finest(String.format("connected, HttpURLConnection = %s", connection));
        return executeConnected(connection, entity);
    }

    /**
     * Send a request to the specified open HTTP server connection.
     *
     * @param connection the open connection associated with the client request
     * @param entity     the request data (if any) associated with the client request
     * @return the response received from the server
     * @throws IOException if an I/O error occurs
     */
    private HttpResponse executeConnected(
            final HttpURLConnection connection, final ByteArrayInputStream entity) throws IOException {
        logger.finest(String.format("ready to write, HttpURLConnection = %s", connection));
        if (entity != null) {
            Stream.write(connection.getOutputStream(), entity);
        }
        logger.finest(String.format("ready to read, HttpURLConnection = %s", connection));
        final int statusCode = connection.getResponseCode();
        logger.finest(String.format("statusCode = %d, HttpURLConnection = %s", statusCode, connection));
        final HttpHeaders httpHeadersResponse = new HttpHeaders();
        final Map<String, List<String>> headerFields = connection.getHeaderFields();
        for (final Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            final String name = entry.getKey();
            for (final String value : entry.getValue()) {
                httpHeadersResponse.add(new HttpHeader(name, value));
            }
        }
        logger.finest(String.format("#headers = %d, HttpURLConnection = %s", httpHeadersResponse.size(), connection));
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            Stream.write(os, connection.getInputStream());
        } catch (IOException e) {
            Stream.write(os, connection.getErrorStream());
        }
        logger.finest(String.format("#entity = %d, HttpURLConnection = %s", os.size(), connection));
        return new HttpResponse(statusCode, httpHeadersResponse, new ByteArrayInputStream(os.toByteArray()));
    }
}
