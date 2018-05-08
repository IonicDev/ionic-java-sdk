package com.ionic.sdk.crypto.env;

import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.hash.Hash;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.core.vm.Network;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Provide simplified access to JVM environment data that can be used to seed a
 * {@link com.ionic.sdk.crypto.secretshare.SecretShareData} implementation.
 */
public final class Environment {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private Environment() {
    }

    /**
     * Retrieve the value associated with a system property of the running JVM.
     *
     * @param name         the name of the system property to retrieve
     * @param defaultValue the value to return if the specified system property does not exist, or if it is empty
     * @return the system property value if it exists, otherwise the specified default value
     */
    public static String sysProp(final String name, final String defaultValue) {
        final String value = System.getProperty(name);
        return Value.defaultOnEmpty(value, defaultValue);
    }

    /**
     * Retrieve the value associated with a system environment variable of the running JVM process.
     *
     * @param name         the name of the system environment variable to retrieve
     * @param defaultValue the value to return if the specified environment variable does not exist, or if it is empty
     * @return the system environment variable value if it exists, otherwise the specified default value
     */
    public static String sysEnv(final String name, final String defaultValue) {
        final String value = System.getenv(name);
        return Value.defaultOnEmpty(value, defaultValue);
    }

    /**
     * Retrieve a string containing the hostname associated with the localhost network interface of the running JVM.
     *
     * @param defaultValue the value to return if the lookup fails
     * @return the hostname associated with the localhost network interface
     */
    public static String hostname(final String defaultValue) {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieve a string containing the delimited set of network (MAC) addresses associated with the running JVM.
     *
     * @param defaultValue the value to return if the network address enumeration fails
     * @return the string representation of the network addresses if available, otherwise the specified default value
     */
    public static String netAddresses(final String defaultValue) {
        try {
            return Value.joinArray(IDC.Message.DELIMITER, Network.getMacAddresses());
        } catch (IonicException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieve the list of delimited filenames in the specified folder which match the filter regex.
     *
     * @param path         the filesystem path of the file to examine
     * @param filter       a regular expression used to filter out filenames that do not match
     * @param defaultValue the value to return if the file does not exist, or cannot be read
     * @return a delimited list containing filenames in the folder which match the filter
     */
    public static String folderContent(final String path, final String filter, final String defaultValue) {
        final Collection<String> value = new TreeSet<String>();
        final Pattern pattern = Pattern.compile(filter);
        final File folder = new File(path);
        final String[] filenames = folder.list();
        if (filenames != null) {
            for (String filename : filenames) {
                if (pattern.matcher(filename).matches()) {
                    value.add(filename);
                }
            }
        }
        return (value.isEmpty()) ? defaultValue : Value.joinCollection(IDC.Message.DELIMITER, value);
    }

    /**
     * Retrieve the base64 representation of the sha256 hash of the content of the specified filesystem path.
     *
     * @param path         the filesystem path of the file to examine
     * @param defaultValue the value to return if the file does not exist, or cannot be read
     * @return the base64 representation of the sha256 hash of the file content, if it can be read
     */
    public static String fileContentHash(final String path, final String defaultValue) {
        try {
            return Transcoder.base64().encode(new Hash().sha256(DeviceUtils.read(new File(path))));
        } catch (IonicException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieve the value associated with the last modified timestamp of the specified filesystem path.
     *
     * @param path         the filesystem path of the file entry to examine
     * @param defaultValue the value to return if the file entry does not exist, or cannot be read
     * @return the string representation of the filesystem entry timestamp, expressed as a long
     * @see File#lastModified()
     */
    public static String dirEntryTime(final String path, final String defaultValue) {
        long lastModified = new File(path).lastModified();
        return ((lastModified == 0L) ? defaultValue : Long.toString(lastModified));
    }
}
