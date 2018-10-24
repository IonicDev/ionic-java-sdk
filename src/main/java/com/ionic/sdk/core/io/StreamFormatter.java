package com.ionic.sdk.core.io;

import com.ionic.sdk.core.codec.Transcoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class to format arbitrary content into lines with a specified maximum length.
 */
public class StreamFormatter {

    /**
     * The maximum width of an output text line.
     */
    private final int width;

    /**
     * The delimiter to write between each line of output.
     */
    private final String delimiter;

    /**
     * Constructor.
     *
     * @param width     the maximum width of an output text line
     * @param delimiter the delimiter to write between each line of output
     */
    public StreamFormatter(final int width, final String delimiter) {
        this.width = width;
        this.delimiter = delimiter;
    }

    /**
     * Write the input string into the output stream, delimiting each line with the specified delimiter.
     *
     * @param is the input text to format
     * @param os the output stream to receive the formatted text
     * @throws IOException on errors reading from the input, or writing to the output
     */
    public void write(final InputStream is, final OutputStream os) throws IOException {
        final byte[] delimiterBytes = Transcoder.utf8().decode(delimiter);
        final byte[] block = new byte[width];
        while (is.available() > 0) {
            final int count = is.read(block);
            if (count > 0) {
                os.write(block, 0, count);
                os.write(delimiterBytes);
            }
        }
    }
}
