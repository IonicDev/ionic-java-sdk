package com.ionic.sdk.agent.request.getkey;

import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.key.KeyObligationsMap;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the output for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#getKeys(GetKeysRequest)} API call.
 * <p>
 * The response will contain a set of 0..n {@link Key} objects, which contain cryptography keys for use in
 * subsequent crypto operations.
 * <p>
 * {@link QueryResult} objects map an external id to a list of key tags associated with Machina key records.  The
 * {@link Key} records corresponding to the mapped key tags may then be requested via usage of the API
 * {@link GetKeysResponse#getKey(String)}, passing the mapped id as the string parameter.
 * <h3>Error Handling</h3>
 * The {@link GetKeysRequest} (as a whole) might be rejected by the service for various reasons.  Use the following
 * APIs to check for request failure:
 * <ul>
 * <li>{@link #getHttpResponseCode()},</li>
 * <li>{@link #getServerErrorCode()},</li>
 * <li>{@link #getServerErrorMessage()},</li>
 * <li>{@link #getServerErrorDataJson()}.</li>
 * </ul>
 * <p>
 * Individual GetKey requests may generate an associated error.  This information is accessible
 * via the {@link IonicError} class, and the API {@link #getErrors()}.
 * <p>
 * Individual GetKeyByExternalId requests may generate an associated error.  This information is available via
 * the {@link QueryResult} class, and the API {@link #getQueryResults()}.
 * <h3>Additional Information</h3>
 * The {@link com.ionic.sdk.key.KeyServices#createKeys(com.ionic.sdk.agent.request.createkey.CreateKeysRequest)} family
 * of APIs allow for new AES keys
 * to be securely generated, in the context of a data encryption usage.  Subsequent calls to the
 * {@link com.ionic.sdk.key.KeyServices#getKeys(GetKeysRequest)} family of APIs allow for the retrieval of the
 * keys, to enable permitted decryption of the secured data.
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/get-key' target='_blank'>Get Key</a> and
 * <a href='https://dev.ionic.com/sdk/tasks/get-key-by-external-id' target='_blank'>Get Key By External Id</a> for
 * more information about the GetKey operation.
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
     * Constructor.
     *
     * @param keys the group of {@link GetKeysResponse.Key} objects to include in the response
     */
    public GetKeysResponse(final GetKeysResponse.Key... keys) {
        this();
        this.keys.addAll(Arrays.asList(keys));
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
     * Convenience method to return the first {@link Key} record contained in the {@link GetKeysResponse}.  As
     * requests for a single key are common, this method provides a shorthand for easily accessing the key.
     * <p>
     * Use of this call implies that a key is expected in the response, and that failure of this expectation should
     * be treated as an exceptional condition.
     *
     * @return the first key in the list of Key objects for an Agent.getKeys() response.
     * @throws IonicException if no keys are available in the response
     */
    public final Key getFirstKey() throws IonicException {
        SdkData.checkTrue(!keys.isEmpty(), SdkError.ISAGENT_KEY_DENIED);
        return keys.iterator().next();
    }

    /**
     * Find the {@link Key} record associated with the specified {@link com.ionic.sdk.key.KeyServices} key tag.
     * <p>
     * If the specified key tag is not found in the {@link com.ionic.sdk.key.KeyServices} response,
     * <code>null</code> is returned.
     *
     * @param keyId the id to search for in the response
     * @return the key record matching the specified key tag, if present; otherwise null
     * @deprecated please migrate usages to the equivalent API {@link #getKey(String)}
     */
    @Deprecated
    public final GetKeysResponse.Key findKey(final String keyId) {
        return getKeyById(keyId);
    }

    /**
     * Find the {@link Key} record associated with the specified {@link com.ionic.sdk.key.KeyServices} key tag.
     * <p>
     * If the specified key tag is not found in the {@link com.ionic.sdk.key.KeyServices} response,
     * <code>null</code> is returned.
     *
     * @param keyId the id to search for in the response
     * @return the key record matching the specified key tag; or <code>null</code> if not found
     */
    public final GetKeysResponse.Key getKey(final String keyId) {
        return getKeyById(keyId);
    }

    /**
     * @param keyId the id to search for in the response
     * @return the key record matching the specified key tag, if present; otherwise null
     */
    private GetKeysResponse.Key getKeyById(final String keyId) {
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
     * Find the {@link IonicError} record associated with the specified {@link AgentKey} key tag.
     * <p>
     * If no error is associated with the specified key tag in the {@link com.ionic.sdk.key.KeyServices} response,
     * <code>null</code> is returned.
     *
     * @param id the key tag to search for in the response errors
     * @return the {@link GetKeysResponse.IonicError} matching the specified key tag, if present; otherwise null
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
     * Find the {@link QueryResult} record associated with the specified {@link com.ionic.sdk.key.KeyServices}
     * external id.
     * <p>
     * If the specified external id is not found in the {@link GetKeysResponse}, <code>null</code> is returned.
     *
     * @param id the external id to search for in the response
     * @return the {@link QueryResult} matching the specified external identifier, if present; otherwise null
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

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = -8256370090345978102L;

    /**
     * Represents a discrete key response object in the context of a {@link GetKeysResponse}.
     * <p>
     * Key objects include an id (a label allowing for references to the record), as well as the byte[]
     * representation of the key.
     */
    public static class Key extends AgentKey {

        /**
         * The device ID associated with this key.
         */
        private String deviceId;

        /**
         * Constructor.
         *
         * @param key an existing object with data to populate this new object
         */
        public Key(final AgentKey key) {
            this(key.getId(), key.getKey(), null, key.getAttributesMap(),
                    key.getMutableAttributesMap(), key.getObligationsMap(), key.getOrigin());
        }

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
         * @param id                the server key tag
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
            setOrigin(Value.defaultOnEmpty(origin, ""));
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

        /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
        private static final long serialVersionUID = 7596297196036949557L;
    }

    /**
     * Represents a discrete error response object in the context of a {@link GetKeysResponse}.
     * <p>
     * IonicError objects are used to indicate service problems while processing a {@link GetKeysRequest}.  Errors
     * might include a server error code and message, and / or a client (SDK) error code.
     */
    public static class IonicError implements Serializable {

        /**
         * A String denoting the id of the key.
         */
        private String keyId;

        /**
         * The client error code (SDK client side error code).
         *
         * @see SdkError
         */
        private int clientError;

        /**
         * The service error code provided by the {@link com.ionic.sdk.key.KeyServices} implementation.
         */
        private int serverError;

        /**
         * The service error message provided by the {@link com.ionic.sdk.key.KeyServices} implementation.
         */
        private String serverMessage;

        /**
         * Constructor.
         *
         * @param keyId         the key tag
         * @param clientError   the client error code (SDK client side error code)
         * @param serverError   the service error code provided by the {@link com.ionic.sdk.key.KeyServices}
         *                      implementation
         * @param serverMessage the server error message string provided by the {@link com.ionic.sdk.key.KeyServices}
         *                      implementation
         */
        public IonicError(final String keyId, final int clientError,
                          final int serverError, final String serverMessage) {
            this.keyId = keyId;
            this.clientError = clientError;
            this.serverError = serverError;
            this.serverMessage = serverMessage;
        }

        /**
         * @return The key tag.
         */
        public final String getKeyId() {
            return keyId;
        }

        /**
         * Set the key tag.
         *
         * @param keyId The key tag
         */
        public final void setKeyId(final String keyId) {
            this.keyId = keyId;
        }

        /**
         * @return The client error code (SDK client side error code).
         * @see SdkError
         */
        public final int getClientError() {
            return clientError;
        }

        /**
         * Set the client error code (SDK client side error code).
         *
         * @param clientError The client error code (SDK client side error code)
         * @see SdkError
         */
        public final void setClientError(final int clientError) {
            this.clientError = clientError;
        }

        /**
         * @return The service error code provided by the {@link com.ionic.sdk.key.KeyServices} implementation.
         */
        public final int getServerError() {
            return serverError;
        }

        /**
         * Set the service error code provided by the {@link com.ionic.sdk.key.KeyServices} implementation.
         *
         * @param serverError the service error code
         */
        public final void setServerError(final int serverError) {
            this.serverError = serverError;
        }

        /**
         * @return The service error message string provided by the {@link com.ionic.sdk.key.KeyServices}
         * implementation.
         */
        public final String getServerMessage() {
            return serverMessage;
        }

        /**
         * Set the service error message string provided by the {@link com.ionic.sdk.key.KeyServices} implementation.
         *
         * @param serverMessage the server error message string
         */
        public final void setServerMessage(final String serverMessage) {
            this.serverMessage = serverMessage;
        }

        /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
        private static final long serialVersionUID = 874472719891645740L;
    }

    /**
     * Represents a discrete query response object in the context of a {@link GetKeysResponse}.
     * <p>
     * Machina key records may include an optional external id, set at the time of key create.  Machina
     * {@link GetKeysRequest} objects may include requests for keys associated with a specified external id, by using
     * the following APIs, and specifying the desired external id:
     * <ul>
     * <li>{@link GetKeysRequest#addExternalId(String)} (for requesting the default quantity),</li>
     * <li>{@link GetKeysRequest#addExternalId(String, int)} (for requesting a specific quantity).</li>
     * </ul>
     * <p>
     * QueryResult objects include references to the requested external id, and to a set of mapped ids (strings)
     * associated with Machina key records.  The {@link Key} records corresponding to the mapped ids may then be
     * requested via usage of the API {@link GetKeysResponse#getKey(String)}, passing the mapped id as the
     * string parameter.
     * <p>
     * Individual GetKey requests by external id may generate an associated error.  This information is accessible
     * via the APIs:
     * <ul>
     * <li>{@link QueryResult#getErrorCode()}</li>
     * <li>{@link QueryResult#getErrorMessage()}</li>
     * </ul>
     * <p>
     * The external id facility is available in support of key usage scenarios such as scheduled key rotation.
     */
    public static class QueryResult implements Serializable {

        /**
         * The string denoting the requested external id of the key.
         */
        private String keyId;

        /**
         * The list of {@link com.ionic.sdk.key.KeyServices} key tags mapped to the specified external id.
         */
        private List<String> mappedIdList;

        /**
         * The error code provided by the {@link com.ionic.sdk.key.KeyServices} implementation, if any, in response
         * to the request for the specified external id.
         */
        private int errorCode;

        /**
         * The error message string provided by the {@link com.ionic.sdk.key.KeyServices} implementation, if any,
         * in response to the request for the specified external id.
         */
        private String errorMessage;

        /**
         * Constructor.
         *
         * @param keyId        the external id of the key
         * @param errorCode    the error code provided by the {@link com.ionic.sdk.key.KeyServices} implementation
         * @param errorMessage the error message string provided by the {@link com.ionic.sdk.key.KeyServices}
         *                     implementation
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
         * @param keyId        the external id of the key
         * @param mappedIdList the list of {@link com.ionic.sdk.key.KeyServices} key tags mapped to the external id
         */
        public QueryResult(final String keyId, final List<String> mappedIdList) {
            this.keyId = keyId;
            this.errorCode = 0;
            this.errorMessage = null;
            final List<String> mappedIdListSafe = (mappedIdList == null) ? new ArrayList<String>() : mappedIdList;
            this.mappedIdList = new ArrayList<String>(mappedIdListSafe);
        }

        /**
         * @return The external id of the key (also known as the key tag).
         */
        public final String getKeyId() {
            return keyId;
        }

        /**
         * Set the external id of the key (also known as the key tag).
         *
         * @param keyId The external id of the key
         */
        public final void setKeyId(final String keyId) {
            this.keyId = keyId;
        }

        /**
         * @return The list of {@link com.ionic.sdk.key.KeyServices} key tags mapped to the specified external id.
         */
        public final List<String> getMappedIds() {
            return mappedIdList;
        }

        /**
         * Set the list of {@link com.ionic.sdk.key.KeyServices} key tags mapped to the specified external id.
         *
         * @param mappedIdList The list of mapped key tags.
         */
        public final void setMappedIds(final List<String> mappedIdList) {
            final List<String> mappedIdListSafe = (mappedIdList == null) ? new ArrayList<String>() : mappedIdList;
            this.mappedIdList = new ArrayList<String>(mappedIdListSafe);
        }

        /**
         * @return The error code provided by the {@link com.ionic.sdk.key.KeyServices} implementation, if any,
         * in response to the request for the specified external id.
         * @see SdkError
         */
        public final int getErrorCode() {
            return errorCode;
        }

        /**
         * Set the error code provided by the {@link com.ionic.sdk.key.KeyServices} implementation, in response
         * to the request for the specified external id.
         *
         * @param errorCode The error code provided by the {@link com.ionic.sdk.key.KeyServices} implementation.
         * @see SdkError
         */
        public final void setErrorCode(final int errorCode) {
            this.errorCode = errorCode;
        }

        /**
         * @return The error message string provided by the {@link com.ionic.sdk.key.KeyServices} implementation,
         * if any, in response to the request for the specified external id.
         * @see SdkError
         */
        public final String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Set the error message string provided by the {@link com.ionic.sdk.key.KeyServices} implementation.
         *
         * @param errorMessage the error message string provided by the
         *                     {@link com.ionic.sdk.key.KeyServices} implementation
         * @see SdkError
         */
        public final void setErrorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
        }

        /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
        private static final long serialVersionUID = 6680563312476596034L;
    }
}
