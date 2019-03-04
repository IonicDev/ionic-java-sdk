package com.ionic.sdk.agent.cipher.file.data;

import com.ionic.sdk.agent.cipher.file.CsvFileCipher;
import com.ionic.sdk.agent.cipher.file.FileCipherAbstract;
import com.ionic.sdk.agent.cipher.file.GenericFileCipher;
import com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher;
import com.ionic.sdk.agent.cipher.file.PdfFileCipher;
import com.ionic.sdk.error.IonicException;

import java.io.File;
import java.util.logging.Logger;

/**
 * Utility class to query the content of a file for Ionic encryption information.  This may be used to examine
 * content to affirm its Ionic state before other operations are attempted.  (For example, one might check that
 * a file is encrypted before attempting decryption.)
 */
public final class FileCrypto {

    /**
     * Class scoped logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FileCrypto.class.getName());

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private FileCrypto() {
    }

    /**
     * Return the label corresponding to the input {@link CipherFamily}.
     *
     * @param cipherFamily the input file family
     * @return the label corresponding to the input
     */
    public static String getFamilyString(final CipherFamily cipherFamily) {
        final String familyString;
        switch (cipherFamily) {
            case FAMILY_GENERIC:
                familyString = FileCipher.Generic.FAMILY;
                break;
            case FAMILY_CSV:
                familyString = FileCipher.Csv.FAMILY;
                break;
            case FAMILY_PDF:
                familyString = FileCipher.Pdf.FAMILY;
                break;
            case FAMILY_OPENXML:
                familyString = FileCipher.OpenXml.FAMILY;
                break;
            default:
                familyString = FileCipher.FAMILY_UNKNOWN;
        }
        return familyString;
    }

    /**
     * Determines if a file is Ionic protected, and various pieces of information about the file.
     *
     * @param filePath the input file path
     * @return the file information object for the specified input file
     * @throws IonicException on failure accessing or parsing the content
     */
    public static FileCryptoFileInfo getFileInfo(final String filePath) throws IonicException {
        final File file = new File(filePath);
        LOGGER.fine(String.format("file, name = %s", file.getName()));
        return getFileInfoInternal(filePath);
    }

    /**
     * Determines if some content is Ionic protected, and various pieces of information about the file.
     *
     * @param inputBytes the input buffer
     * @return the file information object for the specified input buffer
     * @throws IonicException on failure accessing or parsing the content
     */
    public static FileCryptoFileInfo getFileInfo(final byte[] inputBytes) throws IonicException {
        LOGGER.fine(String.format("byte array, length = %d", inputBytes.length));
        return getFileInfoInternal(inputBytes);
    }

    /**
     * OpenXml v1.0 extension.
     */
    private static final String OPENXML_1_0_EXTENSION_DOCXS = ".docxs";
    /**
     * OpenXml v1.0 extension.
     */
    private static final String OPENXML_1_0_EXTENSION_PPTXS = ".pptxs";
    /**
     * OpenXml v1.0 extension.
     */
    private static final String OPENXML_1_0_EXTENSION_XLSXS = ".xlsxs";

    /**
     * Determines if some content is Ionic protected, and various pieces of information about the content.
     * <p>
     * This function may be passed a file that is not Ionic protected.  The read / parse of the Ionic header
     * may also fail due to I/O errors or malformed header content.  In these cases, the function returns a default
     * file info object, which indicates that the InputStream is not Ionic protected.
     *
     * @param filepath the input file name and path
     * @return the file information object for the specified input buffer
     * @throws IonicException on failure accessing or parsing the content
     */
    private static FileCryptoFileInfo getFileInfoInternal(final String filepath) throws IonicException {

        final GenericFileCipher genCipher = new GenericFileCipher(null);
        FileCryptoFileInfo fileInfo = null;

        if (filepath.endsWith(OPENXML_1_0_EXTENSION_DOCXS)
            || filepath.endsWith(OPENXML_1_0_EXTENSION_PPTXS)
            || filepath.endsWith(OPENXML_1_0_EXTENSION_XLSXS)) {
            // these old .<ext>s files (note the 's') have the same encryption format as the
            // generic 1.1 format.  we perform a test here to be sure the file is recognized
            // as such.
            fileInfo = genCipher.getFileInfo(filepath);
            if (!fileInfo.getCipherFamily().equals(CipherFamily.FAMILY_UNKNOWN)) {
                fileInfo.setCipherFamily(CipherFamily.FAMILY_OPENXML);
                fileInfo.setCipherVersion(FileCipher.OpenXml.V10.LABEL);
            }
            return fileInfo;
        }

        final FileCipherAbstract[] fileCiphers = {
                new CsvFileCipher(null),
                new PdfFileCipher(null),
                new OpenXmlFileCipher(null),
                genCipher,
        };
        for (FileCipherAbstract fileCipher : fileCiphers) {
            fileInfo = fileCipher.getFileInfo(filepath);
            if (!fileInfo.getCipherFamily().equals(CipherFamily.FAMILY_UNKNOWN)) {
                break;
            }
        }
        return fileInfo;
    }

    /**
     * Determines if some content is Ionic protected, and various pieces of information about the content.
     * <p>
     * This function may be passed an InputStream that is not Ionic protected.  The read / parse of the Ionic header
     * may also fail due to I/O errors or malformed header content.  In these cases, the function returns a default
     * file info object, which indicates that the InputStream is not Ionic protected.
     *
     * @param headerBytes the input stream
     * @return the file information object for the specified input buffer
     * @throws IonicException on failure accessing or parsing the content
     */
    private static FileCryptoFileInfo getFileInfoInternal(final byte[] headerBytes) throws IonicException {
        final FileCipherAbstract[] fileCiphers = {
                new CsvFileCipher(null),
                new PdfFileCipher(null),
                new OpenXmlFileCipher(null),
                new GenericFileCipher(null),
        };
        FileCryptoFileInfo fileInfo = null;
        for (FileCipherAbstract fileCipher : fileCiphers) {
            fileInfo = fileCipher.getFileInfo(headerBytes);
            if (!fileInfo.getCipherFamily().equals(CipherFamily.FAMILY_UNKNOWN)) {
                break;
            }
        }
        return fileInfo;
    }

    /**
     * Converts a cipher family string (from an Ionic file header, for example), into an instance of the
     * {@link CipherFamily} enumeration.
     *
     * @param family a string representation of the family indicated by the header
     * @return an instance of the {@link CipherFamily} enumeration
     */
    public static CipherFamily toCipherFamily(final String family) {
        CipherFamily cipherFamily = CipherFamily.FAMILY_UNKNOWN;
        if (FileCipher.Generic.FAMILY.equals(family)) {
            cipherFamily = CipherFamily.FAMILY_GENERIC;
        } else if (FileCipher.Csv.FAMILY.equals(family)) {
            cipherFamily = CipherFamily.FAMILY_CSV;
        } else if (FileCipher.Pdf.FAMILY.equals(family)) {
            cipherFamily = CipherFamily.FAMILY_PDF;
        } else if (FileCipher.OpenXml.FAMILY.equals(family)) {
            cipherFamily = CipherFamily.FAMILY_OPENXML;
        }
        return cipherFamily;
    }
}
