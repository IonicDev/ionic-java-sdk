package com.ionic.sdk.agent.request.getkeyspace;

import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the output for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#getKeyspace(GetKeyspaceRequest)} API call.
 * <p>
 * The response will contain information associated with the requested keyspace identifier, including URLs to access
 * the keyspace.
 */
public class GetKeyspaceResponse extends AgentResponseBase {

    /**
     * The specified four-character Machina keyspace.
     */
    private String keyspace;

    /**
     * The fully-qualified domain name for the host machine of the keyspace.
     */
    private String fqdn;

    /**
     * The period of time that this {@link GetKeyspaceResponse} is safe to cache.
     */
    private int ttlSeconds;

    /**
     * The base URLs providing access to the enrollment functionality of the keyspace.
     */
    private final List<String> enrollmentURLs;

    /**
     * The internal identifier for the specified keyspace.
     */
    private final List<String> tenantIDs;

    /**
     * The base URLs providing access to the API functionality of the keyspace.
     */
    private final List<String> apiURLs;

    /**
     * Constructor.
     */
    public GetKeyspaceResponse() {
        this.enrollmentURLs = new ArrayList<String>();
        this.tenantIDs = new ArrayList<String>();
        this.apiURLs = new ArrayList<String>();
    }

    /**
     * @return the specified four-character Machina keyspace
     */
    public String getKeyspace() {
        return keyspace;
    }

    /**
     * Set the specified four-character Machina keyspace.
     *
     * @param keyspace the specified four-character Machina keyspace
     */
    public void setKeyspace(final String keyspace) {
        this.keyspace = keyspace;
    }

    /**
     * @return the fully-qualified domain name for the host machine of the keyspace
     */
    public String getFqdn() {
        return fqdn;
    }

    /**
     * Set the fully-qualified domain name for the host machine of the keyspace.
     *
     * @param fqdn the fully-qualified domain name for the host machine of the keyspace
     */
    public void setFqdn(final String fqdn) {
        this.fqdn = fqdn;
    }

    /**
     * @return the period of time that this {@link GetKeyspaceResponse} is safe to cache.
     */
    public int getTtlSeconds() {
        return ttlSeconds;
    }

    /**
     * Set the period of time that this {@link GetKeyspaceResponse} is safe to cache.
     *
     * @param ttlSeconds the period of time that this {@link GetKeyspaceResponse} is safe to cache
     */
    public void setTtlSeconds(final int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * @return the base URLs providing access to the enrollment functionality of the keyspace
     */
    public final List<String> getEnrollmentURLs() {
        return enrollmentURLs;
    }

    /**
     * @return the first URL providing access to the enrollment functionality of the keyspace
     * @throws IonicException on empty list of enrollmentURLs in response
     */
    public final String getFirstEnrollmentURL() throws IonicException {
        SdkData.checkTrue(!enrollmentURLs.isEmpty(), SdkError.ISAGENT_MISSINGVALUE);
        return enrollmentURLs.iterator().next();
    }

    /**
     * @return the internal identifier for the specified keyspace
     */
    public final List<String> getTenantIDs() {
        return tenantIDs;
    }

    /**
     * @return the base URLs providing access to the API functionality of the keyspace
     */
    public List<String> getApiURLs() {
        return apiURLs;
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = 956831753735162538L;
}
