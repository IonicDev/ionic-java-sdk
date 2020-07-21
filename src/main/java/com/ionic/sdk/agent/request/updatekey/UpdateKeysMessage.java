package com.ionic.sdk.agent.request.updatekey;

import com.ionic.sdk.agent.ServiceProtocol;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.MessageBase;
import com.ionic.sdk.agent.service.IDC;
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
     * @param protocol the protocol used by the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @throws IonicException on random number generation failure
     */
    UpdateKeysMessage(final ServiceProtocol protocol) throws IonicException {
        super(protocol);
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
     */
    @Override
    protected final JsonObject getJsonData(final AgentRequestBase requestBase) throws IonicException {
        SdkData.checkTrue(requestBase instanceof UpdateKeysRequest, SdkError.ISAGENT_ERROR);
        final UpdateKeysRequest updateKeysRequest = (UpdateKeysRequest) requestBase;
        return Json.createObjectBuilder()
                .add(IDC.Payload.PROTECTION_KEYS, getJsonProtectionKeys(updateKeysRequest))
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
            final String msig = getProtocol().signAttributes(getCid(), id, extra, msigs, mattrs, true);
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
}
