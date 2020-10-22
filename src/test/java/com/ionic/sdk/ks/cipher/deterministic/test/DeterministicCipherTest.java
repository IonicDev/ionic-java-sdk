package com.ionic.sdk.ks.cipher.deterministic.test;

import com.ionic.sdk.agent.cipher.chunk.ChunkCipherAbstract;
import com.ionic.sdk.agent.cipher.chunk.ChunkCipherAuto;
import com.ionic.sdk.agent.cipher.chunk.data.ChunkCryptoEncryptAttributes;
import com.ionic.sdk.agent.request.createkey.CreateKeysRequest;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;
import com.ionic.sdk.key.cache.KeyServicesSingleKey;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases enumerating deterministic encryption usage scenarios.
 * <p>
 * Deterministic encryption provides the ability to derive the same ciphertext for a given plaintext.  It can be
 * used in database search applications to match records with a particular value.
 */
public class DeterministicCipherTest {

    /**
     * Demonstrate simple use case of deterministic encryption using {@link ChunkCipherAuto}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public void testChunkCipherAuto_DeterministicIv() throws IonicException {
        // create Machina-backed KeyServices providing access to a single key
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final CreateKeysRequest createKeysRequest = new CreateKeysRequest(new CreateKeysRequest.Key());
        final KeyServicesSingleKey keyServicesWrapper = new KeyServicesSingleKey(keyServices, createKeysRequest);
        // configure chunk cipher to use deterministic IV generation
        final String plainText = "Hello Machina!";
        final ChunkCryptoEncryptAttributes attributes = new ChunkCryptoEncryptAttributes();
        attributes.setMetadata("ionic-iv-algorithm", "HmacSHA256");
        // verify functionality
        final ChunkCipherAbstract chunkCipher1 = new ChunkCipherAuto(keyServicesWrapper);
        final String cipherText1 = chunkCipher1.encrypt(plainText, new ChunkCryptoEncryptAttributes(attributes));
        final ChunkCipherAbstract chunkCipher2 = new ChunkCipherAuto(keyServicesWrapper);
        final String cipherText2 = chunkCipher2.encrypt(plainText, new ChunkCryptoEncryptAttributes(attributes));
        Assert.assertEquals(cipherText1, cipherText2);
    }
}
