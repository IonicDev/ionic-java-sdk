package com.ionic.sdk.core.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

/**
 * Utilities to read / write byte[] from source objects.
 */
public final class Stream {

    /**
     * Constructor. http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Stream() {
    }

    /**
     * Completely read the content of the <code>InputStream</code>, writing the content to the
     * <code>OutputStream</code>.
     *
     * @param is the source data stream from which to read
     * @param os the target data stream to which to write
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void transmit(final InputStream is, final OutputStream os) throws IOException {
        int b;
        while ((b = is.read()) >= 0) {
            os.write(b);
        }
    }

    /**
     * Completely read the requested resource from the parameter URL.
     *
     * @param url
     *            the location of the resource
     * @return a byte[] containing the content of the resource
     * @throws IOException
     *             if an I/O error occurs
     */
    public static byte[] read(final URL url) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
            return readInternal(bis, Integer.MAX_VALUE);
        }
    }

    /**
     * Completely read the underlying resource from the parameter stream.
     *
     * @param is
     *            the stream from which to read
     * @return a byte[] containing the content of the stream
     * @throws IOException
     *             if an I/O error occurs
     */
    public static byte[] read(final InputStream is) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            return readInternal(bis, Integer.MAX_VALUE);
        }
    }

    /**
     * Completely read the underlying resource from the parameter stream.
     *
     * @param bis
     *            the stream from which to read
     * @param length
     *            the maximum size (in bytes) requested in the context of this operation
     * @return a byte[] containing the content of the stream
     * @throws IOException
     *             if an I/O error occurs
     */
    public static byte[] read(final BufferedInputStream bis, final int length) throws IOException {
        return readInternal(bis, length);
    }

    /**
     * Completely read the requested resource from the parameter file.
     * <p>
     * If an unprivileged file is being accessed, the alternate API {@link #read(File)} is recommended.  It
     * is more performant, but does require that the file length be known before the read begins.
     *
     * @param file the location of the resource
     * @return a byte[] containing the content of the resource
     * @throws IOException if an I/O error occurs
     */
    public static byte[] readSlow(final File file) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            return readInternal(bis, Integer.MAX_VALUE);
        }
    }

    /**
     * Completely read the requested resource from the parameter file.
     *
     * @param file
     *            the location of the resource
     * @return a byte[] containing the content of the resource
     * @throws IOException
     *             if an I/O error occurs
     */
    public static byte[] read(final File file) throws IOException {
        final byte[] fileDataBytes = new byte[(int) file.length()];

        final FileInputStream is = new FileInputStream(file);
        try {
            final int readLen = is.read(fileDataBytes);
            if (readLen < file.length()) {
                throw new IOException(file.getName());
            }
        } finally {
            is.close();
        }
        return fileDataBytes;
    }

    /**
     * Completely read the underlying resource from the parameter stream.
     *
     * @param is
     *            the stream from which to read
     * @param length
     *            the maximum size (in bytes) requested in the context of this operation
     * @return a byte[] containing the content of the stream
     * @throws IOException
     *             if an I/O error occurs
     */
    private static byte[] readInternal(final BufferedInputStream is, final int length) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        int count = 0;
        int b;
        while ((b = is.read()) >= 0) {
            os.write(b);
            if (++count >= length) {
                break;
            }
        }
        return os.toByteArray();
    }

    /**
     * Write the parameter input stream to the parameter output stream.
     *
     * @param os
     *            the target of the written bytes
     * @param is
     *            the source of the bytes to be written
     * @throws IOException
     *             if an I/O error occurs
     */
    private static void writeInternal(final BufferedOutputStream os, final BufferedInputStream is) throws IOException {
        int data;
        while ((data = is.read()) >= 0) {
            os.write(data);
        }
    }

    /**
     * Write to disk.
     *
     * @param path
     *            the path to write to.
     * @param dataToWrite
     *            the data to write to disk.
     * @throws IonicException
     *             throws an ISAGENT_OPENFILE exception if File.write fails.
     */
    public static void writeToDisk(final String path, final byte[] dataToWrite) throws IonicException {
        try {
            Files.write(Paths.get(path), dataToWrite);
        } catch (final IOException e) {
            throw new IonicException(SdkError.ISAGENT_OPENFILE, e);
        }
    }

    /**
     * Load file into memory.
     *
     * @param fullPath
     *              the file path to a file we will read into memory
     * @return the in-memory file
     */
    public static byte[] loadFileIntoMemory(final String fullPath) {
        Path path = null;
        byte[] loadedFile = null;
        try {
            path = FileSystems.getDefault().getPath(fullPath);

        } catch (final InvalidPathException ie) {
            return null;
        }

        try {
            loadedFile = Files.readAllBytes(path);

        } catch (final IOException e) {
            return null;
        }

        return loadedFile;
    }

    /**
     * Write the parameter input stream to the parameter output stream.
     *
     * @param os
     *            the target of the written bytes
     * @param is
     *            the source of the bytes to be written
     * @throws IOException
     *             if an I/O error occurs
     */

    public static void write(final OutputStream os, final InputStream is) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
            try (BufferedInputStream bis = new BufferedInputStream(is)) {
                writeInternal(bos, bis);
            }
        }
    }

    /**
     * Write the parameter byte array to the parameter output stream.
     *
     * @param os
     *            the target of the written bytes
     * @param bytes
     *            the source array to be written
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void write(final OutputStream os, final byte[] bytes) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
            try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(bytes))) {
                writeInternal(bos, bis);
            }
        }
    }

    /**
     * Write the parameter byte array to the parameter file.
     *
     * @param file
     *            the target of the written bytes
     * @param bytes
     *            the source array to be written
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void write(final File file, final byte[] bytes) throws IOException {
        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            os.write(bytes);
        }
    }
}
