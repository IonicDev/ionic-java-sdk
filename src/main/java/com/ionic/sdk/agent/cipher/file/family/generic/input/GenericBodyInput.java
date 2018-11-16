package com.ionic.sdk.agent.cipher.file.family.generic.input;

import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;

import java.io.IOException;

/**
 * Interface presenting methods to manage decryption of <code>GenericFileCipher</code> protected content.
 */
@InternalUseOnly
interface GenericBodyInput {

    /**
     * Initialize this object for processing the body of an Ionic-protected file.
     *
     * @throws IonicException on failure to read from the stream, or decrypt the file signature (if present)
     */
    void init() throws IonicException;

    /**
     * Read the next Ionic-protected block from the input resource.
     *
     * @return the next plainText block extracted from the stream
     * @throws IOException    on failure reading from the stream
     * @throws IonicException on failure to decrypt the block, or calculate the block signature
     */
    byte[] read() throws IOException, IonicException;

    /**
     * Finish processing of the input stream.
     *
     * @throws IonicException on failure to verify the file signature (if present)
     */
    void doFinal() throws IonicException;
}
