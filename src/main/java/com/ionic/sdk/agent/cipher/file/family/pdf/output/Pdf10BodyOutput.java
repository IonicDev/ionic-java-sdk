package com.ionic.sdk.agent.cipher.file.family.pdf.output;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.family.generic.output.GenericOutput;
import com.ionic.sdk.agent.cipher.file.family.pdf.body.PdfBodyObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.io.PdfObjectWriter;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Extensions for handling output of {@link com.ionic.sdk.agent.cipher.file.PdfFileCipher}
 * version 1.0 file body content.
 */
@InternalUseOnly
public final class Pdf10BodyOutput implements PdfBodyOutput {

    /**
     * The raw output data stream that is to contain the protected file content.
     */
    private final BufferedOutputStream targetStream;

    /**
     * Parameters to be associated with the encrypt operation.
     */
    private final FileCryptoEncryptAttributes encryptAttributes;

    /**
     * The business logic class for serializing a PDF object.
     */
    private final PdfObjectWriter writer;

    /**
     * The amount of data in the PDF object stream prologue.  The offset of the generic payload can be calculated
     * using this.
     */
    private int pdfObjectBegin;

    /**
     * Wrapped cipher implementing protection of PDF content.
     */
    private final GenericOutput genericOutput;

    /**
     * Constructor.
     *
     * @param targetStream      the raw output data stream that is to contain the protected file content
     * @param sizeInput         the length of the resource to be encrypted
     * @param agent             the key services implementation; used to provide keys for cryptography operations
     * @param encryptAttributes a container for applying desired configuration to the operation,
     *                          and receiving status of the operation
     * @param bodyObjectPayload the PDF object to be written to the stream
     * @throws UnsupportedEncodingException on failure to initialize this object for character-based writes
     */
    Pdf10BodyOutput(final BufferedOutputStream targetStream, final long sizeInput, final KeyServices agent,
                    final FileCryptoEncryptAttributes encryptAttributes, final PdfBodyObject bodyObjectPayload)
            throws UnsupportedEncodingException {
        this.targetStream = targetStream;
        this.encryptAttributes = encryptAttributes;
        this.writer = new PdfObjectWriter(targetStream, bodyObjectPayload, true);
        this.genericOutput = new GenericOutput(targetStream, sizeInput, agent);
    }

    @Override
    public int init() throws IOException, IonicException {
        pdfObjectBegin = writer.writeBegin();
        final FileCryptoEncryptAttributes encryptAttributesWrapped =
                new FileCryptoEncryptAttributes(FileCipher.Generic.V12.LABEL);
        encryptAttributesWrapped.setKeyAttributes(encryptAttributes.getKeyAttributes());
        encryptAttributesWrapped.setMutableKeyAttributes(encryptAttributes.getMutableKeyAttributes());
        encryptAttributesWrapped.setMetadata(encryptAttributes.getMetadata());
        final int wrappedInit = genericOutput.init(encryptAttributesWrapped);
        targetStream.flush();
        encryptAttributes.setKeyResponse(encryptAttributesWrapped.getKeyResponse());
        return pdfObjectBegin + wrappedInit;
    }

    @Override
    public int getBlockLengthPlain() {
        return genericOutput.getBlockLengthPlain();
    }

    @Override
    public int write(final ByteBuffer byteBuffer) throws IOException, IonicException {
        return genericOutput.write(byteBuffer);
    }

    @Override
    public int doFinal() {
        return writer.writeEnd();
    }

    @Override
    public byte[] getSignature() throws IonicException {
        return genericOutput.getSignature();
    }

    @Override
    public int getSignatureOffset() {
        return pdfObjectBegin + genericOutput.getSignatureOffset();
    }

    @Override
    public int getOutputLength() {
        return genericOutput.getOutputLength();
    }

    @Override
    public ByteBuffer getPlainText() {
        return genericOutput.getPlainText();
    }
}
