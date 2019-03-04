package com.ionic.sdk.agent.cipher.file.data;

/**
 * Enumeration of all supported Ionic file cipher file types.
 * <p>
 * As of SDK version 2.3, only the FILETYPE_CSV type is a supported file type.
 */
public enum FileType {

    /**
     * Represents an unknown file type.
     */
    FILETYPE_UNKNOWN,

    /**
     * Not yet implemented.
     */
    FILETYPE_DOCX,

    /**
     * Not yet implemented.
     */
    FILETYPE_PPTX,

    /**
     * Not yet implemented.
     */
    FILETYPE_XLSX,

    /**
     * Not yet implemented.
     */
    FILETYPE_DOCM,

    /**
     * Not yet implemented.
     */
    FILETYPE_PPTM,

    /**
     * Not yet implemented.
     */
    FILETYPE_XLSM,

    /**
     * Not yet implemented.
     */
    FILETYPE_PDF,

    /**
     * Represents the comma-separated variable (CSV) file type.
     */
    FILETYPE_CSV,
}
