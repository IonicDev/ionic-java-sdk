package com.ionic.sdk.core.io;

import com.ionic.sdk.core.annotation.InternalUseOnly;

/**
 * Container for byte data.  This object is intended to be used as an intermediate cache for data
 * in support of Ionic file ciphers.
 * <p>
 * This object is meant to be wrapped by {@link ByteQueue}, which manages multiple blocks and allows for
 * caching of arbitrary amounts of data (subject to JVM memory constraints).
 * <p>
 * In order to simplify its logic, this object handles manipulation of its state naively, and depends
 * on its wrapping object to protect against boundary conditions.
 */
@InternalUseOnly
public final class ByteBlock {

    /**
     * The byte data to be cached.
     */
    private final byte[] block;

    /**
     * The position in this block at which existing data should be read.
     */
    private int head;

    /**
     * The position in this block at which new data should be written.
     */
    private int tail;

    /**
     * Constructor.
     *
     * @param size the maximum amount of data (in bytes) which should be cached in this object
     */
    public ByteBlock(final int size) {
        this.block = new byte[size];
        this.head = 0;
        this.tail = 0;
    }

    /**
     * Reset the internal state of this object, allowing for reuse.
     */
    public void reset() {
        head = 0;
        tail = 0;
    }

    /**
     * @return the position in this block at which existing data should be read
     */
    public int getHead() {
        return head;
    }

    /**
     * @return the position in this block at which new data should be written
     */
    public int getTail() {
        return tail;
    }

    /**
     * @return the amount of data available in this block to read
     */
    public int available() {
        return tail - head;
    }

    /**
     * @return the amount of free space available in this block to be written to
     */
    public int freeSpace() {
        return block.length - (tail - head);
    }

    /**
     * Write data into this object, to be cached for later use.
     *
     * @param bytes  the source buffer from which the data should be read
     * @param offset the offset in the source buffer at which to start reading
     * @param length the number of bytes of source buffer data to write
     * @return the number of bytes of data written
     */
    public int add(final byte[] bytes, final int offset, final int length) {
        final int added = Math.min((block.length - tail), length);
        System.arraycopy(bytes, offset, block, tail, added);
        tail += added;
        return added;
    }

    /**
     * Read cached data from this object.
     *
     * @param bytes  the target buffer to which the data should be written
     * @param offset the offset in the target buffer at which to start writing
     * @param length the number of bytes of data to read
     * @return the number of bytes of data read
     */
    public int remove(final byte[] bytes, final int offset, final int length) {
        final int count = Math.min((tail - head), length);
        System.arraycopy(block, head, bytes, offset, count);
        head += count;
        return count;
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from this data.
     *
     * @param count the number of bytes to be skipped
     * @return the actual number of bytes skipped.
     */
    public int skip(final int count) {
        head += count;
        return count;
    }

    /**
     * Returns a {@code String} object representing this object's value.
     *
     * @return a string representation of the value of this object
     */
    @Override
    public String toString() {
        return String.format(PATTERN_TO_STRING, hashCode(), block.length, head, tail);
    }

    /**
     * The pattern used to construct a string representation of this object, for diagnostic purposes.
     */
    private static final String PATTERN_TO_STRING = "[ByteBlock: hashCode=%x size=%d, head=%d, tail=%d]";
}
