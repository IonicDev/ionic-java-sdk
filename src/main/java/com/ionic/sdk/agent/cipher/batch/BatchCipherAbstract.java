package com.ionic.sdk.agent.cipher.batch;

import com.ionic.sdk.agent.cipher.batch.data.CipherTextItem;
import com.ionic.sdk.agent.cipher.batch.data.PlainTextItem;
import com.ionic.sdk.agent.cipher.data.DecryptAttributes;
import com.ionic.sdk.agent.cipher.data.EncryptAttributes;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.util.List;

/**
 * Ionic Machina Tools batch crypto abstract implementation.  This wrapper object is intended to perform
 * cryptography operations on a discrete set of related data values.  It is intended for use in applications where
 * storage space for the resulting data is constrained.
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
public abstract class BatchCipherAbstract {

    /**
     * Key services implementation; used to broker key transactions and crypto operations.
     */
    private final KeyServices keyServices;

    /**
     * @return the key services implementation; used to broker key transactions and crypto operations
     */
    public final KeyServices getKeyServices() {
        return keyServices;
    }

    /**
     * Constructor.
     *
     * @param keyServices the key services implementation
     */
    public BatchCipherAbstract(final KeyServices keyServices) {
        this.keyServices = keyServices;
    }

    /**
     * Encrypt a discrete set of logically related data values.
     *
     * @param encryptAttributes the encryption parameters
     * @param items             the plaintext values to be encrypted
     * @return the encrypted ciphertext values
     * @throws IonicException on IV generation failure (RNG), on key generation failure, or on cryptography failures
     */
    public abstract List<CipherTextItem> encrypt(
            EncryptAttributes encryptAttributes, List<PlainTextItem> items) throws IonicException;

    /**
     * Decrypt a discrete set of logically related data values.
     *
     * @param keyId             the identifier of the key to be used in the decryption operation
     * @param decryptAttributes the decryption parameters
     * @param items             the ciphertext values to be decrypted
     * @return the decrypted plaintext values
     * @throws IonicException on key generation failure, or on cryptography failures
     */
    public abstract List<PlainTextItem> decrypt(
            String keyId, DecryptAttributes decryptAttributes, List<CipherTextItem> items) throws IonicException;

    /**
     * Reserved key attribute denoting IV data associated with a batch cryptography operation.
     */
    public static final String IONIC_IVS = "ionic-ivs";

    /**
     * Reserved key attribute denoting ATAG data associated with a batch cryptography operation.
     */
    public static final String IONIC_ATAGS = "ionic-atags";

    /**
     * Fetch batch cipher oriented attribute.
     *
     * @param keyAttributes the key attributes object from the key
     * @param key           the key of the desired key attributes entry
     * @return the batch cipher value (to be used in reconstituting the batch plaintexts)
     * @throws IonicException on validation failures of the key attribute
     */
    protected String getBatchAttribute(final KeyAttributesMap keyAttributes, final String key) throws IonicException {
        final List<String> values = keyAttributes.get(key);
        SdkData.checkTrue(values != null, SdkError.ISAGENT_MISSINGVALUE);
        SdkData.checkTrue(1 == values.size(), SdkError.ISAGENT_INVALIDVALUE);
        return values.iterator().next();
    }
}
