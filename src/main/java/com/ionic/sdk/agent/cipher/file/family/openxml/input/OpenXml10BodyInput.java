package com.ionic.sdk.agent.cipher.file.family.openxml.input;

import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.agent.cipher.file.family.generic.input.GenericInput;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * OpenXml10BodyInput version 1.0 extensions for handling the file body content.
 */
@InternalUseOnly
final class OpenXml10BodyInput implements OpenXmlBodyInput {

    /**
     * Metadata describing the Ionic state of the file.
     */
    private final FileCryptoFileInfo fileInfo;

    /**
     * Parameters associated with the decrypt operation.
     */
    private final FileCryptoDecryptAttributes decryptAttributes;

    /**
     * Wrapped cipher implementing protection of OpenXML content.
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
    OpenXml10BodyInput(final InputStream sourceStream, final KeyServices agent,
                        final FileCryptoFileInfo fileInfo, final FileCryptoDecryptAttributes attributes) {
        this.fileInfo = fileInfo;
        this.decryptAttributes = attributes;
        this.genericInput = new GenericInput(new BufferedInputStream(sourceStream),
                FileCipher.Generic.V12.BLOCK_SIZE_CIPHER, agent);
    }

    @Override
    public void init() throws IOException, IonicException {
        try {
            genericInput.init(fileInfo, decryptAttributes);
        } finally {
            if (fileInfo.getCipherFamily() == CipherFamily.FAMILY_GENERIC) {
                fileInfo.setCipherFamily(CipherFamily.FAMILY_OPENXML);
                fileInfo.setCipherVersion(FileCipher.OpenXml.V10.LABEL);
                decryptAttributes.setFamily(CipherFamily.FAMILY_OPENXML);
                decryptAttributes.setVersion(FileCipher.OpenXml.V10.LABEL);
            }
        }
    }

    @Override
    public ByteBuffer read() throws IOException, IonicException {
        return genericInput.read();
    }

    @Override
    public int available() throws IOException {
        // non-zero if there is source data, or data cached in the wrapped stream
        return genericInput.available();
    }

    @Override
    public void doFinal() throws IOException, IonicException {
        genericInput.doFinal();
    }
}
