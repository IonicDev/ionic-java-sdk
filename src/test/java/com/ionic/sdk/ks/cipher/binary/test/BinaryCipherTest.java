package com.ionic.sdk.ks.cipher.binary.test;

import com.ionic.sdk.agent.cipher.binary.BinaryCipherAbstract;
import com.ionic.sdk.agent.cipher.binary.BinaryCipherAesCtr;
import com.ionic.sdk.agent.cipher.binary.BinaryCipherAesGcm;
import com.ionic.sdk.agent.cipher.data.DecryptAttributes;
import com.ionic.sdk.agent.cipher.data.EncryptAttributes;
import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.crypto.jce.CryptoAbstract;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;
import com.ionic.sdk.key.cache.KeyServicesSingleKey;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Test cases demonstrating functionality of {@link BinaryCipherAbstract} implementations.  These provide the
 * ability to
 */
public class BinaryCipherTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Demonstrate simple case of encrypt/decrypt of short, arbitrary binary data chunk.
     *
     * @throws IonicException on cryptography initialization failure, operation failure
     */
    @Test
    public final void testBinaryCipher_CtrEncryptDecrypt_Symmetry() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final BinaryCipherAbstract cipher = new BinaryCipherAesCtr(keyServices);
        final byte[] plainText = new byte[(int) Math.round(Math.pow(2, Byte.SIZE))];
        for (int i = 0; (i < plainText.length); ++i) {
            plainText[i] = (byte) i;
        }
        final byte[] cipherText = cipher.encrypt(plainText, new EncryptAttributes());
        final byte[] plainTextRecover = cipher.decrypt(cipherText, new DecryptAttributes());
        logger.info(String.format("LENGTH: PLAINTEXT=%d, CIPHERTEXT=%d, PLAINTEXT_RECOVER=%d",
                plainText.length, cipherText.length, plainTextRecover.length));
        Assert.assertArrayEquals(plainText, plainTextRecover);
    }

    /**
     * Demonstrate simple case of encrypt/decrypt of short, arbitrary binary data chunk.
     *
     * @throws IonicException on cryptography initialization failure, operation failure
     */
    @Test
    public final void testBinaryCipher_GcmEncryptDecrypt_Symmetry() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final BinaryCipherAbstract cipher = new BinaryCipherAesGcm(keyServices);
        final byte[] plainText = new byte[(int) Math.round(Math.pow(2, Byte.SIZE))];
        for (int i = 0; (i < plainText.length); ++i) {
            plainText[i] = (byte) i;
        }
        final byte[] cipherText = cipher.encrypt(plainText, new EncryptAttributes());
        final byte[] plainTextRecover = cipher.decrypt(cipherText, new DecryptAttributes());
        logger.info(String.format("LENGTH: PLAINTEXT=%d, CIPHERTEXT=%d, PLAINTEXT_RECOVER=%d",
                plainText.length, cipherText.length, plainTextRecover.length));
        Assert.assertArrayEquals(plainText, plainTextRecover);
    }

    /**
     * Demonstrate size differential of ciphertext versus that of original plaintext.
     * <p>
     * For {@link BinaryCipherAesCtr},
     * <pre>
     * length(ciphertext) = length(plaintext) + 31.
     * </pre>
     *
     * @throws IonicException on cryptography initialization failure, operation failure
     */
    @Test
    public final void testBinaryCipher_CtrEncryptDecrypt_SpaceRequirement() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final BinaryCipherAbstract cipher = new BinaryCipherAesCtr(keyServices);
        final int exponentMin = 3;
        final int exponentMax = 14;
        for (int i = exponentMin; (i <= exponentMax); ++i) {
            final int count = (int) Math.round(Math.pow(2, i));
            final byte[] plainText = Transcoder.utf8().decode(Value.generate("a", count));
            final byte[] cipherText = cipher.encrypt(plainText, new EncryptAttributes());
            final double multiplier = ((double) cipherText.length) / ((double) plainText.length);
            logger.info(String.format("%10d %10d %10d %10f",
                    i, plainText.length, cipherText.length, multiplier));
            final byte[] plainTextRecover = cipher.decrypt(cipherText, new DecryptAttributes());
            Assert.assertArrayEquals(plainText, plainTextRecover);
        }
    }

    /**
     * Demonstrate size differential of ciphertext versus that of original plaintext.
     * <p>
     * For {@link BinaryCipherAesGcm},
     * <pre>
     * length(ciphertext) = length(plaintext) + 47.
     * </pre>
     *
     * @throws IonicException on cryptography initialization failure, operation failure
     */
    @Test
    public final void testBinaryCipher_GcmEncryptDecrypt_SpaceRequirement() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final BinaryCipherAbstract cipher = new BinaryCipherAesGcm(keyServices);
        final int exponentMin = 3;
        final int exponentMax = 14;
        for (int i = exponentMin; (i <= exponentMax); ++i) {
            final int count = (int) Math.round(Math.pow(2, i));
            final byte[] plainText = Transcoder.utf8().decode(Value.generate("a", count));
            final byte[] cipherText = cipher.encrypt(plainText, new EncryptAttributes());
            final double multiplier = ((double) cipherText.length) / ((double) plainText.length);
            logger.info(String.format("%10d %10d %10d %10f",
                    i, plainText.length, cipherText.length, multiplier));
            final byte[] plainTextRecover = cipher.decrypt(cipherText, new DecryptAttributes());
            Assert.assertArrayEquals(plainText, plainTextRecover);
        }
    }

    /**
     * Demonstrate deterministic encryption using {@link BinaryCipherAesCtr}.
     *
     * @throws IonicException on cryptography initialization failure, operation failure
     */
    @Test
    public final void testBinaryCipher_CtrDeterministicEncryption_ExpectSymmetry() throws IonicException {
        // create Machina-backed KeyServices providing access to a single key
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final CreateKeysRequest createKeysRequest = new CreateKeysRequest(new CreateKeysRequest.Key());
        final KeyServicesSingleKey keyServicesWrapper = new KeyServicesSingleKey(keyServices, createKeysRequest);
        // configure cipher to use deterministic IV generation
        final EncryptAttributes encryptAttributes = new EncryptAttributes();
        encryptAttributes.setMetadata(AesCipher.IV_ALGORITHM, CryptoAbstract.HMAC_ALGORITHM);
        // verify deterministic functionality
        final byte[] plainText = Transcoder.utf8().decode("Hello Machina!");
        final BinaryCipherAbstract cipher1 = new BinaryCipherAesCtr(keyServicesWrapper);
        final byte[] cipherText1 = cipher1.encrypt(plainText, new EncryptAttributes(encryptAttributes));
        final BinaryCipherAbstract cipher2 = new BinaryCipherAesCtr(keyServicesWrapper);
        final byte[] cipherText2 = cipher2.encrypt(plainText, new EncryptAttributes(encryptAttributes));
        Assert.assertArrayEquals(cipherText1, cipherText2);
        // verify reversibility of operation
        final byte[] plainText1 = cipher1.decrypt(cipherText1, new DecryptAttributes());
        final byte[] plainText2 = cipher2.decrypt(cipherText2, new DecryptAttributes());
        Assert.assertArrayEquals(plainText, plainText1);
        Assert.assertArrayEquals(plainText, plainText2);
    }

    /**
     * Demonstrate deterministic encryption using {@link BinaryCipherAesGcm}.
     *
     * @throws IonicException on cryptography initialization failure, operation failure
     */
    @Test
    public final void testBinaryCipher_GcmDeterministicEncryption_ExpectSymmetry() throws IonicException {
        // create Machina-backed KeyServices providing access to a single key
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final CreateKeysRequest createKeysRequest = new CreateKeysRequest(new CreateKeysRequest.Key());
        final KeyServicesSingleKey keyServicesWrapper = new KeyServicesSingleKey(keyServices, createKeysRequest);
        // configure cipher to use deterministic IV generation
        final EncryptAttributes encryptAttributes = new EncryptAttributes();
        encryptAttributes.setMetadata(AesCipher.IV_ALGORITHM, CryptoAbstract.HMAC_ALGORITHM);
        // verify deterministic functionality
        final byte[] plainText = Transcoder.utf8().decode("Hello Machina!");
        final BinaryCipherAbstract cipher1 = new BinaryCipherAesGcm(keyServicesWrapper);
        final byte[] cipherText1 = cipher1.encrypt(plainText, new EncryptAttributes(encryptAttributes));
        final BinaryCipherAbstract cipher2 = new BinaryCipherAesGcm(keyServicesWrapper);
        final byte[] cipherText2 = cipher2.encrypt(plainText, new EncryptAttributes(encryptAttributes));
        Assert.assertArrayEquals(cipherText1, cipherText2);
        // verify reversibility of operation
        final byte[] plainText1 = cipher1.decrypt(cipherText1, new DecryptAttributes());
        final byte[] plainText2 = cipher2.decrypt(cipherText2, new DecryptAttributes());
        Assert.assertArrayEquals(plainText, plainText1);
        Assert.assertArrayEquals(plainText, plainText2);
    }
}
