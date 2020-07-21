package com.ionic.sdk.agent.cipher.file.family.openxml.input;

import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.agent.cipher.file.family.generic.input.GenericInput;
import com.ionic.sdk.agent.cipher.file.family.openxml.OpenXmlUtils;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.key.KeyServices;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.json.JsonObject;

/**
 * OpenXml FileCipher version 1.1 extensions for handling the file body content.
 */
@InternalUseOnly
final class OpenXml11BodyInput implements OpenXmlBodyInput {

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
     * The raw input data stream containing the protected file content.
     */
    private final ZipInputStream inputZipStream;

    /**
     * Whether we found the Ionic Payload in the Zip stream.
     */
    private boolean foundPayload;

    /**
     * Attempt to retrieve the File Info using the Ionic Info JSON file.
     *
     * @param sourceStream the raw input data containing the protected file content
     * @param fileInfo    the structure into which data about the Ionic state of the file should be written
     * @throws IonicException on failure to open the .zip file
     * @return true if the info file is found.
     */
    public static boolean getFileInfo(final InputStream sourceStream,
                                    final FileCryptoFileInfo fileInfo) throws IonicException {

        JsonObject ionicInfo = null;

        // Get Zip Source inputStream
        try {
            final ZipInputStream scanZipStream = new ZipInputStream(sourceStream);
            ZipEntry zipEntry = null;
            while (null != (zipEntry = scanZipStream.getNextEntry())) {
                if (FileCipher.OpenXml.IONIC_INFO_PATH.equals(zipEntry.getName())) {
                    final byte[] jsonFileInfoBytes = OpenXmlUtils.readZipEntry(scanZipStream);
                    ionicInfo = JsonIO.readObject(jsonFileInfoBytes);
                }
            }
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_BAD_ZIP, e);
        }

        if (ionicInfo != null) {
            parseFileInfoFromJson(ionicInfo, fileInfo);
            return true;
        } else {

            // sourceStream should be a FileInputStream or a ByteArrayInputStream
            // The first can be reset by its channel, the second will reset with reset()
            // Any other inputStream type will attempt reset() and throw an exception if it can't.
            try {
                try {
                    final FileInputStream fs = (FileInputStream) sourceStream;
                    fs.getChannel().position(0);
                } catch (ClassCastException e) {
                    sourceStream.reset();
                }
            } catch (IOException e) {
                throw new IonicException(SdkError.ISFILECRYPTO_BAD_ZIP, e);
            }
            return false;
        }
    }

    /**
     * Read through a JSON file and grab the relevant File Info information.
     *
     * @param ionicInfo   the loaded json file from the zip archive that contains the Ionic fileinfo
     * @param fileInfo    the structure into which data about the Ionic state of the file should be written
     * @throws IonicException on failure to open the .zip file
     */
    private static void parseFileInfoFromJson(final JsonObject ionicInfo,
                                      final FileCryptoFileInfo fileInfo) throws IonicException {

        final String family = Value.defaultOnEmpty(JsonSource.getString(ionicInfo, FileCipher.Header.FAMILY),
                                                    FileCipher.OpenXml.FAMILY);
        SdkData.checkTrue(FileCipher.OpenXml.FAMILY.equals(family), SdkError.ISFILECRYPTO_UNRECOGNIZED);
        final String version = JsonSource.getString(ionicInfo, FileCipher.Header.VERSION);
        SdkData.checkTrue(!Value.isEmpty(version), SdkError.ISFILECRYPTO_MISSINGVALUE);
        final String tag = JsonSource.getString(ionicInfo, FileCipher.Header.TAG);
        final String server = JsonSource.getString(ionicInfo, FileCipher.Header.SERVER);

        fileInfo.setEncrypted(true);
        fileInfo.setCipherFamily(CipherFamily.FAMILY_OPENXML);
        fileInfo.setCipherVersion(version);
        fileInfo.setKeyId(tag);
        fileInfo.setServer(server);
    }

    /**
     * Constructor.
     *
     * @param sourceStream the raw input data containing the protected file content
     * @param agent        the key services implementation; used to provide keys for cryptography operations
     * @param fileInfo     the structure into which data about the Ionic state of the file should be written
     * @param attributes   the parameters associated with the decrypt operation
     * @throws IonicException on failure to open the .zip file
     */
    OpenXml11BodyInput(final InputStream sourceStream,
                        final KeyServices agent,
                        final FileCryptoFileInfo fileInfo,
                        final FileCryptoDecryptAttributes attributes) throws IonicException {
        this.fileInfo = fileInfo;
        this.decryptAttributes = attributes;
        this.foundPayload = false;

        // Get Zip Source inputStream
        try {
            inputZipStream = new ZipInputStream(new BufferedInputStream(sourceStream));
            ZipEntry zipEntry = null;
            while (null != (zipEntry = inputZipStream.getNextEntry())) {
                if (FileCipher.OpenXml.IONIC_EMBED_PATH.equals(zipEntry.getName())) {
                    this.foundPayload = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_BAD_ZIP, e);
        }
        this.genericInput = new GenericInput(
                inputZipStream, FileCipher.Generic.V12.BLOCK_SIZE_CIPHER, agent);
    }

    /**
     * Determine if this instance has a valid zip file containing an Ionic Payload.
     * @return True, if the constructor was able to find the Ionic payload in the Zip file.
     */
    public boolean hasValidPayloadStream() {
        return foundPayload;
    }

    @Override
    public void init() throws IOException, IonicException {

        try {
            genericInput.init(fileInfo, decryptAttributes);
        } finally {
            if (fileInfo.getCipherFamily() == CipherFamily.FAMILY_GENERIC) {
                fileInfo.setCipherFamily(CipherFamily.FAMILY_OPENXML);
                fileInfo.setCipherVersion(FileCipher.OpenXml.V11.LABEL);
                decryptAttributes.setFamily(CipherFamily.FAMILY_OPENXML);
                decryptAttributes.setVersion(FileCipher.OpenXml.V11.LABEL);
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
        inputZipStream.close();
    }
}
