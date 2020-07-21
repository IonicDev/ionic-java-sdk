package com.ionic.sdk.httpclient.tls;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Custom {@link HostnameVerifier}, which does no validation on server certificates it is presented.  Any presented
 * server certificate will be trusted.  Useful for testing.
 * <p>
 * Not intended for use in production applications.
 */
public class TrustAllHostnameVerifier implements HostnameVerifier {

    @Override
    public final boolean verify(final String hostname, final SSLSession session) {
        return true;
    }
}
