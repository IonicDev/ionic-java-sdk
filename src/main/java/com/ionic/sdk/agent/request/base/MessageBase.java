package com.ionic.sdk.agent.request.base;

import com.ionic.sdk.agent.Agent;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.agent.transaction.AgentTransactionUtil;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.hash.Hash;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.error.AgentErrorModuleConstants;
import com.ionic.sdk.error.IonicException;

import javax.json.Json;
import javax.json.JsonObject;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * Encapsulation of helper logic associated with an SDK IDC call.  Includes state associated with request, and
 * conversion of request object into json representation, for submission to IDC.
 */
public abstract class MessageBase {

    /**
     * The {@link com.ionic.sdk.key.KeyServices} implementation.
     */
    private final Agent agent;

    /**
     * A unique id used to identify a particular server transaction, and to help secure its content during transit.
     */
    private final String cid;

    /**
     * Constructor.
     *
     * @param agent the {@link com.ionic.sdk.key.KeyServices} implementation
     * @param cid   the conversation id associated with the client request
     */
    public MessageBase(final Agent agent, final String cid) {
        this.agent = agent;
        this.cid = cid;
    }

    /**
     * @return the KeyServices implementation
     */
    public final Agent getAgent() {
        return agent;
    }

    /**
     * @return the conversation id associated with the client request
     */
    public final String getCid() {
        return cid;
    }

    /**
     * Assemble the json associated with the request.  This json will be securely wrapped in a plaintext request to be
     * sent to the server.
     *
     * @param requestBase the user-generated object containing the attributes of the request
     * @param fingerprint authentication data associated with the client state to be included in the request
     * @return a {@link JsonObject} to be incorporated into the request payload
     * @throws IonicException on errors
     */
    public final JsonObject getJsonMessage(
            final AgentRequestBase requestBase, final Properties fingerprint) throws IonicException {
        return Json.createObjectBuilder()
                .add(IDC.Payload.DATA, getJsonData(requestBase))
                .add(IDC.Payload.META, AgentTransactionUtil.buildStandardJsonMeta(agent, requestBase, fingerprint))
                .build();
    }

    /**
     * Calculate the signature associated with the request attributes.
     * <p>
     * a2\IonicAgents\SDK\ISAgentSDK\ISAgentLib\ISAgentTransactionUtil.cpp:buildSignedAttributes()
     *
     * @param keyId      the identifier for the key used in the signature calculation
     * @param extra      additional data used in the signature calculation
     * @param attrs      the attributes to be associated with any keys generated as part of this request
     * @param areMutable a signal used to construct appropriate authData to use in signature
     * @return a signature to be incorporated into the request payload (for verification)
     * @throws IonicException on cryptography errors
     */
    protected final String buildSignedAttributes(final String keyId, final String extra,
                                                 final String attrs, final boolean areMutable) throws IonicException {
        final AesGcmCipher cipher = new AesGcmCipher();
        cipher.setKey(agent.getActiveProfile().getAesCdEiProfileKey());
        final String authData = areMutable
                ? Value.join(IDC.Signature.DELIMITER, cid, IDC.Signature.MUTABLE, keyId, extra)
                : Value.join(IDC.Signature.DELIMITER, cid, keyId, extra);
        cipher.setAuthData(Transcoder.utf8().decode(authData));
        final byte[] plainText = new Hash().sha256(Transcoder.utf8().decode(attrs));
        return cipher.encryptToBase64(plainText);
    }

    /**
     * Verify the signature provided in the response against a locally generated signature.
     *
     * @param name        the attribute whose value should be checked
     * @param sigExpected the (server-provided) value
     * @param attrs       the source material for the signature
     * @param key         the key to use in calculating the signature
     * @throws IonicException on cryptography errors
     */
    public final void verifySignature(
            final String name, final String sigExpected, final String attrs, final byte[] key) throws IonicException {
        if (sigExpected != null) {
            final String sigActual = CryptoUtils.hmacSHA256Base64(Transcoder.utf8().decode(attrs), key);
            if (!sigExpected.equals(sigActual)) {
                throw new IonicException(AgentErrorModuleConstants.ISAGENT_INVALIDVALUE.value(),
                        new GeneralSecurityException(String.format("%s:[%s]!=[%s]", name, sigExpected, sigActual)));
            }
        }
    }

    /**
     * Assemble the "data" json associated with the request.
     *
     * @param requestBase the user-generated object containing the attributes of the request
     * @return a {@link JsonObject} to be incorporated into the request payload
     * @throws IonicException on errors
     */
    protected abstract JsonObject getJsonData(final AgentRequestBase requestBase) throws IonicException;
}
