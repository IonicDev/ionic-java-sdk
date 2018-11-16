package com.ionic.sdk.agent.request.logmessage;

import com.ionic.sdk.agent.request.base.AgentRequestBase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents the input for an Agent.logMessages() request.
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
    public static class Message {

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
    }
}
