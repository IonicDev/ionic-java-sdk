package com.ionic.sdk.agent.request.createkey;

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

/**
 * Encapsulation of helper logic associated with CreateKeys SDK API.  Includes state associated with request, and
 * conversion of request object into json representation, for submission to IDC.
 */
public class CreateKeysMessage extends MessageBase {

    /**
     * The signature associated with the attributes of each request, used to verify the content of the response.
     */
    private final Properties csigs;

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
    public CreateKeysMessage(final ServiceProtocol protocol) throws IonicException {
        super(protocol);
        this.csigs = new Properties();
        this.msigs = new Properties();
    }

    /**
     * @return the signatures associated with the attributes of each request
     */
    public final Properties getCsigs() {
        return csigs;
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
        SdkData.checkTrue(requestBase instanceof CreateKeysRequest, SdkError.ISAGENT_ERROR);
        final CreateKeysRequest createKeysRequest = (CreateKeysRequest) requestBase;
        return Json.createObjectBuilder()
                .add(IDC.Payload.PROTECTION_KEYS, getJsonProtectionKeys(createKeysRequest))
                .build();
    }

    /**
     * Assemble the "protection keys" json associated with the request.
     *
     * @param createKeysRequest the user-generated object containing the attributes of the request
     * @return a {@link JsonArray} to be incorporated into the request payload
     * @throws IonicException on cryptography errors (used by protected attributes feature)
     */
    private JsonArray getJsonProtectionKeys(final CreateKeysRequest createKeysRequest)
            throws IonicException {
        final String cid = getCid();
        final Collection<JsonObject> jsonProtectionKeys = new ArrayList<JsonObject>();
        for (CreateKeysRequest.Key key : createKeysRequest.getKeys()) {
            final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            final String refId = key.getRefId();
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.REF, refId);
            JsonTarget.add(objectBuilder, IDC.Payload.QTY, key.getQuantity());
            final String cattrs = JsonIO.write(super.generateJsonAttrs(key.getAttributesMap()), false);
            final String mattrs = JsonIO.write(super.generateJsonAttrs(key.getMutableAttributesMap()), false);
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.CATTRS, cattrs);
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.MATTRS, mattrs);
            final String csig = getProtocol().signAttributes(cid, refId, null, csigs, cattrs, false);
            final String msig = getProtocol().signAttributes(cid, refId, null, msigs, mattrs, true);
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.CSIG, csig);
            JsonTarget.addNotNull(objectBuilder, IDC.Payload.MSIG, msig);
            final JsonObject jsonProtectionKey = objectBuilder.build();
            jsonProtectionKeys.add(jsonProtectionKey);
        }
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (JsonObject jsonProtectionKey : jsonProtectionKeys) {
            JsonTarget.addNotNull(jsonArrayBuilder, jsonProtectionKey);
        }
        return jsonArrayBuilder.build();
    }
}
