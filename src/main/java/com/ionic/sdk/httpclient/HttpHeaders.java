package com.ionic.sdk.httpclient;

import com.ionic.sdk.core.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * The set of name value pairs associated with an HTTP request / response.
 */
public class HttpHeaders extends ArrayList<HttpHeader> implements Serializable {

    /**
     * Constructor.
     *
     * @param httpHeaders pre-composed objects to be added to the initial backing collection
     */
    public HttpHeaders(final HttpHeader... httpHeaders) {
        this.addAll(Arrays.asList(httpHeaders));
    }

    /**
     * Return the value of the contained http header associated with the specified name.
     *
     * @param name the label of the sought header entry
     * @return the value of the corresponding header entry
     */
    public final String getHeaderValue(final String name) {
        final HttpHeader httpHeader = getHeader(name);
        return ((httpHeader == null) ? null : httpHeader.getValue());
    }

    /**
     * Return the contained http header associated with the specified name.
     *
     * @param name the label of the sought header entry
     * @return the corresponding header entry
     */
    private HttpHeader getHeader(final String name) {
        HttpHeader httpHeader = null;
        for (final HttpHeader httpHeaderIt : this) {
            if (Value.isEqualIgnoreCase(httpHeaderIt.getName(), name)) {
                httpHeader = httpHeaderIt;
                break;
            }
        }
        return httpHeader;
    }

    /**
     * Return an enumeration of header names contained in this collection of headers.
     *
     * @return a collection of strings representing the names of the contained headers
     */
    public final Collection<String> headerNames() {
        final Collection<String> headerNames = new ArrayList<String>();
        for (final HttpHeader httpHeader : this) {
            final String name = httpHeader.getName();
            if (name != null) {
                headerNames.add(name);
            }
        }
        return headerNames;
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.7.0". */
    private static final long serialVersionUID = -561978172917351189L;
}
