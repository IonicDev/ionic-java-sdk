/**
 * Implementations of Ionic binary ciphers, which may be used to encrypt / decrypt chunks of binary data.
 *
 * These ciphers do not perform text encoding of the ciphertext, so the ciphertext transform incurs a fixed
 * additional storage requirement, depending on the selected cipher.  This is different from the
 * {@link com.ionic.sdk.agent.cipher.chunk.ChunkCipherAbstract} implementations, where the base64 encoding of the
 * ciphertext results in a 33% increase in the storage requirement.
 *
 * <ul>
 * <li>{@link com.ionic.sdk.agent.cipher.binary.BinaryCipherAesCtr} - length(ciphertext) = length(plaintext) + 33</li>
 * <li>{@link com.ionic.sdk.agent.cipher.binary.BinaryCipherAesGcm} - length(ciphertext) = length(plaintext) + 49</li>
 * </ul>
 */
package com.ionic.sdk.agent.cipher.binary;
