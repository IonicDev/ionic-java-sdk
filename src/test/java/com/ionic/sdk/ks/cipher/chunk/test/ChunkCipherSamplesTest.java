package com.ionic.sdk.ks.cipher.chunk.test;

import com.ionic.sdk.agent.cipher.chunk.ChunkCipherAbstract;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherAuto;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherV1;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherV2;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherV3;
import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoChunkInfo;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.logging.Logger;

/**
 * Concise JUnit tests incorporated into JavaDoc.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChunkCipherSamplesTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Verify simple cryptography symmetry using {@link ChunkCipherV1}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testChunkCipherV1_EncryptDecryptString() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final String plainText = "Hello, Machina!";
        final ChunkCipherAbstract chunkCipher = new ChunkCipherV1(keyServices);
        final String cipherText = chunkCipher.encrypt(plainText);
        final String plainTextRecover = chunkCipher.decrypt(cipherText);
        Assert.assertEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", cipherText));
    }

    /**
     * Verify simple cryptography symmetry using {@link ChunkCipherV2}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testChunkCipherV2_EncryptDecryptString() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final String plainText = "Hello, Machina!";
        final ChunkCipherAbstract chunkCipher = new ChunkCipherV2(keyServices);
        final String cipherText = chunkCipher.encrypt(plainText);
        final String plainTextRecover = chunkCipher.decrypt(cipherText);
        Assert.assertEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", cipherText));
    }

    /**
     * Verify simple cryptography symmetry using {@link ChunkCipherV3}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testChunkCipherV3_EncryptDecryptString() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final String plainText = "Hello, Machina!";
        final ChunkCipherAbstract chunkCipher = new ChunkCipherV3(keyServices);
        final String cipherText = chunkCipher.encrypt(plainText);
        final String plainTextRecover = chunkCipher.decrypt(cipherText);
        Assert.assertEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", cipherText));
    }

    /**
     * Verify simple cryptography symmetry using {@link ChunkCipherAuto}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testChunkCipherAuto_EncryptDecryptString() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final String plainText = "Hello, Machina!";
        final ChunkCipherAbstract chunkCipher = new ChunkCipherAuto(keyServices);
        final String cipherText = chunkCipher.encrypt(plainText);
        final ChunkCryptoChunkInfo chunkInfo = chunkCipher.getChunkInfo(cipherText);
        Assert.assertEquals(ChunkCipherV2.ID, chunkInfo.getCipherId());
        final String plainTextRecover = chunkCipher.decrypt(cipherText);
        Assert.assertEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", cipherText));
    }
}
