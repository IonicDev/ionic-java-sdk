package com.ionic.sdk.error;

/**
 * Enumeration of error codes from the Ionic SDK Crypto module.
 */
@SuppressWarnings({"checkstyle:interfaceistype"})
public interface CryptoErrorModuleConstants {

    /**
     * Success code.
     */
     int ISCRYPTO_OK = 0;

    /**
     * Crypto module range base.
     */
     int ISCRYPTO_ERROR_BASE = 50000;

    /**
     * A general error occurred, but its specific problem is not represented with its own code.
     */
     int ISCRYPTO_ERROR = 50001;

    /**
     * An unknown and unexpected error occurred.
     */
     int ISCRYPTO_UNKNOWN = 50002;

    /**
     * A null pointer was passed to one of the crypto functions.
     */
     int ISCRYPTO_NULL_INPUT = 50003;

    /**
     * An invalid input value was encountered.
     * An input value was found that is invalid.  For example, a buffer length
     * input was equal to zero.
     */
     int ISCRYPTO_BAD_INPUT = 50004;

    /**
     * CryptoAbstract module has not been initialized.
     * The crypto module was used before being initialized via cryptoImpl_initialize().
     */
     int ISCRYPTO_NO_INIT = 50005;

    /**
     * Memory allocation error.
     * This can happen if there is not a sufficient amount of memory available
     * to perform an operation.
     */
     int ISCRYPTO_NOMEMORY = 50006;

    /**
     * Key validation error.
     * A key was loaded or generated that did not pass validation.
     * For example, this may happen when loading or generating an RSA private key.
     */
     int ISCRYPTO_KEY_VALIDATION = 50007;

    /**
     * Message signature verification error.
     * A message signature verification failed.  This can be returned by RSA signature
     * verification functions.
     */
     int ISCRYPTO_BAD_SIGNATURE = 50008;

    /**
     * A seed overflow occurred during RSA private key generation.
     * During RSA private key generation, it is possible for the random seed to overflow
     * (although extremely unlikely).  In that situation, RSA key generation is aborted and the
     * ISCRYPTO_RSA_SEED_OVERFLOW error code is returned.
     * <p>
     * When this error is encountered, it is recommended to simply try again.
     */
     int ISCRYPTO_SEED_OVERFLOW = 50009;

    /**
     * Not supported.
     * The attempted operation is not supported.  It may have been deprecated or not applicable for a variation
     * of the interface.
     */
     int ISCRYPTO_NOT_SUPPORTED = 50010;

    /**
     * Fatal error.
     * A fatal error has occurred and the module will no longer be usable.
     * This will happen if any POST test, healthcheck test, or any other operational
     * test fails at any time.  Such failure of any test indicates the possibility of
     * the module being compromised, and so it becomes disabled.
     */
     int ISCRYPTO_FATAL = 50100;
}
