package com.ionic.sdk.agent.cipher.batch.data;

import java.util.Arrays;

/**
 * Generic container for array of bytes.
 */
public class DataItem {

    /**
     * The data associated with the item.
     */
    private final byte[] data;

    /**
     * Constructor.
     *
     * @param data the data to be stored
     */
    public DataItem(final byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * @return the data stored in this object
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
