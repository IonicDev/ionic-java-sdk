package com.ionic.sdk.agent.request.getresources;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.MessageBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.json.JsonTarget;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Encapsulation of helper logic associated with GetKeys SDK API.  Includes state associated with request, and
 * conversion of request object into json representation, for submission to IDC.
 */
public class GetResourcesMessage extends MessageBase {

    /**
     * Constructor.
     *
     * @param agent the {@link com.ionic.sdk.key.KeyServices} implementation
     * @throws IonicException on random number generation failure
     */
    public GetResourcesMessage(final Agent agent) throws IonicException {
        super(agent, AgentTransactionUtil.generateConversationId(agent.getActiveProfile().getDeviceId()));
    }

    /**
     * Assemble the "data" json associated with the request.
     *
     * @param requestBase the user-generated object containing the attributes of the request
     * @return a {@link JsonObject} to be incorporated into the request payload
     */
    @Override
    protected final JsonObject getJsonData(final AgentRequestBase requestBase) {
        return Json.createObjectBuilder()
                .add(IDC.Payload.REQUESTS, getJsonRequests((GetResourcesRequest) requestBase))
                .build();
    }

    /**
     * Assemble the "resources" json associated with the request.
     *
     * @param getResourcesRequest the user-generated object containing the attributes of the request
     * @return a {@link JsonArray} to be incorporated into the request payload
     */
    private JsonArray getJsonRequests(final GetResourcesRequest getResourcesRequest) {
        final Collection<JsonObject> jsonRequests = new ArrayList<JsonObject>();
        for (GetResourcesRequest.Resource resource : getResourcesRequest.getResources()) {
            final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.ID, resource.getRefId());
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.RESOURCE, resource.getResourceId());
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.ARGS, resource.getArgs());
            final JsonObject jsonRequest = objectBuilder.build();
            jsonRequests.add(jsonRequest);
        }
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (JsonObject jsonRequest : jsonRequests) {
            JsonTarget.addNotNull(jsonArrayBuilder, jsonRequest);
        }
        return jsonArrayBuilder.build();
    }
}
