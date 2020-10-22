package com.ionic.sdk.agent;

import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.agent.hfp.Fingerprint;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.hash.Hash;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.IonicServerException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.httpclient.HttpHeaders;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;

import javax.json.Json;
import javax.json.JsonObject;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client context that uses VBE interfaces and endpoints to access Machina key servers.
 */
public final class VbeProtocol implements ServiceProtocol {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The wrapped {@link com.ionic.sdk.key.KeyServices} implementation.
     */
    private final Agent agent;

    /**
     * The device profile associated with this context.
     * <p>
     * In order to support multi-keyspace requests,
     * this {@link DeviceProfile} corresponds to a profile (with matching keyspace) contained in the
     * {@link com.ionic.sdk.device.profile.persistor.ProfilePersistor} associated with the in-use {@link Agent}, and
     * not necessarily the Agent's active profile.
     */
    private DeviceProfile deviceProfile;

    /**
     * Constructor.
     *
     * @param agent the wrapped {@link com.ionic.sdk.key.KeyServices} implementation
     */
    public VbeProtocol(final Agent agent) {
        this(agent, agent.hasActiveProfile() ? agent.getActiveProfile() : new DeviceProfile());
    }

    /**
     * Constructor.
     *
     * @param agent         the wrapped {@link com.ionic.sdk.key.KeyServices} implementation
     * @param deviceProfile the relevant {@link DeviceProfile} record for the request
     */
    public VbeProtocol(final Agent agent, final DeviceProfile deviceProfile) {
        this.agent = agent;
        this.deviceProfile = deviceProfile;
    }

    /**
     * @return the private AES key shared between the enrolled device and EI (Enterprise Infrastructure)
     */
    public byte[] getKeyEi() {
        final DeviceProfile activeProfile = agent.getActiveProfile();
        return (activeProfile == null) ? null : activeProfile.getAesCdEiProfileKey();
    }

    @Override
    public boolean isInitialized() {
        return agent.isInitialized();
    }

    @Override
    public boolean hasIdentity() {
        return (deviceProfile != null);
    }

    @Override
    public boolean isValidIdentity() throws IonicException {
        return deviceProfile.isValid();
    }

    @Override
    public String getIdentity() {
        return deviceProfile.getDeviceId();
    }

    @Override
    public AgentConfig getConfig() {
        return agent.getConfig();
    }

    @Override
    public void addHeader(final HttpHeaders httpHeaders) {
    }

    @Override
    public Fingerprint getFingerprint() {
        return agent.getFingerprint();
    }

    @Override
    public String generateCid() throws IonicException {
        return AgentTransactionUtil.generateConversationId(deviceProfile, IDC.Message.SERVER_API_CID);
    }

    @Override
    public MetadataMap getMetadata() {
        return agent.getMetadata();
    }

    @Override
    public String protectAttributes(final String authData, final String plainText) throws IonicException {
        SdkData.checkTrue(deviceProfile.getAesCdEiProfileKey() != null, SdkError.ISAGENT_NO_DEVICE_PROFILE);
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(deviceProfile.getAesCdEiProfileKey());
        cipher.setAuthData(Transcoder.utf8().decode(authData));
        return cipher.encryptToBase64(plainText);
    }

    @Override
    public String unprotectAttributes(final String authData, final String cipherText,
                                      final byte[] key) throws IonicException {
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(key);
        cipher.setAuthData(Transcoder.utf8().decode(authData));
        return cipher.decryptBase64ToString(cipherText);
    }

    @Override
    public byte[] transformRequestPayload(final byte[] payloadIn, final String cid) throws IonicException {
        // plaintext json; IDC http entity (for debugging)
        //if (logger.isLoggable(Level.FINEST)) {
        //    logger.finest(Transcoder.utf8().encode(payloadIn));
        //}
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(deviceProfile.getAesCdIdcProfileKey());
        cipher.setAuthData(Transcoder.utf8().decode(cid));
        final String payloadInWrapped = cipher.encryptToBase64(payloadIn);
        final JsonObject payload = Json.createObjectBuilder()
                .add(IDC.Payload.CID, cid)
                .add(IDC.Payload.ENVELOPE, payloadInWrapped)
                .build();
        final String payloadOut = JsonIO.write(payload, false);
        // VBE-secured json; IDC http entity (for debugging)
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(payloadOut);
        }
        return Transcoder.utf8().decode(payloadOut);
    }

    @Override
    public byte[] transformResponsePayload(final byte[] payloadIn, final String cidQ) throws IonicException {
        // VBE-secured json; IDC http entity (for debugging)
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(Transcoder.utf8().encode(payloadIn));
        }
        return ((cidQ == null) ? payloadIn : transformResponsePayloadInternal(payloadIn, cidQ));
    }

    /**
     * Perform a protection transform to a service response payload.
     *
     * @param payloadIn the response payload received from the service
     * @param cidQ      the unique id used to identify a particular service transaction
     * @return the transformed payload
     * @throws IonicException on failure to transform the service response payload
     */
    private byte[] transformResponsePayloadInternal(final byte[] payloadIn, final String cidQ) throws IonicException {
        final JsonObject jsonSecure = JsonIO.readObject(payloadIn, Level.WARNING);
        final String cid = JsonSource.getString(jsonSecure, IDC.Payload.CID);
        final String envelope = JsonSource.getString(jsonSecure, IDC.Payload.ENVELOPE);
        try {
            AgentTransactionUtil.checkNotNull(cid, IDC.Payload.CID, cid);
            AgentTransactionUtil.checkNotNull(envelope, IDC.Payload.ENVELOPE, envelope);
            AgentTransactionUtil.checkEqual(cidQ, cidQ, cid);
        } catch (IonicException e) {
            throw new IonicException(e.getReturnCode(), e.getMessage(), new IonicServerException(
                    SdkError.ISAGENT_REQUESTFAILED, JsonIO.write(jsonSecure, false)));
        }
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(deviceProfile.getAesCdIdcProfileKey());
        cipher.setAuthData(Transcoder.utf8().decode(cid));
        final String payloadOut = cipher.decryptBase64ToString(envelope);
        // plaintext json; IDC http entity (for debugging)
        //if (logger.isLoggable(Level.FINEST)) {
        //    logger.finest(payloadOut);
        //}
        return Transcoder.utf8().decode(payloadOut);
    }

    @Override
    public URL getUrl() throws IonicException {
        return AgentTransactionUtil.getProfileUrl(deviceProfile.getServer());
    }

    @Override
    public String getResource(final String version, final String operation) {
        return Value.DELIMITER_SLASH + version + operation;
    }

    @Override
    public byte[] getKeyBytes(final String keyHex, final String authData) throws IonicException {
        final AesGcmCipher cipherEi = new AesGcmCipher();
        cipherEi.setKey(deviceProfile.getAesCdEiProfileKey());
        cipherEi.setAuthData(Transcoder.utf8().decode(authData));
        return cipherEi.decrypt(Transcoder.hex().decode(keyHex));
    }

    @Override
    public String signAttributes(final String cid, final String refId, final String extra, final Properties sigs,
                                 final String attrs, final boolean areMutable) throws IonicException {
        SdkData.checkTrue(deviceProfile.getAesCdEiProfileKey() != null, SdkError.ISAGENT_NO_DEVICE_PROFILE);
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(deviceProfile.getAesCdEiProfileKey());
        final String authData = areMutable
                ? Value.join(IDC.Signature.DELIMITER, cid, IDC.Signature.MUTABLE, refId, extra)
                : Value.join(IDC.Signature.DELIMITER, cid, refId, extra);
        cipher.setAuthData(Transcoder.utf8().decode(authData));
        final byte[] plainText = new Hash().sha256(Transcoder.utf8().decode(attrs));
        final String sig = cipher.encryptToBase64(plainText);
        sigs.put(refId, sig);
        return sig;
    }

    @Override
    public void verifySignature(final String name, final String sigExpected,
                                final String attrs, final byte[] key) throws IonicException {
        if (sigExpected != null) {
            final byte[] keyVerify = ((key == null) ? deviceProfile.getAesCdEiProfileKey() : key);
            final String sigActual = CryptoUtils.hmacSHA256Base64(Transcoder.utf8().decode(attrs), keyVerify);
            if (!sigExpected.equals(sigActual)) {
                throw new IonicException(SdkError.ISAGENT_INVALIDVALUE,
                        new GeneralSecurityException(String.format("%s:[%s]!=[%s]", name, sigExpected, sigActual)));
            }
        }
    }
}
