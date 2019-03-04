package com.ionic.sdk.agent.cipher.file.family.generic.output;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Extensions for handling output of {@link com.ionic.sdk.agent.cipher.file.GenericFileCipher}
 * version 1.1 file body content.
 */
@InternalUseOnly
final class Generic11BodyOutput implements GenericBodyOutput {

    /**
     * The raw output data stream that is to contain the protected file content.
     */
    private final BufferedOutputStream targetStream;

    /**
     * The Ionic cipher used to encrypt file blocks.
     */
    private final AesCtrCipher cipher;

    /**
     * Constructor.
     *
     * @param targetStream the raw output data containing the protected file content
     * @param cipher       the Ionic cipher used to encrypt file blocks
     */
    Generic11BodyOutput(final BufferedOutputStream targetStream, final AesCtrCipher cipher) {
        this.targetStream = targetStream;
        this.cipher = cipher;
    }

    @Override
    public int init() {
        return 0;
    }

    @Override
    public int getBlockLengthPlain() {
        return FileCipher.Generic.V11.BLOCK_SIZE_PLAIN;
    }

    /**
     * Write the next Ionic-protected block to the output resource.  Version 1.1 blocks are delimited by a
     * block header byte and a block footer byte.
     *
     * @param byteBuffer the next plainText block to be written to the stream
     * @return the number of bytes written in the context of this call
     * @throws IOException    on failure writing to the stream
     * @throws IonicException on failure to encrypt the block, or calculate the block signature
     */
    @Override
    public int write(final ByteBuffer byteBuffer) throws IOException, IonicException {
        final byte[] plainText = new byte[byteBuffer.remaining()];
        byteBuffer.get(plainText);
        final byte[] cipherText = Transcoder.utf8().decode(cipher.encryptToBase64(plainText));
        targetStream.write(FileCipher.Generic.V11.BLOCK_HEADER_BYTE);
        targetStream.write(cipherText);
        targetStream.write(FileCipher.Generic.V11.BLOCK_FOOTER_BYTE);
        return 1 + cipherText.length + 1;
    }

    @Override
    public void doFinal() {
    }

    /**
     * Version 1.1 file resources do not have an embedded file signature.
     *
     * @return null
     */
    @Override
    public byte[] getSignature() {
        return null;
    }
}
