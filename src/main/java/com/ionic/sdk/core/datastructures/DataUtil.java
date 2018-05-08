package com.ionic.sdk.core.datastructures;

import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.error.AgentErrorModuleConstants;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.json.JsonU;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Map;
import java.util.Set;

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
            final JsonObject jsonObject = JsonU.getJsonObject(data);
            final Set<Map.Entry<String, JsonValue>> entries = jsonObject.entrySet();
            for (Map.Entry<String, JsonValue> entry : entries) {
                final String key = entry.getKey();
                final JsonValue value = entry.getValue();
                if (value instanceof JsonString) {
                    metadataMap.put(key, ((JsonString) value).getString());
                } else {
                    metadataMap.put(key, value.toString());
                }
            }
        } catch (JsonException e) {
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_PARSEFAILED.value(), e);
        }
        return metadataMap;
    }
}
