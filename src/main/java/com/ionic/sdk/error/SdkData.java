package com.ionic.sdk.error;

/**
 * Utility functions for validating preconditions within the Ionic SDK code.
 */
public final class SdkData {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private SdkData() {
    }

    /**
     * Validate input value.  Value must not be null.
     *
     * @param value   the value to check
     * @param message the message associated with check failure
     * @throws IonicException on null value
     */
    public static void checkNotNull(final Object value, final String message) throws IonicException {
        if (value == null) {
            final int error = SdkError.ISAGENT_NULL_INPUT;
            throw new IonicException(error, message);
        }
    }

    /**
     * Validate input value.  Value must not be null.
     *
     * @param value   the value to check
     * @param message the message associated with check failure
     * @throws NullPointerException on null value
     */
    public static void checkNotNullNPE(final Object value, final String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
    }

    /**
     * Validate input value.  Value must be true.
     *
     * @param value   the value to check
     * @param error   the error code associated with check failure
     * @throws IonicException on false value
     */
    public static void checkTrue(final boolean value, final int error) throws IonicException {
        if (!value) {
            throw new IonicException(error);
        }
    }

    /**
     * Validate input value.  Value must be true.
     *
     * @param value   the value to check
     * @param error   the error code associated with check failure
     * @param message the message associated with check failure
     * @throws IonicException on false value
     */
    public static void checkTrue(final boolean value, final int error, final String message) throws IonicException {
        if (!value) {
            throw new IonicException(error, message);
        }
    }
}
