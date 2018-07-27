package com.ionic.sdk.agent.request.createkey;

import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.base.AgentRequestBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the input for an Agent.createKeys() request.
 */
public class CreateKeysRequest extends AgentRequestBase {

    /**
     * Represents a list of Key objects for an Agent.createKeys() request.
     */
    private final List<Key> keyRequests;

    /**
     * Constructor.
     */
    public CreateKeysRequest() {
        this.keyRequests = new ArrayList<Key>();
    }

    /**
     * @return a list of Key objects for an Agent.createKeys() request.
     */
    public final List<Key> getKeys() {
        return keyRequests;
    }

    /**
     * Add a key request object to the {@link CreateKeysRequest}.
     *
     * @param key the object containing the parameters of the key request
     */
    public final void add(final Key key) {
        keyRequests.add(key);
    }

    /**
     * Retrieve the key request with the matching refId.
     *
     * @param refId an identifier to correlate the request
     * @return the matching key request
     */
    public final Key getKey(final String refId) {
        Key key = null;
        for (Key keyRequest : keyRequests) {
            if (refId.equals(keyRequest.getRefId())) {
                key = keyRequest;
                break;
            }
        }
        return key;
    }

    /**
     * Represents a discrete key request object in the context of a {@link CreateKeysRequest}.
     */
    public static class Key {

        /**
         * A reference to be used to associate any keys received in the response with the request.
         */
        private String refId;

        /**
         * The number of keys requested by this key request.
         */
        private int quantity;

        /**
         * The attributes to be associated with the keys requested by this key request.
         */
        private final KeyAttributesMap attributes;

        /**
         * The attributes to be associated with the keys requested by this key request.
         */
        private final KeyAttributesMap mutableAttributes;

        /**
         * Constructor.
         *
         * @param refId a reference to be used to associate keys received in the response with the request
         */
        public Key(final String refId) {
            this(refId, 1);
        }

        /**
         * Constructor.
         *
         * @param refId    a reference to be used to associate keys received in the response with the request
         * @param quantity the number of keys requested by this key request
         */
        public Key(final String refId, final int quantity) {
            this(refId, quantity, new KeyAttributesMap());
        }

        /**
         * Constructor.
         *
         * @param refId      a reference to be used to associate keys received in the response with the request
         * @param quantity   the number of keys requested by this key request
         * @param attributes the attributes to be associated with the requested keys
         */
        public Key(final String refId, final int quantity, final KeyAttributesMap attributes) {
            this(refId, quantity, attributes, new KeyAttributesMap());
        }

        /**
         * Constructor.
         *
         * @param refId             a reference to be used to associate keys received in the response with the request
         * @param quantity          the number of keys requested by this key request
         * @param attributes        the attributes to be associated with the requested keys
         * @param mutableAttributes the attributes to be associated with the requested keys; which may be updated
         */
        public Key(final String refId, final int quantity, final KeyAttributesMap attributes,
                   final KeyAttributesMap mutableAttributes) {
            this.refId = refId;
            this.quantity = quantity;
            this.attributes = attributes;
            this.mutableAttributes = mutableAttributes;
        }

        /**
         * @return a reference to be used to associate any keys received in the response with the request.
         */
        public final String getRefId() {
            return refId;
        }

        /**
         * Set the reference id for this key request object, in the context of this CreateKeys server operation.
         *
         * @param refId a reference to be used to associate any keys received in the response with the request.
         */
        public final void setRefId(final String refId) {
            this.refId = refId;
        }

        /**
         * @return the number of keys requested by this key request
         */
        public final int getQuantity() {
            return quantity;
        }

        /**
         * Set the number of keys requested by this request.
         *
         * @param quantity the number of keys requested by this key request
         */
        public final void setQuantity(final int quantity) {
            this.quantity = quantity;
        }

        /**
         * @return the attributes to be associated with the keys requested by this key request
         */
        public final KeyAttributesMap getAttributesMap() {
            return attributes;
        }

        /**
         * @return the attributes to be associated with the keys requested by this key request
         * @deprecated
         *      Please migrate usages to the replacement {@link #getMutableAttributesMap()}
         *      method (Ionic SDK 1.x API compatibility).
         */
        @Deprecated
        public final KeyAttributesMap getMutableAttributes() {
            return mutableAttributes;
        }

        /**
         * @return the attributes to be associated with the keys requested by this key request
         */
        public final KeyAttributesMap getMutableAttributesMap() {
            return mutableAttributes;
        }
    }
}
