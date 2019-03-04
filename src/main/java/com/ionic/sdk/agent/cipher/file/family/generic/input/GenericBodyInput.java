package com.ionic.sdk.agent.cipher.file.family.generic.input;

import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Interface presenting methods to manage decryption of {@link com.ionic.sdk.agent.cipher.file.GenericFileCipher}
 * protected content.
 */
@InternalUseOnly
interface GenericBodyInput {

    /**
     * Initialize this object for processing the body of an Ionic-protected resource.
     *
     * @throws IonicException on failure to read from the stream, or decrypt the resource signature (if present)
     */
    void init() throws IonicException;

    /**
     * Read the next Ionic-protected block from the input resource.
     *
     * @return the next plaintext block extracted from the resource, wrapped in a {@link ByteBuffer} object
     * @throws IOException    on failure reading from the resource
     * @throws IonicException on failure to decrypt the block, or calculate the block signature
     */
    ByteBuffer read() throws IOException, IonicException;

    /**
     * Finish processing of the input resource.
     *
     * @throws IonicException on failure to verify the resource signature (if present)
     */
    void doFinal() throws IonicException;
}
