package com.ionic.sdk.keyvault;

import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.key.KeyObligationsMap;
import com.ionic.sdk.cipher.CipherAbstract;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.json.JsonTarget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.TreeMap;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonArray;

/**
 * A class that implements encrypted file storage.
 *
 * This class is used by a couple of the KeyVault subclass implementations and works similar to FileCrypto
 * to save an encrypted file with any given cipher.
 */
public class KeyVaultEncryptedFile {

    // header constants
    /**
     * Delimiter between the JSON header and the encrypted payload.
     */
    private static final byte[] HEADER_DELIM = {'\r', '\n', '\r', '\n'};
    /**
     * Length of the delimiter.
     */
    private static final int HEADER_DELIM_SIZE = HEADER_DELIM.length;
    /**
     * Maximum number of bytes to search through to find the end of the JSON header.
     */
    private static final int HEADER_MAX_SIZE = 200;
    /**
     * Latest version as String.  (There is only one version currently.)
     */
    private static final String FILE_VERSION_LATEST = "1.0";

    // header fields
    /**
     * Header key label.
     */
    private static final String FIELD_KEYVAULT_ID = "keyVaultId";
    /**
     * Header key label.
     */
    private static final String FIELD_CIPHER_ID = "cipherId";
    /**
     * Header key label.
     */
    private static final String FIELD_FILE_VERSION = "fileVersion";

    // body fields
    /**
     * Cryptokey record key label.
     */
    private static final String FIELD_KEY_ID = "keyId";
    /**
     * Cryptokey record key label.
     */
    private static final String FIELD_KEY_DATA = "keyData";
    /**
     * Cryptokey record key label.
     */
    private static final String FIELD_KEY_ATTRIBUTES = "attrs";
    /**
     * Cryptokey record key label.
     */
    private static final String FIELD_KEY_MUTABLE_ATTRIBUTES = "mattrs";
    /**
     * Cryptokey record key label.
     */
    private static final String FIELD_KEY_OBLIGATIONS = "obligs";
    /**
     * Cryptokey record key label.
     */
    private static final String FIELD_KEY_EXPIRATION_TIME = "expireTimeUtc";
    /**
     * Cryptokey record key label.
     */
    private static final String FIELD_KEY_ISSUED_TIME = "issuedTimeUtc";

    // body content for an empty key vault
    /**
     * Alternate payload to indicate an empty vault without generating an error.
     */
    private static final byte[] BODY_CONTENT_EMPTY = {'E', 'M', 'P', 'T', 'Y'};

    /**
     * Delimiter between key record JSON blocks.
     */
    private static final String NEWLINE = "\n";

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Utility function for finding a delimiter in a byte stream.
     * @param search sequence of bytes to search for
     * @param buffer destination to search within
     * @param offset location in the buffer to check
     * @return true if all the bytes in search match the offset into buffer.
     */
    private static boolean areBytesInBuffer(final byte[] search, final byte[] buffer, final int offset) {

        for (int i = 0; i < search.length; i++) {
            if (search[i] != buffer[offset + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Internal function for reading the JSON header.
     * @param jsonString JSON header bytes as UTF8 String
     * @param keyVaultId the vault ID the header value should match
     * @param cipherId the cipher ID the header value should match
     * @param fileVersion the version the header version should match
     * @throws IonicException on any JSON parsing errors or non-matching values
     */
    private void readJsonHeader(final String jsonString,
                                final String keyVaultId,
                                final String cipherId,
                                final String fileVersion) throws IonicException {
        // parse the JSON into memory representation
        final JsonObject jsonHeader = JsonIO.readObject(jsonString, SdkError.ISFILECRYPTO_PARSEFAILED);

        // read key vault ID
        final String headerKeyVaultId = JsonSource.getString(jsonHeader, FIELD_KEYVAULT_ID);
        if (headerKeyVaultId == null) {
            logger.severe(String.format("Failed to read JSON header field '%s', rc = %d.",
                                        FIELD_KEYVAULT_ID, SdkError.ISFILECRYPTO_PARSEFAILED));
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
        if (!keyVaultId.equals(headerKeyVaultId)) {
            logger.severe(String.format("Cannot open encrypted key vault file because it is of the wrong type"
                                        + " (found key vault ID = '%s', expected '%s')",
                                        headerKeyVaultId, keyVaultId));
            throw new IonicException(SdkError.ISKEYVAULT_HEADER_MISMATCH);
        }

        // read cipher ID
        final String headerCipherId = JsonSource.getString(jsonHeader, FIELD_CIPHER_ID);
        if (headerCipherId == null) {
            logger.severe(String.format("Failed to read JSON header field '%s', rc = %d.",
                                        FIELD_CIPHER_ID, SdkError.ISFILECRYPTO_PARSEFAILED));
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
        if (!headerCipherId.equals(cipherId)) {
            logger.severe(String.format("Cannot open encrypted key vault file because it is of the wrong type"
                                        + " (found cipher ID = '%s', expected '%s')",
                                        headerCipherId,
                                        cipherId));
            throw new IonicException(SdkError.ISKEYVAULT_HEADER_MISMATCH);
        }

        // read file version
        final String headerFileVersion = JsonSource.getString(jsonHeader, FIELD_FILE_VERSION);
        if (headerFileVersion == null) {
            logger.severe(String.format("Failed to read JSON header field '%s', rc = %d.",
                                        FIELD_FILE_VERSION, SdkError.ISFILECRYPTO_PARSEFAILED));
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }

        if (!headerFileVersion.equals(fileVersion)) {
            logger.severe(String.format("Cannot open encrypted key vault file because the version is not supported"
                                        + " (found version = '%s', expected '%s')",
                                        headerFileVersion,
                                        fileVersion));
            throw new IonicException(SdkError.ISKEYVAULT_FILE_VERSION);
        }
    }

    /**
     * Error message for array parsing function call.
     */
    private static final String ARRAY_PARSING_ERROR_MESSAGE = "Found JSON value that isn't an array in attribute map.";

    /**
     * Internal function for parsing a JSON section into KeyAttributes.
     * @param keyObject Parsed JsonObject whose individual values are all arrays of strings
     * @return KeyAttributeMap
     * @throws IonicException on Json parsing errors
     */
    private KeyAttributesMap readJsonMapOfAttributes(final JsonObject keyObject) throws IonicException  {

        final KeyAttributesMap attributeMap = new KeyAttributesMap();
        final Map<String, JsonValue> jsonAsMap = (Map<String, JsonValue>) keyObject;
        for (Map.Entry<String, JsonValue> entry : jsonAsMap.entrySet()) {
            final String key = entry.getKey();
            final JsonValue value = entry.getValue();
            final JsonArray array = JsonSource.toJsonArray(value, ARRAY_PARSING_ERROR_MESSAGE);
            final List<String> list = new ArrayList<String>();
            for (JsonValue listVal : array) {
                final String listStr = JsonSource.toString(listVal);
                if (listStr == null) {
                    logger.severe("Found JSON array value that is not a string.");
                    throw new IonicException(SdkError.ISKEYVAULT_INVALIDVALUE);
                } else {
                    list.add(listStr);
                }
            }

            if (list.size() > 0) {
                attributeMap.put(key, list);
            }
        }

        return attributeMap;
    }

    /**
     * Internal function for parsing a JSON section into KeyObligations.
     * @param keyObject Parsed JsonObject whose individual values are all arrays of strings
     * @return KeyObligationsMap
     * @throws IonicException on Json parsing errors
     * @see com.ionic.sdk.agent.transaction.AgentTransactionUtil#toObligations(JsonObject)
     */
    private KeyObligationsMap readJsonMapOfObligations(final JsonObject keyObject) throws IonicException  {

        final KeyObligationsMap attributeMap = new KeyObligationsMap();
        final Map<String, JsonValue> jsonAsMap = (Map<String, JsonValue>) keyObject;
        for (Map.Entry<String, JsonValue> entry : jsonAsMap.entrySet()) {
            final String key = entry.getKey();
            final JsonValue value = entry.getValue();
            final JsonArray array = JsonSource.toJsonArray(value, ARRAY_PARSING_ERROR_MESSAGE);
            final List<String> list = new ArrayList<String>();
            for (JsonValue listVal : array) {
                final String listStr = JsonSource.toString(listVal);
                if (listStr == null) {
                    logger.severe("Found JSON array value that is not a string.");
                    throw new IonicException(SdkError.ISKEYVAULT_INVALIDVALUE);
                } else {
                    list.add(listStr);
                }
            }

            if (list.size() > 0) {
                attributeMap.put(key, list);
            }
        }

        return attributeMap;
    }

    /**
     * Internal function for reading the individual key records in the encrypted JSON.
     * @param keyObject JSON parsed object which contains all the key fields.
     * @return KeyVaultKeyRecord
     * @throws IonicException on parsing errors or missing values.
     */
    private KeyVaultKeyRecord readJsonKeyObject(final JsonObject keyObject) throws IonicException {

        final KeyVaultKeyRecord keyRecord = new KeyVaultKeyRecord();
        keyRecord.setState(KeyVaultKeyRecord.State.KR_STORED);

        // read key ID directly into key record field
        final String keyIdOut = JsonSource.getString(keyObject, FIELD_KEY_ID);
        if (keyIdOut == null) {
            logger.severe(String.format("Failed to read JSON key ID field '%s', rc = %d.",
                                        FIELD_KEY_ID, SdkError.ISFILECRYPTO_PARSEFAILED));
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
        keyRecord.setKeyId(keyIdOut);

        // read key data Base64
        final String keyDataBase64 = JsonSource.getString(keyObject, FIELD_KEY_DATA);
        if (keyDataBase64 == null) {
            logger.severe(String.format("Failed to read JSON key ID field '%s', rc = %d.",
                                        FIELD_KEY_DATA, SdkError.ISFILECRYPTO_PARSEFAILED));
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
        keyRecord.setKeyBytes(CryptoUtils.base64ToBin(keyDataBase64));

        // read key expiration time directly into key record field
        final long expire = JsonSource.getLong(keyObject, FIELD_KEY_EXPIRATION_TIME);
        if (expire == 0) {
            logger.severe(String.format("Failed to read JSON key ID field '%s', rc = %d.",
                                        FIELD_KEY_EXPIRATION_TIME, SdkError.ISFILECRYPTO_PARSEFAILED));
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
        keyRecord.setExpirationServerTimeUtcSeconds(expire);

        // read key issued time directly into key record field
        final long issue = JsonSource.getLong(keyObject, FIELD_KEY_ISSUED_TIME);
        if (issue == 0) {
            logger.severe(String.format("Failed to read JSON key ID field '%s', rc = %d.",
                                        FIELD_KEY_ISSUED_TIME, SdkError.ISFILECRYPTO_PARSEFAILED));
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
        keyRecord.setIssuedServerTimeUtcSeconds(issue);

        // read key attributes object
        final JsonObject attrObject = JsonSource.getJsonObject(keyObject, FIELD_KEY_ATTRIBUTES);
        if (attrObject == null) {
            logger.severe(String.format("Failed to read JSON key ID field '%s', rc = %d.",
                                        FIELD_KEY_ATTRIBUTES, SdkError.ISFILECRYPTO_PARSEFAILED));
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
        keyRecord.setKeyAttributes(readJsonMapOfAttributes(attrObject));

        // read mutable key attributes object (optional, since this field was added after
        // the original data format was created)
        final JsonObject mutAttrObject = JsonSource.getJsonObject(keyObject, FIELD_KEY_MUTABLE_ATTRIBUTES);
        if (mutAttrObject == null) {
            logger.severe(String.format("Failed to read JSON key ID field '%s', rc = %d.",
                                        FIELD_KEY_MUTABLE_ATTRIBUTES, SdkError.ISFILECRYPTO_PARSEFAILED));
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
        keyRecord.setMutableKeyAttributes(readJsonMapOfAttributes(mutAttrObject));

        // read key obligations object
        final JsonObject obligObject = JsonSource.getJsonObject(keyObject, FIELD_KEY_OBLIGATIONS);
        if (obligObject == null) {
            logger.severe(String.format("Failed to read JSON key ID field '%s', rc = %d.",
                                        FIELD_KEY_OBLIGATIONS, SdkError.ISFILECRYPTO_PARSEFAILED));
            throw new IonicException(SdkError.ISFILECRYPTO_PARSEFAILED);
        }
        keyRecord.setKeyObligations(readJsonMapOfObligations(obligObject));

        return keyRecord;
    }

    /**
     * Internal function for reading the unencrypted payload, which should contain a list of JSON blocks.
     * @param jsonBody A set of JSON blocks separated by single newlines. (Therefore, JSON can not include newlines.)
     * @return a map of key vault records
     * @throws IonicException on parsing errors or missing values.
     */
    private Map<String, KeyVaultKeyRecord> readJsonBody(final String jsonBody) throws IonicException {

        final Map<String, KeyVaultKeyRecord> mapKeyRecords = new TreeMap<String, KeyVaultKeyRecord>();

        final String[] keyRowsJson = jsonBody.split(NEWLINE, 0);
        for (String keyRowJson : keyRowsJson) {
            // parse the JSON into memory representation
            final JsonObject jsonRow = JsonIO.readObject(keyRowJson, SdkError.ISFILECRYPTO_PARSEFAILED);
            if (jsonRow == null) {
                logger.warning("Skipped key entry because the JSON string is invalid.");
                continue;
            }

            // read the key object
            final KeyVaultKeyRecord record = readJsonKeyObject(jsonRow);
            mapKeyRecords.put(record.getKeyId(), record);
        }

        return mapKeyRecords;
    }

    /**
     * Internal function for translating a map of string lists into JSON.
     * @param mapOfVectors KeyAttributeMap or KeyObligationMap
     * @return JsonObject
     */
    private JsonObject writeJsonMapOfVectors(final Map<String, List<String>> mapOfVectors) {

        final JsonObjectBuilder mapBuilder = Json.createObjectBuilder();

        for (Map.Entry<String, List<String>> entry : mapOfVectors.entrySet()) {

            final JsonArray jsonList = JsonTarget.toJsonArray(entry.getValue());
            JsonTarget.addNotNull(mapBuilder, entry.getKey(), jsonList);
        }

        return mapBuilder.build();
    }

    /**
     * Creates an instance that implements encrypted file storage.
     *
     * This class is used by a couple of the KeyVault subclass implementations and works similar
     * to FileCrypto to save an encrypted file with any given cipher.
     * @param keyVaultId a vault ID used to verify a saved vault file matches the version being used
     *  to load and decrypt it
    */
    public KeyVaultEncryptedFile(final String keyVaultId) {
        this.keyVaultId = keyVaultId;
    }

    /**
     * Load a map of key records from a KeyVault subclass from an encrypted file using a generic cipher.
     * passed into the function
     * @param filePath File path and name
     * @param cipher cipher the function will use to decrypt the data
     * @return The loaded key map
     * @throws IonicException If the file is missing, fails to open, or fails to fully read.
    */
    public Map<String, KeyVaultKeyRecord> loadAllKeyRecordsFromFile(final String filePath,
                                                             final CipherAbstract cipher) throws IonicException {

        byte[] fileDataBytes = null;

        // make this entire function thread-safe
        synchronized (this) {
            final File file = new File(filePath);
            if (file.exists() && file.length() > 0) {
                try {
                    fileDataBytes = Stream.read(file);
                    SdkData.checkTrue(file.length() == fileDataBytes.length, SdkError.ISKEYVAULT_EOF);
                } catch (IOException e) {
                    logger.warning(String.format("loadAllKeyRecordsFromFile threw an IO Exception %s",
                                                 e.toString()));
                    throw new IonicException(SdkError.ISKEYVAULT_OPENFILE, e);
                }
            }
        }

        // now parse the bytes we read into memory.  note that we are no longer in the IPC critical
        // section that locks the input file.
        return loadAllKeyRecordsFromMemoryInternal(fileDataBytes, cipher);
    }

    /**
     * Load a map of key records from a KeyVault subclass from a memory buffer
     * using a generic cipher passed into the function.
     * @param dataBytes data memory
     * @param cipher cipher the function will use to encrypt the data
     * @return the key map
     * @throws IonicException If the file is missing, fails to open, or fails to fully read.
     */
    public Map<String, KeyVaultKeyRecord> loadAllKeyRecordsFromMemory(final byte[] dataBytes,
                                                                      final CipherAbstract cipher)
                                                                        throws IonicException {
        return loadAllKeyRecordsFromMemoryInternal(dataBytes, cipher);
    }

    /**
     * Load a map of key records from a KeyVault subclass from a memory buffer
     * using a generic cipher passed into the function.
     * @param dataBytes data memory
     * @param cipher cipher the function will use to encrypt the data
     * @return the key map
     * @throws IonicException If the file is missing, fails to open, or fails to fully read.
     */
    private Map<String, KeyVaultKeyRecord> loadAllKeyRecordsFromMemoryInternal(final byte[] dataBytes,
                                                                               final CipherAbstract cipher)
                                                                                throws IonicException {
        // validate state
        if (keyVaultId.length() == 0) {
            logger.severe("Key vault ID cannot be empty.");
            throw new IonicException(SdkError.ISKEYVAULT_MISSINGVALUE);
        }

        // search first 200 bytes for the header/body delimiter
        int headerDelimIndex = 0;
        for (int i = 0; i < (dataBytes.length - HEADER_DELIM_SIZE) && i < HEADER_MAX_SIZE; ++i) {
            if (areBytesInBuffer(HEADER_DELIM, dataBytes, i)) {
                // we found the delimiter, break out of the loop
                headerDelimIndex = i;
                break;
            }
        }

        // ensure the header delimiter was found and that there is data
        // after it to parse
        if (headerDelimIndex <= 0) {
            logger.severe("Failed to load key vault data because no header was found.");
            throw new IonicException(SdkError.ISKEYVAULT_NOHEADER);
        }

        final byte[] header = new byte[headerDelimIndex];
        System.arraycopy(dataBytes, 0, header, 0, headerDelimIndex);

        // parse and read the JSON header
        readJsonHeader(Transcoder.utf8().encode(header), keyVaultId,
                        cipher.getId(), FILE_VERSION_LATEST);

        // decrypt the JSON body ciphertext
        final byte[] encryptedBody = Arrays.copyOfRange(dataBytes,
                                                        headerDelimIndex + HEADER_DELIM_SIZE,
                                                        dataBytes.length);
        final byte[] jsonBody = cipher.decrypt(encryptedBody);
        if (jsonBody == null) {
            logger.severe(String.format("Failed to decrypt JSON body data, rc = %d.", SdkError.ISKEYVAULT_UNKNOWN));
            throw new IonicException(SdkError.ISKEYVAULT_UNKNOWN);
        }

        // parse and read the JSON body if there is one
        if (jsonBody.length != 0 && !Arrays.equals(jsonBody, BODY_CONTENT_EMPTY)) {
            return readJsonBody(Transcoder.utf8().encode(jsonBody));
        }

        // Return a valid empty list in this case.
        return new TreeMap<String, KeyVaultKeyRecord>();
    }

    /**
     * Save a map of key records from a KeyVault subclass into an encrypted file
     * using a generic cipher passed into the function.
     * @param cipher cipher the function will use to encrypt the data
     * @param mapKeyRecords the key map to save from
     * @param filePath File path and name
     * @throws IonicException on any file errors.
     */
    public void saveAllKeyRecordsToFile(final CipherAbstract cipher,
                                final Map<String, KeyVaultKeyRecord> mapKeyRecords,
                                final String filePath) throws IonicException {

        final byte[] fileDataBytes = saveAllKeyRecordsToMemoryInternal(cipher, mapKeyRecords);

        // make this entire function thread-safe
        synchronized (this) {

            final File file = new File(filePath);
            final File folder = new File(filePath).getParentFile();

            // Verify the parent directories exist:
            if ((folder != null) && !folder.exists() && !folder.mkdirs()) {
                logger.warning(String.format("saveAllKeyRecordsToFile failed to create folder: %s",
                                             folder.getAbsolutePath()));
                throw new IonicException(SdkError.ISAGENT_OPENFILE);
            }

            // open file for writing
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileDataBytes);
                fos.close();
            } catch (FileNotFoundException e) {
                logger.warning(String.format("saveAllKeyRecordsToFile threw an Exception %s",
                                             e.toString()));
                throw new IonicException(SdkError.ISKEYVAULT_OPENFILE, e);
            } catch (IOException e) {
                logger.warning(String.format("saveAllKeyRecordsToFile threw an IO Exception %s",
                                             e.toString()));
                throw new IonicException(SdkError.ISKEYVAULT_OPENFILE, e);
            }
        }
    }

    /**
     * Load a map of key records from a KeyVault subclass from a memory buffer
     * using a generic cipher passed into the function.
     * @param cipher cipher the function will use to encrypt the data
     * @param mapKeyRecords the key map to save from
     * @return the keys saved in JSON format and encrypted as a byte[]
     * @throws IonicException on any json errors.
     */
    public byte[] saveAllKeyRecordsToMemory(final CipherAbstract cipher,
                                            final Map<String, KeyVaultKeyRecord> mapKeyRecords)
                                            throws IonicException {
        return saveAllKeyRecordsToMemoryInternal(cipher, mapKeyRecords);
    }

    /**
     * Load a map of key records from a KeyVault subclass from a memory buffer
     * using a generic cipher passed into the function.
     * @param cipher cipher the function will use to encrypt the data
     * @param mapKeyRecords the key map to save from
     * @return the keys saved in JSON format and encrypted as a byte[]
     * @throws IonicException on any json errors.
     */
    private byte[] saveAllKeyRecordsToMemoryInternal(final CipherAbstract cipher,
                                            final Map<String, KeyVaultKeyRecord> mapKeyRecords)
                                            throws IonicException {

        // validate state
        if (keyVaultId.length() == 0) {
            logger.severe("Key vault ID cannot be empty.");
            throw new IonicException(SdkError.ISKEYVAULT_MISSINGVALUE);
        }

        // build JSON header object
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        JsonTarget.addNotNull(objectBuilder, FIELD_KEYVAULT_ID, keyVaultId);
        JsonTarget.addNotNull(objectBuilder, FIELD_CIPHER_ID, cipher.getId());
        JsonTarget.addNotNull(objectBuilder, FIELD_FILE_VERSION, FILE_VERSION_LATEST);

        final String headerString = JsonIO.write(objectBuilder.build(), false);

        // build JSON keys array for the JSON body
        final StringBuilder stringBuf = new StringBuilder();
        for (Map.Entry<String, KeyVaultKeyRecord> entry : mapKeyRecords.entrySet()) {

            final KeyVaultKeyRecord record = entry.getValue();

            // skip records marked for removal
            if (!record.isAlive()) {
                continue;
            }

            final JsonObjectBuilder recordBuilder = Json.createObjectBuilder();
            // write basic key information
            JsonTarget.addNotNull(recordBuilder, FIELD_KEY_ID, record.getKeyId());
            JsonTarget.add(recordBuilder,
                            FIELD_KEY_ISSUED_TIME,
                            record.getIssuedServerTimeUtcSeconds());
            JsonTarget.add(recordBuilder,
                            FIELD_KEY_EXPIRATION_TIME,
                            record.getExpirationServerTimeUtcSeconds());
            // write key data bytes
            JsonTarget.addNotNull(recordBuilder,
                                    FIELD_KEY_DATA,
                                    CryptoUtils.binToBase64(record.getKeyBytes()));

            // write key attributes
            JsonTarget.add(recordBuilder,
                            FIELD_KEY_ATTRIBUTES,
                            writeJsonMapOfVectors(record.getKeyAttributes()));

            // write mutable key attributes
            JsonTarget.add(recordBuilder,
                            FIELD_KEY_MUTABLE_ATTRIBUTES,
                            writeJsonMapOfVectors(record.getMutableKeyAttributes()));

            // write key obligations
            JsonTarget.add(recordBuilder,
                            FIELD_KEY_OBLIGATIONS,
                            writeJsonMapOfVectors(record.getKeyObligations()));

            // add key to the keys json string
            stringBuf.append(JsonIO.write(recordBuilder.build(), false));
            stringBuf.append(NEWLINE);
        }
        String bodyPlainText = stringBuf.toString();

        // ensure the plaintext is never empty, since some ciphers do not support
        // encrypting zero-length plaintext
        if (bodyPlainText.length() == 0) {
            bodyPlainText = Transcoder.utf8().encode(BODY_CONTENT_EMPTY);
        }

        // encrypt the JSON body
        final byte[] bodyCipherText = cipher.encrypt(bodyPlainText);
        final byte[] headerBytes = Transcoder.utf8().decode(headerString);

        // write header, delimiter, and body to the output data bytes
        final byte[] savedBytes = new byte[headerBytes.length + HEADER_DELIM_SIZE + bodyCipherText.length];
        System.arraycopy(headerBytes, 0, savedBytes, 0, headerBytes.length);
        System.arraycopy(HEADER_DELIM, 0, savedBytes, headerBytes.length, HEADER_DELIM_SIZE);
        System.arraycopy(bodyCipherText, 0, savedBytes, headerBytes.length + HEADER_DELIM_SIZE, bodyCipherText.length);

        return savedBytes;
    }

    /**
     * Specific ID of this vault.
     */
    private String keyVaultId;
}
