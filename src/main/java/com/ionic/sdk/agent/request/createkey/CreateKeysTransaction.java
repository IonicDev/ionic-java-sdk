package com.ionic.sdk.agent.request.createkey;

import com.ionic.sdk.agent.ServiceProtocol;
import com.ionic.sdk.agent.key.KeyObligationsMap;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.base.AgentTransactionBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
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
 * An object encapsulating the server request and response for an Agent.createKeys() request.
 */
@InternalUseOnly
public class CreateKeysTransaction extends AgentTransactionBase {

    /**
     * Helper object for serialization of request to json for submission to KeyServices implementation.
     */
    private CreateKeysMessage message;

    /**
     * Constructor.
     *
     * @param protocol     the protocol used by the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public CreateKeysTransaction(
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
        this.message = new CreateKeysMessage(getProtocol());
        final AgentRequestBase agentRequestBase = getRequestBase();
        SdkData.checkTrue(agentRequestBase instanceof CreateKeysRequest, SdkError.ISAGENT_ERROR);
        final CreateKeysRequest request = (CreateKeysRequest) agentRequestBase;
        final JsonObject jsonMessage = message.getJsonMessage(request, fingerprint);
        final String resource = getProtocol().getResource(IDC.Resource.SERVER_API_V24, IDC.Resource.KEYS_CREATE_BASE);
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
        final AgentRequestBase agentRequestBase = getRequestBase();
        final AgentResponseBase agentResponseBase = getResponseBase();
        SdkData.checkTrue(agentRequestBase instanceof CreateKeysRequest, SdkError.ISAGENT_ERROR);
        SdkData.checkTrue(agentResponseBase instanceof CreateKeysResponse, SdkError.ISAGENT_ERROR);
        final CreateKeysRequest request = (CreateKeysRequest) agentRequestBase;
        final CreateKeysResponse response = (CreateKeysResponse) agentResponseBase;
        final String cid = response.getConversationId();
        final JsonObject jsonPayload = response.getJsonPayload();
        final JsonObject jsonData = JsonSource.getJsonObject(jsonPayload, IDC.Payload.DATA);
        final JsonArray jsonProtectionKeys = JsonSource.getJsonArray(jsonData, IDC.Payload.PROTECTION_KEYS);
        for (JsonValue value : jsonProtectionKeys) {
            // deserialize each response key into a user-consumable object
            final JsonObject jsonProtectionKey = JsonSource.toJsonObject(value, IDC.Payload.PROTECTION_KEYS);
            final String ref = JsonSource.getString(jsonProtectionKey, IDC.Payload.REF);
            AgentTransactionUtil.checkNotNull(cid, IDC.Payload.REF, ref);
            final CreateKeysRequest.Key keyRequest = request.getKey(ref);
            final String csigQ = message.getCsigs().getProperty(ref);
            final String msigQ = message.getMsigs().getProperty(ref);
            final String id = JsonSource.getString(jsonProtectionKey, IDC.Payload.ID);
            final String authData = Value.join(IDC.Signature.DELIMITER, cid, ref, id, csigQ, msigQ);
            final String keyHex = JsonSource.getString(jsonProtectionKey, IDC.Payload.KEY);
            final String csig = JsonSource.getString(jsonProtectionKey, IDC.Payload.CSIG);
            final String msig = JsonSource.getString(jsonProtectionKey, IDC.Payload.MSIG);
            final KeyObligationsMap keyObligationsMap = AgentTransactionUtil.toObligations(
                    JsonSource.getJsonObjectNullable(jsonProtectionKey, IDC.Payload.OBLIGATIONS));
            // verify each received response key
            final byte[] keyBytes = getProtocol().getKeyBytes(keyHex, authData);
            response.add(new CreateKeysResponse.Key(ref, id, keyBytes, getProtocol().getIdentity(),
                    keyRequest.getAttributesMap(), keyRequest.getMutableAttributesMap(),
                    keyObligationsMap, IDC.Metadata.KEYORIGIN_IONIC, csig, msig));
        }
    }
}
