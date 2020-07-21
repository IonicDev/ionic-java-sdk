package com.ionic.sdk.agent.key;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Convenience class allowing for terse instantiation of a single {@link KeyAttributesMap} member.
 * <p>
 * See <a href='https://docs.oracle.com/javase/tutorial/java/javaOO/arguments.html#varargs' target='_blank'>
 * Arbitrary Number of Arguments</a> for more information.
 */
public class KeyAttribute extends ArrayList<String> {

    /**
     * The key value associated with the {@link KeyAttributesMap} member.
     */
    private final String key;

    /**
     * Constructor.
     *
     * @param key    the map key
     * @param values the map values, expressed as a varargs
     */
    public KeyAttribute(final String key, final String... values) {
        super();
        this.key = key;
        this.addAll(Arrays.asList(values));
    }

    /**
     * @return the key value associated with the {@link KeyAttributesMap} member
     */
    public String getKey() {
        return key;
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = 3768757403730422339L;
}
