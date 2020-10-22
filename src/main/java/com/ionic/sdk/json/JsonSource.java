package com.ionic.sdk.json;

import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class for requesting data from objects in the "javax.json" package hierarchy.
 */
@InternalUseOnly
public final class JsonSource {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private JsonSource() {
    }

    /**
     * Obtain a reference to the specified JsonString.
     *
     * @param jsonValue the value of the item
     * @param message the error message if this fails.
     * @return the input value, cast to a JsonString
     * @throws IonicException if the value is not of the expected JsonString type
     */
    public static JsonString toJsonString(final JsonValue jsonValue, final String message) throws IonicException {
        SdkData.checkTrue(jsonValue != null, SdkError.ISAGENT_MISSINGVALUE, message);
        SdkData.checkTrue(jsonValue instanceof JsonString, SdkError.ISAGENT_INVALIDVALUE, message);
        return (JsonString) jsonValue;
    }

    /**
     * Obtain a reference to the specified JsonObject.
     *
     * @param jsonValue the value of the item
     * @param message the error message if this fails.
     * @return the input value, cast to a JsonObject
     * @throws IonicException if the value is not of the expected type
     */
    public static JsonObject toJsonObject(final JsonValue jsonValue, final String message) throws IonicException {
        SdkData.checkTrue(jsonValue != null, SdkError.ISAGENT_MISSINGVALUE, message);
        SdkData.checkTrue(jsonValue instanceof JsonObject, SdkError.ISAGENT_INVALIDVALUE, message);
        return (JsonObject) jsonValue;
    }

    /**
     * Obtain a reference to the specified JsonArray.
     *
     * @param jsonValue the value of the item
     * @param message the error message if this fails.
     * @return the input value, cast to a JsonArray
     * @throws IonicException if the value is not of the expected type
     */
    public static JsonArray toJsonArray(final JsonValue jsonValue, final String message) throws IonicException {
        SdkData.checkTrue(jsonValue != null, SdkError.ISAGENT_MISSINGVALUE, message);
        SdkData.checkTrue(jsonValue instanceof JsonArray, SdkError.ISAGENT_INVALIDVALUE, message);
        return (JsonArray) jsonValue;
    }

    /**
     * Utility function to read the String value associated with a json value.
     *
     * @param jsonValue the value of the item
     * @return the String representation of the value
     */
    public static String toString(final JsonValue jsonValue) {
        final String value;
        if (jsonValue == null) {
            value = null;
        } else if (jsonValue instanceof JsonString) {
            value = ((JsonString) jsonValue).getString();
        } else {
            value = jsonValue.toString();
        }
        return value;
    }

    /**
     * Utility function to read the int value from json value.
     *
     * @param jsonValue the value from which to retrieve the int
     * @return the json int value, or 0 if not a number
     */
    public static int toInt(final JsonValue jsonValue) {
        return ((jsonValue instanceof JsonNumber) ? ((JsonNumber) jsonValue).intValue() : 0);
    }

    /**
     * Utility function to read the long value from json value.
     *
     * @param jsonValue the value from which to retrieve the long
     * @return the json long value, or 0 if not a number
     */
    public static long toLong(final JsonValue jsonValue) {
        return ((jsonValue instanceof JsonNumber) ? ((JsonNumber) jsonValue).longValueExact() : 0);
    }

    /**
     * Get a iterator reference allowing traversal of the content of the input json object.
     *
     * @param jsonObject The input object
     * @return an iterator with which the input object content may be traversed
     * @throws IonicException if the object is not of the expected type
     */
    public static Iterator<Map.Entry<String, JsonValue>> getIterator(
            final JsonObject jsonObject) throws IonicException {
        SdkData.checkNotNull(jsonObject, JsonObject.class.getSimpleName());
        return jsonObject.entrySet().iterator();
    }

    /**
     * Get a iterator reference allowing traversal of the content of the input json object.
     *
     * @param jsonObject The input object
     * @return an iterator with which the input object content may be traversed if non-null; otherwise
     * an empty iterator
     */
    public static Iterator<Map.Entry<String, JsonValue>> getIteratorNullable(
            final JsonObject jsonObject) {
        final Iterator<Map.Entry<String, JsonValue>> emptyIterator = Collections.emptyIterator();
        return (jsonObject == null) ? emptyIterator : jsonObject.entrySet().iterator();
    }

    /**
     * Get a iterator reference allowing traversal of the content of the input json array.
     *
     * @param jsonArray The input object
     * @return an iterator with which the input object content may be traversed
     * @throws IonicException if the object is not of the expected type
     */
    public static Iterator<JsonValue> getIterator(final JsonArray jsonArray) throws IonicException {
        SdkData.checkNotNull(jsonArray, JsonArray.class.getSimpleName());
        return jsonArray.iterator();
    }

    /**
     * Get the "javax.json" data type of the specified JsonObject descendant of the parameter object.
     *
     * @param jsonObject the parent of the requested child item
     * @param name       the name of the requested child item
     * @return the data type of the specified child node if it exists; otherwise null
     */
    public static JsonValue.ValueType getValueType(final JsonObject jsonObject, final String name) {
        final JsonValue jsonValue = jsonObject.get(name);
        return ((jsonValue == null) ? null : jsonValue.getValueType());
    }

    /**
     * Get the "javax.json" data type of the specified JsonValue.
     *
     * @param jsonValue the item to query for type
     * @return the data type of the specified child node if it exists; otherwise null
     */
    public static JsonValue.ValueType getValueType(final JsonValue jsonValue) {
        return ((jsonValue == null) ? null : jsonValue.getValueType());
    }

    /**
     * Obtain a reference to the specified JsonObject descendant of the parameter object, or null if the child
     * is not present.
     *
     * @param jsonObject the parent of the requested child item
     * @param name       the name of the requested child item
     * @return the JsonObject child, or null if the child is not present
     * @throws IonicException if the child is present, and is not of the expected type
     */
    public static JsonObject getJsonObjectNullable(
            final JsonObject jsonObject, final String name) throws IonicException {
        final JsonValue jsonValue = jsonObject.get(name);
        if (jsonValue != null) {
            SdkData.checkTrue(jsonValue instanceof JsonObject, SdkError.ISAGENT_INVALIDVALUE, name);
        }
        return (JsonObject) jsonValue;
    }

    /**
     * Obtain a reference to the specified JsonObject descendant of the parameter object.
     *
     * @param jsonObject the parent of the requested child item
     * @param name       the name of the requested child item
     * @return the JsonObject child
     * @throws IonicException if the child is not present, or is not of the expected type
     */
    public static JsonObject getJsonObject(final JsonObject jsonObject, final String name) throws IonicException {
        final JsonValue jsonValue = jsonObject.get(name);
        SdkData.checkTrue(jsonValue != null, SdkError.ISAGENT_MISSINGVALUE, name);
        SdkData.checkTrue(jsonValue instanceof JsonObject, SdkError.ISAGENT_INVALIDVALUE, name);
        return (JsonObject) jsonValue;
    }

    /**
     * Obtain a reference to the specified JsonArray descendant of the parameter object.
     *
     * @param jsonObject the parent of the requested child item
     * @param name       the name of the requested child item
     * @return the JsonArray child
     * @throws IonicException if the child is not present, or is not of the expected type
     */
    public static JsonArray getJsonArray(final JsonObject jsonObject, final String name) throws IonicException {
        final JsonValue jsonValue = jsonObject.get(name);
        SdkData.checkTrue(jsonValue != null, SdkError.ISAGENT_MISSINGVALUE, name);
        SdkData.checkTrue(jsonValue instanceof JsonArray, SdkError.ISAGENT_INVALIDVALUE, name);
        return (JsonArray) jsonValue;
    }

    /**
     * Obtain a reference to the specified JsonArray descendant of the parameter object.
     *
     * @param jsonObject the parent of the requested child item
     * @param name       the name of the requested child item
     * @return the JsonArray child
     * @throws IonicException if the child is not present, or is not of the expected type
     */
    public static JsonArray getJsonArrayNullable(
            final JsonObject jsonObject, final String name) throws IonicException {
        final JsonValue jsonValue = jsonObject.get(name);
        if (jsonValue != null) {
            SdkData.checkTrue(jsonValue instanceof JsonArray, SdkError.ISAGENT_INVALIDVALUE, name);
        }
        return (JsonArray) jsonValue;
    }

    /**
     * Obtain a reference to the specified JsonString descendant of the parameter object.
     *
     * @param jsonObject the object from which to get the string
     * @param name       the name of the string value
     * @return the json value associated with the name, or null if not present
     * @throws IonicException if the child is not present, or is not of the expected type
     */
    public static JsonString getJsonString(final JsonObject jsonObject, final String name) throws IonicException {
        final JsonValue jsonValue = jsonObject.get(name);
        SdkData.checkTrue(jsonValue != null, SdkError.ISAGENT_MISSINGVALUE, name);
        SdkData.checkTrue(jsonValue instanceof JsonString, SdkError.ISAGENT_INVALIDVALUE, name);
        return (JsonString) jsonValue;
    }

    /**
     * Obtain a reference to the specified JsonNumber descendant of the parameter object.
     *
     * @param jsonObject the object from which to get the number
     * @param name       the name of the number value
     * @return the json value associated with the name, or null if not present
     * @throws IonicException if the child is not present, or is not of the expected type
     */
    public static JsonNumber getJsonNumber(final JsonObject jsonObject, final String name) throws IonicException {
        final JsonValue jsonValue = jsonObject.get(name);
        SdkData.checkTrue(jsonValue != null, SdkError.ISAGENT_MISSINGVALUE, name);
        SdkData.checkTrue(jsonValue instanceof JsonNumber, SdkError.ISAGENT_INVALIDVALUE, name);
        return (JsonNumber) jsonValue;
    }

    /**
     * Utility function to read the String value associated with the name within a json object.
     *
     * @param jsonObject the object from which to get the string
     * @param name       the name of the string value
     * @return the json value associated with the name, or null if not present
     */
    public static String getString(final JsonObject jsonObject, final String name) {
        final JsonValue jsonValue = jsonObject.get(name);
        return ((jsonValue instanceof JsonString) ? ((JsonString) jsonValue).getString() : null);
    }

    /**
     * Utility function to read the boolean value associated with the name within a json object.
     *
     * @param jsonObject the object from which to get the boolean
     * @param name       the name of the boolean value
     * @return the boolean value associated with the name, or "false" if not present
     */
    public static boolean getBoolean(final JsonObject jsonObject, final String name) {
        return jsonObject.getBoolean(name, false);
    }

    /**
     * Utility function to read the int value associated with the name within a json object.
     *
     * @param jsonObject the object from which to get the int
     * @param name       the name of the int value
     * @return the json value associated with the name, or 0 if not present
     */
    public static int getInt(final JsonObject jsonObject, final String name) {
        final JsonValue jsonValue = jsonObject.get(name);
        return ((jsonValue instanceof JsonNumber) ? ((JsonNumber) jsonValue).intValue() : 0);
    }

    /**
     * Utility function to read the long value associated with the name within a json object.
     *
     * @param jsonObject the object from which to get the long
     * @param name       the name of the long value
     * @return the json value associated with the name, or 0 if not present
     */
    public static long getLong(final JsonObject jsonObject, final String name) {
        final JsonValue jsonValue = jsonObject.get(name);
        return ((jsonValue instanceof JsonNumber) ? ((JsonNumber) jsonValue).longValueExact() : 0);
    }

    /**
     * Utility function to test size (number of name/value mappings) of a {@link JsonObject}.
     *
     * @param jsonObject the value to test
     * @param size       the expected size of the object
     * @return true, iff the input contains the expected number of mappings
     * @throws IonicException if the child is null
     */
    public static boolean isSize(final JsonObject jsonObject, final int size) throws IonicException {
        SdkData.checkNotNull(jsonObject, JsonObject.class.getName());
        return (jsonObject.size() == size);
    }

    /**
     * Utility function to test size (number of values) of a {@link JsonArray}.
     *
     * @param jsonArray the value to test
     * @param size      the expected size of the array
     * @return true, iff the input contains the expected number of values
     * @throws IonicException if the child is null
     */
    public static boolean isSize(final JsonArray jsonArray, final int size) throws IonicException {
        SdkData.checkNotNull(jsonArray, JsonArray.class.getName());
        return (jsonArray.size() == size);
    }
}
