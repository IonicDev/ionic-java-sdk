package com.ionic.sdk.agent.request.logmessage;

import com.ionic.sdk.agent.ServiceProtocol;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.MessageBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonTarget;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * Encapsulation of helper logic associated with LogMessages SDK API.  Includes state associated with request, and
 * conversion of request object into json representation, for submission to IDC.
 */
public class LogMessagesMessage extends MessageBase {

    /**
     * Constructor.
     *
     * @param protocol the protocol used by the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @throws IonicException on random number generation failure
     */
    LogMessagesMessage(final ServiceProtocol protocol) throws IonicException {
        super(protocol);
    }

    /**
     * Assemble the "data" json associated with the request.
     *
     * @param requestBase the user-generated object containing the attributes of the request
     * @return a {@link JsonValue} to be incorporated into the request payload
     */
    @Override
    protected final JsonValue getJsonData(final AgentRequestBase requestBase) throws IonicException {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        SdkData.checkTrue(requestBase instanceof LogMessagesRequest, SdkError.ISAGENT_ERROR);
        final LogMessagesRequest logMessagesRequest = (LogMessagesRequest) requestBase;
        for (LogMessagesRequest.Message message : logMessagesRequest.getMessages()) {
            JsonTarget.addNotNull(arrayBuilder, getJsonDataOne(message));
        }
        return arrayBuilder.build();
    }

    /**
     * Assemble the json associated with a single message contained in the request.  The data portion of
     * the message is required to be valid json.
     *
     * @param message the user object supplied in the request
     * @return the json representation of the user object, to be included in the request payload
     * @throws IonicException on invalid json specified in the message
     */
    private JsonObject getJsonDataOne(final LogMessagesRequest.Message message) throws IonicException {
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        SdkData.checkTrue((message.getData() != null), SdkError.ISAGENT_MISSINGVALUE,
                SdkError.getErrorString(SdkError.ISAGENT_MISSINGVALUE));
        JsonTarget.addNotNull(objectBuilder, IDC.Payload.DATA,
                JsonIO.readObject(message.getData(), SdkError.ISAGENT_PARSEFAILED));
        JsonTarget.addNotNull(objectBuilder, IDC.Payload.MESSAGE_TYPE, message.getType());
        return objectBuilder.build();
    }
}
