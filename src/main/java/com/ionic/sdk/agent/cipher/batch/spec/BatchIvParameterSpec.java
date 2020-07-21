package com.ionic.sdk.agent.cipher.batch.spec;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.error.IonicException;

import javax.crypto.spec.IvParameterSpec;

/**
 * A utility class using RNG to generate data for use as the initialization vector (IV) for a set of data values.
 */
public final class BatchIvParameterSpec extends AbstractBatchParameterSpec {

    /**
     * Constructor.  Use RNG to generate IV data for a set of encryption / decryption operations.
     *
     * @param count the number of IVs to be obtained from this object
     * @throws IonicException on failure of the rand() function
     */
    public BatchIvParameterSpec(final int count) throws IonicException {
        super(count);
    }

    /**
     * Constructor.  Reconstitute IVs from stored base64 representation.
     *
     * @param ivsB64 previously generated IV data
     */
    public BatchIvParameterSpec(final String ivsB64) {
        super(ivsB64);
    }

    /**
     * Obtain a reference to the next IV to be used.
     *
     * @return an IV to be used in a subsequent cryptography operation
     */
    public IvParameterSpec next() {
        return new IvParameterSpec(getIvNext(), 0, AesCipher.SIZE_IV);
    }
}
