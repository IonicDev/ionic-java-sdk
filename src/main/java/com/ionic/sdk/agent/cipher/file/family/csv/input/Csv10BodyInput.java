package com.ionic.sdk.agent.cipher.file.family.csv.input;

import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.agent.cipher.file.family.generic.input.GenericInput;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.BytesTranscoder;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.ByteQueueInputStream;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * CsvFileCipher version 1.0 extensions for handling the file body content.
 */
@InternalUseOnly
final class Csv10BodyInput implements CsvBodyInput {

    /**
     * The raw input data stream containing the protected file content.
     */
    private final BufferedReader sourceReader;

    /**
     * Metadata describing the Ionic state of the file.
     */
    private final FileCryptoFileInfo fileInfo;

    /**
     * Parameters associated with the decrypt operation.
     */
    private final FileCryptoDecryptAttributes decryptAttributes;

    /**
     * Intermediate buffer holding data from input CSV (base64 encoded) before being read by wrapped cipher.
     */
    private final ByteQueueInputStream targetStream;

    /**
     * Wrapped cipher implementing protection of CSV content.
     */
    private final GenericInput genericInput;

    /**
     * Constructor.
     *
     * @param sourceStream the raw input data containing the protected file content
     * @param agent        the key services implementation; used to provide keys for cryptography operations
     * @param fileInfo     the structure into which data about the Ionic state of the file should be written
     * @param attributes   the parameters associated with the decrypt operation
     */
    Csv10BodyInput(final BufferedInputStream sourceStream, final KeyServices agent,
                   final FileCryptoFileInfo fileInfo, final FileCryptoDecryptAttributes attributes) {
        this.sourceReader = new BufferedReader(new InputStreamReader(sourceStream, StandardCharsets.UTF_8));
        this.fileInfo = fileInfo;
        this.decryptAttributes = attributes;
        this.targetStream = new ByteQueueInputStream(FileCipher.Csv.V10.BLOCK_SIZE);
        this.genericInput = new GenericInput(targetStream, agent);
    }

    @Override
    public void init() throws IonicException, IOException {
        // skip header data, up to and including the "data begin" line
        String line = "";
        while ((line != null) && !line.contains(FileCipher.Csv.V10.DATA_BEGIN_STRING)) {
            line = sourceReader.readLine();
        }
        // fail on inability to find data delimiter
        if ((line == null) || !line.contains(FileCipher.Csv.V10.DATA_BEGIN_STRING)) {
            throw new IonicException(SdkError.ISFILECRYPTO_EOF);
        }
    }

    @Override
    public int available() throws IOException {
        // non-zero if there is source data, or data cached in the wrapped stream
        return sourceReader.ready() ? 1 : genericInput.available();
    }

    @Override
    public byte[] read() throws IonicException, IOException {
        final BytesTranscoder transcoderBase64 = Transcoder.base64();
        String line;
        boolean doneEmbedRead = false;
        while (!doneEmbedRead) {
            line = sourceReader.readLine();
            if (line == null) {
                // end of input reached
                doneEmbedRead = true;
            } else if (line.contains(FileCipher.Csv.V10.DATA_END_STRING)) {
                doneEmbedRead = true;
                // no relevant data past the "end data" marker; skip it
                java.util.logging.Logger.getLogger(getClass().getName()).fine(String.format("%s, %d bytes skipped",
                        FileCipher.Csv.V10.DATA_END_STRING, sourceReader.skip(Long.MAX_VALUE)));
            } else {
                // write to wrapped buffer
                targetStream.addBytes(transcoderBase64.decode(line));
                if (targetStream.available() >= FileCipher.Generic.V12.BLOCK_SIZE_CIPHER) {
                    // there is enough data for the wrapped cipher to extract a complete block
                    doneEmbedRead = true;
                }
            }
        }
        // unidirectional state machine with two states
        byte[] bytes = new byte[0];
        if (decryptAttributes.getFamily().equals(CipherFamily.FAMILY_UNKNOWN)) {
            genericInput.init(fileInfo, decryptAttributes);
            fileInfo.setCipherFamily(CipherFamily.FAMILY_CSV);
            fileInfo.setCipherVersion(FileCipher.Csv.V10.LABEL);
        } else {
            bytes = genericInput.read();
        }
        return bytes;
    }

    @Override
    public void doFinal() throws IonicException {
        genericInput.doFinal();
        decryptAttributes.setFamily(CipherFamily.FAMILY_CSV);
        decryptAttributes.setVersion(FileCipher.Csv.V10.LABEL);
    }
}
