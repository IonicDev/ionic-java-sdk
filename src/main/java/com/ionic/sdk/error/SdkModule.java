package com.ionic.sdk.error;

/**
 * Defines the modules that encompass the Ionic SDK.  These are derived from the base C++ implementation, and so
 * include modules that are yet to be implemented in Java.
 */
public enum SdkModule {

    /**
     * Default value when module cannot be identified.
     */
    MODULE_UNKNOWN,

    /**
     * The module including the default implementation of the KeyServices interface.
     */
    MODULE_ISAGENT,

    /**
     * The module including the Ionic cryptography implementation.
     */
    MODULE_ISCRYPTO,

    /**
     * The module including the Ionic file management implementation.
     */
    MODULE_ISFILECRYPTO,

    /**
     * The module including the Ionic device fingerprint implementation.
     */
    MODULE_ISFINGERPRINT,

    /**
     * The module including the Ionic chunk cryptography implementation, which manages cryptography operations
     * against strings and small byte arrays.
     */
    MODULE_ISCHUNKCRYPTO,

    /**
     * The module including XML management operations.
     */
    MODULE_ISXML,

    /**
     * The module including ZIP management operations (compression / inflation of byte arrays).
     */
    MODULE_ISZIP,

    /**
     * The module including HTTP operations (used to communicate with "ionic.com").
     */
    MODULE_ISHTTP,

    /**
     * The module including IPC operations (unimplemented).
     */
    MODULE_ISIPC,

    /**
     * The module including Ionic key vault operations (unimplemented).
     */
    MODULE_ISKEYVAULT;

    /**
     * Identify the module which produced a given Ionic error code.
     *
     * @param errorCode the error code produced during use of the Ionic SDK
     * @return the module corresponding to the input error code
     * @see SdkError
     */
    public static SdkModule getErrorCodeModule(final int errorCode) {
        SdkModule module = MODULE_UNKNOWN;
        if (isInRange(errorCode, ChunkCryptoErrorModuleConstants.ISCHUNKCRYPTO_ERROR_BASE, BLOCK_SIZE)) {
            module = MODULE_ISCHUNKCRYPTO;
        } else if (isInRange(errorCode, AgentErrorModuleConstants.ISAGENT_ERROR_BASE, BLOCK_SIZE)) {
            module = MODULE_ISAGENT;
        } else if (isInRange(errorCode, CryptoErrorModuleConstants.ISCRYPTO_ERROR_BASE, BLOCK_SIZE)) {
            module = MODULE_ISCRYPTO;
        } else if (isInRange(errorCode, FileCryptoErrorModuleConstants.ISFILECRYPTO_ERROR_BASE, BLOCK_SIZE)) {
            module = MODULE_ISFILECRYPTO;
        }
        return module;
    }

    /**
     * Evaluate whether a given error code value falls within the specified numeric range.
     *
     * @param value     the Ionic error code
     * @param errorBase the base error code for the module
     * @param size      the size of the error code block
     * @return true, iff the value falls within the specified block
     */
    private static boolean isInRange(final int value, final int errorBase, final int size) {
        return ((value >= errorBase) && (value < (errorBase + size)));
    }

    /**
     * The size of each Ionic error code block.
     */
    private static final int BLOCK_SIZE = 10000;
}
