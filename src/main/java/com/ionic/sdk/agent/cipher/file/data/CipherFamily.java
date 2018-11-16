package com.ionic.sdk.agent.cipher.file.data;

/**
 * Enumeration of all supported Ionic file cipher families.
 */
public enum CipherFamily {

    /**
     * Represents an unknown / invalid cipher family.
     */
    FAMILY_UNKNOWN,

    /**
     * Represents the Generic cipher family implemented by {@link com.ionic.sdk.agent.cipher.file.GenericFileCipher}.
     */
    FAMILY_GENERIC,

    /**
     * Represents the CSV cipher family implemented by {@link com.ionic.sdk.agent.cipher.file.CsvFileCipher}.
     */
    FAMILY_CSV,
}
