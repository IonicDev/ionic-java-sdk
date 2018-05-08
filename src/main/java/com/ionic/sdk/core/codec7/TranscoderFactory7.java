package com.ionic.sdk.core.codec7;

import com.ionic.sdk.core.codec.BytesTranscoder;
import com.ionic.sdk.core.codec.TranscoderFactory;

/**
 * Implementation used to abstract away the creation of BytesTranscoder objects.
 * <p>
 * Package "javax.xml.bind" is available in JRE 7, 8.
 * <p>
 * Class "java.util.Base64" is available in JRE 8, 9.
 */
public class TranscoderFactory7 extends TranscoderFactory {

    /**
     * @return a new transcoder for handling base64 data conversions
     */
    @Override
    public final BytesTranscoder base64() {
        return new Base64();
    }

    /**
     * @return a new transcoder for handling hexadecimal data conversions
     */
    @Override
    public final BytesTranscoder hex() {
        return new Hex();
    }
}
