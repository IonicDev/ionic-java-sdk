package com.ionic.sdk.agent.request.getkey;

import com.ionic.sdk.agent.ServiceProtocol;
import com.ionic.sdk.agent.key.KeyAttributesMap;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * An object encapsulating the server request and response for an Agent.getKeys() request.
 */
@InternalUseOnly
public class GetKeysTransaction extends AgentTransactionBase {

    /**
     * Helper object for serialization of request to json for submission to KeyServices implementation.
     */
    private GetKeysMessage message;

    /**
     * Constructor.
     *
     * @param protocol     the protocol used by the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public GetKeysTransaction(
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
        // check for one or the other, protection keys or external ids, must have one
        final AgentRequestBase agentRequestBase = getRequestBase();
        SdkData.checkTrue(agentRequestBase instanceof GetKeysRequest, SdkError.ISAGENT_ERROR);
        final GetKeysRequest request = (GetKeysRequest) agentRequestBase;
        final List<String> externalIds = request.getExternalIds();
        final List<String> keyIds = request.getKeyIds();
        if ((externalIds.isEmpty()) && (keyIds.isEmpty())) {
            throw new IonicException(SdkError.ISAGENT_BADREQUEST, "No key ids or external ids in get key request.");
        }
        this.message = new GetKeysMessage(getProtocol());
        final JsonObject jsonMessage = message.getJsonMessage(request, fingerprint);
        final String resource = getProtocol().getResource(IDC.Resource.SERVER_API_V24, IDC.Resource.KEYS_GET_BASE);
        // assemble the wrapped HTTP payload
        final byte[] envelope = Transcoder.utf8().decode(JsonIO.write(jsonMessage, false));
        // assemble the secured (outer) HTTP payload
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
        final AgentResponseBase agentResponseBase = getResponseBase();
        SdkData.checkTrue(agentResponseBase instanceof GetKeysResponse, SdkError.ISAGENT_ERROR);
        final GetKeysResponse response = (GetKeysResponse) agentResponseBase;
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
            final KeyObligationsMap keyObligationsMap = AgentTransactionUtil.toObligations(
                    JsonSource.getJsonObjectNullable(jsonProtectionKey, IDC.Payload.OBLIGATIONS));
            final String authData = Value.join(IDC.Signature.DELIMITER, cid, id, csig, msig);
            // verify each received response key
            final byte[] clearBytesKey = getProtocol().getKeyBytes(keyHex, authData);
            // verify each received response attributes
            getProtocol().verifySignature(IDC.Payload.CSIG, csig, cattrs, clearBytesKey);
            getProtocol().verifySignature(IDC.Payload.MSIG, msig, mattrs, clearBytesKey);
            final KeyAttributesMap cattrsKey = message.getJsonAttrs(cattrs, id, clearBytesKey);
            final KeyAttributesMap mattrsKey = message.getJsonAttrs(mattrs, id, clearBytesKey);
            response.add(new GetKeysResponse.Key(id, clearBytesKey, getProtocol().getIdentity(), cattrsKey, mattrsKey,
                    keyObligationsMap, IDC.Metadata.KEYORIGIN_IONIC, csig, msig));
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
            final AgentRequestBase agentRequestBase = getRequestBase();
            SdkData.checkTrue(agentRequestBase instanceof GetKeysRequest, SdkError.ISAGENT_ERROR);
            final GetKeysRequest request = (GetKeysRequest) agentRequestBase;
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
