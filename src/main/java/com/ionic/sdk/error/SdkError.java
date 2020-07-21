package com.ionic.sdk.error;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A helper class providing utility functions related to Ionic SDK exceptions.
 */
public final class SdkError implements
        ChunkCryptoErrorModuleConstants,
        AgentErrorModuleConstants,
        CryptoErrorModuleConstants,
        FileCryptoErrorModuleConstants,
        KeyVaultErrorModuleConstants {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private SdkError() {
    }

    /**
     * Gets an {@link com.ionic.sdk.error.SdkModule} enum for any SDK error code emitted by any Ionic SDK module.
     *
     * @param errorCode The error code returned from any Ionic SDK function.
     * @return The enumerator for the module, or {@link SdkModule#MODULE_UNKNOWN} for success or errors out of range.
     */
    public static SdkModule getErrorModule(final int errorCode) {
        return SdkModule.getErrorCodeModule(errorCode);
    }

    /**
     * Provide a textual description of the parameter error code.
     *
     * @param errorCode an Ionic SDK error code
     * @return the text description of the error code
     */
    public static String getErrorString(final int errorCode) {
        final String errorString = String.format(PATTERN_ERROR_STRING, errorCode);
        try {
            return BUNDLE.getString(errorString);
        } catch (MissingResourceException e) {
            return String.format(PATTERN_ERROR_STRING_DEFAULT, errorCode);
        }
    }

    /**
     * Container for error messages associated with Ionic SDK error codes.  {@link ResourceBundle} is loaded
     * on first use.
     */
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com.ionic.sdk.error.SdkError");

    /**
     * Pattern for lookup of the SDK error message associated with a particular error code.
     */
    private static final String PATTERN_ERROR_STRING = "sdkerror.%d.message";

    /**
     * Pattern for error message when resource lookup fails.
     */
    private static final String PATTERN_ERROR_STRING_DEFAULT = "Unknown / unrecognized error code (%d)";
}
