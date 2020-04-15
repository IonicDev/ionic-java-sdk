package com.ionic.sdk.crypto.pbkdf;

import com.ionic.sdk.agent.AgentSdk;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Implementation of Password-Based Key Derivation Function that is backward compatible with existing Ionic
 * PBKDF data.  Derived from Bouncy Castle implementation of PKCS5S2ParametersGenerator, which is compatible
 * with "cryptopp" implementation used by Ionic core SDK.
 */
public class IonicPbkdf2 {

    /**
     * Message authentication code utility from JCE provider used by Ionic SDK.
     */
    private final Mac hMac;

    /**
     * State maintained in context of key derivation.
     */
    private final byte[] state;

    /**
     * Input password used to generate secret key.
     */
    private final byte[] password;

    /**
     * Salt used to protect secret key.
     */
    private final byte[] salt;

    /**
     * Number of times that key derivation function should be run.
     */
    private final int iterationCount;

    /**
     * Desired size of resulting output.
     */
    private final int keyLength;

    /**
     * Constructor.
     *
     * @param password       input password used to generate secret key
     * @param salt           salt used to protect secret key
     * @param iterationCount number of times that key derivation function should be run
     * @param keyLength      desired size of resulting output
     * @throws IonicException on invalid input to the function
     */
    public IonicPbkdf2(final byte[] password, final byte[] salt,
                       final int iterationCount, final int keyLength) throws IonicException {
        SdkData.checkTrue(iterationCount > 0, SdkError.ISCRYPTO_BAD_INPUT);

        this.hMac = AgentSdk.getCrypto().getHmacSha256();
        this.state = new byte[hMac.getMacLength()];
        this.password = Arrays.copyOf(password, password.length);
        this.salt = (salt == null) ? null : Arrays.copyOf(salt, salt.length);
        this.iterationCount = iterationCount;
        this.keyLength = keyLength * Byte.SIZE;
    }

    /**
     * Generate the key using the derivation function with the specified parameters.
     *
     * @return the raw bytes of the generated key
     * @throws IonicException on cryptography operation failures
     */
    public byte[] generate() throws IonicException {
        hMac.reset();
        Arrays.fill(state, (byte) 0);
        try {
            final int keyBytes = keyLength / Byte.SIZE;
            return Arrays.copyOfRange(generateDerivedKey(keyBytes), 0, keyBytes);
        } catch (GeneralSecurityException e) {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, e);
        }
    }

    /**
     * The algorithm's key derivation function.
     *
     * @param var1 desired size (in bytes) of resulting output
     * @return the raw bytes of the generated key
     * @throws GeneralSecurityException on cryptography operation failures
     */
    private byte[] generateDerivedKey(final int var1) throws GeneralSecurityException {
        final int var2 = hMac.getMacLength();
        final int var3 = (var1 + var2 - 1) / var2;
        final int lengthVar4 = 4;
        final byte[] var4 = new byte[lengthVar4];
        final byte[] var5 = new byte[var3 * var2];
        int var6 = 0;

        // http://tutorials.jenkov.com/java-cryptography/mac.html
        final SecretKeySpec key = new SecretKeySpec(password, ALGORITHM_RAW_BYTES);
        hMac.init(key);

        for (int var8 = 1; var8 <= var3; ++var8) {
            final int initVar9 = 3;
            int var9 = initVar9;
            while (++var4[var9] == 0) {
                --var9;
            }

            f(salt, iterationCount, var4, var5, var6);
            var6 += var2;
        }

        return var5;
    }

    /**
     * The algorithm used to seed the HMAC with the password bytes.
     */
    private static final String ALGORITHM_RAW_BYTES = "RawBytes";

    /**
     * The algorithm's inner function.  This function is iterated over a number of times to protect the resulting key.
     *
     * @param var1 the salt applied to the function
     * @param var2 the iteration count specified for the function
     * @param var3 working state for the function
     * @param var4 working state for the function
     * @param var5 the buffer length of the working state
     * @throws ShortBufferException on failure supply enough room to receive the generated data
     */
    private void f(final byte[] var1, final int var2, final byte[] var3,
                   final byte[] var4, final int var5) throws ShortBufferException {
        if (var1 != null) {
            hMac.update(var1, 0, var1.length);
        }

        hMac.update(var3, 0, var3.length);
        hMac.doFinal(state, 0);
        System.arraycopy(state, 0, var4, var5, state.length);

        for (int var6 = 1; var6 < var2; ++var6) {
            hMac.update(state, 0, state.length);
            hMac.doFinal(state, 0);

            for (int var7 = 0; var7 != state.length; ++var7) {
                var4[var5 + var7] ^= state[var7];
            }
        }
    }
}
