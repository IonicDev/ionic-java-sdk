package com.ionic.sdk.agent.transaction;

import com.ionic.sdk.agent.SdkVersion;
import com.ionic.sdk.agent.ServiceProtocol;
import com.ionic.sdk.agent.key.KeyObligationsMap;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.rng.CryptoRng;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.core.vm.VM;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.json.JsonTarget;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Helper methods used during processing of server request/response transactions.
 * <p>
 * ${IONIC_REPO_ROOT}/IonicAgents/SDK/ISAgentSDK/ISAgentLib/ISAgentTransactionUtil.cpp
 */
@InternalUseOnly
public final class AgentTransactionUtil {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private AgentTransactionUtil() {
    }

    /**
     * Resolve the {@link DeviceProfile} to be used for the indicated key ID.
     *
     * @param deviceProfiles the device enrollments to examine
     * @param keyId          the key identifier to match
     * @return the relevant {@link DeviceProfile} record for the request, or null if no {@link DeviceProfile} is
     * found for the key
     */
    public static DeviceProfile getProfileForKeyId(final List<DeviceProfile> deviceProfiles, final String keyId) {
        DeviceProfile deviceProfile = null;
        for (DeviceProfile deviceProfileIt : deviceProfiles) {
            final String keySpace = deviceProfileIt.getKeySpace();
            if ((keyId != null) && (keySpace.length() < keyId.length()) && keyId.startsWith(keySpace)) {
                if (deviceProfile == null) {
                    deviceProfile = deviceProfileIt;
                } else if (deviceProfile.getCreationTimestampSecs() < deviceProfileIt.getCreationTimestampSecs()) {
                    deviceProfile = deviceProfileIt;
                }
            }
        }
        return deviceProfile;
    }

    /**
     * Generate a unique id to be used to identify a particular server transaction, and to help secure its content
     * during transit.
     *
     * @param activeProfile the active profile in the Secure Enrollment Profile
     * @param extra         additional token to append to the CID
     * @return a string to be used in context of a server transaction
     * @throws IonicException on device misconfiguration; random number generation failure
     */
    public static String generateConversationId(
            final DeviceProfile activeProfile, final String extra) throws IonicException {
        SdkData.checkNotNull(activeProfile, DeviceProfile.class.getName());
        final String deviceId = activeProfile.getDeviceId();
        SdkData.checkNotNull(deviceId, IDC.Payload.DEVICE_ID);
        final String date = Long.toString(new Date().getTime());
        // generate a conversation nonce
        final byte[] bytes = new CryptoRng().rand(new byte[Integer.SIZE / Byte.SIZE]);
        final String nonce = Transcoder.base64().encode(bytes).replace(Transcoder.BASE64_PAD, "");
        // generate a conversation id
        return Value.join(IDC.Message.DELIMITER, IDC.Message.CID, deviceId, date, nonce, extra);
    }

    /**
     * HTTP code is an error if it is not in the 200-299 range.
     *
     * @param statusCode an http status code received in an http server response
     * @return true, iff status code is not in the range of http success codes
     */
    public static boolean isHttpErrorCode(final int statusCode) {
        return ((statusCode < HttpURLConnection.HTTP_OK) || (statusCode >= HttpURLConnection.HTTP_MULT_CHOICE));
    }

    /**
     * HTTP code is an error if it is not in the 200-299 range.
     *
     * @param statusCode an http status code received in an http server response
     * @return true, iff status code is in the range of http success codes
     */
    public static boolean isHttpSuccessCode(final int statusCode) {
        return ((statusCode >= HttpURLConnection.HTTP_OK) && (statusCode < HttpURLConnection.HTTP_MULT_CHOICE));
    }

    /**
     * Wrap the creation of a URL from the server setting in a device profile.
     *
     * @param deviceProfile the descriptor for an IDC SDK endpoint
     * @return a URL representation of the server
     * @throws IonicException if the server setting cannot be converted to a URL
     */
    public static URL getProfileUrl(final DeviceProfile deviceProfile) throws IonicException {
        SdkData.checkNotNull(deviceProfile, DeviceProfile.class.getName());
        return getProfileUrlInternal(deviceProfile.getServer());
    }

    /**
     * Wrap the creation of a URL from the server setting in a device profile.
     *
     * @param server the server URL specified in the IDC SDK endpoint
     * @return a URL representation of the server
     * @throws IonicException if the server setting cannot be converted to a URL
     */
    public static URL getProfileUrl(final String server) throws IonicException {
        return getProfileUrlInternal(server);
    }

    /**
     * Wrap the creation of a URL from the server setting in a device profile.
     *
     * @param server the server URL specified in the IDC SDK endpoint
     * @return a URL representation of the server
     * @throws IonicException if the server setting cannot be converted to a URL
     */
    private static URL getProfileUrlInternal(final String server) throws IonicException {
        try {
            return new URL(server);
        } catch (NullPointerException e) {
            throw new IonicException(SdkError.ISAGENT_NULL_INPUT, e);
        } catch (MalformedURLException e) {
            throw new IonicException(SdkError.ISAGENT_INVALIDVALUE, e);
        }
    }

    /**
     * Assemble the meta data associated with the request.
     *
     * @param protocol    the communications protocol of the {@link com.ionic.sdk.key.KeyServices} client
     *                    (authentication, state)
     * @param requestBase the KeyServices client request
     * @param fingerprint authentication data associated with the client state to be included in the request
     * @return a {@link JsonObject} to be incorporated into the request payload
     */
    public static JsonObject buildStandardJsonMeta(
            final ServiceProtocol protocol, final AgentRequestBase requestBase, final Properties fingerprint) {
        final JsonObjectBuilder builderMeta = Json.createObjectBuilder();
        // apply metadata from agent
        for (Map.Entry<String, String> entry : protocol.getMetadata().entrySet()) {
            JsonTarget.addNotNull(builderMeta, entry.getKey(), entry.getValue());
        }
        // apply metadata from request
        for (Map.Entry<String, String> entry : requestBase.getMetadata().entrySet()) {
            JsonTarget.addNotNull(builderMeta, entry.getKey(), entry.getValue());
        }
        // apply fingerprint data
        for (String key : fingerprint.stringPropertyNames()) {
            JsonTarget.addNotNull(builderMeta, key, fingerprint.getProperty(key));
        }
        // apply SDK-provided metadata; https://en.wikipedia.org/wiki/User_agent
        JsonTarget.addNotNull(builderMeta, IDC.Metadata.IONIC_AGENT, SdkVersion.getAgentString());
        JsonTarget.addNotNull(builderMeta, IDC.Metadata.USER_AGENT, protocol.getConfig().getUserAgent());
        JsonTarget.addNotNull(builderMeta, IDC.Metadata.OS_ARCH, System.getProperty(VM.Sys.OS_ARCH));
        JsonTarget.addNotNull(builderMeta, IDC.Metadata.OS_NAME, System.getProperty(VM.Sys.OS_NAME));
        JsonTarget.addNotNull(builderMeta, IDC.Metadata.OS_VERSION, System.getProperty(VM.Sys.OS_VERSION));
        return builderMeta.build();
    }

    /**
     * Validate value against expected value.
     *
     * @param cid      the conversation id associated with the check
     * @param expected the expected value
     * @param actual   the actual value
     * @throws IonicException on expectation failure
     */
    public static void checkEqual(
            final String cid, final String expected, final String actual) throws IonicException {
        if (!Value.isEqual(expected, actual)) {
            final int error = SdkError.ISAGENT_INVALIDVALUE;
            throw new IonicException(error, Value.join(VM.getEol(), SdkError.getErrorString(error), String.format(
                    "Expectation failed (conversation ID %s expected %s, actual %s).", cid, expected, actual)));
        }
    }

    /**
     * Validate input value.  Value should not be null.
     *
     * @param cid   the conversation id associated with the check
     * @param name  the name associated with the value to check
     * @param value the value to check for validity
     * @throws IonicException on null value
     */
    public static void checkNotNull(
            final String cid, final String name, final String value) throws IonicException {
        if (value == null) {
            final int error = SdkError.ISAGENT_INVALIDVALUE;
            throw new IonicException(error, Value.join(VM.getEol(), SdkError.getErrorString(error), String.format(
                    "Null value detected (conversation ID %s, name %s).", cid, name)));
        }
    }

    /**
     * Assemble a {@link KeyObligationsMap} object from the service response key data.
     *
     * @param jsonObject service response {@link JsonObject} representation of obligations data
     * @return {@link KeyObligationsMap} representation of input data
     * @throws IonicException on json parsing errors
     */
    public static KeyObligationsMap toObligations(final JsonObject jsonObject) throws IonicException {
        final KeyObligationsMap keyObligationsMap = new KeyObligationsMap();
        final Iterator<Map.Entry<String, JsonValue>> iterator = JsonSource.getIteratorNullable(jsonObject);
        while (iterator.hasNext()) {
            final Map.Entry<String, JsonValue> entry = iterator.next();
            final List<String> valuesOut = new ArrayList<String>();
            final String key = entry.getKey();
            final JsonArray valuesIn = JsonSource.toJsonArray(entry.getValue(), key);
            for (JsonValue valueIn : valuesIn) {
                if (valueIn instanceof JsonString) {
                    valuesOut.add(JsonSource.toString(valueIn));
                } else {
                    throw new IonicException(SdkError.ISAGENT_INVALIDVALUE, key);
                }
            }
            if (!valuesOut.isEmpty()) {
                keyObligationsMap.put(key, valuesOut);
            }
        }
        return keyObligationsMap;
    }
}
