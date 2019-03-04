package com.ionic.sdk.agent.cipher.file.family.pdf.input;

import com.ionic.sdk.agent.cipher.file.PdfFileCipher;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.agent.cipher.file.family.generic.input.GenericInput;
import com.ionic.sdk.agent.cipher.file.family.pdf.body.PdfBodyObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.IonicPdfDocument;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfObjectReader;
import com.ionic.sdk.agent.cipher.file.family.pdf.xref.Xref;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.io.ByteChannelWindow;
import com.ionic.sdk.core.io.ByteQueueInputStream;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Wrap an input stream with logic to manage the Ionic augmentation of the content (header, content representation).
 */
@InternalUseOnly
public final class PdfInput {

    /**
     * The input data channel containing the protected file content.
     */
    private final SeekableByteChannel sourceChannel;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * Helper object, containing state used to read the stream resource.
     */
    private PdfObjectReader pdfObjectReader;

    /**
     * The view in the source channel encompassing the Ionic generic-protected content.
     */
    private ByteChannelWindow byteChannelWindow;

    /**
     * Intermediate buffer holding data from PDF object to be piped through the wrapped cipher.
     */
    private ByteQueueInputStream intermediateStream;

    /**
     * The buffer to hold a ciphertext block (source buffer for decryption, target buffer for encryption).
     */
    private ByteBuffer cipherText;

    /**
     * Wrapped cipher implementing protection of PDF content.
     */
    private GenericInput genericInput;

    /**
     * Constructor.
     *
     * @param sourceChannel the input channel containing the protected file content
     * @param agent         the key services implementation; used to provide keys for cryptography operations
     */
    public PdfInput(final SeekableByteChannel sourceChannel, final KeyServices agent) {
        this.sourceChannel = sourceChannel;
        this.agent = agent;
    }

    /**
     * Initialize this object for processing an Ionic-protected file.
     *
     * @param fileInfo          the structure into which data about the Ionic state of the file should be written
     * @param decryptAttributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on failure to load or parse header, or specification of an unsupported file format, or
     *                        cipher initialization
     * @throws IOException    on failure reading from the stream
     */
    public void init(final FileCryptoFileInfo fileInfo,
                     final FileCryptoDecryptAttributes decryptAttributes) throws IonicException, IOException {
        fileInfo.setCipherFamily(CipherFamily.FAMILY_UNKNOWN);
        fileInfo.setCipherVersion("");
        final IonicPdfDocument ionicPdfDocument = new IonicPdfDocument(sourceChannel, (int) sourceChannel.size());
        ionicPdfDocument.initialize();
        // at this point, we know that the document content is presenting as a PDF (trailer, xref table)
        // mimic the behavior of the core SDK
        fileInfo.setCipherFamily(CipherFamily.FAMILY_PDF);
        fileInfo.setCipherVersion(PdfFileCipher.VERSION_LATEST);
        // scan the document content for Ionic content
        final Xref xrefIonic = ionicPdfDocument.getXrefIonic();
        SdkData.checkTrue((xrefIonic != null), SdkError.ISFILECRYPTO_NOEMBED);
        pdfObjectReader = new PdfObjectReader(sourceChannel, xrefIonic, ionicPdfDocument.getXrefTable());
        final PdfBodyObject pdfBodyObject = pdfObjectReader.readPartial();
        // ascertain the bounds of the Ionic embed content
        final int start = pdfObjectReader.readStreamInit(pdfBodyObject);
        final int length = pdfObjectReader.readStreamLength(pdfBodyObject.getDictionaryValue(Pdf.KV.LENGTH));
        SdkData.checkTrue((length > 0), SdkError.ISFILECRYPTO_PARSEFAILED);
        // initialize state for document read
        byteChannelWindow = new ByteChannelWindow(sourceChannel, start, start + length);
        intermediateStream = new ByteQueueInputStream(FileCipher.Generic.HEADER_SIZE_MAX);
        // load the Ionic header (applicability to "GetFileInfo()" API call)
        final byte[] bytesHeader = new byte[FileCipher.Generic.HEADER_SIZE_MAX];
        final int bytesRead = byteChannelWindow.read(bytesHeader);
        intermediateStream.addBytes(bytesHeader, 0, bytesRead);
        fileInfo.setEncrypted(true);
        genericInput = new GenericInput(intermediateStream, length, agent);
        cipherText = genericInput.getCipherText();
        final FileCryptoFileInfo fileInfoEmbed = new FileCryptoFileInfo();
        genericInput.init(fileInfoEmbed, decryptAttributes);
        fileInfo.setKeyId(fileInfoEmbed.getKeyId());
        fileInfo.setServer(fileInfoEmbed.getServer());
        decryptAttributes.setFamily(CipherFamily.FAMILY_PDF);
        decryptAttributes.setVersion(PdfFileCipher.VERSION_LATEST);
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream
     * without blocking by the next invocation of a method for this input stream.
     *
     * @return an estimate of the number of bytes that can be read (or skipped over) from this input stream
     * without blocking
     * @throws IOException on failure reading from the input channel
     */
    public int available() throws IOException {
        return genericInput.available() + intermediateStream.available() + (int) byteChannelWindow.available();
    }

    /**
     * Read the Ionic-protected content from the input resource body.
     *
     * @return the content extracted from the stream
     * @throws IOException    on failure reading from the input channel
     * @throws IonicException on failure to read from the embedded Ionic data stream
     */
    public ByteBuffer read() throws IOException, IonicException {
        final int blockSize = FileCipher.Generic.V12.BLOCK_SIZE_CIPHER;
        while ((intermediateStream.available() < blockSize) && (byteChannelWindow.available() > 0)) {
            cipherText.clear();
            byteChannelWindow.read(cipherText);
            intermediateStream.addBytes(cipherText);
        }
        return genericInput.read();
    }

    /**
     * Finish processing of the input stream.
     *
     * @throws IonicException on failure to verify the file signature (if present)
     */
    public void doFinal() throws IonicException {
        genericInput.doFinal();
        pdfObjectReader.readStreamWrapup();
    }
}
