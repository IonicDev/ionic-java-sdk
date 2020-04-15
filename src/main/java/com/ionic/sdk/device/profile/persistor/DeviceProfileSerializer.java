package com.ionic.sdk.device.profile.persistor;

import com.ionic.sdk.core.codec.Transcoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulate the data contained in an Ionic Secure Enrollment Profile.
 *
 * The base implementation contains JSON data describing the enrollments associated with a device.  Some derivatives
 * specify an additional JSON header describing the content of the file.  This class intends to facilitate
 * manipulation of both serialization formats in a consistent way.
 */
public final class DeviceProfileSerializer {

    /**
     * The (optional) JSON encoded header describing the content of the file body.
     */
    private final String header;

    /**
     * The data of the serialized device profile.  This may be encrypted with a given cipher.
     */
    private final byte[] body;

    /**
     * Constructor.
     *
     * @param content the data contained in the specified file
     */
    public DeviceProfileSerializer(final byte[] content) {
        final String text = Transcoder.utf8().encode(content);
        final Matcher matcher = Pattern.compile(HEADER_JSON_DELIMITER).matcher(text);
        if (matcher.find()) {
            this.header = text.substring(0, matcher.start());
            this.body = Arrays.copyOfRange(content, matcher.end(), content.length);
        } else {
            this.header = null;
            this.body = Arrays.copyOf(content, content.length);
        }
    }

    /**
     * @return the JSON encoded header string, if present, which describes the type of the body
     */
    public String getHeader() {
        return header;
    }

    /**
     * @return the data of the serialized device profile
     */
    public byte[] getBody() {
        return Arrays.copyOf(body, body.length);
    }

    /**
     * Serialize the content of this object into a byte stream, suitable for persisting to a filesystem.
     *
     * @return the bytes to be written
     * @throws IOException on failure to encode the object data into UTF-8
     */
    public byte[] serialize() throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(Transcoder.utf8().decode(header));
        os.write(Transcoder.utf8().decode(HEADER_JSON_DELIMITER));
        os.write(body);
        return os.toByteArray();
    }

    /**
     * The separator token used to separate JSON header from the subsequent payload.
     */
    public static final String HEADER_JSON_DELIMITER = "\r\n\r\n";
}
