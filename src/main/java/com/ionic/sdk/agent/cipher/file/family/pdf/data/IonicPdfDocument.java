package com.ionic.sdk.agent.cipher.file.family.pdf.data;

import com.ionic.sdk.agent.cipher.file.family.pdf.body.PdfBodyObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfObjectReader;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfStreamReader;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.Xref;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.XrefTable;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * Ionic extensions to {@link PdfDocument}, allowing retrieval of Ionic payload, if present, from an
 * arbitrary document.
 */
@InternalUseOnly
public final class IonicPdfDocument {

    /**
     * Object encapsulating data and logic associated with a PDF document instance.
     */
    private final PdfDocument pdfDocument;

    /**
     * Constructor.
     *
     * @param channel the {@link java.nio.channels.Channel} interface to the PDF document data
     * @param length  the length (in bytes) of the document content
     */
    public IonicPdfDocument(final SeekableByteChannel channel, final int length) {
        this.pdfDocument = new PdfDocument(channel, length);
    }

    /**
     * Load PDF xref table, which contains index information about document content.
     *
     * @throws IOException    on channel seek / read failure
     * @throws IonicException on data expectation failure
     */
    public void initialize() throws IOException, IonicException {
        pdfDocument.initialize();
    }

    /**
     * @return the container for metadata associated with the PDF document's cross reference table
     */
    public XrefTable getXrefTable() {
        return pdfDocument.getXrefTable();
    }

    /**
     * Find xref corresponding to Ionic payload.
     *
     * @return PDF xref corresponding to Ionic ciphertext payload
     * @throws IOException    on channel seek / read failure
     * @throws IonicException on data expectation failure
     */
    public Xref getXrefIonic() throws IOException, IonicException {
        Xref xrefIonic = null;
        final XrefTable xrefTable = pdfDocument.getXrefTable();
        final PdfDictionary trailerDictionary = xrefTable.getTrailerDictionary();
        SdkData.checkTrue((trailerDictionary != null), SdkError.ISFILECRYPTO_PARSEFAILED);
        // get reference to Ionic info entry
        final String referenceInfo = trailerDictionary.getStringValue(Pdf.KV.INFO);
        final String referenceEncrypt = trailerDictionary.getStringValue(Pdf.KV.ENCRYPT);
        final Xref xrefInfoIndirect = xrefTable.getXrefIndirect(referenceInfo);
        if ((xrefInfoIndirect != null) && xrefInfoIndirect.isInUse() && (referenceEncrypt == null)) {
            // get Ionic "/Info" PDF object dictionary
            final PdfObjectReader pdfObjectReader = pdfDocument.getPdfObjectReader(xrefInfoIndirect.getObjectNumber());
            final PdfBodyObject pdfInfo = pdfObjectReader.readPartial();
            // get reference to Ionic payload
            final String referencePayloadIndirect = pdfInfo.getDictionaryValue(Pdf.KV.IONIC_PAYLOAD);
            final Xref xrefIonicIndirect = xrefTable.getXrefIndirect(referencePayloadIndirect);
            if ((xrefIonicIndirect != null) && xrefIonicIndirect.isInUse()) {
                xrefIonic = xrefTable.get(xrefIonicIndirect.getObjectNumber());
            }
        }
        return xrefIonic;
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
        return pdfDocument.getPdfObjectReader(ordinal);
    }

    /**
     * Given the xref entry associated with the Ionic payload, return the Ionic payload itself.
     *
     * @param xrefIonic PDF xref corresponding to Ionic ciphertext payload
     * @return the bytes associated with the Ionic payload in the PDF object stream
     * @throws IOException    on channel seek / read failure
     * @throws IonicException on data expectation failure
     */
    public byte[] getPayloadIonic(final Xref xrefIonic) throws IOException, IonicException {
        SdkData.checkTrue((xrefIonic != null), SdkError.ISFILECRYPTO_NOEMBED);
        final PdfObjectReader pdfObjectReader = pdfDocument.getPdfObjectReader(xrefIonic.getObjectNumber());
        final PdfBodyObject pdfObjectHeader = pdfObjectReader.readPartial();
        SdkData.checkTrue((pdfObjectHeader != null), SdkError.ISFILECRYPTO_NOEMBED);
        final PdfStreamReader pdfStreamReader = new PdfStreamReader(pdfObjectReader);
        final byte[] pdfStream = pdfStreamReader.readStream(pdfObjectHeader);
        SdkData.checkTrue((pdfStream != null), SdkError.ISFILECRYPTO_NOEMBED);
        return pdfStream;
    }
}
