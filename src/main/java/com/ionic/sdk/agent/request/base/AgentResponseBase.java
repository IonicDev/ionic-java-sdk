package com.ionic.sdk.agent.request.base;

import com.ionic.sdk.core.value.Value;

import javax.json.JsonObject;
import java.io.Serializable;

/**
 * The base class for Ionic Machina Tools service responses to client requests.   These encapsulate the server
 * response to an https request made to the Machina server infrastructure.
 * <p>
 * Service errors encountered in the context of a service request are passed back to the API caller via this object.
 * <p>
 * Machina will generate a "conversation id" (CID) internally for each service interaction.  On service errors, the
 * CID will be helpful to the Machina support team in identifying the cause of the error.
 * <p>
 * Implementations of {@link com.ionic.sdk.key.KeyServices} may choose to set all, some, or none of the
 * {@link AgentResponseBase} data values, as is appropriate for the implementation.  Users of
 * {@link com.ionic.sdk.key.KeyServices} implementations should treat these values as read-only, for
 * diagnostic purposes.
 */
public class AgentResponseBase implements Serializable {

    /**
     * The HTTP server status code from the response.
     */
    private int httpResponseCode;

    /**
     * The json object representation of the server response.
     */
    private transient JsonObject jsonPayload;

    /**
     * The Ionic server error code (if any) from the response.
     */
    private int serverErrorCode;

    /**
     * The Ionic server error text (if any) from the response.
     */
    private String serverErrorMessage;

    /**
     * The Ionic server error json (if any) from the response.
     */
    private String serverErrorDataJson;

    /**
     * The Ionic conversation ID (supplied by the client in the corresponding request), used for correlation and
     * transaction integrity.
     */
    private String cid;

    /**
     * Constructor.
     */
    public AgentResponseBase() {
        this.cid = "";
        this.jsonPayload = null;
    }

    /**
     * @return the HTTP status code from the corresponding server response
     */
    public final int getHttpResponseCode() {
        return httpResponseCode;
    }

    /**
     * Set the HTTP status code from the corresponding server response.
     *
     * @param httpResponseCode the server status code
     */
    public final void setHttpResponseCode(final int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    /**
     * @return the json object representation of the server response
     */
    public final JsonObject getJsonPayload() {
        return jsonPayload;
    }

    /**
     * Set the json object representation of the server response.
     *
     * @param jsonPayload the json response object
     */
    public final void setJsonPayload(final JsonObject jsonPayload) {
        this.jsonPayload = jsonPayload;
    }

    /**
     * @return the Ionic error code from the corresponding server response
     */
    public final int getServerErrorCode() {
        return serverErrorCode;
    }

    /**
     * Set the Ionic error code from the corresponding server response.
     *
     * @param serverErrorCode the Ionic error code
     */
    public final void setServerErrorCode(final int serverErrorCode) {
        this.serverErrorCode = serverErrorCode;
    }

    /**
     * @return the Ionic server error text from the corresponding server response
     */
    public final String getServerErrorMessage() {
        return serverErrorMessage;
    }

    /**
     * Set the Ionic server error text from the corresponding server response.
     *
     * @param serverErrorMessage the Ionic error text
     */
    public final void setServerErrorMessage(final String serverErrorMessage) {
        this.serverErrorMessage = serverErrorMessage;
    }

    /**
     * @return the Ionic server error json (if any) from the corresponding server response
     */
    public final String getServerErrorDataJson() {
        return serverErrorDataJson;
    }

    /**
     * Set the Ionic server error json (if any) from the corresponding server response.
     *
     * @param serverErrorDataJson the Ionic error json (text)
     */
    public final void setServerErrorDataJson(final String serverErrorDataJson) {
        this.serverErrorDataJson = serverErrorDataJson;
    }

    /**
     * @return the Ionic conversation ID from the server response
     */
    public final String getConversationId() {
        return cid;
    }

    /**
     * Set the Ionic conversation ID from the server response.
     *
     * @param cid the Ionic conversation ID
     */
    public final void setConversationId(final String cid) {
        this.cid = Value.defaultOnEmpty(cid, "");
    }

    /**
     * @return whether server response payload requires a "data" component
     */
    protected boolean isDataRequired() {
        return true;
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = -3598379258252305464L;
}
