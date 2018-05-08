package com.ionic.sdk.core.codec;

import com.ionic.sdk.core.vm.Version;

/**
 * Interface class used to abstract away the creation of BytesTranscoder objects.
 * <p>
 * Package "javax.xml.bind" is available in JRE 7, 8.
 * <p>
 * Class "java.util.Base64" is available in JRE 8, 9.
 */
public final class Transcoder {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Transcoder() {
    }

    /**
     * @return a new transcoder for handling base64 data conversions
     */
    public static BytesTranscoder base64() {
        return FACTORY.base64();
    }

    /**
     * @return a new transcoder for handling hexadecimal data conversions
     */
    public static BytesTranscoder hex() {
        return FACTORY.hex();
    }

    /**
     * @return a new transcoder for handling UTF-8 data conversions
     */
    public static BytesTranscoder utf8() {
        return new UTF8();
    }

    /**
     * A TranscoderFactory appropriate for the running JRE version.
     */
    private static final TranscoderFactory FACTORY = getFactory();

    /**
     * At application startup, this method will be called by the above static initializer.  The running JRE
     * determines which BytesTranscoder factory will be instantiated.
     * <p>
     * We are purposefully wrapping the (checked) ReflectiveOperationException with an unchecked exception, as
     * the SDK is dead in the water without a BytesTranscoder factory implementation.
     *
     * @return a TranscoderFactory appropriate for the running JRE version
     * throws IllegalStateException if the factory object cannot be instantiated
     */
    private static TranscoderFactory getFactory() {
        final String className = (Version.isJava7() ? IMPL_JRE7 : IMPL_JRE8);
        try {
            return (TranscoderFactory) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The padding character used to normalize base64 encoded strings to a multiple of the base64 block size.
     */
    public static final String BASE64_PAD = "=";

    /**
     * Implementation class for use in JRE 7.
     */
    private static final String IMPL_JRE7 = "com.ionic.sdk.core.codec7.TranscoderFactory7";

    /**
     * Implementation class for use in JRE 8+.
     */
    private static final String IMPL_JRE8 = "com.ionic.sdk.core.codec8.TranscoderFactory8";
}
