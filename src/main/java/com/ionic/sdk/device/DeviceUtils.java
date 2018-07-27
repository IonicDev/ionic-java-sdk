package com.ionic.sdk.device;

import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.io.File;
import java.io.IOException;

/**
 * Utility class containing various useful functions for an Ionic-enabled device context.
 */
public final class DeviceUtils {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private DeviceUtils() {
    }

    /**
     * Completely read the requested resource from the parameter file.
     *
     * @param file the location of the resource
     * @return a byte[] containing the content of the resource
     * @throws IonicException if an I/O error occurs
     */
    public static byte[] read(final File file) throws IonicException {
        try {
            return Stream.read(file);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_OPENFILE);
        }
    }

    /**
     * Write the parameter byte array to the parameter file.
     *
     * @param file  the target of the written bytes
     * @param bytes the source array to be written
     * @throws IonicException if an I/O error occurs
     */
    public static void write(final File file, final byte[] bytes) throws IonicException {
        try {
            Stream.write(file, bytes);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_OPENFILE);
        }
    }
}
