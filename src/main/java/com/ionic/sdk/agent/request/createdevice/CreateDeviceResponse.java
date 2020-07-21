package com.ionic.sdk.agent.request.createdevice;

import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.device.profile.DeviceProfile;

/**
 * Represents the output for a request to the Ionic Machina
 * Tools {@link com.ionic.sdk.agent.Agent#createDevice(CreateDeviceRequest)} API call.
 * <p>
 * This API is used to enroll devices with Machina, allowing for subsequent requests for cryptography keys and
 * service resources.
 * <p>
 * On success, the response contains a {@link com.ionic.sdk.device.profile.DeviceProfile} object, containing
 * information used by the device to make subsequent API calls.
 * <p>
 * See <a href='https://dev.ionic.com/api/device/create-profile' target='_blank'>Machina Developers</a> for
 * more information about the CreateDevice operation.
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

    /**
     * @return whether server response payload requires a "data" component
     */
    protected boolean isDataRequired() {
        return false;
    }

    /** Value of serialVersionUID from maven coordinates "com.ionic:ionic-sdk:2.8.0". */
    private static final long serialVersionUID = 4245560615669475754L;
}
