package com.ionic.sdk.agent.request.base;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.ServiceProtocol;
import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.IonicServerException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.error.ServerError;
import com.ionic.sdk.httpclient.Http;
import com.ionic.sdk.httpclient.HttpClient;
import com.ionic.sdk.httpclient.HttpClientFactory;
import com.ionic.sdk.httpclient.HttpHeader;
import com.ionic.sdk.httpclient.HttpHeaders;
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * The base class for agent transactions, which encapsulate an http request made to the Ionic server infrastructure,
 * and the associated server response (if any).
 */
@InternalUseOnly
public abstract class AgentTransactionBase {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The protocol of the {@link com.ionic.sdk.key.KeyServices} client (authentication, state).
     */
    private final ServiceProtocol protocol;

    /**
     * The client request to send to the server.
     */
    private final AgentRequestBase requestBase;

    /**
     * The server response to the request.
     */
    private final AgentResponseBase responseBase;

    /**
     * Constructor.
     *
     * @param protocol      the protocol of the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public AgentTransactionBase(
            final ServiceProtocol protocol, final AgentRequestBase requestBase, final AgentResponseBase responseBase) {
        this.protocol = protocol;
        this.requestBase = requestBase;
        this.responseBase = responseBase;
    }

    /**
     * @return the protocol of the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     */
    public ServiceProtocol getProtocol() {
        return protocol;
    }

    /**
     * @return the client request to send to the server
     */
    protected final AgentRequestBase getRequestBase() {
        return requestBase;
    }

    /**
     * @return the server response to the request
     */
    protected final AgentResponseBase getResponseBase() {
        return responseBase;
    }

    /**
     * Request data from the server.  This encapsulates inclusion of fingerprint data in the request, and auto-recovery
     * from server errors encountered in the protocol of the request.
     *
     * @throws IonicException on errors assembling the request or processing the response
     */
    public final void run() throws IonicException {
        // agent must be initialized before transactions are allowed
        SdkData.checkTrue(protocol.isInitialized(), SdkError.ISAGENT_NOINIT);
        // agent needs an active profile to perform most IDC transactions
        if (isIdentityNeeded()) {
            SdkData.checkTrue(protocol.hasIdentity(), SdkError.ISAGENT_NO_DEVICE_PROFILE);
            try {
                protocol.isValidIdentity();
            } catch (IonicException e) {
                throw new IonicException(SdkError.ISAGENT_NO_DEVICE_PROFILE, e);
            }
        }
        // set up the fingerprint field (hashed + hexed)
        final Properties fingerprint = new Properties();
        fingerprint.setProperty(IDC.Payload.HFPHASH, protocol.getFingerprint().getHfpHash());
        // keep track of which auto-recoverable errors we have handled so that we don't
        // try to handle the same one multiple times
        final Set<Integer> autoRecoverErrorsHandled = new TreeSet<Integer>();
        // issue the request and auto-recover on error when possible
        for (int attempt = 1; (attempt <= MAX_RECOVERY_ATTEMPTS); ++attempt) {
            try {
                runWithFingerprint(fingerprint);
                break;
            } catch (IonicException e) {
                if (!handleException(attempt, e, autoRecoverErrorsHandled, fingerprint)) {
                    throw e;
                }
            }
        }
    }

    /**
     * Submit request to IDC, and process response.  Subclasses provide response handling specific to the subclass
     * type.
     *
     * @param fingerprint authentication data associated with the client state to be included in the request
     * @throws IonicException on errors assembling the request or processing the response
     */
    private void runWithFingerprint(final Properties fingerprint) throws IonicException {
        final HttpRequest httpRequest = buildHttpRequest(fingerprint);
        final AgentConfig config = protocol.getConfig();
        final HttpClient httpClientIDC = HttpClientFactory.create(config, httpRequest.getUrl().getProtocol());
        try {
            final HttpResponse httpResponse = httpClientIDC.execute(httpRequest);
            parseHttpResponse(httpRequest, httpResponse);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_REQUESTFAILED, e);
        }
    }

    /**
     * Certain exceptions thrown by the server are expected in the normal flow of SDK usage, and may be handled by
     * adjusting the request state, and retrying the server request.
     *
     * @param attempt       ordinal indicating the current request attempt
     * @param exception     the exception to evaluate to determine whether a retry should be attempted
     * @param errorsHandled the error codes which have already been encountered in the context of this transaction
     * @param fingerprint   authentication data associated with the client state to be included in the request
     * @return true iff the exception was handled
     */
    private boolean handleException(final int attempt, final IonicException exception,
                                    final Set<Integer> errorsHandled, final Properties fingerprint) {
        boolean handled = false;
        final Integer returnCode = exception.getReturnCode();
        // only attempt recovery from a given error once
        if (errorsHandled.contains(returnCode)) {
            logger.warning(String.format("Recoverable error encountered during %s.  Recovery attempt %d. "
                    + "Error code: %d", getClass().getSimpleName(), attempt, returnCode));
        } else if (returnCode == SdkError.ISAGENT_FPHASH_DENIED) {
            // specific client handling of server fingerprint hash rejection is to retry with full fingerprint
            errorsHandled.add(returnCode);
            handleFingerprintDeniedError(fingerprint);
            handled = true;
        } else if (returnCode == SdkError.ISAGENT_CID_TIMESTAMP_DENIED
                && handleCidTimestampDeniedError()) {
            // server is just indicating that our time stamp is too far off, set an offset and retry once
            errorsHandled.add(returnCode);
            handled = true;
        }
        return handled;
    }

    /**
     * Handling of server fingerprint hash rejection is to retry with full fingerprint.
     *
     * @param fingerprint authentication data associated with the client state to be included in the request
     */
    private void handleFingerprintDeniedError(final Properties fingerprint) {
        fingerprint.setProperty(IDC.Payload.HFP, protocol.getFingerprint().getHfp());
        fingerprint.setProperty(IDC.Payload.HFPHASH, protocol.getFingerprint().getHfpHash());
    }

    /**
     * Common handling of server responses to client requests.  This includes logging error codes, deserialization of
     * server response json, and unwrapping of the embedded, secured response.
     *
     * @param httpRequest   the server request
     * @param httpResponse  the server response
     * @param cidQ          the cid of the client request (for comparison to the one found in the server response)
     * @throws IonicException on server error code, inability to deserialize response, unexpected response content,
     *                        or problems parsing the response payload bytes
     */
    protected final void parseHttpResponseBase(
            final HttpRequest httpRequest, final HttpResponse httpResponse,
            final String cidQ) throws IonicException {
        responseBase.setHttpResponseCode(httpResponse.getStatusCode());
        // log an error if we got an unexpected HTTP response code
        if (AgentTransactionUtil.isHttpErrorCode(httpResponse.getStatusCode())) {
            logger.severe(String.format("Received unexpected response code from server.  "
                    + "Expected 200-299, got %d, CID=%s.", httpResponse.getStatusCode(), cidQ));
        }
        // according to "https://dev.ionic.com/api/device/device-request-payload-format", server responses to
        // device requests are expected to be secure JSON, and the unwrapped response is also expected to be JSON
        final String contentType = httpResponse.getHttpHeaders().getHeaderValue(Http.Header.CONTENT_TYPE);
        SdkData.checkNotNull(contentType, Http.Header.CONTENT_TYPE);
        SdkData.checkTrue(contentType.contains(Http.Header.CONTENT_TYPE_SERVER), SdkError.ISAGENT_BADRESPONSE);
        // deserialize, validate server response entity
        final byte[] entitySecure = DeviceUtils.read(httpResponse.getEntity());
        // decompose cleartext content of server response
        // according to "https://dev.ionic.com/api/device/device-request-payload-format", server responses to
        // device requests are expected to be secure JSON, and the unwrapped response is also expected to be JSON
        final byte[] entityClear = protocol.transformResponsePayload(entitySecure, cidQ);
        final JsonObject jsonPayload = JsonIO.readObject(entityClear);
        final JsonObject error = JsonSource.getJsonObjectNullable(jsonPayload, IDC.Payload.ERROR);
        responseBase.setConversationId((cidQ == null)
                ? JsonSource.getString(jsonPayload, IDC.Payload.CID) : cidQ);
        responseBase.setJsonPayload(jsonPayload);
        responseBase.setServerErrorCode((error == null) ? 0 : JsonSource.getInt(error, IDC.Payload.CODE));
        responseBase.setServerErrorMessage((error == null)
                ? null : JsonSource.getString(error, IDC.Payload.MESSAGE));
        responseBase.setServerErrorDataJson((error == null) ? null : JsonIO.write(error, false));
        if (responseBase.getServerErrorDataJson() != null) {
            logger.severe(responseBase.getServerErrorDataJson());
        }
        // Because IonicServerException needs to derive from ServerException; we must wrap here.
        // on removal of ServerException, this stuff can be simplified
        try {
            processResponseErrorServer(cidQ);
        } catch (IonicServerException e) {
            throw new IonicException(e.getReturnCode(), e);
        }
    }

    /**
     * Respond to the server error code (if any) in the server response json.
     * <p>
     * When the server encounters an error during the servicing of a client request, it may respond with:
     * <p>
     * (1) a server error json structure containing information about the error
     * <p>
     * (2) an http error indicating the type of error (typically these do not trigger Ionic-specific logic
     * <p>
     * (3) a json response entity containing data in an unexpected format
     *
     * @param cid the request conversation ID
     * @throws IonicServerException on errors
     */
    private void processResponseErrorServer(final String cid) throws IonicServerException {
        switch (responseBase.getServerErrorCode()) {
            case ServerError.HFPHASH_DENIED:
                throw new IonicServerException(SdkError.ISAGENT_FPHASH_DENIED, cid, responseBase);
                //break;
            case ServerError.CID_TIMESTAMP_DENIED:
                throw new IonicServerException(SdkError.ISAGENT_CID_TIMESTAMP_DENIED, cid, responseBase);
                //break;
            case ServerError.SERVER_OK:
                processResponseErrorHttp(cid);
                break;
            default:
                throw new IonicServerException(SdkError.ISAGENT_REQUESTFAILED, cid, responseBase);
        }
    }

    /**
     * Internal function to parse the server error in case of a CID_TIMESTAMP_DENIED error.
     *
     * @return True if successfully parses the server time and calibrates the Agent, False otherwise.
     */
    private boolean handleCidTimestampDeniedError() {
        logger.finest("Server has denied our CID timestamp.  Auto-calibrating server time offset.");

        // make sure the server provided the error "data" field
        if (getResponseBase().getServerErrorDataJson().length() == 0) {
            logger.severe("Server did not provide any data along with the CID timestamp error.");
            return false;
        }

        // parse the error data JSON
        JsonValue rootValue = null;
        try {
            rootValue = JsonIO.readObject(getResponseBase().getServerErrorDataJson(), SdkError.ISAGENT_PARSEFAILED);
        } catch (IonicException e) {
            logger.severe("Failed to parse error data provided with the CID timestamp error.");
            return false;
        }

        if (rootValue == null || rootValue.getValueType() != JsonValue.ValueType.NUMBER) {
            logger.severe("The error data provided with the CID timestamp error does not contain an integer.");
            return false;
        }

        // use the server time to calibrate the agent
        Agent.calibrateServerTimeOffsetMillis(JsonSource.toLong(rootValue));

        return true;
    }

    /**
     * Respond to the http error code in the server response.
     * <p>
     * When the server encounters an error during the servicing of a client request, it may respond with:
     * <p>
     * (1) a server error json structure containing information about the error
     * <p>
     * (2) an http error indicating the type of error (typically these do not trigger Ionic-specific logic
     * <p>
     * (3) a json response entity containing data in an unexpected format
     *
     * @param cid the request conversation ID
     * @throws IonicServerException on errors
     */
    private void processResponseErrorHttp(final String cid) throws IonicServerException {
        if (AgentTransactionUtil.isHttpErrorCode(responseBase.getHttpResponseCode())) {
            throw new IonicServerException(SdkError.ISAGENT_REQUESTFAILED, cid, responseBase);
        } else {
            processResponseErrorData(cid);
        }
    }

    /**
     * Respond to the detection of an improperly formed server response entity.
     * <p>
     * When the server encounters an error during the servicing of a client request, it may respond with:
     * <p>
     * (1) a server error json structure containing information about the error
     * <p>
     * (2) an http error indicating the type of error (typically these do not trigger Ionic-specific logic)
     * <p>
     * (3) a json response entity containing data in an unexpected format
     *
     * @param cid the request conversation ID
     * @throws IonicServerException on errors
     */
    private void processResponseErrorData(final String cid) throws IonicServerException {
        final JsonObject jsonPayload = responseBase.getJsonPayload();
        final JsonValue.ValueType valueType = JsonSource.getValueType(jsonPayload, IDC.Payload.DATA);
        if (!JsonValue.ValueType.OBJECT.equals(valueType) && responseBase.isDataRequired()) {
            throw new IonicServerException(SdkError.ISAGENT_BADRESPONSE, cid, responseBase);
        }
    }

    /**
     * Create headers to send in ionic.com API calls.
     *
     * @return an object containing the common http headers to be sent in ionic.com calls
     */
    protected final HttpHeaders getHttpHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(new HttpHeader(Http.Header.CONTENT_TYPE, Http.Header.CONTENT_TYPE_CLIENT));
        httpHeaders.add(new HttpHeader(Http.Header.USER_AGENT, protocol.getConfig().getUserAgent()));
        httpHeaders.add(new HttpHeader(Http.Header.ACCEPT_ENCODING, Http.Header.ACCEPT_ENCODING_VALUE));
        protocol.addHeader(httpHeaders);
        return httpHeaders;
    }

    /**
     * Assemble a client request for submission to the IDC infrastructure.
     *
     * @param fingerprint authentication data associated with the client state to be included in the request
     * @return a request object, ready for submission to the server
     * @throws IonicException on failure to assemble the request
     */
    protected abstract HttpRequest buildHttpRequest(Properties fingerprint) throws IonicException;

    /**
     * Parse and process the server response to the client request.
     *
     * @param httpRequest  the server request
     * @param httpResponse the server response
     * @throws IonicException on errors in the server response
     */
    protected abstract void parseHttpResponse(
            HttpRequest httpRequest, HttpResponse httpResponse) throws IonicException;

    /**
     * Indicate whether derived transaction needs to indicate an client identity to the server.  Key
     * transactions must specify a client identity.  Derived transactions where no identity is needed may override
     * this method.
     *
     * @return true iff the server transaction requires client authentication
     */
    protected boolean isIdentityNeeded() {
        return true;
    }

    /**
     * Automatic error recovery options.
     */
    private static final int MAX_RECOVERY_ATTEMPTS = 3;
}
