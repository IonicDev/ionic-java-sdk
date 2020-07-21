package com.ionic.sdk.error;

/**
 * Enumeration of error codes from the Ionic SDK Agent module.
 */
@SuppressWarnings({"checkstyle:interfaceistype"})  // Java JNI SDK API compatibility
public interface KeyVaultErrorModuleConstants {

    /**
     * Success code.
     */
    int ISKEYVAULT_OK = 0;

    /**
     * KeyVault module error code range base.
     */
    int ISKEYVAULT_ERROR_BASE = 16000;

    /**
     * A general error occurred, but its specific problem is not represented with its own code.
     */
    int ISKEYVAULT_ERROR = 16001;

    /**
     * An unknown and unexpected error occurred.
     */
    int ISKEYVAULT_UNKNOWN = 16002;

    /**
     * A memory allocation failed.
     *
     * This can happen if there is not a sufficient amount of memory available to perform an operation.
     */
    int ISKEYVAULT_NOMEMORY = 16003;

    /**
     * An expected and required value was not found.
     *
     * This is typically emitted from functions that are responsible for parsing / deserializing data.
     */
    int ISKEYVAULT_MISSINGVALUE = 16004;

    /**
     * A value was found that is invalid.
     *
     * For example, a string value was expected, but it was actually an integer.  This is typically
     * emitted from functions that are responsible for parsing / deserializing data.
     */
    int ISKEYVAULT_INVALIDVALUE = 16005;

    /**
     * A key was not found.
     *
     * This happens when attempting to access a key that does not exist, for
     * example when trying to retrieve via {@link com.ionic.sdk.keyvault.KeyVaultInterface#getKey(String)}.
     */
    int ISKEYVAULT_KEY_NOT_FOUND = 16006;

    /**
     * A key update request was ignored.
     * This happens when attempting to update a key via
     * {@link com.ionic.sdk.keyvault.KeyVaultInterface#setKey(com.ionic.sdk.keyvault.KeyVaultKey)}, and the provided
     * key is not newer than the key which already exists in the vault. This is not an error, per se,
     * but it is informing the caller that the requested update is not needed, and as such is ignored.
     * The determination is made by comparing key issuance UTC time
     * {@link com.ionic.sdk.keyvault.KeyVaultKey#getIssuedServerTimeUtcSeconds()}.
     * @see com.ionic.sdk.core.date.DateTime
     */
    int ISKEYVAULT_KEY_UPDATE_IGNORED = 16007;

    /**
     * A file failed to open.
     *
     * This normally happens because the file path provided does not exist or it is
     * not accessible due to lack of permission.
     */
    int ISKEYVAULT_OPENFILE = 16008;

    /**
     * The end of a file was found before it was expected.
     *
     * This normally happens if the file has been truncated or is zero length.
     */
    int ISKEYVAULT_EOF = 16009;

    /**
     * A file header could not be found where it was expected.
     *
     * This normally happens when trying to decrypt a file that is not encrypted, or the encrypted file
     * has been corrupted.
     */
    int ISKEYVAULT_NOHEADER = 16010;

    /**
     * The parsing of some serialized data failed.
     *
     * This typically happens if a file or block of data is corrupted or of an unexpected format.
     */
    int ISKEYVAULT_PARSEFAILED = 16011;

    /**
     * A key vault file header has values which were not expected.
     *
     * This typically happens when a key vault attempts to open a file that was saved by
     * a different key vault type.  For example, if a Windows DPAPI key vault object attempts
     * to open a file that was saved by a different key vault type (e.g. Apple Keychain key vault).
     */
    int ISKEYVAULT_HEADER_MISMATCH = 16012;

    /**
     * A key vault load operation was skipped because it was not needed.
     *
     * This happens when a load operation is requested on a key vault, but the vault skipped
     * the operation because it determined that the underlying storage data has not changed
     * since the previous load operation.  A key vault may do this in order to optimize execution
     * time by avoiding costly loads from disk when possible.
     */
    int ISKEYVAULT_LOAD_NOT_NEEDED = 16013;

    /**
     * A key vault save operation could not create the required file path.
     *
     * This happens when a save operation is requested on a key vault, but the vault is
     * unable to create the necessary folder path to store the file. For example, if the
     * destination file path is /a/b/c/vault.dat, and the folder /a/b/c does not exist (or
     * some part of it), then the key vault attempts to create the path. If the path cannot
     * be created, then ISKEYVAULT_CREATE_PATH is returned.
     */
    int ISKEYVAULT_CREATE_PATH = 16014;

    /**
     * A key is invalid in some way (key ID, key bytes, etc).
     *
     * This may happen if a key was found to be invalid. For example, if the key is the wrong size
     * (any size other than 32 bytes), the key ID string is empty or contains invalid characters,
     * etc.
     */
    int ISKEYVAULT_INVALID_KEY = 16015;

    /**
     * A resource was not found.
     *
     * This happens when attempting to access a resource that does not exist.
     */
    int ISKEYVAULT_RESOURCE_NOT_FOUND = 16016;

    /**
     * A key vault file load operation failed due to unsupported file version.
     *
     * This happens when a key vault attempts to load a file from disk, but the
     * version of that file is not supported. This may happen when an older version
     * of the SDK is used to load a file that was saved by a newer version of the SDK.
     */
    int ISKEYVAULT_FILE_VERSION = 16017;
}
