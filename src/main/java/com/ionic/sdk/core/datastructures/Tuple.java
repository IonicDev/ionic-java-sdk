package com.ionic.sdk.core.datastructures;

/**
 * Implementation of a Tuple.
 * @author Ionic Secuirty
 * @param <A> Type of the first reference.
 * @param <B> Type of the second reference.
 */
public class Tuple<A, B> {
    /**
     * Read only first field.
     */
    private final A first;

    /**
     * Read only second field.
     */
    private final B second;

    /**
     * constructor for the Tuple class.
     * @param a the value to store as the first parameter.
     * @param b the value to store as the second parameter.
     */
    public Tuple(final A a, final B b) {
      first  = a;
      second = b;
    }

    /**
     * A getter for the read only first field.
     * @return The first field of the tuple.
     */
    public final A first() {
        return first;
    }

    /**
     * A getter for the read only second field.
     * @return The second field of the tuple.
     */
    public final B second() {
        return second;
    }
  }
