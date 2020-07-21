package com.ionic.sdk.agent.cipher.file.family.openxml;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.core.datastructures.Tuple;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Utility class containing various useful functions for an Ionic file cipher operations.
 */
public final class OpenXmlUtils {

    /**
     * Class scoped logger.
     */
    private static final Logger LOGGER = Logger.getLogger(OpenXmlUtils.class.getName());

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private OpenXmlUtils() {
    }

    /**
     * Given a file, determine what type of Office file it is, for example, Document or Spreadsheet,
     * and while we are doing a pass through the file, also grab the Custom Props file out if there.
     *
     * @param fileToEncrypt The source filesystem file entry to check
     * @param getCustomProps Whether to retrieve the Custom Props file during this pass
     * @return The type as a FileType enumeration
     * @throws IonicException on IO errors
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public static Tuple<FileType, byte[]> doFirstPassThrough(final File fileToEncrypt,
                                                             final boolean getCustomProps) throws IonicException {

        LOGGER.fine(String.format("filename = %s", fileToEncrypt.getName()));
        final ZipFile zip;
        FileType fileType = FileType.FILETYPE_UNKNOWN;
        byte[] customPropsFile = null;

        try {
            zip = new ZipFile(fileToEncrypt, ZipFile.OPEN_READ);

        } catch (IOException e) {

            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        }

        ZipEntry content = zip.getEntry(FileCipher.OpenXml.DOCUMENT_XML_PATH);
        if (null != content) {

            if (findContentTypeIsMacro(zip, FileCipher.OpenXml.CONTENT_TYPES_DOCM_MACRO_VALUE)) {
                fileType = FileType.FILETYPE_DOCM;
            } else {
                fileType = FileType.FILETYPE_DOCX;
            }
        }
        content = zip.getEntry(FileCipher.OpenXml.PRESENTATION_XML_PATH);
        if (null != content) {

            if (findContentTypeIsMacro(zip, FileCipher.OpenXml.CONTENT_TYPES_PPTM_MACRO_VALUE)) {
                fileType = FileType.FILETYPE_PPTM;
            } else {
                fileType = FileType.FILETYPE_PPTX;
            }
        }
        content = zip.getEntry(FileCipher.OpenXml.WORKBOOK_XML_PATH);
        if (null != content) {

            if (findContentTypeIsMacro(zip, FileCipher.OpenXml.CONTENT_TYPES_XLSM_MACRO_VALUE)) {
                fileType = FileType.FILETYPE_XLSM;
            } else {
                fileType = FileType.FILETYPE_XLSX;
            }
        }

        if (getCustomProps) {
            customPropsFile = inflateZipArchive(zip, FileCipher.OpenXml.CUSTOM_PATH);
        }

        try {
            zip.close();

        } catch (IOException e) {
            // Ignore this exception.
        }

        return new Tuple<FileType, byte[]>(fileType, customPropsFile);
    }

    /**
     * Given a stream, determine what type of Office file it is, for example, Document or Spreadsheet,
     * and while we are doing a pass through the stream, also grab the Custom Props file out if there.
     *
     * @param sourceStream The source filesystem file entry to check
     * @param getCustomProps Whether to retrieve the Custom Props file during this pass
     * @return The type as a FileType enumeration
     * @throws IonicException on IO errors
     */
    public static Tuple<FileType, byte[]> doFirstPassThrough(final InputStream sourceStream,
                                                             final boolean getCustomProps) throws IonicException {

        boolean foundDocx = false;
        boolean foundPPtx = false;
        boolean foundXlsx = false;
        byte[] contentBytes = null;
        Document contentDoc = null;
        FileType fileType = FileType.FILETYPE_UNKNOWN;
        byte[] customPropsFile = null;

        try {
            final ZipInputStream zip = new ZipInputStream(sourceStream);

            ZipEntry zipEntry = null;
            while (null != (zipEntry = zip.getNextEntry())) {
                if (FileCipher.OpenXml.CONTENT_TYPES_XML_PATH.equals(zipEntry.getName())) {
                   contentBytes = readZipEntryInternal(zip);
                } else if (FileCipher.OpenXml.CUSTOM_PATH.equals(zipEntry.getName()) && getCustomProps) {
                    customPropsFile = readZipEntryInternal(zip);
                } else if (FileCipher.OpenXml.DOCUMENT_XML_PATH.equals(zipEntry.getName())) {
                    foundDocx = true;
                } else if (FileCipher.OpenXml.PRESENTATION_XML_PATH.equals(zipEntry.getName())) {
                    foundPPtx = true;
                } else if (FileCipher.OpenXml.WORKBOOK_XML_PATH.equals(zipEntry.getName())) {
                    foundXlsx = true;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        }

        String searchName = "";

        if (foundDocx) {
            searchName = FileCipher.OpenXml.CONTENT_TYPES_DOCM_MACRO_VALUE;
        } else if (foundPPtx) {
            searchName = FileCipher.OpenXml.CONTENT_TYPES_PPTM_MACRO_VALUE;
        } else if (foundXlsx) {
            searchName = FileCipher.OpenXml.CONTENT_TYPES_XLSM_MACRO_VALUE;
        }
        boolean foundMacro = false;

        if (contentBytes != null) {

            try {
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder builder = factory.newDocumentBuilder();
                contentDoc = builder.parse(new ByteArrayInputStream(contentBytes));
                contentDoc.getDocumentElement().normalize();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
                throw new IonicException(SdkError.ISFILECRYPTO_BAD_ZIP, e);
            } catch (ParserConfigurationException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
                throw new IonicException(SdkError.ISFILECRYPTO_BAD_ZIP, e);
            } catch (SAXException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
                throw new IonicException(SdkError.ISFILECRYPTO_BAD_ZIP, e);
            }

            final NodeList nList = contentDoc.getElementsByTagName(FileCipher.OpenXml.OVERRIDE_KEY_LABEL);
            for (int temp = 0; temp < nList.getLength(); temp++) {
                final Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    final Element eElement = (Element) nNode;
                    final String contentType = eElement.getAttribute(FileCipher.OpenXml.CONTENT_TYPE_KEY_LABEL);
                    if (searchName.equals(contentType)) {
                        foundMacro = true;
                    }
                }
            }
        }

        if (foundDocx) {
            fileType = foundMacro ? FileType.FILETYPE_DOCM : FileType.FILETYPE_DOCX;
        } else if (foundPPtx) {
            fileType = foundMacro ? FileType.FILETYPE_PPTM : FileType.FILETYPE_PPTX;
        } else if (foundXlsx) {
            fileType = foundMacro ? FileType.FILETYPE_XLSM : FileType.FILETYPE_XLSX;
        } else {
            fileType = FileType.FILETYPE_UNKNOWN;
        }

        return new Tuple<FileType, byte[]>(fileType, customPropsFile);
    }

    /**
     * Given a Zip Archive, find and verify a macro content file within.
     *
     * @param zip An open ZipFile archive
     * @param searchName The name of the attribute to check for in the "[Content_Types].xml" file
     * @return True if the file is there and verified, false otherwise
     * @throws IonicException on IO errors
     */
    private static boolean findContentTypeIsMacro(final ZipFile zip, final String searchName) throws IonicException {

        LOGGER.fine(String.format("searchName = %s", searchName));
        // inflate the zip file into the DOM Parser
        final Document doc = inflateZipArchiveXml(zip, FileCipher.OpenXml.CONTENT_TYPES_XML_PATH);
        if (doc == null) {
            LOGGER.fine(String.format("archive = %s was not found.",
                                        FileCipher.OpenXml.CONTENT_TYPES_XML_PATH));
            return false;
        }

        final NodeList nList = doc.getElementsByTagName(FileCipher.OpenXml.OVERRIDE_KEY_LABEL);
        for (int temp = 0; temp < nList.getLength(); temp++) {
            final Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                final Element eElement = (Element) nNode;
                final String contentType = eElement.getAttribute(FileCipher.OpenXml.CONTENT_TYPE_KEY_LABEL);
                if (searchName.equals(contentType)) {
                    return true;
                }
            }
        }

        LOGGER.fine(String.format("node = %s was not found.", searchName));
        return false;
    }


    /**
     * Given a zip file, deflate the [Content_Types].xml file and return it as a Document.
     *
     * @param zip An open ZipFile archive
     * @return An XML Document representing the [Content_Types].xml file.
     * @throws IonicException on IO errors
     */
    public static Document getContentsXml(final ZipFile zip) throws IonicException {

        // inflate the zip file into the DOM Parser
        return inflateZipArchiveXml(zip, FileCipher.OpenXml.CONTENT_TYPES_XML_PATH);
    }

    /**
     * Given a zip file, deflate the ppt/presentation.xml file and return it as a Document.
     *
     * @param zip An open ZipFile archive
     * @return An XML Document representing the ppt/presentation.xml file.
     * @throws IonicException on IO errors
     */
    public static Document getPowerPointDocumentXml(final ZipFile zip) throws IonicException {

        // inflate the zip file into the DOM Parser
        return inflateZipArchiveXml(zip, FileCipher.OpenXml.PRESENTATION_XML_PATH);
    }

    /**
     * Given a zip file, deflate the xl/workbook.xml file and return it as a Document.
     *
     * @param zip An open ZipFile archive
     * @return An XML Document representing the xl/workbook.xml file.
     * @throws IonicException on IO errors
     */
    public static Document getExcelDocumentXml(final ZipFile zip) throws IonicException {

        // inflate the zip file into the DOM Parser
        return inflateZipArchiveXml(zip, FileCipher.OpenXml.WORKBOOK_XML_PATH);
    }

    /**
     * Given a zip file, deflate the word/document.xml file and return it as a Document.
     *
     * @param zip An open ZipFile archive
     * @return An XML Document representing the word/document.xml file.
     * @throws IonicException on IO errors
     */
    public static Document getWordDocumentXml(final ZipFile zip) throws IonicException {

        // inflate the zip file into the DOM Parser
        return inflateZipArchiveXml(zip, FileCipher.OpenXml.DOCUMENT_XML_PATH);
    }

    /**
     * Given a zip file, deflate the [Content_Types].xml file and return it as a Document.
     *
     * @param is Any InputStream aimed at an OpenXml file, but meant for the coverpage file.
     * @return Two XML Documents representing the [Content_Types].xml file and the _rels/.rels file.
     * @throws IonicException on IO errors
     */
    public static Tuple<Document, Document> getContentsAndRelationsFilesFromStream(final InputStream is)
                                                                                    throws IonicException {

        byte[] contentBytes = null;
        byte[] relationBytes = null;
        Document contents = null;
        Document relations = null;

        try {
            final ZipInputStream zip = new ZipInputStream(is);

            ZipEntry zipEntry = null;
            while (null != (zipEntry = zip.getNextEntry())) {
                if (FileCipher.OpenXml.CONTENT_TYPES_XML_PATH.equals(zipEntry.getName())) {
                   contentBytes = readZipEntryInternal(zip);
                } else if (FileCipher.OpenXml.RELS_XML_PATH.equals(zipEntry.getName())) {
                    relationBytes = readZipEntryInternal(zip);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        }

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();

            if (contentBytes != null) {
                contents = builder.parse(new ByteArrayInputStream(contentBytes));
                contents.getDocumentElement().normalize();
            }
            if (relationBytes != null) {
                relations = builder.parse(new ByteArrayInputStream(relationBytes));
                relations.getDocumentElement().normalize();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IonicException(SdkError.ISFILECRYPTO_BAD_ZIP, e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IonicException(SdkError.ISFILECRYPTO_BAD_ZIP, e);
        } catch (SAXException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IonicException(SdkError.ISFILECRYPTO_BAD_ZIP, e);
        }

        return new Tuple<Document, Document>(contents, relations);
    }

    /**
     * Given a zip file, deflate the [Content_Types].xml file and return it as a Document.
     *
     * @param doc An XML document - should be obtained from getContentsXml(..)
     * @param sExtension A string file extension to register in the [Content_Types].xml file.
     * @param sContentType A string content type to register in the [Content_Types].xml file.
     */
    public static void registerOpenXmlContentType(final Document doc,
                                                  final String sExtension,
                                                  final String sContentType) {

        boolean bFound = false;
        final NodeList nList = doc.getElementsByTagName(FileCipher.OpenXml.DEFAULT_KEY_LABEL);
        for (int temp = 0; temp < nList.getLength(); temp++) {
            final Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                final Element eElement = (Element) nNode;
                final String extensionAttr = eElement.getAttribute(FileCipher.OpenXml.EXTENSION_KEY_LABEL);
                if (Objects.equals(sExtension, extensionAttr)) {

                    bFound = true;
                    eElement.setAttribute(FileCipher.OpenXml.EXTENSION_KEY_LABEL, sExtension);
                    eElement.setAttribute(FileCipher.OpenXml.CONTENT_TYPE_KEY_LABEL, sContentType);
                }
            }
        }
        if (!bFound) {

            final Element eElement = doc.createElement(FileCipher.OpenXml.DEFAULT_KEY_LABEL);
            eElement.setAttribute(FileCipher.OpenXml.EXTENSION_KEY_LABEL, sExtension);
            eElement.setAttribute(FileCipher.OpenXml.CONTENT_TYPE_KEY_LABEL, sContentType);
            doc.getDocumentElement().appendChild(eElement);
        }
    }

    /**
     * Given a zip file, deflate the [Content_Types].xml file and return it as a Document.
     *
     * @param doc An XML document - should be obtained from getContentsXml(..)
     * @param sPartName A string part name to register in the [Content_Types].xml file.
     * @param sContentType A string content type to register in the [Content_Types].xml file.
     */
    public static void registerOpenXmlContentTypeOverride(final Document doc,
                                                          final String sPartName,
                                                          final String sContentType) {
        boolean bFound = false;
        final NodeList nList = doc.getElementsByTagName(FileCipher.OpenXml.OVERRIDE_KEY_LABEL);
        for (int temp = 0; temp < nList.getLength(); temp++) {
            final Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                final Element eElement = (Element) nNode;
                final String partNameAttr = eElement.getAttribute(FileCipher.OpenXml.PART_NAME_KEY_LABEL);
                if (Objects.equals(sPartName, partNameAttr)) {

                    bFound = true;
                    eElement.setAttribute(FileCipher.OpenXml.PART_NAME_KEY_LABEL, sPartName);
                    eElement.setAttribute(FileCipher.OpenXml.CONTENT_TYPE_KEY_LABEL, sContentType);
                }
            }
        }
        if (!bFound) {

            final Element eElement = doc.createElement(FileCipher.OpenXml.OVERRIDE_KEY_LABEL);
            eElement.setAttribute(FileCipher.OpenXml.PART_NAME_KEY_LABEL, sPartName);
            eElement.setAttribute(FileCipher.OpenXml.CONTENT_TYPE_KEY_LABEL, sContentType);
            doc.getDocumentElement().appendChild(eElement);
        }
    }

    /**
     * Given the _rels/.rels Document, add / modify a relationship.
     *
     * @param doc An XML document - should be obtained from getContentsXml(..)
     * @param sType A string type name to register in the _rels/.rels file.
     * @param sTarget A string path name to register in the _rels/.rels file.
     * @param sId A string ID name to register in the _rels/.rels file.
     */
    public static void registerOpenXmlRelationship(final Document doc,
                                                   final String sType,
                                                   final String sTarget,
                                                   final String sId) {
        boolean bFound = false;
        final Element rootNode = doc.getDocumentElement();
        if (FileCipher.OpenXml.RELATIONSHIP_ROOT_LABEL.equals(rootNode.getNodeName())) {
            final NodeList nList = doc.getElementsByTagName(FileCipher.OpenXml.RELATIONSHIP_LABEL);
            for (int temp = 0; temp < nList.getLength(); temp++) {
                final Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    final Element eElement = (Element) nNode;
                    final String targetAttr = eElement.getAttribute(FileCipher.OpenXml.RELATIONSHIP_TARGET_LABEL);
                    if (Objects.equals(sTarget, targetAttr)) {

                        bFound = true;
                        eElement.setAttribute(FileCipher.OpenXml.RELATIONSHIP_ID_LABEL, sId);
                        eElement.setAttribute(FileCipher.OpenXml.RELATIONSHIP_TARGET_LABEL, sTarget);
                        eElement.setAttribute(FileCipher.OpenXml.RELATIONSHIP_TYPE_LABEL, sType);
                    }
                }
            }
            if (!bFound) {

                final Element eElement = doc.createElement(FileCipher.OpenXml.RELATIONSHIP_LABEL);
                eElement.setAttribute(FileCipher.OpenXml.RELATIONSHIP_ID_LABEL, sId);
                eElement.setAttribute(FileCipher.OpenXml.RELATIONSHIP_TARGET_LABEL, sTarget);
                eElement.setAttribute(FileCipher.OpenXml.RELATIONSHIP_TYPE_LABEL, sType);
                rootNode.appendChild(eElement);
            }
        }
    }

    /**
     * Given a zip file, deflate a named entry into a byte[] buffer.
     *
     * @param zip An open ZipFile archive
     * @param archiveName The name of the file to inflate
     * @return A byte array containing the file contents
     * @throws IonicException on IO errors
     */
    private static byte[] inflateZipArchive(final ZipFile zip, final String archiveName) throws IonicException {

        byte[] output = null;
        final ZipEntry content = zip.getEntry(archiveName);
        final InputStream ins;

        if (null == content) {
            return null;
        }

        try {
            ins = zip.getInputStream(content);
            output = readZipEntryInternal(ins);
            ins.close();

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        }
        return output;
    }

    /**
     * Given a zip file, deflate a named entry and parse it into an XML Document.
     *
     * @param zip An open ZipFile archive
     * @param archiveName The name of the file to inflate
     * @return A byte array containing the file contents
     * @throws IonicException on IO errors
     */
    private static Document inflateZipArchiveXml(final ZipFile zip, final String archiveName) throws IonicException {

        LOGGER.fine(String.format("archiveName = %s", archiveName));

        final ZipEntry content = zip.getEntry(archiveName);
        final InputStream ins;

        if (null == content) {
            LOGGER.fine(String.format("archive = %s was not found.", archiveName));
            return null;
        }

        try {
            ins = zip.getInputStream(content);

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(ins);
            doc.getDocumentElement().normalize();
            ins.close();

            return doc;

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IonicException(SdkError.ISFILECRYPTO_OPENFILE, e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED, e);
        } catch (SAXException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED, e);
        }
    }

    /**
     * Read an open stream into a byte buffer.
     * @param zis A ZipInputStream set to a particular entry
     * @throws IOException on IO parsing errors
     * @return A byte array containing the data at the current entry.
     */
    public static byte[] readZipEntry(final ZipInputStream zis) throws IOException {
        return readZipEntryInternal(zis);
    }

    /**
     * Read an open stream into a byte buffer.
     * @param is Any InputStream, but intended for ZipInputStream
     * @throws IOException on IO parsing errors
     * @return A byte array containing the data at the current entry.
     */
    private static byte[] readZipEntryInternal(final InputStream is) throws IOException {

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] block = new byte[FileCipher.OpenXml.ZIPFILE_BLOCK_SIZE];

        while (is.available() > 0) {
            final int length = is.read(block);
            if (length > 0) {
                bos.write(block, 0, length);
            }
        }
        return bos.toByteArray();
    }

    /**
     * Convert the XML file into a byte string.
     * @param doc The XML document
     * @throws IonicException on XML parsing errors
     * @return A byte array containing the string representation of the XML file.
     */
    public static byte[] convertDocumentToByteArray(final Document doc) throws IonicException {

        final DOMSource source = new DOMSource(doc);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final StreamResult result = new StreamResult(bos);

        // Use a Transformer for output
        final TransformerFactory tFactory = TransformerFactory.newInstance();
        try {
            final Transformer transformer = tFactory.newTransformer();
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED, e);
        }

        return bos.toByteArray();
    }
}
