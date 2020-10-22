package com.ionic.sdk.ks.cipher.file.test;

import com.ionic.sdk.agent.cipher.file.CsvFileCipher;
import com.ionic.sdk.agent.cipher.file.FileCipherAbstract;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesDefault;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesInterface;
import com.ionic.sdk.agent.cipher.file.data.CipherFamily;
import com.ionic.sdk.agent.cipher.file.data.FileCipher;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoFileInfo;
import com.ionic.sdk.core.rng.CryptoRng;
import com.ionic.sdk.core.vm.VM;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Test ability to perform file crypto operations using {@link CsvFileCipher}.
 */
public class CsvFileCipherTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Random bytes that are used to dynamically construct plain texts for use in file cipher operations.
     */
    private static byte[] testBytes;

    /**
     * Generate random bytes that can be used to dynamically construct plain texts for use in file cipher operations.
     *
     * @throws IonicException on data generation failure
     */
    @BeforeClass
    public static void setUp() throws IonicException {
        final int blockSize = 1024;
        testBytes = new CryptoRng().rand(new byte[blockSize]);
    }

    /**
     * Test {@link CsvFileCipher} helper APIs.
     *
     * @throws IonicException on failure to initialize Ionic library
     */
    @Test
    public final void testFileCipher_HelperAPIs_Success() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final FileCipherAbstract fileCipher = new CsvFileCipher(keyServices);
        Assert.assertEquals(FileCipher.Csv.V10.LABEL, fileCipher.getDefaultVersion());
        Assert.assertEquals(CipherFamily.FAMILY_CSV, fileCipher.getCipherFamily());
        Assert.assertEquals(FileCipher.Csv.FAMILY, fileCipher.getFamilyString());
        Assert.assertTrue(fileCipher.isVersionSupported(FileCipher.Csv.V10.LABEL));
        Assert.assertFalse(fileCipher.isVersionSupported(FileCipher.Generic.V12.LABEL));
    }

    /**
     * Test {@link CsvFileCipher} byte[] API operations.
     *
     * @throws IonicException on failure to initialize Ionic library, on cryptography operation failures
     * @throws IOException    on data generation failure
     */
    @Test
    public final void testFileCipherBytes_EncryptDecrypt_CodecSymmetry() throws IonicException, IOException {
        logger.info(String.format("PROCESSOR ARCHITECTURE = %s", System.getProperty(VM.Sys.OS_ARCH)));
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final int countMin = 1024;
        final int countMax = getCountMax();
        final int step = 32;
        for (int count = countMin; (count <= countMax); count *= step) {
            logger.info(String.format("TEST WITH PLAINTEXT SIZE %d", count));
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(count);
            while (bos.size() < count) {
                bos.write(testBytes);
            }
            final byte[] plainTextIn = bos.toByteArray();
            logger.info(String.format("PLAINTEXT IN, SIZE=%d, SHA=%s",
                    plainTextIn.length, CryptoUtils.sha256ToBase64(plainTextIn)));
            bos.reset();
            final FileCipherAbstract fileCipher = new CsvFileCipher(keyServices);

            final FileCryptoFileInfo fileInfoPlain = fileCipher.getFileInfo(plainTextIn);
            Assert.assertEquals(CipherFamily.FAMILY_UNKNOWN, fileInfoPlain.getCipherFamily());
            Assert.assertEquals("", fileInfoPlain.getCipherVersion());
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
                Assert.assertEquals(CipherFamily.FAMILY_CSV, fileInfoCipher.getCipherFamily());
                Assert.assertEquals(version, fileInfoCipher.getCipherVersion());
                Assert.assertTrue(fileInfoCipher.getKeyId().startsWith(keyServices.getActiveProfile().getKeySpace()));
                Assert.assertTrue(fileInfoCipher.isEncrypted());
                Assert.assertEquals(keyServices.getActiveProfile().getServer(), fileInfoCipher.getServer());

                final byte[] plainTextOut = fileCipher.decrypt(cipherTextOut);
                logger.info(String.format("PLAINTEXT OUT, SIZE=%d, SHA=%s",
                        plainTextOut.length, CryptoUtils.sha256ToBase64(plainTextOut)));
                Assert.assertEquals(plainTextIn.length, plainTextOut.length);
                Assert.assertArrayEquals(plainTextIn, plainTextOut);
            }
        }
    }

    /**
     * Test {@link CsvFileCipher} path API operations.
     *
     * @throws IonicException on failure to initialize Ionic library, on cryptography operation failures
     * @throws IOException    on data generation failure, filesystem I/O failure
     */
    @Test
    public final void testFileCipherFile_EncryptDecrypt_CodecSymmetry() throws IonicException, IOException {
        logger.info(String.format("PROCESSOR ARCHITECTURE = %s", System.getProperty(VM.Sys.OS_ARCH)));
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final FileCipherAbstract fileCipher = new CsvFileCipher(keyServices);
        final File folder = IonicTestEnvironment.getInstance().getFolderTestOutputsMkdir();
        final int countMin = 1024;
        final int countMax = getCountMax();
        final int step = 32;
        for (int count = countMin; (count <= countMax); count *= step) {
            logger.info(String.format("TEST WITH PLAINTEXT SIZE %d", count));
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(count);
            while (bos.size() < count) {
                bos.write(testBytes);
            }
            final List<String> versions = fileCipher.getVersions();
            for (String version : versions) {
                // filenames to use for this test iteration (clean up before use)
                final String format = String.format("%s.%s.%d", getClass().getSimpleName(), version, count);
                final File filePlainTextIn = new File(folder, format + ".plaintext-in.bin");
                final File fileCipherTextOut = new File(folder, format + ".ciphertext-out.bin");
                final File filePlainTextOut = new File(folder, format + ".plaintext-out.bin");
                logger.info(String.format("CLEAN UP OUTPUT, PLAINTEXT IN=%s, CIPHERTEXT OUT=%s, PLAINTEXT OUT=%s",
                        filePlainTextIn.delete(), fileCipherTextOut.delete(), filePlainTextOut.delete()));
                DeviceUtils.write(filePlainTextIn, bos.toByteArray());

                final FileCryptoFileInfo fileInfoPlain = fileCipher.getFileInfo(filePlainTextIn.getPath());
                Assert.assertEquals(CipherFamily.FAMILY_UNKNOWN, fileInfoPlain.getCipherFamily());
                Assert.assertEquals("", fileInfoPlain.getCipherVersion());
                Assert.assertEquals("", fileInfoPlain.getKeyId());
                Assert.assertFalse(fileInfoPlain.isEncrypted());
                Assert.assertEquals("", fileInfoPlain.getServer());

                // encrypt
                final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes(version);
                fileCipher.encrypt(filePlainTextIn.getPath(), fileCipherTextOut.getPath(), encryptAttributes);

                final FileCryptoFileInfo fileInfoCipher = fileCipher.getFileInfo(fileCipherTextOut.getPath());
                Assert.assertEquals(CipherFamily.FAMILY_CSV, fileInfoCipher.getCipherFamily());
                Assert.assertEquals(version, fileInfoCipher.getCipherVersion());
                Assert.assertTrue(fileInfoCipher.getKeyId().startsWith(keyServices.getActiveProfile().getKeySpace()));
                Assert.assertTrue(fileInfoCipher.isEncrypted());
                Assert.assertEquals(keyServices.getActiveProfile().getServer(), fileInfoCipher.getServer());

                // decrypt
                final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
                fileCipher.decrypt(fileCipherTextOut.getPath(), filePlainTextOut.getPath(), decryptAttributes);
                // verify
                Assert.assertEquals(encryptAttributes.getVersion(), decryptAttributes.getVersion());
                Assert.assertEquals(CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainTextIn)),
                        CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainTextOut)));
            }
        }
    }

    /**
     * Test {@link CsvFileCipher} constructor that accepts a {@link FileCryptoCoverPageServicesInterface}.
     *
     * @throws IonicException on failure to initialize Ionic library, on cryptography operation failures
     */
    @Test
    public final void testCsvCipherFile_CtorCoverPage_ExpectedBehavior() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final FileCryptoCoverPageServicesInterface coverPageServices = new FileCryptoCoverPageServicesDefault();
        final FileCipherAbstract fileCipher = new CsvFileCipher(keyServices, coverPageServices);
        final int count = 1024;
        final byte[] plainText = new byte[count];
        final byte[] cipherText = fileCipher.encrypt(plainText);
        Assert.assertTrue(cipherText.length > plainText.length);
    }

    /**
     * When the test cases in this class are run on a device with an "arm" processor (Raspberry Pi), the time elapsed
     * exceeds the time budget for the test suite.  So the size of the maximum file encryption is limited for this
     * environment.
     *
     * @return the maximum size of plaintext on which to perform file cipher cryptography operations
     */
    private static int getCountMax() {
        final int countMaxArm = 1024 * 64;
        final int countMaxDefault = countMaxArm * 256;
        final boolean isArm = System.getProperty(VM.Sys.OS_ARCH).equals("arm");
        return isArm ? countMaxArm : countMaxDefault;
    }
}
