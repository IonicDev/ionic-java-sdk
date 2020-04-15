package com.ionic.sdk.error;

/**
 * Enumeration that represents error codes from the SDK FileCrypto module.
 */
@SuppressWarnings({"checkstyle:interfaceistype"})  // Java JNI SDK API compatibility
public interface FileCryptoErrorModuleConstants {
    /**
     * Success code.
     */
    int ISFILECRYPTO_OK = 0;

    /**
     * FileCrypto module error code range base.
     */
    int ISFILECRYPTO_ERROR_BASE = 80000;

    /**
     * A general error occurred, but its specific problem is not represented with its own code.
     */
    int ISFILECRYPTO_ERROR = 80001;

    /**
     * An unknown and unexpected error occurred.
     */
    int ISFILECRYPTO_UNKNOWN = 80002;

    /**
     * A memory allocation failed.
     * This can happen if there is not a sufficient amount of memory available to perform an operation.
     */
    int ISFILECRYPTO_NOMEMORY = 80003;

    /**
     * An expected and required value was not found.
     * This is typically emitted from functions that are responsible for parsing / deserializing data.
     */
    int ISFILECRYPTO_MISSINGVALUE = 80004;

    /**
     * A value was found that is invalid.
     * For example, a string value was expected, but it was actually an integer.  This is typically
     * emitted from functions that are responsible for parsing / deserializing data.
     */
    int ISFILECRYPTO_INVALIDVALUE = 80005;

    /**
     * A null pointer was passed to a function that does not accept null pointers.
     */
    int ISFILECRYPTO_NULL_INPUT = 80006;

    /**
     * An invalid input value was encountered.
     * An input value was found that is invalid.  For example, a buffer length
     * input was equal to zero.
     */
    int ISFILECRYPTO_BAD_INPUT = 80007;

    /**
     * A file failed to open.
     * This normally happens because the file path provided does not exist or it is
     * not accessible due to lack of permission.
     */
    int ISFILECRYPTO_OPENFILE = 80008;

    /**
     * The end of a file was found before it was expected.
     * This normally happens if the file has been truncated or is zero length.
     */
    int ISFILECRYPTO_EOF = 80009;

    /**
     * A file header could not be found where it was expected.
     * This normally happens when trying to decrypt a file that is not encrypted, or the encrypted file has
     * been corrupted.
     */
    int ISFILECRYPTO_NOHEADER = 80010;

    /**
     * The parsing of some serialized data failed.
     * This typically happens if a file or block of data is corrupted or of an unexpected format.
     */
    int ISFILECRYPTO_PARSEFAILED = 80011;

    /**
     * The file version is unsupported or unrecognized.
     */
    int ISFILECRYPTO_VERSION_UNSUPPORTED = 80012;

    /**
     * A hash digest verification failed.
     * The computed digest did not match the expected digest.
     */
    int ISFILECRYPTO_HASH_VERIFICATION = 80013;

    /**
     * A failure occurred while writing to a stream.
     * An error flag of some sort was set on the stream when it was being written to.
     */
    int ISFILECRYPTO_STREAM_WRITE = 80014;

    /**
     * A resource was not found.
     * This happens when attempting to access a resource that does not exist.
     */
    int ISFILECRYPTO_RESOURCE_NOT_FOUND = 80015;

    /**
     * A zip file failed to open because it was not formatted correctly.
     * This normally happens because the zip file is corrupted in some way (for example, truncated).
     */
    int ISFILECRYPTO_BAD_ZIP = 80016;

    /**
     * A file is not supported for Ionic protection.
     */
    int ISFILECRYPTO_UNRECOGNIZED = 80017;

    /**
     * A file was requested to be encrypted, but it is already encrypted.
     */
    int ISFILECRYPTO_ALREADY_ENCRYPTED = 80018;

    /**
     * A file was requested to be decrypted, but it is not encrypted.
     */
    int ISFILECRYPTO_NOT_ENCRYPTED = 80019;

    /**
     * A file failed to be renamed.
     * <p>
     * This normally happens because a temporary file was attempted to be renamed in order
     * to overwrite an input file during in-place encryption or decryption, but the rename attempt failed.
     * <p>
     * A system level error code is emitted to the logger in this case.
     * <p>
     * The file may be open by another process or by another thread in the current process, you may
     * not have permissions to write to the file, or some other error occurred.
     */
    int ISFILECRYPTO_RENAMEFILE = 80020;

    /**
     * A file did not contain an Ionic embed stream.
     */
    int ISFILECRYPTO_NOEMBED = 80021;

    /**
     * A file type was specified that does not have a cover page.
     */
    int ISFILECRYPTO_NO_COVERPAGE = 80022;

    /**
     * An error occurred in the Streams lib.
     */
    int ISFILECRYPTO_IOSTREAM_ERROR = 80023;
}
