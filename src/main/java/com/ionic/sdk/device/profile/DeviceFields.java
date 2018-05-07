package com.ionic.sdk.device.profile;

/**
 * keys for DeviceProfile json.
 */
public final class DeviceFields {
    /**
     * key for a json array of device profiles.
     */
    public static final String FIELD_PROFILES = "profiles";

    /**
     * key for the active device id.
     */
    public static final String FIELD_ACTIVE_DEVICE_ID = "activeDeviceId";

    /**
     * The key for the name of a device profile.
     */
    public static final String FIELD_NAME = "name";

    /**
     * The key for the creation time stamp of a device profile.
     */
    public static final String FIELD_CREATION_TIMESTAMP = "creationTimestamp";

    /**
     * The key for the device id of a device profile.
     */
    public static final String FIELD_DEVICE_ID = "deviceId";

    /**
     * The key for the server name of a device profile.
     */
    public static final String FIELD_SERVER = "server";

    /**
     * The key for the aes_idc field of a device profile.
     */
    public static final String FIELD_AES_CD_IDC_KEY = "aesCdIdcKey";

    /**
     * The key for the aes_cd field of a device profile.
     */
    public static final String FIELD_AES_CD_EI_KEY = "aesCdEiKey";

    /**
     * private constructor to prevent initialization of this static class.
     */
    private DeviceFields() { }
}
