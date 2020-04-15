package com.ionic.sdk.core.vm;

/**
 * Encapsulate logic to evaluate version of running JRE.
 * <p>
 * <a href='https://en.wikipedia.org/wiki/Java_version_history' target='_blank'>Java version history</a>
 */
public final class Version {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Version() {
    }

    /**
     * Evaluate whether running JRE is version 7 (released July 2011).
     *
     * @return true iff JRE is version 7
     */
    public static boolean isJava7() {
        return System.getProperty(VM.Sys.JAVA_VERSION).contains(VERSION_1_7);
    }

    /**
     * Evaluate whether running JRE is version 8 (released March 2014).
     *
     * @return true iff JRE is version 8
     */
    public static boolean isJava8() {
        return System.getProperty(VM.Sys.JAVA_VERSION).contains(VERSION_1_8);
    }

    /**
     * Evaluate whether running JRE is version 9 (released September 2017).
     *
     * @return true iff JRE is version 9
     */
    public static boolean isJava9() {
        return System.getProperty(VM.Sys.JAVA_VERSION).contains(VERSION_1_9);
    }

    /**
     * The prefix of the value of the in-use JRE which corresponds to version 7 (released July 2011).
     */
    private static final String VERSION_1_7 = "1.7.";

    /**
     * The prefix of the value of the in-use JRE which corresponds to version 8 (released March 2014).
     */
    private static final String VERSION_1_8 = "1.8.";

    /**
     * The prefix of the value of the in-use JRE which corresponds to version 9 (released September 2017).
     */
    private static final String VERSION_1_9 = "1.9.";
}
