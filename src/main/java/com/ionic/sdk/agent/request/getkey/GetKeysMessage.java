package com.ionic.sdk.agent.request.getkey;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.MessageBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.json.JsonU;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Encapsulation of helper logic associated with GetKeys SDK API.  Includes state associated with request, and
 * conversion of request object into json representation, for submission to IDC.
 */
public class GetKeysMessage extends MessageBase {

    /**
     * Constructor.
     *
     * @param agent the {@link com.ionic.sdk.key.KeyServices} implementation
     */
    public GetKeysMessage(final Agent agent) {
        super(agent, AgentTransactionUtil.generateConversationIdV(agent.getActiveProfile().getDeviceId()));
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
                .add(IDC.Payload.PROTECTION_KEYS, getJsonProtectionKeys((GetKeysRequest) requestBase))
                .build();
    }

    /**
     * Assemble the "protection keys" json associated with the request.
     *
     * @param getKeysRequest the user-generated object containing the attributes of the request
     * @return a {@link JsonArray} to be incorporated into the request payload
     */
    private JsonArray getJsonProtectionKeys(final GetKeysRequest getKeysRequest) {
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String keyId : getKeysRequest.getKeyIds()) {
            jsonArrayBuilder.add(keyId);
        }
        return jsonArrayBuilder.build();
    }

    /**
     * Extract the attributes from the json structure in the parameter string.
     *
     * @param attrs the json-serialized attributes string in the payload
     * @return the map of attribute keys to the list of values associated with the key
     */
    public final KeyAttributesMap getJsonAttrs(final String attrs) {
        final KeyAttributesMap keyAttributes = new KeyAttributesMap();
        if (attrs != null) {
            final JsonObject jsonObject = JsonU.getJsonObject(attrs);
            for (Map.Entry<String, JsonValue> entry : jsonObject.entrySet()) {
                List<String> values = new ArrayList<String>();
                final String key = entry.getKey();
                final JsonValue value = entry.getValue();
                if (value instanceof JsonArray) {
                    final JsonArray jsonArray = (JsonArray) value;
                    for (final JsonValue jsonValue : jsonArray) {
                        if (jsonValue instanceof JsonString) {
                            values.add(((JsonString) jsonValue).getString());
                        } else {
                            values.add(jsonValue.toString());
                        }
                    }
                }
                keyAttributes.put(key, values);
            }
        }
        return keyAttributes;
    }
}
