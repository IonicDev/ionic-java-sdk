package com.ionic.sdk.json;

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

/**
 * Utility methods for serialization / deserialization of json messages using the javax.json library.
 */
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
        try (JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(jsonBytes))) {
            return jsonReader.readObject();
        } catch (JsonException e) {
            throw new IonicException(SdkError.ISAGENT_PARSEFAILED, e);
        }
    }

    /**
     * A helper function that converts a string into a JsonObject.
     *
     * @param jsonString a string that can be parsed into a JsonObject
     * @return the JsonObject representation of the input string
     * @throws IonicException on failure parsing the input json
     */
    public static JsonObject readObject(final String jsonString) throws IonicException {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            return jsonReader.readObject();
        } catch (JsonException e) {
            throw new IonicException(SdkError.ISAGENT_PARSEFAILED, e);
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
        final JsonWriterFactory writerFactory = javax.json.Json.createWriterFactory(config);
        try (final JsonWriter writer = writerFactory.createWriter(os)) {
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
        final JsonWriterFactory writerFactory = javax.json.Json.createWriterFactory(config);
        try (final JsonWriter writer = writerFactory.createWriter(os)) {
            writer.writeArray(jsonArray);
        }
        return Transcoder.utf8().encode(os.toByteArray());
    }
}
