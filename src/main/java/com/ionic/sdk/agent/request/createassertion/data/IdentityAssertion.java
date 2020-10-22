package com.ionic.sdk.agent.request.createassertion.data;

import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Container for data associated with a Machina-generated Identity Assertion.
 * <p>
 * Identity assertions typically have a short validity period (in the range of a few minutes), so any consuming
 * workflows must be designed with this in mind.
 */
public class IdentityAssertion {

    /**
     * The base64-wrapped source data for the assertion.
     */
    private final String base64;

    /**
     * Free-form data attributes associated with the assertion.
     */
    private final TreeMap<String, String> attributes;

    /**
     * The identifier for the assertion.
     */
    private final String id;

    /**
     * The starting validity timestamp for the assertion.
     */
    private final String validAfter;

    /**
     * The ending validity timestamp for the assertion.
     */
    private final String validUntil;

    /**
     * The intended consumer of the assertion.
     */
    private final String recipient;

    /**
     * The name of the entity performing the signing operation.
     */
    private final String signer;

    /**
     * The SHA-256 digest of the canonical form of the assertion.
     */
    private final String signatureB64;

    /**
     * Constructor.
     *
     * @param assertionBase64 the assertion data to be parsed
     * @throws IonicException on invalid input data (JSON parse, expected content)
     * @see com.ionic.sdk.agent.request.createassertion.CreateIdentityAssertionResponse
     */
    public IdentityAssertion(final String assertionBase64) throws IonicException {
        SdkData.checkTrue(!Value.isEmpty(assertionBase64), SdkError.ISAGENT_MISSINGVALUE, IDC.Payload.ASSERTION);
        this.base64 = assertionBase64;
        this.attributes = new TreeMap<String, String>();
        final JsonObject jsonPayload = JsonIO.readObject(Transcoder.base64().decode(assertionBase64));
        final JsonObject jsonAttributes = JsonSource.getJsonObject(jsonPayload, IDC.IdentityAssertion.ATTRIBUTES);
        final Iterator<Map.Entry<String, JsonValue>> iterator = JsonSource.getIterator(jsonAttributes);
        while (iterator.hasNext()) {
            final Map.Entry<String, JsonValue> entry = iterator.next();
            attributes.put(entry.getKey(), JsonSource.toString(entry.getValue()));
        }
        this.id = JsonSource.getString(jsonPayload, IDC.IdentityAssertion.ID);
        this.validAfter = JsonSource.getString(jsonPayload, IDC.IdentityAssertion.VALID_AFTER);
        this.validUntil = JsonSource.getString(jsonPayload, IDC.IdentityAssertion.VALID_UNTIL);
        this.recipient = JsonSource.getString(jsonPayload, IDC.IdentityAssertion.RECIPIENT);
        this.signer = JsonSource.getString(jsonPayload, IDC.IdentityAssertion.SIGNER);
        this.signatureB64 = JsonSource.getString(jsonPayload, IDC.IdentityAssertion.SIGNATURE_B64);
    }

    /**
     * @return the free-form data attributes associated with the assertion
     */
    public TreeMap<String, String> getAttributes() {
        return attributes;
    }

    /**
     * @return the base64 wrapper containing the assertion information
     */
    public String getBase64() {
        return base64;
    }

    /**
     * @return the identifier for the assertion
     */
    public String getId() {
        return id;
    }

    /**
     * @return the starting validity timestamp for the assertion
     */
    public String getValidAfter() {
        return validAfter;
    }

    /**
     * @return the ending validity timestamp for the assertion
     */
    public String getValidUntil() {
        return validUntil;
    }

    /**
     * @return the intended consumer of the assertion
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * @return the name of the entity performing the signing operation
     */
    public String getSigner() {
        return signer;
    }

    /**
     * @return the SHA-256 digest of the canonical form of the assertion
     */
    public String getSignatureB64() {
        return signatureB64;
    }

    /**
     * Assemble the canonical form of the assertion token.  This form is used as the input to generate
     * the assertion signature.
     *
     * @param nonce the single-use token incorporated into the canonical form / digest of the assertion
     * @return the byte[] representation of the assertion
     */
    public byte[] toCanonical(final String nonce) {
        final String nonceUse = (Value.isEmpty(nonce) ? DEFAULT_MFA : nonce);
        final StringBuilder buffer = new StringBuilder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            buffer.append(entry.getKey()).append(DELIMITER);
            buffer.append(entry.getValue()).append(DELIMITER);
        }
        buffer.append(id).append(DELIMITER);
        buffer.append(signer).append(DELIMITER);
        buffer.append(validAfter).append(DELIMITER);
        buffer.append(validUntil).append(DELIMITER);
        buffer.append(recipient).append(DELIMITER);
        buffer.append(nonceUse);
        return Transcoder.utf8().decode(buffer.toString());
    }

    /**
     * Delimiter for canonical identity assertion data value.
     */
    private static final char DELIMITER = (char) 0x1f;

    /**
     * Default Identity Assertion nonce, to be used when one is not provided.
     */
    public static final String DEFAULT_MFA = "ionic";
}
