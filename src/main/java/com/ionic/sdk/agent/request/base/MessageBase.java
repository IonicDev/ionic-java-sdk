package com.ionic.sdk.agent.request.base;

import com.ionic.sdk.agent.ServiceProtocol;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.json.JsonTarget;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Encapsulation of helper logic associated with an SDK IDC call.  Includes state associated with request, and
 * conversion of request object into json representation, for submission to IDC.
 */
public abstract class MessageBase {

    /**
     * The protocol of the {@link com.ionic.sdk.key.KeyServices} client (authentication, state, assembly of
     * service request payloads).
     */
    private final ServiceProtocol protocol;

    /**
     * The unique Ionic conversation ID.  It is supplied by the client in the corresponding request, and is used to
     * identify and correlate a particular server transaction, and to help secure its content during transit.
     */
    private final String cid;

    /**
     * Constructor.
     *
     * @param protocol the protocol of the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @throws IonicException on NULL input
     */
    public MessageBase(final ServiceProtocol protocol) throws IonicException {
        SdkData.checkNotNull(protocol, ServiceProtocol.class.getName());
        this.protocol = protocol;
        this.cid = protocol.generateCid();
    }

    /**
     * @return the protocol of the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     */
    public final ServiceProtocol getProtocol() {
        return protocol;
    }

    /**
     * @return the conversation id associated with the client request
     */
    public final String getCid() {
        return cid;
    }

    /**
     * Assemble the json associated with the request.  This json will be securely wrapped in a plaintext request to be
     * sent to the server.
     *
     * @param requestBase the user-generated object containing the attributes of the request
     * @param fingerprint authentication data associated with the client state to be included in the request
     * @return a {@link JsonObject} to be incorporated into the request payload
     * @throws IonicException on errors
     */
    public final JsonObject getJsonMessage(
            final AgentRequestBase requestBase, final Properties fingerprint) throws IonicException {
        return Json.createObjectBuilder()
                .add(IDC.Payload.DATA, getJsonData(requestBase))
                .add(IDC.Payload.META, AgentTransactionUtil.buildStandardJsonMeta(protocol, requestBase, fingerprint))
                .build();
    }

    /**
     * Assemble the attributes json associated with the request.
     *
     * @param keyAttributes the key attributes to be associated with
     * @return a {@link JsonObject} to be incorporated into the request payload
     * @throws IonicException on cryptography errors (used by protected attributes feature)
     */
    protected final JsonObject generateJsonAttrs(final KeyAttributesMap keyAttributes) throws IonicException {
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        for (Map.Entry<String, List<String>> entry : keyAttributes.entrySet()) {
            final String key = entry.getKey();
            final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (final String value : entry.getValue()) {
                JsonTarget.addNotNull(arrayBuilder, value);
            }
            final JsonArray values = arrayBuilder.build();
            if (isIonicProtect(key)) {
                JsonTarget.addNotNull(objectBuilder, key, encryptIonicAttrs(key, values));
            } else {
                JsonTarget.addNotNull(objectBuilder, key, values);
            }
        }
        return objectBuilder.build();
    }

    /**
     * Evaluate attribute names to determine if "protected attribute" logic should apply.
     *
     * @param name the name of the attribute
     * @return true iff the attribute value should be protected
     * @see <a href="https://dev.ionic.com/device-requests/keys/create.html" target="_blank">Create Keys</a>
     */
    protected final boolean isIonicProtect(final String name) {
        final boolean isIonicProtected = (name.startsWith(IDC.Protect.PREFIX));
        final boolean isIonicIntegrityHash = (name.equals(IDC.Protect.INTEGRITY_HASH));
        return (isIonicProtected || isIonicIntegrityHash);
    }

    /**
     * Encrypt the attributes described by the input parameters.
     *
     * @param name      the name of the attribute to encrypt
     * @param jsonArray the values of the attributes
     * @return the base64, Ionic-protected representation of the attribute values
     * @throws IonicException on cryptography initialization / execution failures
     */
    protected final JsonArray encryptIonicAttrs(final String name, final JsonArray jsonArray) throws IonicException {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        JsonTarget.addNotNull(arrayBuilder, protocol.protectAttributes(name, JsonSource.toString(jsonArray)));
        return arrayBuilder.build();
    }

    /**
     * Decrypt the attributes described by the input parameters.
     *
     * @param jsonValue the encrypted attribute json string
     * @param keyId     the key id used as AAD
     * @param key       the key to decrypt with
     * @return the plaintext json array of values, decrypted
     * @throws IonicException on cryptography initialization / execution failures
     */
    protected final JsonArray decryptIonicAttrs(final JsonValue jsonValue, final String keyId,
                                                final byte[] key) throws IonicException {
        // encrypted attributes are a single entry json array of encrypted values
        final JsonArray jsonArray = JsonSource.toJsonArray(jsonValue, JsonValue.class.getName());
        SdkData.checkTrue(JsonSource.isSize(jsonArray, 1), SdkError.ISAGENT_INVALIDVALUE, JsonArray.class.getName());
        final JsonValue jsonValueIt = JsonSource.getIterator(jsonArray).next();  // grab the first and only entry
        final String value = JsonSource.toString(jsonValueIt);
        final String jsonStringArray = protocol.unprotectAttributes(keyId, value, key);
        return JsonIO.readArray(jsonStringArray);
    }

    /**
     * Assemble the "data" json associated with the request.
     *
     * @param requestBase the user-generated object containing the attributes of the request
     * @return a {@link JsonObject} to be incorporated into the request payload
     * @throws IonicException on errors
     */
    protected abstract JsonValue getJsonData(AgentRequestBase requestBase) throws IonicException;
}
