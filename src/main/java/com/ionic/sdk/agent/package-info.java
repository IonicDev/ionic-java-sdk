/**
 * Contains {@link com.ionic.sdk.agent.Agent}, which is the primary point of interaction with the Ionic SDK.  The
 * Agent class implements {@link com.ionic.sdk.key.KeyServices}, and brokers access to cryptography keys stored on
 * Ionic servers.
 *
 * <h3>AgentSdk</h3>
 * The class {@link com.ionic.sdk.agent.AgentSdk} may optionally be used to configure the Ionic SDK to select a
 * particular {@link java.security.Provider} when cryptographic primitives are needed.  To do this, call the
 * function {@link com.ionic.sdk.agent.AgentSdk#initialize(java.security.Provider)} prior to any other usage of the
 * Ionic SDK.
 * <p>
 * References to particular providers may be obtained by calling the
 * function {@link java.security.Security#getProvider(java.lang.String)}, which returns a provider object if it is
 * registered.
 * <ul>
 * <li>"SunJCE" - the default provider, built into the JRE</li>
 * <li>"BC" - the Bouncy Castle provider, a popular third-party library</li>
 * </ul>
 * <p>
 * Any library to be used by the Ionic SDK must conform to the JCE architecture described
 * <a href='https://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html#Architecture'
 * target='_blank'>
 * here</a>.  In particular, instantiation of cryptography primitives is achieved through the use of a
 * 'getInstance()' call on the desired interface.
 * <pre>    md = MessageDigest.getInstance("SHA-256")</pre>
 * <p>
 * In SDK versions 2.0 - 2.5, the provider "org.bouncycastle.jce.provider.BouncyCastleProvider" was implicitly
 * registered to the JRE on first use of Ionic cryptography.  In SDK versions 2.6+, if usage of Bouncy Castle
 * cryptography is desired, Bouncy Castle must be explicitly registered (using the API
 * {@link com.ionic.sdk.agent.AgentSdk#initialize(java.security.Provider)} prior to any other usage of the Ionic SDK.
 * <p>
 * If no specific {@link java.security.Provider} is specified in the API call
 * {@link com.ionic.sdk.agent.AgentSdk#initialize(java.security.Provider)}, the Ionic SDK default is to use all
 * providers registered to the JRE to satisfy a request for a cryptography primitive.  If a provider is
 * specified, it must implement any requested cryptography primitive.  Known limitations:
 * <ul>
 * <li>JRE 7 has no implementation of the AES GCM algorithm.  This is used by
 * {@link com.ionic.sdk.agent.cipher.chunk.ChunkCipherV3} and
 * {@link com.ionic.sdk.agent.cipher.file.GenericFileCipher} version 1.3.</li>
 * <li>JRE 7-10 have no implementation of the RSA signature algorithm used by the Ionic SDK.  This algorithm is
 * used during the API call
 * {@link com.ionic.sdk.agent.Agent#createDevice(com.ionic.sdk.agent.request.createdevice.CreateDeviceRequest)}.</li>
 * </ul>
 */
package com.ionic.sdk.agent;
