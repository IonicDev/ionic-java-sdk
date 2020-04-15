package com.ionic.sdk.agent.cipher.file.family.csv.sig;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.StreamTokenFinder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Encapsulate logic to apply <code>GenericFileCipher</code> signature to CSV wrapped content.
 */
@InternalUseOnly
public final class CsvSignerFile {

    /**
     * Use this channel to read the (base64 encoded) content, apply the signature, and write the (updated) content.
     */
    private final FileChannel fileChannel;

    /**
     * Constructor.
     *
     * @param fileChannel the csv encoding of the cipherText associated with a file resource, without the signature
     */
    public CsvSignerFile(final FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    /**
     * Apply wrapped resource signature to CSV content.
     *
     * @param signature       the resource signature to be applied to the resource
     * @param headerLengthCsv the position in the resource of the beginning of the wrapped encrypted content
     * @param position        the position in the wrapped content to which the signature should be written
     * @throws IonicException on failure to write signature to content
     */
    public void apply(final byte[] signature, final int headerLengthCsv, final int position) throws IonicException {
        try {
            applyInternal(signature, headerLengthCsv, position);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_STREAM_WRITE, e);
        }
    }

    /**
     * Apply wrapped resource signature to CSV content.
     *
     * @param signature       the resource signature to be applied to the resource
     * @param headerLengthCsv the position in the resource of the beginning of the wrapped encrypted content
     * @param position        the position in the wrapped content to which the signature should be written
     * @throws IOException    on read / write failures
     * @throws IonicException on failure to write signature to content
     */
    private void applyInternal(final byte[] signature,
                               final int headerLengthCsv, final int position) throws IOException, IonicException {
        final int countDelimiter = FileCipher.Csv.V10.LINE_SEPARATOR.length();
        final int countLineBase64 = FileCipher.Csv.V10.WIDTH + countDelimiter;
        // move the file cursor to the beginning of the wrapped content
        fileChannel.position(headerLengthCsv);
        // deserialize enough base64 encoded text to completely contain the space reserved for the signature
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final int countEnoughBytes = position + FileCipher.Generic.V12.SIGNATURE_SIZE_CIPHER;
        final int countEnoughLines = (countEnoughBytes / FileCipher.Csv.V10.WIDTH_RAW) + 1;
        final ByteBuffer byteBuffer = ByteBuffer.allocate(countEnoughLines * countLineBase64);
        final int read = fileChannel.read(byteBuffer);
        if (read != (countEnoughLines * countLineBase64)) {
            throw new IonicException(SdkError.ISFILECRYPTO_STREAM_WRITE);
        }
        // reset the file pointer to the point at which the modified bytes should be written
        fileChannel.position(headerLengthCsv);
        // iterate through the byte cache, base64 decoding them
        byteBuffer.position(0);
        while (bos.size() < countEnoughBytes) {
            final int beginLine = byteBuffer.position();
            final StreamTokenFinder finderEOL = new StreamTokenFinder(
                    Transcoder.utf8().decode(FileCipher.Csv.V10.LINE_SEPARATOR));
            boolean match = false;
            while (!match) {
                match = finderEOL.match(byteBuffer.get());
            }
            final int endLine = byteBuffer.position();
            final byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), beginLine, (endLine - countDelimiter));
            bos.write(Transcoder.base64().decode(Transcoder.utf8().encode(bytes)));
        }
        final byte[] bytesToApply = bos.toByteArray();
        // write the signature to the cached content
        System.arraycopy(signature, 0, bytesToApply, position, signature.length);
        // re-serialize the wrapped resource content with the signature
        for (int cursorEmbed = 0; (cursorEmbed < bytesToApply.length); cursorEmbed += FileCipher.Csv.V10.WIDTH_RAW) {
            final int length = Math.min(FileCipher.Csv.V10.WIDTH_RAW, (bytesToApply.length - cursorEmbed));
            final byte[] bytesLine = Transcoder.utf8().decode(Transcoder.base64().encode(
                    Arrays.copyOfRange(bytesToApply, cursorEmbed, cursorEmbed + length)));
            final ByteBuffer byteBufferWrite = ByteBuffer.wrap(bytesLine);
            fileChannel.write(byteBufferWrite);
            fileChannel.position(fileChannel.position() + 2);
        }
    }
}
