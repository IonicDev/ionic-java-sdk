package com.ionic.sdk.agent.hfp;

import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.hash.Hash;

/**
 * An implementation of a store for fingerprint information, used to authenticate IDC requests for data.
 */
public class Fingerprint {

    /**
     * The hardware fingerprint associated with the container Agent instance.
     */
    private final String hfp;

    /**
     * The cryptographic hash of the hardware fingerprint associated with the container Agent instance.
     */
    private final String hfphash;

    /**
     * Constructor.
     *
     * @param hfp the hardware fingerprint associated with the container Agent instance
     */
    public Fingerprint(final String hfp) {
        this.hfp = (hfp == null) ? HFP_DEFAULT : hfp;
        this.hfphash = Transcoder.hex().encode(new Hash().sha256(Transcoder.utf8().decode(this.hfp)));
    }

    /**
     * @return the hardware fingerprint associated with the container Agent instance
     */
    public final String getHfp() {
        return hfp;
    }

    /**
     * @return the cryptographic hash of the hardware fingerprint associated with the container Agent instance
     */
    public final String getHfpHash() {
        return hfphash;
    }

    /**
     * The default string to be used as the device's hardware fingerprint during server interactions.
     */
    private static final String HFP_DEFAULT = "THIS IS A FINGERPRINT";
}
