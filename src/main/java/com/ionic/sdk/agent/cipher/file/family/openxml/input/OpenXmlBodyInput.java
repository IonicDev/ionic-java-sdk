package com.ionic.sdk.agent.cipher.file.family.openxml.input;

import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface presenting methods to manage decryption of <code>GenericFileCipher</code> protected content.
 */
@InternalUseOnly
interface OpenXmlBodyInput {

    /**
     * Initialize this object for processing the body of an Ionic-protected file.
     *
     * @throws IOException    on failure reading from the stream
     * @throws IonicException on failure to parse
     */
    void init() throws IOException, IonicException;

    /**
     * Read the next Ionic-protected block from the input resource.
     *
     * @return the next plaintext block extracted from the resource, wrapped in a {@link ByteBuffer} object
     * @throws IOException    on failure reading from the resource
     * @throws IonicException on failure to decrypt the block, or calculate the block signature
     */
    ByteBuffer read() throws IOException, IonicException;

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream
     * without blocking by the next invocation of a method for this input stream.
     *
     * @return an estimate of the number of bytes that can be read (or skipped over) from this input stream
     * without blocking
     * @throws IOException if this input stream has been closed, or an I/O error occurs
     */
    int available() throws IOException;

    /**
     * Finish processing of the input stream.
     *
     * @throws IOException    on failure closing the stream
     * @throws IonicException on failure closing the stream
     */
    void doFinal() throws IOException, IonicException;
}
