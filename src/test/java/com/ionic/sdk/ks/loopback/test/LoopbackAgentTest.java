package com.ionic.sdk.ks.loopback.test;

import com.ionic.sdk.agent.cipher.chunk.ChunkCipherV2;
import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.data.DecryptAttributes;
import com.ionic.sdk.agent.cipher.data.EncryptAttributes;
import com.ionic.sdk.agent.cipher.file.GenericFileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.raw.RawCipherAesCtr;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.BytesTranscoder;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.codec.UTF8;
import com.ionic.sdk.core.date.DateTime;
import com.ionic.sdk.core.io.BytePattern;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.ks.loopback.LoopbackAgent;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Test APIs of {@link LoopbackAgent}.  The "loopback agent" implements {@link com.ionic.sdk.key.KeyServices}, with a
 * {@link com.ionic.sdk.keyvault.KeyVaultBase} as the backing persistent store of key data.  The class
 * {@link com.ionic.sdk.core.rng.CryptoRng} is used to generate keys.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoopbackAgentTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The persistent store for the keys associated with the loopback agent instance.
     */
    private static File fileVault;

    /**
     * The password used to protect the {@link com.ionic.sdk.keyvault.impl.KeyVaultPassword}.
     */
    private static String password;

    /**
     * A cache for key IDs created in the context of a run of this test class.  These are cached so they may later be
     * fetched.
     */
    private static Set<String> keyIds;

    /**
     * Generate a new file to contain keys for this invocation of the test suite.  The timestamp of the test run is
     * used to generate the filename, so each invocation of this "org.junit.runners.model.TestClass" will get a
     * fresh file.
     *
     * @throws IonicException on failure to configure the environment for the test (folder "testOutputs")
     */
    @BeforeClass
    public static void setUp() throws IonicException {
        final long timestamp = (System.currentTimeMillis() / DateTime.ONE_SECOND_MILLIS);
        final File folderTestOutputs = IonicTestEnvironment.getInstance().getFolderTestOutputsMkdir();
        final String filename = String.format("KV.loopback.%d.dat", timestamp);
        fileVault = new File(folderTestOutputs, filename);
        password = UUID.randomUUID().toString();
        Logger.getLogger(LoopbackAgentTest.class.getName()).info(
                String.format("setUp(), filename=[%s], password=[%s]", filename, password));
        keyIds = new TreeSet<String>();
    }

    /**
     * Instantiation of the loopback agent causes an invocation of {@link com.ionic.sdk.keyvault.KeyVaultBase#sync()},
     * which will create the specified file.
     *
     * @throws IonicException on failure to initialize the key vault
     */
    @Test
    public final void testLoopbackAgent_A_Initialize_ShouldSucceed() throws IonicException {
        Assert.assertNotNull(fileVault);
        final LoopbackAgent agent = new LoopbackAgent(fileVault, password);
        logger.info(agent.toString());
    }

    /**
     * Create a new {@link com.ionic.sdk.agent.key.AgentKey}.  This key is persisted in the key vault.
     *
     * @throws IonicException on failure to initialize the key vault, or failure to generate a key
     */
    @Test
    public final void testLoopbackAgent_B_CreateKey_ShouldSucceed() throws IonicException {
        Assert.assertNotNull(fileVault);
        final LoopbackAgent agent = new LoopbackAgent(fileVault, password);
        final CreateKeysResponse createKeysResponse = agent.createKey();
        final CreateKeysResponse.Key key = createKeysResponse.getFirstKey();
        keyIds.add(key.getId());
        logger.info(String.format("createKey(),ID=[%s],BYTES=[%s]",
                key.getId(), Transcoder.hex().encode(key.getKey())));
    }

    /**
     * Encrypt and decrypt data using {@link RawCipherAesCtr}.  The original input should be recoverable.
     *
     * @throws IonicException on failure to initialize the key vault, or failure to generate / fetch a key
     */
    @Test
    public final void testLoopbackAgent_C_AesCtrRaw_ShouldSucceed() throws IonicException {
        Assert.assertNotNull(fileVault);
        final LoopbackAgent agent = new LoopbackAgent(fileVault, password);
        final RawCipherAesCtr cipher = new RawCipherAesCtr(agent);
        // perform codec cycle on arbitrary data
        final byte[] plainTextIn = Transcoder.utf8().decode("Data Security as simple as a Yes or No");
        final EncryptAttributes encryptAttributes = new EncryptAttributes();
        final byte[] cipherText = cipher.encrypt(plainTextIn, encryptAttributes);
        logger.info(String.format("key=[%s], ciphertext=[%s]",
                encryptAttributes.getKeyId(), Transcoder.base64().encode(cipherText)));

        final DecryptAttributes decryptAttributes = new DecryptAttributes();
        final byte[] plainTextOut = cipher.decrypt(cipherText, encryptAttributes.getKeyId(), decryptAttributes);
        logger.info(String.format("key=[%s], plaintext=[%s]",
                encryptAttributes.getKeyId(), Transcoder.utf8().encode(plainTextOut)));

        Assert.assertEquals(plainTextIn.length, plainTextOut.length);
        Assert.assertArrayEquals(plainTextIn, plainTextOut);
    }

    /**
     * Encrypt and decrypt data using {@link ChunkCipherV2}.  The original input should be recoverable.
     *
     * @throws IonicException on failure to initialize the key vault, or failure to generate / fetch a key
     */
    @Test
    public final void testLoopbackAgent_D_ChunkCipher_ShouldSucceed() throws IonicException {
        Assert.assertNotNull(fileVault);
        final LoopbackAgent agent = new LoopbackAgent(fileVault, password);
        final ChunkCipherV2 chunkCipher = new ChunkCipherV2(agent);
        // perform codec cycle on arbitrary data
        final String plainTextIn = "Data Security as simple as a Yes or No";
        final ChunkCryptoEncryptAttributes encryptAttributes = new ChunkCryptoEncryptAttributes();
        final String cipherText = chunkCipher.encrypt(plainTextIn, encryptAttributes);
        logger.info(String.format("key=[%s], ciphertext=[%s]", encryptAttributes.getKeyId(), cipherText));

        final ChunkCryptoDecryptAttributes decryptAttributes = new ChunkCryptoDecryptAttributes();
        final String plainTextOut = chunkCipher.decrypt(cipherText, decryptAttributes);
        logger.info(String.format("key=[%s], plaintext=[%s]", encryptAttributes.getKeyId(), plainTextOut));

        Assert.assertEquals(plainTextIn.length(), plainTextOut.length());
        Assert.assertEquals(plainTextIn, plainTextOut);
    }

    /**
     * Encrypt and decrypt data using {@link GenericFileCipher}.  The original input should be recoverable.
     *
     * @throws IonicException on failure to initialize the key vault, or failure to generate / fetch a key
     * @throws IOException    on failure to create test plaintext resource
     */
    @Test
    public final void testLoopbackAgent_E_FileCipher_ShouldSucceed() throws IonicException, IOException {
        Assert.assertNotNull(fileVault);
        final LoopbackAgent agent = new LoopbackAgent(fileVault, password);
        final GenericFileCipher fileCipher = new GenericFileCipher(agent);
        // perform codec cycle on arbitrary data
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF8.NAME));
        while (os.size() < FileCipher.Generic.V11.BLOCK_SIZE_PLAIN) {
            writer.write("Data Security as simple as a Yes or No");
            writer.newLine();
        }
        writer.flush();
        final byte[] plainTextIn = os.toByteArray();

        final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
        final byte[] cipherText = fileCipher.encrypt(plainTextIn, encryptAttributes);
        logger.info(String.format("key=[%s], ciphertext=[%d]", encryptAttributes.getKeyId(), cipherText.length));

        final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
        final byte[] plainTextOut = fileCipher.decrypt(cipherText, decryptAttributes);
        logger.info(String.format("key=[%s], plaintext=[%d]", encryptAttributes.getKeyId(), plainTextOut.length));

        Assert.assertEquals(plainTextIn.length, plainTextOut.length);
        Assert.assertArrayEquals(plainTextIn, plainTextOut);
    }

    /**
     * After the cryptography keys are created in the preceding test cases, check to ensure that this key vault can
     * be decrypted.  The content is traced to the configured loggers for diagnostic purposes.
     *
     * @throws IonicException on failure to access the vault file on the filesystem, on cryptography failures
     */
    @Test
    public final void testLoopbackAgent_F_DecryptVault_ShouldSucceed() throws IonicException {
        // get the key vault file bytes
        final byte[] bytesKeyVault = DeviceUtils.read(fileVault);
        // find the key vault payload bytes
        final byte[] delimiter = Transcoder.utf8().decode("\r\n\r\n");
        final int find = BytePattern.findIn(bytesKeyVault, 0, delimiter);
        Assert.assertTrue(find > 0);
        final byte[] cipherBytes = Arrays.copyOfRange(bytesKeyVault, find + delimiter.length, bytesKeyVault.length);
        // decrypt the keyvault bytes
        final AesGcmCipher cipher = new AesGcmCipher();
        final BytesTranscoder utConverter = Transcoder.utf8();
        final byte[] salt = new byte[0];  // mimic current C++ behavior
        final int iterations = 2000;
        // derive a key from the password using PBKDF2
        final byte[] hashBytes = CryptoUtils.pbkdf2ToBytes(
                utConverter.decode(password), salt, iterations, AesCipher.KEY_BYTES);
        cipher.setKey(hashBytes);
        // set a hard-coded, known auth data
        cipher.setAuthData(utConverter.decode("Ionic Security Inc"));
        // unprotect the key vault data
        final byte[] plaintext = cipher.decrypt(cipherBytes);
        logger.info(Transcoder.utf8().encode(plaintext));
    }

    /**
     * Fetch a key created in a preceding test case of this test invocation.  Key should be accessible.
     *
     * @throws IonicException on failure to initialize the key vault, or failure to fetch specified key
     */
    @Test
    public final void testLoopbackAgent_G_GetKey_ShouldSucceed() throws IonicException {
        final LoopbackAgent agent = new LoopbackAgent(fileVault, password);
        final String keyId = keyIds.iterator().next();
        final GetKeysResponse getKeysResponse = agent.getKey(keyId);
        final GetKeysResponse.Key key = getKeysResponse.getFirstKey();
        logger.info(String.format("getKey(),ID=[%s],BYTES=[%s]",
                key.getId(), Transcoder.hex().encode(key.getKey())));
    }
}
