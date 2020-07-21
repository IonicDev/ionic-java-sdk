package com.ionic.sdk.agent.request.getresources;

import com.ionic.sdk.agent.request.base.AgentResponseBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the output for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#getResources(GetResourcesRequest)} API call.
 * <p>
 * The request will contain information about {@link GetResourcesRequest.Resource} objects, which identify the desired
 * service resources (for example, cover pages for data formats supported by the organization).
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/get-resource' target='_blank'>Machina Developers</a> for
 * more information about the GetResources operation.
 */
public class GetResourcesResponse extends AgentResponseBase {

    /**
     * Represents a list of Key objects for an Agent.getKeys() response.
     */
    private final List<Resource> resources;

    /**
     * Constructor.
     */
    public GetResourcesResponse() {
        super();
        this.resources = new ArrayList<Resource>();
    }

    /**
     * @return a list of Resource objects for an Agent.getResources() response.
     */
    public final List<Resource> getResources() {
        return resources;
    }

    /**
     * Add a resource response object to the {@link GetResourcesResponse}.
     *
     * @param resource the object containing the parameters of the resource response
     */
    public final void add(final Resource resource) {
        resources.add(resource);
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = 2239746847945310472L;

    /**
     * Represents a discrete resource object.
     */
    public static class Resource implements Serializable {

        /**
         * A reference to be used to associate resources received in the response with the request.
         */
        private final String refId;

        /**
         * The content of the requested resource.
         */
        private final String data;

        /**
         * The error, if any, encountered during the server processing of the resource request.
         */
        private final String error;

        /**
         * Constructor.
         *
         * @param refId the reference, supplied by the user in the request, used to associate the request and response
         * @param data  the content of the requested resource
         * @param error the error, if any, encountered during the server processing of the resource request
         */
        public Resource(final String refId, final String data, final String error) {
            this.refId = refId;
            this.data = data;
            this.error = error;
        }

        /**
         * @return the reference, supplied by the user in the request, used to associate the request and response
         */
        public final String getRefId() {
            return refId;
        }

        /**
         * @return the content of the requested resource
         */
        public final String getData() {
            return data;
        }

        /**
         * @return the error, if any, encountered during the server processing of the resource request
         */
        public final String getError() {
            return error;
        }

        /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
        private static final long serialVersionUID = -2903532464909971516L;
    }
}
