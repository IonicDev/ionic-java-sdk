package com.ionic.sdk.core.codec;

/**
 * Extract a common interface from the different classes used to create textual representations of raw byte arrays.
 * Analogous to ISCryptoBytesTranscoder in "$IONIC_REPO_ROOT/IonicAgents/SDK/ISAgentSDK/ISCryptoLib/ISCryptoTypes.h".
 */
public interface BytesTranscoder {

    /**
     * Transform input into a textual representation.
     *
     * @param bytes byte array representation of data to be transformed
     * @return the encoded string
     */
    String encode(byte[] bytes);


    /**
     * Transform input into its raw byte array representation.
     *
     * @param string text representation of data to be transformed
     * @return the decoded byte array
     */
    byte[] decode(String string);
}
