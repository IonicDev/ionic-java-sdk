package com.ionic.sdk.agent.request.getkey;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.key.KeyObligationsMap;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.base.AgentTransactionBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.httpclient.Http;
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * An object encapsulating the server request and response for an Agent.getKeys() request.
 */
public class GetKeysTransaction extends AgentTransactionBase {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Helper object for serialization of request to json for submission to KeyServices implementation.
     */
    private GetKeysMessage message;

    /**
     * Constructor.
     *
     * @param agent        the persistent data associated with the device's Secure Enrollment Profile
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public GetKeysTransaction(
            final Agent agent, final AgentRequestBase requestBase, final AgentResponseBase responseBase) {
        super(agent, requestBase, responseBase);
    }

    /**
     * Assemble a client request for submission to the IDC infrastructure.
     *
     * @param fingerprint authentication data associated with the client state to be included in the request
     * @return a request object, ready for submission to the server
     * @throws IonicException on failure to assemble the request
     */
    @Override
    protected final HttpRequest buildHttpRequest(final Properties fingerprint) throws IonicException {
        final Agent agent = getAgent();
        this.message = new GetKeysMessage(agent);
        final GetKeysRequest request = (GetKeysRequest) getRequestBase();
        // check for one or the other, protection keys or external ids, must have one
        if (request.getExternalIds().isEmpty() && request.getKeyIds().isEmpty()) {
            throw new IonicException(SdkError.ISAGENT_BADREQUEST, "No key ids or external ids in get key request.");
        }
        final JsonObject jsonMessage = message.getJsonMessage(request, fingerprint);
        final String cid = message.getCid();
        final String resource = String.format(IDC.Resource.KEYS_GET, IDC.Resource.SERVER_API_V24);
        final String envelope = JsonIO.write(jsonMessage, false);
        //logger.finest(envelope);  // plaintext json; IDC http entity (for debugging)
        // assemble the secured (outer) HTTP payload
        final DeviceProfile activeProfile = agent.getActiveProfile();
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(activeProfile.getAesCdIdcProfileKey());
        cipher.setAuthData(Transcoder.utf8().decode(cid));
        final String envelopeSecureBase64 = cipher.encryptToBase64(envelope);
        final JsonObject payload = Json.createObjectBuilder()
                .add(IDC.Payload.CID, cid)
                .add(IDC.Payload.ENVELOPE, envelopeSecureBase64)
                .build();
        final String entitySecure = JsonIO.write(payload, false);
        logger.fine(entitySecure);
        // assemble the HTTP request to be sent to the server
        final URL url = AgentTransactionUtil.getProfileUrl(activeProfile);
        final ByteArrayInputStream bis = new ByteArrayInputStream(
                Transcoder.utf8().decode(JsonIO.write(payload, false)));
        return new HttpRequest(url, Http.Method.POST, resource, getHttpHeaders(), bis);
    }

    /**
     * Parse and process the server response to the client request.
     *
     * @param httpRequest  the server request
     * @param httpResponse the server response
     * @throws IonicException on errors in the server response
     */
    @Override
    protected final void parseHttpResponse(
            final HttpRequest httpRequest, final HttpResponse httpResponse) throws IonicException {
        // unwrap the server response
        parseHttpResponseBase(httpRequest, httpResponse, message.getCid());
        // apply logic specific to the response type
        final Agent agent = getAgent();
        final DeviceProfile activeProfile = agent.getActiveProfile();
        //final GetKeysRequest request = (GetKeysRequest) getRequestBase();
        final GetKeysResponse response = (GetKeysResponse) getResponseBase();
        final String cid = response.getConversationId();
        final JsonObject jsonPayload = response.getJsonPayload();
        final JsonObject jsonData = JsonSource.getJsonObject(jsonPayload, IDC.Payload.DATA);
        final JsonArray jsonProtectionKeys = JsonSource.getJsonArray(jsonData, IDC.Payload.PROTECTION_KEYS);
        for (JsonValue value : jsonProtectionKeys) {
            // deserialize each response key into a user-consumable object
            final JsonObject jsonProtectionKey = JsonSource.toJsonObject(value, IDC.Payload.PROTECTION_KEYS);
            final String id = JsonSource.getString(jsonProtectionKey, IDC.Payload.ID);
            final String keyHex = JsonSource.getString(jsonProtectionKey, IDC.Payload.KEY);
            final String cattrs = JsonSource.getString(jsonProtectionKey, IDC.Payload.CATTRS);
            final String mattrs = JsonSource.getString(jsonProtectionKey, IDC.Payload.MATTRS);
            final String csig = JsonSource.getString(jsonProtectionKey, IDC.Payload.CSIG);
            final String msig = JsonSource.getString(jsonProtectionKey, IDC.Payload.MSIG);
            final String authData = Value.join(IDC.Signature.DELIMITER, cid, id, csig, msig);
            // verify each received response key
            final AesGcmCipher cipherEi = new AesGcmCipher();
            cipherEi.setKey(activeProfile.getAesCdEiProfileKey());
            cipherEi.setAuthData(Transcoder.utf8().decode(authData));
            final byte[] clearBytesKey = cipherEi.decrypt(CryptoUtils.hexToBin(keyHex));
            // verify each received response attributes
            message.verifySignature(IDC.Payload.CSIG, csig, cattrs, clearBytesKey);
            message.verifySignature(IDC.Payload.MSIG, msig, mattrs, clearBytesKey);
            final String deviceId = agent.getActiveProfile().getDeviceId();
            final KeyAttributesMap cattrsKey = message.getJsonAttrs(cattrs, id, clearBytesKey);
            final KeyAttributesMap mattrsKey = message.getJsonAttrs(mattrs, id, clearBytesKey);
            response.add(new GetKeysResponse.Key(id, clearBytesKey, deviceId, cattrsKey, mattrsKey,
                    new KeyObligationsMap(), IDC.Metadata.KEYORIGIN_IONIC, csig, msig));
        }
        // populate the errors into the response
        final JsonObject jsonErrors = JsonSource.getJsonObjectNullable(jsonData, IDC.Payload.ERROR_MAP);
        if (jsonErrors != null) {
            final Iterator<Map.Entry<String, JsonValue>> iterator = JsonSource.getIterator(jsonErrors);
            while (iterator.hasNext()) {
                final Map.Entry<String, JsonValue> entry = iterator.next();
                // deserialize each response key into a user-consumable object
                final String keyId = entry.getKey();
                final JsonObject error = JsonSource.toJsonObject(entry.getValue(), IDC.Payload.ERROR_MAP);
                final int serverCode = JsonSource.getInt(error, IDC.Payload.CODE);
                final String serverMessage = JsonSource.getString(error, IDC.Payload.MESSAGE);
                response.add(new GetKeysResponse.IonicError(keyId, 0, serverCode, serverMessage));
            }
        }
        // optional query results map
        final JsonObject jsonQueries = JsonSource.getJsonObjectNullable(jsonData, IDC.Payload.QUERY_RESULTS);
        if (jsonQueries != null) {
            // grab the request
            final GetKeysRequest request = (GetKeysRequest) getRequestBase();
            // for each external id we requested, see if there's optional errors or key'd responses
            for (String extId : request.getExternalIds()) {
                // response reference for each external id must exist (even if empty)
                final JsonObject queryResult = JsonSource.getJsonObject(jsonQueries, extId);
                // mapped key ids are optional
                final JsonArray keyArray = JsonSource.getJsonArrayNullable(queryResult, IDC.Payload.IDS);
                if (keyArray != null) {
                    final List<String> values = new ArrayList<String>();
                    for (final JsonValue jsonValue : keyArray) {
                        values.add(JsonSource.toString(jsonValue));
                    }
                    response.add(new GetKeysResponse.QueryResult(extId, values));
                }
                // errors are optional
                final JsonObject errorObj = JsonSource.getJsonObjectNullable(queryResult, IDC.Payload.ERROR);
                if (errorObj != null) {
                    final int errorCode = JsonSource.getInt(errorObj, IDC.Payload.CODE);
                    final String errorMessage = JsonSource.getString(errorObj, IDC.Payload.MESSAGE);
                    response.add(new GetKeysResponse.QueryResult(extId, errorCode, errorMessage));
                }
            }
        }
    }
}
