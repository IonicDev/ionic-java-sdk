package com.ionic.sdk.core.date;

/**
 * Date related utility functions and declarations.
 * <p>
 * References to timestamps in the Ionic SDK are defined as representing POSIX time.  This is the number of
 * seconds (or milliseconds) that have elapsed since the Unix epoch, that is the time 00:00:00 UTC on 1 January 1970.
 * <p>
 * Timestamps are used in the Ionic SDK to track:
 * <ul>
 * <li>the system clock offset between SDK client machines and Ionic servers,</li>
 * <li>the enrollment time of SDK client devices,</li>
 * <li>the issuance time and expiration time of Ionic keys maintained in an SDK client key vault.</li>
 * </ul>
 * <p>
 * <a href="https://en.wikipedia.org/wiki/Unix_time" target="_blank">More Information</a>
 */
public final class DateTime {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private DateTime() {
    }

    /**
     * Milliseconds in one second.
     */
    public static final long ONE_SECOND_MILLIS = 1000L;
}
