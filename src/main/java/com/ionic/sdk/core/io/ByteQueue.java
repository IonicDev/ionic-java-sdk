package com.ionic.sdk.core.io;

import com.ionic.sdk.core.annotation.InternalUseOnly;

import java.util.ArrayDeque;

/**
 * Container for byte data.  This object is intended to be used as an intermediate cache for data
 * in support of Ionic file ciphers.
 * <p>
 * The implementation uses a FIFO queue of {@link ByteBlock} to hold the cached data.  ByteBlock objects are
 * reused in order to minimize memory allocation overhead.
 */
@InternalUseOnly
public final class ByteQueue {

    /**
     * The size in bytes of each {@link ByteBlock} managed by this queue.
     */
    private final int size;

    /**
     * The FIFO queue of ByteBlock objects which hold the cached data.
     */
    private final ArrayDeque<ByteBlock> queue;

    /**
     * A queue of previously used blocks, which may be reused (in order to streamline the number of allocates needed).
     */
    private final ArrayDeque<ByteBlock> queueFree;

    /**
     * The block in which existing data should be read.
     */
    private ByteBlock headBlock;

    /**
     * The block in which new data should be written.
     */
    private ByteBlock tailBlock;

    /**
     * Constructor.
     *
     * @param size the maximum amount of data (in bytes) which should be cached in each block
     */
    public ByteQueue(final int size) {
        this.size = size;
        this.queue = new ArrayDeque<ByteBlock>();
        this.queueFree = new ArrayDeque<ByteBlock>();
        final ByteBlock byteBlock = new ByteBlock(size);
        this.queue.add(byteBlock);
        this.headBlock = byteBlock;
        this.tailBlock = byteBlock;
    }

    /**
     * @return the amount of data available in all blocks to read
     */
    public int available() {
        int available = 0;
        for (final ByteBlock byteBlock : queue) {
            available += byteBlock.available();
        }
        return available;
    }

    /**
     * @return the amount of free space available in all blocks to be written to
     */
    public int freeSpace() {
        int freeSpace = 0;
        for (final ByteBlock byteBlock : queue) {
            freeSpace += byteBlock.freeSpace();
        }
        for (final ByteBlock byteBlock : queueFree) {
            freeSpace += byteBlock.freeSpace();
        }
        return freeSpace;
    }

    /**
     * Write data into this object, to be cached for later use.
     *
     * @param bytes  the source buffer from which the data should be read
     * @param offset the offset in the source buffer at which to start reading
     * @param length the number of bytes of source buffer data to write
     * @return the number of bytes of data written
     */
    public int addData(final byte[] bytes, final int offset, final int length) {
        int count = 0;
        while (count < length) {
            final ByteBlock block = tailBlock;  // queue.getLast();
            count += block.add(bytes, (offset + count), (length - count));
            if (count < length) {
                final ByteBlock byteBlock = queueFree.isEmpty() ? new ByteBlock(size) : queueFree.pop();
                queue.add(byteBlock);
                tailBlock = byteBlock;
            }
        }
        return count;
    }

    /**
     * Read cached data from this object.
     *
     * @param bytes  the target buffer to which the data should be written
     * @param offset the offset in the target buffer at which to start writing
     * @param length the number of bytes of data to read
     * @return the number of bytes of data read
     */
    public int removeData(final byte[] bytes, final int offset, final int length) {
        int count = 0;
        while (count < length) {
            final ByteBlock block = headBlock;  // queue.getFirst();
            final int removed = block.remove(bytes, offset + count, length - count);
            count += removed;
            if (headBlock.equals(tailBlock)) {
                break;
            }
            if (block.available() == 0) {
                queue.remove(block);
                block.reset();
                queueFree.add(block);  // reuse for malloc efficiency
                headBlock = queue.getFirst();
            }
        }
        return count;
    }

    /**
     * Returns a {@code String} object representing this object's value.
     *
     * @return a string representation of the value of this object
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        for (final ByteBlock byteBlock : queue) {
            buffer.append(byteBlock.toString());
        }
        buffer.append(String.format(PATTERN_CURSORS, headBlock.toString(), tailBlock.toString()));
        return buffer.toString();
    }

    /**
     * The pattern used to construct a string representation of this object, for diagnostic purposes.
     */
    private static final String PATTERN_CURSORS = "%nRead: %s%nWrite: %s";
}
