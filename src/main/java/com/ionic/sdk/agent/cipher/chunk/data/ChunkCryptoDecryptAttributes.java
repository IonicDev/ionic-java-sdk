package com.ionic.sdk.agent.cipher.chunk.data;

import com.ionic.sdk.agent.cipher.data.DecryptAttributes;
import com.ionic.sdk.core.annotation.InternalUseOnly;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

/**
 * On an Ionic SDK decrypt operation, this class provides the ability to specify request metadata that should be
 * sent to the server along with the cryptography key request.
 * <p>
 * The key used for the decryption might have associated cryptography key attributes (fixed and mutable).  This
 * information is communicated back to the SDK caller in this object.
 * <p>
 * The usage pattern for this object is to supply a fresh instance to each API call.  The Ionic SDK will reject
 * any <code>decrypt()</code> operation that is attempted using a previously used {@link ChunkCryptoDecryptAttributes}.
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
