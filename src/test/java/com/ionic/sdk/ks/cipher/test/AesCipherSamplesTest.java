package com.ionic.sdk.ks.cipher.test;

import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.cipher.aes.AesCipherAbstract;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
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
public class AesCipherSamplesTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Verify simple cryptography symmetry using {@link AesCtrCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testAesCtrCipher_EncryptDecryptStringToBytes() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final CreateKeysResponse.Key key = keyServices.createKey().getFirstKey();
        final String plainText = "Hello, Machina!";
        final AesCtrCipher cipher = new AesCtrCipher();
        cipher.setKey(key.getSecretKey());
        final byte[] cipherText = cipher.encryptString(plainText);
        final String plainTextRecover = cipher.decryptToString(cipherText);
        Assert.assertEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", Transcoder.base64().encode(cipherText)));
    }

    /**
     * Verify simple cryptography symmetry using {@link AesCtrCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testAesCtrCipher_EncryptDecryptStringToString() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final CreateKeysResponse.Key key = keyServices.createKey().getFirstKey();
        final String plainText = "Hello, Machina!";
        final AesCipherAbstract cipher = new AesCtrCipher();
        cipher.setKey(key.getSecretKey());
        final String cipherText = cipher.encryptToBase64(plainText);
        final String plainTextRecover = cipher.decryptBase64ToString(cipherText);
        Assert.assertEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", cipherText));
    }

    /**
     * Verify simple cryptography symmetry using {@link AesGcmCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testAesGcmCipher_EncryptDecryptStringToBytes() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final CreateKeysResponse.Key key = keyServices.createKey().getFirstKey();
        final String plainText = "Hello, Machina!";
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(key.getSecretKey());
        cipher.setAuthData(Transcoder.utf8().decode(key.getId()));
        final byte[] cipherText = cipher.encryptString(plainText);
        final String plainTextRecover = cipher.decryptToString(cipherText);
        Assert.assertEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", Transcoder.base64().encode(cipherText)));
    }

    /**
     * Verify simple cryptography symmetry using {@link AesGcmCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testAesGcmCipher_EncryptDecryptStringToString() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final CreateKeysResponse.Key key = keyServices.createKey().getFirstKey();
        final String plainText = "Hello, Machina!";
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(key.getSecretKey());
        cipher.setAuthData(Transcoder.utf8().decode(key.getId()));
        final String cipherText = cipher.encryptToBase64(plainText);
        final String plainTextRecover = cipher.decryptBase64ToString(cipherText);
        Assert.assertEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", cipherText));
    }
}
