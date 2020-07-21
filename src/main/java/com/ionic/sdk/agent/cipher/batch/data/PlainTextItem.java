package com.ionic.sdk.agent.cipher.batch.data;

import com.ionic.sdk.core.codec.Transcoder;

/**
 * Container for item in the context of a {@link com.ionic.sdk.agent.cipher.batch.BatchCipherAbstract} API usage.  An
 * instance of this class may be used to encrypt multiple logically related items using an single Ionic Machina key.
 */
public class PlainTextItem extends DataItem {

    /**
     * Constructor.
     *
     * @param plainText the plaintext data to be encrypted
     */
    public PlainTextItem(final byte[] plainText) {
        super(plainText);
    }

    /**
     * Constructor.
     *
     * @param plainText the plaintext data to be encrypted
     */
    public PlainTextItem(final String plainText) {
        super(Transcoder.utf8().decode(plainText));
    }
}
