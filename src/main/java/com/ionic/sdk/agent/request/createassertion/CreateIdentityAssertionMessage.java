package com.ionic.sdk.agent.request.createassertion;

import com.ionic.sdk.agent.ServiceProtocol;
import com.ionic.sdk.agent.request.base.AgentRequestBase;
import com.ionic.sdk.agent.request.base.MessageBase;
import com.ionic.sdk.agent.request.createassertion.data.IdentityAssertion;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import javax.crypto.SecretKey;
import javax.json.Json;
import javax.json.JsonValue;

/**
 * Container for the service request payload to submit to Machina for processing.
 */
public class CreateIdentityAssertionMessage extends MessageBase {

    /**
     * The AES key associated with the device; to be used for this service operation.
     */
    private final SecretKey deviceKeyEi;

    /**
     * The ephemeral AES key to be used for this service operation.
     */
    private final SecretKey sessionKey;

    /**
     * Constructor.
     *
     * @param protocol the protocol used by the {@link com.ionic.sdk.key.KeyServices} client (authentication, state)
     * @param deviceKeyEi the AES key associated with the device; to be used for this service operation
     * @param sessionKey  the AES key to be used for this service operation
     * @throws IonicException on NULL input
     */
    public CreateIdentityAssertionMessage(final ServiceProtocol protocol, final SecretKey deviceKeyEi,
                                          final SecretKey sessionKey) throws IonicException {
        super(protocol);
        this.deviceKeyEi = deviceKeyEi;
        this.sessionKey = sessionKey;
    }

    @Override
    protected final JsonValue getJsonData(final AgentRequestBase requestBase) throws IonicException {
        SdkData.checkTrue(requestBase instanceof CreateIdentityAssertionRequest, SdkError.ISAGENT_ERROR);
        final CreateIdentityAssertionRequest request = (CreateIdentityAssertionRequest) requestBase;
        // protect session nonce
        final AesGcmCipher cipherNonce = new AesGcmCipher(sessionKey);
        cipherNonce.setAuthData(Transcoder.utf8().decode(request.getUri()));
        final String nonceCryptB64 = cipherNonce.encryptToBase64(
                Value.defaultOnEmpty(request.getNonce(), IdentityAssertion.DEFAULT_MFA));
        // protect session key
        final AesGcmCipher cipherEi = new AesGcmCipher(deviceKeyEi);
        cipherEi.setAuthData(Transcoder.utf8().decode(nonceCryptB64));
        final String keyCryptB64 = cipherEi.encryptToBase64(sessionKey.getEncoded());
        // craft payload
        return Json.createObjectBuilder()
                .add(IDC.Payload.FULL, request.isFullAssertion())
                .add(IDC.Payload.FOR_URI, request.getUri())
                .add(IDC.Payload.ENC_EK_NONCE, nonceCryptB64)
                .add(IDC.Payload.ENC_KAEK, keyCryptB64)
                .build();
    }
}
