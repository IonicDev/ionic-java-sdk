package com.ionic.sdk.error;

/**
 * An Exception class for representing Ionic SDK-specific exceptions.
 * <p>
 * {@link SdkException} has been deprecated in favor of this class.
 */
@SuppressWarnings("deprecation")
public class IonicException extends SdkException {

    /**
     * Get the SDK error code.
     *
     * @return the SDK error code
     */
    // In Ionic SDK 2.1, this needs to be final, as IonicException is a leaf class.  When SdkException is removed,
    // IonicServerException will derive from IonicException, and this method will not be final.
    public final int getReturnCode() {
        return super.getReturnCode();
    }

    /**
     * Returns the detail message string of this exception.
     *
     * @return the detail message string of this exception instance.
     */
    @Override
    public final String getMessage() {
        return Integer.toString(super.getReturnCode()) + SPACER + super.getMessage();
    }

    /**
     * String formatting spacer.
     */
    private static final String SPACER = " - ";

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param errorCode the SDK error code
     */
    public IonicException(final int errorCode) {
        super(errorCode);
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param errorCode the SDK error code
     * @param cause     the underlying cause of this exception, if any
     */
    public IonicException(final int errorCode, final Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param errorCode the SDK error code
     * @param message   the text description of the error
     */
    public IonicException(final int errorCode, final String message) {
        super(errorCode, message);
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
}
