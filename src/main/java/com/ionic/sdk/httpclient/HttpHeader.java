package com.ionic.sdk.httpclient;

/**
 * A single name value pair associated with an HTTP request / response.
 */
public class HttpHeader {

    /**
     * The name of the specified header.
     */
    private final String name;

    /**
     * The value of the specified header.
     */
    private final String value;

    /**
     * Constructor for this name / value pair.
     *
     * @param nameIn  the name of the specified header
     * @param valueIn the value of the specified header
     */
    public HttpHeader(final String nameIn, final String valueIn) {
        this.name = nameIn;
        this.value = valueIn;
    }

    /**
     * @return the name of the specified header
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the value of the specified header
     */
    public final String getValue() {
        return value;
    }
}
