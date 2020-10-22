package com.ionic.sdk.cipher.aes;

/**
 * Constants related to the use of the AES algorithm within the SDK.
 */
public final class AesCipher {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private AesCipher() {
    }

    /**
     * Length in bits of Ionic infrastructure AES keys.
     */
    public static final int KEY_BITS = 256;

    /**
     * Length in bytes of Ionic infrastructure AES keys.
     */
    public static final int KEY_BYTES = (KEY_BITS / Byte.SIZE);

    /**
     * Length in bytes of initialization vector used when protecting data using Ionic AES cipher.
     */
    public static final int SIZE_IV = 16;

    /**
     * Length in bytes of authentication tag used to detect data tampering using AES/GCM.  Also called:
     * <ul>
     * <li>message authentication code (MAC),</li>
     * <li>integrity check value (ICV).</li>
     * </ul>
     */
    public static final int SIZE_ATAG = 16;

    /**
     * Label for AES algorithm.
     */
    public static final String ALGORITHM = "AES";

    /**
     * Label for AES algorithm, CTR transform (used by ChunkCrypto).
     */
    public static final String TRANSFORM_CTR = "AES/CTR/NoPadding";

    /**
     * Label for AES algorithm, GCM transform (used in communications with ionic.com).
     */
    public static final String TRANSFORM_GCM = "AES/GCM/NoPadding";

    /**
     * Label for metadata indicating use of alternate IV generation strategy.
     */
    public static final String IV_ALGORITHM = "ionic-iv-algorithm";

    /**
     * String pattern used to calculate HMAC when using deterministic encryption.
     */
    public static final String PATTERN_HMAC = "Ionic IV key %s %d";
}
