package com.ionic.sdk.httpclient.tls;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * Custom TLS trust manager, which does no validation on server certificates it is presented.  Any presented
 * server certificate will be trusted.  Useful for testing.
 * <p>
 * Not intended for use in production applications.
 */
public class TrustAllTrustManager implements X509TrustManager {

    /**
     * Given the partial or complete certificate chain provided by the peer, build a certificate path to a trusted root
     * and return if it can be validated and is trusted for client SSL authentication based on the authentication type.
     *
     * @param chain    the peer certificate chain
     * @param authType the authentication type based on the client certificate
     */
    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
    }

    /**
     * Given the partial or complete certificate chain provided by the peer, build a certificate path to a trusted root
     * and return if it can be validated and is trusted for server SSL authentication based on the authentication type.
     *
     * @param chain    the peer certificate chain
     * @param authType the authentication type based on the client certificate
     */
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
    }

    /**
     * Return an array of certificate authority certificates which are trusted for authenticating peers.
     *
     * @return an empty array of acceptable CA issuer certificates
     */
    @Override
    public final X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
