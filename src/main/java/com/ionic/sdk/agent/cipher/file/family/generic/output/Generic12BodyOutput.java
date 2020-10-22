package com.ionic.sdk.agent.cipher.file.family.generic.output;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.IonicException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * Extensions for handling output of {@link com.ionic.sdk.agent.cipher.file.GenericFileCipher}
 * version 1.2 file body content.
 */
@InternalUseOnly
final class Generic12BodyOutput implements GenericBodyOutput {

    /**
     * The raw output data stream that is to contain the protected file content.
     */
    private final BufferedOutputStream targetStream;

    /**
     * The Ionic cipher used to encrypt file blocks.
     */
    private final AesCtrCipher cipher;

    /**
     * The cryptography key used to encrypt and sign the file content.
     */
    private final AgentKey key;

    /**
     * The buffer to hold a plaintext block (source buffer for encryption, target buffer for decryption).
     */
    private final ByteBuffer plainText;

    /**
     * The buffer to hold a ciphertext block (source buffer for decryption, target buffer for encryption).
     */
    private final ByteBuffer cipherText;

    /**
     * A running buffer used to store block hashes.  These are hashed and signed at the completion of the
     * file crypto operation, and the result is prepended to the file content.
     */
    private final ByteArrayOutputStream plainTextBlockHashes;

    /**
     * Constructor.
     *
     * @param targetStream the raw output data containing the protected file content
     * @param cipher       the Ionic cipher used to encrypt file blocks
     * @param key          the cryptography key used to decrypt and verify the file content
     * @param plainText    ByteBuffer containing bytes to encrypt
     * @param cipherText   ByteBuffer to receive the result of the cryptography operation
     */
    Generic12BodyOutput(final BufferedOutputStream targetStream, final AesCtrCipher cipher, final AgentKey key,
                        final ByteBuffer plainText, final ByteBuffer cipherText) {
        this.targetStream = targetStream;
        this.plainTextBlockHashes = new ByteArrayOutputStream();
        this.cipher = cipher;
        this.key = key;
        this.plainText = plainText;
        this.cipherText = cipherText;
    }

    /**
     * The v1.2 streaming cipher needs to set aside room for the file signature, so that it may be inserted after all
     * of the file data is written.
     *
     * @throws IOException on failure reading from the stream
     */
    @Override
    public int init() throws IOException {
        targetStream.write(new byte[FileCipher.Generic.V12.SIGNATURE_SIZE_CIPHER]);
        return FileCipher.Generic.V12.SIGNATURE_SIZE_CIPHER;
    }

    @Override
    public int getBlockLengthPlain() {
        return FileCipher.Generic.V12.BLOCK_SIZE_PLAIN;
    }

    @Override
    public int write(final ByteBuffer byteBuffer) throws IOException, IonicException {
        final byte[] plainTextBlockHash = CryptoUtils.hmacSHA256(byteBuffer, key.getKey());
        plainTextBlockHashes.write(plainTextBlockHash);
        plainText.position(0);
        cipherText.clear();
        final int encryptedLen = cipher.encrypt(plainText, cipherText);
        final WritableByteChannel cipherChannel = Channels.newChannel(targetStream);
        cipherText.limit(cipherText.position());
        cipherText.position(0);
        cipherChannel.write(cipherText);
        targetStream.flush();
        return encryptedLen;
    }

    @Override
    public void doFinal() {
    }

    /**
     * The v1.2 streaming cipher contains a file signature, used to validate decryption of the content.
     *
     * @throws IonicException on signature generation failure
     */
    @Override
    public byte[] getSignature() throws IonicException {
        final byte[] blockHashes = plainTextBlockHashes.toByteArray();
        final byte[] hmacSHA256 = CryptoUtils.hmacSHA256(blockHashes, key.getKey());
        return cipher.encrypt(hmacSHA256);
    }
}
