package com.ionic.sdk.agent.cipher.file.data;

import com.ionic.sdk.agent.cipher.data.EncryptAttributes;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

/**
 * On an Ionic Machina Tools encrypt operation, this class provides the ability to exchange information with the
 * Machina server about the operation.
 * <p>
 * Request metadata may be sent to the server along with the cryptography key request.  This may specify information
 * about the client making the request.  See <a href='https://dev.ionic.com/sdk/tasks/set-request-metadata'
 * target='_blank'>Set
 * Request Metadata</a> for more information.
 * <p>
 * The cryptography key used for the encryption might have associated key attributes (fixed and mutable).  These are
 * used to indicate metadata that is associated with the encryption.  For example, a 'classification' attribute may
 * indicate the desired security level of the encrypted data.  The Machina key release policy may then be configured
 * to allow data access only to the devices of designated individuals and groups.  This
 * information is communicated from the SDK caller to Machina in this object.  See
 * <a href='https://dev.ionic.com/sdk/tasks/create-key-with-fixed-attributes' target='_blank'>Fixed Attributes</a> and
 * <a href='https://dev.ionic.com/sdk/tasks/create-key-with-mutable-attributes'
 * target='_blank'>Mutable Attributes</a> for more
 * information.
 * <p>
 * The usage pattern for this object is to supply a fresh instance to each API call.  The Machina Tools SDK will reject
 * any <code>encrypt()</code> operation that is attempted using a previously used {@link FileCryptoEncryptAttributes}.
 * <p>
 * See <a href='https://dev.ionic.com/sdk/features' target='_blank'>Machina Developers</a> for
 * more information on Machina file encryption.
 */
public final class FileCryptoEncryptAttributes extends EncryptAttributes {

    /**
     * The identifier for the Ionic file cipher family to be used in the encryption operation.
     */
    private CipherFamily cipherFamily;

    /**
     * The identifier for the Ionic file cipher family version to be used in the encryption operation.
     */
    private String version;

    /**
     * Indicates whether an OpenXML file should copy the /docProps/custom.xml file into the cover page.
     * By default, this is set to true.
     */
    private boolean bShouldCopyCustomProps;

    /**
     * Constructor.
     */
    public FileCryptoEncryptAttributes() {
        this("", null, null);
    }

    /**
     * Constructor.
     *
     * @param version the Ionic file cipher family version to be used in the encryption operation
     */
    public FileCryptoEncryptAttributes(final String version) {
        this(version, null, null);
    }

    /**
     * Constructor.
     *
     * @param keyAttributes the fixed attributes to be associated with the newly created key at the Ionic server
     */
    public FileCryptoEncryptAttributes(final KeyAttributesMap keyAttributes) {
        this("", keyAttributes, null);
    }

    /**
     * Constructor.
     *
     * @param keyAttributes     the fixed attributes to be associated with the newly created key at the Ionic server
     * @param mutableAttributes the mutable attributes to be associated with the newly created key at the Ionic server
     */
    public FileCryptoEncryptAttributes(final KeyAttributesMap keyAttributes, final KeyAttributesMap mutableAttributes) {
        this("", keyAttributes, mutableAttributes);
    }

    /**
     * Constructor.
     *
     * @param version       the Ionic file cipher family version to be used in the encryption operation
     * @param keyAttributes the fixed attributes to be associated with the newly created key at the Ionic server
     */
    public FileCryptoEncryptAttributes(final String version, final KeyAttributesMap keyAttributes) {
        this(version, keyAttributes, null);
    }

    /**
     * Constructor.
     *
     * @param version           the file family version to be used in the encryption operation
     * @param keyAttributes     the fixed attributes to be associated with the newly created key at the Ionic server
     * @param mutableAttributes the mutable attributes to be associated with the newly created key at the Ionic server
     */
    public FileCryptoEncryptAttributes(final String version, final KeyAttributesMap keyAttributes,
                                       final KeyAttributesMap mutableAttributes) {
        super.setKeyAttributes(keyAttributes);
        super.setMutableKeyAttributes(mutableAttributes);
        this.cipherFamily = CipherFamily.FAMILY_UNKNOWN;
        this.version = version;
        this.bShouldCopyCustomProps = true;
    }

    /**
     * Constructor.
     * <p>
     * The usage pattern for this object is to supply a fresh instance to each SDK cryptography API call.  This
     * constructor may be used to copy the user-settable fields from a previously configured attributes object.
     *
     * @param encryptAttributes attributes from a previous cryptography operation
     */
    public FileCryptoEncryptAttributes(final FileCryptoEncryptAttributes encryptAttributes) {
        this(encryptAttributes.getVersion(), encryptAttributes.getKeyAttributes(),
                encryptAttributes.getMutableKeyAttributes());
        super.setMetadata(encryptAttributes.getMetadata());
    }

    /**
     * Set the CipherFamily associated with the encryption.
     * <p>
     * Ionic SDK clients should not call this function.  Any value set prior to the encryption operation will cause
     * an {@link com.ionic.sdk.error.IonicException} to be thrown by the operation.
     *
     * @param cipherFamily the file family to be used in the encryption operation
     */
    @InternalUseOnly
    public void setFamily(final CipherFamily cipherFamily) {
        this.cipherFamily = cipherFamily;
    }

    /**
     * @return the Ionic file cipher family to be used in the encryption operation
     */
    public CipherFamily getFamily() {
        return cipherFamily;
    }

    /**
     * Set the version of the file cipher to be used during the encryption operation.
     *
     * @param version the Ionic file cipher family version to be used in the encryption operation
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * @return the Ionic file cipher family version to be used in the encryption operation
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set whether to copy the /docProps/custom.xml file into the cover page. Defaults to true.
     *
     * @param bShouldCopyCustomProps Whether to copy the /docProps/custom.xml file into the cover page.
     */
    public void setShouldCopyCustomProps(final boolean bShouldCopyCustomProps) {
        this.bShouldCopyCustomProps = bShouldCopyCustomProps;
    }

    /**
     * Get whether to copy the /docProps/custom.xml file into the cover page. Defaults to true.
     *
     * @return Whether to copy the /docProps/custom.xml file into the cover page.
     */
    public boolean getShouldCopyCustomProps() {
        return bShouldCopyCustomProps;
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
        SdkData.checkTrue(null == getKeyResponse(), SdkError.ISAGENT_INVALIDVALUE, className);
        SdkData.checkTrue(null == getServerErrorResponse(), SdkError.ISAGENT_INVALIDVALUE, className);
    }
}
