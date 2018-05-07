package com.ionic.sdk.core.codec;

/**
 * Factory class used to abstract away the creation of BytesTranscoder objects.
 * <p>
 * Package "javax.xml.bind" is available in JRE 7, 8.
 * <p>
 * Class "java.util.Base64" is available in JRE 8, 9.
 */
public abstract class TranscoderFactory {

    /**
     * @return a new transcoder for handling base64 data conversions
     */
    public abstract BytesTranscoder base64();

    /**
     * @return a new transcoder for handling hexadecimal data conversions
     */
    public abstract BytesTranscoder hex();
}
