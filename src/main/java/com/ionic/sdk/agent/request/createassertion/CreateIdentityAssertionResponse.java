package com.ionic.sdk.agent.request.createassertion;

import com.ionic.sdk.agent.request.base.AgentResponseBase;

/**
 * Represents the output for a request to the Ionic Machina Tools CreateAssertion API call.
 */
public class CreateIdentityAssertionResponse extends AgentResponseBase {

    /**
     * Output of this server operation.  This value is a base64 encoded JSON string, containing assertion data.
     */
    private String assertionBase64;

    /**
     * Constructor.
     */
    public CreateIdentityAssertionResponse() {
    }

    /**
     * Constructor.
     *
     * @param assertionBase64 the  encoded service operation result text
     */
    public CreateIdentityAssertionResponse(final String assertionBase64) {
        this.assertionBase64 = assertionBase64;
    }

    /**
     * @return the service operation result text
     */
    public String getAssertionBase64() {
        return assertionBase64;
    }

    /**
     * Set the service operation result text.
     *
     * @param assertionBase64 the text to set
     */
    public void setAssertionBase64(final String assertionBase64) {
        this.assertionBase64 = assertionBase64;
    }

    /** Value of serialVersionUID for this class. */
    private static final long serialVersionUID = 8790791301462667352L;
}
