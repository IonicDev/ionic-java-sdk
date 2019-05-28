/**
 * Implementations of Ionic raw ciphers.
 * <p>
 * These combine usage of:
 * <ul>
 * <li>a {@link com.ionic.sdk.key.KeyServices} implementation, which brokers cryptography key creates and fetches</li>
 * <li>a {@link com.ionic.sdk.cipher.aes.AesCipherAbstract} implementation, which wraps the underlying JCE cipher</li>
 * </ul>
 * <p>
 * The abstract classes {@link com.ionic.sdk.agent.cipher.chunk.ChunkCipherAbstract} and
 * {@link com.ionic.sdk.agent.cipher.file.FileCipherAbstract} have implementations which encode the Ionic cryptography
 * key ID and encryption metadata into the resulting ciphertext, facilitating the later retrieval of the key on
 * decryption.  Raw ciphers do not associate the key ID and metadata with the ciphertext.  This allows Ionic users to
 * define alternative means of persisting this data, either together or separately, in order to fit the needs of a
 * custom use case.
 */
package com.ionic.sdk.agent.cipher.raw;
