package com.ionic.sdk.error;

import com.ionic.sdk.agent.SdkVersion;
import com.ionic.sdk.core.value.Value;

/**
 * An Exception class for representing Ionic SDK-specific exceptions.
 * <p>
 * {@link SdkException} has been deprecated in favor of this class.
 *
 * @see com.ionic.sdk.error.SdkError
 */
@SuppressWarnings("deprecation")
public class IonicException extends SdkException {

    /**
     * Get the SDK error code.
     *
     * @return the SDK error code
     * @see com.ionic.sdk.error.SdkError
     */
    // In Ionic SDK 2.1, this needs to be final, as IonicException is a leaf class.  When SdkException is removed,
    // IonicServerException will derive from IonicException, and this method will not be final.
    @Override
    public final int getReturnCode() {
        return super.getReturnCode();
    }

    /**
     * Returns the detail message string of this exception.
     *
     * @return the detail message string of this exception instance.
     * @see com.ionic.sdk.error.SdkError
     */
    @Override
    public final String getMessage() {
        return Value.join(SPACER, super.getReturnCode(), super.getMessage(),
                getConversationIdInternal(), SdkVersion.getVersionString());
    }

    /**
     * @return conversation id associated with failed server communication
     */
    public String getConversationId() {
        return getConversationIdInternal();
    }

    /**
     * @return conversation id associated with failed server communication
     */
    private String getConversationIdInternal() {
        final Throwable cause = getCause();
        final IonicServerException serverException =
                (cause instanceof IonicServerException) ? ((IonicServerException) cause) : null;
        return (serverException == null ? null : serverException.getConversationId());
    }

    /**
     * String formatting spacer.
     */
    private static final String SPACER = " - ";

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param errorCode the SDK error code
     * @see com.ionic.sdk.error.SdkError
     */
    public IonicException(final int errorCode) {
        super(errorCode);
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param errorCode the SDK error code
     * @param cause     the underlying cause of this exception, if any
     * @see com.ionic.sdk.error.SdkError
     */
    public IonicException(final int errorCode, final Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param errorCode the SDK error code
     * @param message   the text description of the error
     * @see com.ionic.sdk.error.SdkError
     */
    public IonicException(final int errorCode, final String message) {
        super(errorCode, Value.join(SPACER, SdkError.getErrorString(errorCode), message));
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param cause the underlying cause of this exception, if any
     */
    public IonicException(final Throwable cause) {
        super(cause);
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param message the text description of the error
     * @param cause   the underlying cause of this exception, if any
     */
    public IonicException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param errorCode the SDK error code
     * @param message   the text description of the error
     * @param cause     the underlying cause of this exception, if any
     * @see com.ionic.sdk.error.SdkError
     */
    public IonicException(final int errorCode, final String message, final Throwable cause) {
        super(errorCode, Value.join(SPACER, SdkError.getErrorString(errorCode), message), cause);
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.7.0". */
    private static final long serialVersionUID = -7188300090018888838L;
}
