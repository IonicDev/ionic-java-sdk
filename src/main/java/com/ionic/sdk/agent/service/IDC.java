package com.ionic.sdk.agent.service;

/**
 * Constant definitions used in communications with ionic.com.
 */
public final class IDC {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private IDC() {
    }

    /**
     * Constants related to the assembly of HTTP resource portion of request.
     */
    public static final class Resource {

        /** Checkstyle / FinalClass. */
        private Resource() {
        }

        /**
         * Server API version 2.2.
         */
        public static final String SERVER_API_V22 = "v2.2";

        /**
         * Server API version 2.3.
         */
        public static final String SERVER_API_V23 = "v2.3";

        /**
         * Server API version to be included in request URLs.
         */
        public static final String SERVER_API_V24 = "v2.4";

        /**
         * Resource for CreateAssertion requests.
         */
        public static final String ASSERTION_CREATE = "/assertions/create";

        /**
         * Resource for CreateKeys requests (substitute server api version).
         */
        public static final String KEYS_CREATE = "/%s/keys/create";

        /**
         * Resource for CreateKeys requests (prepend with context-specific path).
         */
        public static final String KEYS_CREATE_BASE = "/keys/create";

        /**
         * Resource for GetKeys requests (substitute server api version).
         */
        public static final String KEYS_GET = "/%s/keys/fetch";

        /**
         * Resource for CreateKeys requests (prepend with context-specific path).
         */
        public static final String KEYS_GET_BASE = "/keys/fetch";

        /**
         * Resource for UpdateKeys requests (substitute server api version).
         */
        public static final String KEYS_UPDATE = "/%s/keys/modify";

        /**
         * Resource for LogMessages requests (substitute server api version).
         */
        public static final String MESSAGES_PUT = "/%s/messages";

        /**
         * Resource for GetResources requests (substitute server api version).
         */
        public static final String RESOURCES_GET = "/%s/resources";

        /**
         * Resource for CreateDevice requests (substitute server api version, keyspace).
         */
        public static final String DEVICE_CREATE = "/%s/register/%s";

        /**
         * Resource for GetKeyspace requests (substitute server api version, keyspace).
         */
        public static final String KEYSPACE_GET = "/%s/kns/keyspaces/%s";

        /**
         * Resource for identity sources document requests (substitute keyspace).
         */
        public static final String IDENTITY_SOURCES = "/keyspace/%s/identity_sources";
    }

    /**
     * Text names associated with components of ionic.com request conversation IDs.
     */
    public static final class Message {

        /** Checkstyle / FinalClass. */
        private Message() {
        }

        /**
         * Server API version to be included in conversation IDs.
         */
        public static final String SERVER_API_CID = "2.4.0";

        /**
         * Token used when assembling a conversation ID.
         */
        public static final String CID = "CID";

        /**
         * Delimiter used when assembling a conversation ID.
         */
        public static final String DELIMITER = "|";
    }

    /**
     * Text names associated with identifiers in ionic.com request metadata.
     */
    public static final class Metadata {

        /** Checkstyle / FinalClass. */
        private Metadata() {
        }

        /**
         * Agent metadata attribute name.
         */
        public static final String CLIENT_TYPE = "ionic-client-type";

        /**
         * Agent metadata attribute name.
         */
        public static final String CLIENT_VERSION = "ionic-client-version";

        /**
         * Agent metadata attribute name.
         */
        public static final String APPLICATION_NAME = "ionic-application-name";

        /**
         * Agent metadata attribute name.
         */
        public static final String APPLICATION_VERSION = "ionic-application-version";

        /**
         * Request metadata attribute name.
         */
        public static final String IONIC_AGENT = "ionic-agent";

        /**
         * This string constant represents the key origin ID for keys that originate
         * from an Ionic key server. Outside of advanced use cases by an SDK consumer, this is
         * the only key origin string that will ever be used.
         *
         * @see com.ionic.sdk.agent.request.createkey.CreateKeysResponse.Key#getOrigin
         */
        public static final String KEYORIGIN_IONIC = "ionic-keyserver";

        /**
         * Request metadata attribute name.
         */
        public static final String USER_AGENT = "ionic-user-agent";

        /**
         * The default user agent string sent in communications with ionic.com.
         */
        public static final String USER_AGENT_DEFAULT = "Ionic Fusion Agent";

        /**
         * Request metadata attribute name.
         */
        public static final String OS_ARCH = "ionic-os-arch";

        /**
         * Request metadata attribute name.
         */
        public static final String OS_NAME = "ionic-os-name";

        /**
         * Request metadata attribute name.
         */
        public static final String OS_VERSION = "ionic-os-version";
    }

    /**
     * Text names associated with identifiers in ionic.com request / response payloads.
     */
    public static final class Payload {

        /** Checkstyle / FinalClass. */
        private Payload() {
        }

        /**
         * Request / response JSON attribute name.
         */
        public static final String ARGS = "args";

        /**
         * Request / response JSON attribute name.
         */
        public static final String ANSWERS = "answers";

        /**
         * Request / response JSON attribute name.
         */
        public static final String ASSERTION = "assertion";

        /**
         * Request / response JSON attribute name.
         */
        public static final String AUTH = "AUTH";

        /**
         * Request / response JSON attribute name.
         */
        public static final String CATTRS = "cattrs";

        /**
         * Request / response JSON attribute name.
         */
        public static final String CID = "cid";

        /**
         * Request / response JSON attribute name.
         */
        public static final String CODE = "code";

        /**
         * Request / response JSON attribute name.
         */
        public static final String CSIG = "csig";

        /**
         * Request / response JSON attribute name.
         */
        public static final String DATA = "data";

        /**
         * Request / response JSON attribute name.
         */
        public static final String DEVICE_ID = "deviceID";

        /**
         * Request / response JSON attribute name.
         */
        public static final String ENC_EK_NONCE = "encEKNonce";

        /**
         * Request / response JSON attribute name.
         */
        public static final String ENC_KAEK = "encKAEK";

        /**
         * Request / response JSON attribute name.
         */
        public static final String ENC_KARESP = "encKAResp";

        /**
         * Request / response JSON attribute name.
         */
        public static final String ENROLL = "enroll";

        /**
         * Request / response JSON attribute name.
         */
        public static final String ENVELOPE = "envelope";

        /**
         * Request / response JSON attribute name.
         */
        public static final String ERROR = "error";

        /**
         * Request / response JSON attribute name.
         */
        public static final String ERROR_MAP = "errorMap";

        /**
         * Request / response JSON attribute name.
         */
        public static final String FORCE = "force";

        /**
         * Request / response JSON attribute name.
         */
        public static final String FOR_URI = "forUri";

        /**
         * Request / response JSON attribute name.
         */
        public static final String FQDN = "fqdn";

        /**
         * Request / response JSON attribute name.
         */
        public static final String FULL = "full";

        /**
         * Request / response JSON attribute name.
         */
        public static final String HFP = "hfp";

        /**
         * Request / response JSON attribute name.
         */
        public static final String HFPHASH = "hfphash";

        /**
         * Request / response JSON attribute name.
         */
        public static final String ID = "id";

        /**
         * Request / response JSON attribute name.
         */
        public static final String IDS = "ids";

        /**
         * Request / response JSON attribute name.
         */
        public static final String IONIC_EXTERNAL_ID = "ionic-external-id";

        /**
         * Request / response JSON attribute name.
         */
        public static final String KEY = "key";

        /**
         * Request / response JSON attribute name.
         */
        public static final String KEYSPACE = "keyspace";

        /**
         * Request / response JSON attribute name.
         */
        public static final String MATTRS = "mattrs";

        /**
         * Request / response JSON attribute name.
         */
        public static final String MESSAGE = "message";

        /**
         * Request / response JSON attribute name.
         */
        public static final String MESSAGE_TYPE = "message_type";

        /**
         * Request / response JSON attribute name.
         */
        public static final String META = "meta";

        /**
         * Request / response JSON attribute name.
         */
        public static final String MSIG = "msig";

        /**
         * Request / response JSON attribute name.
         */
        public static final String OBLIGATIONS = "obligations";

        /**
         * Request / response JSON attribute name.
         */
        public static final String PREVCSIG = "prevcsig";

        /**
         * Request / response JSON attribute name.
         */
        public static final String PREVMSIG = "prevmsig";

        /**
         * Request / response JSON attribute name.
         */
        public static final String PROTECTION_KEYS = "protection-keys";

        /**
         * Request / response JSON attribute name.
         */
        public static final String PROTECTION_KEY_QUERIES = "protection-key-queries";

        /**
         * Request / response JSON attribute name.
         */
        public static final String PUBKEYDERB64 = "TKRespPubKDERB64";

        /**
         * Request / response JSON attribute name.
         */
        public static final String QTY = "qty";

        /**
         * Request / response JSON attribute name.
         */
        public static final String QUERY_RESULTS = "query-results";

        /**
         * Request / response JSON attribute name.
         */
        public static final String REF = "ref";

        /**
         * Request / response JSON attribute name.
         */
        public static final String REQUESTS = "requests";

        /**
         * Request / response JSON attribute name.
         */
        public static final String RESOURCE = "resource";

        /**
         * Request / response JSON attribute name.
         */
        public static final String RESPONSES = "responses";

        /**
         * Request / response JSON attribute name.
         */
        public static final String SEPAESK = "SEPAESK";

        /**
         * Request / response JSON attribute name.
         */
        public static final String SEPAESK_IDC = "SEPAESK-IDC";

        /**
         * Request / response JSON attribute name.
         */
        public static final String SIGS = "sigs";

        /**
         * Request / response JSON attribute name.
         */
        public static final String TENANT_ID = "tenantid";

        /**
         * Request / response JSON attribute name.
         */
        public static final String TTLSECONDS = "ttlSeconds";

        /**
         * Request / response JSON attribute name.
         */
        public static final String URL = "url";

        /**
         * Request / response JSON attribute name.
         */
        public static final String G = "g";

        /**
         * Request / response JSON attribute name.
         */
        public static final String K = "k";

        /**
         * Request / response JSON attribute name.
         */
        public static final String P = "p";

        /**
         * Request / response JSON attribute name.
         */
        public static final String S = "s";
    }

    /**
     * Labels associated with the content of Machina Identity Assertion message payloads.
     */
    public static final class IdentityAssertion {

        /** Checkstyle / FinalClass. */
        private IdentityAssertion() {
        }

        /**
         * Identity Assertion JSON attribute name.
         */
        public static final String ASSERTION_VERSION = "assnVersion";

        /**
         * Identity Assertion JSON attribute name.
         */
        public static final String ATTRIBUTES = "attributes";

        /**
         * Identity Assertion JSON attribute name.
         */
        public static final String DEVICE_ID = "deviceID";

        /**
         * Identity Assertion JSON attribute name.
         */
        public static final String ID = "id";

        /**
         * Identity Assertion JSON attribute name.
         */
        public static final String RECIPIENT = "recipient";

        /**
         * Identity Assertion JSON attribute name.
         */
        public static final String SIGNATURE_B64 = "signatureB64";

        /**
         * Identity Assertion JSON attribute name.
         */
        public static final String SIGNER = "signer";

        /**
         * Identity Assertion JSON attribute name.
         */
        public static final String VALID_AFTER = "validAfter";

        /**
         * Identity Assertion JSON attribute name.
         */
        public static final String VALID_UNTIL = "validUntil";
    }

    /**
     * Text names associated with identifiers used in signature generation.
     */
    public static final class Signature {

        /** Checkstyle / FinalClass. */
        private Signature() {
        }

        /**
         * Delimiter used when assembling a message signature.
         */
        public static final String DELIMITER = ":";

        /**
         * Delimiter used when assembling a message signature.
         */
        public static final String DELIMITER_COMMA = ",";

        /**
         * Flag to indicate mutable attributes.
         */
        public static final String MUTABLE = "m";

        /**
         * Flag to indicate force update.
         */
        public static final String FORCE = "force";
    }

    /**
     * Text names associated with identifiers used in Secret Share Key Persistor.
     */
    public static final class SSKP {

        /** Checkstyle / FinalClass. */
        private SSKP() {
        }

        /**
         * Secret Share Key Persistor JSON attribute name.
         */
        public static final String SALT = "salt";

        /**
         * Secret Share Key Persistor JSON attribute name.
         */
        public static final String SHARES = "shares";
    }

    /**
     * Text strings associated with key attribute identifiers for which attribute level protection should be applied.
     */
    public static final class Protect {

        /** Checkstyle / FinalClass. */
        private Protect() {
        }

        /**
         * Protected key attribute name prefix.
         */
        public static final String PREFIX = "ionic-protected-";

        /**
         * Protected key attribute name special case.
         */
        public static final String INTEGRITY_HASH = "ionic-integrity-hash";
    }
}
