package com.ionic.sdk.core.vm;

import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.AgentErrorModuleConstants;
import com.ionic.sdk.error.IonicException;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.TreeSet;

/**
 * Utility functions accessing networking APIs.
 */
public final class Network {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Network() {
    }

    /**
     * Enumerate the hardware addresses available to this JVM.
     *
     * @return an array of hex-encoded String representations of the JVM hardware addresses (MAC addresses)
     * @throws IonicException on failure to enumerate the JVM hardware addresses
     */
    public static String[] getMacAddresses() throws IonicException {
        // order addresses to protect against API shuffling the results
        final Collection<String> macAddresses = new TreeSet<String>();
        try {
            final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                final NetworkInterface networkInterface = networkInterfaces.nextElement();
                final byte[] hardwareAddress = networkInterface.getHardwareAddress();
                if (hardwareAddress != null) {
                    macAddresses.add(Transcoder.hex().encode(hardwareAddress));
                }
            }
        } catch (SocketException e) {
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_ERROR.value(), e);
        }
        return macAddresses.toArray(new String[macAddresses.size()]);
    }
}
