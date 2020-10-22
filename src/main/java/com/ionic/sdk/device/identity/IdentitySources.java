package com.ionic.sdk.device.identity;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.request.getkeyspace.GetKeyspaceResponse;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.httpclient.Http;
import com.ionic.sdk.httpclient.HttpClient;
import com.ionic.sdk.httpclient.HttpClientDefault;
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

/**
 * Container for information obtained from the identity sources document associated with a Machina keyspace.
 */
public class IdentitySources {

    /**
     * Deserialized identity sources document for the keyspace.
     */
    private final JsonObject jsonObject;

    /**
     * Constructor.
     *
     * @param keyspace the four-character Machina keyspace to be queried
     * @throws IonicException on failure to query the enrollment server for the identity sources information
     */
    public IdentitySources(final String keyspace) throws IonicException {
        this(keyspace, toEnrollmentURL(keyspace));
    }

    /**
     * Constructor.
     * <p>
     * For keyspaces that are not registered in KNS (for example onsite instances), the identity sources
     * information is available if the relevant enrollment server is known.
     *
     * @param keyspace      the four-character Machina keyspace to be queried
     * @param urlEnrollment the URL representation of the enrollment server to be queried
     * @throws IonicException on failure to query KNS (Keyspace Name Service) or the enrollment server for
     *                        the identity sources information
     */
    public IdentitySources(final String keyspace, final URL urlEnrollment) throws IonicException {
        final HttpClient httpClient = new HttpClientDefault(new AgentConfig(), urlEnrollment.getProtocol());
        final String resource = String.format(IDC.Resource.IDENTITY_SOURCES, keyspace);
        final HttpRequest httpRequest1 = new HttpRequest(urlEnrollment, Http.Method.GET, resource);
        try {
            final HttpResponse httpResponse1 = httpClient.execute(httpRequest1);
            SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponse1.getStatusCode()),
                    SdkError.ISAGENT_REQUESTFAILED, SdkError.getErrorString(SdkError.ISAGENT_REQUESTFAILED));
            final byte[] entity = Stream.read(httpResponse1.getEntity());
            this.jsonObject = JsonIO.readObject(entity);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_UNEXPECTEDRESPONSE);
        }
    }

    /**
     * @return the Machina tenant ID associated with the keyspace
     * @throws IonicException on data expectation failures (missing SAML configuration)
     */
    public String getTenantIdSaml() throws IonicException {
        final JsonObject identitySources = JsonSource.getJsonObject(jsonObject, IDENTITY_SOURCES);
        final JsonArray saml = JsonSource.getJsonArray(identitySources, METHOD_SAML);
        final Iterator<JsonValue> iterator = JsonSource.getIterator(saml);
        SdkData.checkTrue(iterator.hasNext(), SdkError.ISAGENT_MISSINGVALUE, METHOD_SAML);
        final JsonObject saml0 = (JsonObject) iterator.next();
        return JsonSource.getString(saml0, TENANT_ID);
    }

    /**
     * @return the SAML enrollment URL registered for the keyspace
     * @throws IonicException on data expectation failures (missing SAML configuration)
     */
    public String getUrlSaml() throws IonicException {
        final JsonObject identitySources = JsonSource.getJsonObject(jsonObject, IDENTITY_SOURCES);
        final JsonArray saml = JsonSource.getJsonArray(identitySources, METHOD_SAML);
        final Iterator<JsonValue> iterator = JsonSource.getIterator(saml);
        SdkData.checkTrue(iterator.hasNext(), SdkError.ISAGENT_MISSINGVALUE, METHOD_SAML);
        final JsonObject saml0 = (JsonObject) iterator.next();
        return JsonSource.getString(saml0, URI);
    }

    /**
     * Query the Keyspace Name Service (KNS) for the enrollment URL registered to the keyspace.
     *
     * @param keyspace the four-character Machina keyspace to be queried
     * @return the enrollment URL associated with the keyspace
     * @throws IonicException on failure to query KNS; failure to fetch the public key content
     */
    private static URL toEnrollmentURL(final String keyspace) throws IonicException {
        final Agent agent = new Agent();
        agent.initializeWithoutProfiles();
        final GetKeyspaceResponse getKeyspaceResponse = agent.getKeyspace(keyspace);
        return DeviceUtils.toUrl(getKeyspaceResponse.getFirstEnrollmentURL());
    }

    /**
     * Identity sources document attribute name.
     */
    private static final String IDENTITY_SOURCES = "identitySources";

    /**
     * Identity sources document attribute name.
     */
    private static final String METHOD_SAML = "SAML";

    /**
     * Identity sources document attribute name.
     */
    private static final String TENANT_ID = "tenantId";

    /**
     * Identity sources document attribute name.
     */
    private static final String URI = "uri";
}
