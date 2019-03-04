package com.ionic.sdk.agent.cipher.file.family.pdf.io;

import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.io.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Object specialized to serialize a PDF object stream.
 */
@InternalUseOnly
public final class PdfStreamWriter {

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
     * Constructor.
     *
     * @param outputStream the (byte based) target of the PDF object write
     * @param printStream  the character based wrapper of the PDF object write target
     */
    PdfStreamWriter(final OutputStream outputStream, final PrintStream printStream) {
        this.outputStream = outputStream;
        this.printStream = printStream;
    }

    /**
     * Write the PDF stream prologue to the member output stream.
     *
     * @return the number of bytes written to the stream
     */
    int writeBegin() {
        printStream.print(Pdf.Token.STREAM);
        printStream.flush();
        return Pdf.Token.STREAM.length();
    }

    /**
     * Consume the content of the source input, writing it into a PDF container with expected delimiter tokens.
     *
     * @param stream the source of the PDF object stream
     * @throws IOException on failures writing to the output
     */
    public void write(final InputStream stream) throws IOException {
        Stream.transmit(stream, outputStream);
    }

    /**
     * Write the PDF stream epilogue to the member output stream.
     *
     * @return the number of bytes written to the stream
     */
    int writeEnd() {
        printStream.print(Pdf.Token.EOL);
        printStream.print(Pdf.Token.ENDSTREAM);
        printStream.flush();
        return Pdf.Token.EOL.length() + Pdf.Token.ENDSTREAM.length();
    }
}
