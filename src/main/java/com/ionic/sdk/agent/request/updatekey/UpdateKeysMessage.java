package com.ionic.sdk.agent.request.updatekey;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.MessageBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.json.JsonU;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Encapsulation of helper logic associated with CreateKeys SDK API.  Includes state associated with request, and
 * conversion of request object into json representation, for submission to IDC.
 */
public class UpdateKeysMessage extends MessageBase {

    /**
     * The signature associated with the mutable attributes of each request, used to verify the content of the response.
     */
    private final Properties msigs;

    /**
     * Constructor.
     *
     * @param agent the {@link com.ionic.sdk.key.KeyServices} implementation
     */
    public UpdateKeysMessage(final Agent agent) {
        super(agent, AgentTransactionUtil.generateConversationIdV(agent.getActiveProfile().getDeviceId()));
        this.msigs = new Properties();
    }

    /**
     * @return the signatures associated with the mutable attributes of each request
     */
    public final Properties getMsigs() {
        return msigs;
    }

    /**
     * Assemble the "data" json associated with the request.
     *
     * @param requestBase the user-generated object containing the attributes of the request
     * @return a {@link JsonObject} to be incorporated into the request payload
     * @throws IonicException on cryptography errors
     *                      final CreateKeysRequest createKeysRequest
     */
    @Override
    protected final JsonObject getJsonData(final AgentRequestBase requestBase) throws IonicException {
        return Json.createObjectBuilder()
                .add(IDC.Payload.PROTECTION_KEYS, getJsonProtectionKeys((UpdateKeysRequest) requestBase))
                .build();
    }

    /**
     * Assemble the "protection keys" json associated with the request.
     *
     * @param updateKeysRequest the user-generated object containing the attributes of the request
     * @return a {@link JsonArray} to be incorporated into the request payload
     * @throws IonicException on cryptography errors
     */
    private JsonArray getJsonProtectionKeys(final UpdateKeysRequest updateKeysRequest)
            throws IonicException {
        final Collection<JsonObject> jsonProtectionKeys = new ArrayList<JsonObject>();
        for (UpdateKeysRequest.Key key : updateKeysRequest.getKeys()) {
            final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            final String id = key.getId();
            objectBuilder.add(IDC.Payload.ID, id).add(IDC.Payload.FORCE, key.isForceUpdate());
            // spec says omit or blank, but due to server bug must be blank for now (4/2018)
            final String csig = "";
            final String prevcsig = key.isForceUpdate() ? "" : key.getAttributesSigBase64FromServer();
            final String prevmsig = key.isForceUpdate() ? "" : key.getMutableAttributesSigBase64FromServer();
            JsonU.addNotNull(objectBuilder, IDC.Payload.CSIG, csig);
            JsonU.addNotNull(objectBuilder, IDC.Payload.PREVCSIG, prevcsig);
            JsonU.addNotNull(objectBuilder, IDC.Payload.PREVMSIG, prevmsig);
            final String extra = (key.isForceUpdate() ? IDC.Signature.FORCE : null);
            final String mattrs = JsonU.toJson(getJsonAttrs(key.getMutableAttributes()), false);
            final String msig = super.buildSignedAttributes(id, extra, mattrs, true);
            msigs.put(id, msig);
            JsonU.addNotNull(objectBuilder, IDC.Payload.MATTRS, mattrs);
            JsonU.addNotNull(objectBuilder, IDC.Payload.MSIG, msig);
            final JsonObject jsonProtectionKey = objectBuilder.build();
            jsonProtectionKeys.add(jsonProtectionKey);
        }
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (JsonObject jsonProtectionKey : jsonProtectionKeys) {
            jsonArrayBuilder.add(jsonProtectionKey);
        }
        return jsonArrayBuilder.build();
    }

    /**
     * Assemble the attributes json associated with the request.
     *
     * @param keyAttributes the key attributes to be associated with
     * @return a {@link JsonObject} to be incorporated into the request payload
     */
    private JsonObject getJsonAttrs(final KeyAttributesMap keyAttributes) {
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        for (Map.Entry<String, List<String>> entry : keyAttributes.entrySet()) {
            final String key = entry.getKey();
            final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (final String value : entry.getValue()) {
                arrayBuilder.add(value);
            }
            objectBuilder.add(key, arrayBuilder.build());
        }
        return objectBuilder.build();
    }
}
