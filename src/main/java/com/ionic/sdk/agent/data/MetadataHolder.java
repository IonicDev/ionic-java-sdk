package com.ionic.sdk.agent.data;

/**
 * A base class used by several other classes that wish to store metadata.
 */
public class MetadataHolder {

    /**
     * The map of metadata attributes.
     */
    private MetadataMap metadata;

    /**
     * Constructor.
     */
    public MetadataHolder() {
        this.metadata = new MetadataMap();
    }

    /**
     * @return the metadata attributes
     */
    public final MetadataMap getMetadata() {
        return metadata;
    }

    /**
     * Update a single metadata attribute.
     *
     * @param name  the attribute name
     * @param value the attribute value
     */
    public final void setMetadata(final String name, final String value) {
        metadata.put(name, value);
    }

    /**
     * Update the metadata attributes.
     *
     * @param metadata the attributes which should replace the existing attributes
     */
    public final void setMetadata(final MetadataMap metadata) {
        this.metadata = metadata;
    }

    /**
     * Lookup a single value associated with the requested name.  If not present, the default value should be returned.
     *
     * @param name         the attribute name
     * @param defaultValue the value to return, if the attribute is not currently set
     * @return the value associated with the requested name, if present; otherwise the specified default value
     */
    public final String getMetadata(final String name, final String defaultValue) {
        return (metadata.containsKey(name) ? metadata.get(name) : defaultValue);
    }

    /**
     * Lookup a single value associated with the requested name.
     *
     * @param name the attribute name
     * @return the value associated with the requested name, if present; otherwise null
     */
    public final String getMetadata(final String name) {
        return metadata.get(name);
    }
}
