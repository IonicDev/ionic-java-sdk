package com.ionic.sdk.agent.cipher.file.family.pdf.output;

import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface presenting methods to manage encryption of file content into a
 * {@link com.ionic.sdk.agent.cipher.file.PdfFileCipher} format.
 */
@InternalUseOnly
interface PdfBodyOutput {

    /**
     * Initialize this object for processing the body of an Ionic-protected file.
     *
     * @return the number of bytes written in the context of this call
     * @throws IOException    on failure reading from the stream
     * @throws IonicException on failure to decrypt the file signature (if present)
     */
    int init() throws IOException, IonicException;

    /**
     * The embedded generic file format specifies a plain text block length.  This is the amount of plain text that
     * constitutes a block on which an encrypt operation is performed.
     *
     * @return the amount of plain text that should be converted to cipher text in a single operation
     */
    int getBlockLengthPlain();

    /**
     * @return the {@link ByteBuffer} allocated to hold a plaintext block for this cryptography operation
     */
    ByteBuffer getPlainText();

    /**
     * Write the next Ionic-protected block to the output resource.
     *
     * @param byteBuffer the next plainText block to be written to the stream
     * @return the number of bytes written in the context of this call
     * @throws IOException    on failure writing to the stream
     * @throws IonicException on failure to encrypt the block, or calculate the block signature
     */
    int write(ByteBuffer byteBuffer) throws IOException, IonicException;

    /**
     * Finish processing of the output stream.
     *
     * @return the number of bytes written in the context of this call
     * @throws IOException on failure writing to the stream
     */
    int doFinal() throws IOException;

    /**
     * Retrieve the calculated file signature for the output.  This is inserted into the file content immediately
     * after the Ionic generic file header.
     *
     * @return the Ionic-protected signature bytes associated with the output
     * @throws IonicException on failure to calculate the file signature (if present)
     */
    byte[] getSignature() throws IonicException;

    /**
     * The length of the Ionic file header.  We cache this position so we can seek in the content to the
     * correct position at which the file signature should be written.
     *
     * @return the number of bytes in the Ionic file header
     */
    int getSignatureOffset();

    /**
     * The length of the output of the wrapped encryption operation.  This is tracked so that it may be available to
     * this wrapping cipher.
     *
     * @return the number of bytes in the wrapped generic ciphertext stream
     */
    int getOutputLength();
}
