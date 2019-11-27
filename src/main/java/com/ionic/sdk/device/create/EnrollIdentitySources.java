package com.ionic.sdk.device.create;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.config.AgentConfig;
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
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.json.JsonU;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Find an SDK enrollment implementation that is offered by the specified enrollment server.
 */
public class EnrollIdentitySources {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

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
     * @param url          the URL of the enrollment server
     * @param agent        the agent instance used to perform the enrollment
     * @param rsaKeyHolder the client-side RSA keypair to use in the context of the request
     * @throws IonicException on invalid input URL
     */
    public EnrollIdentitySources(final String url, final Agent agent,
                                 final RsaKeyHolder rsaKeyHolder) throws IonicException {
        this.url = AgentTransactionUtil.getProfileUrl(url);
        this.agent = agent;
        this.rsaKeyHolder = rsaKeyHolder;
    }

    /**
     * Enroll a new device in the Ionic server infrastructure.  Use the default URL for the supported
     * enrollment method.
     * <p>
     * reference: stash/projects/IP/repos/gyre/browse/api-spec/private/enrollment/GET_sources.md
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
            final HttpRequest httpRequest = new HttpRequest(url, Http.Method.GET, url.getFile());
            final HttpResponse httpResponse = httpClient.execute(httpRequest);
            SdkData.checkTrue(AgentTransactionUtil.isHttpSuccessCode(
                    httpResponse.getStatusCode()), SdkError.ISAGENT_REQUESTFAILED);
            logger.info(httpResponse.getHttpHeaders().getHeaderValue(Http.Header.CONTENT_TYPE));
            final byte[] entity = Stream.read(httpResponse.getEntity());
            final JsonObject jsonEntity = JsonU.getJsonObject(Transcoder.utf8().encode(entity));
            logger.finest(JsonU.toJson(jsonEntity, true));
            final JsonObject jsonIS = JsonSource.getJsonObject(jsonEntity, IDENTITY_SOURCES);
            if (JsonValue.ValueType.ARRAY.equals(JsonSource.getValueType(jsonIS, IDC))) {
                // first preference is to perform IDC enrollment
                final JsonArray jsonIonicAuth = JsonSource.getJsonArray(jsonIS, IDC);
                final Iterator<JsonValue> iterator = JsonSource.getIterator(jsonIonicAuth);
                while (iterator.hasNext()) {
                    final JsonObject jsonMethod = (JsonObject) iterator.next();
                    final boolean isDefault = JsonSource.getBoolean(jsonMethod, IS_DEFAULT);
                    if (isDefault) {
                        final String uri = JsonSource.getString(jsonMethod, URI);
                        final EnrollIonicAuth enrollIonicAuth = new EnrollIonicAuth(uri, agent, rsaKeyHolder);
                        return enrollIonicAuth.enroll(user, pass, deviceProfileName);
                    }
                }
                // did not find a default, though spec requires a default
                throw new IonicException(SdkError.ISAGENT_MISSINGVALUE, IS_DEFAULT);
            } else if (JsonValue.ValueType.ARRAY.equals(JsonSource.getValueType(jsonIS, SAML))) {
                // second preference is to perform SAML enrollment
                final JsonArray jsonSAML = JsonSource.getJsonArray(jsonIS, SAML);
                final Iterator<JsonValue> iterator = JsonSource.getIterator(jsonSAML);
                while (iterator.hasNext()) {
                    final JsonObject jsonMethod = (JsonObject) iterator.next();
                    final boolean isDefault = JsonSource.getBoolean(jsonMethod, IS_DEFAULT);
                    if (isDefault) {
                        final String uri = JsonSource.getString(jsonMethod, URI);
                        final EnrollSAML enrollSAML = new EnrollSAML(uri, agent, rsaKeyHolder);
                        return enrollSAML.enroll(user, pass, deviceProfileName);
                    }
                }
                // did not find a default, though spec requires a default
                throw new IonicException(SdkError.ISAGENT_MISSINGVALUE, IS_DEFAULT);
            } else {
                // only IDC and SAML enrollment methods are currently implemented at
                // client; neither supported at server
                throw new IonicException(SdkError.ISAGENT_NOTIMPLEMENTED);
            }
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_REQUESTFAILED, e);
        }
    }

    /**
     * Text string associated with content in "identity sources" JSON document.
     */
    private static final String IDENTITY_SOURCES = "identitySources";

    /**
     * Text string associated with content in "identity sources" JSON document.
     */
    private static final String IDC = "IDC";

    /**
     * Text string associated with content in "identity sources" JSON document.
     */
    private static final String SAML = "SAML";

    /**
     * Text string associated with content in "identity sources" JSON document.
     */
    private static final String URI = "uri";

    /**
     * Text string associated with content in "identity sources" JSON document.
     */
    private static final String IS_DEFAULT = "isDefault";
}
