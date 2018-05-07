package com.ionic.sdk.agent.request.getresources;

import com.ionic.sdk.agent.request.base.AgentResponseBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the output for an Agent.getResources() request.
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

    /**
     * Represents a discrete resource object.
     */
    public static class Resource {

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
    }
}
