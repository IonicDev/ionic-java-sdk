package com.ionic.sdk.cipher.aes.model;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Class encapsulating capability to generate symmetric cryptography keys.
 */
public class AesKeyGenerator {

    /**
     * Constructor.
     *
     * @throws IonicException on failure of platform preconditions for use of Ionic APIs.
     */
    public AesKeyGenerator() throws IonicException {
        AgentSdk.initialize();
    }

    /**
     * Creates a new cryptographic key.
     *
     * @return a {@link AesKeyHolder} object containing the newly created key
     * @throws IonicException on cryptography errors
     */
    public final AesKeyHolder generate() throws IonicException {
        try {
            final KeyGenerator keygenSymmetric = KeyGenerator.getInstance(AesCipher.ALGORITHM);
            keygenSymmetric.init(AesCipher.KEY_BITS);
            return new AesKeyHolder(keygenSymmetric.generateKey());
        } catch (NoSuchAlgorithmException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }
}
