package com.ionic.sdk.agent.cipher.file.family.pdf.io;

import com.ionic.sdk.agent.cipher.file.family.pdf.body.PdfBodyObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.Xref;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.XrefTable;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class used to deserialize a PDF object contained in a valid PDF document.
 */
@InternalUseOnly
public final class PdfObjectReader {

    /**
     * The channel interface, through which the PDF document data is accessed.
     */
    private final SeekableByteChannel channel;

    /**
     * The xref associated with the PDF object to be parsed.
     */
    private final Xref xref;

    /**
     * The collection of xref entries associated with the active PDF document.
     */
    private final XrefTable xrefTable;

    /**
     * @return the channel interface, through which the PDF document data is accessed
     */
    public SeekableByteChannel getChannel() {
        return channel;
    }

    /**
     * Constructor.
     *
     * @param channel   the source of the PDF object to be parsed
     * @param xref      the xref associated with the PDF object to be parsed
     * @param xrefTable the collection of xref entries associated with the active PDF document
     * @throws IonicException on failure to parse the PDF object stream object (if present)
     * @throws IOException    on failure to read or write to the channel
     */
    public PdfObjectReader(final SeekableByteChannel channel, final Xref xref, final XrefTable xrefTable)
            throws IonicException, IOException {
        final boolean isObjStm = (xref.getObjStmEntry() != null);
        this.channel = isObjStm ? new PdfObjStmReader(xref, xrefTable).getChannel(channel) : channel;
        this.xref = xref;
        this.xrefTable = xrefTable;
    }

    /**
     * Read the dictionary associated with the PDF object.  This can be examined to determine whether or not
     * the object contains a stream.
     *
     * @return a container for metadata about the PDF object
     * @throws IonicException on failure to parse the PDF object
     * @throws IOException    on failure to read or write to the channel
     */
    public PdfBodyObject readPartial() throws IonicException, IOException {
        final SeekableByteChannel position = channel.position(xref.getOffset());
        SdkData.checkTrue((xref.getOffset() == position.position()), SdkError.ISFILECRYPTO_EOF);
        final Scanner scanner = new Scanner(channel, Pdf.CHARSET);

        final String prologue;
        final boolean isObjectStream = (xref.getObjStmEntry() != null);
        if (isObjectStream) {
            prologue = "";
        } else {
            final String withinHorizon = scanner.findWithinHorizon(OBJ_PROLOGUE, MAX_OBJ_START);
            SdkData.checkTrue((withinHorizon != null), SdkError.ISFILECRYPTO_PARSEFAILED);
            prologue = withinHorizon;
        }

        final PdfDictionaryReader reader = new PdfDictionaryReader(scanner);
        final PdfObject value = reader.read();
        final int offsetStream = prologue.length() + reader.getCount();
        return new PdfBodyObject(xref, value, offsetStream, prologue);
    }

    /**
     * Position the channel cursor at the beginning of the PDF object's stream.
     *
     * @param pdfBodyObject a container for metadata describing the PDF object
     * @return the position in the channel of the PDF object's stream bytes
     * @throws IOException    on failure to read or write the channel's cursor
     * @throws IonicException on expectation failure associated with the PDF object stream wrapper
     */
    public int readStreamInit(final PdfBodyObject pdfBodyObject) throws IOException, IonicException {
        final int offsetPdfStream = pdfBodyObject.getXref().getOffset() + pdfBodyObject.getOffsetStream();
        channel.position(offsetPdfStream);
        final Scanner scanner = new Scanner(channel, Pdf.CHARSET);
        final String streamOpen = scanner.findWithinHorizon(OBJ_STREAM_OPEN, 32);
        SdkData.checkTrue((streamOpen != null), SdkError.ISFILECRYPTO_PARSEFAILED);
        final int lengthToken = scanner.match().end() - scanner.match().start();
        return (int) channel.position(offsetPdfStream + lengthToken).position();
    }

    /**
     * Retrieve the length of the PDF object stream.  This may be recorded as an indirect object reference.
     *
     * @param lengthStream the value retrieved from the PDF dictionary associated with the object
     * @return the length of the PDF object stream
     * @throws IOException    on failure to read or write to the channel
     * @throws IonicException on expectation failure associated with the PDF object stream wrapper
     */
    public int readStreamLength(final String lengthStream) throws IOException, IonicException {
        final Matcher matcherDirect = Pattern.compile(REGEX_REFERENCE_DIRECT).matcher(lengthStream);
        final Matcher matcherIndirect = Pattern.compile(REGEX_REFERENCE_INDIRECT).matcher(lengthStream);
        if (matcherDirect.matches()) {
            return Value.toInt(lengthStream, 0);
        } else if (matcherIndirect.matches()) {
            final int objectNumber = Value.toInt(matcherIndirect.group(1), 0);
            SdkData.checkTrue(objectNumber > 0, SdkError.ISFILECRYPTO_PARSEFAILED);
            final Xref xrefIndirect = xrefTable.get(objectNumber);
            final PdfObjectReader readerIndirect = new PdfObjectReader(channel, xrefIndirect, xrefTable);
            final PdfBodyObject pdfBodyObjectIndirect = readerIndirect.readPartial();
            return Value.toInt(pdfBodyObjectIndirect.getStringValue(), 0);
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
    }

    /**
     * Check the PDF object footer to verify conformity with the PDF file format.
     *
     * @throws IonicException on expectation failure associated with the PDF object stream wrapper
     */
    public void readStreamWrapup() throws IonicException {
        final Scanner scanner = new Scanner(channel, Pdf.CHARSET);
        final String streamClose = scanner.findWithinHorizon(PdfObjectReader.OBJ_STREAM_CLOSE, 32);
        SdkData.checkTrue((streamClose != null), SdkError.ISFILECRYPTO_PARSEFAILED);
    }

    /**
     * Define a maximum amount of data (in bytes) in the prelude of an object declaration.
     */
    private static final int MAX_OBJ_START = 256;

    /**
     * Regular expression used to parse the prologue of a file-scoped PDF object.
     */
    private static final Pattern OBJ_PROLOGUE = Pattern.compile("(?s)\\G(\\d+)\\s(\\d+)\\sobj\\s*");

    /**
     * Regular expression used to describe an object stream length.
     */
    private static final String REGEX_REFERENCE_DIRECT = "\\d+";

    /**
     * Regular expression used to describe an indirect reference to an object stream length.
     */
    private static final String REGEX_REFERENCE_INDIRECT = "(\\d+) (\\d+) R";

    /**
     * Regular expression used to delimit a PDF object stream (PDF 32000-1:2008, section 7.3.8).
     */
    private static final Pattern OBJ_STREAM_OPEN = Pattern.compile("\\Gstream\\s{1,2}");

    /**
     * Regular expression used to delimit a PDF object stream (PDF 32000-1:2008, section 7.3.8).
     */
    private static final Pattern OBJ_STREAM_CLOSE = Pattern.compile("\\G\\s{0,2}endstream\\s{1,2}endobj\\s");
}
