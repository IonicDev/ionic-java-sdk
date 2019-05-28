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
    public static final class Header {

        /**
         * Constructor.
         * http://checkstyle.sourceforge.net/config_design.html#FinalClass
         */
        private Header() {
        }

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
    public static final class Generic {

        /**
         * Constructor.
         * http://checkstyle.sourceforge.net/config_design.html#FinalClass
         */
        private Generic() {
        }

        /**
         * File format family generic.
         */
        public static final String FAMILY = "generic";

        /**
         * Key in resource header JSON used to record default size of a single encryption block.
         */
        public static final String BLOCK_SIZE = "block_size";

        /**
         * Key in resource header JSON used to record the count of blocks which use the same encryption key.
         */
        public static final String META_SIZE = "meta_size";

        /**
         * Open mode for <code>RandomAccessFile</code> reading and writing, as with "rw", and also require
         * that every update to the file's content be written synchronously to the underlying storage device.
         *
         * @see java.io.RandomAccessFile#RandomAccessFile(java.io.File, String)
         */
        public static final String OPEN_MODE_ENCRYPT = "rwd";

        /**
         * Default maximum amount of bytes to read when looking for an Ionic generic file header.
         */
        public static final int HEADER_SIZE_MAX = 10000;

        /**
         * File format family generic, version 1.1.
         */
        public static final class V11 {

            /**
             * Constructor.
             * http://checkstyle.sourceforge.net/config_design.html#FinalClass
             */
            private V11() {
            }

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
        public static final class V12 {

            /**
             * Constructor.
             * http://checkstyle.sourceforge.net/config_design.html#FinalClass
             */
            private V12() {
            }

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

        /**
         * File format family generic, version 1.3.
         */
        public static final class V13 {

            /**
             * Constructor.
             * http://checkstyle.sourceforge.net/config_design.html#FinalClass
             */
            private V13() {
            }

            /**
             * File format version.
             */
            public static final String LABEL = "1.3";

            /**
             * The separator token used to separate JSON header from the subsequent payload (generic file cipher V1.3).
             */
            public static final String DELIMITER = V12.DELIMITER;

            /**
             * Size of a default plain text file block.
             */
            public static final int BLOCK_SIZE_PLAIN = 8 * 1024 * 1024;

            /**
             * Count of blocks in a v1.3 resource which use the same encryption key.
             */
            public static final int META_SIZE = 4096;
        }
    }

    /**
     * File cipher family data constants.
     */
    public static final class Csv {

        /**
         * Constructor.
         * http://checkstyle.sourceforge.net/config_design.html#FinalClass
         */
        private Csv() {
        }

        /**
         * File format family CSV (comma-separated variable).
         */
        public static final String FAMILY = "csv";

        /**
         * File format family CSV, version 1.0.
         */
        public static final class V10 {

            /**
             * Constructor.
             * http://checkstyle.sourceforge.net/config_design.html#FinalClass
             */
            private V10() {
            }

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

    /**
     * File cipher family data constants.
     */
    public static final class Pdf {

        /**
         * Constructor.
         * http://checkstyle.sourceforge.net/config_design.html#FinalClass
         */
        private Pdf() {
        }

        /**
         * File format family PDF.
         */
        public static final String FAMILY = "pdf";

        /**
         * File format family OpenXML, version 1.0.
         */
        public static final class V10 {

            /**
             * Constructor.
             * http://checkstyle.sourceforge.net/config_design.html#FinalClass
             */
            private V10() {
            }

            /**
             * File format version.
             */
            public static final String LABEL = "1.0";
        }
    }

    /**
     * File cipher family data constants.
     */
    public static final class OpenXml {

        /**
         * Constructor.
         * http://checkstyle.sourceforge.net/config_design.html#FinalClass
         */
        private OpenXml() {
        }

        /**
         * File format family XML (MS Office files).
         */
        public static final String FAMILY = "openxml";

        /**
         * File format family OpenXML, version 1.0.
         */
        public static final class V10 {

            /**
             * Constructor.
             * http://checkstyle.sourceforge.net/config_design.html#FinalClass
             */
            private V10() {
            }

            /**
             * File format version.
             */
            public static final String LABEL = "1.0";
        }
        /**
         * File format family OpenXML, version 1.1.
         */
        public static final class V11 {

            /**
             * Constructor.
             * http://checkstyle.sourceforge.net/config_design.html#FinalClass
             */
            private V11() {
            }

            /**
             * File format version.
             */
            public static final String LABEL = "1.1";
        }

        /**
         * Size of a plain text file block.
         */
        public static final int ZIPFILE_BLOCK_SIZE = 256 * 32; // 8k
        /**
         * Zip file signature bytes.
         */
        public static final String ZIPFILE_HEADER_BYTES = "\u0050\u004b\u0003\u0004";
        /**
         * content file in a Word doc file.
         */
        public static final String DOCUMENT_XML_PATH = "word/document.xml";
        /**
         * content file in an OpenXML file, contains relationship mapping.
         */
        public static final String RELS_XML_PATH = "_rels/.rels";
        /**
         * content file in a Power Point file.
         */
        public static final String PRESENTATION_XML_PATH = "ppt/presentation.xml";
        /**
         * content file in an Excel spreadsheet file.
         */
        public static final String WORKBOOK_XML_PATH = "xl/workbook.xml";
        /**
         * content file in an OpenXML file.
         */
        public static final String CONTENT_TYPES_XML_PATH = "[Content_Types].xml";
        /**
         * content file in a Word file when it is macro enabled.
         */
        public static final String CONTENT_TYPES_DOCM_MACRO_VALUE =
                "application/vnd.ms-word.document.macroEnabled.main+xml";
        /**
         * content file in a Power Point file when it is macro enabled.
         */
        public static final String CONTENT_TYPES_PPTM_MACRO_VALUE =
                "application/vnd.ms-powerpoint.presentation.macroEnabled.main+xml";
        /**
         * content file in an Excel file when it is macro enabled.
         */
        public static final String CONTENT_TYPES_XLSM_MACRO_VALUE =
                "application/vnd.ms-excel.sheet.macroEnabled.main+xml";
        /**
         * label in content file.
         */
        public static final String OVERRIDE_KEY_LABEL = "Override";
        /**
         * label in content file.
         */
        public static final String CONTENT_TYPE_KEY_LABEL = "ContentType";
        /**
         * label in content file.
         */
        public static final String DEFAULT_KEY_LABEL = "Default";
        /**
         * label in content file.
         */
        public static final String EXTENSION_KEY_LABEL = "Extension";
        /**
         * label in content file.
         */
        public static final String PART_NAME_KEY_LABEL = "PartName";
        /**
         * Ionic content type extension.
         */
        public static final String ION_CONTENT_TYPE_EXT = "ion";
        /**
         * Ionic content type.
         */
        public static final String ION_CONTENT_TYPE = "application/octet-stream";
        /**
         * Custom properties content type part name.
         */
        public static final String CUSTOM_CONTENT_TYPE_PART = "/docProps/custom.xml";
        /**
         * Custom properties content type.
         */
        public static final String CUSTOM_CONTENT_TYPE =
                "application/vnd.openxmlformats-officedocument.custom-properties+xml";
        /**
         * Relationship file labels.
         */
        public static final String RELATIONSHIP_ROOT_LABEL = "Relationships";
        /**
         * Relationship file labels.
         */
        public static final String RELATIONSHIP_LABEL = "Relationship";
        /**
         * Relationship file labels - ID.
         */
        public static final String RELATIONSHIP_ID_LABEL = "Id";
        /**
         * Relationship file labels - Target.
         */
        public static final String RELATIONSHIP_TARGET_LABEL = "Target";
        /**
         * Relationship file labels - Type.
         */
        public static final String RELATIONSHIP_TYPE_LABEL = "Type";
        /**
         * Relationship file labels - Ionic payload ID.
         */
        public static final String IONIC_EMBED_REL_ID = "rIdIonic";
        /**
         * Relationship file labels - Ionic payload target.
         */
        public static final String IONIC_EMBED_PATH = "ionic/embed.ion";
        /**
         * Relationship file labels - Ionic payload type.
         */
        public static final String IONIC_EMBED_TYPE = "http://ionic.com/relationship/embed/protectedDocument";
        /**
         * Relationship file labels - Ionic file info ID.
         */
        public static final String IONIC_INFO_REL_ID = "rIdIonicFileInfo";
        /**
         * Relationship file labels - Ionic file info target.
         */
        public static final String IONIC_INFO_PATH = "ionic/fileinfo.ion";
        /**
         * Relationship file labels - Ionic file info type.
         */
        public static final String IONIC_INFO_TYPE =
                "http://ionic.com/relationship/embed/protectedDocumentFileInfo";
        /**
         * Relationship file labels - Custom file info ID.
         */
        public static final String CUSTOM_REL_ID = "rIdCustomXml";
        /**
         * Relationship file labels - Custom file info target.
         */
        public static final String CUSTOM_PATH = "docProps/custom.xml";
        /**
         * Relationship file labels - Custom file info type.
         */
        public static final String CUSTOM_TYPE =
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties";
    }
}
