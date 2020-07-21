/**
 * Implementations of Ionic batch ciphers, which may be used to encrypt / decrypt sets of related data values with a
 * single key.  It is intended for use in applications where storage space for the resulting data in constrained.
 * <p>
 * The storage requirement for the encrypted form of the data value set is equal to that of the original data, plus
 * that of the associated key id (eleven printable ASCII characters).  Each encrypted data value may be stored in a
 * space equal to that of the original plaintext value.
 * <p>
 * The initialization vectors used for the
 * ciphertexts are derived using RNG, and stored in the {@link com.ionic.sdk.agent.key.KeyAttributesMap} of the key.
 */
package com.ionic.sdk.agent.cipher.batch;
