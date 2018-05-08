package com.ionic.sdk.agent.request.getkey;

import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.key.KeyObligationsMap;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.service.IDC;

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
     * Constructor.
     */
    public GetKeysResponse() {
        super();
        this.keys = new ArrayList<Key>();
    }

    /**
     * @return a list of Key objects for an Agent.createKeys() response.
     */
    public final List<Key> getKeys() {
        return keys;
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
     * Represents a discrete key response object in the context of a {@link GetKeysResponse}.
     */
    public static class Key extends AgentKey {

        /**
         * The device ID associated with this key.
         */
        private final String deviceId;

        /**
         * The key origin string.
         */
        private final String origin;

        /**
         * Constructor.
         */
        public Key() {
            this("", new byte[0], null, new KeyAttributesMap(), new KeyAttributesMap(),
                    new KeyObligationsMap(), IDC.Metadata.KEYORIGIN_IONIC, "", "");
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
                    new KeyObligationsMap(), IDC.Metadata.KEYORIGIN_IONIC, "", "");
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
                    new KeyObligationsMap(), IDC.Metadata.KEYORIGIN_IONIC, "", "");
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
            this.deviceId = deviceId;
            setAttributesMap(attributes);
            setMutableAttributes(mutableAttributes);
            setMutableAttributesFromServer(mutableAttributes);
            setObligationsMap(obligations);
            this.origin = origin;
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
         * @return the key origin string
         */
        public final String getOrigin() {
            return origin;
        }
    }
}
