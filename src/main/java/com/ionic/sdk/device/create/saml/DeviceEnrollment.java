package com.ionic.sdk.device.create.saml;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceRequest;
import com.ionic.sdk.agent.request.createdevice.CreateDeviceResponse;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.device.identity.IdentitySources;
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
import java.security.PublicKey;
import java.util.Properties;

/**
 * Widget providing an interface to enroll a device to Machina, given a pre-generated Security Assertion.
 * <p>
 * An instance of this class may be initialized with either:
 * <ul>
 * <li>a keyspace id (a four character string identifier for the keyspace)</li>
 * <li>an enrollment URL for the keyspace</li>
 * </ul>
 * <p>
 * If initialized with a keyspace id, the Keyspace Name Service (KNS) is used during object construction
 * to lookup the associated enrollment URL.
 */
public final class DeviceEnrollment {

    /**
     * The enrollment URL for the targeted keyspace, which provides the starting point for generation
     * of a Security Assertion for the keyspace.
     */
    private final URL url;

    /**
     * Constructor.
     *
     * @param keyspace the four character Machina keyspace that is the provider of the desired identity assertion
     * @throws IonicException on failure to lookup the enrollment URL associated with the keyspace
     */
    public DeviceEnrollment(final String keyspace) throws IonicException {
        this.url = DeviceUtils.toUrl(new IdentitySources(keyspace).getUrlSaml());
    }

    /**
     * Constructor.
     *
     * @param url the enrollment URL for the targeted keyspace
     */
    public DeviceEnrollment(final URL url) {
        this.url = url;
    }

    /**
     * Use the supplied assertion to generate a {@link com.ionic.sdk.device.profile.DeviceProfile} enrollment.
     *
     * @param assertion a (pre-generated) SAML assertion (proof of validated authentication to keyspace)
     * @return the Machina response output data object, containing the created device profile
     * @throws IonicException on errors during Machine service interactions
     */
    public CreateDeviceResponse enroll(final byte[] assertion) throws IonicException {
        final HttpClient httpClient = new HttpClientDefault(new AgentConfig(), url.getProtocol());
        final Properties enrollmentState = new Properties();
        try {
            initialize(httpClient, enrollmentState);
            submitSaml(httpClient, assertion, enrollmentState);
            retrievePubkey(httpClient, enrollmentState);
            return createDevice(enrollmentState);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_REQUESTFAILED, e);
        }
    }

    /**
     * Query the enrollment URL for context needed for subsequent service interactions.
     *
     * @param httpClient      the object encapsulating the http transactions needed to enroll a new device
     * @param enrollmentState the container for data used by the client to perform the device enrollment
     * @throws IonicException on service response data expectation failures
     * @throws IOException    on inability to perform the associated HTTP transaction
     */
    private void initialize(final HttpClient httpClient, final Properties enrollmentState)
            throws IonicException, IOException {
        // submit request
        final HttpRequest httpRequest = new HttpRequest(url, Http.Method.GET, url.getFile());
        final HttpResponse httpResponse = httpClient.execute(httpRequest);
        SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponse.getStatusCode()),
                SdkError.ISAGENT_REQUESTFAILED, SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
        // extract data from response
        final HttpHeaders httpHeaders = httpResponse.getHttpHeaders();
        setPropertyChecked(enrollmentState, httpHeaders, Enroll.Header.SAML_RELAY_STATE);
    }

    /**
     * Submit the pre-generated SAML assertion, proving identity to the Machina tenant.
     *
     * @param httpClient      the object encapsulating the http transactions needed to enroll a new device
     * @param assertion       the container for the supplied Identity Assertion data
     * @param enrollmentState the container for data used by the client to perform the device enrollment
     * @throws IonicException on service response data expectation failures
     * @throws IOException    on inability to perform the associated HTTP transaction
     */
    private void submitSaml(final HttpClient httpClient, final byte[] assertion, final Properties enrollmentState)
            throws IonicException, IOException {
        final String samlRelayState = enrollmentState.getProperty(Enroll.Header.SAML_RELAY_STATE);
        // submit request
        final URL urlSaml = DeviceUtils.toUrl(samlRelayState);
        final byte[] payload = Transcoder.utf8().decode(String.format(Enroll.Payload.SAML_RESPONSE,
                HttpUtils.urlEncode(Transcoder.base64().encode(assertion))));
        final HttpRequest httpRequest = new HttpRequest(
                urlSaml, Http.Method.POST, urlSaml.getFile(), new HttpHeaders(), new ByteArrayInputStream(payload));
        final HttpResponse httpResponse = httpClient.execute(httpRequest);
        SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponse.getStatusCode()),
                SdkError.ISAGENT_REQUESTFAILED, SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
        // extract data from response
        final HttpHeaders httpHeaders = httpResponse.getHttpHeaders();
        setPropertyChecked(enrollmentState, httpHeaders, Enroll.Header.REG_PUBKEY_URL);
        setPropertyChecked(enrollmentState, httpHeaders, Enroll.Header.REG_IONIC_URL);
        setPropertyChecked(enrollmentState, httpHeaders, Enroll.Header.REG_ENROLL_TAG);
        setPropertyChecked(enrollmentState, httpHeaders, Enroll.Header.REG_STOKEN);
        setPropertyChecked(enrollmentState, httpHeaders, Enroll.Header.REG_UIDAUTH);
    }

    /**
     * Retrieve the asymmetric Ionic tenant keyspace public key, used to build the {@link CreateDeviceRequest}.
     *
     * @param httpClient      the object encapsulating the http transactions needed to enroll a new device
     * @param enrollmentState the container for data used by the client to perform the device enrollment
     * @throws IonicException on service response data expectation failures
     * @throws IOException    on inability to perform the associated HTTP transaction
     */
    private void retrievePubkey(final HttpClient httpClient, final Properties enrollmentState)
            throws IonicException, IOException {
        final String urlString = enrollmentState.getProperty(Enroll.Header.REG_PUBKEY_URL);
        // submit request
        final URL urlPubkey = DeviceUtils.toUrl(urlString);
        final HttpRequest httpRequest = new HttpRequest(urlPubkey, Http.Method.GET, urlPubkey.getFile());
        final HttpResponse httpResponse = httpClient.execute(httpRequest);
        SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponse.getStatusCode()),
                SdkError.ISAGENT_REQUESTFAILED, SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
        // extract data from response
        final byte[] pubkeyBytes = Stream.read(httpResponse.getEntity());
        final String pubkeyText = Transcoder.utf8().encode(pubkeyBytes).trim();
        enrollmentState.setProperty(PublicKey.class.getName(), pubkeyText);
    }

    /**
     * Assemble the enrollment request, submit it to the Machina service, and process the response.
     *
     * @param enrollmentState the container for data used by the client to perform the device enrollment
     * @return the {@link CreateDeviceResponse}, which on success contains the new device profile
     * @throws IonicException on failure to initialize cryptography; on Machina operation failure
     */
    private CreateDeviceResponse createDevice(final Properties enrollmentState) throws IonicException {
        final String pubkey = enrollmentState.getProperty(PublicKey.class.getName());
        final String server = enrollmentState.getProperty(Enroll.Header.REG_IONIC_URL);
        final String keyspace = enrollmentState.getProperty(Enroll.Header.REG_ENROLL_TAG);
        final String token = enrollmentState.getProperty(Enroll.Header.REG_STOKEN);
        final String uid = enrollmentState.getProperty(Enroll.Header.REG_UIDAUTH);
        // transact with server
        final Agent agent = new Agent();
        agent.initializeWithoutProfiles();
        final CreateDeviceRequest request = new CreateDeviceRequest(
                getClass().getName(), server, keyspace, token, uid, pubkey);
        request.setRsaKeyHolder(null);
        return agent.createDevice(request);
    }

    /**
     * Extract the specified HTTP header value, and store to the enrollment state property set.
     *
     * @param properties  the enrollment state property set
     * @param httpHeaders the HTTP response header from which to extract the data value
     * @param name        the name of the HTTP response header associated with the desired value
     * @throws IonicException on expected data missing from the HTTP header set
     */
    private void setPropertyChecked(final Properties properties, final HttpHeaders httpHeaders, final String name)
            throws IonicException {
        final String value = httpHeaders.getHeaderValue(name);
        SdkData.checkTrue(value != null, SdkError.ISAGENT_MISSINGVALUE, name);
        properties.setProperty(name, value);
    }
}
