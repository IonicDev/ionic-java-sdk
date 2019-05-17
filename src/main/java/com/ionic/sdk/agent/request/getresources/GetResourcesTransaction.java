package com.ionic.sdk.agent.request.getresources;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.base.AgentTransactionBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
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
import java.util.Properties;
import java.util.logging.Logger;

/**
 * An object encapsulating the server request and response for an Agent.getResources() request.
 */
public class GetResourcesTransaction extends AgentTransactionBase {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Helper object for serialization of request to json for submission to server.
     */
    private GetResourcesMessage message;

    /**
     * Constructor.
     *
     * @param agent        the persistent data associated with the device's Secure Enrollment Profile
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public GetResourcesTransaction(
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
        this.message = new GetResourcesMessage(agent);
        final GetResourcesRequest request = (GetResourcesRequest) getRequestBase();
        final JsonObject jsonMessage = message.getJsonMessage(request, fingerprint);
        final String cid = message.getCid();
        final String resource = String.format(IDC.Resource.RESOURCES_GET, IDC.Resource.SERVER_API_V23);
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
        final Agent agent = getAgent();
        final DeviceProfile deviceProfile = agent.getActiveProfile();
        // unwrap the server response
        parseHttpResponseBase(httpRequest, httpResponse, message.getCid(), deviceProfile);
        // apply logic specific to the response type
        //final Agent agent = getAgent();
        //final DeviceProfile activeProfile = agent.getActiveProfile();
        //final GetResourcesRequest request = (GetResourcesRequest) getRequestBase();
        final GetResourcesResponse response = (GetResourcesResponse) getResponseBase();
        //final String cid = response.getConversationId();
        final JsonObject jsonPayload = response.getJsonPayload();
        final JsonObject jsonData = JsonSource.getJsonObject(jsonPayload, IDC.Payload.DATA);
        final JsonArray jsonResponses = JsonSource.getJsonArray(jsonData, IDC.Payload.RESPONSES);
        for (JsonValue value : jsonResponses) {
            // deserialize each response key into a user-consumable object
            final JsonObject jsonResponse = JsonSource.toJsonObject(value, IDC.Payload.RESPONSES);
            final String id = JsonSource.getString(jsonResponse, IDC.Payload.ID);
            final String data = JsonSource.getString(jsonResponse, IDC.Payload.DATA);
            final String error = JsonSource.getString(jsonResponse, IDC.Payload.ERROR);
            response.add(new GetResourcesResponse.Resource(id, data, error));
        }
    }
}
