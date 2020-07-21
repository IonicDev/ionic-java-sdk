package com.ionic.sdk.service.api;

import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;

import javax.json.JsonObject;

/**
 * Container for information exposed by Machina web service API version document.
 */
public final class ServiceVersion {

    /**
     * The maximum Machina web service API version supported by this endpoint.
     */
    private final String versionMax;

    /**
     * The minimum Machina web service API version supported by this endpoint.
     */
    private final String versionMin;

    /**
     * The Machina web service software version running on this endpoint.
     */
    private final String sprint;

    /**
     * Constructor.
     *
     * @param json the data bytes of the json document served by the endpoint
     * @throws IonicException on unexpected data in the json document
     */
    public ServiceVersion(final byte[] json) throws IonicException {
        final JsonObject jsonObject = JsonIO.readObject(json);
        final JsonObject versions = JsonSource.getJsonObject(jsonObject, VERSIONS);
        this.versionMax = JsonSource.getString(versions, HIGHEST);
        this.versionMin = JsonSource.getString(versions, LOWEST);
        this.sprint = JsonSource.getString(versions, SPRINT);
    }

    /**
     * @return the maximum Machina web service API version supported by this endpoint
     */
    public String getVersionMax() {
        return versionMax;
    }

    /**
     * @return the minimum Machina web service API version supported by this endpoint
     */
    public String getVersionMin() {
        return versionMin;
    }

    /**
     * @return the Machina web service software version running on this endpoint
     */
    public String getSprint() {
        return sprint;
    }

    /**
     * Marker for json version document content.
     */
    private static final String VERSIONS = "versions";

    /**
     * Marker for json version document content.
     */
    private static final String HIGHEST = "highest";

    /**
     * Marker for json version document content.
     */
    private static final String LOWEST = "lowest";

    /**
     * Marker for json version document content.
     */
    private static final String SPRINT = "sprint";
}
