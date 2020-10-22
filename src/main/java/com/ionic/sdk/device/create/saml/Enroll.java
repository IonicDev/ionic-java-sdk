package com.ionic.sdk.device.create.saml;

/**
 * Constant definitions used during Machina device enrollment transactions.
 */
public final class Enroll {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Enroll() {
    }

    /**
     * Text names associated with http headers used in device enrollment.
     */
    public static final class Header {

        /** Checkstyle / FinalClass. */
        private Header() {
        }

        /**
         * Http header name used in device enrollment.
         */
        public static final String REG_ENROLL_TAG = "X-Ionic-Reg-Enrollment-Tag";

        /**
         * Http header name used in device enrollment.
         */
        public static final String REG_IONIC_URL = "X-Ionic-Reg-Ionic-Url";

        /**
         * Http header name used in device enrollment.
         */
        public static final String REG_PUBKEY_URL = "X-Ionic-Reg-Pubkey-Url";

        /**
         * Http header name used in device enrollment.
         */
        public static final String REG_STOKEN = "X-Ionic-Reg-Stoken";

        /**
         * Http header name used in device enrollment.
         */
        public static final String REG_UIDAUTH = "X-Ionic-Reg-Uidauth";

        /**
         * Http header name used in device enrollment.
         */
        public static final String SAML_REDIRECT = "X-Saml-Redirect";

        /**
         * Http header name used in device enrollment.
         */
        public static final String SAML_RELAY_STATE = "X-Saml-Relay-State";

        /**
         * Http header name used in device enrollment.
         */
        public static final String SAML_REQUEST = "X-Saml-Request";

        /**
         * Http header name used in device enrollment.
         */
        public static final String SAML_RESPONSE = "X-Saml-Response";
    }

    /**
     * Text strings associated with http payloads used in device enrollment.
     */
    public static final class Payload {

        /** Checkstyle / FinalClass. */
        private Payload() {
        }

        /**
         * Text string associated with http payload used in device enrollment.
         */
        public static final String SAML_REQUEST = "user=%s&password=%s&SAMLRequest=%s";

        /**
         * Text string associated with http payload used in device enrollment.
         */
        public static final String SAML_RESPONSE = "SAMLResponse=%s";
    }
}
