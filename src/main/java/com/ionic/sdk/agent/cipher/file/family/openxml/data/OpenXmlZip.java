package com.ionic.sdk.agent.cipher.file.family.openxml.data;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.family.openxml.OpenXmlUtils;
import com.ionic.sdk.error.IonicException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;

/**
 * In memory data needed to re-assemble the final output zip file. It contains
 * a coverpage zip, a stream reference to the encrypted package, and an array
 * of in memory XML files to add or replace in the coverpage zip.
 */
public class OpenXmlZip {

    /**
     * Class scoped logger.
     */
    private static final Logger LOGGER = Logger.getLogger(OpenXmlZip.class.getName());

    /**
     * The cover page.
     */
    private final ZipInputStream zipInputStream;

    /**
     * The output stream created from OpenZipFile and used by the write and close functions.
     */
    private ZipOutputStream zipOutputStream = null;

    /**
     * A mapping of modified XML files to include in the zip file.
     */
    private HashMap<String, Document> modifiedXmlDocs;

    /**
     * A list of new files to include in the zip file.
     */
    private ArrayList<OpenXmlZipEntry> newEntries;

    /**
     * ZipOutputStream getter.
     * @return ZipOutputStream created by openZipFile
     */
    public ZipOutputStream getZipOutputStream() {
        return zipOutputStream;
    }

    /**
     * Constructor.
     * @param zipInputStream The starting zip file - for encryption, it will be the coverpage zip
     */
    public OpenXmlZip(final ZipInputStream zipInputStream) {
        this.zipInputStream = zipInputStream;
        this.modifiedXmlDocs = new HashMap<String, Document>();
        this.newEntries = new ArrayList<OpenXmlZipEntry>();
    }

    /**
     * Register an updated XML file to override any current version.
     * @param entryName The name or file path of the document file
     * @param updatedXML The updated XML file
     */
    public final void addModifiedXmlFile(final String entryName, final Document updatedXML) {
        modifiedXmlDocs.put(entryName, updatedXML);
    }

    /**
     * Register another file for inclusion in the zip output.
     * @param zipEntry The other file represented by an OpenXmlZipEntry
     */
    public final void addNewEntry(final OpenXmlZipEntry zipEntry) {
        newEntries.add(zipEntry);
    }

    /**
     * Open the modified zip at the destination file in preparation to write.
     * @param outputFile The output filesystem file
     * @throws IOException on IO errors
     * @return Returns the ZipOutputStream
     */
    public ZipOutputStream openZipFile(final File outputFile) throws IOException {
        LOGGER.fine(String.format("output = %s", outputFile.getName()));
        zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        return zipOutputStream;
    }

    /**
     * Open the modified zip at the destination file in preparation to write.
     * @param outputStream The output stream
     * @throws IOException on IO errors
     * @return Returns the ZipOutputStream
     */
    public ZipOutputStream openZipFile(final OutputStream outputStream) throws IOException {
        zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
        return zipOutputStream;
    }

    /**
     * Write out the modified zip to the destination file.
     * @throws IonicException on XML parsing errors
     * @throws IOException on IO errors
     */
    public void writeZipFile() throws IOException, IonicException {
        LOGGER.fine("Writing the file.");

        final byte[] block = new byte[FileCipher.OpenXml.ZIPFILE_BLOCK_SIZE];

        ZipEntry entry = null;
        while (null != (entry = zipInputStream.getNextEntry())) {

            LOGGER.fine(String.format("Entry = %s", entry.getName()));

            final ZipEntry entryCopy = new ZipEntry(entry);

            // reset the compressed size value - since we may not match the original value.
            entryCopy.setCompressedSize(-1);

            final Document doc = modifiedXmlDocs.get(entry.getName());
            if (doc != null) {

                final byte[] docAsBytes = OpenXmlUtils.convertDocumentToByteArray(doc);

                entryCopy.setSize(docAsBytes.length);
                entryCopy.setTime(System.currentTimeMillis());
                zipOutputStream.putNextEntry(entryCopy);
                zipOutputStream.write(docAsBytes);
                zipOutputStream.closeEntry();

            } else {

                zipOutputStream.putNextEntry(entryCopy);
                while (zipInputStream.available() > 0) {
                    final int length = zipInputStream.read(block);
                    if (length > 0) {
                        zipOutputStream.write(block, 0, length);
                    }
                }
                zipOutputStream.closeEntry();
            }
        }

        for (OpenXmlZipEntry oxEntry : newEntries) {
            oxEntry.addEntryToOpenXmlZip(this);
        }
    }

    /**
     * Close the modified zip file.
     * @throws IOException on IO errors
     */
    public void closeZipFile() throws IOException {
        LOGGER.fine("Closing the file.");

        zipOutputStream.finish();
        zipOutputStream.flush();
        zipOutputStream.close();
        zipOutputStream = null;
    }
}
