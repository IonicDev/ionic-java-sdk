package com.ionic.sdk.core.vm;

/**
 * Encapsulation of logic to derive the version of the JVM from the system property set.
 * <p>
 * There are no known Ionic SDK incompatibilities (compile or runtime) with any version &gt;= JRE 7.
 */
public enum JavaVersion {

    /**
     * The version advertised by the JRE is not recognized by the Ionic SDK.
     */
    VERSION_UNRECOGNIZED,

    /**
     * JRE version 7 (released July 2011).
     */
    VERSION_1_7,

    /**
     * JRE version 8 (released March 2014).
     */
    VERSION_1_8,

    /**
     * JRE version 9 (released September 2017).
     */
    VERSION_1_9,

    /**
     * JRE version 10 (released March 2018).
     */
    VERSION_1_10,

    /**
     * JRE version 11 (released September 2018).
     */
    VERSION_1_11,

    /**
     * JRE version 12 (released March 2019).
     */
    VERSION_1_12,

    /**
     * Future JRE versions.
     */
    VERSION_FUTURE;

    /**
     * @return the enum corresponding to the currently detected JRE version
     */
    public static JavaVersion current() {
        final String javaVersion = System.getProperty(VM.Sys.JAVA_VERSION);
        if (javaVersion == null) {
            throw new IllegalStateException(new NullPointerException(VM.Sys.JAVA_VERSION));
        } else if (javaVersion.startsWith(PREFIX_1_7)) {
            return VERSION_1_7;
        } else if (javaVersion.startsWith(PREFIX_1_8)) {
            return VERSION_1_8;
        } else if (javaVersion.startsWith(PREFIX_1_9)) {
            return VERSION_1_9;
        } else if (javaVersion.startsWith(PREFIX_1_10)) {
            return VERSION_1_10;
        } else if (javaVersion.startsWith(PREFIX_1_11)) {
            return VERSION_1_11;
        } else if (javaVersion.startsWith(PREFIX_1_12)) {
            return VERSION_1_12;
        } else {
            return VERSION_FUTURE;
        }
    }

    /**
     * The prefix of the value of the in-use JRE which corresponds to version 7 (released July 2011).
     */
    private static final String PREFIX_1_7 = "1.7.";

    /**
     * The prefix of the value of the in-use JRE which corresponds to version 8 (released March 2014).
     */
    private static final String PREFIX_1_8 = "1.8.";

    /**
     * The prefix of the value of the in-use JRE which corresponds to version 9 (released September 2017).
     */
    private static final String PREFIX_1_9 = "9.0.";

    /**
     * The prefix of the value of the in-use JRE which corresponds to version 10 (released March 2018).
     */
    private static final String PREFIX_1_10 = "10.0.";

    /**
     * The prefix of the value of the in-use JRE which corresponds to version 11 (released September 2018).
     */
    private static final String PREFIX_1_11 = "11.0.";

    /**
     * The prefix of the value of the in-use JRE which corresponds to version 12 (released March 2019).
     */
    private static final String PREFIX_1_12 = "12.0.";
}
