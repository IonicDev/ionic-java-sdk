package com.ionic.sdk.agent.cipher.file.family.generic.output;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonTarget;

import javax.json.Json;
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
     * @param version the cipher family version to be checked for support
     * @param server  the server used to perform the key request for the specified cryptography key
     * @param tag     the identifier associated with the specified cryptography key
     * @return the serialized JSON representation of the header data
     * @throws IonicException on specification of an unsupported file format version
     */
    public String write(final String version, final String server, final String tag) throws IonicException {
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        String delimiter;
        if (FileCipher.Generic.V11.LABEL.equals(version)) {
            JsonTarget.addNotNull(objectBuilder, FileCipher.Header.FAMILY, (String) null);
            delimiter = FileCipher.Generic.V11.DELIMITER;
        } else if (FileCipher.Generic.V12.LABEL.equals(version)) {
            JsonTarget.addNotNull(objectBuilder, FileCipher.Header.FAMILY, FileCipher.Generic.FAMILY);
            delimiter = FileCipher.Generic.V12.DELIMITER;
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
        JsonTarget.addNotNull(objectBuilder, FileCipher.Header.SERVER, server);
        JsonTarget.addNotNull(objectBuilder, FileCipher.Header.TAG, tag);
        JsonTarget.addNotNull(objectBuilder, FileCipher.Header.VERSION, version);
        return (JsonIO.write(objectBuilder.build(), false) + delimiter);
    }
}
