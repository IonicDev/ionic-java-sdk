package com.ionic.sdk.agent.cipher.file.family.pdf.io;

import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfArray;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfDictionary;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfObject;
import com.ionic.sdk.agent.cipher.file.family.pdf.data.PdfString;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Utility class used to deserialize a PDF dictionary into an in memory map.
 */
@InternalUseOnly
public final class PdfDictionaryReader {

    /**
     * A simple text scanner used to parse the PDF dictionary using regular expressions.
     */
    private final Scanner scanner;

    /**
     * The amount of Scanner text encompassing the dictionary.
     */
    private int count;

    /**
     * @return the amount of Scanner text encompassing the dictionary
     */
    public int getCount() {
        return count;
    }

    /**
     * Constructor.
     *
     * @param scanner a simple text scanner used to parse the PDF dictionary using regular expressions
     */
    public PdfDictionaryReader(final Scanner scanner) {
        this.scanner = scanner;
        this.count = 0;
    }

    /**
     * Deserialize the PDF object from its serialized form into memory.
     *
     * @return a {@link PdfObject} containing the object data
     * @throws IonicException on failure to parse the PDF dictionary
     */
    public PdfObject read() throws IonicException {
        return readValue();
    }

    /**
     * Deserialize the PDF dictionary from its serialized form into memory.
     *
     * @param token the PDF token found at the beginning of the input {@link Scanner} (facilitates recursion)
     * @return a {@link PdfDictionary} containing the dictionary mappings
     * @throws IonicException on failure to parse the PDF dictionary
     */
    private PdfDictionary readInner(final String token) throws IonicException {
        final PdfDictionary dictionary = new PdfDictionary();
        SdkData.checkTrue((token != null), SdkError.ISFILECRYPTO_PARSEFAILED);
        count += scanner.match().end() - scanner.match().start();
        final String findClose = readInner(dictionary);
        SdkData.checkTrue(TOKEN_DICT_CLOSE.equals(findClose), SdkError.ISFILECRYPTO_PARSEFAILED);
        return dictionary;
    }

    /**
     * Iterate through the input scanner, deserializing name / value pairs into the target map.
     *
     * @param dictionary the map into which the name / value pairs should be stored
     * @return the PDF token indicating the end of the dictionary
     * @throws IonicException on failure to parse the PDF dictionary
     */
    private String readInner(final PdfDictionary dictionary) throws IonicException {
        String name = null;
        while (scanner.hasNext()) {
            name = readName();
            if (name.equals(TOKEN_DICT_CLOSE)) {
                break;
            } else {
                final PdfObject value = readValue();
                dictionary.put(name, value);
            }
        }
        return name;
    }

    /**
     * Extract the name portion of a PDF name / value pair.
     *
     * @return the name to be associated with the name / value pair
     * @throws IonicException on failure to parse the PDF dictionary
     */
    private String readName() throws IonicException {
        final int horizonDictionaryClose = 32;  // regex should consume any trailing spaces found
        final String findClose = scanner.findWithinHorizon(OBJ_DICT_CLOSE, horizonDictionaryClose);
        final String findName = (findClose == null)
                ? scanner.findWithinHorizon(OBJ_NAME, 0) : findClose;
        SdkData.checkTrue((findName != null), SdkError.ISFILECRYPTO_PARSEFAILED);
        count += scanner.match().end() - scanner.match().start();
        return scanner.match().group(1);
    }

    /**
     * Extract the value portion of a PDF name / value pair.
     *
     * @return the value to be associated with the name / value pair
     * @throws IonicException on failure to parse the PDF dictionary
     */
    private PdfObject readValue() throws IonicException {
        final int horizonDictionaryOpen = 32;  // regex should consume any trailing spaces found
        final int horizonDictionaryValue = 1024;
        if (scanner.findWithinHorizon(OBJ_NAME, horizonDictionaryValue) != null) {
            count += scanner.match().end() - scanner.match().start();
            return new PdfString(scanner.match().group(1));
        } else if (scanner.findWithinHorizon(OBJ_INDIRECT, horizonDictionaryValue) != null) {
            count += scanner.match().end() - scanner.match().start();
            return new PdfString(scanner.match().group(1));
        } else if (scanner.findWithinHorizon(OBJ_NUMERIC, horizonDictionaryValue) != null) {
            count += scanner.match().end() - scanner.match().start();
            return new PdfString(scanner.match().group(1));
        } else if (scanner.findWithinHorizon(OBJ_BOOLEAN, horizonDictionaryValue) != null) {
            count += scanner.match().end() - scanner.match().start();
            return new PdfString(scanner.match().group(1));
        } else if (scanner.findWithinHorizon(OBJ_LITERAL, horizonDictionaryValue) != null) {
            count += scanner.match().end() - scanner.match().start();
            return new PdfString(scanner.match().group(1));
        } else if (scanner.findWithinHorizon(OBJ_DICT_OPEN, horizonDictionaryOpen) != null) {
            return readDictionaryValue();
        } else if (scanner.findWithinHorizon(OBJ_LITERAL_HEX, horizonDictionaryValue) != null) {
            count += scanner.match().end() - scanner.match().start();
            return new PdfString(scanner.match().group(1));
        } else if (scanner.findWithinHorizon(OBJ_ARRAY_OPEN, horizonDictionaryValue) != null) {
            count += scanner.match().end() - scanner.match().start();
            return readArrayValue();
        } else {
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
    }

    /**
     * Parse an embedded PDF dictionary in the context of a PDF dictionary name / value pair.
     *
     * @return the value to be associated with the name / value pair
     * @throws IonicException on failure to parse the PDF value
     */
    private PdfDictionary readDictionaryValue() throws IonicException {
        final PdfDictionaryReader readerInner = new PdfDictionaryReader(scanner);
        final String dictionaryOpen = scanner.match().group(1);
        final PdfDictionary dictionaryInner = readerInner.readInner(dictionaryOpen);
        count += readerInner.getCount();
        return dictionaryInner;
    }

    /**
     * Parse an embedded PDF array in the context of a PDF dictionary name / value pair.
     *
     * @return the value to be associated with the name / value pair
     * @throws IonicException on failure to parse the PDF value
     */
    private PdfArray readArrayValue() throws IonicException {
        final PdfArray array = new PdfArray();
        readArrayValueInner(array);
        return array;
    }

    /**
     * Iterate through the content of the PDF array, adding items as encountered to the array.
     *
     * @param array the container which will hold the PdfValue objects
     * @return the parameter container
     * @throws IonicException on failure to parse the PDF value
     */
    private PdfObject readArrayValueInner(final PdfArray array) throws IonicException {
        while (scanner.hasNext()) {
            final int horizonArrayClose = 32;  // regex should consume any trailing spaces found
            final String findClose = scanner.findWithinHorizon(OBJ_ARRAY_CLOSE, horizonArrayClose);
            if (findClose == null) {
                final PdfObject findValue = readValue();
                SdkData.checkTrue((findValue != null), SdkError.ISFILECRYPTO_PARSEFAILED);
                array.add(findValue);
            } else {
                count += scanner.match().end() - scanner.match().start();
                break;
            }
        }
        return array;
    }

    /**
     * Token indicating the end of a PDF object dictionary value (PDF 32000-1:2008, section 7.3.7).
     */
    private static final String TOKEN_DICT_CLOSE = ">>";

    /**
     * Token indicating the end of a PDF object array value (PDF 32000-1:2008, section 7.3.6).
     */
    @SuppressWarnings("PMD.UnusedPrivateField")  // preserve for possible future use
    private static final String TOKEN_ARRAY_CLOSE = "]";

    /**
     * Regular expression used to detect a PDF object dictionary value (PDF 32000-1:2008, section 7.3.7).
     */
    private static final Pattern OBJ_DICT_OPEN = Pattern.compile("\\G(<<)(\\s{0,3}|(?=/)|(?=>>))");

    /**
     * Regular expression used to detect the end of a PDF object dictionary value (PDF 32000-1:2008, section 7.3.7).
     */
    private static final Pattern OBJ_DICT_CLOSE = Pattern.compile("\\G(>>)(\\s*|(?=/)|(?=<<))");

    /**
     * Regular expression used to detect a PDF object name value (PDF 32000-1:2008, section 7.3.5).
     */
    private static final Pattern OBJ_NAME = Pattern.compile("(?s)\\G(/[\\w\\x3a\\x2e\\x2b\\x2d\\x23\\x24\\x5c\\x7b]+)"
            + "(\\s+|(?=/)|(?=<<)|(?=>>)|(?=\\[)|(?=\\()|(?=])|(?=<))");

    /**
     * Regular expression used to detect a PDF object indirect value (PDF 32000-1:2008, section 7.3.10).
     */
    private static final Pattern OBJ_INDIRECT = Pattern.compile("(?s)\\G(\\d+\\s\\d+\\sR)(\\s*|(?=/)|(?=>>)|(?=]))");

    /**
     * Regular expression used to detect a PDF object literal string (PDF 32000-1:2008, section 7.3.4.2).
     */
    private static final Pattern OBJ_LITERAL = Pattern.compile(
            "(?s)\\G(\\([^()]*(?:\\([^()]*\\)[^()]*)*\\))(\\s+|(?=/)|(?=>>)|(?=\\()|(?=]))");

    /**
     * Regular expression used to detect a PDF object literal string (PDF 32000-1:2008, section 7.3.4.2).
     */
    private static final Pattern OBJ_LITERAL_HEX = Pattern.compile("(?s)\\G(<.+?>)(\\s+|(?=/)|(?=>>)|(?=<)|(?=]))");

    /**
     * Regular expression used to detect a PDF object array value (PDF 32000-1:2008, section 7.3.6).
     */
    private static final Pattern OBJ_ARRAY_OPEN = Pattern.compile("(?s)\\G(\\[)(\\s*)");

    /**
     * Regular expression used to detect a PDF object array value (PDF 32000-1:2008, section 7.3.6).
     */
    private static final Pattern OBJ_ARRAY_CLOSE = Pattern.compile("(?s)\\G(])(\\s*|(?=/)|(?=>>))");

    /**
     * Regular expression used to detect a PDF object numeric value (PDF 32000-1:2008, section 7.3.3).
     */
    private static final Pattern OBJ_NUMERIC = Pattern.compile(
            "\\G(-?\\d+(\\x2e\\d+)?)(\\s+|(?=/)|(?=>>)|(?=\\[)|(?=]))");

    /**
     * Regular expression used to detect a PDF object boolean value (PDF 32000-1:2008, section 7.3.2).
     */
    private static final Pattern OBJ_BOOLEAN = Pattern.compile("\\G(false|true)\\s*");
}
