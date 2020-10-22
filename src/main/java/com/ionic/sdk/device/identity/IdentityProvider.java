package com.ionic.sdk.device.identity;

import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.device.create.saml.Enroll;
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
 * Widget providing an interface to request Security Assertions proving authenticated access to a Machina
 * keyspace tenant, given login credentials.
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
public final class IdentityProvider {

    /**
     * The enrollment URL for the targeted keyspace, which provides the starting point for generation
     * of a Security Assertion for the keyspace.
     */
    private final URL url;

    /**
     * @return the enrollment URL for the targeted keyspace
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Constructor.
     *
     * @param keyspace the four character Machina keyspace that is the provider of the desired identity assertion
     * @throws IonicException on failure to lookup the SAML enrollment URL associated with the keyspace
     */
    public IdentityProvider(final String keyspace) throws IonicException {
        this.url = DeviceUtils.toUrl(new IdentitySources(keyspace).getUrlSaml());
    }

    /**
     * Constructor.
     *
     * @param url the SAML enrollment URL for the targeted keyspace
     */
    public IdentityProvider(final URL url) {
        this.url = url;
    }

    /**
     * Request an identity assertion from the target Machina keyspace.  This assertion proves the ability to
     * authenticate to the keyspace, given the supplied credentials.  SAML identity assertions are used to enroll
     * devices to the Machina keyspace, allowing for authenticated access to privileged data.
     *
     * @param user the account name of the identity to authenticate
     * @param pass the password of the identity to authenticate
     * @return a byte[] containing the XML-formatted identity assertion
     * @throws IonicException on network failure; authentication failure; malformed data
     */
    public byte[] generateSamlAssertion(final String user, final String pass) throws IonicException {
        final HttpClient httpClient = new HttpClientDefault(new AgentConfig(), url.getProtocol());
        try {
            // submit initial request (query for saml parameters)
            final HttpRequest httpRequest1 = new HttpRequest(url, Http.Method.GET, url.getFile());
            final HttpResponse httpResponse1 = httpClient.execute(httpRequest1);
            SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponse1.getStatusCode()),
                    SdkError.ISAGENT_REQUESTFAILED, SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
            final HttpHeaders httpHeaders1 = httpResponse1.getHttpHeaders();
            final String samlRedirect = httpHeaders1.getHeaderValue(Enroll.Header.SAML_REDIRECT);
            final String samlRequest = httpHeaders1.getHeaderValue(Enroll.Header.SAML_REQUEST);
            final String samlRelayState = httpHeaders1.getHeaderValue(Enroll.Header.SAML_RELAY_STATE);
            SdkData.checkTrue((samlRedirect != null), SdkError.ISAGENT_MISSINGVALUE, Enroll.Header.SAML_REDIRECT);
            SdkData.checkTrue((samlRequest != null), SdkError.ISAGENT_MISSINGVALUE, Enroll.Header.SAML_REQUEST);
            SdkData.checkTrue((samlRelayState != null), SdkError.ISAGENT_MISSINGVALUE, Enroll.Header.SAML_RELAY_STATE);
            // submit saml request
            final URL urlSaml = DeviceUtils.toUrl(samlRedirect);
            final byte[] payloadSaml = Transcoder.utf8().decode(String.format(Enroll.Payload.SAML_REQUEST,
                    HttpUtils.urlEncode(user), HttpUtils.urlEncode(pass), HttpUtils.urlEncode(samlRequest)));
            final HttpRequest httpRequestSaml = new HttpRequest(urlSaml, Http.Method.POST, urlSaml.getFile(),
                    new HttpHeaders(), new ByteArrayInputStream(payloadSaml));
            final HttpResponse httpResponseSaml = httpClient.execute(httpRequestSaml);
            SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponseSaml.getStatusCode()),
                    SdkError.ISAGENT_REQUESTFAILED, SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
            final HttpHeaders httpHeadersSaml = httpResponseSaml.getHttpHeaders();
            final String samlResponse = httpHeadersSaml.getHeaderValue(Enroll.Header.SAML_RESPONSE);
            return Transcoder.base64().decode(samlResponse);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_REQUESTFAILED, e);
        }
    }
}
