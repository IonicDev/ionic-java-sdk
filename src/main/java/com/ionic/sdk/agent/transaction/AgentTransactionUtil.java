package com.ionic.sdk.agent.transaction;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.rng.RNG;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.core.vm.VM;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.AgentErrorModuleConstants;
import com.ionic.sdk.error.IonicException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Helper methods used during processing of server request/response transactions.
 * <p>
 * ${IONIC_REPO_ROOT}/IonicAgents/SDK/ISAgentSDK/ISAgentLib/ISAgentTransactionUtil.cpp
 */
public final class AgentTransactionUtil {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private AgentTransactionUtil() {
    }

    /**
     * Generate a unique id to be used to identify a particular server transaction, and to help secure its content
     * during transit.
     *
     * @param deviceId the id associated with the active record in the Secure Enrollment Profile.
     * @return a string to be used in context of a server transaction
     */
    public static String generateConversationId(final String deviceId) {
        final String date = Long.toString(new Date().getTime());
        // generate a conversation nonce
        final byte[] bytes = RNG.fill(new byte[Integer.SIZE / Byte.SIZE]);
        final String nonce = Transcoder.base64().encode(bytes).replace(Transcoder.BASE64_PAD, "");
        // generate a conversation id
        return Value.join(IDC.Message.DELIMITER, IDC.Message.CID, deviceId, date, nonce);
    }

    /**
     * Generate a unique id to be used to identify a particular server transaction, and to help secure its content
     * during transit.
     *
     * @param deviceId the id associated with the active record in the Secure Enrollment Profile.
     * @return a string to be used in context of a server transaction
     */
    public static String generateConversationIdV(final String deviceId) {
        final String date = Long.toString(new Date().getTime());
        // generate a conversation nonce
        final byte[] bytes = RNG.fill(new byte[Integer.SIZE / Byte.SIZE]);
        final String nonce = Transcoder.base64().encode(bytes).replace(Transcoder.BASE64_PAD, "");
        // generate a conversation id
        return Value.join(IDC.Message.DELIMITER, IDC.Message.CID, deviceId, date, nonce, IDC.Message.SERVER_API_CID);
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
     * Wrap the creation of a URL from the server setting in a device profile.
     *
     * @param deviceProfile the descriptor for an IDC SDK endpoint
     * @return a URL representation of the server
     * @throws IonicException if the server setting cannot be converted to a URL
     */
    public static URL getProfileUrl(final DeviceProfile deviceProfile) throws IonicException {
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
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_NULL_INPUT.value(), e);
        } catch (MalformedURLException e) {
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_INVALIDVALUE.value(), e);
        }
    }

    /**
     * Assemble the meta data associated with the request.
     *
     * @param agent       the {@link com.ionic.sdk.key.KeyServices} implementation
     * @param requestBase the client request
     * @param fingerprint authentication data associated with the client state to be included in the request
     * @return a {@link JsonObject} to be incorporated into the request payload
     */
    public static JsonObject buildStandardJsonMeta(
            final Agent agent, final AgentRequestBase requestBase, final Properties fingerprint) {
        final JsonObjectBuilder builderMeta = Json.createObjectBuilder();
        // apply metadata from agent
        for (Map.Entry<String, String> entry : agent.getMetadata().entrySet()) {
            builderMeta.add(entry.getKey(), entry.getValue());
        }
        // apply metadata from request
        for (Map.Entry<String, String> entry : requestBase.getMetadata().entrySet()) {
            builderMeta.add(entry.getKey(), entry.getValue());
        }
        // apply fingerprint data
        for (Map.Entry<Object, Object> entry : fingerprint.entrySet()) {
            builderMeta.add(entry.getKey().toString(), entry.getValue().toString());
        }
        // apply SDK-provided metadata
        builderMeta
                .add(IDC.Metadata.IONIC_AGENT, ionicAgent())  // https://en.wikipedia.org/wiki/User_agent
                .add(IDC.Metadata.USER_AGENT, agent.getConfig().getUserAgent())
                .add(IDC.Metadata.OS_ARCH, System.getProperty(VM.Sys.OS_ARCH))
                .add(IDC.Metadata.OS_NAME, System.getProperty(VM.Sys.OS_NAME))
                .add(IDC.Metadata.OS_VERSION, System.getProperty(VM.Sys.OS_VERSION));
        return builderMeta.build();
    }

    /**
     * Generate the Ionic agent string, which identifies the SDK language and version in communications with ionic.com.
     *
     * @return a string identifying the SDK language and version
     */
    private static String ionicAgent() {
        return String.format(IONIC_AGENT, AgentTransactionUtil.class.getPackage().getImplementationVersion());
    }

    /**
     * Ionic agent string pattern.
     */
    private static final String IONIC_AGENT = "IonicSDK/2 Java/%s";

    /**
     * Validate value against expected value.
     *
     * @param cid      the conversation id associated with the check
     * @param expected the expected value
     * @param actual   the actual value
     * @throws IonicException on expectation failure
     */
    public static void checkEqual(final String cid, final String expected, final String actual) throws IonicException {
        if (!Value.isEqual(expected, actual)) {
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_INVALIDVALUE.value(), new IOException(
                    String.format("Expectation failed (conversation ID %s expected %s, actual %s).",
                            cid, expected, actual)));
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
    public static void checkNotNull(final String cid, final String name, final String value) throws IonicException {
        if (value == null) {
            throw new IonicException(AgentErrorModuleConstants.ISAGENT_INVALIDVALUE.value(),
                    new IOException(String.format("Null value detected (conversation ID %s, name %s).", cid, name)));
        }
    }
}
