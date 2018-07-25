package com.ionic.sdk.json;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.util.Collection;

/**
 * Utility class for putting data into objects in the "javax.json" package hierarchy.
 */
public final class JsonTarget {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private JsonTarget() {
    }

    /**
     * A helper function that guards against the insertion of a null value (which is not allowed) into a JsonObject.
     *
     * @param objectBuilder the json container into which the name value pair is to be added
     * @param name          the attribute name
     * @param value         the attribute value
     */
    public static void addNotNull(final JsonObjectBuilder objectBuilder, final String name, final JsonValue value) {
        if (value != null) {
            objectBuilder.add(name, value);
        }
    }

    /**
     * A helper function that guards against the insertion of a null value (which is not allowed) into a JsonObject.
     *
     * @param objectBuilder the json container into which the name value pair is to be added
     * @param name          the attribute name
     * @param value         the attribute value
     */
    public static void addNotNull(final JsonObjectBuilder objectBuilder, final String name, final String value) {
        if (value != null) {
            objectBuilder.add(name, value);
        }
    }

    /**
     * A helper function that encapsulates an attribute add into a JsonObject.
     *
     * @param objectBuilder the json container into which the name value pair is to be added
     * @param name          the attribute name
     * @param value         the attribute value
     */
    public static void add(final JsonObjectBuilder objectBuilder, final String name, final long value) {
        objectBuilder.add(name, value);
    }

    /**
     * A helper function that encapsulates an attribute add into a JsonObject.
     *
     * @param objectBuilder the json container into which the name value pair is to be added
     * @param name          the attribute name
     * @param value         the attribute value
     */
    public static void add(final JsonObjectBuilder objectBuilder, final String name, final boolean value) {
        objectBuilder.add(name, value);
    }

    /**
     * A helper function that encapsulates an attribute add into a JsonObject.
     *
     * @param objectBuilder the json container into which the name value pair is to be added
     * @param name          the attribute name
     * @param value         the attribute value
     */
    public static void add(final JsonObjectBuilder objectBuilder, final String name, final JsonArray value) {
        objectBuilder.add(name, value);
    }

    /**
     * A helper function that encapsulates an attribute add into a JsonObject.
     *
     * @param objectBuilder the json container into which the name value pair is to be added
     * @param name          the attribute name
     * @param value         the attribute value
     */
    public static void add(final JsonObjectBuilder objectBuilder, final String name, final JsonObject value) {
        objectBuilder.add(name, value);
    }

    /**
     * A helper function that guards against the insertion of a null value (which is not allowed) into a JsonArray.
     *
     * @param arrayBuilder the json container into which the name value pair is to be added
     * @param value        the item which should be added to the array
     */
    public static void addNotNull(final JsonArrayBuilder arrayBuilder, final JsonValue value) {
        if (value != null) {
            arrayBuilder.add(value);
        }
    }

    /**
     * A helper function that guards against the insertion of a null value (which is not allowed) into a JsonArray.
     *
     * @param arrayBuilder the json container into which the name value pair is to be added
     * @param value        the item which should be added to the array
     */
    public static void addNotNull(final JsonArrayBuilder arrayBuilder, final String value) {
        if (value != null) {
            arrayBuilder.add(value);
        }
    }

    /**
     * Assemble a JsonArray from the method input.
     *
     * @param items the tokens that should be incorporated into the output
     * @return a {@link JsonArray} containing the inputted items
     */
    public static JsonArray toJsonArray(final Collection<String> items) {
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String item : items) {
            jsonArrayBuilder.add(item);
        }
        return jsonArrayBuilder.build();
    }
}
