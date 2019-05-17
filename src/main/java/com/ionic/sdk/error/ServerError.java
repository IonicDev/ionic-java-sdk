package com.ionic.sdk.error;

/**
 * A helper class providing definitions for server error codes encountered during ionic.com interactions.
 */
public final class ServerError {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private ServerError() {
    }

    /**
     * Server has accepted request.
     */
    public static final int SERVER_OK = 0;

    /**
     * Current mattr signature (msig) does not match prevmsig.
     */
    public static final int MATTR_SIGNATURE_MISMATCH = 409;

    /**
     * Error processing request.
     */
    public static final int PROCESSING_ERROR = 4000;

    /**
     * Server has rejected request due to mismatched fingerprint hash.
     */
    public static final int HFPHASH_DENIED = 4001;

    /**
     * Server has rejected request due to out of range timestamp (embedded in request conversation ID).
     */
    public static final int CID_TIMESTAMP_DENIED = 4002;

    /**
     * Server has rejected request due to internal error.
     */
    public static final int INTERNAL_ERROR = 4009;

    /**
     * Server has rejected request due to invalid resource name.
     */
    public static final int KEY_INVALID_RESOURCE_NAME = 4106;

    /**
     * Server has rejected request due to invalid resource arguments.
     */
    public static final int KEY_INVALID_RESOURCE_ARGS = 4107;

    /**
     * Server has rejected key request atom; "Attribute cannot be defined as both fixed and mutable".
     */
    public static final int KEY_INVALID_CATTR_MATTR = 4108;

    /**
     * Server has rejected update key request atom due to attribute signature failure.
     */
    public static final int POLICY_STALE_ATTRIBUTES = 4202;

    /**
     * Illegal attempt to modify a fixed attribute for an existing key.
     */
    public static final int KEY_MODIFY_FIXED_ATTRIBUTE = 4203;
}
