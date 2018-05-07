package com.ionic.sdk.agent.data;

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
     */
    public final void set(final String key, final String value) {
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
}
