package com.ionic.sdk.agent.request.getkey;

import com.ionic.sdk.agent.request.base.AgentRequestBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the input for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#getKeys(GetKeysRequest)} API call.
 * <p>
 * The request may contain a set of 1..n key tags.  Each key tag uniquely identifies an cryptography key stored by the
 * Machina infrastructure.  To request a key fetch, use the API {@link #add(String)}.
 * <p>
 * The request may also contain a set of 1..n external ids.  Each external id identifies a set of cryptography keys
 * stored by the Machina infrastructure.  To request a key fetch by external id, use the following APIs, specifying
 * the external id as the string parameter:
 * <ul>
 * <li>{@link GetKeysRequest#addExternalId(String)} (for requesting the default quantity),</li>
 * <li>{@link GetKeysRequest#addExternalId(String, int)} (for requesting a specific quantity).</li>
 * </ul>
 * <p>
 * The CreateKey / CreateKeys family of APIs allow for new AES keys to be securely generated, in the context of a data
 * encryption usage.  Subsequent GetKey / GetKeys calls allow for the retrieval of the keys, to enable permitted
 * decryption of the secured data.
 * <p>
 * See <a href='https://dev.ionic.com/sdk/tasks/get-key' target='_blank'>Get Key</a> and
 * <a href='https://dev.ionic.com/sdk/tasks/get-key-by-external-id' target='_blank'>Get Key By External Id</a> for
 * more information about the GetKey operation.
 */
public class GetKeysRequest extends AgentRequestBase {

    /**
     * Represents a list of Key objects for an Agent.getKeys() request.
     */
    private final List<String> keyIds;

    /**
     * Represents a list of External Key objects for an Agent.getKeys() request.
     */
    private final List<ExternalId> externalIds;

    /**
     * Constructor.
     */
    public GetKeysRequest() {
        this.keyIds = new ArrayList<String>();
        this.externalIds = new ArrayList<ExternalId>();
    }

    /**
     * Constructor.
     *
     * @param keyIds an initial group of Machina key IDs to include in the service transaction
     */
    public GetKeysRequest(final String... keyIds) {
        this();
        this.keyIds.addAll(Arrays.asList(keyIds));
    }

    /**
     * @return a list of Key objects for an Agent.getKeys() request.
     */
    public final List<String> getKeyIds() {
        return keyIds;
    }

    /**
     * Add a key request object to the {@link GetKeysRequest}.
     *
     * @param keyId the object containing the parameters of the key request
     */
    public final void add(final String keyId) {
        keyIds.add(keyId);
    }

    /**
     * @return the list of external ids for an Agent.getKeys() request.
     */
    public final List<String> getExternalIds() {
        final List<String> externalIdList = new ArrayList<String>();
        for (ExternalId externalIdIt : this.externalIds) {
            externalIdList.add(externalIdIt.getExternalId());
        }
        return externalIdList;
    }

    /**
     * @return the list of {@link ExternalId} objects for an Agent.getKeys() request.
     */
    public final List<ExternalId> getExternalIdObjects() {
        return externalIds;
    }

    /**
     * Add an external key request object to the {@link GetKeysRequest}.
     *
     * @param externalId the object containing the parameters of the key request
     */
    public final void addExternalId(final String externalId) {
        externalIds.add(new ExternalId(externalId));
    }

    /**
     * Add an external key request object to the {@link GetKeysRequest}.
     *
     * @param externalId the object containing the parameters of the key request
     * @param quantity the number of keys requested by this key request
     */
    public final void addExternalId(final String externalId, final int quantity) {
        externalIds.add(new ExternalId(externalId, quantity));
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = -1929556960018921637L;

    /**
     * Represents a request for External Key objects in the context of a call to
     * {@link com.ionic.sdk.agent.Agent#getKeys(GetKeysRequest)}.
     */
    public static class ExternalId implements Serializable {

        /**
         * An external (non-Ionic) label used to group a set of Ionic cryptography keys.
         */
        private String externalId;

        /**
         * The number of keys requested by this key request.
         */
        private int quantity;

        /**
         * Constructor.
         *
         * @param externalId a label used to group a set of Ionic cryptography keys
         */
        public ExternalId(final String externalId) {
            this(externalId, 0);
        }

        /**
         * Constructor.
         *
         * @param externalId a label used to group a set of Ionic cryptography keys
         * @param quantity the number of keys requested by this key request
         */
        public ExternalId(final String externalId, final int quantity) {
            this.externalId = externalId;
            this.quantity = quantity;
        }

        /**
         * @return the label used to group a set of Ionic cryptography keys
         */
        public final String getExternalId() {
            return externalId;
        }

        /**
         * @return the number of keys requested by this key request
         */
        public final int getQuantity() {
            return quantity;
        }

        /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
        private static final long serialVersionUID = -3547675875933369430L;
    }
}
