package com.ionic.sdk.agent.cipher.file.family.openxml.data;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.family.generic.output.GenericOutput;
import com.ionic.sdk.error.IonicException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * In memory data needed to re-assemble the final output zip file. This class
 * contains a particular zip entry from a variety of possible sources.
 */
public class OpenXmlZipEntry {

    /**
     * Zip entry - stores name and dates.
     */
    private ZipEntry entry;

    /**
     * Data source.
     */
    private InputStream inputStream;

    /**
     * Encryption module - file content can be encrypted as it is copied into the destination zip file.
     */
    private GenericOutput encryptor;

    /**
     * Constructor OpenXmlZipEntry - works with OpenXmlZip to add various other data sources to a new Zip file.
     * @param zip Alternate zip file to copy an entry from
     * @param entryToCopy Name of the entry to copy
     * @throws IOException on IO errors
     */
    public OpenXmlZipEntry(final ZipFile zip, final String entryToCopy) throws IOException {

        final ZipEntry content = zip.getEntry(entryToCopy);
        if (content != null) {

            entry = new ZipEntry(content);
            entry.setCompressedSize(-1); // reset the compressed size value - since we may not match the original value.
            inputStream = zip.getInputStream(content);
        }
    }

    /**
     * Constructor OpenXmlZipEntry - works with OpenXmlZip to add various other data sources to a new Zip file.
     * @param inputFile Generic input file to add to the new zip file.
     * @throws FileNotFoundException on IO errors
     */
    public OpenXmlZipEntry(final File inputFile) throws FileNotFoundException {

        final FileInputStream fis = new FileInputStream(inputFile);
        this.inputStream = new BufferedInputStream(fis);
        entry = new ZipEntry(inputFile.getName());
        entry.setTime(inputFile.lastModified());
        entry.setSize(inputFile.length());
    }

    /**
     * Constructor OpenXmlZipEntry - works with OpenXmlZip to add various other data sources to a new Zip file.
     * @param inputStream Generic input stream to add to the new zip file.
     * @param filename Name of the entry
     */
    public OpenXmlZipEntry(final InputStream inputStream, final String filename) {

        this.inputStream = inputStream;
        entry = new ZipEntry(filename);
        entry.setTime(System.currentTimeMillis());
    }

    /**
     * Constructor OpenXmlZipEntry - works with OpenXmlZip to add various other data sources to a new Zip file.
     * @param inputStream Generic input stream to add to the new zip file.
     * @param filename Name of the entry
     * @param size total size of the entry
     */
    public OpenXmlZipEntry(final InputStream inputStream, final String filename, final long size) {

        this.inputStream = inputStream;
        entry = new ZipEntry(filename);
        entry.setTime(System.currentTimeMillis());
        entry.setSize(size);
    }

    /**
     * Set an encryptor on this entry.
     * NOTE: The encryptor must be a fully streamed encryptor with no seeks.
     * Generic v1.3 is the only version that will work here.
     *
     * @param encryptor Generic encryptor (output) to use in encrypting the entry
     */
    public final void setEncryption(final GenericOutput encryptor) {
        this.encryptor = encryptor;
    }

    /**
     * Write this data source as a Zip entry to a ZipOutputStream.
     *
     * @param openXmlZip OpenXmlZip that has been opened and has an output stream to write the entry.
     * @throws IonicException on encryption errors
     * @throws IOException on IO errors
     */
    final void addEntryToOpenXmlZip(final OpenXmlZip openXmlZip) throws IOException, IonicException {

        final ZipOutputStream zos = openXmlZip.getZipOutputStream();

        if (entry != null && inputStream != null) {

            zos.putNextEntry(entry);

            if (encryptor == null) {

                final byte[] block = new byte[FileCipher.OpenXml.ZIPFILE_BLOCK_SIZE];

                while (inputStream.available() > 0) {
                    final int length = inputStream.read(block);
                    if (length > 0) {
                        zos.write(block, 0, length);
                    }
                }

            } else {
                final ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
                final ByteBuffer bufferPlainText = encryptor.getPlainText();
                while (inputStream.available() > 0) {
                    final int length = inputChannel.read(bufferPlainText);
                    if (length > 0) {
                        bufferPlainText.limit(bufferPlainText.position());
                        bufferPlainText.position(0);
                        encryptor.write(bufferPlainText);
                    }
                }
                encryptor.doFinal();
            }
            zos.closeEntry();
        }
    }
}
