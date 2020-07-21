package com.ionic.sdk.agent.request.logmessage;

import com.ionic.sdk.agent.request.base.AgentRequestBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents the input for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#logMessages(LogMessagesRequest)} API call.
 * <p>
 * The request will contain information about {@link Message} objects, which allow client information to be posted
 * to the Machina infrastructure for later analysis.
 */
public class LogMessagesRequest extends AgentRequestBase {

    /**
     * The collection of messages accumulated in this request.
     */
    private final Collection<Message> messages;

    /**
     * Constructor.
     */
    public LogMessagesRequest() {
        this.messages = new ArrayList<Message>();
    }

    /**
     * @return the collection of <code>Message</code> accumulated in this request.
     */
    public final Collection<Message> getMessages() {
        return messages;
    }

    /**
     * Add a message object to the {@link LogMessagesRequest}.
     *
     * @param message the object containing the parameters of the message
     */
    public final void add(final Message message) {
        messages.add(message);
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = -163833622243208551L;

    /**
     * Class used to store an individual log message for a <code>LogMessagesRequest</code>.
     * <p>
     * The message must meet the following requirements:
     * <ol>
     * <li>the "type" field is optional; it may be null</li>
     * <li>the "data" field is required; it must be non-null, and it must be valid JSON</li>
     * <li>the "data" field root object must contain the following four fields:
     * <ul>
     * <li>"@type" ["type": "string"] must begin with "ionic.com/types"</li>
     * <li>"datetime" ["type": "string"] must conform to ISO 8601 GMT format (e.g. "2000-01-01T00:00:00Z")</li>
     * <li>"application" ["type": "object"] may be empty</li>
     * <li>"messages" ["type": "array"] must contain [1..n] JsonObject, must not be empty</li>
     * </ul>
     * </ol>
     */
    public static class Message implements Serializable {

        /**
         * The type of message being sent.
         */
        private final String type;

        /**
         * The data associated with the message being sent.
         */
        private final String data;

        /**
         * Constructor.
         *
         * @param type the type of message
         * @param data the data associated with the message
         */
        public Message(final String type, final String data) {
            this.type = type;
            this.data = data;
        }

        /**
         * @return the type of message being sent
         */
        public final String getType() {
            return type;
        }

        /**
         * @return the data associated with the message being sent
         */
        public final String getData() {
            return data;
        }

        /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
        private static final long serialVersionUID = 3900758834643627218L;
    }
}
