package com.ionic.sdk.agent.request.getkeyspace;

import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.core.value.Value;

/**
 * Represents the input for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#getKeyspace(GetKeyspaceRequest)} API call.
 * <p>
 * The request will contain:
 * <ul>
 * <li>(required) the desired four-character keyspace identifier for which information is desired,</li>
 * <li>(optional) the API URL used to perform the request (if unset, defaults to "https://api.ionic.com").</li>
 * </ul>
 */
public class GetKeyspaceRequest extends AgentRequestBase {

    /**
     * Base URL for querying Ionic Machina infrastructure for keyspace metadata.
     */
    public static final String API_BASE_URL = "https://api.ionic.com";

    /**
     * The four-character Machina keyspace to be queried.
     */
    private final String keyspace;

    /**
     * Custom API URL for querying Ionic Machina infrastructure for keyspace metadata.
     */
    private final String url;

    /**
     * Constructor.
     *
     * @param keyspace the four-character Machina keyspace to be queried
     */
    public GetKeyspaceRequest(final String keyspace) {
        this(keyspace, null);
    }

    /**
     * Constructor.
     *
     * @param keyspace the four-character Machina keyspace to be queried
     * @param url      the API URL used to perform the request
     */
    public GetKeyspaceRequest(final String keyspace, final String url) {
        this.keyspace = keyspace;
        this.url = Value.isEmpty(url) ? API_BASE_URL : url;
    }

    /**
     * @return the four-character Machina keyspace to be queried
     */
    public String getKeyspace() {
        return keyspace;
    }

    /**
     * @return the API URL used to perform the request
     */
    public String getUrl() {
        return url;
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = 3429766797893551566L;
}
