package com.ionic.sdk.error;

import com.ionic.sdk.core.value.Value;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A helper class providing utility functions related to Ionic SDK exceptions.
 */
public final class SdkError {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private SdkError() {
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
            return Value.join(Value.DELIMITER_SLASH, e.getClass().getSimpleName(), errorString);
        }
    }

    /**
     * Container for error messages associated with Ionic SDK error codes, loaded on first use.
     */
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com.ionic.sdk.error.SdkError");

    /**
     * Pattern for lookup of the SDK error message associated with a particular error code.
     */
    private static final String PATTERN_ERROR_STRING = "sdkerror.%d.message";
}
