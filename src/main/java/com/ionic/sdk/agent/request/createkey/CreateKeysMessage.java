package com.ionic.sdk.agent.request.createkey;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.MessageBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.hash.Hash;
import com.ionic.sdk.core.value.Value;
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
     * @param agent the {@link com.ionic.sdk.key.KeyServices} implementation
     */
    public CreateKeysMessage(final Agent agent) {
        super(agent, AgentTransactionUtil.generateConversationIdV(agent.getActiveProfile().getDeviceId()));
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
     *                      final CreateKeysRequest createKeysRequest
     */
    @Override
    protected final JsonObject getJsonData(final AgentRequestBase requestBase) throws IonicException {
        return Json.createObjectBuilder()
                .add(IDC.Payload.PROTECTION_KEYS, getJsonProtectionKeys((CreateKeysRequest) requestBase))
                .build();
    }

    /**
     * Assemble the "protection keys" json associated with the request.
     *
     * @param createKeysRequest the user-generated object containing the attributes of the request
     * @return a {@link JsonArray} to be incorporated into the request payload
     * @throws IonicException on cryptography errors
     */
    private JsonArray getJsonProtectionKeys(final CreateKeysRequest createKeysRequest)
            throws IonicException {
        final Collection<JsonObject> jsonProtectionKeys = new ArrayList<JsonObject>();
        for (CreateKeysRequest.Key key : createKeysRequest.getKeys()) {
            final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            final String refId = key.getRefId();
            objectBuilder.add(IDC.Payload.REF, refId).add(IDC.Payload.QTY, key.getQuantity());
            final String cattrs = JsonU.toJson(getJsonAttrs(key.getAttributesMap()), false);
            final String csig = getStringSIG(cattrs, refId, false);
            objectBuilder.add(IDC.Payload.CATTRS, cattrs).add(IDC.Payload.CSIG, csig);
            csigs.put(refId, csig);
            final String mattrs = JsonU.toJson(getJsonAttrs(key.getMutableAttributes()), false);
            final String msig = getStringSIG(mattrs, refId, true);
            objectBuilder.add(IDC.Payload.MATTRS, mattrs).add(IDC.Payload.MSIG, msig);
            msigs.put(refId, msig);
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

    /**
     * Calculate the signature associated with the request attributes.
     *
     * @param attrs      the attributes to be associated with any keys generated as part of this request
     * @param refId      a reference to be used to associate any keys received in the response with the request
     * @param areMutable a signal used to construct appropriate authData to use in signature
     * @return a signature to be incorporated into the request payload (for verification)
     * @throws IonicException on cryptography errors
     */
    private String getStringSIG(
            final String attrs, final String refId, final boolean areMutable) throws IonicException {
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(getAgent().getActiveProfile().getAesCdEiProfileKey());
        final String authData = areMutable
                ? Value.join(IDC.Signature.DELIMITER, getCid(), IDC.Signature.MUTABLE, refId)
                : Value.join(IDC.Signature.DELIMITER, getCid(), refId);
        cipher.setAuthData(Transcoder.utf8().decode(authData));
        final byte[] plainText = new Hash().sha256(Transcoder.utf8().decode(attrs));
        return cipher.encryptToBase64(plainText);
    }
}
