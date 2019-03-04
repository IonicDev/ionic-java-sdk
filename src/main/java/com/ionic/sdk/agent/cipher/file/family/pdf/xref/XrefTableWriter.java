package com.ionic.sdk.agent.cipher.file.family.pdf.xref;

import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfDictionary;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfDictionaryWriter;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.value.Value;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Object specialized to serialize a PDF cross reference (xref) table.
 */
@InternalUseOnly
public final class XrefTableWriter {

    /**
     * The (character based) wrapper allowing character output to be properly encoded into the target
     * of the PDF object write.
     */
    private final PrintStream printStream;

    /**
     * Constructor.
     *
     * @param outputStream the (byte based) target of the PDF object write
     * @throws UnsupportedEncodingException on failure to initialize this object for character-based writes
     */
    public XrefTableWriter(final OutputStream outputStream) throws UnsupportedEncodingException {
        this.printStream = new PrintStream(outputStream, false, Pdf.CHARSET);
    }

    /**
     * Serialize the content of the parameter table to the member output stream wrapper.
     *
     * @param startObjectNumber the number of the first object in the table (used to derive the other object numbers)
     * @param xrefTable         the in-memory representation of the object to be serialized
     */
    public void write(final int startObjectNumber, final XrefTable xrefTable) {
        printStream.print(Value.join(Pdf.Token.SPACER, startObjectNumber, xrefTable.size()));
        printStream.print(Pdf.Token.EOL);
        for (Xref xref : xrefTable.values()) {
            printStream.print(String.format(Pdf.XREF_FORMAT,
                    xref.getOffset(), xref.getGenerationNum(), xref.getInUse()));
            printStream.print(Pdf.Token.SPACER);
            printStream.print(Pdf.Token.EOL);
        }
        printStream.flush();
    }

    /**
     * Serialize the content of the parameter PDF dictionary to the member output stream wrapper.
     *
     * @param dictionary the in-memory representation of the object to be serialized
     * @throws UnsupportedEncodingException on failure to initialize this object for character-based writes
     */
    public void write(final PdfDictionary dictionary) throws UnsupportedEncodingException {
        new PdfDictionaryWriter(printStream).write(dictionary, false);
    }
}
