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
import java.util.Arrays;

/**
 * Encapsulate logic to apply <code>GenericFileCipher</code> signature to CSV wrapped content.
 */
@InternalUseOnly
public final class CsvSignerBytes {

    /**
     * Use this buffer to read the (base64 encoded) content, apply the signature, and write the (updated) content.
     */
    private final ByteBuffer byteBuffer;

    /**
     * Constructor.
     *
     * @param bytes the csv encoding of the cipherText associated with a file resource, without the signature
     */
    public CsvSignerBytes(final byte[] bytes) {
        this.byteBuffer = ByteBuffer.wrap(bytes);
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
     * @throws IOException on failure to write signature to content
     */
    private void applyInternal(final byte[] signature,
                               final int headerLengthCsv, final int position) throws IOException {
        final int countDelimiter = FileCipher.Csv.V10.LINE_SEPARATOR.length();
        // move the buffer cursor to the beginning of the wrapped content
        byteBuffer.position(headerLengthCsv);
        // deserialize enough base64 encoded text to completely contain the space reserved for the signature
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final int countEnoughBytes = position + FileCipher.Generic.V12.SIGNATURE_SIZE_CIPHER;
        while (bos.size() < countEnoughBytes) {
            final int beginLine = byteBuffer.position();
            final StreamTokenFinder finderEOL = new StreamTokenFinder(
                    Transcoder.utf8().decode(FileCipher.Csv.V10.LINE_SEPARATOR));
            boolean match = false;
            while (!match) {
                match = finderEOL.match(byteBuffer.get());
            }
            final int endLine = byteBuffer.position();
            final byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), beginLine, endLine - countDelimiter);
            bos.write(Transcoder.base64().decode(Transcoder.utf8().encode(bytes)));
        }
        final byte[] bytesToApply = bos.toByteArray();
        // write the signature to the wrapped content
        System.arraycopy(signature, 0, bytesToApply, position, signature.length);
        // re-serialize the wrapped resource content with the signature
        int cursorCsv = headerLengthCsv;
        for (int cursorEmbed = 0; (cursorEmbed < bytesToApply.length); cursorEmbed += FileCipher.Csv.V10.WIDTH_RAW) {
            final int length = Math.min(FileCipher.Csv.V10.WIDTH_RAW, (bytesToApply.length - cursorEmbed));
            final byte[] bytesLine = Transcoder.utf8().decode(Transcoder.base64().encode(
                    Arrays.copyOfRange(bytesToApply, cursorEmbed, cursorEmbed + length)));
            System.arraycopy(bytesLine, 0, byteBuffer.array(), cursorCsv, bytesLine.length);
            cursorCsv += FileCipher.Csv.V10.WIDTH + countDelimiter;
        }
    }
}
