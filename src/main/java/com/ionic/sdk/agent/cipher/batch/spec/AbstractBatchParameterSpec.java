package com.ionic.sdk.agent.cipher.batch.spec;

import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.rng.CryptoRng;
import com.ionic.sdk.error.IonicException;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

/**
 * A utility class using RNG to generate data for use as the initialization vector (IV) for a set of data values.
 */
public abstract class AbstractBatchParameterSpec {

    /**
     * The IV data.
     */
    private final byte[] ivData;

    /**
     * The index (to be pre-incremented) of the portion of data to be used as the next IV.
     */
    private int index;

    /**
     * @return the IV data associated with the batch encryption operation
     */
    public final String getIvData() {
        return Transcoder.base64().encode(ivData);
    }

    /**
     * @return the IV data associated with the next batch data item
     */
    protected final byte[] getIvNext() {
        final int offset = (++index) * AesCipher.SIZE_IV;
        return Arrays.copyOfRange(ivData, offset, offset + AesCipher.SIZE_IV);
    }

    /**
     * Constructor.  Use RNG to generate IV data for a set of encryption / decryption operations.
     *
     * @param count the number of IVs to be obtained from this object
     * @throws IonicException on failure of the rand() function
     */
    public AbstractBatchParameterSpec(final int count) throws IonicException {
        this.ivData = new CryptoRng().rand(new byte[count * AesCipher.SIZE_IV]);
        this.index = -1;
    }

    /**
     * Constructor.  Reconstitute IVs from stored base64 representation.
     *
     * @param ivsB64 previously generated IV data
     */
    public AbstractBatchParameterSpec(final String ivsB64) {
        this.ivData = Transcoder.base64().decode(ivsB64);
        this.index = -1;
    }

    /**
     * Obtain a reference to the next IV to be used.
     *
     * @return an IV to be used in a subsequent cryptography operation
     */
    public abstract AlgorithmParameterSpec next();
}
