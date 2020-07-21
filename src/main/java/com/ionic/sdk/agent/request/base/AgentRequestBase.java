package com.ionic.sdk.agent.request.base;

import com.ionic.sdk.agent.data.MetadataHolder;

import java.io.Serializable;

/**
 * The base class for Ionic Machina Tools service requests.   These encapsulate an https request made to the
 * Machina server infrastructure.
 * <p>
 * Request metadata may be sent to the server along with the service request.  This may specify information
 * about the client making the request.  See <a href='https://dev.ionic.com/sdk/tasks/set-request-metadata'
 * target='_blank'>Set
 * Request Metadata</a> for more information.
 */
public class AgentRequestBase extends MetadataHolder implements Serializable {

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = 6081520288189577693L;
}
