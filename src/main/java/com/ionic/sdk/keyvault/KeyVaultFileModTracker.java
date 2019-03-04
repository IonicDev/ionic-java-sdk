package com.ionic.sdk.keyvault;

import java.io.File;

/**
 * A Key Vault utility class for tracking whether a file has changed from some other source.
 */
public class KeyVaultFileModTracker {

    /**
     * Tracking result.
     */
    public enum Result {

        /**
         * Could not find the file.
         */
        FILE_NOT_FOUND,

        /**
         * File has not changed since last tracking check.
         */
        FILE_UNCHANGED,

        /**
         * File has changed since last tracking check.
         */
        FILE_CHANGED,
    }

    /**
     * Filepath the file to track.
     */
    private final String filePath;

    /**
     * File size the last time file was tracked.
     */
    private long lastFileSize = 0;

    /**
     * File time the last time file was tracked.
     */
    private long lastFileModTime = 0;

    /**
     * File size the last time file was tracked.
     */
    private boolean haslastFileInfo = false;


    /**
     * Constructor.
     * @param filePath filePath to track.
     */
    public KeyVaultFileModTracker(final String filePath) {
        this.filePath = filePath;
    }

    /**
     * Get the file path of the tracked file.
     * @return file path
     */
    public final String getFilePath() {
        return filePath;
    }

    /**
     * Get the changed/unchanged result.
     * @return An enumerated result: changed, unchanged or not found
     */
    public Result recordFileInfo() {

        // query the file
        final File trackedFile = new File(filePath);
        if (!trackedFile.exists()) {
            return Result.FILE_NOT_FOUND;
        }

        // default to return the file has changed, in case we dont have information on
        // this file from any previous query
        Result result = Result.FILE_CHANGED;
        final long fileTime = trackedFile.lastModified();
        final long fileSize = trackedFile.length();

        // determine if the file has changed by looking at file modification
        // time as well as file size
        if (haslastFileInfo && fileTime == lastFileModTime && fileSize == lastFileSize) {
            result = Result.FILE_UNCHANGED;
        }

        // update our file info if changes were detected
        if (result == Result.FILE_CHANGED) {

            lastFileModTime = fileTime;
            lastFileSize = fileSize;
            haslastFileInfo = true;
        }

        return result;
    }
}
