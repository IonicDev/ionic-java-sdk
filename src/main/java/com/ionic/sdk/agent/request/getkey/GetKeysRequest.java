package com.ionic.sdk.agent.request.getkey;

import com.ionic.sdk.agent.request.base.AgentRequestBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the input for an Agent.getKeys() request.
 */
public class GetKeysRequest extends AgentRequestBase {

    /**
     * Represents a list of Key objects for an Agent.getKeys() request.
     */
    private final List<String> keyIds;

    /**
     * Constructor.
     */
    public GetKeysRequest() {
        this.keyIds = new ArrayList<String>();
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
}
