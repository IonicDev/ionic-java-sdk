package com.ionic.sdk.core.io;

import com.ionic.sdk.core.annotation.InternalUseOnly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * An implementation of {@link SeekableByteChannel} that operates against an in-memory byte array.
 * <p>
 * Ionic file cipher implementations need to be able to seek in the file stream in order to perform
 * cryptography operations.  For {@link java.io.File} APIs, {@link java.nio.channels.FileChannel} provides this
 * functionality. For byte[] APIs, {@link ByteBuffer} provides equivalent functionality, but exposes different
 * interfaces.  This class wraps ByteBuffer in a SeekableByteChannel, so byte[] and File APIs may be implemented
 * using the SeekableByteChannel interfaces.
 * <p>
 * See also <a href='http://errorprone.info/bugpattern/ByteBufferBackingArray'
 * target='_blank'>rationale for constructor API.</a>
 */
@InternalUseOnly
public final class SeekableByteBufferChannel implements SeekableByteChannel {

    /**
     * Container for the preloaded bytes to be processed.
     */
    private final ByteBuffer byteBuffer;

    /**
     * Constructor.
     *
     * @param bytes the preloaded bytes to be processed
     */
    public SeekableByteBufferChannel(final byte[] bytes) {
        this.byteBuffer = ByteBuffer.wrap(bytes);
    }

    @Override
    public int read(final ByteBuffer dst) {
        final int length = Math.min(dst.remaining(), byteBuffer.remaining());
        if (length > 0) {
            dst.put(byteBuffer.array(), byteBuffer.position(), length);
            byteBuffer.position(byteBuffer.position() + length);
        }
        return (length > 0) ? length : -1;
    }

    @Override
    public int write(final ByteBuffer src) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long position() {
        return byteBuffer.position();
    }

    @Override
    public SeekableByteChannel position(final long newPosition) {
        byteBuffer.position((int) newPosition);
        return this;
    }

    @Override
    public long size() {
        return byteBuffer.capacity();
    }

    @Override
    public SeekableByteChannel truncate(final long size) throws IOException {
        throw new IOException(new UnsupportedOperationException());
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() {
    }
}
