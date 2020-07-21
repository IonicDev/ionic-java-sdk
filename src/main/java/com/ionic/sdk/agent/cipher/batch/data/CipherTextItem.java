package com.ionic.sdk.agent.cipher.batch.data;

/**
 * Container for item in the context of a {@link com.ionic.sdk.agent.cipher.batch.BatchCipherAbstract} API usage.  An
 * instance of this class may be used to encrypt multiple logically related items using an single Ionic Machina key.
 */
public class CipherTextItem extends DataItem {

    /**
     * Constructor.
     *
     * @param cipherText the data to be stored
     */
    public CipherTextItem(final byte[] cipherText) {
        super(cipherText);
    }
}
