package com.ionic.sdk.agent.cipher.file.family.openxml.output;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.family.generic.output.GenericOutput;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Extensions for handling output of {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher}
 * version 1.0 file body content.
 */
@InternalUseOnly
final class OpenXml10BodyOutput implements OpenXmlBodyOutput {

    /**
     * The raw output data stream that is to contain the protected file content.
     */
    private final OutputStream targetStream;

    /**
     * Parameters associated with the decrypt operation.
     */
    private final FileCryptoEncryptAttributes encryptAttributes;

    /**
     * Wrapped cipher implementing protection of CSV content.
     */
    private final GenericOutput genericOutput;

    /**
     * Constructor.
     *
     * @param targetStream the raw output data containing the protected file content
     * @param agent        the key services implementation; used to provide keys for cryptography operations
     * @param encryptAttributes a container for applying desired configuration to the operation,
     */
    OpenXml10BodyOutput(final OutputStream targetStream, final KeyServices agent,
                        final FileCryptoEncryptAttributes encryptAttributes) {
        this.targetStream = targetStream;
        this.encryptAttributes = encryptAttributes;
        this.genericOutput = new GenericOutput(targetStream, FileCipher.Generic.V11.BLOCK_SIZE_PLAIN, agent);
    }

    @Override
    public void init() throws IOException, IonicException {
        final FileCryptoEncryptAttributes genericEncryptAttributes =
                new FileCryptoEncryptAttributes(FileCipher.Generic.V11.LABEL,
                                                encryptAttributes.getKeyAttributes(),
                                                encryptAttributes.getMutableKeyAttributes());
        genericEncryptAttributes.setMetadata(encryptAttributes.getMetadata());
        genericOutput.init(genericEncryptAttributes);
        encryptAttributes.setKeyResponse(genericEncryptAttributes.getKeyResponse());
    }

    @Override
    public void doEncryption(final InputStream plainText) throws IOException, IonicException {
        final ReadableByteChannel plainChannel = Channels.newChannel(plainText);
        final ByteBuffer bufferPlainText = genericOutput.getPlainText();
        while (plainText.available() > 0) {
            bufferPlainText.clear();
            plainChannel.read(bufferPlainText);
            bufferPlainText.position(0);
            genericOutput.write(bufferPlainText);
        }
        genericOutput.doFinal();
        targetStream.flush();
    }
}
