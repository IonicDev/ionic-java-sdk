package com.ionic.sdk.agent.cipher.file.data;

import com.ionic.sdk.agent.cipher.data.DecryptAttributes;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

/**
 * On an Ionic Machina Tools decrypt operation, this class provides the ability to exchange information with the
 * Machina server about the operation.
 * <p>
 * Request metadata may be sent to the server along with the cryptography key request.  This may specify information
 * about the client making the request.  See <a href='https://dev.ionic.com/sdk/tasks/set-request-metadata'
 * target='_blank'>Set
 * Request Metadata</a> for more information.
 * <p>
 * The cryptography key used for the decryption might have associated key attributes (fixed and mutable).  These are
 * used to indicate metadata that is associated with the encryption.  For example, a 'classification' attribute may
 * indicate the desired security level of the encrypted data.  The Machina key release policy may then be configured
 * to allow data access only to the devices of designated individuals and groups.  This
 * information is communicated back to the SDK caller in this object.  See
 * <a href='https://dev.ionic.com/sdk/tasks/create-key-with-fixed-attributes' target='_blank'>Fixed Attributes</a> and
 * <a href='https://dev.ionic.com/sdk/tasks/create-key-with-mutable-attributes'
 * target='_blank'>Mutable Attributes</a> for more
 * information.
 * <p>
 * The usage pattern for this object is to supply a fresh instance to each API call.  The Machina Tools SDK will reject
 * any <code>decrypt()</code> operation that is attempted using a previously used {@link FileCryptoDecryptAttributes}.
 * <p>
 * See <a href='https://dev.ionic.com/sdk/features' target='_blank'>Machina Developers</a> for
 * more information on Machina file encryption.
 */
public final class FileCryptoDecryptAttributes extends DecryptAttributes {

    /**
     * The identifier for the Ionic file cipher family used in the decryption operation.
     */
    private CipherFamily cipherFamily;

    /**
     * The identifier for the Ionic file cipher family version used in the decryption operation.
     */
    private String version;

    /**
     * Value indicating whether denied page object should be populated if an access denied error is encountered.
     */
    private boolean shouldProvideAccessDeniedPage;

    /**
     * The serialized byte stream to be supplied to the caller when a key request is denied.
     */
    private byte[] accessDeniedPage;

    /**
     * Constructor.
     */
    public FileCryptoDecryptAttributes() {
        this.cipherFamily = CipherFamily.FAMILY_UNKNOWN;
        this.version = "";
        this.shouldProvideAccessDeniedPage = false;
        this.accessDeniedPage = null;
    }

    /**
     * Set the version of the file cipher used during the decryption operation.
     * <p>
     * Ionic SDK clients should not call this function.  Any value set prior to the decryption operation will cause
     * an {@link com.ionic.sdk.error.IonicException} to be thrown by the operation.
     *
     * @param version the Ionic file cipher family version used in the decryption operation
     */
    @InternalUseOnly
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * @return the Ionic file cipher family version used in the decryption operation
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the CipherFamily associated with the decryption.
     * <p>
     * Ionic SDK clients should not call this function.  Any value set prior to the decryption operation will cause
     * an {@link com.ionic.sdk.error.IonicException} to be thrown by the operation.
     *
     * @param cipherFamily the file family to be used in the decryption operation
     */
    @InternalUseOnly
    public void setFamily(final CipherFamily cipherFamily) {
        this.cipherFamily = cipherFamily;
    }

    /**
     * @return the file family to be used in the decryption operation
     */
    public CipherFamily getFamily() {
        return cipherFamily;
    }

    /**
     * Get whether access denied page object should be populated in the event that an access denied error is
     * encountered.
     *
     * @return access denied page enablement option
     */
    public boolean shouldProvideAccessDeniedPage() {
        return shouldProvideAccessDeniedPage;
    }

    /**
     * Indicate whether access denied page object should be populated in the event that
     * an access denied error is encountered.
     *
     * @param state access denied page enablement option
     */
    public void setShouldProvideAccessDeniedPage(final boolean state) {
        shouldProvideAccessDeniedPage = state;
    }

    /**
     * Get the byte vector containing the access denied page for the appropriate document format.
     *
     * @return access denied page bytes
     */
    public byte[] getAccessDeniedPageOut() {
        return (accessDeniedPage == null) ? null : accessDeniedPage.clone();
    }

    /**
     * Set the byte vector containing the access denied page for the appropriate document format.
     *
     * @param accessDeniedPage access denied page bytes
     */
    public void setAccessDeniedPageOut(final byte[] accessDeniedPage) {
        this.accessDeniedPage = (accessDeniedPage == null) ? null : accessDeniedPage.clone();
    }

    /**
     * Verify that object is in the expected state prior to the Ionic server key request.
     * <p>
     * The usage pattern for this object is to supply a fresh instance to each SDK cryptography API call.  The
     * Ionic SDK will reject an operation that is attempted using a previously used instance of this class.
     *
     * @throws IonicException on expectation failure
     */
    public void validateInput() throws IonicException {
        final String className = getClass().getName();
        SdkData.checkTrue(CipherFamily.FAMILY_UNKNOWN.equals(cipherFamily), SdkError.ISAGENT_INVALIDVALUE, className);
        SdkData.checkTrue("".equals(version), SdkError.ISAGENT_INVALIDVALUE, className);
        SdkData.checkTrue(null == getKeyResponse(), SdkError.ISAGENT_INVALIDVALUE, className);
        SdkData.checkTrue(null == getServerErrorResponse(), SdkError.ISAGENT_INVALIDVALUE, className);
    }
}
