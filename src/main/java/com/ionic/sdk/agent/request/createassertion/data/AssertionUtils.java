package com.ionic.sdk.agent.request.createassertion.data;

import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkData;
import com.ionic.sdk.error.SdkError;

/**
 * Utility logic relating to Machina Identity Assertions.
 */
public final class AssertionUtils {

    /**
     * Constructor.
     * http://checkstyle.sourceforge.net/config_design.html#FinalClass
     */
    private AssertionUtils() {
    }

    /**
     * Extract the Machina keyspace from the specified identity assertion signer.
     *
     * @param signer the name of the entity performing the signing operation
     * @return the Machina keyspace ID associated with the signing operation
     * @throws IonicException on invalid input
     */
    public static String toKeyspace(final String signer) throws IonicException {
        SdkData.checkTrue(signer != null, SdkError.ISAGENT_MISSINGVALUE, IDC.IdentityAssertion.SIGNER);
        return signer.split(REGEX_TOKEN_DOT)[0];
    }

    /**
     * Regular expression token used to split identity assertion signer into its constituent parts.
     */
    private static final String REGEX_TOKEN_DOT = "\\.";
}
