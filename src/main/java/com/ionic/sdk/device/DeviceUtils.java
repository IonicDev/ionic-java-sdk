package com.ionic.sdk.device;

import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.core.res.Resource;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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
     * Read a single byte from the parameter input stream.
     *
     * @param is the input stream from which to read
     * @return the next byte of data, or <code>-1</code> if the end of the stream is reached
     * @throws IonicException on stream read error
     */
    public static int readByte(final InputStream is) throws IonicException {
        try {
            return is.read();
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_EOF, e);
        }
    }

    /**
     * Completely read the requested resource from the parameter URL.
     *
     * @param url the location of the resource
     * @return a byte[] containing the content of the resource
     * @throws IonicException if an I/O error occurs
     */
    public static byte[] read(final URL url) throws IonicException {
        try {
            return Stream.read(url);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_OPENFILE, e);
        }
    }

    /**
     * Completely read the requested resource from the parameter stream.
     *
     * @param is the stream containing the resource content
     * @return a byte[] containing the content of the resource
     * @throws IonicException if an I/O error occurs
     */
    public static byte[] read(final InputStream is) throws IonicException {
        try {
            return Stream.read(is);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_OPENFILE, e);
        }
    }

    /**
     * Completely read the requested resource from the parameter file.
     * <p>
     * If an unprivileged file is being accessed, the alternate API {@link #read(File)} is recommended.  It
     * is more performant, but does require that the file length be known before the read begins.
     *
     * @param file the location of the resource
     * @return a byte[] containing the content of the resource
     * @throws IonicException if an I/O error occurs
     */
    public static byte[] readSlow(final File file) throws IonicException {
        try {
            return Stream.readSlow(file);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_OPENFILE, e);
        }
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
            throw new IonicException(SdkError.ISAGENT_OPENFILE, e);
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
            throw new IonicException(SdkError.ISAGENT_OPENFILE, e);
        }
    }

    /**
     * Write the parameter byte array to the parameter output stream.
     *
     * @param os    the target output stream  of the written bytes
     * @param bytes the source array to be written
     * @throws IonicException if an I/O error occurs
     */
    public static void write(final OutputStream os, final byte[] bytes) throws IonicException {
        try {
            Stream.write(os, bytes);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_OPENFILE, e);
        }
    }

    /**
     * Resolve a classpath resource to its filesystem location.
     *
     * @param resource the path of a resource expected to be on the JRE process classpath
     * @return the corresponding filesystem path of the resource
     * @throws IonicException on failure to locate the resource
     */
    public static File toFile(final String resource) throws IonicException {
        final URL url = Resource.resolve(resource);
        SdkData.checkTrue((url != null), SdkError.ISAGENT_RESOURCE_NOT_FOUND, URL.class.getName());
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IonicException(SdkError.ISAGENT_RESOURCE_NOT_FOUND, e);
        }
    }

    /**
     * Construct a valid {@link URL} from its string representation.
     *
     * @param url the string representation of a URL
     * @return a valid {@link URL}
     * @throws IonicException on failure to interpret the input as a {@link URL}
     */
    public static URL toUrl(final String url) throws IonicException {
        try {
            return new URL(url);
        } catch (NullPointerException e) {
            throw new IonicException(SdkError.ISAGENT_NULL_INPUT, e);
        } catch (MalformedURLException e) {
            throw new IonicException(SdkError.ISAGENT_INVALIDVALUE, e);
        }
    }
}
