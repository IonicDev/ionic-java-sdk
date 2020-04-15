package com.ionic.sdk.agent.request.getkey;

import com.ionic.sdk.agent.request.base.AgentRequestBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the input for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#getKeys(GetKeysRequest)} API call.
 * <p>
 * The request may contain a set of 1..n key ids.  Each key id uniquely identifies an cryptography key stored by the
 * Machina infrastructure.
 * <p>
 * The request may also contain a set of 1..n external ids.  Each external id identifies a set of cryptography keys
 * stored by the Machina infrastructure.
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
    private final List<String> externalIds;

    /**
     * Constructor.
     */
    public GetKeysRequest() {
        this.keyIds = new ArrayList<String>();
        this.externalIds = new ArrayList<String>();
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
     * @return a list of Key objects for an Agent.getKeys() request.
     */
    public final List<String> getExternalIds() {
        return externalIds;
    }

    /**
     * Add an external key request object to the {@link GetKeysRequest}.
     *
     * @param externalId the object containing the parameters of the key request
     */
    public final void addExternalId(final String externalId) {
        externalIds.add(externalId);
    }
}
