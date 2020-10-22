package com.ionic.sdk.agent.cipher.binary;

import com.ionic.sdk.agent.cipher.data.DecryptAttributes;
import com.ionic.sdk.agent.cipher.data.EncryptAttributes;
import com.ionic.sdk.agent.key.AgentKey;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;

import java.nio.ByteBuffer;

/**
 * Cipher implementation specialized for dealing with binary data.  As with
 * {@link com.ionic.sdk.agent.cipher.chunk.ChunkCipherAbstract} implementations, the Machina key tag is incorporated
 * into the output ciphertext.  This makes the plaintext recoverable, given only the ciphertext as input.
 * <p>
 * The size requirement for a {@link BinaryCipherAesGcm} ciphertext is equal to the size of the input plaintext, plus
 * an additional 49 bytes:
 * <ul>
 * <li>11 bytes to hold the Machina key tag</li>
 * <li>2 bytes to hold a key tag delimiter byte, and a version indicator byte</li>
 * <li>4 bytes to hold a 32-bit integer, indicating the ciphertext data length</li>
 * <li>16 bytes to hold a 128-bit initialization vector (IV) for the cryptography operation</li>
 * <li>16 bytes to hold a 128-bit authentication data (ADATA) as an integrity check for the cryptography operation</li>
 * </ul>
 * <p>
 * <code>{KEYTAG}{DELIMITER}{VERSION}{DATA_LENGTH}{IV}{CIPHERTEXT}{ATAG}</code>
 */
public class BinaryCipherAesGcm extends BinaryCipherAbstract {

    /**
     * Constructor.
     *
     * @param keyServices the key services implementation
     */
    public BinaryCipherAesGcm(final KeyServices keyServices) {
        super(keyServices);
    }

    @Override
    protected final byte[] encryptInternal(final ByteBuffer plainBuffer, final EncryptAttributes encryptAttributes)
            throws IonicException {
        final AgentKey key = encryptAttributes.getKeyResponse();
        // {KEYTAG}{DELIMITER}{VERSION}{DATA_LENGTH}
        final int countHeader = key.getId().length() + 1 + 1 + (Integer.SIZE / Byte.SIZE);
        final int countBody = AesCipher.SIZE_IV + plainBuffer.capacity() + AesCipher.SIZE_ATAG;  // AES-GCM
        final byte[] cipherText = new byte[countHeader + countBody];
        final ByteBuffer cipherBuffer = ByteBuffer.wrap(cipherText);
        // write ciphertext header
        cipherBuffer.put(Transcoder.utf8().decode(key.getId()));
        cipherBuffer.put(Transcoder.utf8().decode(DELIMITER));
        cipherBuffer.put(Transcoder.utf8().decode(VERSION_2));
        cipherBuffer.putInt(countBody);  // AES-GCM
        // perform cryptography
        final AesGcmCipher cipher = new AesGcmCipher(key.getKey());
        cipher.setAuthData(Transcoder.utf8().decode(key.getId()));
        cipher.setMetadata(encryptAttributes.getMetadata());
        final int count = cipher.encrypt(plainBuffer, cipherBuffer);
        SdkData.checkTrue(cipherText.length == (countHeader + count), SdkError.ISAGENT_ERROR);
        return cipherText;
    }

    @Override
    protected final byte[] decryptInternal(final ByteBuffer cipherBuffer, final DecryptAttributes decryptAttributes)
            throws IonicException {
        final AgentKey key = decryptAttributes.getKeyResponse();
        // check input
        final byte[] versionExpected = Transcoder.utf8().decode(VERSION_2);
        final byte version = cipherBuffer.get();
        SdkData.checkTrue(version == versionExpected[0], SdkError.ISAGENT_INVALIDVALUE);
        final int lengthData = cipherBuffer.getInt();
        final byte[] plainText = new byte[lengthData - AesCipher.SIZE_IV - AesCipher.SIZE_ATAG];
        final ByteBuffer plainBuffer = ByteBuffer.wrap(plainText);
        // perform cryptography
        final AesGcmCipher cipher = new AesGcmCipher(key.getSecretKey());
        cipher.setAuthData(Transcoder.utf8().decode(key.getId()));
        final int count = cipher.decrypt(plainBuffer, cipherBuffer);
        SdkData.checkTrue(plainText.length == count, SdkError.ISAGENT_ERROR);
        return plainText;
    }

    /**
     * The version indicator for this cipher type.
     */
    private static final String VERSION_2 = "2";
}
