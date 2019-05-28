package com.ionic.sdk.ks.test;

import com.ionic.sdk.agent.SdkVersion;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.error.SdkModule;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Test coverage for Ionic SDK helper classes.
 */
public class IonicSdkTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Test coverage for {@link SdkVersion APIs}.
     */
    @Test
    public final void testSdkVersion_APIs_Success() {
        final String versionString = SdkVersion.getVersionString();
        logger.info(versionString);
        final int expectedLength = 12;
        Assert.assertEquals(expectedLength, versionString.length());
    }

    /**
     * Test coverage for {@link SdkModule APIs}.
     */
    @Test
    public final void testSdkErrorModule_APIs_Success() {
        Assert.assertEquals(SdkModule.MODULE_ISCHUNKCRYPTO, SdkModule.getErrorCodeModule(SdkError.ISCHUNKCRYPTO_ERROR));
        Assert.assertEquals(SdkModule.MODULE_ISAGENT, SdkModule.getErrorCodeModule(SdkError.ISAGENT_ERROR));
        Assert.assertEquals(SdkModule.MODULE_ISCRYPTO, SdkModule.getErrorCodeModule(SdkError.ISCRYPTO_ERROR));
        Assert.assertEquals(SdkModule.MODULE_ISFILECRYPTO, SdkModule.getErrorCodeModule(SdkError.ISFILECRYPTO_ERROR));
        Assert.assertNotEquals(SdkModule.MODULE_ISFILECRYPTO, SdkModule.getErrorCodeModule(SdkError.ISAGENT_ERROR));
    }
}
