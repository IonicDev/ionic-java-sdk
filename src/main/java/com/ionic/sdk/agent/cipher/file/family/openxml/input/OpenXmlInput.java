package com.ionic.sdk.agent.cipher.file.family.openxml.input;

import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.agent.cipher.file.family.openxml.OpenXmlUtils;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.datastructures.Tuple;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Wrap an input stream with logic to manage the Ionic augmentation of the content (header, cipher blocks).
 */
@InternalUseOnly
public final class OpenXmlInput {

    /**
     * The raw input data stream containing the protected file content.
     */
    private final InputStream sourceStream;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices agent;

    /**
     * The cipher family implementation for managing the file body content for the specified version.
     */
    private OpenXmlBodyInput bodyInput;

    /**
     * Constructor.
     *
     * @param inputStream the raw input data containing the protected file content
     * @param agent       the key services implementation; used to provide keys for cryptography operations
     */
    public OpenXmlInput(final InputStream inputStream, final KeyServices agent) {
        this.sourceStream = inputStream;
        this.agent = agent;
    }

    /**
     * Attempt to retrieve the File Info using the Ionic Info JSON file and fallback to opening the encrypted
     * payload and letting Generic Cipher determine the File Info.
     *
     * @param sourceStream the raw input data containing the protected file content
     * @param fileInfo    the structure into which data about the Ionic state of the file should be written
     * @throws IonicException on failure to open the .zip file
     */
    public static void getFileInfo(final InputStream sourceStream,
                                    final FileCryptoFileInfo fileInfo) throws IonicException {

        final String version = determinePossibleEncryptionVersion(sourceStream);

        if (FileCipher.OpenXml.V10.LABEL.equals(version)) {

            // We need to ignore V10 files here as they are indistinguishable from pure Generic files.

            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);

        } else if (FileCipher.OpenXml.V11.LABEL.equals(version)) {

            final Tuple<FileType, byte[]> typeAndProps = OpenXmlUtils.doFirstPassThrough(sourceStream, false);
            if (typeAndProps.first() == FileType.FILETYPE_UNKNOWN) {
                throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
            }

            if (OpenXml11BodyInput.getFileInfo(sourceStream, fileInfo)) {
                // Found the Ionic Info file and successfully parsed it.
                return;
            } else {
                // Ionic Info either too old or this isn't encrypted at all
                // Next, try finding and examining the Ionic Payload.
                final OpenXml11BodyInput bodyInput = new OpenXml11BodyInput(sourceStream,
                                                                        null,
                                                                        fileInfo,
                                                                        new FileCryptoDecryptAttributes());
                if (!bodyInput.hasValidPayloadStream()) {
                    // No Ionic payload in the Zip file, so file is unencrypted, but suitable for this family.
                    fileInfo.setEncrypted(false);
                    fileInfo.setCipherFamily(CipherFamily.FAMILY_OPENXML);
                    fileInfo.setCipherVersion(FileCipher.OpenXml.V11.LABEL);
                    return;
                }

                try {
                    bodyInput.init();
                } catch (IOException e) {
                    throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
                }
            }
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
    }

    /**
     * Read the first four bytes and check for JSON ('{') or a ZIP header.
     *
     * @param sourceStream the raw input data containing the protected file content
     * @throws IonicException on failure to load or parse header
     * @return The version as a String or throws an exception
     */
    private static String determinePossibleEncryptionVersion(final InputStream sourceStream) throws IonicException {

        final byte[] firstFourBytes = new byte[FileCipher.OpenXml.ZIPFILE_HEADER_BYTES.length()];

        try {
            final int readLen = sourceStream.read(firstFourBytes);
            if (readLen != firstFourBytes.length) {
                throw new IonicException(SdkError.ISFILECRYPTO_BAD_INPUT);
            }

            // sourceStream should be a FileInputStream or a ByteArrayInputStream
            // The first can be reset by its channel, the second will reset with reset()
            // Any other inputStream type will attempt reset() and throw an exception if it can't.
            try {
                final FileInputStream fs = (FileInputStream) sourceStream;
                fs.getChannel().position(0);
            } catch (ClassCastException e) {
                sourceStream.reset();
            }
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_EOF);
        }

        if (firstFourBytes[0] == '{') {
            return FileCipher.OpenXml.V10.LABEL;
        } else if (Arrays.equals(firstFourBytes, Transcoder.utf8().decode(FileCipher.OpenXml.ZIPFILE_HEADER_BYTES))) {
            return FileCipher.OpenXml.V11.LABEL;
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_NOT_ENCRYPTED);
        }
    }

    /**
     * Initialize this object for processing an Ionic-protected file.  The file is expected to begin with a JSON
     * header, describing metadata associated with the file.
     *
     * @param fileInfo          the structure into which data about the Ionic state of the file should be written
     * @param decryptAttributes the attributes to be used in the context of the decrypt operation
     * @throws IonicException on failure to load or parse header, or specification of an unsupported file format, or
     *                        cipher initialization
     */
    public void init(final FileCryptoFileInfo fileInfo,
                     final FileCryptoDecryptAttributes decryptAttributes) throws IonicException {

        final String version = determinePossibleEncryptionVersion(sourceStream);

        if (FileCipher.OpenXml.V10.LABEL.equals(version)) {
            bodyInput = new OpenXml10BodyInput(sourceStream, agent, fileInfo, decryptAttributes);
        } else if (FileCipher.OpenXml.V11.LABEL.equals(version)) {
            bodyInput = new OpenXml11BodyInput(sourceStream, agent, fileInfo, decryptAttributes);
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }

        try {
            // Each body is responsible for parsing the heading and populating the FileInfo and attributes.
            bodyInput.init();
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        }
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream
     * without blocking by the next invocation of a method for this input stream.
     *
     * @return an estimate of the number of bytes that can be read (or skipped over) from this input stream
     * without blocking
     * @throws IOException if this input stream has been closed, or an I/O error occurs
     */
    public int available() throws IOException {
        return bodyInput.available();
    }

    /**
     * Read the next Ionic-protected block from the input resource.
     *
     * @return the next plainText block extracted from the resource, wrapped in a {@link ByteBuffer} object
     * @throws IOException    on failure reading from the resource
     * @throws IonicException on failure to decrypt the block, or calculate the block signature
     */
    public ByteBuffer read() throws IOException, IonicException {
        if (bodyInput == null) {
            throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
        } else {
            return bodyInput.read();
        }
    }

    /**
     * Finish processing of the input stream.
     *
     * @throws IonicException on failure to verify the file signature (if present)
     */
    public void doFinal() throws IonicException {
        try {
            bodyInput.doFinal();
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_IOSTREAM_ERROR);
        }
    }
}
