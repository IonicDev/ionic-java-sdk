package com.ionic.sdk.agent.config;

import com.ionic.sdk.agent.service.IDC;

import java.util.Properties;

/**
 * Configuration object used by Agent. This object is used to configure an instance of Agent.
 */
public class AgentConfig {

    /**
     * Arbitrary name-value pairs associated with this AgentConfig.
     */
    private final Properties properties;

    /**
     * The user agent to communicate to the server on a server request.
     */
    private String userAgent;

    /**
     * The name of the HTTP client implementation that should be used when communicating with the Ionic server.
     */
    private String httpImpl;

    /**
     * The number of seconds to wait after making a server request for a response.
     */
    private int httpTimeoutSecs;

    /**
     * The maximum number of HTTP redirects.
     */
    private int maxRedirects;

    /**
     * Get the path of file this config object was loaded from, if any.
     */
    private String originFile;

    /**
     * Initializes the object to be empty. All properties will be unset with exception to the following:
     * <ol>
     * <li>Maximum redirects default value is 2.</li>
     * <li>User-Agent default value is "Ionic Fusion Agent".</li>
     * </ol>
     */
    public AgentConfig() {
        this.properties = new Properties();
        clearInternal();
    }

    /**
     * Initializes the object with an existing AgentConfig object. For internal use only.
     *
     * @param agentConfig the existing config which should be copied
     */
    public AgentConfig(final AgentConfig agentConfig) {
        this();
        this.properties.putAll(agentConfig.properties);
        this.setUserAgent(agentConfig.getUserAgent());
        this.setHttpImpl(agentConfig.getHttpImpl());
        this.setHttpTimeoutSecs(agentConfig.getHttpTimeoutSecs());
        this.setMaxRedirects(agentConfig.getMaxRedirects());
        this.originFile = agentConfig.getOriginFile();
    }

    /**
     * Set all properties back to a default value. After calling clear(), the object will be equal to a newly
     * instantiated config object using the default constructor.
     */
    public final void clear() {
        clearInternal();
    }

    /**
     * Set all properties back to a default value. After calling clear(), the object will be equal to a newly
     * instantiated config object using the default constructor.
     */
    private void clearInternal() {
        this.properties.clear();
        this.userAgent = IDC.Metadata.USER_AGENT_DEFAULT;
        this.httpImpl = "";
        this.httpTimeoutSecs = HTTP_TIMEOUT_SECS_DEFAULT;
        this.maxRedirects = HTTP_REDIRECTS_DEFAULT;
        this.originFile = "";
    }

    /**
     * Initialize configuration values with those from another {@link AgentConfig} instance.
     *
     * @param agentConfig the existing config which should be copied
     */
    public final void initialize(final AgentConfig agentConfig) {
        clearInternal();
        this.properties.putAll(agentConfig.properties);
        this.setUserAgent(agentConfig.getUserAgent());
        this.setHttpImpl(agentConfig.getHttpImpl());
        this.setHttpTimeoutSecs(agentConfig.getHttpTimeoutSecs());
        this.setMaxRedirects(agentConfig.getMaxRedirects());
        this.originFile = agentConfig.getOriginFile();
    }

    /**
     * Determine if the configuration is in a valid state.
     *
     * @return True if the agent configuration is valid; false otherwise.
     */
    public final boolean isValid() {
        return !(userAgent == null || userAgent.isEmpty());
    }

    /**
     * Set the User-Agent header value to use for HTTP communication.
     *
     * @param userAgent The User-Agent header value.
     */
    public final void setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Get the User-Agent header value to use for HTTP communication.
     *
     * @return String representing the User-Agent header value.
     */
    public final String getUserAgent() {
        return this.userAgent;
    }

    /**
     * Set the HTTP implementation to use. This field is used to specify a non-default HTTP implementation. Only set
     * this field if you need an Agent object to use a custom HTTP implementation.
     *
     * @param httpImpl The name of the HTTP implementation to use.
     */
    public final void setHttpImpl(final String httpImpl) {
        this.httpImpl = httpImpl;
    }

    /**
     * Get the name of the HTTP implementation to use.
     *
     * @return Name of the HTTP implementation in use.
     */
    public final String getHttpImpl() {
        return this.httpImpl;
    }

    /**
     * Set the HTTP timeout.
     *
     * @param httpTimeoutSecs The HTTP timeout.
     */
    public final void setHttpTimeoutSecs(final int httpTimeoutSecs) {
        this.httpTimeoutSecs = httpTimeoutSecs;
    }

    /**
     * Get the HTTP timeout.
     *
     * @return Get the HTTP timeout.
     */
    public final int getHttpTimeoutSecs() {
        return httpTimeoutSecs;
    }

    /**
     * Set the maximum number of HTTP redirects.
     *
     * @param maxRedirects Maximum number of HTTP redirects.
     */
    public final void setMaxRedirects(final int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    /**
     * Get the maximum number of HTTP redirects.
     *
     * @return Maximum number of HTTP redirects.
     */
    public final int getMaxRedirects() {
        return this.maxRedirects;
    }

    /**
     * Set a configuration property string by name.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     */
    public final void setProperty(final String name, final String value) {
        properties.setProperty(name, value);
    }

    /**
     * Get a configuration property string by name.
     *
     * @param name         The parameter name.
     * @param defaultValue The default value to use, in case the parameter is not found.
     * @return The value of the named configuration property if it was found, or else the specified default value.
     */
    public final String getProperty(final String name, final String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    /**
     * Get a configuration property string by name.
     *
     * @param name The parameter name.
     * @return The value of the named configuration property.
     */
    public final String getProperty(final String name) {
        return properties.getProperty(name);
    }

    /**
     * Get a configuration property boolean by name. If the property value is "0", "false", or "no", then the boolean
     * value will be false. Otherwise, the boolean value will be true. The string comparison is case-insensitive.
     *
     * @param name         The parameter name.
     * @param defaultValue The default value to use, in case the parameter is not found or is empty.
     * @return Property value, as a boolean value.
     */
    public final boolean getPropertyBool(final String name, final boolean defaultValue) {
        final String value = properties.getProperty(name);
        if (value == null) {
            return defaultValue;
        } else {
            return !("0".equals(value) || "false".equals(value) || "no".equals(value));  // magic_string_ok
        }
    }

    /**
     * Get the path of file this config object was loaded from, if any.
     *
     * @return the path of file this config object was loaded from, if any
     */
    public final String getOriginFile() {
        return originFile;
    }

    /**
     * Default number of seconds to wait for a server response before timing out the connection.
     */
    private static final int HTTP_TIMEOUT_SECS_DEFAULT = 30;

    /**
     * Default number of redirects to follow when making an HTTP request.
     */
    private static final int HTTP_REDIRECTS_DEFAULT = 2;

    /**
     * Property names associated with {@link AgentConfig} settings.
     */
    public static class Key {

        /**
         * If set to true, all {@link com.ionic.sdk.device.profile.DeviceProfile} objects associated with the
         * relevant {@link com.ionic.sdk.agent.Agent} will be considered when choosing
         * a server on
         * a {@link com.ionic.sdk.agent.Agent#getKeys(com.ionic.sdk.agent.request.getkey.GetKeysRequest)} operation.
         * <p>
         * If unspecified, default is {@link Boolean#TRUE}.
         */
        public static final String AUTOSELECT_PROFILE = "autoselectprofile";
    }
}
