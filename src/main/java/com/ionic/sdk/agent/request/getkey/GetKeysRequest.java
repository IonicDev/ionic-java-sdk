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
