package com.ionic.sdk.agent.cipher.file.family.generic.input;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.BytesReader;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.key.KeyServices;

import javax.json.JsonObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * GenericFileCipher version 1.3 input extensions for handling the file body content.  These include handling the
 * file content signature stored at the end of the body.
 */
@InternalUseOnly
final class Generic13BodyInput implements GenericBodyInput {

    /**
     * The raw input data stream containing the protected file content.
     */
    private final BufferedInputStream sourceStream;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * The cryptography key used to decrypt and verify the file content.
     */
    private final AgentKey key;

    /**
     * The count of encryption blocks which use the same encryption key.
     */
    private final int metaSize;

    /**
     * The running count of ciphertext blocks decrypted by this class instance.  Cryptography keys are rotated
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
     * The Ionic cipher used to encrypt file blocks.
     */
    private final AesGcmCipher cipher;

    /**
     * Constructor.
     *
     * @param sourceStream the raw input data containing the protected file content
     * @param agent        the key services implementation; used to provide keys for cryptography operations
     * @param key          the cryptography key used to decrypt and verify the content of the first meta block
     * @param blockSize    the default size of a single encryption block
     * @param metaSize     the count of blocks which use the same encryption key
     * @param plainText    ByteBuffer to receive the result of the cryptography operation
     * @param cipherText   ByteBuffer containing bytes to decrypt
     * @throws IonicException on cipher initialization failures
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")  // preserve the existing API signature
    Generic13BodyInput(final BufferedInputStream sourceStream, final KeyServices agent, final AgentKey key,
                       final int blockSize, final int metaSize,
                       final ByteBuffer plainText, final ByteBuffer cipherText) throws IonicException {
        this.sourceStream = sourceStream;
        this.agent = agent;
        this.key = key;
        this.metaSize = metaSize;
        this.blockIndex = 0;
        this.plainText = plainText;
        this.cipherText = cipherText;
        this.cipher = new AesGcmCipher();
        if (key != null) {
            this.cipher.setKey(key.getKey());
            this.cipher.setAuthData(Transcoder.utf8().decode(key.getId()));
        }
    }

    /**
     * Start processing of the input stream.
     */
    @Override
    public void init() {
    }

    @Override
    public ByteBuffer read() throws IOException, IonicException {
        // cryptography keys are rotated every 'metaSize' blocks
        if ((blockIndex > 0) && ((blockIndex % metaSize) == 0)) {
            // deserialize Ionic generic v1.3 key rotation JSON header
            final String ionicHeader = new GenericHeaderInput().read(sourceStream);
            final JsonObject jsonHeader = JsonIO.readObject(ionicHeader, SdkError.ISFILECRYPTO_PARSEFAILED);
            final String tag = JsonSource.getString(jsonHeader, FileCipher.Header.TAG);
            SdkData.checkTrue(!Value.isEmpty(tag), SdkError.ISFILECRYPTO_MISSINGVALUE);
            // rotate file cipher key
            final GetKeysResponse.Key keyRotate = agent.getKey(tag).getFirstKey();
            cipher.setKey(keyRotate.getKey());
            cipher.setAuthData(Transcoder.utf8().decode(keyRotate.getId()));
        }
        // consume input data
        final ReadableByteChannel readableChannel = Channels.newChannel(sourceStream);
        // consume Generic v1.3 block length (32 bit integer)
        cipherText.clear();
        cipherText.limit(Integer.SIZE / Byte.SIZE);
        final int countInteger = readableChannel.read(cipherText);
        SdkData.checkTrue(cipherText.limit() == countInteger, SdkError.ISFILECRYPTO_EOF);
        final int sizeBlock = BytesReader.readInt(cipherText.array(), 0, cipherText.limit());
        // consume Generic v1.3 block
        cipherText.clear();
        cipherText.limit(sizeBlock);
        final int countBlock = readableChannel.read(cipherText);
        SdkData.checkTrue(sizeBlock == countBlock, SdkError.ISFILECRYPTO_EOF);
        // perform decryption
        cipherText.limit(cipherText.position());
        cipherText.position(0);
        plainText.limit(cipherText.limit());
        plainText.position(0);
        if (key != null) {
            cipher.decrypt(plainText, cipherText);
        }
        plainText.limit(plainText.position());
        plainText.position(0);
        // wrap up processing
        ++blockIndex;
        return plainText;
    }

    /**
     * Finish processing of the input stream.
     */
    @Override
    public void doFinal() {
    }
}
