package com.ionic.sdk.agent.request.createassertion;

import com.ionic.sdk.agent.VbeProtocol;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.AgentResponseBase;
import com.ionic.sdk.agent.request.base.AgentTransactionBase;
import com.ionic.sdk.agent.request.createassertion.data.IdentityAssertion;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.rng.CryptoRng;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.httpclient.Http;
import com.ionic.sdk.httpclient.HttpRequest;
import com.ionic.sdk.httpclient.HttpResponse;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.util.Properties;

/**
 * An object encapsulating the server request and response for a Machina CreateAssertion request.
 */
public class CreateIdentityAssertionTransaction extends AgentTransactionBase {

    /**
     * The AES key associated with the device; to be used for this service operation.
     */
    private final SecretKey deviceKeyEi;

    /**
     * Container for the service request payload to submit to Machina for processing.
     */
    private CreateIdentityAssertionMessage message;

    /**
     * Constructor.
     *
     * @param protocol the protocol used by the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @param requestBase  the client request
     * @param responseBase the server response
     */
    public CreateIdentityAssertionTransaction(
            final VbeProtocol protocol,
            final AgentRequestBase requestBase, final AgentResponseBase responseBase) {
        super(protocol, requestBase, responseBase);
        this.deviceKeyEi = new SecretKeySpec(protocol.getKeyEi(), AesCipher.ALGORITHM);
        this.message = null;
    }

    @Override
    protected final HttpRequest buildHttpRequest(final Properties fingerprint) throws IonicException {
        final SecretKey sessionKey = new SecretKeySpec(
                new CryptoRng().rand(new byte[AesCipher.KEY_BYTES]), AesCipher.ALGORITHM);
        message = new CreateIdentityAssertionMessage(getProtocol(), deviceKeyEi, sessionKey);
        // assemble the request
        final AgentRequestBase requestBase = getRequestBase();
        SdkData.checkTrue(requestBase instanceof CreateIdentityAssertionRequest, SdkError.ISAGENT_ERROR);
        final CreateIdentityAssertionRequest request = (CreateIdentityAssertionRequest) requestBase;
        final JsonObject jsonMessage = message.getJsonMessage(request, fingerprint);
        final String resource = getProtocol().getResource(IDC.Resource.SERVER_API_V24, IDC.Resource.ASSERTION_CREATE);
        final byte[] envelope = Transcoder.utf8().decode(JsonIO.write(jsonMessage, false));
        final byte[] envelopeSecure = getProtocol().transformRequestPayload(envelope, message.getCid());
        // assemble the HTTP request to be sent to the server
        final ByteArrayInputStream bis = new ByteArrayInputStream(envelopeSecure);
        return new HttpRequest(getProtocol().getUrl(), Http.Method.POST, resource, getHttpHeaders(), bis);
    }

    @Override
    protected final void parseHttpResponse(
            final HttpRequest httpRequest, final HttpResponse httpResponse) throws IonicException {
        parseHttpResponseBase(httpRequest, httpResponse, message.getCid());
        final AgentRequestBase requestBase = getRequestBase();
        final AgentResponseBase responseBase = getResponseBase();
        SdkData.checkTrue(requestBase instanceof CreateIdentityAssertionRequest, SdkError.ISAGENT_ERROR);
        SdkData.checkTrue(responseBase instanceof CreateIdentityAssertionResponse, SdkError.ISAGENT_ERROR);
        final CreateIdentityAssertionRequest request = (CreateIdentityAssertionRequest) requestBase;
        final CreateIdentityAssertionResponse response = (CreateIdentityAssertionResponse) responseBase;
        // extract the assertion from the service response
        final JsonObject jsonPayload = response.getJsonPayload();
        final JsonObject jsonData = JsonSource.getJsonObject(jsonPayload, IDC.Payload.DATA);
        final String encKAResp = JsonSource.getString(jsonData, IDC.Payload.ENC_KARESP);
        SdkData.checkTrue(!Value.isEmpty(encKAResp), SdkError.ISAGENT_BADRESPONSE, IDC.Payload.ENC_KARESP);
        // unwrap the assertion (service encryption)
        final AesGcmCipher cipherNonce = new AesGcmCipher(deviceKeyEi);
        cipherNonce.setAuthData(Transcoder.utf8().decode(
                Value.defaultOnEmpty(request.getNonce(), IdentityAssertion.DEFAULT_MFA)));
        // extract the data
        final JsonObject jsonAssertion = JsonIO.readObject(cipherNonce.decryptBase64(encKAResp));
        final String textAssertion = JsonSource.getString(jsonAssertion, IDC.Payload.ASSERTION);
        response.setAssertionBase64(textAssertion);
    }
}
