package com.ionic.sdk.core.io;

import java.io.OutputStream;

/**
 * Use this class to enable read access to an output stream, in order to buffer bytes.  This is needed to implement
 * Ionic file ciphers.
 */
public final class ByteQueueOutputStream extends OutputStream {

    /**
     * Buffer of bytes which may be read from and written to, allowing for buffered handling of the data stream.
     */
    private final ByteQueue byteQueue;

    /**
     * Intermediate buffer for data to be written to this stream.  This single byte array is used to transfer data
     * from the InputStream "write()" API (which takes a single byte) to the ByteQueue API (which takes byte[]).
     */
    private final byte[] byteBuffer;

    /**
     * Constructor.
     *
     * @param blockSize the size in bytes of each {@link ByteBlock} managed by the member queue
     */
    public ByteQueueOutputStream(final int blockSize) {
        this.byteQueue = new ByteQueue(blockSize);
        this.byteBuffer = new byte[1];
    }

    /**
     * @return the bytes buffered in this <code>OutputStream</code>
     */
    public ByteQueue getByteQueue() {
        return byteQueue;
    }

    @Override
    public void write(final int b) {
        byteBuffer[0] = (byte) b;
        byteQueue.addData(byteBuffer, 0, byteBuffer.length);
    }
}
