package com.ionic.sdk.core.io;

import java.util.Arrays;

/**
 * Utility class to scan a byte stream for a delimiter token.
 */
public final class StreamTokenFinder {

    /**
     * The token to search for in the stream.
     */
    private final byte[] token;

    /**
     * The current state of the match.
     */
    private int matchIndex;

    /**
     * Constructor.
     *
     * @param token the token to search for in the stream
     */
    public StreamTokenFinder(final byte[] token) {
        this.token = Arrays.copyOf(token, token.length);
        this.matchIndex = 0;
    }

    /**
     * Supply another byte from the incoming stream to this state machine.
     *
     * @param b the cursor byte from the input stream being processed
     * @return true iff the byte is the final byte of the delmiter token
     */
    public boolean match(final byte b) {
        final boolean matchByte = (b == token[matchIndex]);
        matchIndex = (matchByte ? (matchIndex + 1) : 0);
        return (matchIndex == token.length);
    }

    /**
     * Reset this object to search for the next instance in the byte stream of the delimiter token.
     */
    public void reset() {
        this.matchIndex = 0;
    }
}
