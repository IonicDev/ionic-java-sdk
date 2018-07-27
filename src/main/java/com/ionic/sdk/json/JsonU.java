package com.ionic.sdk.json;

import com.ionic.sdk.error.IonicException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Utility methods for working with objects from the javax.json library.
 */
public final class JsonU {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private JsonU() {
    }

    /**
     * This is how we can serialize javax.json objects.
     *
     * @param jsonObject the container for the source json data
     * @param pretty     a flag indicating whether the json should be emitted in a human-readable format
     * @return a human readable representation of the source data
     */
    public static String toJson(final JsonObject jsonObject, final boolean pretty) {
        return JsonIO.write(jsonObject, pretty);
    }

    /**
     * Utility function to read the String value associated with the name within a json object.
     *
     * @param jsonObject the object from which to get the string
     * @param name       the name of the string
     * @return the json value associated with the name, or null if not present
     */
    public static String getString(final JsonObject jsonObject, final String name) {
        return JsonSource.getString(jsonObject, name);
    }

    /**
     * Utility function to read the int value associated with the name within a json object.
     *
     * @param jsonObject the object from which to get the int
     * @param name       the name of the int
     * @return the json value associated with the name, or 0 if not present
     */
    public static int getInt(final JsonObject jsonObject, final String name) {
        return JsonSource.getInt(jsonObject, name);
    }

    /**
     * A helper function that converts a string into a JsonObject.
     *
     * @param jsonString a string that can be parsed into a JsonObject
     * @return the JsonObject representation of the input string
     * @throws IonicException on failure parsing the input json
     */
    public static JsonObject getJsonObject(final String jsonString) throws IonicException {
        return JsonIO.readObject(jsonString);
    }

    /**
     * A helper function that converts a string into a JsonArray.
     *
     * @param jsonString a string that can be parsed into a JsonArray
     * @return the JsonArray representation of the input string
     * @throws IonicException on failure parsing the input json
     */
    public static JsonArray getJsonArray(final String jsonString) throws IonicException {
        return JsonIO.readArray(jsonString);
    }

    /**
     * A helper function that guards against the insertion of a null value (which is not allowed) into a JsonObject.
     *
     * @param objectBuilder the json container into which the name value pair is to be added
     * @param name          the attribute name
     * @param value         the attribute value
     */
    public static void addNotNull(final JsonObjectBuilder objectBuilder, final String name, final String value) {
        JsonTarget.addNotNull(objectBuilder, name, value);
    }
}
