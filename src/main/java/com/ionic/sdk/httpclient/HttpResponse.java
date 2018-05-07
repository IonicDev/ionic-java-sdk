package com.ionic.sdk.httpclient;

import java.io.ByteArrayInputStream;

/**
 * Encapsulate the server response to a client HTTP request.
 */
public class HttpResponse {
    /**
     * The HTTP status code associated with the response.
     */
    private final int statusCode;

    /**
     * The HTTP headers associated with the response.
     */
    private final HttpHeaders httpHeaders;

    /**
     * The entity bytes associated with the response.
     */
    private final ByteArrayInputStream entity;

    /**
     * Constructor, complete specifying the data associated with the response.
     *
     * @param statusCodeIn  the HTTP response status code
     * @param httpHeadersIn the HTTP headers
     * @param entityIn      the HTTP response entity
     */
    public HttpResponse(final int statusCodeIn, final HttpHeaders httpHeadersIn, final ByteArrayInputStream entityIn) {
        this.statusCode = statusCodeIn;
        this.httpHeaders = httpHeadersIn;
        this.entity = entityIn;
    }

    /**
     * @return the HTTP status code associated with the response.
     */
    public final int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the HTTP headers associated with the response.
     */
    public final HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    /**
     * @return the entity bytes associated with the response.
     */
    public final ByteArrayInputStream getEntity() {
        return entity;
    }
}
