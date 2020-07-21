package com.ionic.sdk.agent.data;

import com.ionic.sdk.error.SdkData;

import java.util.HashMap;

/**
 * Storage container for metadata.
 */
public class MetadataMap extends HashMap<String, String> {

    /**
     * Returns true if there are no entries in the map, false otherwise.
     *
     * @return true if there are no entries in the map, false otherwise.
     */
    public final boolean empty() {
        return super.isEmpty();
    }

    /**
     * Inserts key-value pair into collection.
     *
     * @param key   metadata key
     * @param value metadata value
     * @throws NullPointerException on invalid (null) parameters
     */
    public final void set(final String key, final String value) {
        SdkData.checkNotNullNPE(key, getClass().getName());
        SdkData.checkNotNullNPE(value, getClass().getName());
        super.put(key, value);
    }

    /**
     * Checks if collection contains given key.
     *
     * @param key metadata key
     * @return true if collection contains key, false otherwise
     */
    public final boolean hasKey(final String key) {
        return key != null && super.containsKey(key);
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.7.0". */
    private static final long serialVersionUID = 8283514370155381706L;
}
