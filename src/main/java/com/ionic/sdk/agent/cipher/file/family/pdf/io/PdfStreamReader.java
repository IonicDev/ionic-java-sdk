package com.ionic.sdk.agent.cipher.file.family.pdf.io;

import com.ionic.sdk.agent.cipher.file.family.pdf.body.PdfBodyObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.Pdf;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.io.ByteChannelWindow;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Object specialized to use {@link PdfObjectReader} APIs to extract the stream resource from a serialized PDF object.
 */
@InternalUseOnly
public final class PdfStreamReader {

    /**
     * Helper object, containing state used to read the stream resource.
     */
    private final PdfObjectReader pdfObjectReader;

    /**
     * Constructor.
     *
     * @param pdfObjectReader helper object, containing state used to read the stream resource
     */
    public PdfStreamReader(final PdfObjectReader pdfObjectReader) {
        this.pdfObjectReader = pdfObjectReader;
    }

    /**
     * A shortcut method to read the entire PDF object stream into memory.
     *
     * @param pdfBodyObject a container for metadata describing the PDF object
     * @return the bytes associated with the PDF object stream
     * @throws IOException    on failure to read or write to the channel
     * @throws IonicException on expectation failure associated with the PDF object stream wrapper
     */
    public byte[] readStream(final PdfBodyObject pdfBodyObject) throws IOException, IonicException {
        final int start = pdfObjectReader.readStreamInit(pdfBodyObject);
        final int length = pdfObjectReader.readStreamLength(pdfBodyObject.getDictionaryValue(Pdf.KV.LENGTH));
        SdkData.checkTrue((length > 0), SdkError.ISFILECRYPTO_PARSEFAILED);
        final int end = start + length;
        final ByteChannelWindow byteChannelWindow = new ByteChannelWindow(pdfObjectReader.getChannel(), start, end);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final byte[] buffer = new byte[BLOCK_SIZE_PLAIN];
        while (byteChannelWindow.available() > 0) {
            final int bytesRead = byteChannelWindow.read(buffer);
            os.write(buffer, 0, bytesRead);
        }
        pdfObjectReader.readStreamWrapup();
        return os.toByteArray();
    }

    /**
     * Block size used by this class to read the PDF stream resource from the input channel.
     */
    public static final int BLOCK_SIZE_PLAIN = 256 * 256;
}
