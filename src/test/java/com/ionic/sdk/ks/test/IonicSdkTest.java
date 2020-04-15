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
     * Test coverage for {@link SdkVersion APIs}.  When tests are run against undecorated code, git commit
     * is not available.
     */
    @Test
    public final void testSdkVersion_APIs_Success() {
        final String versionString = SdkVersion.getVersionString();
        logger.info(versionString.length() + "::" + versionString);
        final int expectedMinLength = 12;  // 12 version, 1 space, 7-9 git commit
        Assert.assertTrue(versionString.length() >= expectedMinLength);
    }

    /**
     * Test coverage for {@link SdkModule APIs}.  Compatibility checks against legacy JNI wrapper APIs.
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
