package com.ionic.sdk.core.date;

import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility methods for converting a <code>Date</code> object to / from an ISO 8601 string.
 */
public final class DateTime8601 {

    /**
     * Parse a ISO 8601 date string as a date.
     *
     * @param date the formatted string input
     * @return the corresponding date
     * @throws IonicException on invalid input
     */
    public static Date fromString(final String date) throws IonicException {
        return new DateTime8601().fromStringInner(date);
    }

    /**
     * Encode a date as an ISO 8601 date string.
     *
     * @param date the date to represent
     * @return the formatted string
     */
    public static String toString(final Date date) {
        return new DateTime8601().toStringInner(date);
    }

    /**
     * The formatter to use when converting <code>Date</code> objects to / from <code>String</code> objects.
     */
    private final DateFormat dateFormat;

    /**
     * Constructor.
     */
    private DateTime8601() {
        this.dateFormat = new SimpleDateFormat(ISO_8601_MILLI_Z, Locale.getDefault());
        dateFormat.setTimeZone(TZ_GMT);
    }

    /**
     * Parse a ISO 8601 date string as a date.
     *
     * @param date the formatted string input
     * @return the corresponding date
     * @throws IonicException on invalid input
     */
    private Date fromStringInner(final String date) throws IonicException {
        try {
            final String dateNormalized = date.replaceFirst(PRESERVE_MILLIS_FROM, PRESERVE_MILLIS_TO);
            return dateFormat.parse(dateNormalized);
        } catch (ParseException e) {
            throw new IonicException(SdkError.ISAGENT_INVALIDVALUE, e);
        }
    }

    /**
     * Encode a date as an ISO 8601 date string.
     *
     * @param date the date to represent
     * @return the formatted string
     */
    private String toStringInner(final Date date) {
        return dateFormat.format(date);
    }

    /**
     * The format string used to represent a GMT date as a string.
     */
    private static final String ISO_8601_MILLI_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Pattern used to normalize ISO 8601 date to a maximum of three digits of millisecond precision.
     */
    private static final String PRESERVE_MILLIS_FROM = "(\\.\\d{3})\\d*";

    /**
     * Pattern used to normalize ISO 8601 date to a maximum of three digits of millisecond precision.
     */
    private static final String PRESERVE_MILLIS_TO = "$1";

    /**
     * The format string used to represent a GMT date as a string.
     */
    private static final String TZ_ID_GMT = "GMT";

    /**
     * The timezone ID corresponding to GMT.
     */
    private static final TimeZone TZ_GMT = TimeZone.getTimeZone(TZ_ID_GMT);
}
