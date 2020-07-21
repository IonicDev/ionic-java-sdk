package com.ionic.sdk.agent.cipher.batch;

import com.ionic.sdk.agent.cipher.batch.data.CipherTextItem;
import com.ionic.sdk.agent.cipher.batch.data.PlainTextItem;
import com.ionic.sdk.agent.cipher.batch.spec.BatchGcmParameterSpec;
import com.ionic.sdk.agent.cipher.data.DecryptAttributes;
import com.ionic.sdk.agent.cipher.data.EncryptAttributes;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.agent.request.createkey.CreateKeysResponse;
import com.ionic.sdk.agent.request.getkey.GetKeysResponse;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysRequest;
import com.ionic.sdk.agent.request.updatekey.UpdateKeysResponse;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Ionic Machina Tools batch crypto implementation, version 2.  This wrapper object is intended to perform
 * cryptography operations on a discrete set of related data values.  It is intended for use in applications where
 * storage space for the resulting data is constrained.
 * <p>
 * The underlying cryptography leverages {@link AesGcmCipher}.
 * <p>
 * The storage requirement for the encrypted form of the data value set is equal to that of the original data, plus
 * that of the associated key id (eleven printable ASCII characters).  The initialization vectors used for the
 * ciphertexts are derived using RNG, and stored in the {@link KeyAttributesMap} of the key.  The GCM authentication
 * tags of the ciphertexts are stored in the mutable {@link KeyAttributesMap} of the key.
 * <p>
 * The {@link com.ionic.sdk.agent.cipher.batch.data.PlainTextItem} values provided in a call to
 * {@link #encrypt(EncryptAttributes, List)} must match (count and order) the
 * {@link com.ionic.sdk.agent.cipher.batch.data.CipherTextItem} values provided in a subsequent call to
 * {@link #decrypt(String, DecryptAttributes, List)}.  Otherwise, the decryption operation will fail.
 */
public final class BatchCipherV2 extends BatchCipherAbstract {

    /**
     * Constructor.
     *
     * @param keyServices the key services implementation; used to broker key transactions and crypto operations
     */
    public BatchCipherV2(final KeyServices keyServices) {
        super(keyServices);
    }

    @Override
    public List<CipherTextItem> encrypt(final EncryptAttributes encryptAttributes,
                                          final List<PlainTextItem> items) throws IonicException {
        final ArrayList<CipherTextItem> cipherTextResults = new ArrayList<CipherTextItem>();
        // fabricate batch IV
        final KeyAttributesMap keyAttributes = encryptAttributes.getKeyAttributes();
        final BatchGcmParameterSpec batchParameterSpec = new BatchGcmParameterSpec(items.size());
        keyAttributes.put(IONIC_IVS, Collections.singletonList(batchParameterSpec.getIvData()));
        // storage for batch ATAGs
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(items.size() * AesCipher.SIZE_ATAG);
        // create key
        final KeyServices keyServices = getKeyServices();
        final CreateKeysResponse createKeysResponse = keyServices.createKey(
                keyAttributes, encryptAttributes.getMetadata());
        final CreateKeysResponse.Key createKey = createKeysResponse.getFirstKey();
        encryptAttributes.setKeyResponse(createKey);
        // iterate through plaintexts
        final AesGcmCipher cipher = new AesGcmCipher(createKey.getSecretKey());
        cipher.setAuthData(Transcoder.utf8().decode(createKey.getId()));
        for (PlainTextItem item : items) {
            final byte[] plainText = item.getData();
            final byte[] cipherText = new byte[plainText.length + AesCipher.SIZE_ATAG];
            final ByteBuffer plainBuffer = ByteBuffer.wrap(plainText);
            final ByteBuffer cipherBuffer = ByteBuffer.wrap(cipherText);
            final int count = cipher.encrypt(plainBuffer, cipherBuffer, batchParameterSpec.next());
            SdkData.checkTrue(cipherText.length == count, SdkError.ISAGENT_ERROR);
            cipherTextResults.add(new CipherTextItem(Arrays.copyOfRange(cipherText, 0, plainText.length)));
            bos.write(cipherText, plainText.length, AesCipher.SIZE_ATAG);
        }
        // store GCM ATAGs of batch as a mutable attribute of Machina key
        final UpdateKeysRequest updateKeysRequest = new UpdateKeysRequest();
        final UpdateKeysRequest.Key updateKey = new UpdateKeysRequest.Key(createKey, false);
        final KeyAttributesMap mutableAttributes = updateKey.getMutableAttributesMap();
        mutableAttributes.put(IONIC_ATAGS, Collections.singletonList(Transcoder.base64().encode(bos.toByteArray())));
        updateKeysRequest.addKey(updateKey);
        // carry out update transaction
        final UpdateKeysResponse updateKeysResponse = keyServices.updateKeys(updateKeysRequest);
        SdkData.checkTrue(updateKeysResponse.getErrors().isEmpty(), SdkError.ISAGENT_REQUESTFAILED);
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
        final BatchGcmParameterSpec batchParameterSpec = new BatchGcmParameterSpec(ivs);
        // reconstitute batch ATAGs
        final String atags = getBatchAttribute(decryptAttributes.getMutableKeyAttributes(), IONIC_ATAGS);
        final ByteArrayInputStream isAtags = new ByteArrayInputStream(Transcoder.base64().decode(atags));
        // iterate through ciphertexts
        final AesGcmCipher cipher = new AesGcmCipher(getKey.getSecretKey());
        cipher.setAuthData(Transcoder.utf8().decode(getKey.getId()));
        for (CipherTextItem item : items) {
            final byte[] cipherText = item.getData();
            final byte[] cipherTextWithAtag = Arrays.copyOf(cipherText, cipherText.length + AesCipher.SIZE_ATAG);
            final int countAtag = isAtags.read(cipherTextWithAtag, cipherText.length, AesCipher.SIZE_ATAG);
            SdkData.checkTrue(countAtag == AesCipher.SIZE_ATAG, SdkError.ISAGENT_INVALIDVALUE);
            final byte[] plainText = new byte[cipherText.length];
            final ByteBuffer cipherBuffer = ByteBuffer.wrap(cipherTextWithAtag);
            final ByteBuffer plainBuffer = ByteBuffer.wrap(plainText);
            final int count = cipher.decrypt(plainBuffer, cipherBuffer, batchParameterSpec.next());
            SdkData.checkTrue(plainText.length == count, SdkError.ISAGENT_INVALIDVALUE);
            plainTextResults.add(new PlainTextItem(plainText));
        }
        return plainTextResults;
    }
}
