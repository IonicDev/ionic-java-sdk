package com.ionic.sdk.agent.request.logmessage;

import com.ionic.sdk.agent.request.base.AgentResponseBase;

/**
 * Represents the output for an Agent.logMessages() request.
 */
public class LogMessagesResponse extends AgentResponseBase {

    /**
     * @return whether server response payload requires a "data" component
     */
    @Override
    protected final boolean isDataRequired() {
        return false;
    }
}
