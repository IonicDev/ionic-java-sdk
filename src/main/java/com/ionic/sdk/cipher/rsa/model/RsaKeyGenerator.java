package com.ionic.sdk.cipher.rsa.model;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.cipher.rsa.RsaCipher;
import com.ionic.sdk.error.CryptoErrorModuleConstants;
import com.ionic.sdk.error.IonicException;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Class encapsulating capability to generate asymmetric cryptography keypairs.
 */
public final class RsaKeyGenerator {

    /**
     * Creates a new cryptographic keypair.
     *
     * @param keySize length in bits of the RSA keys
     * @return a {@link RsaKeyHolder} object containing the newly created keypair
     * @throws IonicException on cryptography errors
     */
    public RsaKeyHolder generate(final int keySize) throws IonicException {
        try {
            AgentSdk.initialize(null);
            final KeyPairGenerator keygen = KeyPairGenerator.getInstance(RsaCipher.ALGORITHM);
            keygen.initialize(keySize);
            return new RsaKeyHolder(keygen.generateKeyPair());
        } catch (NoSuchAlgorithmException e) {
            throw new IonicException(CryptoErrorModuleConstants.ISCRYPTO_ERROR.value(), e);
        }
    }
}
