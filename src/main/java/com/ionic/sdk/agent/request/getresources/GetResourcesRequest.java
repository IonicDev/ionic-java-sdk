package com.ionic.sdk.agent.request.getresources;

import com.ionic.sdk.agent.request.base.AgentRequestBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the input for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#getResources(GetResourcesRequest)} API call.
 * <p>
 * The request will contain information about {@link Resource} objects, which identify the desired
 * service resources (for example, cover pages for data formats supported by the organization).
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/get-resource' target='_blank'>Machina Developers</a> for
 * more information about the GetResources operation.
 */
public class GetResourcesRequest extends AgentRequestBase {

    /**
     * Represents a list of Key objects for an Agent.createKeys() request.
     */
    private final List<Resource> resources;

    /**
     * Constructor.
     */
    public GetResourcesRequest() {
        this.resources = new ArrayList<Resource>();
    }

    /**
     * @return a list of Resource objects for an Agent.getResources() request.
     */
    public final List<Resource> getResources() {
        return resources;
    }

    /**
     * Add a resource request object to the {@link GetResourcesRequest}.
     *
     * @param resource the object containing the parameters of the resource request
     */
    public final void add(final Resource resource) {
        resources.add(resource);
    }

    /**
     * Retrieve the {@link Resource} with the matching reference id.
     * <p>
     * If the specified reference id is not found in the {@link GetResourcesRequest}, <code>null</code> is returned.
     *
     * @param refId a reference identifier to correlate {@link GetResourcesRequest.Resource} records to the
     *              corresponding {@link GetResourcesResponse.Resource}
     * @return the matching {@link GetResourcesRequest.Resource} record; or <code>null</code> if not found
     */
    public final Resource getResource(final String refId) {
        Resource resource = null;
        for (Resource resourceIt : resources) {
            if (refId.equals(resourceIt.getRefId())) {
                resource = resourceIt;
                break;
            }
        }
        return resource;
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = -4412487267607877485L;

    /**
     * Represents a discrete resource object.
     */
    public static class Resource implements Serializable {

        /**
         * A reference to be used to associate resources received in the response with the request.
         */
        private final String refId;

        /**
         * ID for type of resource being requested.
         * e.g. "markings", "marking-values", "coverpage"
         */
        private final String resourceId;

        /**
         * Supplementary arguments associated with the resource request.
         */
        private final String args;

        /**
         * Constructor.
         *
         * @param refId      reference used to associate request with response data
         * @param resourceId ID for requested resource type
         */
        public Resource(final String refId, final String resourceId) {
            this(refId, resourceId, null);
        }

        /**
         * Constructor.
         *
         * @param refId      reference used to associate request with response data
         * @param resourceId ID for requested resource type
         * @param args       supplementary arguments associated with the request
         */
        public Resource(final String refId, final String resourceId, final String args) {
            this.refId = refId;
            this.resourceId = resourceId;
            this.args = args;
        }

        /**
         * @return the reference used to associate individual resource requests with response data
         */
        public final String getRefId() {
            return ((refId == null) ? "" : refId);
        }

        /**
         * @return the id associated with the requested resource
         */
        public final String getResourceId() {
            return ((resourceId == null) ? "" : resourceId);
        }

        /**
         * @return the args associated with the resource request
         */
        public final String getArgs() {
            return ((args == null) ? "" : args);
        }

        /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
        private static final long serialVersionUID = -969910446044409664L;
    }
}
