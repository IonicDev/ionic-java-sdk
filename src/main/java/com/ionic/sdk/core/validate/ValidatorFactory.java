package com.ionic.sdk.core.validate;

import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.lang.reflect.Constructor;

/**
 * Encapsulate the assembly of a new validator, given a string specifying the requested class name.
 */
public final class ValidatorFactory {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private ValidatorFactory() {
    }

    /**
     * Create a new validator for use in checking messages.
     *
     * @param agentConfig the configuration settings associated with this agent instance
     * @return a Validator of the requested type, if configured
     * @throws IonicException on specification of a class name which cannot be instantiated as a Validator
     */
    public static Validator create(final AgentConfig agentConfig) throws IonicException {
        SdkData.checkNotNull(agentConfig, AgentConfig.class.getName());
        final String validatorImpl = agentConfig.getProperty(Validator.class.getName());
        return Value.isEmpty(validatorImpl) ? null : createReflect(validatorImpl);
    }

    /**
     * Create a new instance of the HttpClient class specified by the configuration.
     *
     * @param className the name of the class to be instantiated
     * @return an instance of the Validator-derived class
     * @throws IonicException on specification of a class name which cannot be instantiated as a Validator
     */
    private static Validator createReflect(final String className) throws IonicException {
        try {
            final Class<?> c = Class.forName(className);
            final Constructor<?> ctor = c.getConstructor();
            final Object object = ctor.newInstance();
            if (object instanceof Validator) {
                return (Validator) object;
            } else {
                throw new IonicException(SdkError.ISAGENT_BADCONFIG, object.getClass().getName());
            }
        } catch (ReflectiveOperationException e) {
            throw new IonicException(SdkError.ISAGENT_BADCONFIG, e);
        }
    }
}
