package com.ionic.sdk.agent.cipher.file.family.pdf.io;

import com.ionic.sdk.agent.cipher.file.family.pdf.body.PdfBodyObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfDictionary;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.Xref;
import com.ionic.sdk.core.annotation.InternalUseOnly;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Object specialized to serialize a PDF object.
 */
@InternalUseOnly
public class PdfObjectWriter {

    /**
     * The (byte based) target of the PDF object write.
     */
    private final OutputStream outputStream;

    /**
     * The (character based) wrapper allowing character output to be properly encoded into the target
     * of the PDF object write.
     */
    private final PrintStream printStream;

    /**
     * The PDF object to be written.
     */
    private final PdfBodyObject pdfBodyObject;

    /**
     * Flag used to format content (for compatibility with the core Ionic SDK).
     */
    private final boolean compatibility;

    /**
     * Object used to serialize a PDF object stream.
     */
    private final PdfStreamWriter streamWriter;

    /**
     * Constructor.
     *
     * @param outputStream  the (byte based) target of the PDF object write
     * @param pdfBodyObject the in-memory representation of the object to be serialized
     * @param compatibility a flag used to format content (for compatibility with the core Ionic SDK)
     * @throws UnsupportedEncodingException on failure to initialize this object for character-based writes
     */
    public PdfObjectWriter(final OutputStream outputStream, final PdfBodyObject pdfBodyObject,
                           final boolean compatibility) throws UnsupportedEncodingException {
        this.outputStream = outputStream;
        this.printStream = new PrintStream(outputStream, false, Pdf.CHARSET);
        this.pdfBodyObject = pdfBodyObject;
        this.compatibility = compatibility;
        this.streamWriter = new PdfStreamWriter(outputStream, printStream);
    }

    /**
     * Serialize the PDF object prologue (object start tag, dictionary, stream start tag).
     *
     * @return the number of bytes written in the context of this call
     * @throws UnsupportedEncodingException on failure to initialize this object for character-based writes
     */
    public int writeBegin() throws UnsupportedEncodingException {
        // write PDF object prologue
        final Xref xref = pdfBodyObject.getXref();
        final String objBegin = String.format(Pdf.Token.OBJ, xref.getObjectNumber(), xref.getGenerationNum());
        printStream.print(objBegin);
        printStream.print(Pdf.Token.EOL);
        final PdfDictionary pdfDictionary = pdfBodyObject.getDictionary();
        // write PDF stream prologue
        final PdfDictionaryWriter writer = new PdfDictionaryWriter(printStream);
        final int dictionaryWrite = writer.write(pdfDictionary, compatibility);
        final int streamWriteBegin = pdfBodyObject.hasStream() ? streamWriter.writeBegin() : 0;
        printStream.flush();
        return objBegin.length() + Pdf.Token.EOL.length() + dictionaryWrite + streamWriteBegin;
    }

    /**
     * Serialize a chunk of the PDF object stream.
     *
     * @param bytes PDF stream data to be written
     * @return the number of bytes written in the context of this call
     * @throws IOException on failure writing to the stream
     */
    public int write(final byte[] bytes) throws IOException {
        outputStream.write(bytes);
        return bytes.length;
    }

    /**
     * Serialize the PDF object epilogue (stream end tag, object end tag).
     *
     * @return the number of bytes written in the context of this call
     */
    public int writeEnd() {
        final int streamWriteEnd = pdfBodyObject.hasStream() ? streamWriter.writeEnd() : 0;
        printStream.print(Pdf.Token.ENDOBJ);
        printStream.flush();
        return streamWriteEnd + Pdf.Token.ENDOBJ.length();
    }
}
