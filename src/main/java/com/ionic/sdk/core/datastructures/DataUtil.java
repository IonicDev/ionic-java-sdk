package com.ionic.sdk.core.datastructures;

import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Iterator;
import java.util.Map;

/**
 * Utilities for managing data exchanged with IDC.
 */
public final class DataUtil {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private DataUtil() {
    }

    /**
     * Convert parameter json data into a map object, for ease of access to the top level content.
     * <p>
     * This function does not recurse into multi-level json, but instead operates only at the top level of the
     * parameter object.  If you wish to decompose a multi-level json structure, use of a proper json parser
     * (such as the one available in javax.json) is recommended.
     *
     * @param data a json string containing name/value pairs
     * @return a map representation of the parameter data
     * @throws IonicException on json parsing error (input cannot be serialized as JsonObject)
     */
    public static MetadataMap toMetadataMap(final String data) throws IonicException {
        final MetadataMap metadataMap = new MetadataMap();
        try {
            final JsonObject jsonObject = JsonIO.readObject(data);
            final Iterator<Map.Entry<String, JsonValue>> iterator = JsonSource.getIterator(jsonObject);
            while (iterator.hasNext()) {
                final Map.Entry<String, JsonValue> entry = iterator.next();
                metadataMap.put(entry.getKey(), JsonSource.toString(entry.getValue()));
            }
        } catch (JsonException e) {
            throw new IonicException(SdkError.ISAGENT_PARSEFAILED, e);
        }
        return metadataMap;
    }
}
