package com.ionic.sdk.error;

/**
 * An Exception class for representing Ionic SDK-specific exceptions.
 *
 * @deprecated Please migrate usages to the drop-in replacement class {@link IonicException}.
 */
@Deprecated
public class SdkException extends Exception {

    /**
     * The code from the SDK error.
     */
    private final int errorCode;

    /**
     * Get the SDK error code.
     *
     * @return the SDK error code
     */
    @SuppressWarnings({"checkstyle:designforextension"})
    public int getReturnCode() {
        return errorCode;
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param errorCode the SDK error code
     */
    public SdkException(final int errorCode) {
        super(SdkError.getErrorString(errorCode));
        this.errorCode = errorCode;
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param errorCode the SDK error code
     * @param cause     the underlying cause of this exception, if any
     */
    public SdkException(final int errorCode, final Throwable cause) {
        super(SdkError.getErrorString(errorCode), cause);
        this.errorCode = errorCode;
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param errorCode the SDK error code
     * @param message   the text description of the error
     */
    public SdkException(final int errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param cause the underlying cause of this exception, if any
     */
    public SdkException(final Throwable cause) {
        super(cause);
        this.errorCode = -1;
    }

    /**
     * Initializes the exception with an SDK error code.
     *
     * @param message the text description of the error
     * @param cause   the underlying cause of this exception, if any
     */
    public SdkException(final String message, final Throwable cause) {
        super(message, cause);
        this.errorCode = -1;
    }
}
