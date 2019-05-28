package com.ionic.sdk.agent.cipher.chunk.data;

import com.ionic.sdk.agent.cipher.data.EncryptAttributes;
import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

/**
 * On an Ionic SDK encrypt operation, this class provides the ability to specify cryptography key attributes (fixed
 * and mutable) and request metadata that should be sent to the server along with the cryptography key request.
 * <p>
 * The usage pattern for this object is to supply a fresh instance to each API call. The Ionic SDK will reject any
 * <code>encrypt()</code> operation that is attempted using a previously used {@link ChunkCryptoEncryptAttributes}.
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
