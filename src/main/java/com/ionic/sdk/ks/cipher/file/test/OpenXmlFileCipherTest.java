package com.ionic.sdk.ks.cipher.file.test;

import com.ionic.sdk.agent.cipher.file.FileCipherAbstract;
import com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesDefault;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesInterface;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.core.res.Resource;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.key.KeyServices;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

/**
 * Test ability to perform file crypto operations using {@link OpenXmlFileCipher}.
 */
public class OpenXmlFileCipherTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Test {@link OpenXmlFileCipher} helper APIs.
     *
     * @throws IonicException on failure to initialize Ionic library
     */
    @Test
    public final void testFileCipher_HelperAPIs_Success() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final FileCipherAbstract fileCipher = new OpenXmlFileCipher(keyServices);
        Assert.assertEquals(FileCipher.OpenXml.V11.LABEL, fileCipher.getDefaultVersion());
        Assert.assertEquals(CipherFamily.FAMILY_OPENXML, fileCipher.getCipherFamily());
        Assert.assertEquals(FileCipher.OpenXml.FAMILY, fileCipher.getFamilyString());
        Assert.assertTrue(fileCipher.isVersionSupported(FileCipher.OpenXml.V11.LABEL));
        Assert.assertFalse(fileCipher.isVersionSupported(FileCipher.Generic.V12.LABEL));
    }

    /**
     * Test {@link OpenXmlFileCipher} byte[] API operations.
     *
     * @throws IonicException on failure to initialize Ionic library, on cryptography operation failures
     */
    @Test
    public final void testFileCipherBytes_EncryptDecrypt_CodecSymmetry() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final URL url = Resource.resolve(OPENXML_RESOURCE);
        Assert.assertNotNull(url);
        final byte[] plainTextIn = DeviceUtils.read(url);

        logger.info(String.format("PLAINTEXT IN, SIZE=%d, SHA=%s",
                plainTextIn.length, CryptoUtils.sha256ToBase64(plainTextIn)));
        final FileCipherAbstract fileCipher = new OpenXmlFileCipher(keyServices);

        final FileCryptoFileInfo fileInfoPlain = fileCipher.getFileInfo(plainTextIn);
        Assert.assertEquals(CipherFamily.FAMILY_OPENXML, fileInfoPlain.getCipherFamily());
        Assert.assertEquals(FileCipher.OpenXml.V11.LABEL, fileInfoPlain.getCipherVersion());
        Assert.assertEquals("", fileInfoPlain.getKeyId());
        Assert.assertFalse(fileInfoPlain.isEncrypted());
        Assert.assertEquals("", fileInfoPlain.getServer());

        final List<String> versions = fileCipher.getVersions();
        for (String version : versions) {
            final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes(version);
            final byte[] cipherTextOut = fileCipher.encrypt(plainTextIn, encryptAttributes);
            logger.info(String.format("CIPHERTEXT OUT, VERSION=%s, SIZE=%d, SHA=%s",
                    version, cipherTextOut.length, CryptoUtils.sha256ToBase64(cipherTextOut)));

            final FileCryptoFileInfo fileInfoCipher = fileCipher.getFileInfo(cipherTextOut);
            if (version.equals(FileCipher.OpenXml.V10.LABEL)) {
                Assert.assertEquals(CipherFamily.FAMILY_UNKNOWN, fileInfoCipher.getCipherFamily());
                Assert.assertEquals("", fileInfoCipher.getCipherVersion());
                Assert.assertTrue(fileInfoCipher.getKeyId().isEmpty());
                Assert.assertFalse(fileInfoCipher.isEncrypted());
                Assert.assertEquals("", fileInfoCipher.getServer());
            } else if (version.equals(FileCipher.OpenXml.V11.LABEL)) {
                Assert.assertEquals(CipherFamily.FAMILY_OPENXML, fileInfoCipher.getCipherFamily());
                Assert.assertEquals(version, fileInfoCipher.getCipherVersion());
                Assert.assertTrue(fileInfoCipher.getKeyId().startsWith(keyServices.getActiveProfile().getKeySpace()));
                Assert.assertTrue(fileInfoCipher.isEncrypted());
                Assert.assertEquals(keyServices.getActiveProfile().getServer(), fileInfoCipher.getServer());
            } else {
                throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
            }
            final byte[] plainTextOut = fileCipher.decrypt(cipherTextOut);
            final File folder = IonicTestEnvironment.getInstance().getFolderTestOutputsMkdir();
            DeviceUtils.write(new File(folder, "plainTextIn.docx"), plainTextIn);
            DeviceUtils.write(new File(folder, "cipherTextOut.docx"), cipherTextOut);
            DeviceUtils.write(new File(folder, "plainTextOut.docx"), plainTextOut);
            logger.info(String.format("PLAINTEXT OUT, SIZE=%d, SHA=%s",
                    plainTextOut.length, CryptoUtils.sha256ToBase64(plainTextOut)));
            Assert.assertEquals(plainTextIn.length, plainTextOut.length);
            Assert.assertArrayEquals(plainTextIn, plainTextOut);
        }
    }

    /**
     * Test {@link OpenXmlFileCipher} path API operations.
     *
     * @throws IonicException on failure to initialize Ionic library, on cryptography operation failures
     */
    @Test
    public final void testFileCipherFile_EncryptDecrypt_CodecSymmetry() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final FileCipherAbstract fileCipher = new OpenXmlFileCipher(keyServices);
        final File folder = IonicTestEnvironment.getInstance().getFolderTestOutputsMkdir();

        final URL url = Resource.resolve(OPENXML_RESOURCE);
        Assert.assertNotNull(url);
        final byte[] plainTextIn = DeviceUtils.read(url);

        final List<String> versions = fileCipher.getVersions();
        for (String version : versions) {
            // filenames to use for this test iteration (clean up before use)
            final String format = String.format("%s.%s", getClass().getSimpleName(), version);
            final File filePlainTextIn = new File(folder, format + ".plaintext-in.bin");
            final File fileCipherTextOut = new File(folder, format + ".ciphertext-out.bin");
            final File filePlainTextOut = new File(folder, format + ".plaintext-out.bin");
            logger.info(String.format("CLEAN UP OUTPUT, PLAINTEXT IN=%s, CIPHERTEXT OUT=%s, PLAINTEXT OUT=%s",
                    filePlainTextIn.delete(), fileCipherTextOut.delete(), filePlainTextOut.delete()));
            DeviceUtils.write(filePlainTextIn, plainTextIn);

            final FileCryptoFileInfo fileInfoPlain = fileCipher.getFileInfo(filePlainTextIn.getPath());
            Assert.assertEquals(CipherFamily.FAMILY_OPENXML, fileInfoPlain.getCipherFamily());
            Assert.assertEquals(FileCipher.OpenXml.V11.LABEL, fileInfoPlain.getCipherVersion());
            Assert.assertEquals("", fileInfoPlain.getKeyId());
            Assert.assertFalse(fileInfoPlain.isEncrypted());
            Assert.assertEquals("", fileInfoPlain.getServer());

            // encrypt
            final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes(version);
            fileCipher.encrypt(filePlainTextIn.getPath(), fileCipherTextOut.getPath(), encryptAttributes);

            final FileCryptoFileInfo fileInfoCipher = fileCipher.getFileInfo(fileCipherTextOut.getPath());
            if (version.equals(FileCipher.OpenXml.V10.LABEL)) {
                Assert.assertEquals(CipherFamily.FAMILY_UNKNOWN, fileInfoCipher.getCipherFamily());
                Assert.assertEquals("", fileInfoCipher.getCipherVersion());
                Assert.assertTrue(fileInfoCipher.getKeyId().isEmpty());
                Assert.assertFalse(fileInfoCipher.isEncrypted());
                Assert.assertEquals("", fileInfoCipher.getServer());
            } else if (version.equals(FileCipher.OpenXml.V11.LABEL)) {
                Assert.assertEquals(CipherFamily.FAMILY_OPENXML, fileInfoCipher.getCipherFamily());
                Assert.assertEquals(version, fileInfoCipher.getCipherVersion());
                Assert.assertTrue(fileInfoCipher.getKeyId().startsWith(keyServices.getActiveProfile().getKeySpace()));
                Assert.assertTrue(fileInfoCipher.isEncrypted());
                Assert.assertEquals(keyServices.getActiveProfile().getServer(), fileInfoCipher.getServer());
            } else {
                throw new IonicException(SdkError.ISFILECRYPTO_VERSION_UNSUPPORTED);
            }

            // decrypt
            final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
            fileCipher.decrypt(fileCipherTextOut.getPath(), filePlainTextOut.getPath(), decryptAttributes);
            // verify
            Assert.assertEquals(encryptAttributes.getVersion(), decryptAttributes.getVersion());
            Assert.assertEquals(CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainTextIn)),
                    CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainTextOut)));
        }
    }

    /**
     * Test {@link OpenXmlFileCipher} constructor that accepts a {@link FileCryptoCoverPageServicesInterface}.
     *
     * <code>OpenXmlFileCipher</code> expects that plain text supplied to an <code>encrypt()</code> call is of a
     * known OpenXml file type.  On bad input, an exception is thrown.
     *
     * @throws IonicException on failure to initialize Ionic library, on cryptography operation failures
     */
    @Test
    public final void testFileCipherFile_CtorCoverPageBadInput_ExpectedBehavior() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final FileCryptoCoverPageServicesInterface coverPageServices = new FileCryptoCoverPageServicesDefault();
        final FileCipherAbstract fileCipher = new OpenXmlFileCipher(keyServices, coverPageServices);
        final int count = 1024;
        final byte[] plainText = new byte[count];
        try {
            final byte[] cipherText = fileCipher.encrypt(plainText);
            Assert.assertTrue(cipherText.length > plainText.length);
            Assert.fail("exception should be thrown");
        } catch (IonicException e) {
            Assert.assertEquals(SdkError.ISFILECRYPTO_NO_COVERPAGE, e.getReturnCode());
        }
    }

    /**
     * Test {@link OpenXmlFileCipher} constructor that accepts a {@link FileCryptoCoverPageServicesInterface}.
     *
     * <code>OpenXmlFileCipher</code> expects that plain text supplied to an <code>encrypt()</code> call is of a
     * known OpenXml file type.
     *
     * @throws IonicException on failure to initialize Ionic library, on cryptography operation failures
     */
    @Test
    public final void testFileCipherFile_CtorCoverPage_ExpectedBehavior() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final FileCryptoCoverPageServicesInterface coverPageServices = new FileCryptoCoverPageServicesDefault();
        final FileCipherAbstract fileCipher = new OpenXmlFileCipher(keyServices, coverPageServices);
        final URL url = Resource.resolve(OPENXML_RESOURCE);
        Assert.assertNotNull(url);
        final byte[] plainText = DeviceUtils.read(url);
        final byte[] cipherText = fileCipher.encrypt(plainText);
        Assert.assertTrue(cipherText.length > plainText.length);
    }

    /**
     * A small OpenXml resource which may be used to test cryptography APIs.
     */
    private static final String OPENXML_RESOURCE =
            "com/ionic/sdk/agent/cipher/file/openxml/docx_v1_1_plaintext_good_portions.docx";
}
