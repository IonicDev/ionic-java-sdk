package com.ionic.sdk.httpclient;

import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.lang.reflect.Constructor;

/**
 * Encapsulate the assembly of a new http client, given a string specifying the requested type.
 */
@InternalUseOnly
public final class HttpClientFactory {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private HttpClientFactory() {
    }

    /**
     * Create a new http client for use in making key services requests.
     *
     * @param agentConfig the configuration settings associated with this agent instance
     * @param protocol    the protocol to be checked for proxy configuration (e.g. "http", "https")
     * @return an HttpClient of the requested type
     * @throws IonicException on specification of a class name which cannot be instantiated as an HttpClient
     */
    public static HttpClient create(final AgentConfig agentConfig, final String protocol) throws IonicException {
        SdkData.checkNotNull(agentConfig, AgentConfig.class.getName());
        return Value.isEmpty(agentConfig.getHttpImpl())
                ? new HttpClientDefault(agentConfig, protocol)
                : createReflect(agentConfig.getHttpImpl(), agentConfig, protocol);
    }

    /**
     * Create a new instance of the HttpClient class specified by the configuration.
     *
     * @param className   the name of the class to be instantiated
     * @param agentConfig the configuration settings associated with this agent instance
     * @param protocol    the protocol to be checked for proxy configuration (e.g. "http", "https")
     * @return an instance of the HttpClient-derived class
     * @throws IonicException on specification of a class name which cannot be instantiated as an HttpClient
     */
    private static HttpClient createReflect(final String className, final AgentConfig agentConfig,
                                            final String protocol) throws IonicException {
        try {
            final Class<?> c = Class.forName(className);
            final Constructor<?> ctor = c.getConstructor(AgentConfig.class, String.class);
            final Object object = ctor.newInstance(agentConfig, protocol);
            if (object instanceof HttpClient) {
                return (HttpClient) object;
            } else {
                throw new IonicException(SdkError.ISAGENT_BADCONFIG, object.getClass().getName());
            }
        } catch (ReflectiveOperationException e) {
            throw new IonicException(SdkError.ISAGENT_BADCONFIG, e);
        }
    }
}
