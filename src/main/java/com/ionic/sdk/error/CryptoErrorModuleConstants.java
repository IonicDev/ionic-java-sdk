package com.ionic.sdk.error;

/**
 * Enumeration of error codes from the Ionic SDK Crypto module.
 */
@SuppressWarnings({"checkstyle:interfaceistype"})  // Java JNI SDK API compatibility
public interface CryptoErrorModuleConstants {

    /**
     * Success code.
     */
     int ISCRYPTO_OK = 0;

    /**
     * Crypto module error code range base.
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
     * Crypto module has not been initialized.
     * <p>
     * This error code is present in order to maintain compatibility with the C++ SDK.  It is unused
     * in this language implementation.
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
     * ISCRYPTO_SEED_OVERFLOW error code is returned.
     * <p>
     * When this error is encountered, it is recommended to simply try again.
     * <p>
     * This error code is present in order to maintain compatibility with the C++ SDK.  It is unused
     * in this language implementation.
     */
     int ISCRYPTO_SEED_OVERFLOW = 50009;

    /**
     * This is a reserved error code that was used in development at one point and then deprecated.
     */
     int ISCRYPTO_NOT_SUPPORTED = 50010;

    /**
     * This is a reserved error code that was used in development at one point and then deprecated.
     */
     int ISCRYPTO_FATAL_ERROR = 50011;

    /**
     * This error indicates the sufficient entropy test has failed on a Linux install. The test
     * reads the entropy stream and waits to see if it is quickly refilled. A default Linux install
     * will fail the test even if there is sufficient entropy initially available. The Ionic SDK
     * requires 'haveged' or some similar solution including mapping /dev/urandom into /dev/random.
     */
     int ISCRYPTO_LIMITED_ENTROPY = 50012;

    /**
     * A file failed to open, seek, or read/write.  This normally happens because the file path provided
     * does not exist or it is not accessible due to lack of permission.
     */
     int ISCRYPTO_FILE_ERROR = 50013;

    /**
     * Fatal error.
     * A fatal error has occurred and the module will no longer be usable.
     * This will happen if any POST test, healthcheck test, or any other operational
     * test fails at any time.  Such failure of any test indicates the possibility of
     * the module being compromised, and so it becomes disabled.
     */
     int ISCRYPTO_FATAL = 50100;
}
