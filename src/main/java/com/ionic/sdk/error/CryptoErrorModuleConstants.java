package com.ionic.sdk.error;

/**
 * Enumeration that represents error codes from the SDK Crypto module.
 * <p>
 * checkstyle - interfaces should describe a type and hence have methods.
 */
public enum CryptoErrorModuleConstants {

    /**
     * Success code.
     */
    ISCRYPTO_OK(0),

    /**
     * A general error occurred, but its specific problem is not represented with its own code.
     */
    ISCRYPTO_ERROR(50001),

    /**
     * An unknown and unexpected error occurred.
     */
    ISCRYPTO_UNKNOWN(50002),

    /**
     * A null pointer was passed to one of the crypto functions.
     */
    ISCRYPTO_NULL_INPUT(50003),

    /**
     * An invalid input value was encountered.
     * An input value was found that is invalid.  For example, a buffer length
     * input was equal to zero.
     */
    ISCRYPTO_BAD_INPUT(50004),

    /**
     * CryptoAbstract module has not been initialized.
     * The crypto module was used before being initialized via cryptoImpl_initialize().
     */
    ISCRYPTO_NO_INIT(50005),

    /**
     * Memory allocation error.
     * This can happen if there is not a sufficient amount of memory available
     * to perform an operation.
     */
    ISCRYPTO_NOMEMORY(50006),

    /**
     * Key validation error.
     * A key was loaded or generated that did not pass validation.
     * For example, this may happen when loading or generating an RSA private key.
     */
    ISCRYPTO_KEY_VALIDATION(50007),

    /**
     * Message signature verification error.
     * A message signature verification failed.  This can be returned by RSA signature
     * verification functions.
     */
    ISCRYPTO_BAD_SIGNATURE(50008),

    /**
     * A seed overflow occurred during RSA private key generation.
     * During RSA private key generation, it is possible for the random seed to overflow
     * (although extremely unlikely).  In that situation, RSA key generation is aborted and the
     * ISCRYPTO_RSA_SEED_OVERFLOW error code is returned.
     * <p>
     * When this error is encountered, it is recommended to simply try again.
     */
    ISCRYPTO_SEED_OVERFLOW(50009),

    /**
     * Not supported.
     * The attempted operation is not supported.  It may have been deprecated or not applicable for a variation
     * of the interface.
     */
    ISCRYPTO_NOT_SUPPORTED(50010),

    /**
     * Fatal error.
     * A fatal error has occurred and the module will no longer be usable.
     * This will happen if any POST test, healthcheck test, or any other operational
     * test fails at any time.  Such failure of any test indicates the possibility of
     * the module being compromised, and so it becomes disabled.
     */
    ISCRYPTO_FATAL(50100);

    /**
     * The error code associated with this error type.
     */
    private final int value;

    /**
     * The error message associated with this error type.
     */
    private final String message;

    /**
     * Instantiate an enum of this type.
     *
     * @param value the error code associated with this error type
     */
    CryptoErrorModuleConstants(final int value) {
        this.value = value;
        this.message = SdkError.getErrorString(value);
    }

    /**
     * @return the error code associated with this error type
     */
    public int value() {
        return value;
    }

    /**
     * @return the error message associated with this error type
     */
    public String message() {
        return message;
    }
}
