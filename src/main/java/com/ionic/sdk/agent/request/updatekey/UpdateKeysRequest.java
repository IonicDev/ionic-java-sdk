package com.ionic.sdk.agent.request.updatekey;

import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.request.base.AgentRequestBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the input for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#updateKeys(UpdateKeysRequest)} API call.
 * <p>
 * The request will contain information about {@link Key} objects, which specify the cryptography key objects to be
 * updated, as well as attributes that should be associated with the specified keys.  {@link UpdateKeysRequest} may
 * contain 1..n {@link Key} objects.
 * <p>
 * The UpdateKey / UpdateKeys family of APIs allow for the mutable attributes associated with existing AES keys to
 * be updated.  Subsequent GetKey / GetKeys calls allow for the retrieval of the keys, via the key id, and receive any
 * updates to the key's mutable attributes.
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/update-key' target='_blank'>Machina Developers</a> for
 * more information about the UpdateKey operation.
 */
public class UpdateKeysRequest extends AgentRequestBase {

    /**
     * Represents a list of Key objects for an Agent.createKeys() request.
     */
    private final List<Key> keyRequests;

    /**
     * Constructor.
     */
    public UpdateKeysRequest() {
        this.keyRequests = new ArrayList<Key>();
    }

    /**
     * @return a list of Key objects for an Agent.createKeys() request.
     */
    public final List<Key> getKeys() {
        return keyRequests;
    }

    /**
     * Add a key request object to the {@link UpdateKeysRequest}.
     *
     * @param key the object containing the parameters of the key request
     * @deprecated
     *      Please migrate usages to the replacement {@link #addKey(Key)}
     *      method (Ionic SDK 1.x API compatibility).
     */
    @Deprecated
    public final void add(final Key key) {
        keyRequests.add(key);
    }

    /**
     * Add a key request object to the {@link UpdateKeysRequest}.
     *
     * @param key the object containing the parameters of the key request
     */
    public final void addKey(final Key key) {
        keyRequests.add(key);
    }

    /**
     * Add a key request object to the {@link UpdateKeysRequest}.
     *
     * @param key         the object containing the parameters of the key request
     * @param forceUpdate instruction for server to unconditionally overwrite the existing mutable attributes
     */
    public final void addKey(final Key key, final boolean forceUpdate) {
        keyRequests.add(new Key(key, forceUpdate));
    }

    /**
     * Retrieve the {@link UpdateKeysRequest.Key} record associated with the specified key tag.
     *
     * @param keyId a Machina key tag
     * @return the matching {@link UpdateKeysRequest.Key} record, or <code>null</code> if not found
     * @deprecated Please migrate usages to the replacement {@link #getKey(String)}
     */
    @Deprecated
    public final Key findKey(final String keyId) {
        Key key = null;
        for (Key keyRequest : keyRequests) {
            if (keyId.equals(keyRequest.getId())) {
                key = keyRequest;
                break;
            }
        }
        return key;
    }

    /**
     * Retrieve the {@link UpdateKeysRequest.Key} record associated with the specified key tag.
     *
     * @param keyId a Machina key tag
     * @return the matching {@link UpdateKeysRequest.Key} record, or <code>null</code> if not found
     */
    public final Key getKey(final String keyId) {
        Key key = null;
        for (Key keyRequest : keyRequests) {
            if (keyId.equals(keyRequest.getId())) {
                key = keyRequest;
                break;
            }
        }
        return key;
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = 9157502748079517417L;

    /**
     * Represents a discrete key request object in the context of a {@link UpdateKeysRequest}.
     */
    public static class Key extends AgentKey {

        /**
         * Flag indicating desire to force write of updated mutable attributes in the context of this request.
         */
        private final boolean forceUpdate;

        /**
         * Constructor.
         */
        public Key() {
            super();
            this.forceUpdate = false;
        }

        /**
         * Constructor.
         *
         * @param key from which to copy attributes
         */
        public Key(final AgentKey key) {
            super(key);
            this.forceUpdate = false;
        }

        /**
         * Constructor.
         *
         * @param key         from which to copy attributes
         * @param forceUpdate instruction for server to unconditionally overwrite the existing mutable attributes
         */
        public Key(final AgentKey key, final boolean forceUpdate) {
            super(key);
            this.forceUpdate = forceUpdate;
        }

        /**
         * @return server instruction to overwrite existing mutable attributes
         * @deprecated
         *      Please migrate usages to the replacement {@link #getForceUpdate()}
         *      method (Ionic SDK 1.x API compatibility).
         */
        @Deprecated
        public final boolean isForceUpdate() {
            return forceUpdate;
        }

        /**
         * @return server instruction to overwrite existing mutable attributes
         */
        public final boolean getForceUpdate() {
            return forceUpdate;
        }

        /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
        private static final long serialVersionUID = 1512173787583075644L;
    }
}
