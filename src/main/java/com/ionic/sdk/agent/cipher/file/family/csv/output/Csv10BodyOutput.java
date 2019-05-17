package com.ionic.sdk.agent.cipher.file.family.csv.output;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.family.generic.output.GenericOutput;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.ByteQueueOutputStream;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Extensions for handling output of {@link com.ionic.sdk.agent.cipher.file.CsvFileCipher}
 * version 1.0 file body content.
 */
@InternalUseOnly
final class Csv10BodyOutput implements CsvBodyOutput {

    /**
     * The raw output data stream that is to contain the protected file content.
     */
    private final BufferedOutputStream targetStream;

    /**
     * Parameters to be associated with the encrypt operation.
     */
    private final FileCryptoEncryptAttributes encryptAttributes;

    /**
     * Intermediate buffer holding data from input CSV (base64 encoded) before being read by wrapped cipher.
     */
    private final ByteQueueOutputStream byteQueueOutputStream;

    /**
     * Wrapped cipher implementing protection of CSV content.
     */
    private final GenericOutput genericOutput;

    /**
     * @return the {@link ByteBuffer} allocated to hold a plaintext block for this cryptography operation
     */
    public ByteBuffer getPlainText() {
        return genericOutput.getPlainText();
    }

    /**
     * Constructor.
     *
     * @param targetStream      the raw output data containing the protected file content
     * @param sizeInput         the length of the resource to be encrypted
     * @param agent             the key services implementation; used to provide keys for cryptography operations
     * @param encryptAttributes the attributes to pass along to the key created by the operation
     */
    Csv10BodyOutput(final BufferedOutputStream targetStream, final long sizeInput, final KeyServices agent,
                    final FileCryptoEncryptAttributes encryptAttributes) {
        this.targetStream = targetStream;
        this.encryptAttributes = encryptAttributes;
        this.byteQueueOutputStream = new ByteQueueOutputStream(FileCipher.Csv.V10.BLOCK_SIZE);
        this.genericOutput = new GenericOutput(byteQueueOutputStream, sizeInput, agent);
    }

    @Override
    public void init() throws IOException, IonicException {
        targetStream.write(Transcoder.utf8().decode(FileCipher.Csv.V10.DATA_BEGIN_STRING));
        targetStream.write(Transcoder.utf8().decode(FileCipher.Csv.V10.LINE_SEPARATOR));
        final FileCryptoEncryptAttributes encryptAttributesWrapped =
                new FileCryptoEncryptAttributes(FileCipher.Generic.V12.LABEL);
        encryptAttributesWrapped.setKeyAttributes(encryptAttributes.getKeyAttributes());
        encryptAttributesWrapped.setMutableKeyAttributes(encryptAttributes.getMutableKeyAttributes());
        encryptAttributesWrapped.setMetadata(encryptAttributes.getMetadata());
        genericOutput.init(encryptAttributesWrapped);
        targetStream.flush();
        encryptAttributes.setKeyResponse(encryptAttributesWrapped.getKeyResponse());
    }

    @Override
    public int getBlockLengthPlain() {
        return genericOutput.getBlockLengthPlain();
    }

    @Override
    public void write(final ByteBuffer byteBuffer) throws IOException, IonicException {
        genericOutput.write(byteBuffer);
        final byte[] data = new byte[FileCipher.Csv.V10.WIDTH_RAW];
        while (byteQueueOutputStream.getByteQueue().available() >= FileCipher.Csv.V10.WIDTH_RAW) {
            final int countRead = byteQueueOutputStream.getByteQueue().removeData(data, 0, data.length);
            targetStream.write(Transcoder.utf8().decode(Transcoder.base64().encode(Arrays.copyOf(data, countRead))));
            targetStream.write(Transcoder.utf8().decode(FileCipher.Csv.V10.LINE_SEPARATOR));
        }
    }

    @Override
    public void doFinal() throws IOException {
        final byte[] data = new byte[FileCipher.Csv.V10.WIDTH_RAW];
        while (byteQueueOutputStream.getByteQueue().available() > 0) {
            final int countToRead = Math.min(
                    byteQueueOutputStream.getByteQueue().available(), FileCipher.Csv.V10.WIDTH_RAW);
            final int countRead = byteQueueOutputStream.getByteQueue().removeData(data, 0, countToRead);
            targetStream.write(Transcoder.utf8().decode(Transcoder.base64().encode(Arrays.copyOf(data, countRead))));
            targetStream.write(Transcoder.utf8().decode(FileCipher.Csv.V10.LINE_SEPARATOR));
        }
        targetStream.write(Transcoder.utf8().decode(FileCipher.Csv.V10.DATA_END_STRING));
        targetStream.write(Transcoder.utf8().decode(FileCipher.Csv.V10.LINE_SEPARATOR));
    }

    @Override
    public int getHeaderLengthWrapped() {
        return genericOutput.getHeaderLength();
    }

    @Override
    public byte[] getSignatureWrapped() throws IonicException {
        return genericOutput.getSignature();
    }
}
