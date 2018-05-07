package com.ionic.sdk.httpclient;

import java.io.ByteArrayInputStream;
import java.net.URL;

/**
 * Encapsulate an HTTP client request, destined for an HTTP server, with the expectation of receiving an HTTP response.
 */
public final class HttpRequest {
    /**
     * The URL associated with the request.
     */
    private final URL url;

    /**
     * The HTTP method associated with the request.
     */
    private final String method;

    /**
     * The resource URI associated with the request.
     */
    private final String resource;

    /**
     * The HTTP headers associated with the request.
     */
    private final HttpHeaders httpHeaders;

    /**
     * The entity bytes associated with the request.
     */
    private final ByteArrayInputStream entity;

    /**
     * Assemble an HTTP request, to be sent to an HTTP server.
     *
     * @param urlIn      the destination address of the target
     * @param methodIn   the HTTP method
     * @param resourceIn the request resource
     */
    public HttpRequest(final URL urlIn, final String methodIn, final String resourceIn) {
        this(urlIn, methodIn, resourceIn, new HttpHeaders(), null);
    }

    /**
     * Assemble an HTTP request, to be sent to an HTTP server.
     *
     * @param urlIn         the destination address of the target
     * @param methodIn      the HTTP method
     * @param resourceIn    the request resource
     * @param httpHeadersIn the request HTTP headers
     * @param entityIn      the entity bytes associated with the request
     */
    public HttpRequest(final URL urlIn, final String methodIn, final String resourceIn,
                       final HttpHeaders httpHeadersIn, final ByteArrayInputStream entityIn) {
        this.url = urlIn;
        this.method = methodIn;
        this.resource = resourceIn;
        this.httpHeaders = httpHeadersIn;
        this.entity = entityIn;
    }

    /**
     * @return the URL associated with the request
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @return the HTTP method associated with the request
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the resource URI associated with the request
     */
    public String getResource() {
        return resource;
    }

    /**
     * @return the HTTP headers associated with the request
     */
    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    /**
     * @return the entity bytes associated with the request
     */
    public ByteArrayInputStream getEntity() {
        return entity;
    }
}
