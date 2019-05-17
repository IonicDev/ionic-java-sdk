package com.ionic.sdk.keyvault.impl;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.keyvault.KeyVaultBase;
import com.ionic.sdk.keyvault.KeyVaultEncryptedFile;
import com.ionic.sdk.keyvault.KeyVaultFileModTracker;
import com.ionic.sdk.keyvault.KeyVaultKeyRecord;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Password protected encrypted Key Vault, using Password-Based Key Derivation Function.
 */
public final class KeyVaultPassword extends KeyVaultBase {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Private const for {@link KeyVaultPassword} ID.
     */
    private static final String VAULT_ID = "keyvault-password";

    /**
     * Private const for {@link KeyVaultPassword} Label.
     */
    private static final String VAULT_LABEL = "Ionic Password Key Vault";

    /**
     * The Ionic auth string.
     */
    private static final String IONIC_AUTH_DATA = "Ionic Security Inc";

    /**
     * The cipher used to encrypt and decrypt the key vault file.
     */
    private final AesGcmCipher cipher;

    /**
     * The user-specified filesystem path to which the KeyVault file should be saved.
     */
    private String filePath;

    /**
     * File modification tracker lets us know if the file has been updated since the last time we accessed it.
     */
    private KeyVaultFileModTracker fileModTracker = null;

    /**
     * Constructor with specific filename.
     *
     * @param filePath File path to store the encrypted key vault.
     * @throws IonicException on failure of the underlying JRE cipher to initialize
     */
    public KeyVaultPassword(final String filePath) throws IonicException {
        super();
        this.cipher = new AesGcmCipher();
        this.filePath = filePath;
    }

    /**
     * Provide password used to protect the serialized byte stream containing DeviceProfile objects.
     *
     * @param password the client-supplied string used to protect the DeviceProfile objects on serialization
     * @throws IonicException on cryptography initialization failures; bad input; cryptography operation failures
     */
    public void setPassword(final String password) throws IonicException {
        // derive a key from the password using PBKDF2 (mimic current C++ behavior)
        final int iterations = 2000;
        cipher.setKey(CryptoUtils.pbkdf2ToBytes(
                Transcoder.utf8().decode(password), new byte[0], iterations, AesCipher.KEY_BYTES));
        // set a hard-coded, known auth data
        cipher.setAuthData(Transcoder.utf8().decode(IONIC_AUTH_DATA));
    }

    /**
     * ID accessor.
     * @return Vault ID constant
     */
    @Override
    public String getId() {
        return VAULT_ID;
    }

    /**
     * Label accessor.
     * @return Vault label constant
     */
    @Override
    public String getLabel() {
        return VAULT_LABEL;
    }

    /**
     * Return the security level of this class.
     * @return 100 (constant)
     */
    @Override
    public int getSecurityLevel() {
        return VAULT_SECURITY_LEVEL;
    }

    /**
     * Get the file path used for key vault data storage.
     *
     * @return Returns the file path used for key vault data storage.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Set the file path used for key vault data storage.
     *
     * @param filePath The file path to use for key vault data storage.
     */
    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void cleanVaultStore() {
        logger.fine("cleanVaultStore()");

        mapKeyRecords.clear();
        try {
            SdkData.checkTrue(filePath != null, SdkError.ISFILECRYPTO_MISSINGVALUE);
            final File outputFile = new File(filePath);
            if (!outputFile.delete()) {
                logger.info("Failed to delete the vault store file - may not exist yet.");
            }
        } catch (Exception e) {
            logger.severe(String.format("Exception attempting to delete the vault store file: %s.", e.toString()));
        }
    }

    @Override
    protected Map<String, KeyVaultKeyRecord> loadAllKeyRecords() throws IonicException {
        logger.fine("loadAllKeyRecords()");

        // check to see if the input file exists
        SdkData.checkTrue(filePath != null, SdkError.ISFILECRYPTO_MISSINGVALUE);
        final File inputCipherFile = new File(filePath);

        if (!inputCipherFile.exists()) {
            throw new IonicException(SdkError.ISKEYVAULT_RESOURCE_NOT_FOUND,
                    String.format("No key vault storage file exists at '%s'.",
                            inputCipherFile.getAbsolutePath()));
        }

        // record file information. if the file has not changed since our last
        // load or save operation, then we can skip this load operation
        if (fileModificationPoint(filePath) == KeyVaultFileModTracker.Result.FILE_UNCHANGED) {
            throw new IonicException(SdkError.ISKEYVAULT_LOAD_NOT_NEEDED,
                    "File has not changed since last load.");
        }

        // read encrypted file.
        final KeyVaultEncryptedFile file = new KeyVaultEncryptedFile(VAULT_ID);
        return file.loadAllKeyRecordsFromFile(inputCipherFile.getAbsolutePath(), cipher);
    }

    @Override
    protected int saveAllKeyRecords(final Map<String, KeyVaultKeyRecord> mapKeyRecords) throws IonicException {
        logger.fine("saveAllKeyRecords()");

        // write encrypted file
        SdkData.checkTrue(filePath != null, SdkError.ISFILECRYPTO_MISSINGVALUE);
        final KeyVaultEncryptedFile file = new KeyVaultEncryptedFile(VAULT_ID);
        file.saveAllKeyRecordsToFile(cipher, mapKeyRecords, filePath);

        // record file information
        fileModificationPoint(filePath);

        return SdkError.ISKEYVAULT_OK;
    }

    /**
     * Function that uses the KeyVaultFileModTracker to determine whether the key vault has changed
     * outside the context of this instance of the vault.
     *
     * @param filePath the filesystem path associated with the KeyVault file
     * @return A {@link KeyVaultFileModTracker.Result}
     */
    private KeyVaultFileModTracker.Result fileModificationPoint(final String filePath) {
        // if we don't have a file tracker object, or we have changed the file we are tracking, then
        // create a new one here
        if ((fileModTracker == null) || !fileModTracker.getFilePath().equals(filePath)) {

            fileModTracker = new KeyVaultFileModTracker(filePath);
        }

        return fileModTracker.recordFileInfo();
    }
}
