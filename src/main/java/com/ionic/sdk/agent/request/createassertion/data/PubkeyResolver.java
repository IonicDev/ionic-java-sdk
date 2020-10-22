package com.ionic.sdk.agent.request.createassertion.data;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.request.getkeyspace.GetKeyspaceResponse;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.httpclient.Http;
import com.ionic.sdk.httpclient.HttpClient;
import com.ionic.sdk.httpclient.HttpClientDefault;
import com.ionic.sdk.httpclient.HttpHeaders;
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Utility class for fetching the RSA public key associated with a given Machina keyspace.  This may be used to
 * validate information associated with the keyspace (for example, device identity assertions).
 */
public class PubkeyResolver {

    /**
     * The {@link com.ionic.sdk.key.KeyServices} implementation used to query for public key information.
     */
    private final Agent agent;

    /**
     * Constructor.
     *
     * @param agent the {@link com.ionic.sdk.key.KeyServices} implementation used to query for public key information
     */
    public PubkeyResolver(final Agent agent) {
        this.agent = agent;
    }

    /**
     * Given a Machina tenant keyspace, derive the RSA public key associated with the tenant.
     *
     * @param keyspace the Machina tenant keyspace associated with the Identity Assertion
     * @return the base64-encoded string representation of the raw RSA public key
     * @throws IonicException on failure to query KNS; failure to fetch the public key content
     */
    public String getPublicKeyKeyspace(final String keyspace) throws IonicException {
        return getKeyspacePubkey(keyspaceToPubkeyURL(keyspace));
    }

    /**
     * Given a Machina tenant enrollment URL, derive the RSA public key associated with the tenant.
     *
     * @param enrollmentURL the enrollment URL for the Machina tenant keyspace associated with the Identity Assertion
     * @return the base64-encoded string representation of the raw RSA public key
     * @throws IonicException on failure to query public key URL
     */
    public String getPublicKeyEnrollURL(final String enrollmentURL) throws IonicException {
        final String pubkeyURL = enrollmentURL.replace(RESOURCE_REGISTER, RESOURCE_PUBKEY);
        return getKeyspacePubkey(DeviceUtils.toUrl(pubkeyURL));
    }

    /**
     * Given a Machina keyspace, derive the URL associated with the RSA public key of the keyspace.
     *
     * @param keyspace the Machina keyspace associated with the Identity Assertion
     * @return the URL of the associated RSA public key
     * @throws IonicException on failure to query KNS (Keyspace Name Service) for the keyspace enrollment information
     */
    private URL keyspaceToPubkeyURL(final String keyspace) throws IonicException {
        final GetKeyspaceResponse getKeyspaceResponse = agent.getKeyspace(keyspace);
        final List<String> enrollmentURLs = getKeyspaceResponse.getEnrollmentURLs();
        SdkData.checkTrue(!enrollmentURLs.isEmpty(), SdkError.ISAGENT_REQUESTFAILED, IDC.Payload.ENROLL);
        final String enrollmentURL = enrollmentURLs.iterator().next().replace(RESOURCE_REGISTER, RESOURCE_PUBKEY);
        return DeviceUtils.toUrl(enrollmentURL);
    }

    /**
     * Resource fragment for Machina enrollment URL for a given namespace.
     */
    private static final String RESOURCE_REGISTER = "/register";

    /**
     * Resource fragment for Machina public key URL for a given namespace.
     */
    private static final String RESOURCE_PUBKEY = "/pubkey";

    /**
     * Fetch the RSA public key for the keyspace associated with the Identity Assertion.
     *
     * @param url the URL of the associated RSA public key
     * @return the base64-encoded string representation of the raw RSA public key
     * @throws IonicException on failure to fetch the public key content
     */
    private String getKeyspacePubkey(final URL url) throws IonicException {
        final HttpRequest httpRequest = new HttpRequest(url, Http.Method.GET, url.getFile(), new HttpHeaders(), null);
        final HttpClient httpClient = new HttpClientDefault(agent.getConfig(), url.getProtocol());
        try {
            final HttpResponse httpResponse = httpClient.execute(httpRequest);
            final byte[] pubkeyEntity = Stream.read(httpResponse.getEntity());
            return Transcoder.utf8().encode(pubkeyEntity).trim();
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_REQUESTFAILED);
        }
    }
}
