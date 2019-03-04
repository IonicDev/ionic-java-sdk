package com.ionic.sdk.agent.cipher.file.family.pdf.output;

import com.ionic.sdk.agent.cipher.file.PdfFileCipher;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesInterface;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.agent.cipher.file.family.pdf.body.PdfBodyObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfDictionary;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfDocument;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfString;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfObjectReader;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfObjectWriter;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.Xref;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.XrefTable;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.XrefTableWriter;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.SeekableByteBufferChannel;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Wrap an output stream with logic to manage the Ionic augmentation of the content.
 */
@InternalUseOnly
public final class PdfOutput {

    /**
     * The buffered output data stream that is to contain the protected file content.  Output is written to this
     * stream for efficiency.
     */
    private final BufferedOutputStream targetStream;

    /**
     * The length of the resource to be encrypted.
     */
    private final long sizeInput;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * Cover page services implementation; used to substitute cover pages to display on failure to access crypto key.
     */
    private final FileCryptoCoverPageServicesInterface coverPageServices;

    /**
     * The cipher family implementation for managing the file body content for the specified version.
     */
    private PdfBodyOutput bodyOutput;

    /**
     * Container for metadata derived from Ionic document cover page.
     */
    private PdfDocument documentCoverPage;

    /**
     * The PDF xrefs associated with the Ionic encryption wrapper for the source document.
     */
    private final Collection<Xref> xrefsIonic;

    /**
     * Cache value of reference in "init()", to be added to trailer dictionary in "doFinal()".
     */
    private Xref referenceInfoIonic;

    /**
     * The running count of the number of bytes written to the cipher text stream.
     */
    private int sizeDocumentWrite;

    /**
     * The position in the PDF output stream at which the generic file signature should be written.
     */
    private int signatureOffset;

    /**
     * The position in the output stream of the Ionic ciphertext payload PDF object.  This is cached so that the
     * length of the ciphertext may be updated in the payload dictionary after the length is known.
     */
    private int offsetPayloadIonic;

    /**
     * Cached copy of the Ionic payload PDF object.  This is held so that the prologue dictionary may be rewritten
     * once the ciphertext length is known.
     */
    private PdfBodyObject bodyObjectPayload;

    /**
     * Constructor.
     *
     * @param outputStream      the raw output data that will contain the protected file content
     * @param sizeInput         the length of the resource to be encrypted
     * @param agent             the key services implementation; used to provide keys for cryptography operations
     * @param coverPageServices the cover page services implementation
     */
    public PdfOutput(final OutputStream outputStream, final long sizeInput, final KeyServices agent,
                     final FileCryptoCoverPageServicesInterface coverPageServices) {
        this.targetStream = new BufferedOutputStream(outputStream);
        this.sizeInput = sizeInput;
        this.agent = agent;
        this.coverPageServices = coverPageServices;
        this.xrefsIonic = new ArrayList<Xref>();
        this.referenceInfoIonic = null;
        this.sizeDocumentWrite = 0;
        this.signatureOffset = 0;
        this.offsetPayloadIonic = 0;
    }

    /**
     * Initialize this object for processing the body of an Ionic-protected file.
     *
     * @param encryptAttributes a container for applying desired configuration to the operation,
     *                          and receiving status of the operation
     * @throws IOException    on failure writing to the stream
     * @throws IonicException on:
     *                        <ul>
     *                        <li>incorrect / missing Ionic file version</li>
     *                        <li>cover page fetch failure</li>
     *                        <li>key creation failure</li>
     *                        <li>failure writing to output stream</li>
     *                        </ul>
     */
    public void init(final FileCryptoEncryptAttributes encryptAttributes) throws IonicException, IOException {
        final String version = Value.defaultOnEmpty(encryptAttributes.getVersion(), PdfFileCipher.VERSION_LATEST);
        encryptAttributes.setFamily(CipherFamily.FAMILY_PDF);
        encryptAttributes.setVersion(version);
        SdkData.checkTrue(!Value.isEmpty(version), SdkError.ISFILECRYPTO_MISSINGVALUE);
        final byte[] coverPage = coverPageServices.getCoverPage(FileType.FILETYPE_PDF);
        targetStream.write(coverPage);
        sizeDocumentWrite += coverPage.length;
        // load cover page (some Ionic content derived from it)
        final SeekableByteBufferChannel channelCoverPage = new SeekableByteBufferChannel(coverPage);
        documentCoverPage = new PdfDocument(channelCoverPage, coverPage.length);
        documentCoverPage.initialize();
        // copy zero xref from cover page
        final XrefTable xrefTable = documentCoverPage.getXrefTable();
        final Collection<Xref> xrefsTable = xrefTable.values();
        if (!xrefsTable.isEmpty()) {
            xrefsIonic.add(xrefsTable.iterator().next());
        }
        // the cover page trailer dictionary forms the basis of the Ionic "info" PDF object in the ciphertext
        final PdfDictionary trailerDictionary = xrefTable.getTrailerDictionary();
        SdkData.checkTrue(trailerDictionary != null, SdkError.ISFILECRYPTO_PARSEFAILED);
        final int size = Value.toInt(trailerDictionary.getStringValue(Pdf.KV.SIZE), 0);
        final String referenceInfo = trailerDictionary.getStringValue(Pdf.KV.INFO);
        final int referenceId = Xref.getIdentifier(referenceInfo);
        final Xref xrefInfo = documentCoverPage.getXrefIndirect(referenceId);
        final int objNumInfoIonic = (xrefInfo == null) ? size : xrefInfo.getObjectNumber();
        // the generation number is incremented so as not to conflict with the previous "info" PDF object
        final int genNumInfoIonic = (xrefInfo == null) ? 0 : xrefInfo.getGenerationNum() + 1;
        final Xref xrefInfoIonic = new Xref(objNumInfoIonic, coverPage.length, genNumInfoIonic, Pdf.XREF_IN_USE, null);
        xrefsIonic.add(xrefInfoIonic);
        referenceInfoIonic = (xrefInfo == null) ? xrefInfoIonic : xrefInfo;
        final int objNumPayloadIonic = (xrefInfo == null) ? size + 1 : size;
        final PdfDictionary dictionaryInfo;
        if (xrefInfo == null) {
            dictionaryInfo = new PdfDictionary();
            dictionaryInfo.put(Pdf.KV.INFO, new PdfString(Value.join(
                    Pdf.Token.SPACER, objNumInfoIonic, xrefInfoIonic.getGenerationNum(), Pdf.REFERENCE)));
            dictionaryInfo.put(Pdf.KV.SIZE, new PdfString(Integer.toString(objNumPayloadIonic + 1)));
        } else {
            final PdfObjectReader readerInfo = documentCoverPage.getPdfObjectReader(xrefInfo.getObjectNumber());
            final PdfBodyObject bodyInfo = readerInfo.readPartial();
            dictionaryInfo = bodyInfo.getDictionary();
        }
        SdkData.checkTrue(dictionaryInfo != null, SdkError.ISFILECRYPTO_PARSEFAILED);
        // add the trailer dictionary reference to the Ionic payload
        dictionaryInfo.put(Pdf.KV.IONIC_PAYLOAD, new PdfString(Value.join(
                Pdf.Token.SPACER, objNumPayloadIonic, 0, Pdf.REFERENCE)));
        //dictionaryInfo.remove(Pdf.KV.LENGTH);
        // serialize the Ionic "info" PDF object
        final PdfBodyObject bodyObjectInfo = new PdfBodyObject(xrefInfoIonic, dictionaryInfo, 0, null);
        final ByteArrayOutputStream osInfo = new ByteArrayOutputStream();
        final PdfObjectWriter writerInfo = new PdfObjectWriter(osInfo, bodyObjectInfo, false);
        writerInfo.writeBegin();
        writerInfo.writeEnd();
        targetStream.write(osInfo.toByteArray());
        sizeDocumentWrite += osInfo.size();
        // serialize the Ionic "payload" PDF object
        offsetPayloadIonic = coverPage.length + osInfo.size();
        final Xref xrefPayloadIonic = new Xref(objNumPayloadIonic, offsetPayloadIonic, 0, Pdf.XREF_IN_USE, null);
        xrefsIonic.add(xrefPayloadIonic);
        final PdfDictionary dictionaryPayload = new PdfDictionary();
        // placeholder for size of GenericFileCipher payload
        dictionaryPayload.put(Pdf.KV.LENGTH, new PdfString(String.format(Pdf.LENGTH_FORMAT, 0)));
        bodyObjectPayload = new PdfBodyObject(xrefPayloadIonic, dictionaryPayload, -1, null);
        if (FileCipher.Pdf.V10.LABEL.equals(version)) {
            bodyOutput = new Pdf10BodyOutput(targetStream, sizeInput, agent, encryptAttributes, bodyObjectPayload);
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
        // save the seek location of the generic signature (to be written after the encryption operation completes)
        signatureOffset = sizeDocumentWrite;
        sizeDocumentWrite += bodyOutput.init();
        signatureOffset += bodyOutput.getSignatureOffset();
    }

    /**
     * The embedded (Ionic generic) file format specifies a plain text block length.  This is the amount of plain
     * text that constitutes a block on which an encrypt operation is performed.
     *
     * @return the amount of plain text that should be converted to cipher text in a single operation
     */
    public int getBlockLengthPlain() {
        return bodyOutput.getBlockLengthPlain();
    }

    /**
     * @return the {@link ByteBuffer} allocated to hold a plaintext block for this cryptography operation
     */
    public ByteBuffer getPlainText() {
        return bodyOutput.getPlainText();
    }

    /**
     * Write the next Ionic-protected block to the output resource.
     *
     * @param byteBuffer the next plainText block to be written to the stream
     * @throws IOException    on failure writing to the stream
     * @throws IonicException on failure to encrypt the block, or calculate the block signature
     */
    public void write(final ByteBuffer byteBuffer) throws IOException, IonicException {
        sizeDocumentWrite += bodyOutput.write(byteBuffer);
    }

    /**
     * Finish processing of the output stream.
     *
     * @throws IOException    on failure flushing the stream
     * @throws IonicException on missing data
     */
    public void doFinal() throws IOException, IonicException {
        // flush the embedded generic document to the output stream
        sizeDocumentWrite += bodyOutput.doFinal();
        // write the PDF epilogue for the ciphertext output stream
        final ByteArrayOutputStream osEpiloguePdf = new ByteArrayOutputStream();
        osEpiloguePdf.write(Transcoder.utf8().decode(Pdf.Token.XREF));
        final XrefTableWriter xrefTableWriter = new XrefTableWriter(osEpiloguePdf);
        for (Xref xref : xrefsIonic) {
            // as Ionic xrefs are expected to be sparse, each is serialized into a discrete xref table
            final XrefTable xrefTable = new XrefTable(0);
            xrefTable.put(xref.getObjectNumber(), xref);
            xrefTableWriter.write(xref.getObjectNumber(), xrefTable);
        }
        // continuation of PDF epilogue; trailer dictionary inherited from cover page
        final XrefTable xrefTable = documentCoverPage.getXrefTable();
        final PdfDictionary trailerDictionary = xrefTable.getTrailerDictionary();
        final PdfObject encrypt = trailerDictionary.remove(Pdf.KV.ENCRYPT);
        final int sizeOld = Value.toInt(trailerDictionary.getStringValue(Pdf.KV.SIZE), 0);
        final int size = (encrypt == null) ? sizeOld : sizeOld - 1;
        SdkData.checkTrue(referenceInfoIonic != null, SdkError.ISFILECRYPTO_MISSINGVALUE);
        trailerDictionary.put(Pdf.KV.INFO, new PdfString(referenceInfoIonic.toReference()));
        trailerDictionary.put(Pdf.KV.PREV, new PdfString(Integer.toString(xrefTable.getOffset())));
        trailerDictionary.put(Pdf.KV.SIZE, new PdfString(Integer.toString(size + 1))); // original + ciphertext payload
        osEpiloguePdf.write(Transcoder.utf8().decode(Pdf.Token.TRAILER));
        xrefTableWriter.write(trailerDictionary);
        final PrintStream printStream = new PrintStream(osEpiloguePdf, false, Pdf.CHARSET);
        printStream.print(Pdf.Token.STARTXREF);
        printStream.print(sizeDocumentWrite);
        printStream.print(Pdf.Token.EOL);
        printStream.print(Pdf.Token.EOF);
        targetStream.write(osEpiloguePdf.toByteArray());
        targetStream.flush();
    }

    /**
     * Retrieve the calculated file signature for the output.  This is inserted into the file content immediately
     * after the Ionic generic file header.
     *
     * @return the Ionic-protected signature bytes associated with the output
     * @throws IonicException on failure to calculate the file signature (if present)
     */
    public byte[] getSignature() throws IonicException {
        return bodyOutput.getSignature();
    }

    /**
     * The PDF document contains an Ionic payload PDF object.  This object includes a PDF stream with the Ionic
     * ciphertext, formatted as a {@link com.ionic.sdk.agent.cipher.file.GenericFileCipher} byte stream.  This
     * function returns the offset in the PDF stream at which the generic file signature should be written.
     *
     * @return the position in the PDF output stream at which the generic file signature should be written
     */
    public int getSignatureOffset() {
        return signatureOffset;
    }

    /**
     * Serialize the beginning bytes of the Ionic ciphertext payload PDF object, up to the beginning of the
     * ciphertext stream.  This is written to the PDF stream (in the correct location) after the length is known.
     *
     * @return the beginning bytes of the Ionic ciphertext payload PDF object
     * @throws IonicException on failure to initialize this object for character-based writes
     */
    public byte[] getIonicPayloadPrologue() throws IonicException {
        final int sizeCipherText = bodyOutput.getOutputLength();
        final PdfDictionary dictionary = bodyObjectPayload.getDictionary();
        SdkData.checkTrue(dictionary != null, SdkError.ISFILECRYPTO_PARSEFAILED);
        dictionary.put(Pdf.KV.LENGTH,
                new PdfString(String.format(Pdf.LENGTH_FORMAT, sizeCipherText)));
        final PdfBodyObject bodyObject = new PdfBodyObject(bodyObjectPayload.getXref(),
                bodyObjectPayload.getDictionary(), bodyObjectPayload.getOffsetStream(),
                bodyObjectPayload.getPrologue());
        try {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final PdfObjectWriter writer = new PdfObjectWriter(os, bodyObject, true);
            writer.writeBegin();
            return os.toByteArray();
        } catch (UnsupportedEncodingException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_IOSTREAM_ERROR, e);
        }
    }

    /**
     * @return the position in the output stream of the Ionic ciphertext payload PDF object
     */
    public int getOffsetPayloadIonic() {
        return offsetPayloadIonic;
    }
}
