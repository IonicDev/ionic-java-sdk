package com.ionic.sdk.agent.request.base;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.AgentErrorModuleConstants;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.IonicServerException;
import com.ionic.sdk.httpclient.Http;
import com.ionic.sdk.httpclient.HttpClient;
import com.ionic.sdk.httpclient.HttpClientFactory;
import com.ionic.sdk.httpclient.HttpHeader;
import com.ionic.sdk.httpclient.HttpHeaders;
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;
import com.ionic.sdk.json.JsonU;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * The base class for agent transactions, which encapsulate an http request made to the Ionic server infrastructure,
 * and the associated server response (if any).
 */
public abstract class AgentTransactionBase {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The {@link com.ionic.sdk.key.KeyServices} implementation.
     */
    private final Agent agent;

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
     * @param agent        the KeyServices implementation
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public AgentTransactionBase(
            final Agent agent, final AgentRequestBase requestBase, final AgentResponseBase responseBase) {
        this.agent = agent;
        this.requestBase = requestBase;
        this.responseBase = responseBase;
    }

    /**
     * @return the KeyServices implementation
     */
    protected final Agent getAgent() {
        return agent;
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
     * from server errors encountered in the context of the request.
     *
     * @throws IonicException on errors assembling the request or processing the response
     */
    public final void run() throws IonicException {
        if (!agent.isInitialized()) {
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_NOINIT.value());
        }
        // set up the fingerprint field (hashed + hexed)
        final Properties fingerprint = new Properties();
        fingerprint.setProperty(IDC.Payload.HFPHASH, agent.getFingerprint().getHfpHash());
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
        final AgentConfig config = agent.getConfig();
        final HttpClient httpClientIDC = HttpClientFactory.create(config.getHttpImpl(),
                config.getHttpTimeoutSecs(), config.getMaxRedirects(), httpRequest.getUrl().getProtocol());
        try {
            final HttpResponse httpResponse = httpClientIDC.execute(httpRequest);
            parseHttpResponse(httpResponse);
        } catch (IOException e) {
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_REQUESTFAILED.value(), e);
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
        } else if (returnCode == AgentErrorModuleConstants.ISAGENT_FPHASH_DENIED.value()) {
            // specific client handling of server fingerprint hash rejection is to retry with full fingerprint
            errorsHandled.add(returnCode);
            handleFingerprintDeniedError(fingerprint);
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
        fingerprint.setProperty(IDC.Payload.HFP, agent.getFingerprint().getHfp());
        fingerprint.setProperty(IDC.Payload.HFPHASH, agent.getFingerprint().getHfpHash());
    }

    @SuppressWarnings({"checkstyle:javadocmethod"})
    /**
     * Common handling of server responses to client requests.  This includes logging error codes, deserialization of
     * server response json, and unwrapping of the embedded, secured response.
     *
     * @param httpResponse the server response
     * @param cidQ         the cid of the client request (for comparison to the one found in the server response)
     * @throws IonicException on server error code, inability to deserialize response, or unexpected response content
     * @throws javax.json.JsonException  on problems parsing the response payload bytes
     */
    protected final void parseHttpResponseBase(
            final HttpResponse httpResponse, final String cidQ) throws IonicException {
        responseBase.setHttpResponseCode(httpResponse.getStatusCode());
        // log an error if we got an unexpected HTTP response code
        if (AgentTransactionUtil.isHttpErrorCode(httpResponse.getStatusCode())) {
            logger.severe(String.format("Received unexpected response code from server.  "
                    + "Expected 200-299, got %d, CID=%s.", httpResponse.getStatusCode(), cidQ));
        }
        if (cidQ != null) {
            // deserialize server response entity
            final JsonReader readerSecure = Json.createReader(httpResponse.getEntity());
            final JsonObject jsonSecure = readerSecure.readObject();
            logger.fine(JsonU.toJson(jsonSecure, true));
            final String cid = JsonU.getString(jsonSecure, IDC.Payload.CID);
            final String envelope = JsonU.getString(jsonSecure, IDC.Payload.ENVELOPE);
            try {
                AgentTransactionUtil.checkNotNull(cid, IDC.Payload.CID, cid);
                AgentTransactionUtil.checkNotNull(envelope, IDC.Payload.ENVELOPE, envelope);
            } catch (IonicException e) {
                int error = AgentErrorModuleConstants.ISAGENT_BADRESPONSE.value();
                throw new IonicException(error, new IOException(JsonU.toJson(jsonSecure, false)));
            }
            AgentTransactionUtil.checkEqual(cidQ, cidQ, cid);
            parseHttpResponseBase2(cid, envelope);
        }
    }

    /**
     * Unwrap the secured response from the response envelope.
     *
     * @param cid      the cid in the server response
     * @param envelope the ciphertext containing the protected server response
     * @throws IonicException on cryptography or server response errors
     * @throws javax.json.JsonException  on problems parsing the response payload bytes
     */
    private void parseHttpResponseBase2(final String cid, final String envelope) throws IonicException {
        // unwrap content of secure envelope
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(agent.getActiveProfile().getAesCdIdcProfileKey());
        cipher.setAuthData(Transcoder.utf8().decode(cid));
        final byte[] entityClear = cipher.decryptBase64(envelope);
        //logger.finest(new UTF8().encode(entityClear));  // plaintext json; IDC http entity (for debugging)
        // decompose cleartext content of server response
        final JsonReader readerClear = Json.createReader(new ByteArrayInputStream(entityClear));
        final JsonObject jsonPayload = readerClear.readObject();
        final JsonObject error = jsonPayload.getJsonObject(IDC.Payload.ERROR);
        responseBase.setConversationId(cid);
        responseBase.setJsonPayload(jsonPayload);
        responseBase.setServerErrorCode((error == null) ? 0 : JsonU.getInt(error, IDC.Payload.CODE));
        responseBase.setServerErrorMessage((error == null) ? null : JsonU.getString(error, IDC.Payload.MESSAGE));
        responseBase.setServerErrorDataJson((error == null) ? null : JsonU.toJson(error, false));
        if (responseBase.getServerErrorDataJson() != null) {
            logger.severe(responseBase.getServerErrorDataJson());
        }
        // Because IonicServerException needs to derive from ServerException; we must wrap here.
        // on removal of ServerException, this stuff can be simplified
        try {
            processResponseErrorServer(cid);
        } catch (IonicServerException e) {
            throw new IonicException(e);
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
            case SERVER_ERROR_HFPHASH_DENIED:
                throwException(AgentErrorModuleConstants.ISAGENT_FPHASH_DENIED.value(), responseBase, cid);
                break;
            case SERVER_ERROR_CID_TIMESTAMP_DENIED:
                throwException(AgentErrorModuleConstants.ISAGENT_CID_TIMESTAMP_DENIED.value(), responseBase, cid);
                break;
            case SERVER_OK:
                processResponseErrorHttp(cid);
                break;
            default:
                throwException(AgentErrorModuleConstants.ISAGENT_REQUESTFAILED.value(), responseBase, cid);
        }
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
            throwException(AgentErrorModuleConstants.ISAGENT_REQUESTFAILED.value(), responseBase, cid);
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
     * (2) an http error indicating the type of error (typically these do not trigger Ionic-specific logic
     * <p>
     * (3) a json response entity containing data in an unexpected format
     *
     * @param cid the request conversation ID
     * @throws IonicServerException on errors
     */
    private void processResponseErrorData(final String cid) throws IonicServerException {
        final JsonObject jsonPayload = responseBase.getJsonPayload();
        if (jsonPayload.getJsonObject(IDC.Payload.DATA) == null) {
            throwException(AgentErrorModuleConstants.ISAGENT_BADRESPONSE.value(), responseBase, cid);
        }
    }

    /**
     * Create and throw a new ServerException object containing the relevant data from the server response.
     *
     * @param errorCode    the client error code to pass to the caller
     * @param responseBase the class containing the server response information
     * @param cid          the request conversation ID
     * @throws IonicServerException always
     */
    private static void throwException(
            final int errorCode, final AgentResponseBase responseBase, final String cid) throws IonicServerException {
        throw new IonicServerException(errorCode, responseBase.getHttpResponseCode(),
                responseBase.getServerErrorCode(), responseBase.getServerErrorMessage(),
                responseBase.getServerErrorDataJson(), cid);
    }

    /**
     * Create headers to send in ionic.com API calls.
     *
     * @return an object containing the common http headers to be sent in ionic.com calls
     */
    protected final HttpHeaders getHttpHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(new HttpHeader(Http.Header.CONTENT_TYPE, Http.Header.CONTENT_TYPE_VALUE));
        httpHeaders.add(new HttpHeader(Http.Header.USER_AGENT, agent.getConfig().getUserAgent()));
        httpHeaders.add(new HttpHeader(Http.Header.ACCEPT_ENCODING, Http.Header.ACCEPT_ENCODING_VALUE));
        return httpHeaders;
    }

    /**
     * Assemble a client request for submission to the IDC infrastructure.
     *
     * @param fingerprint authentication data associated with the client state to be included in the request
     * @return a request object, ready for submission to the server
     * @throws IonicException on failure to assemble the request
     */
    protected abstract HttpRequest buildHttpRequest(final Properties fingerprint) throws IonicException;

    /**
     * Parse and process the server response to the client request.
     *
     * @param httpResponse the server response
     * @throws IonicException on errors in the server response
     */
    protected abstract void parseHttpResponse(final HttpResponse httpResponse) throws IonicException;

    /**
     * Automatic error recovery options.
     */
    private static final int MAX_RECOVERY_ATTEMPTS = 3;

    /**
     * Server has accepted request.
     */
    private static final int SERVER_OK = 0;

    /**
     * Server has rejected request due to mismatched fingerprint hash.
     */
    private static final int SERVER_ERROR_HFPHASH_DENIED = 4001;

    /**
     * Server has rejected request due to out of range timestamp (embedded in request conversation ID).
     */
    private static final int SERVER_ERROR_CID_TIMESTAMP_DENIED = 4002;

    /**
     * Server has rejected update key request atom due to attribute signature failure.
     */
    public static final int POLICY_SERVER_ERROR_STALE_ATTRIBUTES = 4202;
}
