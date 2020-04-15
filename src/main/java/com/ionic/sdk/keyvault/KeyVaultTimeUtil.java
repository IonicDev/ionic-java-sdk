package com.ionic.sdk.keyvault;

import com.ionic.sdk.agent.Agent;

/**
 * A utility class for managing server time.
 */
public final class KeyVaultTimeUtil {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private KeyVaultTimeUtil() {
    }

    /**
     * Get the current server time UTC seconds.
     * <p>
     * This method returns the current server time UTC seconds.  See {@link Agent#getServerTimeUtcMillis()}
     * for more information about how this time value is determined and why it is useful.
     * <p>
     * This method should be used for calculating the issued time and expiration time of
     * a key (see {@link KeyVaultKey#setIssuedServerTimeUtcSeconds(long)} and
     * {@link KeyVaultKey#setExpirationServerTimeUtcSeconds(long)}).
     *
     * @return Server time in UTC seconds
     * @see com.ionic.sdk.core.date.DateTime
     */
    public static long getCurrentServerTimeUtcSeconds() {
        return Agent.getServerTimeUtcSecs();
    }
}
