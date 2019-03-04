package com.ionic.sdk.agent.cipher.file.family.openxml.output;

import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;

import java.io.InputStream;
import java.io.IOException;

/**
 * Interface presenting methods to manage encryption of file content into a
 * {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher} format.
 */
@InternalUseOnly
interface OpenXmlBodyOutput {

    /**
     * Initialize this object for processing the body of an Ionic-protected file.
     *
     * @throws IOException    on failure reading from the stream
     * @throws IonicException on failure to decrypt the file signature (if present)
     */
    void init() throws IOException, IonicException;

    /**
     * Finish processing the input stream into the output stream.
     *
     * @param plainText the output stream presenting the binary plain text output buffer
     * @throws IOException on failure flushing the stream
     * @throws IonicException on failure to encrypt the block, or calculate the block signature
     */
    void doEncryption(InputStream plainText) throws IOException, IonicException;
}
