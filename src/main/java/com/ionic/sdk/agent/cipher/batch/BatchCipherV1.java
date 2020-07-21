package com.ionic.sdk.agent.cipher.batch;

import com.ionic.sdk.agent.cipher.batch.data.CipherTextItem;
import com.ionic.sdk.agent.cipher.batch.data.PlainTextItem;
import com.ionic.sdk.agent.cipher.batch.spec.BatchIvParameterSpec;
import com.ionic.sdk.agent.cipher.data.DecryptAttributes;
import com.ionic.sdk.agent.cipher.data.EncryptAttributes;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ionic Machina Tools batch crypto implementation, version 1.  This wrapper object is intended to perform
 * cryptography operations on a discrete set of related data values.  It is intended for use in applications where
 * storage space for the resulting data is constrained.
 * <p>
 * The underlying cryptography leverages {@link AesCtrCipher}.
 * <p>
 * The storage requirement for the encrypted form of the data value set is equal to that of the original data, plus
 * that of the associated key id (eleven printable ASCII characters).  The initialization vectors used for the
 * ciphertexts are derived using RNG, and stored in the {@link KeyAttributesMap} of the key.
 * <p>
 * The {@link com.ionic.sdk.agent.cipher.batch.data.PlainTextItem} values provided in a call to
 * {@link #encrypt(EncryptAttributes, List)} must match (count and order) the
 * {@link com.ionic.sdk.agent.cipher.batch.data.CipherTextItem} values provided in a subsequent call to
 * {@link #decrypt(String, DecryptAttributes, List)}.  Otherwise, the decryption operation will fail.
 */
public final class BatchCipherV1 extends BatchCipherAbstract {

    /**
     * Constructor.
     *
     * @param keyServices the key services implementation; used to broker key transactions and crypto operations
     */
    public BatchCipherV1(final KeyServices keyServices) {
        super(keyServices);
    }

    @Override
    public List<CipherTextItem> encrypt(final EncryptAttributes encryptAttributes,
                                          final List<PlainTextItem> items) throws IonicException {
        final ArrayList<CipherTextItem> cipherTextResults = new ArrayList<CipherTextItem>();
        // fabricate batch IV
        final KeyAttributesMap keyAttributes = encryptAttributes.getKeyAttributes();
        final BatchIvParameterSpec batchParameterSpec = new BatchIvParameterSpec(items.size());
        keyAttributes.put(IONIC_IVS, Collections.singletonList(batchParameterSpec.getIvData()));
        // create key
        final KeyServices keyServices = getKeyServices();
        final CreateKeysResponse createKeysResponse = keyServices.createKey(
                keyAttributes, encryptAttributes.getMetadata());
        final CreateKeysResponse.Key createKey = createKeysResponse.getFirstKey();
        encryptAttributes.setKeyResponse(createKey);
        // iterate through plaintexts
        final AesCtrCipher cipher = new AesCtrCipher(createKey.getSecretKey());
        for (PlainTextItem item : items) {
            final byte[] plainText = item.getData();
            final byte[] cipherText = new byte[plainText.length];
            final ByteBuffer plainBuffer = ByteBuffer.wrap(plainText);
            final ByteBuffer cipherBuffer = ByteBuffer.wrap(cipherText);
            final int count = cipher.encrypt(plainBuffer, cipherBuffer, batchParameterSpec.next());
            SdkData.checkTrue(cipherText.length == count, SdkError.ISAGENT_ERROR);
            cipherTextResults.add(new CipherTextItem(cipherText));
        }
        return cipherTextResults;
    }

    @Override
    public List<PlainTextItem> decrypt(final String keyId, final DecryptAttributes decryptAttributes,
                                         final List<CipherTextItem> items) throws IonicException {
        final ArrayList<PlainTextItem> plainTextResults = new ArrayList<PlainTextItem>();
        // fetch key
        final KeyServices keyServices = getKeyServices();
        final GetKeysResponse getKeysResponse = keyServices.getKey(keyId, decryptAttributes.getMetadata());
        final GetKeysResponse.Key getKey = getKeysResponse.getFirstKey();
        decryptAttributes.setKeyResponse(getKey);
        // reconstitute batch IVs
        final String ivs = getBatchAttribute(decryptAttributes.getKeyAttributes(), IONIC_IVS);
        final BatchIvParameterSpec batchParameterSpec = new BatchIvParameterSpec(ivs);
        // iterate through ciphertexts
        final AesCtrCipher cipher = new AesCtrCipher(getKey.getSecretKey());
        for (CipherTextItem item : items) {
            final byte[] cipherText = item.getData();
            final byte[] plainText = new byte[cipherText.length];
            final ByteBuffer cipherBuffer = ByteBuffer.wrap(cipherText);
            final ByteBuffer plainBuffer = ByteBuffer.wrap(plainText);
            final int count = cipher.decrypt(plainBuffer, cipherBuffer, batchParameterSpec.next());
            SdkData.checkTrue(plainText.length == count, SdkError.ISAGENT_ERROR);
            plainTextResults.add(new PlainTextItem(plainText));
        }
        return plainTextResults;
    }
}
