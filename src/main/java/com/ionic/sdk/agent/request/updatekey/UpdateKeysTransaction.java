package com.ionic.sdk.agent.request.updatekey;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.base.AgentTransactionBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.IonicServerException;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * An object encapsulating the server request and response for an Agent.updateKeys() request.
 */
public class UpdateKeysTransaction extends AgentTransactionBase {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Helper object for serialization of request to json for submission to KeyServices implementation.
     */
    private UpdateKeysMessage message;

    /**
     * Constructor.
     *
     * @param agent        the persistent data associated with the device's Secure Enrollment Profile
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public UpdateKeysTransaction(
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
        this.message = new UpdateKeysMessage(agent);
        final UpdateKeysRequest request = (UpdateKeysRequest) getRequestBase();
        final JsonObject jsonMessage = message.getJsonMessage(request, fingerprint);
        final String cid = message.getCid();
        final String resource = String.format(IDC.Resource.KEYS_UPDATE, IDC.Resource.SERVER_API_V24);
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
        final UpdateKeysRequest request = (UpdateKeysRequest) getRequestBase();
        final UpdateKeysResponse response = (UpdateKeysResponse) getResponseBase();
        //final String cid = response.getConversationId();
        final JsonObject jsonPayload = response.getJsonPayload();
        final JsonObject jsonData = JsonSource.getJsonObject(jsonPayload, IDC.Payload.DATA);
        // populate the keys into the response
        final JsonArray jsonProtectionKeys = JsonSource.getJsonArray(jsonData, IDC.Payload.PROTECTION_KEYS);
        for (JsonValue value : jsonProtectionKeys) {
            // deserialize each response key into a user-consumable object
            final JsonObject jsonProtectionKey = JsonSource.toJsonObject(value, IDC.Payload.PROTECTION_KEYS);
            final String id = JsonSource.getString(jsonProtectionKey, IDC.Payload.ID);
            final UpdateKeysRequest.Key keyQ = request.getKey(id);
            final String csigQ = "";
            final String msigQ = message.getMsigs().getProperty(id, "");
            final String prevcsig = keyQ.getForceUpdate() ? "" : keyQ.getAttributesSigBase64FromServer();
            final String prevmsig = keyQ.getForceUpdate() ? "" : keyQ.getMutableAttributesSigBase64FromServer();
            final String csig = JsonSource.getString(jsonProtectionKey, IDC.Payload.CSIG);
            final String msig = JsonSource.getString(jsonProtectionKey, IDC.Payload.MSIG);
            final String sigs = JsonSource.getString(jsonProtectionKey, IDC.Payload.SIGS);
            // verify each received response key
            final String macR = Value.join(IDC.Signature.DELIMITER_COMMA, prevcsig, csigQ, prevmsig, msigQ);
            final String mac = Value.join(IDC.Signature.DELIMITER, keyQ.getId(), macR);
            message.verifySignature(IDC.Payload.SIGS, sigs, mac, activeProfile.getAesCdEiProfileKey());
            final UpdateKeysResponse.Key keyA = new UpdateKeysResponse.Key(
                    keyQ, activeProfile.getDeviceId(), IDC.Metadata.KEYORIGIN_IONIC);
            keyA.setAttributesSigBase64FromServer(csig);
            keyA.setMutableAttributesSigBase64FromServer(msig);
            response.add(keyA);
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
                response.add(new UpdateKeysResponse.IonicError(keyId, 0, serverCode, serverMessage));
            }
        }
        if (JsonSource.isSize(jsonProtectionKeys, 0) && (jsonErrors != null)) {
            throw new IonicException(SdkError.ISAGENT_REQUESTFAILED, new IonicServerException(
                    SdkError.ISAGENT_STALE_KEY_ATTRIBUTES, message.getCid(), response));
        }
    }
}
