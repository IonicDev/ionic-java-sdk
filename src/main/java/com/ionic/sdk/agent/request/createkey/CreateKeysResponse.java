package com.ionic.sdk.agent.request.createkey;

import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.key.KeyObligationsMap;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the output for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#createKeys(CreateKeysRequest)} API call.
 * <p>
 * The request will contain 0..n {@link CreateKeysResponse.Key} objects, which contain cryptography keys for use in
 * subsequent crypto operations.
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
     * @return the first key in the list of Key objects for an Agent.getKeys() response.
     * @throws IonicException if no keys are available in the response
     */
    public final Key getFirstKey() throws IonicException {
        SdkData.checkTrue(!keyResponses.isEmpty(), SdkError.ISAGENT_KEY_DENIED);
        return keyResponses.iterator().next();
    }

    /**
     * @param refId the reference to search for in the server response
     * @return the key record, if present, matching the specified key identifier
     */
    public final Key findKey(final String refId) {
        Key key = null;
        for (Key keyIt : keyResponses) {
            if (refId.equals(keyIt.getRefId())) {
                key = keyIt;
                break;
            }
        }
        return key;
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
        private String refId;

        /**
         * The device id associated with the creation request.
         */
        private String deviceId;

        /**
         * Constructor.
         */
        public Key() {
            this(null, "", new byte[0], null, null, null, null, null, null, null);
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
         * @param refId             the client key id
         * @param id                the server key id
         * @param key               the crypto key bytes
         * @param deviceId          the associated Ionic device id
         * @param attributes        the attributes for the key
         * @param mutableAttributes the updatable attributes for the key
         * @param keyObligations    the obligations for the key
         * @param origin            the origin of the key
         */
        @SuppressWarnings({"checkstyle:parameternumber"})  // ability to efficiently instantiate from server response
        public Key(final String refId, final String id, final byte[] key, final String deviceId,
                   final KeyAttributesMap attributes, final KeyAttributesMap mutableAttributes,
                   final KeyObligationsMap keyObligations, final String origin) {
            this(refId, id, key, deviceId, attributes, mutableAttributes, keyObligations, origin, null, null);
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
            this.refId = Value.defaultOnEmpty(refId, "");
            setId(id);
            setKey(key);
            this.deviceId = Value.defaultOnEmpty(deviceId, "");
            setAttributesMap(attributes);
            setMutableAttributesMap(mutableAttributes);
            setMutableAttributesMapFromServer(mutableAttributes);
            setObligationsMap(keyObligations);
            setOrigin(Value.defaultOnEmpty(origin, ""));
            setAttributesSigBase64FromServer(attributesSig);
            setMutableAttributesSigBase64FromServer(mutableAttributesSig);
        }

        /**
         * @return the client reference associated with this key.
         */
        public final String getRefId() {
            return refId;
        }

        /**
         * Set the client reference for this key, in the context of this CreateKeys server operation.
         *
         * @param refId a reference to be used to associate response keys with the corresponding request.
         */
        public final void setRefId(final String refId) {
            this.refId = Value.defaultOnEmpty(refId, "");
        }

        /**
         * @return the device id associated with the creation request
         */
        public final String getDeviceId() {
            return deviceId;
        }

        /**
         * Set the device id associated with the creation request.
         *
         * @param deviceId the device id associated with the creation request
         */
        public final void setDeviceId(final String deviceId) {
            this.deviceId = Value.defaultOnEmpty(deviceId, "");
        }
    }
}
