package com.ionic.sdk.core.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Encapsulate logic for denoting a view of a {@link SeekableByteChannel}, with a discrete start and end point.
 * <p>
 * As a side effect, note that the instantiation and manipulation of this object alters the position of the
 * {@link SeekableByteChannel}.  During the useful lifetime of this object, no other object should use the channel.
 */
public class ByteChannelWindow {

    /**
     * The backing source of data for the window.
     */
    private final SeekableByteChannel channel;

    /**
     * The offset of the channel at which data reads should start.
     */
    private final long start;

    /**
     * The offset of the channel at which data reads should cease.
     */
    private final long end;

    /**
     * Constructor.
     *
     * @param channel the backing source of data for the window
     * @param start   the offset of the channel at which data reads should begin
     * @param end     the offset of the channel at which data reads should finish
     * @throws IOException on failure to set the starting position for subsequent channel reads
     */
    public ByteChannelWindow(final SeekableByteChannel channel, final long start, final long end) throws IOException {
        this.channel = channel;
        this.channel.position(start);
        this.start = start;
        this.end = end;
    }

    /**
     * @return the offset of the channel at which data reads should begin
     */
    public long getStart() {
        return start;
    }

    /**
     * Returns the number of bytes that can be read from the wrapped {@link SeekableByteChannel}.
     *
     * @return the number of bytes that can be read from the wrapped {@link SeekableByteChannel}
     * @throws IOException on failure to read the current position from the underlying channel
     */
    public long available() throws IOException {
        return end - channel.position();
    }

    /**
     * Reads some number of bytes from the backing channel, and stores them into the buffer array.
     *
     * @param bytes the buffer into which the data is read
     * @return the total number of bytes read into the buffer
     * @throws IOException on failure to read the current position or data from the underlying channel
     */
    public int read(final byte[] bytes) throws IOException {
        final int bytesToRead = Math.min(bytes.length, (int) (end - channel.position()));
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, bytesToRead);
        return channel.read(byteBuffer);
    }

    /**
     * Reads some number of bytes from the backing channel, and stores them into the buffer array.
     *
     * @param byteBuffer the buffer into which the data is read
     * @return the total number of bytes read into the buffer
     * @throws IOException on failure to read the current position or data from the underlying channel
     */
    public int read(final ByteBuffer byteBuffer) throws IOException {
        final int bytesToRead = Math.min(byteBuffer.capacity(), (int) (end - channel.position()));
        final int position = byteBuffer.position();
        byteBuffer.limit(position + bytesToRead);
        final int bytesRead = channel.read(byteBuffer);
        byteBuffer.position(position);
        return bytesRead;
    }
}
