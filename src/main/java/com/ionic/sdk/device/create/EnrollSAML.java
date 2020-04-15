package com.ionic.sdk.device.create;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceRequest;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceResponse;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.rsa.model.RsaKeyHolder;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.httpclient.Http;
import com.ionic.sdk.httpclient.HttpClient;
import com.ionic.sdk.httpclient.HttpClientDefault;
import com.ionic.sdk.httpclient.HttpHeaders;
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;
import com.ionic.sdk.httpclient.HttpUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * A utility class used to enroll a new device to an Ionic key tenant via using Security Assertion Markup Language
 * (SAML) authentication.
 * <p>
 * The enrollment procedure consists of the following HTTP transactions:
 * <ol>
 *     <li>Ionic authentication credentials are validated</li>
 *     <li>the SAML request payload is sent to the URL specified in the previous HTTP response</li>
 *     <li>the SAML response payload is sent to the SAML relay URL</li>
 *     <li>the Ionic key server asymmetric public key is fetched</li>
 *     <li>the "createDevice()" request payload is crafted, and sent to the Ionic enrollment HTTPS endpoint</li>
 * </ol>
 */
public final class EnrollSAML {

    /**
     * The URL of the enrollment server for the desired tenant.
     */
    private final URL url;

    /**
     * The agent used to perform the enrollment.
     */
    private final Agent agent;

    /**
     * The client-side RSA keypair to use in the context of the request.
     */
    private final RsaKeyHolder rsaKeyHolder;

    /**
     * Constructor.  Accept parameters to use for enrollment request.
     *
     * @param url the URL of the enrollment server
     * @throws IonicException on invalid input URL
     */
    public EnrollSAML(final String url) throws IonicException {
        this(url, new Agent(), null);
    }

    /**
     * Constructor.  Accept parameters to use for enrollment request.
     *
     * @param url   the URL of the enrollment server
     * @param agent the agent instance used to perform the enrollment
     * @throws IonicException on invalid input URL
     */
    public EnrollSAML(final String url, final Agent agent) throws IonicException {
        this(url, agent, null);
    }

    /**
     * Constructor.  Accept parameters to use for enrollment request.
     *
     * @param url          the URL of the enrollment server
     * @param agent        the agent instance used to perform the enrollment
     * @param rsaKeyHolder the client-side RSA keypair to use in the context of the request
     * @throws IonicException on invalid input URL
     */
    public EnrollSAML(final String url, final Agent agent, final RsaKeyHolder rsaKeyHolder) throws IonicException {
        this.url = AgentTransactionUtil.getProfileUrl(url);
        this.agent = agent;
        this.rsaKeyHolder = rsaKeyHolder;
    }

    /**
     * Enroll a new device in the Ionic server infrastructure.
     *
     * @param user              the account name of the identity to which the new device profile should be associated
     * @param pass              the password of the identity to which the new device profile should be associated
     * @param deviceProfileName the label for the new device profile
     * @return the {@link CreateDeviceResponse} object containing the new device profile information
     * @throws IonicException on failure of any step in the enrollment process
     */
    public CreateDeviceResponse enroll(
            final String user, final String pass, final String deviceProfileName) throws IonicException {
        try {
            final HttpClient httpClient = new HttpClientDefault(new AgentConfig(), url.getProtocol());
            final HttpResponse httpResponse1 = initiateTransaction(httpClient, url);
            final HttpResponse httpResponse2 = submitSamlRequest(httpClient, httpResponse1, user, pass);
            final HttpResponse httpResponse3 = submitSamlResponse(httpClient, httpResponse1, httpResponse2);
            final HttpResponse httpResponse4 = retrievePubkey(httpClient, httpResponse3);
            return createDevice(httpResponse3, httpResponse4, deviceProfileName);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_REQUESTFAILED, e);
        }
    }

    /**
     * Initiate the SAML transaction.
     *
     * @param httpClient the object encapsulating the http transactions needed to enroll a new device
     * @param url        the URL of the enrollment server
     * @return the http server response to this request, containing the request and URLs for subsequent requests
     * @throws IOException    on http failures during the server request
     * @throws IonicException on invalid input URL, or on receipt of an http request error code
     */
    private HttpResponse initiateTransaction(
            final HttpClient httpClient, final URL url) throws IOException, IonicException {
        final HttpRequest httpRequest = new HttpRequest(url, Http.Method.GET, url.getFile());
        final HttpResponse httpResponse = httpClient.execute(httpRequest);
        SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponse.getStatusCode()),
                SdkError.ISAGENT_REQUESTFAILED, SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
        return httpResponse;
    }

    /**
     * Submit the SAML request received in step 1 to the server URL received in step 1.
     *
     * @param httpClient    the object encapsulating the http transactions needed to enroll a new device
     * @param httpResponse1 the http server response received during step 1 of the enrollment process
     * @param user          the account name of the identity to which the new device profile should be associated
     * @param pass          the password of the identity to which the new device profile should be associated
     * @return the http server response to this request, which contains the SAML response
     * @throws IOException    on http failures during the server request
     * @throws IonicException on invalid input URL, or on receipt of an http request error code
     */
    private HttpResponse submitSamlRequest(
            final HttpClient httpClient, final HttpResponse httpResponse1,
            final String user, final String pass) throws IOException, IonicException {
        final String samlRedirect = httpResponse1.getHttpHeaders().getHeaderValue(Header.X_SAML_REDIRECT);
        final String samlRequest = httpResponse1.getHttpHeaders().getHeaderValue(Header.X_SAML_REQUEST);
        SdkData.checkTrue((samlRedirect != null), SdkError.ISAGENT_INVALIDVALUE,
                SdkError.getErrorString(SdkError.ISAGENT_INVALIDVALUE));
        SdkData.checkTrue((samlRequest != null), SdkError.ISAGENT_INVALIDVALUE,
                SdkError.getErrorString(SdkError.ISAGENT_INVALIDVALUE));
        final URL url2 = AgentTransactionUtil.getProfileUrl(samlRedirect);
        final byte[] entity = Transcoder.utf8().decode(String.format(Payload.SAML_REQUEST,
                HttpUtils.urlEncode(user), HttpUtils.urlEncode(pass), HttpUtils.urlEncode(samlRequest)));
        final HttpRequest httpRequest = new HttpRequest(
                url2, Http.Method.POST, url2.getFile(), new HttpHeaders(), new ByteArrayInputStream(entity));
        final HttpResponse httpResponse = httpClient.execute(httpRequest);
        SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponse.getStatusCode()),
                SdkError.ISAGENT_REQUESTFAILED, SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
        return httpResponse;
    }

    /**
     * Submit the SAML response received in step 2 to the server URL received in step 1.
     *
     * @param httpClient    the object encapsulating the http transactions needed to enroll a new device
     * @param httpResponse1 the http server response received during step 1 of the enrollment process
     * @param httpResponse2 the http server response received during step 2 of the enrollment process
     * @return the http server response to this request, which contains the URL of the Ionic enrollment public key
     * @throws IOException    on http failures during the server request
     * @throws IonicException on invalid input URL, or on receipt of an http request error code
     */
    private HttpResponse submitSamlResponse(
            final HttpClient httpClient, final HttpResponse httpResponse1,
            final HttpResponse httpResponse2) throws IOException, IonicException {
        final String samlRelayState1 = httpResponse1.getHttpHeaders().getHeaderValue(Header.X_SAML_RELAY_STATE);
        final URL url3 = AgentTransactionUtil.getProfileUrl(samlRelayState1);
        final String samlResponse2 = httpResponse2.getHttpHeaders().getHeaderValue(Header.X_SAML_RESPONSE);
        SdkData.checkTrue((samlResponse2 != null), SdkError.ISAGENT_REQUESTFAILED,
                SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
        final byte[] entity = Transcoder.utf8().decode(String.format(Payload.SAML_RESPONSE,
                HttpUtils.urlEncode(samlResponse2)));
        final HttpRequest httpRequest = new HttpRequest(
                url3, Http.Method.POST, url3.getFile(), new HttpHeaders(), new ByteArrayInputStream(entity));
        final HttpResponse httpResponse = httpClient.execute(httpRequest);
        SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponse.getStatusCode()),
                SdkError.ISAGENT_REQUESTFAILED, SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
        return httpResponse;
    }

    /**
     * Acquire the asymmetric Ionic public key, used to build the {@link CreateDeviceRequest}.
     *
     * @param httpClient    the object encapsulating the http transactions needed to enroll a new device
     * @param httpResponse3 the http server response received during step 3 of the enrollment process
     * @return the http server response to this request, which contains the Ionic enrollment public key in its payload
     * @throws IOException    on http failures during the server request
     * @throws IonicException on invalid input URL, or on receipt of an http request error code
     */
    private HttpResponse retrievePubkey(
            final HttpClient httpClient, final HttpResponse httpResponse3) throws IOException, IonicException {
        final String urlPubkey = httpResponse3.getHttpHeaders().getHeaderValue(Header.X_IONIC_REG_PUBKEY_URL);
        final URL url4 = AgentTransactionUtil.getProfileUrl(urlPubkey);
        final HttpRequest httpRequest = new HttpRequest(url4, Http.Method.GET, url4.getFile());
        final HttpResponse httpResponse = httpClient.execute(httpRequest);
        SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponse.getStatusCode()),
                SdkError.ISAGENT_REQUESTFAILED, SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
        return httpResponse;
    }

    /**
     * Assemble the enrollment request, submit it to the Ionic infrastructure, and process the response.
     *
     * @param httpResponse3     the http server response received during step 3 of the enrollment process
     * @param httpResponse4     the http server response received during step 4 of the enrollment process
     * @param deviceProfileName the label for the new device profile
     * @return the {@link CreateDeviceResponse} to this request, which on success contains the new device profile
     * @throws IOException    on http failures during the server request
     * @throws IonicException on invalid input URL, or on receipt of an http request error code
     */
    private CreateDeviceResponse createDevice(
            final HttpResponse httpResponse3, final HttpResponse httpResponse4,
            final String deviceProfileName) throws IOException, IonicException {
        // transact with server
        final String server = httpResponse3.getHttpHeaders().getHeaderValue(Header.X_IONIC_REG_IONIC_URL);
        final String keyspace = httpResponse3.getHttpHeaders().getHeaderValue(Header.X_IONIC_REG_ENROLLMENT_TAG);
        final String token = httpResponse3.getHttpHeaders().getHeaderValue(Header.X_IONIC_REG_STOKEN);
        final String uid = httpResponse3.getHttpHeaders().getHeaderValue(Header.X_IONIC_REG_UIDAUTH);
        final byte[] pubkeyBytes = Stream.read(httpResponse4.getEntity());
        final String pubkeyText = Transcoder.utf8().encode(pubkeyBytes).trim();
        agent.initializeWithoutProfiles();
        final CreateDeviceRequest request = new CreateDeviceRequest(
                deviceProfileName, server, keyspace, token, uid, pubkeyText);
        request.setRsaKeyHolder(rsaKeyHolder);
        return agent.createDevice(request);
    }

    /**
     * Text names associated with http headers used in device enrollment.
     */
    private static class Header {

        /**
         * Http header name used in device enrollment.
         */
        private static final String X_IONIC_REG_ENROLLMENT_TAG = "X-Ionic-Reg-Enrollment-Tag";

        /**
         * Http header name used in device enrollment.
         */
        private static final String X_IONIC_REG_IONIC_URL = "X-Ionic-Reg-Ionic-Url";

        /**
         * Http header name used in device enrollment.
         */
        private static final String X_IONIC_REG_PUBKEY_URL = "X-Ionic-Reg-Pubkey-Url";

        /**
         * Http header name used in device enrollment.
         */
        private static final String X_IONIC_REG_STOKEN = "X-Ionic-Reg-Stoken";

        /**
         * Http header name used in device enrollment.
         */
        private static final String X_IONIC_REG_UIDAUTH = "X-Ionic-Reg-Uidauth";

        /**
         * Http header name used in device enrollment.
         */
        private static final String X_SAML_REDIRECT = "X-Saml-Redirect";

        /**
         * Http header name used in device enrollment.
         */
        private static final String X_SAML_RELAY_STATE = "X-Saml-Relay-State";

        /**
         * Http header name used in device enrollment.
         */
        private static final String X_SAML_REQUEST = "X-Saml-Request";

        /**
         * Http header name used in device enrollment.
         */
        private static final String X_SAML_RESPONSE = "X-Saml-Response";
    }

    /**
     * Text strings associated with http payloads used in device enrollment.
     */
    private static class Payload {

        /**
         * Text string associated with http payload used in device enrollment.
         */
        private static final String SAML_REQUEST = "user=%s&password=%s&SAMLRequest=%s";

        /**
         * Text string associated with http payload used in device enrollment.
         */
        private static final String SAML_RESPONSE = "SAMLResponse=%s";
    }
}
