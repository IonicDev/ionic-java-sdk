package com.ionic.sdk.agent.cipher.file.data;

/**
 * Enumeration of all supported Ionic file cipher file types.
 * <p>
 * As of SDK version 2.3, only the FILETYPE_CSV type is a supported file type.
 * <p>
 * As of SDK version 2.4, all enumerated types are supported.
 */
public enum FileType {

    /**
     * Represents an unknown file type.
     */
    FILETYPE_UNKNOWN,

    /**
     * Represents the Word file type, as used by {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher}.
     */
    FILETYPE_DOCX,

    /**
     * Represents the PowerPoint file type, as used by {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher}.
     */
    FILETYPE_PPTX,

    /**
     * Represents the Excel file type, as used by {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher}.
     */
    FILETYPE_XLSX,

    /**
     * Represents the Word Macro file type, as used by {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher}.
     */
    FILETYPE_DOCM,

    /**
     * Represents the PowerPoint Macro file type, as used by {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher}.
     */
    FILETYPE_PPTM,

    /**
     * Represents the Excel Macro file type, as used by {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher}.
     */
    FILETYPE_XLSM,

    /**
     * Represents the Portable Document Format file type, as used
     * by {@link com.ionic.sdk.agent.cipher.file.PdfFileCipher}.
     */
    FILETYPE_PDF,

    /**
     * Represents the comma-separated variable (CSV) file type, as used
     * by {@link com.ionic.sdk.agent.cipher.file.CsvFileCipher}.
     */
    FILETYPE_CSV,
}
