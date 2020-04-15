package com.ionic.sdk.agent.cipher.chunk.data;

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
 * any <code>decrypt()</code> operation that is attempted using a previously used {@link ChunkCryptoDecryptAttributes}.
 * <p>
 * See <a href='https://dev.ionic.com/sdk/formats/chunk' target='_blank'>Machina Developers</a> for more information
 * on Machina data chunk encryption.
 */
public class ChunkCryptoDecryptAttributes extends DecryptAttributes {

    /**
     * The identifier for the cipher used in the decryption operation.
     */
    private String cipherId;

    /**
     * Constructor.
     */
    public ChunkCryptoDecryptAttributes() {
        this.cipherId = "";
    }

    /**
     * @return the identifier the Ionic chunk cipher implementation used in the decryption operation
     */
    public final String getCipherId() {
        return cipherId;
    }

    /**
     * Set the cipher identifier associated with the decryption.
     * <p>
     * Ionic SDK clients should not call this function.  Any value set prior to the decryption operation will cause
     * an {@link com.ionic.sdk.error.IonicException} to be thrown by the operation.
     *
     * @param cipherId the Ionic chunk cipher identifier associated with the decryption
     */
    @InternalUseOnly
    public final void setCipherId(final String cipherId) {
        this.cipherId = cipherId;
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
