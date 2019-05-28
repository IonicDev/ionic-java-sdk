package com.ionic.sdk.agent.request.updatekey;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.MessageBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonTarget;

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
import java.util.TreeSet;

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
     * @throws IonicException on random number generation failure
     */
    UpdateKeysMessage(final Agent agent) throws IonicException {
        super(agent, AgentTransactionUtil.generateConversationId(
                agent.getActiveProfile(), IDC.Message.SERVER_API_CID));
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
        // check for duplicate key IDs specified in request
        final Collection<String> ids = new TreeSet<String>();
        final Collection<JsonObject> jsonProtectionKeys = new ArrayList<JsonObject>();
        for (UpdateKeysRequest.Key key : updateKeysRequest.getKeys()) {
            final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            final String id = key.getId();
            ids.add(id);
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.ID, id);
            JsonTarget.add(objectBuilder, IDC.Payload.FORCE, key.getForceUpdate());
            // spec says omit or blank, but due to server bug must be blank for now (4/2018)
            final String csig = "";
            final String prevcsig = key.getForceUpdate() ? "" : key.getAttributesSigBase64FromServer();
            final String prevmsig = key.getForceUpdate() ? "" : key.getMutableAttributesSigBase64FromServer();
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.CSIG, csig);
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.PREVCSIG, prevcsig);
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.PREVMSIG, prevmsig);
            final String extra = (key.getForceUpdate() ? IDC.Signature.FORCE : null);
            final String mattrs = JsonIO.write(super.generateJsonAttrs(key.getMutableAttributesMap()), false);
            final String msig = super.buildSignedAttributes(id, extra, mattrs, true);
            msigs.put(id, msig);
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.MATTRS, mattrs);
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.MSIG, msig);
            final JsonObject jsonProtectionKey = objectBuilder.build();
            jsonProtectionKeys.add(jsonProtectionKey);
        }
        // check for duplicate key IDs specified in request
        SdkData.checkTrue(updateKeysRequest.getKeys().size() == ids.size(), SdkError.ISAGENT_DUPLICATE_KEY);
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (JsonObject jsonProtectionKey : jsonProtectionKeys) {
            JsonTarget.addNotNull(jsonArrayBuilder, jsonProtectionKey);
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
                JsonTarget.addNotNull(arrayBuilder, value);
            }
            JsonTarget.addNotNull(objectBuilder, key, arrayBuilder.build());
        }
        return objectBuilder.build();
    }
}
