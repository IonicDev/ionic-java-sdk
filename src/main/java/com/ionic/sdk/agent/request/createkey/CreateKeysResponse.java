package com.ionic.sdk.agent.request.createkey;

import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.key.KeyObligationsMap;
import com.ionic.sdk.agent.request.base.AgentResponseBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the output for an Agent.createKeys() request.
 */
public class CreateKeysResponse extends AgentResponseBase {

    /**
     * Represents a list of Key objects for an Agent.createKeys() response.
     */
    private final List<Key> keyResponses;

    /**
     * Constructor.
     */
    public CreateKeysResponse() {
        super();
        this.keyResponses = new ArrayList<Key>();
    }

    /**
     * @return a list of Key objects for an Agent.createKeys() response.
     */
    public final List<Key> getKeys() {
        return keyResponses;
    }

    /**
     * Add a key response object to the {@link CreateKeysResponse}.
     *
     * @param key the object containing the parameters of the key response
     */
    public final void add(final Key key) {
        keyResponses.add(key);
    }

    /**
     * Represents a discrete key response object in the context of a {@link CreateKeysResponse}.
     */
    public static class Key extends AgentKey {

        /**
         * The client supplied id associated with this key.
         */
        private final String refId;

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
         */
        public Key() {
            this(null, null, null, null, null, null, null, null, null, null);
        }

        /**
         * Constructor.
         *
         * @param refId    the client key id
         * @param id       the server key id
         * @param key      the crypto key bytes
         * @param deviceId the associated Ionic device id
         */
        public Key(final String refId, final String id, final byte[] key, final String deviceId) {
            this(refId, id, key, deviceId, null, null, null, null, null, null);
        }

        /**
         * Constructor.
         *
         * @param refId          the client key id
         * @param id             the server key id
         * @param key            the crypto key bytes
         * @param deviceId       the associated Ionic device id
         * @param attributes     the attributes for the key
         * @param keyObligations the obligations for the key
         * @param origin         the origin of the key
         */
        public Key(final String refId, final String id, final byte[] key, final String deviceId,
                   final KeyAttributesMap attributes, final KeyObligationsMap keyObligations, final String origin) {
            this(refId, id, key, deviceId, attributes, null, keyObligations, origin, null, null);
        }

        /**
         * Constructor.
         *
         * @param refId                the client key id
         * @param id                   the server key id
         * @param key                  the crypto key bytes
         * @param deviceId             the associated Ionic device id
         * @param attributes           the attributes for the key
         * @param mutableAttributes    the updatable attributes for the key
         * @param keyObligations       the obligations for the key
         * @param origin               the origin of the key
         * @param attributesSig        the server signature applied to the immutable attributes (authentication)
         * @param mutableAttributesSig the server signature applied to the mutable attributes (authentication)
         */
        @SuppressWarnings({"checkstyle:parameternumber"})  // ability to efficiently instantiate from server response
        public Key(final String refId, final String id, final byte[] key, final String deviceId,
                   final KeyAttributesMap attributes, final KeyAttributesMap mutableAttributes,
                   final KeyObligationsMap keyObligations, final String origin,
                   final String attributesSig, final String mutableAttributesSig) {
            this.refId = refId;
            setId(id);
            setKey(key);
            this.deviceId = deviceId;
            setAttributesMap(attributes);
            setMutableAttributes(mutableAttributes);
            setMutableAttributesFromServer(mutableAttributes);
            setObligationsMap(keyObligations);
            this.origin = origin;
            setAttributesSigBase64FromServer(attributesSig);
            setMutableAttributesSigBase64FromServer(mutableAttributesSig);
        }

        /**
         * @return the client key id associated with this key.
         */
        public final String getRefId() {
            return refId;
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
}
