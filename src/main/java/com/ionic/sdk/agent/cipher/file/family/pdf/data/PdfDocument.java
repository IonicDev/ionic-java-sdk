package com.ionic.sdk.agent.cipher.file.family.pdf.data;

import com.ionic.sdk.agent.cipher.file.family.pdf.body.PdfBodyObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfDictionaryReader;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfObjectReader;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfStreamReader;
import com.ionic.sdk.agent.cipher.file.family.pdf.stream.StreamXrefTableReader;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.Xref;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.XrefTable;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.core.zip.Flate;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.MatchResult;

/**
 * Container for information describing the layout of a PDF document, and logic that extracts this information
 * from the document.
 */
@InternalUseOnly
public final class PdfDocument {

    /**
     * The channel interface through which the PDF document data is accessed.
     */
    private final SeekableByteChannel channel;

    /**
     * The length of the backing PDF document data source.
     */
    private final int length;

    /**
     * The container for metadata associated with the PDF document's cross reference tables.
     */
    private final Collection<XrefTable> xrefTables;

    /**
     * @return the container for metadata associated with the PDF document's cross reference table
     */
    public XrefTable getXrefTable() {
        return xrefTables.isEmpty() ? new XrefTable(0) : xrefTables.iterator().next();
    }

    /**
     * Constructor.
     *
     * @param channel the {@link java.nio.channels.Channel} interface to the PDF document data
     * @param length  the length (in bytes) of the document content
     */
    public PdfDocument(final SeekableByteChannel channel, final int length) {
        this.channel = channel;
        this.length = length;
        this.xrefTables = new ArrayList<XrefTable>();
    }

    /**
     * Load PDF xref tables, which contain index information about document content.
     *
     * @throws IOException    on channel seek / read failure
     * @throws IonicException on data expectation failure
     */
    public void initialize() throws IOException, IonicException {
        SdkData.checkTrue((length > 0), SdkError.ISFILECRYPTO_EOF);
        String offsetXref = getStartXref((int) channel.size());
        while (offsetXref != null) {
            final int offset = Value.toInt(offsetXref, length);
            SdkData.checkTrue((offset >= 0), SdkError.ISFILECRYPTO_PARSEFAILED);
            SdkData.checkTrue((offset < length), SdkError.ISFILECRYPTO_PARSEFAILED);
            final XrefTable xrefTable = loadXrefTable(offset);
            xrefTables.add(xrefTable);
            final PdfDictionary trailerDictionary = xrefTable.getTrailerDictionary();
            offsetXref = trailerDictionary.getStringValue(Pdf.KV.PREV);
        }
    }

    /**
     * The PDF document footer contains data indicating its "active" xref table.  Obtain a reference
     * to the offset of this table.
     * <p>
     * According to the specification (PDF 32000-1:2008, section 7.5.5), the last three logical lines shall contain:
     * <ol>
     * <li>startxref</li>
     * <li>{byte offset of the cross reference table}</li>
     * <li>%%EOF</li>
     * </ol>
     * <p>
     * If any of these lines are missing, the file will be rejected by this reader implementation with the
     * error code {@link com.ionic.sdk.error.AgentErrorModuleConstants#ISAGENT_PARSEFAILED}.
     *
     * @param offsetEnd the location from which to seek backward to find the PDF "startxref" offset
     * @return the data to be parsed to determine the xref table offset
     * @throws IOException    on channel seek / read failure
     * @throws IonicException on data expectation failure
     */
    private String getStartXref(final int offsetEnd) throws IOException, IonicException {
        final int offsetBegin = Math.max(0, (offsetEnd - MAX_STARTXREF));
        final byte[] bytes = new byte[offsetEnd - offsetBegin];
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        channel.position(offsetBegin);
        final int bytesReadStartXref = channel.read(byteBuffer);
        SdkData.checkTrue((bytes.length == bytesReadStartXref), SdkError.ISFILECRYPTO_PARSEFAILED);
        final Scanner scanner = new Scanner(new ByteArrayInputStream(bytes), Pdf.CHARSET);
        final String startxref = scanner.findWithinHorizon(Pdf.Regex.STARTXREF, 0);
        SdkData.checkTrue((startxref != null), SdkError.ISFILECRYPTO_PARSEFAILED);
        return scanner.match().group(1);
    }

    /**
     * Given a starting offset, load the xref table at that starting offset.  This includes the trailing
     * dictionary object found at the end of an xref table.
     *
     * @param offset the offset within the resource of the start of the xref table
     * @return the loaded xref table
     * @throws IOException    on channel seek / read failure
     * @throws IonicException on data expectation failure
     */
    private XrefTable loadXrefTable(final int offset) throws IOException, IonicException {
        channel.position(offset);
        final Scanner scanner = new Scanner(channel, Pdf.CHARSET);
        final String xrefTableHeader = scanner.findWithinHorizon(Pdf.Regex.XREF_TABLE_HEADER, MAX_STARTXREF);
        if (xrefTableHeader == null) {
            // interpret as xref (cross reference) stream (PDF 32000-1:2008, section 7.5.4)
            final Xref xref = new Xref(0, offset, 0, Pdf.XREF_IN_USE, null);
            final PdfObjectReader reader = new PdfObjectReader(channel, xref, null);
            final PdfBodyObject pdfBodyObject = reader.readPartial();
            final String type = pdfBodyObject.getDictionaryValue(Pdf.KV.TYPE);
            final String encrypt = pdfBodyObject.getDictionaryValue(Pdf.KV.ENCRYPT);
            SdkData.checkTrue(Pdf.KV.XREF.equals(type), SdkError.ISFILECRYPTO_PARSEFAILED);
            SdkData.checkTrue(null == encrypt, SdkError.ISFILECRYPTO_UNRECOGNIZED);
            final PdfStreamReader pdfStreamReader = new PdfStreamReader(reader);
            final byte[] pdfStreamRaw = pdfStreamReader.readStream(pdfBodyObject);
            final boolean deflated = Pdf.KV.FLATE_DECODE.equals(pdfBodyObject.getDictionaryValue(Pdf.KV.FILTER));
            final byte[] pdfStream = (deflated ? Flate.inflate(pdfStreamRaw) : pdfStreamRaw);
            // parse stream data into table
            final StreamXrefTableReader xrefTableReader = new StreamXrefTableReader(pdfBodyObject);
            return xrefTableReader.read(offset, channel, pdfStream);
        } else {
            // iterate through data, loading xref table sections, until trailing dictionary is found
            return parseXrefTable(offset, scanner);
        }
    }

    /**
     * Deserialize the xref table from the parameter scanner object.
     *
     * @param offset  the offset within the resource of the start of the xref table
     * @param scanner the I/O object from which to read the xref table data
     * @return the loaded xref table
     * @throws IonicException on data expectation failure
     */
    private XrefTable parseXrefTable(final int offset, final Scanner scanner) throws IonicException {
        final XrefTable xrefTable = new XrefTable(offset);
        boolean foundTrailer = false;
        while (!foundTrailer) {
            if (scanner.findWithinHorizon(Pdf.Regex.XREF_SECTION_HEADER, MAX_XREF_SECTION_HEADER) != null) {
                // load xref table section
                final MatchResult matchXrefSection = scanner.match();
                final int xrefStart = Value.toInt(matchXrefSection.group(1), 0);
                final int xrefCount = Value.toInt(matchXrefSection.group(2), 0);
                for (int i = xrefStart; (i < (xrefStart + xrefCount)); ++i) {
                    final String xrefText = scanner.findWithinHorizon(Pdf.Regex.XREF_ENTRY, SIZE_XREF);
                    SdkData.checkTrue((xrefText != null), SdkError.ISFILECRYPTO_PARSEFAILED);
                    final MatchResult matchXrefItem = scanner.match();
                    final Xref xref = new Xref(matchXrefItem.group(), i);
                    xrefTable.put(i, xref);
                }
            } else if (scanner.findWithinHorizon(Pdf.Regex.TRAILER_HEAD, length) != null) {
                foundTrailer = true;
                // load xref table trailing dictionary
                final PdfDictionaryReader reader = new PdfDictionaryReader(scanner);
                final PdfObject pdfObject = reader.read();
                SdkData.checkTrue((pdfObject instanceof PdfDictionary), SdkError.ISFILECRYPTO_PARSEFAILED);
                xrefTable.setTrailerDictionary((PdfDictionary) pdfObject);
            } else {
                throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
            }
        }
        return xrefTable;
    }

    /**
     * Read the header portion of a PDF object record.
     *
     * @param ordinal the identifier associated with the desired PDF object record
     * @return an object containing associated PDF dictionary data and additional state
     * @throws IOException    on channel seek / read failure
     * @throws IonicException on data expectation failure
     */
    public PdfObjectReader getPdfObjectReader(final int ordinal) throws IOException, IonicException {
        final Xref xref = getXrefIndirectInternal(ordinal);
        final XrefTable xrefTable = xrefTables.isEmpty() ? new XrefTable(0) : xrefTables.iterator().next();
        SdkData.checkTrue((xref != null), SdkError.ISFILECRYPTO_PARSEFAILED);
        return new PdfObjectReader(channel, xref, xrefTable);
    }

    /**
     * Fetch the xref associated with the parameter ordinal.
     *
     * @param ordinal the integer ID of the desired PDF object xref
     * @return the xref with the specified id
     */
    public Xref getXrefIndirect(final int ordinal) {
        return getXrefIndirectInternal(ordinal);
    }

    /**
     * Fetch the xref associated with the parameter ordinal.
     *
     * @param ordinal the integer ID of the desired PDF object xref
     * @return the xref with the specified id
     */
    private Xref getXrefIndirectInternal(final int ordinal) {
        Xref xref = null;
        final Iterator<XrefTable> iterator = xrefTables.iterator();
        while (iterator.hasNext() && (xref == null)) {
            final XrefTable xrefTable = iterator.next();
            xref = xrefTable.get(ordinal);
        }
        return xref;
    }

    /**
     * Define a maximum amount of data (in bytes) to back scan from a channel offset for a "startxref" token.
     */
    private static final int MAX_STARTXREF = 256;

    /**
     * Define a maximum amount of data (in bytes) expected in an xref section header (start index, count).
     */
    private static final int MAX_XREF_SECTION_HEADER = 16;

    /**
     * Define a maximum amount of data (in bytes) expected in an xref entry.
     */
    private static final int SIZE_XREF = 20;
}
