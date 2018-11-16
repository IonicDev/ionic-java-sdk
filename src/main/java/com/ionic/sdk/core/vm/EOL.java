package com.ionic.sdk.core.vm;

import com.ionic.sdk.core.codec.Transcoder;

/**
 * Utility functions to handle normalization of text data streams from the filesystem file.
 */
public final class EOL {

    /**
     * Constructor. http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private EOL() {
    }

    /**
     * Ensure that content end of line character is CRLF.
     *
     * @param data content, which may have either LF or CRLF end of line character
     * @return normalized content, where each end of line character is CRLF
     */
    public static byte[] normalizeToWindows(final byte[] data) {
        final String text = Transcoder.utf8().encode(data);
        final String textLinux = text.replace(LINE_SEPARATOR_WINDOWS, LINE_SEPARATOR_LINUX);
        final String textWindows = textLinux.replace(LINE_SEPARATOR_LINUX, LINE_SEPARATOR_WINDOWS);
        return Transcoder.utf8().decode(textWindows);
    }

    /**
     * The carriage return character.
     */
    public static final String CARRIAGE_RETURN = "\r";

    /**
     * The carriage return character.
     */
    public static final String NEWLINE = "\n";

    /**
     * The Linux operating system newline sequence.
     */
    public static final String LINE_SEPARATOR_LINUX = NEWLINE;

    /**
     * The Windows operating system newline sequence.
     */
    public static final String LINE_SEPARATOR_WINDOWS = CARRIAGE_RETURN + NEWLINE;
}
