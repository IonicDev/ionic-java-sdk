package com.ionic.sdk.agent.cipher.file.family.csv.output;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

/**
 * Utility class for producing an Ionic file header associated with a <code>CsvFileCipher</code> resource.
 */
@InternalUseOnly
final class CsvHeaderOutput {

    /**
     * Given the parameters of a <code>CsvFileCipher</code> resource, produce a text representation, suitable
     * for use as a header when serializing a protected version of the resource.
     *
     * @param version the cipher family version to be checked for support
     * @return the serialized text representation of the header data
     * @throws IonicException on specification of an unsupported file format version
     */
    public String write(final String version) throws IonicException {
        final StringBuilder buffer = new StringBuilder();
        if (FileCipher.Csv.V10.LABEL.equals(version)) {
            buffer.append(FileCipher.Csv.V10.VERSION_1_0_STRING);
            buffer.append(FileCipher.Csv.V10.LINE_SEPARATOR);
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
        return buffer.toString();
    }
}
