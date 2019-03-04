package com.ionic.sdk.agent.cipher.file.family.pdf.data;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Declarations used when parsing PDF document content.
 */
public final class Pdf {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Pdf() {
    }

    /**
     * Declarations for text tokens used during serialization of a PDF object stream.
     */
    public static class Token {

        /**
         * The in-line delimiter used to separate tokens in a serialized PDF object.
         */
        public static final String SPACER = " ";

        /**
         * The default end of line character, used to delimit portions of a PDF object.
         */
        public static final String EOL = "\n";

        /**
         * The prologue string format pattern of a serialized PDF object.  The replacement tokens will contain
         * the PDF object number and PDF generation number.
         */
        public static final String OBJ = "%d %d obj";

        /**
         * The epilogue of a serialized PDF object.
         */
        public static final String ENDOBJ = "endobj" + EOL;

        /**
         * The prologue of a serialized PDF dictionary.
         */
        public static final String DICT = "<<";

        /**
         * The epilogue of a serialized PDF dictionary.
         */
        public static final String ENDDICT = ">>" + EOL;

        /**
         * The prologue of a serialized PDF stream.
         */
        public static final String STREAM = "stream" + EOL;

        /**
         * The epilogue of a serialized PDF stream.
         */
        public static final String ENDSTREAM = "endstream" + EOL;

        /**
         * The marker associated with the a PDF xref (cross reference) table.
         */
        public static final String XREF = "xref" + EOL;

        /**
         * The epilogue marker associated with the trailing dictionary of the top-level PDF xref
         * (cross reference) table.
         */
        public static final String TRAILER = "trailer" + EOL;

        /**
         * The epilogue marker associated with the offset of the top-level PDF xref (cross reference) table.
         */
        public static final String STARTXREF = "startxref" + EOL;

        /**
         * The end of file marker in a serialized PDF stream.  The PDF specification allows for modification of a
         * document by appending new content, so there may be multiple instances of this marker within a single
         * PDF byte stream.
         */
        public static final String EOF = "%%EOF" + EOL;
    }

    /**
     * Declarations for regular expressions used to navigate through the top level of the PDF document.
     */
    public static class Regex {
        /**
         * Regex for identification of "xref" portion of PDF document footer.
         */
        public static final Pattern XREF_TABLE_HEADER = Pattern.compile("\\G\\s*xref\\s+");

        /**
         * Regex for identification of section header contained in "xref" section of PDF document footer.
         */
        public static final Pattern XREF_SECTION_HEADER = Pattern.compile("\\G(\\d+)\\s(\\d+)\\s{1,3}");

        /**
         * Regex for identification of entry contained in "xref" section of PDF document footer.
         */
        public static final Pattern XREF_ENTRY = Pattern.compile("\\G(\\d{10})\\s(\\d{5})\\s(\\w)\\s{1,2}");

        /**
         * Regex for identification of index entry contained in prologue of PDF object stream.
         */
        public static final Pattern OBJSTM_ENTRY = Pattern.compile("\\G(\\d+) (\\d+) ");

        /**
         * Regex for identification of "trailer" portion of PDF document footer.  This leaves the scanner
         * at the starting position of the dictionary, suitable for consumption by PdfDictionaryReader.
         */
        public static final Pattern TRAILER_HEAD = Pattern.compile("\\G(?s)trailer\\s{1,2}");

        /**
         * Regex for identification of "trailer" portion of PDF document footer.
         */
        public static final Pattern TRAILER = Pattern.compile("(?s)trailer\\s{1,2}(<<.+?>>)\\s{1,2}");

        /**
         * Regex for identification of "startxref" portion of PDF document footer.
         */
        public static final Pattern STARTXREF = Pattern.compile("startxref\\s+(\\d+)\\s+%%EOF\\s*");
    }

    /**
     * Format string used to synthesize an xref record from a PDF cross-reference stream.
     */
    public static final String XREF_FORMAT = "%010d %05d %s";

    /**
     * Format string used to synthesize a PDF object length dictionary value.  We use a fixed width so that we may
     * seek back after writing the ciphertext stream to record its size.
     */
    public static final String LENGTH_FORMAT = "%010d";

    /**
     * Character indicating an in-use xref within an XrefTable entry.
     */
    public static final String XREF_IN_USE = "n";

    /**
     * Marker indicating an indirect document reference.
     */
    public static final String REFERENCE = "R";

    /**
     * The charset to use when converting back and forth between byte streams and character streams, in the
     * context of processing PDF documents when performing Ionic cryptography operations.
     */
    public static final String CHARSET = StandardCharsets.ISO_8859_1.name();

    /**
     * Declarations for PDF dictionary keys and values used within the code.
     */
    public static class KV {

        /**
         * PDF dictionary key used to identify the PDF object containing the Ionic ciphertext metadata.
         */
        public static final String INFO = "/Info";

        /**
         * PDF dictionary key used to identify native PDF encryption (incompatible with Ionic protection).
         */
        public static final String ENCRYPT = "/Encrypt";

        /**
         * PDF dictionary key used to identify the startxref of the previous document xref table.
         */
        public static final String PREV = "/Prev";

        /**
         * PDF dictionary key used to identify the PDF object containing the Ionic ciphertext.
         */
        public static final String IONIC_PAYLOAD = "/IonicPayload";

        /**
         * PDF dictionary key used to identify the length of the associated PDF stream.
         */
        public static final String LENGTH = "/Length";

        /**
         * PDF dictionary key used to identify the number of bytes associated with a PDF cross-reference stream record.
         */
        public static final String COLUMNS = "/Columns";

        /**
         * PDF dictionary key used to identify the parameters associated with a PDF cross-reference stream.
         */
        public static final String DECODE_PARMS = "/DecodeParms";

        /**
         * PDF dictionary key used to identify the starting offset and count for xref records in a
         * PDF cross-reference stream.
         */
        public static final String INDEX = "/Index";

        /**
         * PDF dictionary key used to identify the size (in records) of a PDF cross-reference stream.
         */
        public static final String SIZE = "/Size";

        /**
         * PDF dictionary key used to identify the width of the fields in a PDF cross-reference stream table entry.
         */
        public static final String W = "/W";

        /**
         * PDF dictionary key used to identify the methods used to serialize the associated PDF object.
         */
        public static final String FILTER = "/Filter";

        /**
         * PDF dictionary key used to specify the starting offset of the embedded objects within a PDF object stream.
         */
        public static final String FIRST = "/First";

        /**
         * PDF dictionary value used to denote the use of the FLATE compression algorithm to reduce the size of the
         * serialized PDF object.  The class {@link java.util.zip.Inflater} may be used to recover the original bytes.
         */
        public static final String FLATE_DECODE = "/FlateDecode";

        /**
         * PDF dictionary key used to specify the type of object found in the object's stream.
         */
        public static final String TYPE = "/Type";

        /**
         * PDF dictionary value used to specify a cross reference stream.
         */
        public static final String XREF = "/XRef";
    }
}
