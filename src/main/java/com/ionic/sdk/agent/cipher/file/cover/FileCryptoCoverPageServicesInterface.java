package com.ionic.sdk.agent.cipher.file.cover;

import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.error.IonicException;

/**
 * Cover Page Service provider interface class used by the various cipher implementations.
 * <p>
 * This interface allows for a consumer to override the default cover page provider with their own implementation.
 */
public abstract class FileCryptoCoverPageServicesInterface {

    /**
     * Get the requested cover page.
     * <p>
     * This function is called by the file ciphers to retrieve a cover page when creating your own
     * implementation of FileCryptoCoverPageServicesInterface.
     *
     * @param fileType the {@link FileType} requested
     * @return the bytes of the cover page
     * @throws IonicException on failure to fetch cover page of the requested type
     */
    public abstract byte[] getCoverPage(FileType fileType) throws IonicException;

    /**
     * Get the requested access denied page.
     * <p>
     * This function is called by the file ciphers to retrieve an access denied page when creating your own
     * implementation of FileCryptoCoverPageServicesInterface.
     *
     * @param fileType the {@link FileType} requested
     * @return the bytes of the access denied page
     * @throws IonicException on failure to fetch access denied page of the requested type
     */
    public abstract byte[] getAccessDeniedPage(FileType fileType) throws IonicException;
}
