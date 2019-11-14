package com.ionic.sdk.ks.cipher.chunk.test;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherAbstract;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherAuto;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherV1;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherV2;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherV3;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Test ability to perform simple chunk crypto operations.
 */
public class ChunkCipherAutoTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Enrollment test cases to be run on Java 8 need Ionic cryptography initialized with BouncyCastle provider.
     *
     * @throws IonicException on cryptography initialization failure
     */
    @BeforeClass
    public static void setUp() throws IonicException {
        AgentSdk.initialize(new BouncyCastleProvider());
    }

    /**
     * Test ability to encrypt / decrypt some text.  Instances of {@link ChunkCipherAuto} use {@link ChunkCipherV2} by
     * default to encrypt.
     *
     * @throws IonicException <ul>
     *                        <li>ISAGENT_RESOURCE_NOT_FOUND on failure to initialize KeyServices</li>
     *                        <li>ISCHUNKCRYPTO_BAD_INPUT on invalid API call parameters</li>
     *                        <li>ISAGENT_KEY_DENIED on KeyServices key create failure</li>
     *                        </ul>
     */
    @Test
    public final void testChunkCodec_EncryptAutoDecryptV2_CodecSymmetry() throws IonicException {
        // setup
        final String plainTextIn = "Hello, Ionic!";
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        // encrypt
        final ChunkCipherAbstract chunkCipherEncrypt = new ChunkCipherAuto(keyServices);
        final String cipherTextOut = chunkCipherEncrypt.encrypt(plainTextIn);
        logger.info(cipherTextOut);
        // decrypt
        final ChunkCipherAbstract chunkCipherDecrypt = new ChunkCipherV2(keyServices);
        final String plainTextOut = chunkCipherDecrypt.decrypt(cipherTextOut);
        // verify
        Assert.assertEquals(plainTextIn, plainTextOut);
    }

    /**
     * Test ability to encrypt / decrypt some text.  Instances of {@link ChunkCipherAuto} can decrypt data encrypted
     * by any {@link ChunkCipherAbstract} format.
     *
     * @throws IonicException <ul>
     *                        <li>ISAGENT_RESOURCE_NOT_FOUND on failure to initialize KeyServices</li>
     *                        <li>ISCHUNKCRYPTO_BAD_INPUT on invalid API call parameters</li>
     *                        <li>ISAGENT_KEY_DENIED on KeyServices key create failure</li>
     *                        </ul>
     */
    @Test
    public final void testChunkCodec_EncryptV1DecryptAuto_CodecSymmetry() throws IonicException {
        // setup
        final String plainTextIn = "Hello, Ionic!";
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        // encrypt
        final ChunkCipherAbstract chunkCipherEncrypt = new ChunkCipherV1(keyServices);
        final String cipherTextOut = chunkCipherEncrypt.encrypt(plainTextIn);
        logger.info(cipherTextOut);
        // decrypt
        final ChunkCipherAbstract chunkCipherDecrypt = new ChunkCipherAuto(keyServices);
        final String plainTextOut = chunkCipherDecrypt.decrypt(cipherTextOut);
        // verify
        Assert.assertEquals(plainTextIn, plainTextOut);
    }

    /**
     * Test ability to encrypt / decrypt some text.  Instances of {@link ChunkCipherAuto} can decrypt data encrypted
     * by any {@link ChunkCipherAbstract} format.
     *
     * @throws IonicException <ul>
     *                        <li>ISAGENT_RESOURCE_NOT_FOUND on failure to initialize KeyServices</li>
     *                        <li>ISCHUNKCRYPTO_BAD_INPUT on invalid API call parameters</li>
     *                        <li>ISAGENT_KEY_DENIED on KeyServices key create failure</li>
     *                        </ul>
     */
    @Test
    public final void testChunkCodec_EncryptV2DecryptAuto_CodecSymmetry() throws IonicException {
        // setup
        final String plainTextIn = "Hello, Ionic!";
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        // encrypt
        final ChunkCipherAbstract chunkCipherEncrypt = new ChunkCipherV2(keyServices);
        final String cipherTextOut = chunkCipherEncrypt.encrypt(plainTextIn);
        logger.info(cipherTextOut);
        // decrypt
        final ChunkCipherAbstract chunkCipherDecrypt = new ChunkCipherAuto(keyServices);
        final String plainTextOut = chunkCipherDecrypt.decrypt(cipherTextOut);
        // verify
        Assert.assertEquals(plainTextIn, plainTextOut);
    }

    /**
     * Test ability to encrypt / decrypt some text.  Instances of {@link ChunkCipherAuto} can decrypt data encrypted
     * by any {@link ChunkCipherAbstract} format.
     *
     * @throws IonicException <ul>
     *                        <li>ISAGENT_RESOURCE_NOT_FOUND on failure to initialize KeyServices</li>
     *                        <li>ISCHUNKCRYPTO_BAD_INPUT on invalid API call parameters</li>
     *                        <li>ISAGENT_KEY_DENIED on KeyServices key create failure</li>
     *                        </ul>
     */
    @Test
    public final void testChunkCodec_EncryptV3DecryptAuto_CodecSymmetry() throws IonicException {
        // setup
        final String plainTextIn = "Hello, Ionic!";
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        // encrypt
        final ChunkCipherAbstract chunkCipherEncrypt = new ChunkCipherV3(keyServices);
        final String cipherTextOut = chunkCipherEncrypt.encrypt(plainTextIn);
        logger.info(cipherTextOut);
        // decrypt
        final ChunkCipherAbstract chunkCipherDecrypt = new ChunkCipherAuto(keyServices);
        final String plainTextOut = chunkCipherDecrypt.decrypt(cipherTextOut);
        // verify
        Assert.assertEquals(plainTextIn, plainTextOut);
    }
}
