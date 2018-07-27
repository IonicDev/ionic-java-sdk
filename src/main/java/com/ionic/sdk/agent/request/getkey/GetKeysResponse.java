package com.ionic.sdk.agent.request.getkey;

import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.key.KeyObligationsMap;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.core.value.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the output for an Agent.getKeys() request.
 */
public class GetKeysResponse extends AgentResponseBase {

    /**
     * Represents a list of Key objects for an Agent.getKeys() response.
     */
    private final List<Key> keys;

    /**
     * Represents a list of IonicError objects for an Agent.getKeys() response.
     */
    private final List<IonicError> errors;

    /**
     * Represents a list of QueryResult objects for an Agent.getKeys() response.
     */
    private final List<QueryResult> results;

    /**
     * Constructor.
     */
    public GetKeysResponse() {
        super();
        this.keys = new ArrayList<Key>();
        this.errors = new ArrayList<IonicError>();
        this.results = new ArrayList<QueryResult>();
    }

    /**
     * @return a list of Key objects for an Agent.getKeys() response.
     */
    public final List<Key> getKeys() {
        return keys;
    }

    /**
     * @return a list of IonicError objects for an Agent.getKeys() response.
     */
    public final List<IonicError> getErrors() {
        return errors;
    }

    /**
     * @return a list of QueryResult objects for an Agent.getKeys() response.
     */
    public final List<QueryResult> getQueryResults() {
        return results;
    }

    /**
     * @param keyId the key identifier to search for in the server response
     * @return the key, if present, matching the specified key identifier
     */
    public final GetKeysResponse.Key getKey(final String keyId) {
        GetKeysResponse.Key key = null;
        for (GetKeysResponse.Key keyIt : keys) {
            if (keyId.equals(keyIt.getId())) {
                key = keyIt;
                break;
            }
        }
        return key;
    }

    /**
     * Retrieve the key error with the matching id.
     *
     * @param id an identifier to correlate the response
     * @return the matching error response
     */
    public final GetKeysResponse.IonicError getError(final String id) {
        GetKeysResponse.IonicError error = null;
        for (GetKeysResponse.IonicError errorIt : errors) {
            if (id.equals(errorIt.getKeyId())) {
                error = errorIt;
                break;
            }
        }
        return error;
    }

    /**
     * Retrieve the key error with the matching id.
     *
     * @param id an identifier to correlate the response
     * @return the matching error response
     */
    public final GetKeysResponse.QueryResult getQueryResult(final String id) {
        GetKeysResponse.QueryResult result = null;
        for (GetKeysResponse.QueryResult resultIt : results) {
            if (id.equals(resultIt.getKeyId())) {
                result = resultIt;
                break;
            }
        }
        return result;
    }

    /**
     * Add a key response object to the {@link GetKeysResponse}.
     *
     * @param key the object containing the parameters of the key response
     */
    public final void add(final Key key) {
        keys.add(key);
    }

    /**
     * Add a error response object to the {@link GetKeysResponse}.
     *
     * @param error the object containing the parameters of the key response
     */
    public final void add(final IonicError error) {
        errors.add(error);
    }

    /**
     * Add a query result response object to the {@link GetKeysResponse}.
     *
     * @param result the object containing the parameters of the key response
     */
    public final void add(final QueryResult result) {
        results.add(result);
    }

    /**
     * Represents a discrete key response object in the context of a {@link GetKeysResponse}.
     */
    public static class Key extends AgentKey {

        /**
         * The device ID associated with this key.
         */
        private String deviceId;

        /**
         * The key origin string.
         */
        private String origin;

        /**
         * Constructor.
         */
        public Key() {
            this("", new byte[0], null, new KeyAttributesMap(), new KeyAttributesMap(),
                    new KeyObligationsMap(), "", "", "");
        }

        /**
         * Constructor.
         *
         * @param id       the server id associated with this key
         * @param key      the raw bytes comprising the crypto key
         * @param deviceId the device ID associated with this key
         */
        public Key(final String id, final byte[] key, final String deviceId) {
            this(id, key, deviceId, new KeyAttributesMap(), new KeyAttributesMap(),
                    new KeyObligationsMap(), "", "", "");
        }

        /**
         * Constructor.
         *
         * @param id                the server id associated with this key
         * @param key               the raw bytes comprising the crypto key
         * @param attributes        the attributes associated with the key
         * @param mutableAttributes the updatable attributes associated with the key
         */
        public Key(final String id, final byte[] key,
                   final KeyAttributesMap attributes, final KeyAttributesMap mutableAttributes) {
            this(id, key, null, attributes, mutableAttributes,
                    new KeyObligationsMap(), "", "", "");
        }

        /**
         * Constructor.
         *
         * @param id                the server key id
         * @param key               the crypto key bytes
         * @param deviceId          the associated Ionic device id
         * @param attributes        the attributes for the key
         * @param mutableAttributes the updatable attributes for the key
         * @param keyObligations    the obligations for the key
         * @param origin            the origin of the key
         */
        @SuppressWarnings({"checkstyle:parameternumber"})  // ability to efficiently instantiate from server response
        public Key(final String id, final byte[] key, final String deviceId,
                   final KeyAttributesMap attributes, final KeyAttributesMap mutableAttributes,
                   final KeyObligationsMap keyObligations, final String origin) {
            this(id, key, deviceId, attributes, mutableAttributes, keyObligations, origin, null, null);
        }

        /**
         * Constructor.
         *
         * @param id                   the server id associated with this key
         * @param key                  the raw bytes comprising the crypto key
         * @param deviceId             the device ID associated with this key
         * @param attributes           the attributes associated with the key
         * @param mutableAttributes    the updatable attributes associated with the key
         * @param obligations          the (server specified) obligations to be observed by the requesting client
         * @param origin               the key origin string
         * @param attributesSig        the server signature applied to the immutable attributes (authentication)
         * @param mutableAttributesSig the server signature applied to the mutable attributes (authentication)
         */
        @SuppressWarnings({"checkstyle:parameternumber"})  // ability to efficiently instantiate from server response
        public Key(final String id, final byte[] key, final String deviceId,
                   final KeyAttributesMap attributes, final KeyAttributesMap mutableAttributes,
                   final KeyObligationsMap obligations, final String origin,
                   final String attributesSig, final String mutableAttributesSig) {
            setId(id);
            setKey(key);
            this.deviceId = Value.defaultOnEmpty(deviceId, "");
            setAttributesMap(attributes);
            setMutableAttributesMap(mutableAttributes);
            setMutableAttributesMapFromServer(mutableAttributes);
            setObligationsMap(obligations);
            this.origin = Value.defaultOnEmpty(origin, "");
            setAttributesSigBase64FromServer(attributesSig);
            setMutableAttributesSigBase64FromServer(mutableAttributesSig);
        }

        /**
         * @return the device ID associated with this key
         */
        public final String getDeviceId() {
            return deviceId;
        }

        /**
         * Set the device ID associated with this key (typically the device which requested creation of this key).
         *
         * @param deviceId the Ionic device ID associated with this key
         */
        public final void setDeviceId(final String deviceId) {
            this.deviceId = Value.defaultOnEmpty(deviceId, "");
        }

        /**
         * @return the key origin string
         */
        public final String getOrigin() {
            return origin;
        }

        /**
         * Set the origin string for this cryptography key.
         *
         * @param origin the origin identifier (typically Agent.KEYORIGIN_IONIC_KEYSERVER)
         */
        public final void setOrigin(final String origin) {
            this.origin = Value.defaultOnEmpty(origin, "");
        }
    }

    /**
     * Represents a discrete error response object in the context of a {@link GetKeysResponse}.
     */
    public static class IonicError {

        /**
         * A String denoting the id of the key.
         */
        private String keyId;

        /**
         * The client error code (SDK client side error code).
         */
        private int clientError;

        /**
         * The server error code provided by an Ionic server.
         */
        private int serverError;

        /**
         * The server error message string provided by an Ionic server.
         */
        private String serverMessage;

        /**
         * Constructor.
         *
         * @param keyId         the key ID (also known as the key tag)
         * @param clientError   the client error code (SDK client side error code)
         * @param serverError   the server error code provided by an Ionic server
         * @param serverMessage the server error message string provided by an Ionic server
         */
        public IonicError(final String keyId, final int clientError,
                          final int serverError, final String serverMessage) {
            this.keyId = keyId;
            this.clientError = clientError;
            this.serverError = serverError;
            this.serverMessage = serverMessage;
        }

        /**
         * @return The key ID (also known as the key tag).
         */
        public final String getKeyId() {
            return keyId;
        }

        /**
         * Set the key ID (key tag).
         *
         * @param keyId The key ID (also known as the key tag)
         */
        public final void setKeyId(final String keyId) {
            this.keyId = keyId;
        }

        /**
         * @return The client error code (SDK client side error code).
         */
        public final int getClientError() {
            return clientError;
        }

        /**
         * Set the client error code (SDK client side error code).
         *
         * @param clientError The client error code (SDK client side error code)
         */
        public final void setClientError(final int clientError) {
            this.clientError = clientError;
        }

        /**
         * @return The server error code provided by an Ionic server.
         */
        public final int getServerError() {
            return serverError;
        }

        /**
         * Set the server error code provided by an Ionic server.
         *
         * @param serverError the server error code provided by an Ionic server
         */
        public final void setServerError(final int serverError) {
            this.serverError = serverError;
        }

        /**
         * @return The server error message string provided by an Ionic server.
         */
        public final String getServerMessage() {
            return serverMessage;
        }

        /**
         * Set the server error message string provided by an Ionic server.
         *
         * @param serverMessage the server error message string provided by an Ionic server
         */
        public final void setServerMessage(final String serverMessage) {
            this.serverMessage = serverMessage;
        }
    }

    /**
     * Represents a discrete query response object in the context of a {@link GetKeysResponse}.
     */
    public static class QueryResult {

        /**
         * A String denoting the id of the key.
         */
        private String keyId;

        /**
         * Represents a list of mapped ids.
         */
        private List<String> mappedIdList;

        /**
         * The error code provided by an Ionic server.
         */
        private int errorCode;

        /**
         * The error message string provided by an Ionic server.
         */
        private String errorMessage;

        /**
         * Constructor.
         *
         * @param keyId        the key ID (also known as the key tag)
         * @param errorCode    the error code provided by an Ionic server
         * @param errorMessage the error message string provided by an Ionic server
         */
        public QueryResult(final String keyId, final int errorCode, final String errorMessage) {
            this.keyId = keyId;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.mappedIdList = new ArrayList<String>();
        }

        /**
         * Constructor.
         *
         * @param keyId           the key ID (also known as the key tag)
         * @param mappedIdList    a list of ids mapped on the server
         */
        public QueryResult(final String keyId, final List<String> mappedIdList) {
            this.keyId = keyId;
            this.errorCode = -1;
            this.errorMessage = null;
            this.mappedIdList = new ArrayList<String>(mappedIdList);
        }

        /**
         * @return The key ID (also known as the key tag).
         */
        public final String getKeyId() {
            return keyId;
        }

        /**
         * Set the key ID (key tag).
         *
         * @param keyId The key ID (also known as the key tag)
         */
        public final void setKeyId(final String keyId) {
            this.keyId = keyId;
        }

        /**
         * @return The list of mapped ids.
         */
        public final List<String> getMappedIds() {
            return mappedIdList;
        }

        /**
         * Set the list of mapped ids.
         *
         * @param mappedIdList The list of mapped ids.
         */
        public final void setMappedIds(final List<String> mappedIdList) {
            this.mappedIdList = new ArrayList<String>(mappedIdList);
        }

        /**
         * @return The error code provided by an Ionic server.
         */
        public final int getErrorCode() {
            return errorCode;
        }

        /**
         * Set the error code provided by an Ionic server.
         *
         * @param errorCode the error code provided by an Ionic server
         */
        public final void getErrorCode(final int errorCode) {
            this.errorCode = errorCode;
        }

        /**
         * @return The error message string provided by an Ionic server.
         */
        public final String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Set the error message string provided by an Ionic server.
         *
         * @param errorMessage the error message string provided by an Ionic server
         */
        public final void setErrorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
