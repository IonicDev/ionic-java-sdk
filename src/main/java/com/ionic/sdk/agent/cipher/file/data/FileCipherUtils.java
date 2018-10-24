package com.ionic.sdk.agent.cipher.file.data;

import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.io.FileSystem;
import com.ionic.sdk.core.rng.CryptoRng;
import com.ionic.sdk.error.IonicException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class containing various useful functions for an Ionic file cipher operations.
 */
@InternalUseOnly
public final class FileCipherUtils {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private FileCipherUtils() {
    }

    /**
     * Given a file, generate another file with a random name within the same directory.
     * <p>
     * The semantics of the file cipher in-place operation are to produce a output file as a sibling of the
     * original file, containing the result of the specified cryptography operation.  The result file is then
     * moved to replace the original file.
     *
     * @param file the source filesystem file entry to use in the generation
     * @return a random filesystem file entry appropriate for use as a temporary file
     * @throws IonicException on random number generation failure
     */
    public static File generateTempFile(final File file) throws IonicException {
        return new File(file.getParentFile(), generateTempFileName(file.getName()));
    }

    /**
     * Given a filename, generate another random filename.
     *
     * @param filename the source file name to use in the generation
     * @return a random file name appropriate for use as a temporary file
     * @throws IonicException on random number generation failure
     */
    private static String generateTempFileName(final String filename) throws IonicException {
        return String.format(FORMAT_TEMP_NAME, filename, new CryptoRng().randInt32(0, Integer.MAX_VALUE));
    }

    /**
     * The pattern used to generate the temp filename when performing an in-place FileCipher encryption / decryption.
     */
    private static final String FORMAT_TEMP_NAME = "%s.TMP-%d";

    /**
     * Renames the file denoted by "sourceFile" to exist at the new path specified by "targetFile".  If the
     * operation fails, targetFile is retained without change, and sourceFile is discarded.  These semantics
     * are useful in the context of the <code>FileCipher</code> in-place APIs.
     * <p>
     * {@link java.nio.file.Files#move}
     * <p>
     * {@link java.nio.file.Files#delete(java.nio.file.Path)}}
     *
     * @param sourceFile the old filesystem entry
     * @param targetFile the new filesystem entry
     * @throws IonicException on filesystem operation failure
     */
    public static void renameFile(final File sourceFile, final File targetFile) throws IonicException {
        final Logger logger = Logger.getLogger(FileSystem.class.getName());
        try {
            Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            try {
                Files.delete(sourceFile.toPath());
            } catch (IOException ee) {
                logger.log(Level.SEVERE, ee.getMessage(), ee);
                throw new IonicException(ee);
            }
        }
    }
}
