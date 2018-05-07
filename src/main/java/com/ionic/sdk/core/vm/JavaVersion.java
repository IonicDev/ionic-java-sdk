package com.ionic.sdk.core.vm;

/**
 * Encapsulation of logic to derive the version of the JVM from the system property set.
 */
public enum JavaVersion {

    /**
     * JRE version 7 (released July 2011).
     */
    VERSION_1_7,

    /**
     * JRE version 8 (released March 2014).
     */
    VERSION_1_8,

    /**
     * JRE version 7 (released September 2017).
     */
    VERSION_1_9;

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
        } else {
            throw new IllegalStateException(javaVersion);
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
}
