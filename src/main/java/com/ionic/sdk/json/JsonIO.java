package com.ionic.sdk.json;

import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for serialization / deserialization of json messages using the javax.json library.
 */
@InternalUseOnly
public final class JsonIO {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private JsonIO() {
    }

    /**
     * A helper function that converts a byte array into a JsonObject.  The byte array is assumed to be a
     * string encoded using the UTF-8 encoding.
     *
     * @param jsonStream a byte stream that can be parsed into a JsonObject
     * @return the JsonObject representation of the input string
     * @throws IonicException on failure parsing the input json
     */
    public static JsonObject readObject(final InputStream jsonStream) throws IonicException {
        try (JsonReader jsonReader = Json.createReader(jsonStream)) {
            return jsonReader.readObject();
        } catch (JsonException e) {
            throw new IonicException(SdkError.ISAGENT_PARSEFAILED, e);
        }
    }

    /**
     * A helper function that converts a byte array into a JsonObject.  The byte array is assumed to be a
     * string encoded using the UTF-8 encoding.
     *
     * @param jsonBytes a byte array that can be parsed into a JsonObject
     * @return the JsonObject representation of the input string
     * @throws IonicException on failure parsing the input json
     */
    public static JsonObject readObject(final byte[] jsonBytes) throws IonicException {
        return readObjectInternal(jsonBytes, null);
    }

    /**
     * A helper function that converts a byte array into a JsonObject.  The byte array is assumed to be a
     * string encoded using the UTF-8 encoding.
     * <p>
     * If input parameter is null, then null is returned.
     *
     * @param jsonBytes a byte array that can be parsed into a JsonObject
     * @return the JsonObject representation of the input string if non-null
     * @throws IonicException on failure parsing the input json
     */
    public static JsonObject readObjectNotNull(final byte[] jsonBytes) throws IonicException {
        return (jsonBytes == null) ? null : readObjectInternal(jsonBytes, null);
    }

    /**
     * A helper function that converts a byte array into a JsonObject.  The byte array is assumed to be a
     * string encoded using the UTF-8 encoding.
     *
     * @param jsonBytes a byte array that can be parsed into a JsonObject
     * @param level     the level at which JsonException should be logged, if encountered
     * @return the JsonObject representation of the input string
     * @throws IonicException on failure parsing the input json
     */
    public static JsonObject readObject(final byte[] jsonBytes, final Level level) throws IonicException {
        return readObjectInternal(jsonBytes, level);
    }

    /**
     * A helper function that converts a byte array into a JsonObject.  The byte array is assumed to be a
     * string encoded using the UTF-8 encoding.
     *
     * @param jsonBytes a byte array that can be parsed into a JsonObject
     * @param level     the level at which JsonException should be logged, if encountered
     * @return the JsonObject representation of the input string
     * @throws IonicException on failure parsing the input json
     */
    private static JsonObject readObjectInternal(final byte[] jsonBytes, final Level level) throws IonicException {
        try (JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(jsonBytes))) {
            return jsonReader.readObject();
        } catch (JsonException e) {
            handleJsonException(Transcoder.utf8().encode(jsonBytes), level, e);
            throw new IonicException(SdkError.ISAGENT_PARSEFAILED, e);
        }
    }

    /**
     * A helper function that converts a string into a JsonObject.
     *
     * @param jsonString a string that can be parsed into a JsonObject
     * @param errorCode  the error code to return on parse failure
     * @return the JsonObject representation of the input string
     * @throws IonicException on failure parsing the input json
     */
    public static JsonObject readObject(final String jsonString, final int errorCode) throws IonicException {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            return jsonReader.readObject();
        } catch (JsonException e) {
            throw new IonicException(errorCode, e);
        }
    }

    /**
     * A helper function that converts a string into a JsonArray.
     *
     * @param jsonString a string that can be parsed into a JsonArray
     * @return the JsonArray representation of the input string
     * @throws IonicException on failure parsing the input json
     */
    public static JsonArray readArray(final String jsonString) throws IonicException {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            return jsonReader.readArray();
        } catch (JsonException e) {
            throw new IonicException(SdkError.ISAGENT_PARSEFAILED, e);
        }
    }

    /**
     * On <code>JsonException</code>, conditionally log exception detail.
     *
     * @param message text to include in log message
     * @param level   the level at which JsonException should be logged
     * @param e       exception to be wrapped
     */
    private static void handleJsonException(final String message, final Level level, final JsonException e) {
        if (level != null) {
            Logger.getLogger(JsonIO.class.getName()).log(level, message, e);
        }
    }

    /**
     * This is how we can serialize javax.json objects.
     *
     * @param jsonObject the container for the source json data
     * @param pretty     a flag indicating whether the json should be emitted in a human-readable format
     * @return a human readable representation of the source data
     */
    public static String write(final JsonObject jsonObject, final boolean pretty) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final Map<String, Boolean> config = new TreeMap<String, Boolean>();
        if (pretty) {
            config.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
        }
        final JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        try (JsonWriter writer = writerFactory.createWriter(os)) {
            writer.writeObject(jsonObject);
        }
        return Transcoder.utf8().encode(os.toByteArray());
    }

    /**
     * This is how we can serialize javax.json objects.
     *
     * @param jsonArray the container for the source json data
     * @param pretty    a flag indicating whether the json should be emitted in a human-readable format
     * @return a human readable representation of the source data
     */
    public static String write(final JsonArray jsonArray, final boolean pretty) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final Map<String, Boolean> config = new TreeMap<String, Boolean>();
        if (pretty) {
            config.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
        }
        final JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        try (JsonWriter writer = writerFactory.createWriter(os)) {
            writer.writeArray(jsonArray);
        }
        return Transcoder.utf8().encode(os.toByteArray());
    }
}
