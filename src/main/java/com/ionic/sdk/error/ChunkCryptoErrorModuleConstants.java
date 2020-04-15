package com.ionic.sdk.error;

/**
 * Enumeration of error codes from the Ionic SDK ChunkCrypto module.
 */
@SuppressWarnings({"checkstyle:interfaceistype"})  // Java JNI SDK API compatibility
public interface ChunkCryptoErrorModuleConstants {

    /**
     * Success code.
     */
     int ISCHUNKCRYPTO_OK = 0;

    /**
     * ChunkCrypto module error code range base.
     */
     int ISCHUNKCRYPTO_ERROR_BASE = 20000;

    /**
     * A general error occurred, but its specific problem is not represented with its own code.
     */
     int ISCHUNKCRYPTO_ERROR = 20001;

    /**
     * An unknown and unexpected error occurred.
     */
     int ISCHUNKCRYPTO_UNKNOWN = 20002;

    /**
     * A memory allocation failed.
     * This can happen if there is not a sufficient amount of memory available to perform an operation.
     */
     int ISCHUNKCRYPTO_NOMEMORY = 20003;

    /**
     * An expected and required value was not found.
     * This is typically emitted from functions that are responsible for parsing / deserializing data.
     */
     int ISCHUNKCRYPTO_MISSINGVALUE = 20004;

    /**
     * A value was found that is invalid.
     * For example, a string value was expected, but it was actually an integer.  This is typically
     *     emitted from functions that are responsible for parsing / deserializing data.
     */
     int ISCHUNKCRYPTO_INVALIDVALUE = 20005;

    /**
     * A null pointer was passed to a function that does not accept null pointers.
     */
     int ISCHUNKCRYPTO_NULL_INPUT = 20006;

    /**
     * An invalid input value was encountered.
     * An input value was found that is invalid.  For example, a buffer length
     *     input was equal to zero.
     */
     int ISCHUNKCRYPTO_BAD_INPUT = 20007;

    /**
     * The end of a data chunk was found before it was expected.
     * This normally happens if the data chunk has been truncated or is zero length.
     */
     int ISCHUNKCRYPTO_EOF = 20009;

    /**
     * The parsing of some serialized data failed.
     * This typically happens if a file or block of data is corrupted or of an unexpected format.
     */
     int ISCHUNKCRYPTO_PARSEFAILED = 20011;

    /**
     * A hash digest verification failed.
     * The computed digest did not match the expected digest.
     */
     int ISCHUNKCRYPTO_HASH_VERIFICATION = 20013;

    /**
     * A failure occurred while writing to a stream.
     * An error flag of some sort was set on the stream when it was being written to.
     */
     int ISCHUNKCRYPTO_STREAM_WRITE = 20014;

    /**
     * A resource was not found.
     * This happens when attempting to access a resource that does not exist.
     */
     int ISCHUNKCRYPTO_RESOURCE_NOT_FOUND = 20015;

    /**
     * A data chunk is not supported for Ionic protection.
     */
     int ISCHUNKCRYPTO_UNRECOGNIZED = 20017;

    /**
     * A data chunk was requested to be encrypted, but it is already encrypted.
     */
     int ISCHUNKCRYPTO_ALREADY_ENCRYPTED = 20018;

    /**
     * A data chunk was requested to be decrypted, but it is not encrypted.
     */
     int ISCHUNKCRYPTO_NOT_ENCRYPTED = 20019;
}
