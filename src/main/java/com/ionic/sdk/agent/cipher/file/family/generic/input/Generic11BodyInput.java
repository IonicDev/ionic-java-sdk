package com.ionic.sdk.agent.cipher.file.family.generic.input;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * GenericFileCipher version 1.1 extensions for handling the file body content.
 */
@InternalUseOnly
final class Generic11BodyInput implements GenericBodyInput {

    /**
     * The raw input data stream containing the protected file content.
     */
    private final BufferedInputStream sourceStream;

    /**
     * The Ionic cipher used to encrypt file blocks.
     */
    private final AesCtrCipher cipher;

    /**
     * Constructor.
     *
     * @param sourceStream the raw input data containing the protected file content
     * @param key          the cryptography key used to decrypt and verify the file content
     * @throws IonicException on cipher initialization failures
     */
    Generic11BodyInput(final BufferedInputStream sourceStream, final AgentKey key) throws IonicException {
        this.sourceStream = sourceStream;
        cipher = new AesCtrCipher();
        cipher.setKey(key.getKey());
    }

    /**
     * Initialize this object for processing the body of an Ionic-protected file.
     */
    @Override
    public void init() {
    }

    /**
     * Read the next Ionic-protected block from the input resource.  Version 1.1 blocks are delimited by a
     * block header byte and a block footer byte.
     *
     * @return the next plainText block extracted from the stream
     * @throws IOException    on failure reading from the stream
     * @throws IonicException on failure to parse or decrypt the block
     */
    @Override
    public byte[] read() throws IOException, IonicException {
        final byte[] blockMax = new byte[FileCipher.Generic.V11.BLOCK_SIZE_CIPHER];
        final int count = sourceStream.read(blockMax);
        final byte[] block = (FileCipher.Generic.V11.BLOCK_SIZE_CIPHER == count)
                ? blockMax : Arrays.copyOf(blockMax, count);
        if (block[0] != FileCipher.Generic.V11.BLOCK_HEADER_BYTE) {
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        } else if (block[count - 1] != FileCipher.Generic.V11.BLOCK_FOOTER_BYTE) {
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        } else {
            final byte[] blockTrim = Arrays.copyOfRange(blockMax, 1, (count - 1));
            return cipher.decryptBase64(Transcoder.utf8().encode(blockTrim));
        }
    }

    /**
     * Finish processing of the input stream.
     */
    @Override
    public void doFinal() {
    }
}
