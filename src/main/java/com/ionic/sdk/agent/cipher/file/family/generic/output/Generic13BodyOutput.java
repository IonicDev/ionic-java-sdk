package com.ionic.sdk.agent.cipher.file.family.generic.output;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.json.JsonTarget;
import com.ionic.sdk.key.KeyServices;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * Extensions for handling output of {@link com.ionic.sdk.agent.cipher.file.GenericFileCipher}
 * version 1.3 file body content.
 */
@InternalUseOnly
final class Generic13BodyOutput implements GenericBodyOutput {

    /**
     * The raw output data stream that is to contain the protected file content.
     */
    private final BufferedOutputStream targetStream;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * The cryptography key used to encrypt the file content.
     */
    private final AgentKey key;

    /**
     * The Ionic cipher used to encrypt file blocks.
     */
    private final AesGcmCipher cipher;

    /**
     * The default size of a single encryption block.
     */
    private final int blockSize;

    /**
     * The count of encryption blocks which use the same encryption key.
     */
    private final int metaSize;

    /**
     * The running count of ciphertext blocks encrypted by this class instance.  Cryptography keys are rotated
     * every {@link #metaSize} blocks.
     */
    private int blockIndex;

    /**
     * The buffer to hold a plaintext block (source buffer for encryption, target buffer for decryption).
     */
    private final ByteBuffer plainText;

    /**
     * The buffer to hold a ciphertext block (source buffer for decryption, target buffer for encryption).
     */
    private final ByteBuffer cipherText;

    /**
     * Constructor.
     *
     * @param targetStream the raw output data containing the protected file content
     * @param agent        the key services implementation; used to provide keys for cryptography operations
     * @param key          the cryptography key used to encrypt the content of the first meta block
     * @param blockSize    the default size of a single encryption block
     * @param metaSize     the count of blocks which use the same encryption key
     * @param plainText    ByteBuffer containing bytes to encrypt
     * @param cipherText   ByteBuffer to receive the result of the cryptography operation
     * @throws IonicException on failure to instantiate the AES/GCM cipher
     */
    Generic13BodyOutput(final BufferedOutputStream targetStream, final KeyServices agent,
                        final AgentKey key, final int blockSize, final int metaSize,
                        final ByteBuffer plainText, final ByteBuffer cipherText) throws IonicException {
        this.targetStream = targetStream;
        this.agent = agent;
        this.key = key;
        this.cipher = new AesGcmCipher(key.getKey());
        this.cipher.setAuthData(Transcoder.utf8().decode(key.getId()));
        this.blockSize = blockSize;
        this.metaSize = metaSize;
        this.blockIndex = 0;
        this.plainText = plainText;
        this.cipherText = cipherText;
    }

    @Override
    public int init() {
        return 0;
    }

    @Override
    public int getBlockLengthPlain() {
        return blockSize;
    }

    @Override
    public int write(final ByteBuffer byteBuffer) throws IOException, IonicException {
        final DataOutputStream dos = new DataOutputStream(targetStream);
        // cryptography keys are rotated every 'metaSize' blocks
        if ((blockIndex > 0) && ((blockIndex % metaSize) == 0)) {
            // rotate file cipher key
            final CreateKeysResponse createKeysResponse = agent.createKey(
                    key.getAttributesMap(), key.getMutableAttributesMap());
            final CreateKeysResponse.Key keyRotate = createKeysResponse.getFirstKey();
            cipher.setKey(keyRotate.getKey());
            cipher.setAuthData(Transcoder.utf8().decode(keyRotate.getId()));
            // serialize Ionic generic v1.3 key rotation JSON header
            final JsonObjectBuilder jsonHeaderBuilder = Json.createObjectBuilder();
            JsonTarget.addNotNull(jsonHeaderBuilder, FileCipher.Header.TAG, keyRotate.getId());
            final String headerText = new GenericHeaderOutput().write(
                    jsonHeaderBuilder, FileCipher.Generic.V13.LABEL);
            dos.write(Transcoder.utf8().decode(headerText));
        }
        plainText.position(0);
        cipherText.clear();
        final int encryptedLen = cipher.encrypt(plainText, cipherText);
        dos.writeInt(encryptedLen);
        final WritableByteChannel cipherChannel = Channels.newChannel(targetStream);
        cipherText.limit(cipherText.position());
        cipherText.position(0);
        cipherChannel.write(cipherText);
        targetStream.flush();
        ++blockIndex;
        return encryptedLen;
    }

    @Override
    public void doFinal() {
    }

    @Override
    public byte[] getSignature() {
        return null;
    }
}
