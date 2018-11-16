package com.ionic.sdk.agent.cipher.file.data;

/**
 * Data class used to describe attributes of a file.
 */
public final class FileCryptoFileInfo {

    /**
     * Flag indicating whether or not the content is encrypted.
     */
    private boolean encrypted;

    /**
     * The Ionic cipher family used to encrypt the content.
     */
    private CipherFamily cipherFamily;

    /**
     * The Ionic cipher family version used to encrypt the content.
     */
    private String cipherVersion;

    /**
     * The identifier associated with the key used to encrypt the content.
     */
    private String keyId;

    /**
     * The issuing server for the key used to encrypt the content.
     */
    private String server;

    /**
     * Initializes the object to be empty.
     * <p>
     * The following defaults are set:
     * <ul>
     * <li>Encrypted = false</li>
     * <li>Cipher Family = {@link CipherFamily#FAMILY_UNKNOWN}</li>
     * <li>Cipher Version = (empty string)</li>
     * <li>Key ID = (empty string)</li>
     * <li>Server = (empty string)</li>
     * </ul>
     */
    public FileCryptoFileInfo() {
        this(false, CipherFamily.FAMILY_UNKNOWN, "", "", "");
    }

    /**
     * Initializes the object to be empty.
     * <p>
     * The following defaults are set:
     * <ul>
     * <li>Encrypted = false</li>
     * <li>Cipher Family = {@link CipherFamily#FAMILY_UNKNOWN}</li>
     * <li>Cipher Version = (empty string)</li>
     * <li>Key ID = (empty string)</li>
     * <li>Server = (empty string)</li>
     * </ul>
     *
     * @param encrypted     specifies if the file is encrypted or not
     * @param cipherFamily  cipher family of the file
     * @param cipherVersion cipher version of the file
     * @param keyId         the new id we want to associate with this key
     * @param server        server that the key ID was issued from
     */
    public FileCryptoFileInfo(final boolean encrypted,
                              final CipherFamily cipherFamily, final String cipherVersion,
                              final String keyId, final String server) {
        this.encrypted = encrypted;
        this.cipherFamily = cipherFamily;
        this.cipherVersion = cipherVersion;
        this.keyId = keyId;
        this.server = server;
    }

    /**
     * Determines whether the file is encrypted or not.
     *
     * @return true if file is encrypted, false otherwise
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Set the file encrypted property.
     *
     * @param encrypted specifies if the file is encrypted or not
     */
    public void setEncrypted(final boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * Determines the cipher family of the file. If the {@link #isEncrypted()} property is set to true, then this
     * cipher family represents the family that the file is encrypted with. Otherwise, this cipher family
     * represents the family that can be used to encrypt it.
     *
     * @return cipher family of file (see {@link CipherFamily})
     */
    public CipherFamily getCipherFamily() {
        return cipherFamily;
    }

    /**
     * Sets the cipher family of the file.
     *
     * @param cipherFamily cipher family of the file
     */
    public void setCipherFamily(final CipherFamily cipherFamily) {
        this.cipherFamily = cipherFamily;
    }

    /**
     * Determines the cipher version of the file. If the {@link #isEncrypted()} property is set to true, then this
     * cipher version represents the version that the file is encrypted with. Otherwise, this cipher version
     * represents the version that it is recommended to encrypt with.
     *
     * @return cipher version of the file
     */
    public String getCipherVersion() {
        return cipherVersion;
    }

    /**
     * Sets the cipher version of the file.
     *
     * @param cipherVersion cipher version of the file
     */
    public void setCipherVersion(final String cipherVersion) {
        this.cipherVersion = cipherVersion;
    }

    /**
     * Gets the key ID that was used to encrypt the file. This field is only relevant if the encrypted property
     * is set to true.
     *
     * @return Id of the key.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Sets the key ID that was used to encrypt the file. This field is only relevant if the encrypted property
     * is set to true.
     *
     * @param keyId the new id we want to associate with this key
     */
    public void setKeyId(final String keyId) {
        this.keyId = keyId;
    }

    /**
     * Gets the server that the key ID was issued from. This field is only relevant if the encrypted property
     * is set to true.
     *
     * @return The server that this key ID was issued from.
     */
    public String getServer() {
        return server;
    }

    /**
     * Sets the server that the key ID was issued from. This field is only relevant if the encrypted property
     * is set to true.
     *
     * @param server server that the key ID was issued from
     */
    public void setServer(final String server) {
        this.server = server;
    }
}
