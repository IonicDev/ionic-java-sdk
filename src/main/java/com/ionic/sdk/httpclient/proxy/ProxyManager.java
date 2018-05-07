package com.ionic.sdk.httpclient.proxy;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

/**
 * Encapsulate operations to query process environment for proxy settings, and conditionally create Proxy object
 * used when opening a URLConnection.
 */
public final class ProxyManager {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private ProxyManager() {
    }

    /**
     * @param protocol the protocol to be checked for proxy configuration (e.g. "http", "https")
     * @return a configured Proxy object, iff proxy settings are specified in the VM properties
     */
    public static Proxy getProxy(final String protocol) {
        Proxy proxy = Proxy.NO_PROXY;
        // determine whether or not proxy should be used; based on VM properties
        //   https://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
        final String proxyHost = System.getProperty(String.format(PROTOCOL_PROXY_HOST, protocol));
        if (proxyHost != null) {
            final String proxyPort = System.getProperty(
                    String.format(PROTOCOL_PROXY_PORT, protocol), DEFAULT_PROXY_PORT);
            final SocketAddress socketAddress = new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort));
            proxy = new Proxy(Proxy.Type.HTTP, socketAddress);
        }
        return proxy;
    }

    /**
     * Pattern to check system properties for proxy host defined for protocol.
     */
    private static final String PROTOCOL_PROXY_HOST = "%s.proxyHost";

    /**
     * Pattern to check system properties for proxy port defined for protocol.
     */
    private static final String PROTOCOL_PROXY_PORT = "%s.proxyPort";

    /**
     * Default proxy port (to be used if one is not defined).
     */
    private static final String DEFAULT_PROXY_PORT = "3128";
}
