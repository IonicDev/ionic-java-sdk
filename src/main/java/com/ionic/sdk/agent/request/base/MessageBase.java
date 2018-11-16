package com.ionic.sdk.agent.request.base;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.hash.Hash;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.crypto.CryptoUtils;
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
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Encapsulation of helper logic associated with an SDK IDC call.  Includes state associated with request, and
 * conversion of request object into json representation, for submission to IDC.
 */
public abstract class MessageBase {

    /**
     * The {@link com.ionic.sdk.key.KeyServices} implementation.
     */
    private final Agent agent;

    /**
     * A unique id used to identify a particular server transaction, and to help secure its content during transit.
     */
    private final String cid;

    /**
     * Constructor.
     *
     * @param agent the {@link com.ionic.sdk.key.KeyServices} implementation
     * @param cid   the conversation id associated with the client request
     * @throws IonicException on NULL input
     */
    public MessageBase(final Agent agent, final String cid) throws IonicException {
        this.agent = agent;
        this.cid = cid;
        SdkData.checkNotNull(agent, Agent.class.getName());
        SdkData.checkNotNull(cid, IDC.Payload.CID);
    }

    /**
     * @return the KeyServices implementation
     */
    public final Agent getAgent() {
        return agent;
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
                .add(IDC.Payload.META, AgentTransactionUtil.buildStandardJsonMeta(agent, requestBase, fingerprint))
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
     * Calculate the signature associated with the request attributes.
     * <p>
     * a2\IonicAgents\SDK\ISAgentSDK\ISAgentLib\ISAgentTransactionUtil.cpp:buildSignedAttributes()
     *
     * @param keyId      the identifier for the key used in the signature calculation
     * @param extra      additional data used in the signature calculation
     * @param attrs      the attributes to be associated with any keys generated as part of this request
     * @param areMutable a signal used to construct appropriate authData to use in signature
     * @return a signature to be incorporated into the request payload (for verification)
     * @throws IonicException on cryptography errors
     */
    protected final String buildSignedAttributes(final String keyId, final String extra,
                                                 final String attrs, final boolean areMutable) throws IonicException {
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(agent.getActiveProfile().getAesCdEiProfileKey());
        final String authData = areMutable
                ? Value.join(IDC.Signature.DELIMITER, cid, IDC.Signature.MUTABLE, keyId, extra)
                : Value.join(IDC.Signature.DELIMITER, cid, keyId, extra);
        cipher.setAuthData(Transcoder.utf8().decode(authData));
        final byte[] plainText = new Hash().sha256(Transcoder.utf8().decode(attrs));
        return cipher.encryptToBase64(plainText);
    }

    /**
     * Evaluate attribute names to determine if "protected attribute" logic should apply.
     *
     * @param name the name of the attribute
     * @return true iff the attribute value should be protected
     * @see <a href="https://dev.ionic.com/device-requests/keys/create.html">Create Keys</a>
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
        final String value = JsonSource.toString(jsonArray);
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(agent.getActiveProfile().getAesCdEiProfileKey());
        cipher.setAuthData(Transcoder.utf8().decode(name));
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        final String encryptedJsonString = cipher.encryptToBase64(value);
        JsonTarget.addNotNull(arrayBuilder, encryptedJsonString);
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
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(key);
        cipher.setAuthData(Transcoder.utf8().decode(keyId));
        final String jsonStringArray = cipher.decryptBase64ToString(value);
        return JsonIO.readArray(jsonStringArray);
    }

    /**
     * Verify the signature provided in the response against a locally generated signature.
     *
     * @param name        the attribute whose value should be checked
     * @param sigExpected the (server-provided) value
     * @param attrs       the source material for the signature
     * @param key         the key to use in calculating the signature
     * @throws IonicException on cryptography errors
     */
    public final void verifySignature(
            final String name, final String sigExpected, final String attrs, final byte[] key) throws IonicException {
        if (sigExpected != null) {
            final String sigActual = CryptoUtils.hmacSHA256Base64(Transcoder.utf8().decode(attrs), key);
            if (!sigExpected.equals(sigActual)) {
                throw new IonicException(SdkError.ISAGENT_INVALIDVALUE,
                        new GeneralSecurityException(String.format("%s:[%s]!=[%s]", name, sigExpected, sigActual)));
            }
        }
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
