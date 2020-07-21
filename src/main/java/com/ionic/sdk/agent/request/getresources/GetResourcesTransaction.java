package com.ionic.sdk.agent.request.getresources;

import com.ionic.sdk.agent.ServiceProtocol;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.base.AgentTransactionBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.httpclient.Http;
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.util.Properties;

/**
 * An object encapsulating the server request and response for an Agent.getResources() request.
 */
@InternalUseOnly
public class GetResourcesTransaction extends AgentTransactionBase {

    /**
     * Helper object for serialization of request to json for submission to server.
     */
    private GetResourcesMessage message;

    /**
     * Constructor.
     *
     * @param protocol     the protocol used by the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public GetResourcesTransaction(
            final ServiceProtocol protocol, final AgentRequestBase requestBase, final AgentResponseBase responseBase) {
        super(protocol, requestBase, responseBase);
        this.message = null;
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
        this.message = new GetResourcesMessage(getProtocol());
        final AgentRequestBase agentRequestBase = getRequestBase();
        SdkData.checkTrue(agentRequestBase instanceof GetResourcesRequest, SdkError.ISAGENT_ERROR);
        final GetResourcesRequest request = (GetResourcesRequest) agentRequestBase;
        final JsonObject jsonMessage = message.getJsonMessage(request, fingerprint);
        final String resource = String.format(IDC.Resource.RESOURCES_GET, IDC.Resource.SERVER_API_V24);
        // assemble the inner HTTP payload
        final byte[] envelope = Transcoder.utf8().decode(JsonIO.write(jsonMessage, false));
        // assemble the outer (secured) HTTP payload
        final byte[] envelopeSecure = getProtocol().transformRequestPayload(envelope, message.getCid());
        // assemble the HTTP request to be sent to the server
        final ByteArrayInputStream bis = new ByteArrayInputStream(envelopeSecure);
        return new HttpRequest(getProtocol().getUrl(), Http.Method.POST, resource, getHttpHeaders(), bis);
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
        //final Agent agent = getAgent();
        //final DeviceProfile activeProfile = agent.getActiveProfile();
        //final GetResourcesRequest request = (GetResourcesRequest) getRequestBase();
        final AgentResponseBase agentResponseBase = getResponseBase();
        SdkData.checkTrue(agentResponseBase instanceof GetResourcesResponse, SdkError.ISAGENT_ERROR);
        final GetResourcesResponse response = (GetResourcesResponse) agentResponseBase;
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
