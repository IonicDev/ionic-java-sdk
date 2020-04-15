package com.ionic.sdk.agent.cipher.file.family.pdf.stream;

import com.ionic.sdk.agent.cipher.file.family.pdf.body.PdfBodyObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfDictionary;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfObjectReader;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfStreamReader;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.Xref;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.XrefTable;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.io.SeekableByteBufferChannel;
import com.ionic.sdk.core.value.BytesReader;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.core.zip.Flate;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * Utility class to deserialize a PDF file cross-reference stream (PDF 32000-1:2008, section 7.5.8).
 * <p>
 * Derived from core SDK:
 * "IonicAgents\SDK\ISAgentSDK\ISFileCryptoLib\ISFileCryptoCipherPdfUtils.cpp::processDecodeParmsForFlateDecode()"
 */
@InternalUseOnly
public final class StreamXrefTableReader {

    /**
     * The PDF metadata associated with the cross-reference stream object.
     */
    private final PdfBodyObject pdfBodyObject;

    /**
     * Constructor.
     *
     * @param pdfBodyObject the PDF metadata associated with the cross-reference stream object
     */
    public StreamXrefTableReader(final PdfBodyObject pdfBodyObject) {
        this.pdfBodyObject = pdfBodyObject;
    }

    /**
     * Parse the bytes associated with the cross-reference stream into a table containing the xref entries.
     *
     * @param offset  the byte offset in the parameter channel at which the xref table begins
     * @param channel the source channel of the PdfDocument; this is used to resolve object stream references
     * @param bytes   the serialized representation of the cross-reference stream
     * @return the table containing the xref entries
     * @throws IonicException on failure to parse the specified data as an XrefTable
     * @throws IOException    on channel seek / read failure
     */
    public XrefTable read(final int offset, final SeekableByteChannel channel, final byte[] bytes)
            throws IonicException, IOException {
        // process metadata descriptor for cross-reference stream
        final PdfDictionary dictionary = pdfBodyObject.getDictionary();
        SdkData.checkTrue(dictionary != null, SdkError.ISFILECRYPTO_PARSEFAILED);
        // storage for object numbers to be stitched into XrefTable entries
        final Collection<Integer> objnums = new ArrayList<Integer>();
        // index (if present) contains the starting object id, as well as the count of referenced table objects
        final List<PdfObject> index = dictionary.getArrayValue(Pdf.KV.INDEX);
        if (index != null) {
            SdkData.checkTrue(((index.size() % 2) == 0), SdkError.ISFILECRYPTO_PARSEFAILED);
            final Iterator<PdfObject> it = index.iterator();
            while (it.hasNext()) {
                final int start = Value.toInt(it.next().toString(), 0);
                final int count = Value.toInt(it.next().toString(), 0);
                for (int i = 0; (i < count); ++i) {
                    objnums.add(start + i);
                }
            }
        } else {
            // if index not present, infer "count" from value of size parameter
            final String size = dictionary.getStringValue(Pdf.KV.SIZE);
            final int start = 0;
            final int count = Value.toInt(size, 0);
            for (int i = 0; (i < count); ++i) {
                objnums.add(start + i);
            }
        }
        // PDF "W" value describes the width of the fields in a single cross-reference table entry
        //   see (PDF 32000-1:2008, section 7.5.8.2)
        final int countFieldsValueW = 3;
        final List<PdfObject> array = dictionary.getArrayValue(Pdf.KV.W);
        SdkData.checkTrue((array != null), SdkError.ISFILECRYPTO_PARSEFAILED);
        SdkData.checkTrue(countFieldsValueW == array.size(), SdkError.ISFILECRYPTO_PARSEFAILED);
        final Iterator<PdfObject> it = array.iterator();
        final int field1Width = Value.toInt(it.next().toString(), 0);
        final int field2Width = Value.toInt(it.next().toString(), 0);
        final int field3Width = Value.toInt(it.next().toString(), 0);
        // calculate/validate the size of the table data
        final PdfDictionary decodeParms = (PdfDictionary) dictionary.get(Pdf.KV.DECODE_PARMS);
        final boolean isDecodeParms = (decodeParms != null);
        final int columnsFields = field1Width + field2Width + field3Width;
        final int columnsRow = columnsFields + (isDecodeParms ? 1 : 0);
        final int rows = bytes.length / columnsRow;
        SdkData.checkTrue(bytes.length == (rows * columnsRow), SdkError.ISFILECRYPTO_PARSEFAILED);
        SdkData.checkTrue(objnums.size() == rows, SdkError.ISFILECRYPTO_PARSEFAILED);
        final int columnsRecord = columnsRow + (isDecodeParms ? 0 : 1);
        // iterate through the entries, adding each to table
        final XrefTable xrefTable = new XrefTable(offset);
        byte[] recordLast = new byte[columnsRecord];  // used by data compression algorithm
        for (int cursor = 0; (cursor < bytes.length); cursor += columnsRow) {
            final byte[] record = new byte[columnsRecord];
            System.arraycopy(bytes, cursor, record, (isDecodeParms ? 0 : 1), columnsRow);
            applyCompressionTransform(record, recordLast);
            final Integer objnum = objnums.iterator().next();
            objnums.remove(objnum);
            processXref(record, objnum, xrefTable, field1Width, field2Width, field3Width);
            recordLast = record;
        }
        // wrapup, propagate object dictionary to table
        xrefTable.setTrailerDictionary(dictionary);
        // wrapup, resolve any reference to object streams that were found in this table
        resolveObjStmOffsets(channel, xrefTable);
        return xrefTable;
    }

    /**
     * Decode PNG encoded table to obtain true values.
     * <p>
     * See also: <a href='https://en.wikipedia.org/wiki/Portable_Network_Graphics#Filtering'
     * target='_blank'>PNG filtering</a>
     *
     * @param record     the bytes associated with one serialized xref record
     * @param recordLast the previous xref record (used by decompression filter, which saves offsets from previous)
     * @throws IonicException on unexpected record data
     */
    private void applyCompressionTransform(final byte[] record, final byte[] recordLast) throws IonicException {
        final byte predictor = record[0];
        switch (predictor) {
            case 0:  // None
                break;
            case 1:  // Sub
                for (int i = 1; (i < record.length); ++i) {
                    record[i] += record[i - 1];
                }
                break;
            case 2:  // Up
                for (int i = 1; (i < record.length); ++i) {
                    record[i] += recordLast[i];
                }
                break;
            default:
                throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED,
                        new IllegalArgumentException(String.format("XREF STREAM, predictor=%d", predictor)));
        }
    }

    /**
     * Add xref data from raw xref record to parameter table.
     *
     * @param record      the bytes associated with one serialized xref record
     * @param objnum      the PDF object number associated with the record
     * @param xrefTable   the table into which the record should be inserted
     * @param field1Width the width of the first field in each cross reference stream table entry
     * @param field2Width the width of the second field in each cross reference stream table entry
     * @param field3Width the width of the third field in each cross reference stream table entry
     * @throws IonicException on unexpected record data
     */
    private void processXref(
            final byte[] record, final int objnum, final XrefTable xrefTable,
            final int field1Width, final int field2Width, final int field3Width) throws IonicException {
        final int offsetField1 = 1;
        final int offsetField2 = offsetField1 + field1Width;
        final int offsetField3 = offsetField2 + field2Width;
        final int field1 = BytesReader.readInt(record, offsetField1, (offsetField1 + field1Width));
        final int field2 = BytesReader.readInt(record, offsetField2, (offsetField2 + field2Width));
        final int field3 = BytesReader.readInt(record, offsetField3, (offsetField3 + field3Width));
        final String trace = String.format(FORMAT_XREF_TRACE, objnum, field1, field2, field3);
        //Logger.getLogger(getClass().getName()).finest(trace);
        switch (field1) {
            case 0:
                break;
            case 1:
                xrefTable.put(objnum, new Xref(objnum, field2, 0, Pdf.XREF_IN_USE, null));
                break;
            case 2:
                // placeholder object (ObjStm offset to be resolved in post-processing)
                xrefTable.put(objnum, new Xref(objnum, 0, 0, Pdf.XREF_IN_USE, new ObjStmEntry(field2, field3)));
                break;
            default:
                throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED, new IllegalArgumentException(trace));
        }
    }

    /**
     * Format string used to trace content of decoded cross reference stream table entry.
     */
    private static final String FORMAT_XREF_TRACE = "objnum=%d field1=%d, field2=%d, field3=%d";

    /**
     * Read the referenced ObjStm records in order to resolve the offsets of any wrapped PDF document objects.
     *
     * @param channel   the interface to the PDF document data
     * @param xrefTable the table containing references to the PDF objects within the document
     * @throws IonicException on unexpected record data
     * @throws IOException    on failure to read or write to the channel
     */
    private void resolveObjStmOffsets(final SeekableByteChannel channel, final XrefTable xrefTable)
            throws IonicException, IOException {
        // find all object streams referenced in this XrefTable
        final Collection<Integer> objStms = new TreeSet<Integer>();
        for (Xref xref : xrefTable.values()) {
            final ObjStmEntry objStmEntry = xref.getObjStmEntry();
            if (objStmEntry != null) {
                objStms.add(objStmEntry.getObjectNumber());
            }
        }
        // for each object stream, load in order to parse the prologue content (containing the object offsets)
        for (Integer objStmIt : objStms) {
            final Xref xref = xrefTable.get(objStmIt);
            SdkData.checkTrue(xref != null, SdkError.ISFILECRYPTO_PARSEFAILED);
            // read object stream
            final PdfObjectReader objectReaderIt = new PdfObjectReader(channel, xref, null);
            final PdfBodyObject objectIt = objectReaderIt.readPartial();
            final int firstIt = Value.toInt(objectIt.getDictionaryValue(Pdf.KV.FIRST), 0);
            final PdfStreamReader streamReaderIt = new PdfStreamReader(objectReaderIt);
            final byte[] pdfStreamRawIt = streamReaderIt.readStream(objectIt);
            final boolean deflatedIt = Pdf.KV.FLATE_DECODE.equals(objectIt.getDictionaryValue(Pdf.KV.FILTER));
            final byte[] pdfStreamIt = (deflatedIt ? Flate.inflate(pdfStreamRawIt) : pdfStreamRawIt);
            // iterate through object stream header to find offset of each entry
            final SeekableByteBufferChannel channelIt = new SeekableByteBufferChannel(pdfStreamIt);
            final Scanner scannerIt = new Scanner(channelIt, Pdf.CHARSET);
            final int horizonEntry = 32;
            while (scannerIt.findWithinHorizon(Pdf.Regex.OBJSTM_ENTRY, horizonEntry) != null) {
                final int objnumIt = Value.toInt(scannerIt.match().group(1), 0);
                final int offsetIt = firstIt + Value.toInt(scannerIt.match().group(2), 0);
                final Xref xrefIt = xrefTable.get(objnumIt);
                SdkData.checkTrue(xrefIt != null, SdkError.ISFILECRYPTO_PARSEFAILED);
                final ObjStmEntry objStmEntryIt = xrefIt.getObjStmEntry();
                SdkData.checkTrue(objStmEntryIt != null, SdkError.ISFILECRYPTO_PARSEFAILED);
                SdkData.checkTrue(objStmEntryIt.getObjectNumber() == objStmIt, SdkError.ISFILECRYPTO_PARSEFAILED);
                xrefIt.setOffset(offsetIt);
            }
        }
    }
}
