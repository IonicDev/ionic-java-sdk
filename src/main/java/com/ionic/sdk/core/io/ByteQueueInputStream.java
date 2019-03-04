package com.ionic.sdk.core.io;


import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Use this class to enable write access to an input stream, in order to buffer bytes.  This is needed to implement
 * Ionic file ciphers.
 */
public final class ByteQueueInputStream extends InputStream {

    /**
     * Buffer of bytes which may be read from and written to, allowing for buffered handling of the data stream.
     */
    private final ByteQueue byteQueue;

    /**
     * Intermediate buffer for data to be read from this stream.  This single byte array is used to transfer data
     * from the ByteQueue API (which takes byte[]) to the InputStream "read()" API (which takes a single byte).
     */
    private final byte[] byteBuffer;

    /**
     * Constructor.
     *
     * @param blockSize the size in bytes of each {@link ByteBlock} managed by the member queue
     */
    public ByteQueueInputStream(final int blockSize) {
        this.byteQueue = new ByteQueue(blockSize);
        this.byteBuffer = new byte[1];
    }

    /**
     * Write additional bytes to the buffer, which will then be consumed by the InputStream "read()" operation.
     *
     * @param bytes  the bytes that should be written to the internal buffer
     * @param offset the offset in the parameter buffer from which to start reading
     * @param length the number of bytes to add
     */
    public void addBytes(final byte[] bytes, final int offset, final int length) {
        byteQueue.addData(bytes, offset, length);
    }

    /**
     * Write additional bytes to the buffer, which will then be consumed by the InputStream "read()" operation.
     *
     * @param byteBuffer the {@link ByteBuffer} which contains data to be written to the internal buffer
     */
    public void addBytes(final ByteBuffer byteBuffer) {
        final int sizeBlock = 256 * 256;
        final byte[] block = new byte[sizeBlock];
        while (byteBuffer.position() < byteBuffer.limit()) {
            final int count = Math.min(byteBuffer.limit() - byteBuffer.position(), block.length);
            byteBuffer.get(block, 0, count);
            byteQueue.addData(block, 0, count);
        }
    }

    @Override
    public int read() {
        int b = -1;
        if (byteQueue.available() > 0) {
            byteQueue.removeData(byteBuffer, 0, byteBuffer.length);
            b = byteBuffer[0] & BYTE_MASK;
        }
        return b;
    }

    @Override
    public int read(final byte[] bytes, final int offset, final int len) {
        final int countToRead = Math.min(len, byteQueue.available());
        return byteQueue.removeData(bytes, offset, countToRead);
    }

    @Override
    public int available() {
        return byteQueue.available();
    }

    /**
     * Mask used to filter out high order bits of int to make a byte.
     */
    private static final int BYTE_MASK = (1 << Byte.SIZE) - 1;
}
