package com.ionic.sdk.agent.cipher.file.family.generic.output;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;

import javax.json.JsonObjectBuilder;

/**
 * Utility class for producing a JSON representation of metadata associated with a
 * <code>GenericFileCipher</code> resource.
 */
@InternalUseOnly
final class GenericHeaderOutput {

    /**
     * Given the parameters of a <code>GenericFileCipher</code> resource, produce a JSON representation, suitable
     * for use as a header when serializing a protected version of the resource.
     *
     * @param jsonHeaderBuilder the JSON object used to hold the attributes to be serialized
     * @param version           the cipher family version to be checked for support
     * @return the serialized JSON representation of the header data
     * @throws IonicException on specification of an unsupported file format version
     */
    public String write(final JsonObjectBuilder jsonHeaderBuilder, final String version) throws IonicException {
        final String delimiter;
        if (FileCipher.Generic.V11.LABEL.equals(version)) {
            delimiter = FileCipher.Generic.V11.DELIMITER;
        } else if (FileCipher.Generic.V12.LABEL.equals(version)) {
            delimiter = FileCipher.Generic.V12.DELIMITER;
        } else if (FileCipher.Generic.V13.LABEL.equals(version)) {
            delimiter = FileCipher.Generic.V13.DELIMITER;
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
        return (JsonIO.write(jsonHeaderBuilder.build(), false) + delimiter);
    }
}
