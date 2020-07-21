package com.ionic.sdk.service.api;

import com.ionic.sdk.agent.config.AgentConfig;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Code widget for requesting information about a given Machina API endpoint.
 */
public class ServiceQuery {

    /**
     * The Machina service endpoint to be queried.
     */
    private final URL url;

    /**
     * Constructor.
     *
     * @param url the Machina service endpoint to be queried
     */
    public ServiceQuery(final URL url) {
        this.url = url;
    }

    /**
     * @return the Machina service endpoint to be queried
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Query the Machina service endpoint for the version document.
     *
     * @return a {@link ServiceVersion}, containing information about the software version in use at the endpoint
     * @throws IonicException <ul>
     *                        <li>{@link SdkError#ISAGENT_REQUESTFAILED} on service failure to provide requested
     *                        version resource</li>
     *                        <li>{@link SdkError#ISAGENT_BADRESPONSE} on unexpected service response (non-json)</li>
     *                        </ul>
     */
    public ServiceVersion getVersion() throws IonicException {
        final HttpRequest httpRequest = new HttpRequest(url, Http.Method.GET, url.getFile(), new HttpHeaders(), null);
        final AgentConfig config = new AgentConfig();
        final HttpClient httpClient = new HttpClientDefault(config, url.getProtocol());
        try {
            final HttpResponse httpResponse = httpClient.execute(httpRequest);
            // validate response
            final int statusCode = httpResponse.getStatusCode();
            SdkData.checkTrue(statusCode == HttpURLConnection.HTTP_OK, SdkError.ISAGENT_REQUESTFAILED);
            final String contentType = httpResponse.getHttpHeaders().getHeaderValue(Http.Header.CONTENT_TYPE);
            SdkData.checkNotNull(contentType, Http.Header.CONTENT_TYPE);
            SdkData.checkTrue(contentType.contains(Http.Header.CONTENT_TYPE_SERVER), SdkError.ISAGENT_BADRESPONSE);
            return new ServiceVersion(Stream.read(httpResponse.getEntity()));
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_REQUESTFAILED, e);
        }
    }
}
