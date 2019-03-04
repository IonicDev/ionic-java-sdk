package com.ionic.sdk.agent.cipher.file.family.pdf.io;

import com.ionic.sdk.agent.cipher.file.family.pdf.body.PdfBodyObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.Xref;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.XrefTable;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.io.SeekableByteBufferChannel;
import com.ionic.sdk.core.zip.Flate;
import com.ionic.sdk.error.IonicException;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * Utility class for acquiring a channel to an embedded object stream within a PDF object.
 */
@InternalUseOnly
public final class PdfObjStmReader {

    /**
     * The xref that refers to the file-scoped object stream.
     */
    private final Xref xrefWrapper;

    /**
     * The index table for the PDF document, containing references to all of the contained objects.
     */
    private final XrefTable xrefTableWrapper;

    /**
     * Constructor.
     *
     * @param xref      the reference to the file-scoped object stream
     * @param xrefTable the PDF document reference table
     */
    PdfObjStmReader(final Xref xref, final XrefTable xrefTable) {
        this.xrefWrapper = xref;
        this.xrefTableWrapper = xrefTable;
    }

    /**
     * Obtain a reference to the object stream data containing the data associated with the member xref.
     *
     * @param channel the file-scoped channel
     * @return the object stream-scoped channel contained within the PDF object
     * @throws IonicException on expectation failure associated with the PDF object stream wrapper
     * @throws IOException    on failure to read or write to the channel
     */
    public SeekableByteChannel getChannel(final SeekableByteChannel channel) throws IonicException, IOException {
        final int objectNumber = xrefWrapper.getObjStmEntry().getObjectNumber();
        final Xref xref = xrefTableWrapper.get(objectNumber);
        final PdfObjectReader pdfObjectReader = new PdfObjectReader(channel, xref, xrefTableWrapper);
        final PdfBodyObject pdfBodyObject = pdfObjectReader.readPartial();
        final PdfStreamReader pdfStreamReader = new PdfStreamReader(pdfObjectReader);
        final byte[] pdfStreamRaw = pdfStreamReader.readStream(pdfBodyObject);
        final String filter = pdfBodyObject.getDictionaryValue(Pdf.KV.FILTER);
        final boolean deflated = Pdf.KV.FLATE_DECODE.equals(filter);
        final byte[] pdfStream = (deflated ? Flate.inflate(pdfStreamRaw) : pdfStreamRaw);
        return new SeekableByteBufferChannel(pdfStream);
    }
}
