package com.ionic.sdk.core.zip;

import com.ionic.sdk.core.io.Stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

/**
 * Utility class, used to apply compression codec transforms to arbitrary data.
 */
public final class Flate {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Flate() {
    }

    /**
     * Inflate bytes previously deflated using "FLATE" transform.
     *
     * @param bytesDeflate the input bytes, which are deflated
     * @return the original bytes
     * @throws IOException on stream failure
     */
    public static byte[] inflate(final byte[] bytesDeflate) throws IOException {
        final InflaterInputStream is = new InflaterInputStream(new ByteArrayInputStream(bytesDeflate));
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        Stream.transmit(is, os);
        os.flush();
        return os.toByteArray();
    }
}
