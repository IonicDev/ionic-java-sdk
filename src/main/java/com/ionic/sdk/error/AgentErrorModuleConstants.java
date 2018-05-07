package com.ionic.sdk.error;

/**
 *
 * Enum AgentError.
 *
 * @author Ionic Security
 *
 */
public enum AgentErrorModuleConstants {

    /**
     * Success code.
     */
    ISAGENT_OK(0),

    /**
     * error code range base.
     */
    ISAGENT_ERROR_BASE(40000),

    /**
     * A general error occurred, but its specific problem is not represented with
     * its own code.
     */
    ISAGENT_ERROR(ISAGENT_ERROR_BASE.value + 1),

    /**
     * An unknown and unexpected error occurred.
     */
    ISAGENT_UNKNOWN(ISAGENT_ERROR.value + 1),

    /**
     * A memory allocation failed. This can happen if there is not a sufficient
     * amount of memory available to perform an operation.
     */
    ISAGENT_NOMEMORY(ISAGENT_UNKNOWN.value + 1),

    /**
     * An expected and required value was not found. This is typically emitted from
     * functions that are responsible for parsing / deserializing data.
     */
    ISAGENT_MISSINGVALUE(ISAGENT_NOMEMORY.value + 1),

    /**
     * A value was found that is invalid. For example, a string value was expected,
     * but it was actually an integer. This is typically emitted from functions that
     * are responsible for parsing / deserializing data.
     */
    ISAGENT_INVALIDVALUE(ISAGENT_MISSINGVALUE.value + 1),

    /**
     * The agent was used before being initialized. Make sure that you call
     * ISAgent::initialize, ISAgent::initializeWithoutProfiles, or another
     * initialization function before trying to use the ISAgent object.
     */
    ISAGENT_NOINIT(ISAGENT_INVALIDVALUE.value + 1),

    /**
     * Agent initialization was performed twice. This will happen if you try calling
     * one of the agent initialization functions after it has already been
     * initialized.
     */
    ISAGENT_DOUBLEINIT(ISAGENT_NOINIT.value + 1),

    /**
     * Fingerprint creation failed.
     */
    ISAGENT_CREATEFINGERPRINT(ISAGENT_DOUBLEINIT.value + 1),

    /**
     * A network request failed. A request failed due to connection failure, unknown
     * server, unresponsive server, or other network problem.
     */
    ISAGENT_REQUESTFAILED(ISAGENT_CREATEFINGERPRINT.value + 1),

    /**
     * The parsing of some serialized data failed. This typically happens if a file
     * or block of data is corrupted or of an unexpected format.
     */
    ISAGENT_PARSEFAILED(ISAGENT_REQUESTFAILED.value + 1),

    /**
     * A server replied with a response that the agent was not expecting. This can
     * happen if the server rejects a request, it doesn't support the API version
     * used by the agent, etc.
     */
    ISAGENT_UNEXPECTEDRESPONSE(ISAGENT_PARSEFAILED.value + 1),

    /**
     * The agent configuration is invalid. This could be because the HTTP
     * implementation is not recognized, the server hostname/IP is empty, etc.
     */
    ISAGENT_BADCONFIG(ISAGENT_UNEXPECTEDRESPONSE.value + 1),

    /**
     * A file failed to open. This normally happens because the file path provided
     * does not exist or it is not accessible due to lack of permission.
     */
    ISAGENT_OPENFILE(ISAGENT_BADCONFIG.value + 1),

    /**
     * The request object is invalid. The request object failed validation in the
     * agent code. This can happen if any of the request data is not acceptable
     * (e.g. empty key ID, required properties missing).
     */
    ISAGENT_BADREQUEST(ISAGENT_OPENFILE.value + 1),

    /**
     * The response object is invalid. The response object failed validation in the
     * agent code. This means that the server responded with an invalid /
     * unacceptable response.
     */
    ISAGENT_BADRESPONSE(ISAGENT_BADREQUEST.value + 1),

    /**
     * The server redirected the agent to an invalid location (e.g. an empty URL)
     * This is typically indicative that there is a problem on the server side.
     */
    ISAGENT_BADREDIRECT(ISAGENT_BADRESPONSE.value + 1),

    /**
     * The maximum amount of allowed server redirects has been hit. The server
     * redirected us too many times. The maximum number of allowed redirects can be
     * set during ISAgent initialization via ISAgentConfig::setMaxRedirects().
     */
    ISAGENT_TOOMANYREDIRECTS(ISAGENT_BADREDIRECT.value + 1),

    /**
     * The function (or a function it depends on) is not implemented.
     */
    ISAGENT_NOTIMPLEMENTED(ISAGENT_TOOMANYREDIRECTS.value + 1),

    /**
     * The function (or a function it depends on) is not allowed to be called This
     * typically happens if the function would knowingly cause a problem if it were
     * to be run. For example, an async task (ISAgentAsyncTask) is not allowed to
     * wait for the async processor to stop (ISAgentAsyncProcessor::waitForStop())
     * because this would cause a deadlock condition.
     */
    ISAGENT_NOTALLOWED(ISAGENT_NOTIMPLEMENTED.value + 1),

    /**
     * An operation has timed out. This happens when a blocking function has been
     * used with a maximum wait time, and that time has expired.
     */
    ISAGENT_TIMEOUT(ISAGENT_NOTALLOWED.value + 1),

    /**
     * A null pointer was passed to a function that does not accept null pointers.
     */
    ISAGENT_NULL_INPUT(ISAGENT_TIMEOUT.value + 1),

    /**
     * An active device profile has not been set. This happens when a function is
     * called that requires an active device profile, but there is no profile set
     * yet (e.g. ISAgent::createKeys()).
     */
    ISAGENT_NO_DEVICE_PROFILE(ISAGENT_NULL_INPUT.value + 1),

    /**
     * A resource was not found. This happens when attempting to access a resource
     * that does not exist.
     */
    ISAGENT_RESOURCE_NOT_FOUND(ISAGENT_NO_DEVICE_PROFILE.value + 1),

    /**
     * A request to create or fetch a key was denied by the server.
     */
    ISAGENT_KEY_DENIED(ISAGENT_RESOURCE_NOT_FOUND.value + 1),

    /**
     * A request was denied because there was a fingerprint hash mismatch.
     */
    ISAGENT_FPHASH_DENIED(ISAGENT_KEY_DENIED.value + 1),

    /**
     * A request was denied because the full fingerprint was denied.
     */
    ISAGENT_FPFULL_DENIED(ISAGENT_FPHASH_DENIED.value + 1),

    /**
     * A request was denied because the conversation ID (CID) timestamp was denied.
     */
    ISAGENT_CID_TIMESTAMP_DENIED(ISAGENT_FPFULL_DENIED.value + 1),

    /**
     * No default profile persistor is available for the current platform. A profile
     * persistor must be specified.
     */
    ISAGENT_NO_PROFILE_PERSISTOR(ISAGENT_CID_TIMESTAMP_DENIED.value + 1),

    /**
     * Failed to load device profiles. This may happen if an incorrect password was
     * provided, the storage file is corrupt, or some other problem occurred.
     */
    ISAGENT_LOAD_PROFILES_FAILED(ISAGENT_NO_PROFILE_PERSISTOR.value + 1),

    /**
     * A key is invalid in some way (key ID, key bytes, etc). This may happen if a
     * key was found to be invalid. For example, if the key is the wrong size (any
     * size other than 32 bytes), the key ID string is empty or contains invalid
     * characters, etc.
     */
    ISAGENT_INVALID_KEY(ISAGENT_LOAD_PROFILES_FAILED.value + 1),

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
    ISAGENT_STALE_KEY_ATTRIBUTES(ISAGENT_INVALID_KEY.value + 1),

    /**
     * error code range base.
     */
    ISAGENT_ERROR_BASE_JAVA(42000),

    /**
     * SDK initialization failed.  This can be due to the use of an out-of-date JRE that does not support
     * 256 bit AES keys (reference JDK-8170157 for information to correct this issue).
     */
    ISAGENT_INIT_FAILED_KEY_SIZE(ISAGENT_ERROR_BASE_JAVA.value + 1),

    /**
     * SDK initialization failed.  This can be due to the use of a JRE that does not support needed cryptography
     * primitives.  The bouncycastle library (1.56+) may be added to the JRE security providers to correct this issue.
     */
    ISAGENT_INIT_FAILED_PLATFORM(ISAGENT_INIT_FAILED_KEY_SIZE.value + 1);

    /**
     * the error code value.
     */
    private final int value;

    /**
     * the error message.
     */
    private final String message;

    /**
     * Agent Error constructor.
     *
     * @param value
     *            of the error code.
     */
    AgentErrorModuleConstants(final int value) {
        this.value = value;
        this.message = SdkError.getErrorString(value);
    }

    /**
     * Getter for the error code.
     *
     * @return returns the error code
     */
    public int value() {
        return value;
    }

    /**
     * Getter for the error message.
     *
     * @return returns the error message
     */
    public String message() {
        return message;
    }
}
