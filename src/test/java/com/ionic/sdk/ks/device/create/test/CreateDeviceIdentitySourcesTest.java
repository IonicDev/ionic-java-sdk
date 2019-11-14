package com.ionic.sdk.ks.device.create.test;

import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.httpclient.Http;
import com.ionic.sdk.httpclient.HttpClient;
import com.ionic.sdk.httpclient.HttpClientDefault;
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.json.JsonU;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test common resource for enumerating available enrollment methods.
 */
public class CreateDeviceIdentitySourcesTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The "identity sources" URL is a well-known URI for enumerating the enrollment methods configured for a given
     * tenant keyspace.  When this test a run in the context of the defined system environment variable
     * "TEST_REGISTRATION_URL", derive the "identity sources" URL from the registration URL, and query it for the JSON
     * enumerating the enrollment methods.
     *
     * @throws IOException    on failure to access the identity sources document at the derived URL
     * @throws IonicException on failure to parse the identity sources document at the derived URL
     */
    @Test
    public final void testIdentitySources_EnumerateEnrollmentMethods_Success() throws IOException, IonicException {
        // get the test suite registration URL
        final String testRegistrationUrl = System.getenv("TEST_REGISTRATION_URL");
        Assume.assumeNotNull("skip this test when no registration URL is defined", testRegistrationUrl);
        // check the URL for the expected format (keyspace is expected to be four characters)
        final Pattern patternKeyspace = Pattern.compile("(.+?)/keyspace/(\\S{4})/.+");
        final Matcher matcherKeyspace = patternKeyspace.matcher(testRegistrationUrl);
        Assume.assumeTrue("expect keyspace to be derivable from registration URL", matcherKeyspace.matches());
        // derive the identity sources URL
        final String baseURL = matcherKeyspace.group(1);
        final String keyspace = matcherKeyspace.group(2);
        logger.info(String.format("[%s]  [%s]", baseURL, keyspace));
        final URL url = new URL(String.format("%s/keyspace/%s/identity_sources", baseURL, keyspace));
        // fetch the document at the identity sources URL
        final HttpClient httpClient = new HttpClientDefault(new AgentConfig(), url.getProtocol());
        final HttpRequest httpRequest = new HttpRequest(url, Http.Method.GET, url.getFile());
        final HttpResponse httpResponse = httpClient.execute(httpRequest);
        logger.info(String.format("REQUEST: [%s], STATUS: [%d]",
                url.toExternalForm(), httpResponse.getStatusCode()));
        Assert.assertTrue(AgentTransactionUtil.isHttpSuccessCode(httpResponse.getStatusCode()));
        final byte[] entity = Stream.read(httpResponse.getEntity());
        // parse the document at the identity sources URL
        final JsonObject jsonEntity = JsonU.getJsonObject(Transcoder.utf8().encode(entity));
        logger.info(JsonU.toJson(jsonEntity, true));
        final JsonObject jsonIS = JsonSource.getJsonObject(jsonEntity, "identitySources");
        final Iterator<Map.Entry<String, JsonValue>> iteratorSource = JsonSource.getIterator(jsonIS);
        while (iteratorSource.hasNext()) {
            final Map.Entry<String, JsonValue> entry = iteratorSource.next();
            final String key = entry.getKey();
            final JsonValue value = entry.getValue();
            if (JsonValue.ValueType.NULL.equals(JsonSource.getValueType(value))) {
                logger.finest("ENROLLMENT METHOD NOT SUPPORTED FOR THIS TENANT " + key);
            } else if (value instanceof JsonArray) {
                logger.finest("ENROLLMENT METHOD SUPPORTED FOR THIS TENANT " + key);
                // enumerate configuration parameters
                logger.info(key + " - " + url.toExternalForm());
                final JsonArray jsonArray = (JsonArray) value;
                final Iterator<JsonValue> iteratorMethod = JsonSource.getIterator(jsonArray);
                final JsonValue jsonValue = iteratorMethod.next();
                logger.info(JsonU.toJson((JsonObject) jsonValue, true));
            } else {
                Assert.fail("UNEXPECTED IDENTITY SOURCES CONTENT " + key);
            }
        }
    }
}
