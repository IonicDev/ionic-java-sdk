package com.ionic.sdk.error;

/**
 * Enumeration that represents error codes from the SDK ChunkCrypto module.
 */
public enum ChunkCryptoErrorModuleConstants {

    /**
     * Success code.
     */
    ISCHUNKCRYPTO_OK(0),

    /**
     * A general error occurred, but its specific problem is not represented with its own code.
     */
    ISCHUNKCRYPTO_ERROR_BASE(20000),

    /**
     * A general error occurred, but its specific problem is not represented with its own code.
     */
    ISCHUNKCRYPTO_ERROR(ISCHUNKCRYPTO_ERROR_BASE.value + 1),

    /**
     * An unknown and unexpected error occurred.
     */
    ISCHUNKCRYPTO_UNKNOWN(ISCHUNKCRYPTO_ERROR.value + 1),

    /**
     * A memory allocation failed.
     * This can happen if there is not a sufficient amount of memory available to perform an operation.
     */
    ISCHUNKCRYPTO_NOMEMORY(ISCHUNKCRYPTO_UNKNOWN.value + 1),

    /**
     * An expected and required value was not found.
     * This is typically emitted from functions that are responsible for parsing / deserializing data.
     */
    ISCHUNKCRYPTO_MISSINGVALUE(ISCHUNKCRYPTO_NOMEMORY.value + 1),

    /**
     * A value was found that is invalid.
     * For example, a string value was expected, but it was actually an integer.  This is typically
     * emitted from functions that are responsible for parsing / deserializing data.
     */
    ISCHUNKCRYPTO_INVALIDVALUE(ISCHUNKCRYPTO_MISSINGVALUE.value + 1),

    /**
     * A null pointer was passed to a function that does not accept null pointers.
     */
    ISCHUNKCRYPTO_NULL_INPUT(ISCHUNKCRYPTO_INVALIDVALUE.value + 1),

    /**
     * An invalid input value was encountered.
     * An input value was found that is invalid.  For example, a buffer length
     * input was equal to zero.
     */
    ISCHUNKCRYPTO_BAD_INPUT(ISCHUNKCRYPTO_NULL_INPUT.value + 1),

    /**
     * The end of a data chunk was found before it was expected.
     * This normally happens if the data chunk has been truncated or is zero length.
     */
    ISCHUNKCRYPTO_EOF(ISCHUNKCRYPTO_BAD_INPUT.value + 1),

    /**
     * The parsing of some serialized data failed.
     * This typically happens if a file or block of data is corrupted or of an unexpected format.
     */
    ISCHUNKCRYPTO_PARSEFAILED(ISCHUNKCRYPTO_EOF.value + 1),

    /**
     * A hash digest verification failed.
     * The computed digest did not match the expected digest.
     */
    ISCHUNKCRYPTO_HASH_VERIFICATION(ISCHUNKCRYPTO_PARSEFAILED.value + 1),

    /**
     * A failure occurred while writing to a stream.
     * An error flag of some sort was set on the stream when it was being written to.
     */
    ISCHUNKCRYPTO_STREAM_WRITE(ISCHUNKCRYPTO_HASH_VERIFICATION.value + 1),

    /**
     * A resource was not found.
     * This happens when attempting to access a resource that does not exist.
     */
    ISCHUNKCRYPTO_RESOURCE_NOT_FOUND(ISCHUNKCRYPTO_STREAM_WRITE.value + 1),

    /**
     * A data chunk is not supported for Ionic protection.
     */
    ISCHUNKCRYPTO_UNRECOGNIZED(ISCHUNKCRYPTO_RESOURCE_NOT_FOUND.value + 1),

    /**
     * A data chunk was requested to be encrypted, but it is already encrypted.
     */
    ISCHUNKCRYPTO_ALREADY_ENCRYPTED(ISCHUNKCRYPTO_UNRECOGNIZED.value + 1),

    /**
     * A data chunk was requested to be decrypted, but it is not encrypted.
     */
    ISCHUNKCRYPTO_NOT_ENCRYPTED(ISCHUNKCRYPTO_ALREADY_ENCRYPTED.value + 1);

    /**
     * the error code value.
     */
    private final int value;

    /**
     * the error message.
     */
    private final String message;

    /**
     * Chunk Crypto Error constructor.
     *
     * @param value of the error code.
     */
    ChunkCryptoErrorModuleConstants(final int value) {
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
