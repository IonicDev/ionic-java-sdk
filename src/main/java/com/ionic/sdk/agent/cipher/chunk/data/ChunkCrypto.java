package com.ionic.sdk.agent.cipher.chunk.data;

import com.ionic.sdk.agent.cipher.chunk.ChunkCipherAuto;

/**
 * Utility class for evaluating strings to determine the operant Ionic chunk format.
 */
public final class ChunkCrypto {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private ChunkCrypto() {
    }

    /**
     * Determines if a chunk is Ionic protected and various pieces of information about the chunk.
     *
     * @param inputString The input to be tested against known Ionic chunk cipher formats.
     * @return The chunk information object for the specified input string.
     */
    public static ChunkCryptoChunkInfo getChunkInfo(final String inputString) {
        final ChunkCipherAuto chunkCipher = new ChunkCipherAuto(null);
        return chunkCipher.getChunkInfo(inputString);
    }
}
