package com.ionic.sdk.agent.cipher.chunk.data;

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
 * any <code>encrypt()</code> operation that is attempted using a previously used {@link ChunkCryptoEncryptAttributes}.
 * <p>
 * See <a href='https://dev.ionic.com/sdk/formats/chunk' target='_blank'>Machina Developers</a> for more information
 * on Machina data chunk encryption.
 */
public final class ChunkCryptoEncryptAttributes extends EncryptAttributes {

    /**
     * The identifier for the Ionic chunk cipher implementation used in the encryption operation.
     */
    private String cipherId;

    /**
     * Constructor.
     */
    public ChunkCryptoEncryptAttributes() {
        this(null, null);
    }

    /**
     * Constructor.
     *
     * @param keyAttributes the fixed attributes to be associated with the newly created key at the Ionic server
     */
    public ChunkCryptoEncryptAttributes(final KeyAttributesMap keyAttributes) {
        this(keyAttributes, null);
    }

    /**
     * Constructor.
     *
     * @param keyAttributes     the fixed attributes to be associated with the newly created key at the Ionic server
     * @param mutableAttributes the mutable attributes to be associated with the newly created key at the Ionic server
     */
    public ChunkCryptoEncryptAttributes(
            final KeyAttributesMap keyAttributes, final KeyAttributesMap mutableAttributes) {
        super.setKeyAttributes(keyAttributes);
        super.setMutableKeyAttributes(mutableAttributes);
        this.cipherId = "";
    }

    /**
     * Constructor.
     * <p>
     * The usage pattern for this object is to supply a fresh instance to each SDK cryptography API call.  This
     * constructor may be used to copy the user-settable fields from a previously configured attributes object.
     *
     * @param encryptAttributes attributes from a previous cryptography operation
     */
    public ChunkCryptoEncryptAttributes(final ChunkCryptoEncryptAttributes encryptAttributes) {
        this(encryptAttributes.getKeyAttributes(), encryptAttributes.getMutableKeyAttributes());
        super.setMetadata(encryptAttributes.getMetadata());
    }

    /**
     * Set the identifier for the Ionic chunk cipher used in the encryption operation.
     * <p>
     * Ionic SDK clients should not call this function.  Any value set prior to the encryption operation will cause
     * an {@link com.ionic.sdk.error.IonicException} to be thrown by the operation.
     *
     * @param cipherId the identifier for the cipher used in the encryption operation
     */
    @InternalUseOnly
    public void setCipherId(final String cipherId) {
        this.cipherId = cipherId;
    }

    /**
     * @return the identifier the Ionic chunk cipher implementation used in the encryption operation
     */
    public String getCipherId() {
        return cipherId;
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
        SdkData.checkTrue("".equals(cipherId), SdkError.ISAGENT_INVALIDVALUE, className);
        SdkData.checkTrue(null == getKeyResponse(), SdkError.ISAGENT_INVALIDVALUE, className);
        SdkData.checkTrue(null == getServerErrorResponse(), SdkError.ISAGENT_INVALIDVALUE, className);
    }
}
