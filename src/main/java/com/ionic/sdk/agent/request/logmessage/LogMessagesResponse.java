package com.ionic.sdk.agent.request.logmessage;

import com.ionic.sdk.agent.request.base.AgentResponseBase;

/**
 * Represents the output for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#logMessages(LogMessagesRequest)} API call.
 * <p>
 * As this operation does not produce output, the only information available in the response is that of the
 * base class.
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
