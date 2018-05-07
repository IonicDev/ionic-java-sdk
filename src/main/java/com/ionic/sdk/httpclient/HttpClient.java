package com.ionic.sdk.httpclient;

import java.io.IOException;

/**
 * The object that provides the ability to communicate with an HTTP server.
 */
public interface HttpClient {

    /**
     * Send a request to the specified HTTP server.
     *
     * @param httpRequest the data associated with the client request
     * @return the response received from the server
     * @throws IOException if an I/O error occurs
     */
    HttpResponse execute(final HttpRequest httpRequest) throws IOException;
}
