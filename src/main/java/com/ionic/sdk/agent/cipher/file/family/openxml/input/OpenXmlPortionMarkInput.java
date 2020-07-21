package com.ionic.sdk.agent.cipher.file.family.openxml.input;

import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.family.openxml.data.OpenXmlZip;
import com.ionic.sdk.agent.request.getkey.GetKeysRequest;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Post process a decrypted file or byte array to check for and decrypt any portion marked sections.
 */
@InternalUseOnly
public final class OpenXmlPortionMarkInput {

    /**
     * The xmlDocument found in the OpenXML file.
     */
    private Document xmlDocument = null;

    /**
     * A list of the XML nodes containing portion marked text.
     */
    private ArrayList<OpenXmlPortionMarkNode> foundNodes = null;

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private KeyServices agent = null;

    /**
     * The attributes to be used in the context of the decrypt operation.
     */
    private FileCryptoDecryptAttributes attributes;

    /**
     * Main body of a docx/docm file.
     */
    private static final String DOCX_BODY_LABEL = "w:body";
    /**
     * Main body portion mark label.
     */
    private static final String DOCX_P_LABEL = "w:p";
    /**
     * Main body portion mark label.
     */
    private static final String DOCX_R_LABEL = "w:r";
    /**
     * Main body portion mark value.
     */
    private static final String DOCX_RSI_LABEL = "w:rsidRPr";
    /**
     * Main body portion mark value.
     */
    private static final String DOCX_RPR_LABEL = "w:rPr";
    /**
     * Main body portion mark value.
     */
    private static final String DOCX_HIGHLIGHT_LABEL = "w:highlight";
    /**
     * Main body portion mark value.
     */
    private static final String DOCX_VAL_LABEL = "w:val";
    /**
     * Main body portion mark value.
     */
    private static final String DOCX_T_LABEL = "w:t";
    /**
     * Green color label.
     */
    private static final String COLOR_GREEN = "green";
    /**
     * Blue color label.
     */
    private static final String COLOR_BLUE = "blue";
    /**
     * Red color label.
     */
    private static final String COLOR_RED = "red";
    /**
     * Dark yellow label.
     */
    private static final String COLOR_DARKYELLOW = "darkYellow";
    /**
     * Restricted level classification.
     */
    private static final String CLASS_RESTRICTED = "Restricted";
    /**
     * Confidential level classification.
     */
    private static final String CLASS_CONFIDENTIAL = "Confidential";
    /**
     * Top Secret level classification.
     */
    private static final String CLASS_TOPSECRET = "Top Secret";
    /**
     * Secret level classification.
     */
    private static final String CLASS_SECRET = "Secret";

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     *
     * @param agent the key services implementation; used to provide keys for cryptography operations
     * @param attributes the attributes to be used in the context of the decrypt operation
     */
    public OpenXmlPortionMarkInput(final KeyServices agent, final FileCryptoDecryptAttributes attributes) {
        this.agent = agent;
        this.attributes = attributes;
    }

   /**
     * Check for portion marked sections in a decrypted document file, and if found, decrypt them and
     * write out a new file.
     *
     * NOTE: Since this is a prototype feature, the document processing is done in RAM.
     *
     * @param inputStream the raw input data containing the protected file content
     * @return true if portion marked sections are found
     */
    public boolean findPortionMarkedSections(final InputStream inputStream) {

        try {
            final ZipInputStream scanZipStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry = null;
            while (null != (zipEntry = scanZipStream.getNextEntry())) {
                if (FileCipher.OpenXml.DOCUMENT_XML_PATH.equals(zipEntry.getName())) {

                    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    final DocumentBuilder builder = factory.newDocumentBuilder();
                    xmlDocument = builder.parse(scanZipStream);
                    xmlDocument.getDocumentElement().normalize();
                    break;
                }
            }
        } catch (IOException e) {
            return false;
        } catch (SAXException e) {
            return false;
        } catch (ParserConfigurationException e) {
            return false;
        }

        if (null == xmlDocument) {
            return false;
        }

        final NodeList nList = xmlDocument.getElementsByTagName(DOCX_BODY_LABEL);
        if (nList.getLength() == 0) {
            return false;
        }
        for (int temp = 0; temp < nList.getLength(); temp++) {
            final Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                final ArrayList<OpenXmlPortionMarkNode> found = processDocxPortions(nNode);
                if (foundNodes != null) {
                    foundNodes.addAll(found);
                } else {
                    foundNodes = found;
                }
            }
         }

         if (foundNodes != null && !foundNodes.isEmpty()) {
            return true;
         }
         return false;
    }

    /**
     * Decrypt Word document Portion Marked (prototype) file give the portion marked sections have
     * previously been discovered using findPortionMarkedSections().
     *
     * @param inputStream the raw input data containing the protected file content
     * @param outputStream  the output stream which has the fully decrypted file.
     * @return True if a new zip was written to the outputStream
     * @throws IonicException on failure to open the .zip file
     */
    public boolean decryptPortionMarkedSections(final InputStream inputStream,
                                             final OutputStream outputStream) throws IonicException {
        boolean decryptedAny = false;
        final GetKeysResponse getKeyResponse = getKeys();
        if (getKeyResponse != null) {
            final AesCtrCipher cipher = new AesCtrCipher();


            for (OpenXmlPortionMarkNode node : foundNodes) {

                final String nodeText = node.getText();
                final int endPos = nodeText.indexOf(CIPHER_BLOCK_SEPARATOR);

                if (endPos != -1) {

                    final String keyId = nodeText.substring(0, endPos);
                    final GetKeysResponse.Key foundKey = getKeyResponse.getKey(keyId);
                    if (foundKey != null) {

                        cipher.setKey(foundKey.getKey());
                        final String encryptedText = nodeText.substring(endPos + 1);
                        final String plainText = cipher.decryptBase64ToString(encryptedText);

                        node.getNode().setNodeValue(plainText);
                        decryptedAny = true;
                    }
                }
            }
        }

        if (decryptedAny) {

            try {
                final ZipInputStream zipStream = new ZipInputStream(inputStream);
                final OpenXmlZip zipOut = new OpenXmlZip(zipStream);

                // Add a modified XML file
                zipOut.addModifiedXmlFile(FileCipher.OpenXml.DOCUMENT_XML_PATH, xmlDocument);

                // Write out the destination file.
                zipOut.openZipFile(outputStream);
                zipOut.writeZipFile();
                zipOut.closeZipFile();

            } catch (IOException e) {
                throw new IonicException(SdkError.ISFILECRYPTO_STREAM_WRITE, e);
            }
        }
        return decryptedAny;
    }

    /**
     * Cipher block separator for encrypted OpenXML 1.0 files.
     */
    private static final int CIPHER_BLOCK_SEPARATOR = '|';

    /**
     * Walk through previously discovered Portion Marked sections, collect the Key Ids, and request those
     * keys from the key service agaent.
     *
     * @throws IonicException on failure to open the .zip file
     * @return The GetKeysResponse from the service.
     */
    private GetKeysResponse getKeys() throws IonicException {
        final HashSet<String> keysRequested = new HashSet<String>();

        final GetKeysRequest getKeysRequest = new GetKeysRequest();
        getKeysRequest.setMetadata(attributes.getMetadata());
        for (OpenXmlPortionMarkNode node : foundNodes) {

            final String nodeText = node.getText();
            final int endPos = nodeText.indexOf(CIPHER_BLOCK_SEPARATOR);

            if (endPos != -1) {
                // add key ID to the key fetch request object
                final String keyId = nodeText.substring(0, endPos);
                if (!keysRequested.contains(keyId)) {

                    getKeysRequest.add(keyId);
                    keysRequested.add(keyId);
                }
            }
        }
        return agent.getKeys(getKeysRequest);
    }


    /**
     * Walk through the w:body node of a Word Document XML file and look for portion marked sections.
     *
     * NOTE: Since this is a prototype feature, the document processing is done in RAM
     *
     * @param xmlNode the XML Node described above
     * @return count of portion marked sections found
     */
    private static ArrayList<OpenXmlPortionMarkNode> processDocxPortions(final Node xmlNode) {

        final ArrayList<OpenXmlPortionMarkNode> foundNodes = new ArrayList<OpenXmlPortionMarkNode>();

        final NodeList nList = xmlNode.getChildNodes();
        for (int index = 0; index < nList.getLength(); index++) {
            final Node item = nList.item(index);
            if (item.getNodeType() == Node.ELEMENT_NODE
                && item.hasAttributes()
                && DOCX_P_LABEL.equals(item.getNodeName())) {

                final NodeList nPList = item.getChildNodes();
                for (int indexP = 0; indexP < nPList.getLength(); indexP++) {
                    final Node itemP = nPList.item(indexP);

                    if (itemP.getNodeType() == Node.ELEMENT_NODE
                        && itemP.hasAttributes()
                        && DOCX_R_LABEL.equals(itemP.getNodeName())) {

                        final Element eElement = (Element) itemP;
                        if (eElement.getAttribute(DOCX_RSI_LABEL).length() > 0) {
                            final OpenXmlPortionMarkNode foundNode = processDocxPortionsRNode(eElement);
                            if (foundNode != null) {
                                foundNodes.add(foundNode);
                            }
                        }
                    }
                }
            }
        }

        return foundNodes;
    }

    /**
     * Walk through an "w:r" node and find highlight, color, and text attributes.
     *
     * NOTE: Since this is a prototype feature, the document processing is done in RAM
     *
     * @param eElement the XML element described above
     * @return A OpenXmlPortionMarkNode if everything is found, or null
     */
    private static OpenXmlPortionMarkNode processDocxPortionsRNode(final Element eElement) {

        String color = null;
        String text = null;
        Node node = null;

        final NodeList nList = eElement.getChildNodes();
        for (int index = 0; index < nList.getLength(); index++) {
            final Node item = nList.item(index);
            if (item.getNodeType() == Node.ELEMENT_NODE
                && DOCX_RPR_LABEL.equals(item.getNodeName())) {

                final NodeList rprList = item.getChildNodes();
                for (int rprIndex = 0; rprIndex < rprList.getLength(); rprIndex++) {
                    final Node rprItem = rprList.item(rprIndex);
                    if (rprItem.getNodeType() == Node.ELEMENT_NODE
                        && DOCX_HIGHLIGHT_LABEL.equals(rprItem.getNodeName())) {

                        final Element higlightElem = (Element) rprItem;
                        color = higlightElem.getAttribute(DOCX_VAL_LABEL);
                    }
                }
            } else if (item.getNodeType() == Node.ELEMENT_NODE
                && DOCX_T_LABEL.equals(item.getNodeName())) {

                final NodeList tList = item.getChildNodes();
                for (int tIndex = 0; tIndex < tList.getLength(); tIndex++) {
                    final Node tItem = tList.item(tIndex);
                    if (tItem.getNodeType() == Node.TEXT_NODE) {

                        text = tItem.getNodeValue();
                        node = tItem;
                    }
                }
            }
        }

        if (color == null || text == null || node == null
            || getHilightColorConfidentiality(color) == null) {
            return null;
        }

        final int endPos = text.indexOf(CIPHER_BLOCK_SEPARATOR);
        if (endPos == -1) {
            // Portion marked section, but NOT encrypted.
            return null;
        }

        return new OpenXmlPortionMarkNode(color, text, node);
    }

    /**
     * Convert color text of a Portion Marked (prototype) section to a classification label.
     *
     * @param color the XML element color code
     * @return A String representing the confidentiality level, or null if the color is unrecognized.
     */
    private static String getHilightColorConfidentiality(final String color) {
        if (color.equals(COLOR_GREEN)) {
            return CLASS_RESTRICTED;
        } else if (color.equals(COLOR_BLUE)) {
            return CLASS_CONFIDENTIAL;
        } else if (color.equals(COLOR_DARKYELLOW)) {
            return CLASS_SECRET;
        } else if (color.equals(COLOR_RED)) {
            return CLASS_TOPSECRET;
        }
        return null;
    }
}
