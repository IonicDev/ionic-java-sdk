package com.ionic.sdk.agent.request.createdevice;

import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.device.profile.DeviceProfile;

/**
 * Represents the output for an Agent.createDevice() request.
 */
public final class CreateDeviceResponse extends AgentResponseBase {

    /**
     * Request state.
     */
    private DeviceProfile deviceProfile;

    /**
     * Constructor.
     */
    public CreateDeviceResponse() {
        super();
    }

    /**
     * @return the newly created device profile
     */
    public DeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    /**
     * Class instance variable setter.
     *
     * @param deviceProfile the device profile extracted from the server response payload
     */
    public void setDeviceProfile(final DeviceProfile deviceProfile) {
        this.deviceProfile = deviceProfile;
    }
}
