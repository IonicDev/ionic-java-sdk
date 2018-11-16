package com.ionic.sdk.core.io;

import java.io.File;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Utility functions relating to access to JVM filesystem resources.
 */
public final class FileSystem {

    /**
     * Constructor. http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private FileSystem() {
    }

    /**
     * Enumerate (recursive) files contained in specified folder.
     *
     * @param file    folder or file to examine for File descendants
     * @param recurse flag indicating whether content of child folders should be included in result
     * @return a list of files descended from specified folder
     */
    public static Collection<File> listFiles(final File file, final boolean recurse) {
        final Collection<File> files = new TreeSet<File>();
        addFilesInternal(files, file, true, recurse);
        return files;
    }

    /**
     * Recurse through the source tree, finding source files to be tested for strings.
     *
     * @param files   the container to hold found files to be checked
     * @param file    the intermediate file to be checked / recursed (if folder)
     * @param isRoot  flag indicating whether the initial parameter file is being checked
     * @param recurse flag indicating whether content of child folders should be included in result
     */
    private static void addFilesInternal(final Collection<File> files, final File file,
                                         final boolean isRoot, final boolean recurse) {
        if (!file.isDirectory()) {
            files.add(file);
        } else if (file.isDirectory() && (isRoot || recurse)) {
            final File[] filesIt = listFilesInternal(file);
            for (File fileIt : filesIt) {
                addFilesInternal(files, fileIt, false, recurse);
            }
        }
    }

    /**
     * Enumerate file children (if any) of specified filesystem folder.
     *
     * @param folder folder to examine for File children
     * @return an array of file objects contained in specified folder
     */
    private static File[] listFilesInternal(final File folder) {
        final File[] files = folder.listFiles();
        return ((files == null) ? new File[0] : files);
    }
}
