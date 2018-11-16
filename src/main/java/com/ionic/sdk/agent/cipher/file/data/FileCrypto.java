package com.ionic.sdk.agent.cipher.file.data;

import com.ionic.sdk.agent.cipher.file.CsvFileCipher;
import com.ionic.sdk.agent.cipher.file.FileCipherAbstract;
import com.ionic.sdk.agent.cipher.file.GenericFileCipher;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
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
        String familyString;
        switch (cipherFamily) {
            case FAMILY_GENERIC:
                familyString = FileCipher.Generic.FAMILY;
                break;
            case FAMILY_CSV:
                familyString = FileCipher.Csv.FAMILY;
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
        try {
            try (FileInputStream is = new FileInputStream(file)) {
                final byte[] inputBytes = new byte[FileCipher.Csv.V10.BLOCK_SIZE];
                final int read = is.read(inputBytes, 0, inputBytes.length);
                return getFileInfoInternal((read < 0) ? new byte[0] : Arrays.copyOf(inputBytes, read));
            }
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        }
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
                new GenericFileCipher(null),
        };
        FileCryptoFileInfo fileInfo = new FileCryptoFileInfo();
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
        }
        return cipherFamily;
    }
}
