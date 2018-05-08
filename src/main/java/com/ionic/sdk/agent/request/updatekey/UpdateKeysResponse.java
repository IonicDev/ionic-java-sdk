package com.ionic.sdk.agent.request.updatekey;

import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.request.base.AgentResponseBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the output for an Agent.updateKeys() request.
 */
public class UpdateKeysResponse extends AgentResponseBase {

    /**
     * Represents a list of Key objects for an Agent.updateKeys() response.
     */
    private final List<Key> keys;

    /**
     * Represents a list of Key objects for an Agent.updateKeys() response.
     */
    private final List<IonicError> errors;

    /**
     * Constructor.
     */
    public UpdateKeysResponse() {
        super();
        this.keys = new ArrayList<Key>();
        this.errors = new ArrayList<IonicError>();
    }

    /**
     * @return a list of Key objects for an Agent.updateKeys() response.
     */
    public final List<Key> getKeys() {
        return keys;
    }

    /**
     * @return a list of Error objects for an Agent.updateKeys() response.
     */
    public final List<IonicError> getErrors() {
        return errors;
    }

    /**
     * Add a key response object to the {@link UpdateKeysResponse}.
     *
     * @param key the object containing the parameters of the key response
     */
    public final void add(final Key key) {
        keys.add(key);
    }

    /**
     * Add a error response object to the {@link UpdateKeysResponse}.
     *
     * @param error the object containing the parameters of the key response
     */
    public final void add(final IonicError error) {
        errors.add(error);
    }

    /**
     * Retrieve the key response with the matching id.
     *
     * @param id an identifier to correlate the response
     * @return the matching key response
     */
    public final UpdateKeysResponse.Key getKey(final String id) {
        UpdateKeysResponse.Key key = null;
        for (UpdateKeysResponse.Key keyIt : keys) {
            if (id.equals(keyIt.getId())) {
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
    public final UpdateKeysResponse.IonicError getError(final String id) {
        UpdateKeysResponse.IonicError error = null;
        for (UpdateKeysResponse.IonicError errorIt : errors) {
            if (id.equals(errorIt.getKeyId())) {
                error = errorIt;
                break;
            }
        }
        return error;
    }

    /**
     * Represents a discrete key response object in the context of a {@link UpdateKeysResponse}.
     */
    public static class Key extends AgentKey {

        /**
         * The device id associated with the creation request.
         */
        private final String deviceId;

        /**
         * The origin of the associated key.
         */
        private final String origin;

        /**
         * Constructor.
         *
         * @param agentKey the base key implementation
         * @param deviceId the associated Ionic device id
         * @param origin   the origin of the key
         */
        public Key(final AgentKey agentKey, final String deviceId, final String origin) {
            super(agentKey);
            this.deviceId = deviceId;
            this.origin = origin;
        }

        /**
         * @return the device id associated with the creation request
         */
        public final String getDeviceId() {
            return deviceId;
        }

        /**
         * @return the origin of the associated key
         */
        public final String getOrigin() {
            return origin;
        }
    }

    /**
     * Represents a discrete error response object in the context of a {@link UpdateKeysResponse}.
     */
    public static class IonicError {

        /**
         * A String denoting the id of the key.
         */
        private final String keyId;

        /**
         * The client error code (SDK client side error code).
         */
        private final int clientError;

        /**
         * The server error code provided by an Ionic server.
         */
        private final int serverError;

        /**
         * The server error message string provided by an Ionic server.
         */
        private final String serverMessage;

        /**
         * Constructor.
         *
         * @param keyId         the key ID (also known as the key tag)
         * @param clientError   the client error code (SDK client side error code)
         * @param serverError   the server error code provided by an Ionic server
         * @param serverMessage the server error message string provided by an Ionic server
         */
        public IonicError(
                final String keyId, final int clientError, final int serverError, final String serverMessage) {
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
         * @return The client error code (SDK client side error code).
         */
        public final int getClientError() {
            return clientError;
        }

        /**
         * @return The server error code provided by an Ionic server.
         */
        public final int getServerError() {
            return serverError;
        }

        /**
         * @return The server error message string provided by an Ionic server.
         */
        public final String getServerMessage() {
            return serverMessage;
        }
    }
}
