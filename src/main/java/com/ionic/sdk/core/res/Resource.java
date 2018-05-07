package com.ionic.sdk.core.res;

import java.net.URL;

/**
 * Utilities to access classpath resources independent of the location of the resource.
 */
public final class Resource {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Resource() {
    }

    /**
     * Finds the resource with the given name.
     * @param name the resource name
     * @return the URL of the requested resource, or null if not found
     */
    public static URL resolve(final String name) {
        final Thread thread = Thread.currentThread();
        final ClassLoader classLoader = thread.getContextClassLoader();
        return classLoader.getResource(name);
    }
}
