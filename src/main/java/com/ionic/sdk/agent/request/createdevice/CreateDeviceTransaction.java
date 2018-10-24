package com.ionic.sdk.agent.request.createdevice;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.base.AgentTransactionBase;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.aes.AesCtrCipher;
import com.ionic.sdk.cipher.aes.model.AesKeyGenerator;
import com.ionic.sdk.cipher.aes.model.AesKeyHolder;
import com.ionic.sdk.cipher.rsa.RsaCipher;
import com.ionic.sdk.cipher.rsa.model.RsaKeyGenerator;
import com.ionic.sdk.cipher.rsa.model.RsaKeyHolder;
import com.ionic.sdk.cipher.rsa.model.RsaKeyPersistor;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.date.DateTime;
import com.ionic.sdk.core.io.Stream;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.profile.DeviceProfile;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.httpclient.Http;
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * An object encapsulating the server request and response for an Agent.getResources() request.
 */
public class CreateDeviceTransaction extends AgentTransactionBase {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Request state.
     */
    private final RsaKeyHolder rsaKeyHolder;

    /**
     * Request state.
     */
    private final AesKeyHolder aesKeyHolder;

    /**
     * Constructor.
     *
     * @param agent        the persistent data associated with the device's Secure Enrollment Profile
     * @param requestBase  the client request
     * @param responseBase the server response
     * @throws IonicException on errors generating session cryptography keys for use in the request
     */
    public CreateDeviceTransaction(
            final Agent agent, final AgentRequestBase requestBase,
            final AgentResponseBase responseBase) throws IonicException {
        super(agent, requestBase, responseBase);
        int errorCode = SdkError.ISAGENT_ERROR;
        SdkData.checkTrue(requestBase instanceof CreateDeviceRequest, errorCode, SdkError.getErrorString(errorCode));
        final CreateDeviceRequest request = (CreateDeviceRequest) requestBase;
        final RsaKeyHolder keyHolder = request.getRsaKeyHolder();
        this.rsaKeyHolder = ((keyHolder == null) ? new RsaKeyGenerator().generate(RsaCipher.KEY_BITS) : keyHolder);
        this.aesKeyHolder = new AesKeyGenerator().generate();
    }

    /**
     * Assemble a client request for submission to the IDC infrastructure.
     *
     * @param fingerprint authentication data associated with the client state to be included in the request
     * @return a request object, ready for submission to the server
     * @throws IonicException on failure to assemble the request
     */
    @Override
    protected final HttpRequest buildHttpRequest(final Properties fingerprint) throws IonicException {
        final CreateDeviceRequest request = (CreateDeviceRequest) getRequestBase();

        // ASSEMBLE BASE REQUEST (OUR SESSION PUBKEY, PLUS SERVER SUPPLIED TOKEN)
        // serialize RSA session public key using DER and encode with Base64
        final String pubkeySessionB64 = new RsaKeyPersistor().toBase64Public(rsaKeyHolder);
        // build AUTH data string
        final String authdata = Value.join(IDC.Signature.DELIMITER_COMMA, request.getToken(), request.getUidAuth());
        final String authdataB64 = CryptoUtils.binToBase64(Transcoder.utf8().decode(authdata));
        // build the JSON-serialized payload that will be encrypted and signed
        final JsonObject jsonPayloadRoot = Json.createObjectBuilder()
                .add(IDC.Payload.PUBKEYDERB64, pubkeySessionB64)
                .add(IDC.Payload.AUTH, authdataB64)
                .build();
        final String payloadPlainText = JsonIO.write(jsonPayloadRoot, false);

        // ENCRYPT USING CLIENT SYMMETRIC KEY
        // encrypt payload with AES session key and encode with Base64
        final AesCtrCipher cipherAES = new AesCtrCipher();
        cipherAES.setKey(aesKeyHolder.getKey().getEncoded());
        final String payloadSecureB64 = cipherAES.encryptToBase64(payloadPlainText);

        // ASSEMBLE SECURE REQUEST (USE SERVER PUBKEY TO SECURE CLIENT SYMMETRIC KEY)
        // encrypt AES session key with RSA public key from EI and encode with Base64
        final RsaKeyHolder rsaKeyHolderEI = new RsaKeyPersistor().fromBase64(request.getEiRsaPublicKey(), null);
        final RsaCipher cipherRSAEI = new RsaCipher();
        cipherRSAEI.setKeypairInstance(rsaKeyHolderEI.getKeypair());
        final byte[] encrypt = cipherRSAEI.encrypt(aesKeyHolder.getKey().getEncoded());
        final String aesSessionKeyB64 = CryptoUtils.binToBase64(encrypt);

        // SIGN OUR REQUEST (SERVER WILL VERIFY SIGNATURE USING CLIENT PUBKEY)
        // sign our encrypted, Base64-encoded payload using our RSA private session key
        final RsaCipher cipherRSASession = new RsaCipher();
        cipherRSASession.setKeypairInstance(this.rsaKeyHolder.getKeypair());
        final String signatureB64 = cipherRSASession.sign(Transcoder.utf8().decode(payloadSecureB64));

        // ASSEMBLE LINE REQUEST
        // build final JSON data
        final JsonObject jsonRequestRoot = Json.createObjectBuilder()
                .add(IDC.Payload.G, signatureB64)
                .add(IDC.Payload.K, request.getEtag())
                .add(IDC.Payload.P, payloadSecureB64)
                .add(IDC.Payload.S, aesSessionKeyB64)
                .build();
        final String entitySecure = JsonIO.write(jsonRequestRoot, false);
        logger.fine(entitySecure);
        final String resource = String.format(IDC.Resource.DEVICE_CREATE,
                IDC.Resource.SERVER_API_V22, request.getEtag());
        // assemble the HTTP request to be sent to the server
        final URL url = AgentTransactionUtil.getProfileUrl(request.getServer());
        logger.fine(request.getServer());
        final ByteArrayInputStream bis = new ByteArrayInputStream(
                Transcoder.utf8().decode(JsonIO.write(jsonRequestRoot, false)));
        return new HttpRequest(url, Http.Method.POST, resource, getHttpHeaders(), bis);
    }

    /**
     * Parse and process the server response to the client request.
     *
     * @param httpRequest  the server request
     * @param httpResponse the server response
     * @throws IonicException on errors in the server response
     */
    @Override
    protected final void parseHttpResponse(
            final HttpRequest httpRequest, final HttpResponse httpResponse) throws IonicException {
        // unwrap the server response
        parseHttpResponseBase(httpRequest, httpResponse, null);
        try {
            // deserialize, validate server response entity
            final String entity = Transcoder.utf8().encode(Stream.read(httpResponse.getEntity()));
            logger.fine(entity);
            final CreateDeviceRequest request = (CreateDeviceRequest) getRequestBase();
            final CreateDeviceResponse response = (CreateDeviceResponse) getResponseBase();
            final JsonObject json = JsonIO.readObject(entity, SdkError.ISAGENT_PARSEFAILED);
            final String cid = JsonSource.getString(json, IDC.Payload.CID);
            logger.finest(cid);
            final String deviceId = JsonSource.getString(json, IDC.Payload.DEVICE_ID);
            final String sepAesk = JsonSource.getString(json, IDC.Payload.SEPAESK);
            final String sepAeskIdc = JsonSource.getString(json, IDC.Payload.SEPAESK_IDC);

            final AesCtrCipher aesCipher = new AesCtrCipher();
            aesCipher.setKey(aesKeyHolder.getKey().getEncoded());
            final byte[] keyEI = aesCipher.decrypt(CryptoUtils.base64ToBin(sepAesk));

            final RsaCipher rsaCipher = new RsaCipher();
            rsaCipher.setKeypairInstance(rsaKeyHolder.getKeypair());
            final byte[] keyIDC = rsaCipher.decrypt(CryptoUtils.base64ToBin(sepAeskIdc));

            final long creationTimestamp = (System.currentTimeMillis() / DateTime.ONE_SECOND_MILLIS);
            final DeviceProfile deviceProfile = new DeviceProfile(request.getDeviceProfileName(),
                    creationTimestamp, deviceId, request.getServer(), keyIDC, keyEI);
            response.setDeviceProfile(deviceProfile);
        } catch (IOException e) {
            throw new IonicException(SdkError.ISAGENT_BADRESPONSE, e);
        }
    }
}
