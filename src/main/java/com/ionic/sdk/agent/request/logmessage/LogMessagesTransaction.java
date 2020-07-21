package com.ionic.sdk.agent.request.logmessage;

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

import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.util.Properties;

/**
 * An object encapsulating the server request and response for an Agent.logMessages() request.
 */
@InternalUseOnly
public class LogMessagesTransaction extends AgentTransactionBase {

    /**
     * Helper object for serialization of request to json for submission to server.
     */
    private LogMessagesMessage message;

    /**
     * Constructor.
     *
     * @param protocol     the protocol used by the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public LogMessagesTransaction(
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
        this.message = new LogMessagesMessage(getProtocol());
        final AgentRequestBase agentRequestBase = getRequestBase();
        SdkData.checkTrue(agentRequestBase instanceof LogMessagesRequest, SdkError.ISAGENT_ERROR);
        final LogMessagesRequest request = (LogMessagesRequest) agentRequestBase;
        final JsonObject jsonMessage = message.getJsonMessage(request, fingerprint);
        // assemble the inner HTTP payload
        final byte[] envelope = Transcoder.utf8().decode(JsonIO.write(jsonMessage, false));
        // assemble the outer (secured) HTTP payload
        final byte[] envelopeSecure = getProtocol().transformRequestPayload(envelope, message.getCid());
        // assemble the HTTP request to be sent to the server
        final String resource = String.format(IDC.Resource.MESSAGES_PUT, IDC.Resource.SERVER_API_V24);
        final ByteArrayInputStream bis = new ByteArrayInputStream(envelopeSecure);
        return new HttpRequest(getProtocol().getUrl(), Http.Method.POST, resource, getHttpHeaders(), bis);
    }


    /**
     * Parse and process the server response to the client request.
     *
     * @param httpResponse the server response
     * @throws IonicException on errors in the server response
     */
    @Override
    protected final void parseHttpResponse(
            final HttpRequest httpRequest, final HttpResponse httpResponse) throws IonicException {
        // unwrap the server response
        parseHttpResponseBase(httpRequest, httpResponse, message.getCid());
        // apply logic specific to the response type
        final AgentResponseBase agentResponseBase = getResponseBase();
        SdkData.checkTrue(agentResponseBase instanceof LogMessagesResponse, SdkError.ISAGENT_ERROR);
        final LogMessagesResponse response = (LogMessagesResponse) agentResponseBase;
        final JsonObject jsonPayload = response.getJsonPayload();
        // server response to a LogMessages request is expected to be an empty json object
        SdkData.checkTrue(JsonSource.isSize(jsonPayload, 0), SdkError.ISAGENT_INVALIDVALUE,
                SdkError.getErrorString(SdkError.ISAGENT_INVALIDVALUE));
    }
}
