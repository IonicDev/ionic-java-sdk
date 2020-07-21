package com.ionic.sdk.agent.cipher.file.family.openxml.output;

import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesInterface;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.agent.cipher.file.family.generic.output.GenericOutput;
import com.ionic.sdk.agent.cipher.file.family.openxml.data.OpenXmlZip;
import com.ionic.sdk.agent.cipher.file.family.openxml.data.OpenXmlZipEntry;
import com.ionic.sdk.agent.cipher.file.family.openxml.OpenXmlUtils;
import com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.datastructures.Tuple;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonTarget;
import com.ionic.sdk.key.KeyServices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipInputStream;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.w3c.dom.Document;

/**
 * Extensions for handling output of {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher}
 * version 1.0 file body content.
 */
@InternalUseOnly
final class OpenXml11BodyOutput implements OpenXmlBodyOutput {

    /**
     * The raw output data stream that is to contain the protected file content.
     */
    private final OutputStream targetStream;

    /**
     * The agent the key services implementation; used to provide keys for cryptography operations.
     */
    private final KeyServices agent;

    /**
     * The cover page services implementation.
     */
    private final FileCryptoCoverPageServicesInterface coverPageServices;

    /**
     * Parameters associated with the decrypt operation.
     */
    private final FileCryptoEncryptAttributes encryptAttributes;

    /**
     * Wrapped cipher implementing protection of CSV content.
     */
    private GenericOutput genericOutput = null;

    /**
     * Object representing the output zip file and all the various elements the make it up.
     */
    private OpenXmlZip outputZip = null;

    /**
     * The speific OpenXML file type discovered in an early step.
     */
    private final FileType fileType;

    /**
     * An optional custom properties file to include in the coverpage file.
     */
    private final byte[] customPropFile;

    /**
     * The contents XML doc from the coverpage.
     */
    private Document contents = null;

    /**
     * The relationships XML doc from the coverpage.
     */
    private Document relationships = null;

    /**
     * an optional filename to use as a temp file during the process.  Can be null.
     */
    private final File tempFile;

    /**
     * The temp file stream, if it is not null.
     */
    private FileOutputStream fileOutputStream = null;

    /**
     * The temp file stream, if it is not null.
     */
    private ByteArrayOutputStream byteOutputStream = null;

    /**
     * Constructor.
     *
     * @param targetStream the raw output data containing the protected file content
     * @param agent the key services implementation; used to provide keys for cryptography operations
     * @param coverPageServices the cover page services implementation
     * @param encryptAttributes a container for applying desired configuration to the operation,
     * @param fileType the speific OpenXML file type discovered in an early step
     * @param customPropFile an optional custom properties file to include in the coverpage file
     * @param tempFile      an optional filename to use as a temp file during the process
     *                      if this param is null, the process will use RAM buffer.
     */
    OpenXml11BodyOutput(final OutputStream targetStream, final KeyServices agent,
                        final FileCryptoCoverPageServicesInterface coverPageServices,
                        final FileCryptoEncryptAttributes encryptAttributes,
                        final FileType fileType,
                        final byte[] customPropFile,
                        final File tempFile) {
        this.targetStream = targetStream;
        this.agent = agent;
        this.coverPageServices = coverPageServices;
        this.encryptAttributes = encryptAttributes;
        this.fileType = fileType;
        this.customPropFile = customPropFile;
        this.tempFile = tempFile;
    }

    @Override
    public void init() throws IOException, IonicException {
        final ByteArrayInputStream coverpageStream = new ByteArrayInputStream(coverPageServices.getCoverPage(fileType));
        final Tuple<Document, Document> docs = OpenXmlUtils.getContentsAndRelationsFilesFromStream(coverpageStream);
        contents = docs.first();
        relationships = docs.second();
        coverpageStream.reset();

        // Update the content references:
        OpenXmlUtils.registerOpenXmlContentType(contents,
                                                FileCipher.OpenXml.ION_CONTENT_TYPE_EXT,
                                                FileCipher.OpenXml.ION_CONTENT_TYPE);
        if (customPropFile != null) {
            OpenXmlUtils.registerOpenXmlContentTypeOverride(contents,
                                                            FileCipher.OpenXml.CUSTOM_CONTENT_TYPE_PART,
                                                            FileCipher.OpenXml.CUSTOM_CONTENT_TYPE);
        }

        // Update the Relationships:
        OpenXmlUtils.registerOpenXmlRelationship(relationships,
                                                 FileCipher.OpenXml.IONIC_EMBED_TYPE,
                                                 FileCipher.OpenXml.IONIC_EMBED_PATH,
                                                 FileCipher.OpenXml.IONIC_EMBED_REL_ID);
        OpenXmlUtils.registerOpenXmlRelationship(relationships,
                                                 FileCipher.OpenXml.IONIC_INFO_TYPE,
                                                 FileCipher.OpenXml.IONIC_INFO_PATH,
                                                 FileCipher.OpenXml.IONIC_INFO_REL_ID);
        if (customPropFile != null) {
            OpenXmlUtils.registerOpenXmlRelationship(relationships,
                                                     FileCipher.OpenXml.CUSTOM_TYPE,
                                                     FileCipher.OpenXml.CUSTOM_PATH,
                                                     FileCipher.OpenXml.CUSTOM_REL_ID);
        }

        outputZip = new OpenXmlZip(new ZipInputStream(coverpageStream));
        outputZip.addModifiedXmlFile(FileCipher.OpenXml.CONTENT_TYPES_XML_PATH, contents);
        outputZip.addModifiedXmlFile(FileCipher.OpenXml.RELS_XML_PATH, relationships);

        OutputStream tempOutput = null;
        if (tempFile != null) {
            fileOutputStream = new FileOutputStream(tempFile);
            tempOutput = fileOutputStream;
        } else {
            byteOutputStream = new ByteArrayOutputStream();
            tempOutput = byteOutputStream;
        }
        final FileCryptoEncryptAttributes genericEncryptAttributes =
                new FileCryptoEncryptAttributes(FileCipher.Generic.V12.LABEL,
                                                encryptAttributes.getKeyAttributes(),
                                                encryptAttributes.getMutableKeyAttributes());
        genericEncryptAttributes.setMetadata(encryptAttributes.getMetadata());
        genericOutput = new GenericOutput(tempOutput, FileCipher.Generic.V12.BLOCK_SIZE_PLAIN, agent);
        genericOutput.init(genericEncryptAttributes);
        encryptAttributes.setKeyResponse(genericEncryptAttributes.getKeyResponse());
    }

    @Override
    public void doEncryption(final InputStream plainText) throws IOException, IonicException {
        final ReadableByteChannel plainChannel = Channels.newChannel(plainText);

        // Step one, encrypt to the tempFile:
        InputStream encryptedPayloadStream = null;
        final ByteBuffer bufferPlainText = genericOutput.getPlainText();

        while (plainText.available() > 0) {
            final int length = plainChannel.read(bufferPlainText);
            if (length > 0) {
                bufferPlainText.limit(bufferPlainText.position());
                bufferPlainText.position(0);
                genericOutput.write(bufferPlainText);
            }
        }
        genericOutput.doFinal();
        final byte[] signature = genericOutput.getSignature();
        final long fileSize;
        if (fileOutputStream != null) {
            if (signature != null) {
                fileOutputStream.getChannel().position(genericOutput.getHeaderLength());
                fileOutputStream.write(signature);
            }
            fileOutputStream.close();
            encryptedPayloadStream = new FileInputStream(tempFile);
            fileSize = tempFile.length();
        } else {
            final byte[] bytes = byteOutputStream.toByteArray();
            if (signature != null) {
                System.arraycopy(signature, 0, bytes,
                                   genericOutput.getHeaderLength(), signature.length);
            }
            encryptedPayloadStream = new ByteArrayInputStream(bytes);
            fileSize = bytes.length;
        }

        // Step Two, write out the final zip file
        final OpenXmlZipEntry payloadEntry = new OpenXmlZipEntry(
                encryptedPayloadStream, FileCipher.OpenXml.IONIC_EMBED_PATH, fileSize);
        outputZip.addNewEntry(payloadEntry);

        if (customPropFile != null) {
            final OpenXmlZipEntry propEntry = new OpenXmlZipEntry(new ByteArrayInputStream(customPropFile),
                                                            FileCipher.OpenXml.CUSTOM_PATH,
                                                            customPropFile.length);
            outputZip.addNewEntry(propEntry);
        }

        final byte[] jsonInfo = Transcoder.utf8().decode(createJsonInfo());
        final OpenXmlZipEntry infoEntry = new OpenXmlZipEntry(
                                        new ByteArrayInputStream(jsonInfo),
                                        FileCipher.OpenXml.IONIC_INFO_PATH,
                                        jsonInfo.length);
        outputZip.addNewEntry(infoEntry);

        // Write out the destination file.
        outputZip.openZipFile(targetStream);
        outputZip.writeZipFile();
        outputZip.closeZipFile();

        encryptedPayloadStream.close();
    }

    /**
     * Ionic info file JSON field label.
     */
    private static final String JSON_FIELD_FAMILY = "family";
    /**
     * Ionic info file JSON field label.
     */
    private static final String JSON_FIELD_VERSION = "version";
    /**
     * Ionic info file JSON field label.
     */
    private static final String JSON_FIELD_SERVER = "server";
    /**
     * Ionic info file JSON field label.
     */
    private static final String JSON_FIELD_TAG = "tag";
    /**
     * Ionic info file JSON field label.
     */
    private static final String JSON_FIELD_PORTION_MARKING = "portionMarkingEnabled";

    /**
     * Create the Ionic info file.
     *
     * @return JSON as a string containing the Ionic file info
     */
    private String createJsonInfo() {
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        JsonTarget.addNotNull(objectBuilder, JSON_FIELD_FAMILY, FileCipher.OpenXml.FAMILY);
        JsonTarget.addNotNull(objectBuilder, JSON_FIELD_VERSION, OpenXmlFileCipher.VERSION_LATEST);
        JsonTarget.addNotNull(objectBuilder, JSON_FIELD_SERVER, agent.getActiveProfile().getServer());
        JsonTarget.addNotNull(objectBuilder, JSON_FIELD_TAG, encryptAttributes.getKeyId());
        JsonTarget.add(objectBuilder, JSON_FIELD_PORTION_MARKING, 0); // We do not create Portion marking in Java.
        return JsonIO.write(objectBuilder.build(), true);
    }
}
