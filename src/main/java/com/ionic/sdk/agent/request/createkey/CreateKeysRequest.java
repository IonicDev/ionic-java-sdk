package com.ionic.sdk.agent.request.createkey;

import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.service.IDC;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the input for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#createKeys(CreateKeysRequest)} API call.
 * <p>
 * The request will contain information about {@link Key} objects, which specify the desired quantity of keys, as
 * well as attributes that should be associated with the requested keys.  {@link CreateKeysRequest} may
 * contain 1..n {@link Key} objects.
 * <p>
 * Each key returned from the call will be identified using a key id, accessed
 * via {@link com.ionic.sdk.agent.key.KeyBase#getId()}.  This key id is typically associated with the data encrypted
 * using the key.
 * <p>
 * The CreateKey / CreateKeys family of APIs allow for new AES keys to be securely generated, in the context of a data
 * encryption usage.  Subsequent GetKey / GetKeys calls allow for the retrieval of the keys, via the key id, to enable
 * permitted decryption of the secured data.
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/create-key' target='_blank'>Machina Developers</a> for
 * more information about the CreateKey operation.
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
     * Constructor.
     *
     * @param keys an initial group of {@link Key} objects to include in the service transaction
     */
    public CreateKeysRequest(final Key... keys) {
        this();
        this.keyRequests.addAll(Arrays.asList(keys));
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
     * Find the {@link CreateKeysRequest.Key} record associated with the specified reference id.
     * <p>
     * If the specified reference id is not found in the {@link CreateKeysRequest}, <code>null</code> is returned.
     *
     * @param refId a reference identifier to correlate {@link CreateKeysRequest.Key} records to the
     *              corresponding {@link CreateKeysResponse.Key}
     * @return the matching {@link CreateKeysRequest.Key} record; or <code>null</code> if not found
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

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = 7896365050876806964L;

    /**
     * Represents a discrete key request object in the context of a {@link CreateKeysRequest}.
     */
    public static class Key implements Serializable {

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
         */
        public Key() {
            this(IDC.Payload.REF);
        }

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
         * @deprecated Please migrate usages to the replacement {@link #getMutableAttributesMap()}
         * method (Ionic SDK 1.x API compatibility).
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

        /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
        private static final long serialVersionUID = 6972104104350430296L;
    }
}
