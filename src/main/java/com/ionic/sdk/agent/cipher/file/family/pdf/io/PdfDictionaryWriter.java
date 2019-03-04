package com.ionic.sdk.agent.cipher.file.family.pdf.io;

import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfDictionary;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfObject;
import com.ionic.sdk.core.annotation.InternalUseOnly;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Object specialized to serialize a PDF dictionary.  Dictionaries may precede object streams within a PDF object.
 */
@InternalUseOnly
public class PdfDictionaryWriter {

    /**
     * The (character based) wrapper for a byte stream, allowing character output to be properly encoded into
     * the target of the PDF object write.
     */
    private final PrintStream printStream;

    /**
     * Constructor.
     *
     * @param printStream the character based wrapper of the PDF object write target
     */
    public PdfDictionaryWriter(final PrintStream printStream) {
        this.printStream = printStream;
    }

    /**
     * Serialize the content of the parameter PDF dictionary to the member output stream.
     *
     * @param dictionary             the in-memory representation of the dictionary to be serialized
     * @param formatForCompatibility a flag used to format content (for compatibility with the core Ionic SDK)
     * @return the number of bytes written in the context of this call
     * @throws UnsupportedEncodingException on failure to initialize this object for character-based writes
     */
    public int write(final PdfDictionary dictionary, final boolean formatForCompatibility)
            throws UnsupportedEncodingException {
        final String dictionaryText = writeInternal(dictionary, formatForCompatibility);
        printStream.print(dictionaryText);
        return dictionaryText.length();
    }

    /**
     * Serialize the content of the parameter PDF dictionary.
     *
     * @param dictionary             the in-memory representation of the dictionary to be serialized
     * @param formatForCompatibility a flag used to format content (for compatibility with the core Ionic SDK)
     * @return the string representation of the PDF dictionary
     * @throws UnsupportedEncodingException on failure to initialize this object for character-based writes
     */
    private static String writeInternal(final PdfDictionary dictionary, final boolean formatForCompatibility)
            throws UnsupportedEncodingException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(os, false, Pdf.CHARSET);
        ps.print(Pdf.Token.DICT);
        ps.print(formatForCompatibility ? "" : Pdf.Token.SPACER);
        final Map.Entry<String, PdfObject> first = dictionary.entrySet().iterator().next();
        for (Map.Entry<String, PdfObject> entry : dictionary.entrySet()) {
            ps.print(first.equals(entry) ? "" : Pdf.Token.SPACER);
            ps.print(entry.getKey());
            ps.print(Pdf.Token.SPACER);
            final PdfObject value = entry.getValue();
            if (value instanceof PdfDictionary) {
                ps.print(writeInternal((PdfDictionary) value, formatForCompatibility));
            } else {
                ps.print(value);
            }
        }
        ps.print(formatForCompatibility ? "" : Pdf.Token.SPACER);
        ps.print(Pdf.Token.ENDDICT);
        ps.flush();
        return new String(os.toByteArray(), Pdf.CHARSET);
    }
}
