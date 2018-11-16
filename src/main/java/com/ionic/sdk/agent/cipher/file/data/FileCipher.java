package com.ionic.sdk.agent.cipher.file.data;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.vm.EOL;
import com.ionic.sdk.crypto.CryptoUtils;

/**
 * Constant definitions used in Ionic-protected files.
 */
public final class FileCipher {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private FileCipher() {
    }

    /**
     * File cipher header data constants.
     */
    public static class Header {

        /**
         * File format family.
         */
        public static final String FAMILY = "family";

        /**
         * Ionic server associated with protection key.
         */
        public static final String SERVER = "server";

        /**
         * Ionic key ID associated with protected data.
         */
        public static final String TAG = "tag";

        /**
         * File format version.
         */
        public static final String VERSION = "version";
    }

    /**
     * File format family.
     */
    public static final String FAMILY_UNKNOWN = "unknown";

    /**
     * File cipher family data constants.
     */
    public static class Generic {

        /**
         * File format family generic.
         */
        public static final String FAMILY = "generic";

        /**
         * Open mode for <code>RandomAccessFile</code> reading and writing, as with "rw", and also require
         * that every update to the file's content be written synchronously to the underlying storage device.
         *
         * @see java.io.RandomAccessFile#RandomAccessFile(java.io.File, String)
         */
        public static final String OPEN_MODE_ENCRYPT = "rwd";

        /**
         * File format family generic, version 1.1.
         */
        public static class V11 {

            /**
             * File format version.
             */
            public static final String LABEL = "1.1";

            /**
             * The separator token used to separate JSON header from the subsequent payload (generic file cipher V1.1).
             */
            public static final String DELIMITER = "|";

            /**
             * Size of a plain text file block.
             */
            public static final int BLOCK_SIZE_PLAIN = 8192;

            /**
             * Size of a crypto file block.  Each block is base64 encoded, and includes a header and footer byte.
             */
            public static final int BLOCK_SIZE_CIPHER = ((BLOCK_SIZE_PLAIN + AesCipher.SIZE_IV) * 4 / 3) + (1 + 1);

            /**
             * Block header for <code>GenericFileCipher</code> version 1.1 blocks.
             */
            public static final String BLOCK_HEADER = "$";

            /**
             * Block footer for <code>GenericFileCipher</code> version 1.1 blocks.
             */
            public static final String BLOCK_FOOTER = "^";

            /**
             * Block header byte for <code>GenericFileCipher</code> version 1.1 blocks.
             */
            public static final byte BLOCK_HEADER_BYTE = Transcoder.utf8().decode(BLOCK_HEADER)[0];

            /**
             * Block footer byte for <code>GenericFileCipher</code> version 1.1 blocks.
             */
            public static final byte BLOCK_FOOTER_BYTE = Transcoder.utf8().decode(BLOCK_FOOTER)[0];
        }

        /**
         * File format family generic, version 1.2.
         */
        public static class V12 {

            /**
             * File format version.
             */
            public static final String LABEL = "1.2";

            /**
             * The separator token used to separate JSON header from the subsequent payload (generic file cipher V1.2).
             */
            public static final String DELIMITER = "\r\n\r\n";

            /**
             * Size of a file signature.
             */
            public static final int SIGNATURE_SIZE_CIPHER = CryptoUtils.SHA256_DIGEST_SIZE + AesCipher.SIZE_IV;

            /**
             * Size of a plain text file block.
             */
            public static final int BLOCK_SIZE_PLAIN = 10 * 1000 * 1000;

            /**
             * Size of a crypto file block.
             */
            public static final int BLOCK_SIZE_CIPHER = BLOCK_SIZE_PLAIN + AesCipher.SIZE_IV;
        }
    }

    /**
     * File cipher family data constants.
     */
    public static class Csv {

        /**
         * File format family CSV (comma-separated variable).
         */
        public static final String FAMILY = "csv";

        /**
         * File format family CSV, version 1.0.
         */
        public static class V10 {

            /**
             * File format version.
             */
            public static final String LABEL = "1.0";

            /**
             * Quote used to delimit protected content in Ionic CSV format.  This is handled in common
             * CSV enabled client applications by obscuring protected content.
             */
            public static final String QUOTE_CHAR = "\"";

            /**
             * Ionic token indicating file format version 1.0.
             */
            public static final String VERSION_1_0_STRING = "[IONIC-FILE-CSV-1.0]";

            /**
             * Ionic token indicating start of cipher text (base64 encoded, line wrapped at 80 columns).
             */
            public static final String DATA_BEGIN_STRING = "[IONIC-DATA-BEGIN]";

            /**
             * Ionic token indicating end of cipher text (base64 encoded, line wrapped at 80 columns).
             */
            public static final String DATA_END_STRING = "[IONIC-DATA-END]";

            /**
             * The CSV newline sequence.  The Ionic CSV body format is base64 encoded, and line wrapped at 80 columns.
             */
            public static final String LINE_SEPARATOR = EOL.CARRIAGE_RETURN + EOL.NEWLINE;

            /**
             * Width for single line of base64 encoded CSV payload text.
             */
            public static final int WIDTH = 80;

            /**
             * Width for single line of raw data for base64 encoded CSV payload text.
             */
            public static final int WIDTH_RAW = WIDTH * 3 / 4;

            /**
             * Size of blocks used to cache intermediate content during encrypt / decrypt operations.
             */
            public static final int BLOCK_SIZE = 1024 * 1024;
        }
    }
}
