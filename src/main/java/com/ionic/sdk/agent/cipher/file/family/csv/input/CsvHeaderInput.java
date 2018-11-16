package com.ionic.sdk.agent.cipher.file.family.csv.input;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.StreamTokenFinder;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Utility class for consuming an Ionic file header associated with a <code>CsvFileCipher</code> resource.
 */
@InternalUseOnly
final class CsvHeaderInput {

    /**
     * Extract the Ionic type header from the file stream.  This is done via use of a regular expression to locate
     * the delimiter marking the end of the header, and the beginning of the file content.
     *
     * @param is the stream from which the Ionic file header is to be read
     * @return a string containing the header
     * @throws IonicException on failure accessing or parsing the stream content
     */
    public String read(final InputStream is) throws IonicException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String token = null;
        final StreamTokenFinder finderV10 = new StreamTokenFinder(
                Transcoder.utf8().decode(FileCipher.Csv.V10.VERSION_1_0_STRING));
        int i;
        while ((i = DeviceUtils.readByte(is)) >= 0) {
            bos.write(i);
            final byte b = (byte) i;
            if (finderV10.match(b)) {
                token = FileCipher.Generic.V11.DELIMITER;
            } else if (bos.size() >= MAX_HEADER_SIZE) {
                throw new IonicException(SdkError.ISFILECRYPTO_NOHEADER);
            }
            if (token != null) {
                break;
            }
        }
        if (token == null) {
            throw new IonicException(SdkError.ISFILECRYPTO_EOF);
        }
        return Transcoder.utf8().encode(bos.toByteArray()).replace(token, "");
    }

    /**
     * Default maximum amount of bytes to read when looking for a header.
     */
    public static final int MAX_HEADER_SIZE =
            FileCipher.Csv.V10.VERSION_1_0_STRING.length() + FileCipher.Csv.V10.DATA_BEGIN_STRING.length() + 1000;
}
