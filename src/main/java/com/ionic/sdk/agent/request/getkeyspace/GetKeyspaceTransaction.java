package com.ionic.sdk.agent.request.getkeyspace;

import com.ionic.sdk.agent.ServiceProtocol;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.base.AgentTransactionBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.device.DeviceUtils;
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
import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * An object encapsulating the server request and response for
 * an {@link com.ionic.sdk.agent.Agent#getKeyspace(GetKeyspaceRequest)} operation.
 */
@InternalUseOnly
public class GetKeyspaceTransaction extends AgentTransactionBase {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     *
     * @param protocol     the protocol used by the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public GetKeyspaceTransaction(final ServiceProtocol protocol,
                                  final AgentRequestBase requestBase,
                                  final AgentResponseBase responseBase) {
        super(protocol, requestBase, responseBase);
    }

    @Override
    protected final HttpRequest buildHttpRequest(final Properties fingerprint) throws IonicException {
        final AgentRequestBase agentRequestBase = getRequestBase();
        SdkData.checkTrue(agentRequestBase instanceof GetKeyspaceRequest, SdkError.ISAGENT_ERROR);
        final GetKeyspaceRequest request = (GetKeyspaceRequest) agentRequestBase;
        final URL url = AgentTransactionUtil.getProfileUrl(request.getUrl());
        final String resource = String.format(IDC.Resource.KEYSPACE_GET,
                IDC.Resource.SERVER_API_V24, request.getKeyspace());
        return new HttpRequest(url, Http.Method.GET, resource);
    }

    @Override
    protected final void parseHttpResponse(final HttpRequest httpRequest,
                                           final HttpResponse httpResponse) throws IonicException {
        // apply logic specific to the response type
        final byte[] payload = DeviceUtils.read(httpResponse.getEntity());
        final String contentType = httpResponse.getHttpHeaders().getHeaderValue(Http.Header.CONTENT_TYPE);
        SdkData.checkTrue(
                Http.Header.CONTENT_TYPE_SERVER.equals(contentType), SdkError.ISAGENT_BADRESPONSE, contentType);
        try {
            final JsonObject jsonPayload = JsonIO.readObject(payload);
            parseHttpResponsePayload(jsonPayload);
        } catch (IonicException e) {
            throw new IonicException(SdkError.ISAGENT_BADRESPONSE, e);
        }
    }

    /**
     * Parse the service response payload.  Logic is encoded here to enforce the expected payload content.
     *
     * @param jsonPayload the {@link JsonObject} representation of the response payload
     * @throws IonicException on expectation failures with regard to the payload content
     */
    private void parseHttpResponsePayload(final JsonObject jsonPayload) throws IonicException {
        final AgentRequestBase agentRequestBase = getRequestBase();
        final AgentResponseBase agentResponseBase = getResponseBase();
        SdkData.checkTrue(agentRequestBase instanceof GetKeyspaceRequest, SdkError.ISAGENT_ERROR);
        SdkData.checkTrue(agentResponseBase instanceof GetKeyspaceResponse, SdkError.ISAGENT_ERROR);
        final GetKeyspaceRequest request = (GetKeyspaceRequest) agentRequestBase;
        final GetKeyspaceResponse response = (GetKeyspaceResponse) agentResponseBase;
        response.setKeyspace(JsonSource.getJsonString(jsonPayload, IDC.Payload.KEYSPACE).getString());
        response.setFqdn(JsonSource.getJsonString(jsonPayload, IDC.Payload.FQDN).getString());
        response.setTtlSeconds(JsonSource.getJsonNumber(jsonPayload, IDC.Payload.TTLSECONDS).intValue());
        final JsonObject jsonAnswers = JsonSource.getJsonObject(jsonPayload, IDC.Payload.ANSWERS);
        final JsonArray jsonEnroll = JsonSource.getJsonArray(jsonAnswers, IDC.Payload.ENROLL);
        for (JsonValue jsonValue : jsonEnroll) {
            final JsonString jsonString = (JsonString) jsonValue;
            response.getEnrollmentURLs().add(JsonSource.toString(jsonString));
        }
        final JsonArray jsonTenantId = JsonSource.getJsonArray(jsonAnswers, IDC.Payload.TENANT_ID);
        for (JsonValue jsonValue : jsonTenantId) {
            final JsonString jsonString = (JsonString) jsonValue;
            response.getTenantIDs().add(JsonSource.toString(jsonString));
        }
        // url array is optional
        final JsonArray jsonURL = JsonSource.getJsonArrayNullable(jsonAnswers, IDC.Payload.URL);
        if (jsonURL != null) {
            for (JsonValue jsonValue : jsonURL) {
                final JsonString jsonString = (JsonString) jsonValue;
                response.getApiURLs().add(JsonSource.toString(jsonString));
            }
        }
        if (response.getApiURLs().isEmpty()) {
            logger.warning(String.format("No API URLs received from server '%s'.", request.getUrl()));
        }
    }

    @Override
    protected final boolean isIdentityNeeded() {
        return false;
    }
}
