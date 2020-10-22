package com.ionic.sdk.agent.request.createassertion;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.agent.request.createassertion.data.IdentityAssertion;
import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.cipher.rsa.model.RsaKeyHolder;
import com.ionic.sdk.cipher.rsa.model.RsaKeyPersistor;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.date.DateTime8601;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Date;

/**
 * Utility class to perform validation operation on a Machina Identity Assertion.
 * <p>
 * If a nonce was specified in the original {@link CreateIdentityAssertionRequest}, then the same nonce must be
 * provided to the user of this class.  The
 * result of the {@link CreateIdentityAssertionTransaction} cannot be validated without the same nonce.
 */
public class IdentityAssertionValidator {

    /**
     * The public key component of the RSA keypair used to generate the identity assertion.  If this value
     * is supplied, it should be in the format presented by the Machina service at the URL of the keyspace
     * public key.
     */
    private final String pubkeyBase64;

    /**
     * Constructor.
     *
     * @param pubkeyBase64 the public key component of the RSA keypair used to generate the identity assertion
     */
    public IdentityAssertionValidator(final String pubkeyBase64) {
        this.pubkeyBase64 = pubkeyBase64;
    }

    /**
     * Check the validity of a Machina Identity Assertion.
     * <p>
     * If a nonce was specified in the original {@link CreateIdentityAssertionRequest}, then the same nonce must be
     * provided to the user of this class.  The
     * result of the {@link CreateIdentityAssertionTransaction} cannot be validated without the same nonce.
     *
     * @param assertionBase64 the container for the supplied Identity Assertion data
     * @param date            the date which should be used for the Assertion validity period check; if
     *                        null, the current time is used
     * @param uri             the intended consumer of the assertion; if null, this assertion data is not checked
     * @return an container object holding the data from the unwrapped assertionBase64 input
     * @throws IonicException on invalid input data; data validity failures
     */
    public IdentityAssertion validate(final String assertionBase64,
                                      final Date date, final String uri) throws IonicException {
        return validateInternal(assertionBase64, date, uri, null);
    }

    /**
     * Check the validity of a Machina Identity Assertion.
     * <p>
     * If a nonce was specified in the original {@link CreateIdentityAssertionRequest}, then the same nonce must be
     * provided to the user of this class.  The
     * result of the {@link CreateIdentityAssertionTransaction} cannot be validated without the same nonce.
     *
     * @param assertionBase64 the container for the supplied Identity Assertion data
     * @param date            the date which should be used for the Assertion validity period check; if
     *                        null, the current time is used
     * @param uri             the intended consumer of the assertion; if null, this assertion data is not checked
     * @param nonce           the single-use token incorporated into the canonical form / digest of the assertion
     * @return an container object holding the data from the unwrapped assertionBase64 input
     * @throws IonicException on invalid input data; data validity failures
     */
    public IdentityAssertion validate(final String assertionBase64, final Date date,
                                      final String uri, final String nonce) throws IonicException {
        return validateInternal(assertionBase64, date, uri, nonce);
    }

    /**
     * Check the validity of a Machina Identity Assertion.
     *
     * @param assertionBase64 the container for the supplied Identity Assertion data
     * @param date            the date which should be used for the Assertion validity period check; if
     *                        null, the current time is used
     * @param uri             the intended consumer of the assertion; if null, this assertion data is not checked
     * @param nonce           the single-use token incorporated into the canonical form / digest of the assertion
     * @return an container object holding the data from the unwrapped assertionBase64 input
     * @throws IonicException on invalid input data; data validity failures
     */
    private IdentityAssertion validateInternal(final String assertionBase64, final Date date,
                                               final String uri, final String nonce) throws IonicException {
        // default arguments
        final Date dateUse = (date == null) ? new Date() : date;
        final String nonceUse = (Value.isEmpty(nonce) ? IdentityAssertion.DEFAULT_MFA : nonce);
        // deserialize JSON
        SdkData.checkTrue(!Value.isEmpty(assertionBase64), SdkError.ISAGENT_MISSINGVALUE, IDC.Payload.ASSERTION);
        final IdentityAssertion assertion = new IdentityAssertion(assertionBase64);
        final String deviceID = assertion.getAttributes().get(IDC.IdentityAssertion.DEVICE_ID);
        SdkData.checkTrue(!Value.isEmpty(deviceID), SdkError.ISAGENT_MISSINGVALUE, IDC.IdentityAssertion.DEVICE_ID);
        final String assertionVersion = assertion.getAttributes().get(IDC.IdentityAssertion.ASSERTION_VERSION);
        SdkData.checkTrue(VALUE_VERSION_EXPECTED.equals(assertionVersion),
                SdkError.ISAGENT_UNEXPECTEDRESPONSE, IDC.IdentityAssertion.ASSERTION_VERSION);
        // validate assertion date range
        final String validAfter = assertion.getValidAfter();
        final String validUntil = assertion.getValidUntil();
        SdkData.checkTrue(!Value.isEmpty(validAfter), SdkError.ISAGENT_MISSINGVALUE, IDC.IdentityAssertion.VALID_AFTER);
        SdkData.checkTrue(!Value.isEmpty(validUntil), SdkError.ISAGENT_MISSINGVALUE, IDC.IdentityAssertion.VALID_UNTIL);
        final Date dateFrom = DateTime8601.fromString(validAfter);
        final Date dateTo = DateTime8601.fromString(validUntil);
        SdkData.checkTrue(dateFrom.before(dateUse), SdkError.ISAGENT_INVALIDVALUE,
                String.format("ASSERTION NOT YET VALID: [%s] BEFORE [%s]",
                        DateTime8601.toString(dateUse), DateTime8601.toString(dateFrom)));
        SdkData.checkTrue(dateTo.after(dateUse), SdkError.ISAGENT_INVALIDVALUE,
                String.format("ASSERTION EXPIRED: [%s] AFTER [%s]",
                        DateTime8601.toString(dateUse), DateTime8601.toString(dateTo)));
        // validate uri
        if (uri != null) {
            final String recipient = assertion.getRecipient();
            SdkData.checkTrue(!Value.isEmpty(recipient),
                    SdkError.ISAGENT_MISSINGVALUE, IDC.IdentityAssertion.RECIPIENT);
            SdkData.checkTrue(recipient.contains(uri), SdkError.ISAGENT_INVALIDVALUE, uri);
        }
        // assemble public key (used to validate signature)
        final RsaKeyHolder rsaKeyHolder = new RsaKeyPersistor().fromBase64(pubkeyBase64, null);
        // validate cryptographic signature
        try {
            final byte[] signatureAssertion = Transcoder.base64().decode(assertion.getSignatureB64());
            final Signature signature = AgentSdk.getCrypto().getSignatureRsa();
            signature.initVerify(rsaKeyHolder.getPublicKey());
            signature.update(assertion.toCanonical(nonceUse));
            final boolean verified = signature.verify(signatureAssertion);
            SdkData.checkTrue(verified, SdkError.ISCRYPTO_BAD_SIGNATURE, IDC.IdentityAssertion.SIGNATURE_B64);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_BAD_SIGNATURE, e);
        }
        return assertion;
    }

    /**
     * Identity Assertion data version zero.
     */
    public static final String VALUE_VERSION_EXPECTED = "0";
}
