package com.ionic.sdk.agent;

/**
 * Defines the version of the Ionic SDK.
 */
public final class SdkVersion {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private SdkVersion() {
    }

    /**
     * Generate the Ionic agent string, which identifies the SDK language and version in communications with ionic.com.
     *
     * @return a string identifying the SDK language and version
     */
    public static String getAgentString() {
        return String.format(IONIC_AGENT, getVersionStringInternal());
    }

    /**
     * Ionic agent string pattern.
     */
    private static final String IONIC_AGENT = "IonicSDK/2 Java/%s";

    /**
     * @return the version string of the Ionic SDK, loaded from the SDK jar manifest through the standard interface
     */
    public static String getVersionString() {
        return getVersionStringInternal();
    }

    /**
     * @return the version string of the Ionic SDK, loaded from the SDK jar manifest through the standard interface
     */
    private static String getVersionStringInternal() {
        final String version = SdkVersion.class.getPackage().getImplementationVersion();
        return (version == null) ? VERSION : version;
    }

    /**
     * The built in Ionic SDK version used as a fallback when the version cannot be loaded from the jar manifest.
     */
    private static final String VERSION = "2.2.1+000000";
}
