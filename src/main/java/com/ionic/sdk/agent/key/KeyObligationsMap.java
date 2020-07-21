package com.ionic.sdk.agent.key;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Contains map of obligations used when a key was created.
 */
public final class KeyObligationsMap extends TreeMap<String, List<String>> {

    /**
     * Constructs an empty KeyObligationsMap.
     */
    public KeyObligationsMap() {
        super();
    }

    /**
     * Constructs a new HashMap with the same mappings as the specified map.
     *
     * @param keyMap
     *      The specified map to initialize with.
     */
    public KeyObligationsMap(final KeyObligationsMap keyMap) {
        super();
        if (keyMap != null) {
            for (final Entry<String, List<String>> entry : keyMap.entrySet()) {
                put(entry.getKey(), new ArrayList<String>(entry.getValue()));
            }
        }
    }

    /**
     * Check if the map is empty or not.
     *
     * @deprecated
     *      please use {@link Map#isEmpty isEmpty} instead.
     * @return true if it contains no key-value mappings.
     */
    @Deprecated
    public boolean empty() {
        return this.isEmpty();
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key
     *          attribute key
     * @return attribute value
     */
    public List<String> get(final String key) {
        return ((key == null) ? null : super.get(key));
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old value is replaced.
     *
     * @deprecated
     *      please use {@link Map#put put} instead.
     *
     * @param key
     *          attribute key
     * @param value
     *          attribute value, which cannot be null
     * @throws NullPointerException
     *          when either the key or the value is null.
     */
    @Deprecated
    public void set(final String key, final Collection<String> value)
        throws NullPointerException {
        if (key == null) {
            throw new NullPointerException("key cannot be null");
        }
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }

        // ignore return of HashMap.put()
        this.put(key, new ArrayList<String>(value));
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @deprecated
     *  please use {@link Map#remove remove} instead.
     *
     * @param key
     *          attribute key
     * @throws NullPointerException
     *           when the specified key is null
     * @throws IndexOutOfBoundsException
     *           when the specified key does not exist
     */
    @Deprecated
    public void delete(final String key)
        throws NullPointerException, IndexOutOfBoundsException {
        if (key == null) {
            throw new NullPointerException("key cannot be null");
        }
        if (this.remove(key) == null) {
            throw new IndexOutOfBoundsException("no mapping for key");
        }
    }

    /**
     * Checks if map contains given key.
     *
     * @deprecated
     *      please use {@link Map#containsKey containsKey} instead.
     *
     * @param key
     *          attribute key
     * @return true if map contains key, false otherwise
     */
    @Deprecated
    public boolean hasKey(final String key) {
        return ((key != null) && this.containsKey(key));
    }

    /**
     * Returns an iterator to enable iterating over the elements of this map.
     *
     * @deprecated
     *      please use {@link Map#entrySet entrySet} and iterate over that set.
     *
     * @return Iterator object
     */
    @Deprecated
    public Iterator<Map.Entry<String, List<String>>> iterator() {
        return this.entrySet().iterator();
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.7.0". */
    private static final long serialVersionUID = 3239952739509879818L;
}
