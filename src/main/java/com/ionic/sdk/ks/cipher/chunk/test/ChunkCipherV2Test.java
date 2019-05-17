package com.ionic.sdk.ks.cipher.chunk.test;

import com.ionic.sdk.agent.cipher.chunk.ChunkCipherAbstract;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherV2;
import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoChunkInfo;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Test ability to perform simple chunk crypto operations.
 */
public class ChunkCipherV2Test {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Test {@link ChunkCipherV2} helper APIs.
     *
     * @throws IonicException on failure to initialize Ionic library
     */
    @Test
    public final void testChunkV1_HelperAPIs_Success() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final ChunkCipherAbstract chunkCipher = new ChunkCipherV2(keyServices);
        Assert.assertEquals(ChunkCipherV2.ID, chunkCipher.getId());
        Assert.assertEquals(ChunkCipherV2.ID, chunkCipher.getLabel());
    }

    /**
     * Test ability to encrypt some text.
     *
     * @throws IonicException <ul>
     *                        <li>ISAGENT_RESOURCE_NOT_FOUND on failure to initialize KeyServices</li>
     *                        <li>ISCHUNKCRYPTO_BAD_INPUT on invalid API call parameters</li>
     *                        <li>ISAGENT_KEY_DENIED on KeyServices key create failure</li>
     *                        </ul>
     */
    @Test
    public final void testChunkV2_EncryptDecryptText_Success() throws IonicException {
        final String plainText = "Hello, Ionic!";
        // setup Ionic test agent
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        // Encrypt a string using the chunk data format.
        final ChunkCipherAbstract chunkCipher = new ChunkCipherV2(keyServices);
        final String encryptedText = chunkCipher.encrypt(plainText);
        logger.info("Plain Text: " + plainText);
        logger.info("Ionic Chunk Encrypted Text: " + encryptedText);

        final ChunkCryptoChunkInfo chunkInfo = chunkCipher.getChunkInfo(encryptedText);
        Assert.assertEquals(ChunkCipherV2.ID, chunkInfo.getCipherId());
        Assert.assertTrue(chunkInfo.getKeyId().startsWith(keyServices.getActiveProfile().getKeySpace()));
        Assert.assertTrue(chunkInfo.isEncrypted());

        final byte[] plainTextOut = chunkCipher.decryptToBytes(encryptedText);
        Assert.assertEquals(plainText, Transcoder.utf8().encode(plainTextOut));
    }

    /**
     * Test ability to encrypt / decrypt some bytes.
     *
     * @throws IonicException <ul>
     *                        <li>ISAGENT_RESOURCE_NOT_FOUND on failure to initialize KeyServices</li>
     *                        <li>ISCHUNKCRYPTO_BAD_INPUT on invalid API call parameters</li>
     *                        <li>ISAGENT_KEY_DENIED on KeyServices key create failure</li>
     *                        </ul>
     */
    @Test
    public final void testChunkV2_EncryptBytes_Success() throws IonicException {
        final String plainTextIn = "Hello, Ionic!";
        // setup Ionic test agent
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        // Encrypt a string using the chunk data format.
        final ChunkCipherAbstract chunkCipher = new ChunkCipherV2(keyServices);
        final String cipherTextOut = chunkCipher.encrypt(Transcoder.utf8().decode(plainTextIn));
        logger.info("Plain Text: " + plainTextIn);
        logger.info("Ionic Chunk Encrypted Text: " + cipherTextOut);
        final String plainTextOut = chunkCipher.decrypt(Transcoder.utf8().decode(cipherTextOut));
        Assert.assertEquals(plainTextIn, plainTextOut);
    }

    /**
     * Test ability to encrypt / decrypt some text.
     *
     * @throws IonicException <ul>
     *                        <li>ISAGENT_RESOURCE_NOT_FOUND on failure to initialize KeyServices</li>
     *                        <li>ISCHUNKCRYPTO_BAD_INPUT on invalid API call parameters</li>
     *                        <li>ISAGENT_KEY_DENIED on KeyServices key create failure</li>
     *                        </ul>
     */
    @Test
    public final void testChunkV2_EncryptDecryptText_CodecSymmetry() throws IonicException {
        // setup Ionic test agent
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        // check the test data
        for (final String plainTextIt : TEST_TEXTS) {
            // decrypt using the chunk data format
            final ChunkCipherAbstract chunkCipher = new ChunkCipherV2(keyServices);
            final String cipherText = chunkCipher.encrypt(plainTextIt);
            logger.info("Ionic Chunk Encrypted Text: " + cipherText);
            final String plainText = chunkCipher.decrypt(cipherText);
            logger.info("Plain Text: " + plainText);
            Assert.assertEquals(plainTextIt, plainText);
        }
    }

    /**
     * Test input for chunk encrypt function.
     */
    private static final String[] TEST_TEXTS = {
            "Hello, Ionic!",
            // ES
            "H\u00c9ll\u00d3, I\u00d3n\u00edc!",
            "HÉllÓ, IÓníc!",
            // FR
            "H\u00eall\u00f4, I\u00f4ni\u00e7!",
            "Hêllô, Iôniç!",
            // DE
            "Hell\u00f6, I\u00f6nic\u00df!",
            "Hellö, Iönicß!",
            // RU
            "H\u0437ll\u043e, I\u043en\u0457c!",
            "Hзllо, Iоnїc!",
            // JA
            "H\u30e8ll\u56de, I\u56den\u5de5c!",
            "Hヨll回, I回n工c!",
    };
}
