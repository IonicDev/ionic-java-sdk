package com.ionic.sdk.cipher.aes.model;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.error.CryptoErrorModuleConstants;
import com.ionic.sdk.error.IonicException;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Class encapsulating capability to generate symmetric cryptography keys.
 */
public class AesKeyGenerator {

    /**
     * Creates a new cryptographic key.
     *
     * @return a {@link AesKeyHolder} object containing the newly created key
     * @throws IonicException on cryptography errors
     */
    public final AesKeyHolder generate() throws IonicException {
        try {
            AgentSdk.initialize(null);
            final KeyGenerator keygenSymmetric = KeyGenerator.getInstance(AesCipher.ALGORITHM);
            keygenSymmetric.init(AesCipher.KEY_BITS);
            return new AesKeyHolder(keygenSymmetric.generateKey());
        } catch (NoSuchAlgorithmException e) {
            throw new IonicException(CryptoErrorModuleConstants.ISCRYPTO_ERROR.value(), e);
        }
    }
}
