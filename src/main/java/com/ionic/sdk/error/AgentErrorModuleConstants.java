package com.ionic.sdk.error;

/**
 * Enumeration of error codes from the Ionic SDK Agent module.
 */
@SuppressWarnings({"checkstyle:interfaceistype"})  // Java JNI SDK API compatibility
public interface AgentErrorModuleConstants {

    /**
     * Success code.
     */
    int ISAGENT_OK = 0;

    /**
     * Agent module error code range base.
     */
    int ISAGENT_ERROR_BASE = 40000;

    /**
     * A general error occurred, but its specific problem is not represented with
     * its own code.
     */
    int ISAGENT_ERROR = 40001;

    /**
     * An unknown and unexpected error occurred.
     */
    int ISAGENT_UNKNOWN = 40002;

    /**
     * A memory allocation failed. This can happen if there is not a sufficient
     * amount of memory available to perform an operation.
     */
    int ISAGENT_NOMEMORY = 40003;

    /**
     * An expected and required value was not found. This is typically emitted from
     * functions that are responsible for parsing / deserializing data.
     */
    int ISAGENT_MISSINGVALUE = 40004;

    /**
     * A value was found that is invalid. For example, a string value was expected,
     * but it was actually an integer. This is typically emitted from functions that
     * are responsible for parsing / deserializing data.
     */
    int ISAGENT_INVALIDVALUE = 40005;

    /**
     * The agent was used before being initialized. Make sure that you call
     * {@link com.ionic.sdk.agent.Agent#initialize()}, {@link com.ionic.sdk.agent.Agent#initializeWithoutProfiles()},
     * or another
     * initialization function before trying to use the Agent object.
     */
    int ISAGENT_NOINIT = 40006;

    /**
     * Agent initialization was performed twice. This will happen if you try calling
     * one of the agent initialization functions after it has already been
     * initialized.
     */
    int ISAGENT_DOUBLEINIT = 40007;

    /**
     * Fingerprint creation failed.
     */
    int ISAGENT_CREATEFINGERPRINT = 40008;

    /**
     * A network request failed. A request failed due to connection failure, unknown
     * server, unresponsive server, or other network problem.
     */
    int ISAGENT_REQUESTFAILED = 40009;

    /**
     * The parsing of some serialized data failed. This typically happens if a file
     * or block of data is corrupted or of an unexpected format.
     */
    int ISAGENT_PARSEFAILED = 40010;

    /**
     * A server replied with a response that the agent was not expecting. This can
     * happen if the server rejects a request, it doesn't support the API version
     * used by the agent, etc.
     */
    int ISAGENT_UNEXPECTEDRESPONSE = 40011;

    /**
     * The agent configuration is invalid. This could be because the HTTP
     * implementation is not recognized, the server hostname/IP is empty, etc.
     */
    int ISAGENT_BADCONFIG = 40012;

    /**
     * A file failed to open. This normally happens because the file path provided
     * does not exist or it is not accessible due to lack of permission.
     */
    int ISAGENT_OPENFILE = 40013;

    /**
     * The request object is invalid. The request object failed validation in the
     * agent code. This can happen if any of the request data is not acceptable
     * (e.g. empty key ID, required properties missing).
     */
    int ISAGENT_BADREQUEST = 40014;

    /**
     * The response object is invalid. The response object failed validation in the
     * agent code. This means that the server responded with an invalid /
     * unacceptable response.
     */
    int ISAGENT_BADRESPONSE = 40015;

    /**
     * The server redirected the agent to an invalid location (e.g.&nbsp;an empty URL).
     * This is typically indicative that there is a problem on the server side.
     */
    int ISAGENT_BADREDIRECT = 40016;

    /**
     * The maximum amount of allowed server redirects has been hit. The server
     * redirected us too many times. The maximum number of allowed redirects can be
     * set during Agent initialization via {@link com.ionic.sdk.agent.config.AgentConfig#setMaxRedirects(int)}.
     */
    int ISAGENT_TOOMANYREDIRECTS = 40017;

    /**
     * The function (or a function it depends on) is not implemented.
     */
    int ISAGENT_NOTIMPLEMENTED = 40018;

    /**
     * The function (or a function it depends on) is not allowed to be called. This
     * typically happens if the function would knowingly cause a problem if it were
     * to be run.
     */
    int ISAGENT_NOTALLOWED = 40019;

    /**
     * An operation has timed out. This happens when a blocking function has been
     * used with a maximum wait time, and that time has expired.
     */
    int ISAGENT_TIMEOUT = 40020;

    /**
     * A null pointer was passed to a function that does not accept null pointers.
     */
    int ISAGENT_NULL_INPUT = 40021;

    /**
     * An active device profile has not been set. This happens when a function is
     * called that requires an active device profile, but there is no profile set yet
     * (e.g. {@link com.ionic.sdk.agent.Agent#createKeys(com.ionic.sdk.agent.request.createkey.CreateKeysRequest)}).
     */
    int ISAGENT_NO_DEVICE_PROFILE = 40022;

    /**
     * A resource was not found. This happens when attempting to access a resource
     * that does not exist.
     */
    int ISAGENT_RESOURCE_NOT_FOUND = 40023;

    /**
     * A request to create or fetch a key was denied by the server.
     */
    int ISAGENT_KEY_DENIED = 40024;

    /**
     * A request was denied because there was a fingerprint hash mismatch.
     */
    int ISAGENT_FPHASH_DENIED = 40025;

    /**
     * A request was denied because the full fingerprint was denied.
     */
    int ISAGENT_FPFULL_DENIED = 40026;

    /**
     * A request was denied because the conversation ID (CID) timestamp was denied.
     */
    int ISAGENT_CID_TIMESTAMP_DENIED = 40027;

    /**
     * No default profile persistor is available for the current platform. A profile
     * persistor must be specified.
     */
    int ISAGENT_NO_PROFILE_PERSISTOR = 40028;

    /**
     * Failed to load device profiles. This may happen if an incorrect password was
     * provided, the storage file is corrupt, or some other problem occurred.
     */
    int ISAGENT_LOAD_PROFILES_FAILED = 40029;

    /**
     * A key is invalid in some way (key ID, key bytes, etc). For example, if the key is the wrong size (any
     * size other than 32 bytes), the key ID string is empty or contains invalid
     * characters, etc.
     */
    int ISAGENT_INVALID_KEY = 40030;

    /**
     * Key update failed because key attribute data is stale.
     * <p>
     * This happens when a key update request is performed via updateKeys() or updateKey()
     * and the key server denies the request because it detected that the client has a stale version of the key.
     * A stale key is one which has been updated by another client without the knowledge of the client who requested
     * a separate update.
     * <p>
     * This may happen, for example, if two or more different clients attempt to update the same key at a very similar
     * time. In this case, there is a race condition, and the first client request to be processed works successfully,
     * but the other clients will have their requests denied.
     * <p>
     * To remedy this error, the client must retrieve the key again from the key server (via getKey() or
     * variants), apply the desired changes to it (via implementation of KeyAttributesMapMerger such as
     * KeyAttributesMapMergerDefault), and perform another update request via updateKeys() or updateKey().
     */
    int ISAGENT_STALE_KEY_ATTRIBUTES = 40031;

    /**
     * A keys update request contains two or more entries with the same key id.
     * <p>
     * This is a user error.
     */
    int ISAGENT_DUPLICATE_KEY = 40032;

    /**
     * A header was not found in the specified data.
     * <p>
     * This may happen if the data version is older and special handling for this error may be necessary to
     * support previous SDK versions existing on the system.
     */
    int ISAGENT_HEADER_NOT_FOUND = 40033;

    /**
     * An HTTPS secure connection was not achieved.
     * <p>
     * This commonly happens when the device OS is either setup without TLS 1.x, or is intentionally
     * set to use a weaker security standard. Ionic servers require at least TLS 1.0.
     */
    int ISAGENT_NO_SECURE_CONNECTION = 40034;

    /**
     * Agent module (Java) error code range base.
     */
    int ISAGENT_ERROR_BASE_JAVA = 42000;

    /**
     * SDK initialization failed.  This can be due to the use of an out-of-date JRE that does not support
     * 256 bit AES keys (reference JDK-8170157 for information to correct this issue).
     */
    int ISAGENT_INIT_FAILED_KEY_SIZE = 42001;

    /**
     * SDK initialization failed.  This can be due to the use of a JRE that does not support needed cryptography
     * primitives.
     */
    int ISAGENT_INIT_FAILED_PLATFORM = 42002;
}
