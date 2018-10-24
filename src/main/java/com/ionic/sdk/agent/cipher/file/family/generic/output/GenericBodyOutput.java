package com.ionic.sdk.agent.cipher.file.family.generic.output;

import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;

import java.io.IOException;

/**
 * Interface presenting methods to manage encryption of file content into a
 * {@link com.ionic.sdk.agent.cipher.file.GenericFileCipher} format.
 */
@InternalUseOnly
interface GenericBodyOutput {

    /**
     * Initialize this object for processing the body of an Ionic-protected file.
     *
     * @throws IOException    on failure reading from the stream
     * @throws IonicException on failure to decrypt the file signature (if present)
     */
    void init() throws IOException, IonicException;

    /**
     * Each generic file format specifies a plain text block length.  This is the amount of plain text that
     * constitutes a block on which an encrypt operation is performed.
     *
     * @return the amount of plain text that should be converted to cipher text in a single operation
     */
    int getBlockLengthPlain();

    /**
     * Write the next Ionic-protected block to the output resource.
     *
     * @param block the next plainText block to be written to the stream
     * @throws IOException    on failure writing to the stream
     * @throws IonicException on failure to encrypt the block, or calculate the block signature
     */
    void write(byte[] block) throws IOException, IonicException;

    /**
     * Finish processing of the output stream.
     */
    void doFinal();

    /**
     * Retrieve the calculated file signature for the output.  This is inserted into the file content immediately
     * after the Ionic file header.
     *
     * @return the Ionic-protected signature bytes associated with the output
     * @throws IonicException on failure to calculate the file signature (if present)
     */
    byte[] getSignature() throws IonicException;
}
