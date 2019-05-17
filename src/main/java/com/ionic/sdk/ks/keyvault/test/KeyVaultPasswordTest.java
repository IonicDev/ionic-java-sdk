package com.ionic.sdk.ks.keyvault.test;

import com.ionic.sdk.core.date.DateTime;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.keyvault.impl.KeyVaultPassword;
import com.ionic.sdk.keyvault.utils.KeyVaultTestUtils;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Test;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Basic coverage tests of {@link com.ionic.sdk.keyvault.impl.KeyVaultPassword}.
 */
public class KeyVaultPasswordTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Create, read, update, delete operations on a key vault.
     *
     * @throws IonicException on test environment issues
     */
    @Test
    public final void testKeyVault_Crud_Success() throws IonicException {
        final long creationTimestamp = (System.currentTimeMillis() / DateTime.ONE_SECOND_MILLIS);
        final String uuid = UUID.randomUUID().toString();
        final File folder = IonicTestEnvironment.getInstance().getFolderTestOutputsMkdir();
        final String filename = String.format("%s.%d.vault", getClass().getSimpleName(), creationTimestamp);
        final File file = new File(folder, filename);
        logger.info(file.getPath());
        final KeyVaultPassword vault = new KeyVaultPassword(file.getPath());
        final KeyVaultPassword vaultCopy = new KeyVaultPassword(file.getPath());
        vault.setPassword(uuid);
        vaultCopy.setPassword(uuid);
        vault.cleanVaultStore();

        // perform the CRUD test with NUM_KEYS_TO_TEST keys
        final int numKeysToTest = 10;
        KeyVaultTestUtils.testKeyVaultCrud(vault, vaultCopy, numKeysToTest);
        vault.cleanVaultStore();

        logger.info("testKeyVaultPasswordCrud testKeyVaultWithCorruptFile");
        // Test with a corrupted Key Vault
        KeyVaultTestUtils.testKeyVaultWithCorruptFile(vault, file);
        vault.cleanVaultStore();
    }
}
