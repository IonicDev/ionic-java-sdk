package com.ionic.sdk.ks.cipher.file.test;

import com.ionic.sdk.agent.cipher.file.CsvFileCipher;
import com.ionic.sdk.agent.cipher.file.FileCipherAbstract;
import com.ionic.sdk.agent.cipher.file.GenericFileCipher;
import com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher;
import com.ionic.sdk.agent.cipher.file.PdfFileCipher;
import com.ionic.sdk.agent.cipher.file.cover.FileCryptoCoverPageServicesDefault;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoDecryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileCryptoEncryptAttributes;
import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.key.KeyServices;
import com.ionic.sdk.ks.test.IonicTestEnvironment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.logging.Logger;

/**
 * Concise JUnit tests incorporated into JavaDoc.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileCipherSamplesTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Reference to test suite output folder.
     */
    private File folderOutput = null;

    /**
     * Get reference to test suite output folder.
     *
     * @throws IonicException on failure to get reference to output folder
     */
    @Before
    public void setUp() throws IonicException {
        folderOutput = IonicTestEnvironment.getInstance().getFolderTestOutputsMkdir();
        Assert.assertTrue(folderOutput.exists());
    }

    /**
     * Verify simple byte[] cryptography symmetry
     * using {@link com.ionic.sdk.agent.cipher.file.GenericFileCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testFileCipherGeneric_EncryptDecryptBytes() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final int sizeData = 1024;
        final byte[] plainText = new byte[sizeData];
        final FileCipherAbstract fileCipher = new GenericFileCipher(keyServices);
        final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
        final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
        final byte[] cipherText = fileCipher.encrypt(plainText, encryptAttributes);
        final byte[] plainTextRecover = fileCipher.decrypt(cipherText, decryptAttributes);
        Assert.assertArrayEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", Transcoder.base64().encode(cipherText)));
    }

    /**
     * Verify simple file content cryptography symmetry
     * using {@link com.ionic.sdk.agent.cipher.file.GenericFileCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testFileCipherGeneric_EncryptDecryptFile() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final int sizeData = 1024;
        final byte[] plainText = new byte[sizeData];
        final File filePlainText = new File(folderOutput, "generic.plainText.txt");
        final File fileCipherText = new File(folderOutput, "generic.cipherText.txt");
        final File filePlainTextRecover = new File(folderOutput, "generic.plainText.recover.txt");
        DeviceUtils.write(filePlainText, plainText);
        final FileCipherAbstract fileCipher = new GenericFileCipher(keyServices);
        final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
        final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
        fileCipher.encrypt(filePlainText.getPath(), fileCipherText.getPath(), encryptAttributes);
        fileCipher.decrypt(fileCipherText.getPath(), filePlainTextRecover.getPath(), decryptAttributes);
        final String sha256PlainText = CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainText));
        final String sha256Recover = CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainTextRecover));
        Assert.assertEquals(sha256PlainText, sha256Recover);
    }

    /**
     * Verify simple byte[] cryptography symmetry
     * using {@link com.ionic.sdk.agent.cipher.file.CsvFileCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testFileCipherCsv_EncryptDecryptBytes() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final byte[] plainText = Transcoder.utf8().decode("a,b,c\n1,2,3\n");
        final FileCipherAbstract fileCipher = new CsvFileCipher(keyServices);
        final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
        final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
        final byte[] cipherText = fileCipher.encrypt(plainText, encryptAttributes);
        final byte[] plainTextRecover = fileCipher.decrypt(cipherText, decryptAttributes);
        Assert.assertArrayEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", Transcoder.base64().encode(cipherText)));
    }

    /**
     * Verify simple file content cryptography symmetry
     * using {@link com.ionic.sdk.agent.cipher.file.CsvFileCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testFileCipherCsv_EncryptDecryptFile() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final byte[] plainText = Transcoder.utf8().decode("a,b,c\n1,2,3\n");
        final File filePlainText = new File(folderOutput, "plainText.csv");
        final File fileCipherText = new File(folderOutput, "cipherText.csv");
        final File filePlainTextRecover = new File(folderOutput, "plainText.recover.csv");
        DeviceUtils.write(filePlainText, plainText);
        final FileCipherAbstract fileCipher = new CsvFileCipher(keyServices);
        final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
        final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
        fileCipher.encrypt(filePlainText.getPath(), fileCipherText.getPath(), encryptAttributes);
        fileCipher.decrypt(fileCipherText.getPath(), filePlainTextRecover.getPath(), decryptAttributes);
        final String sha256PlainText = CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainText));
        final String sha256Recover = CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainTextRecover));
        Assert.assertEquals(sha256PlainText, sha256Recover);
    }

    /**
     * Verify simple byte[] cryptography symmetry
     * using {@link com.ionic.sdk.agent.cipher.file.PdfFileCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testFileCipherPdf_EncryptDecryptBytes() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        // https://stackoverflow.com/questions/17279712/
        final byte[] plainText = new FileCryptoCoverPageServicesDefault().getCoverPage(FileType.FILETYPE_PDF);
        final FileCipherAbstract fileCipher = new PdfFileCipher(keyServices);
        final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
        final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
        final byte[] cipherText = fileCipher.encrypt(plainText, encryptAttributes);
        final byte[] plainTextRecover = fileCipher.decrypt(cipherText, decryptAttributes);
        Assert.assertArrayEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", Transcoder.base64().encode(cipherText)));
    }

    /**
     * Verify simple file content cryptography symmetry
     * using {@link com.ionic.sdk.agent.cipher.file.PdfFileCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testFileCipherPdf_EncryptDecryptFile() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final byte[] plainText = new FileCryptoCoverPageServicesDefault().getCoverPage(FileType.FILETYPE_PDF);
        final File filePlainText = new File(folderOutput, "plainText.pdf");
        final File fileCipherText = new File(folderOutput, "cipherText.pdf");
        final File filePlainTextRecover = new File(folderOutput, "plainText.recover.pdf");
        DeviceUtils.write(filePlainText, plainText);
        final FileCipherAbstract fileCipher = new PdfFileCipher(keyServices);
        final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
        final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
        fileCipher.encrypt(filePlainText.getPath(), fileCipherText.getPath(), encryptAttributes);
        fileCipher.decrypt(fileCipherText.getPath(), filePlainTextRecover.getPath(), decryptAttributes);
        final String sha256PlainText = CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainText));
        final String sha256Recover = CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainTextRecover));
        Assert.assertEquals(sha256PlainText, sha256Recover);
    }

    /**
     * Verify simple byte[] cryptography symmetry
     * using {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testFileCipherOpenXml_EncryptDecryptBytes() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final byte[] plainText = new FileCryptoCoverPageServicesDefault().getCoverPage(FileType.FILETYPE_DOCX);
        final FileCipherAbstract fileCipher = new OpenXmlFileCipher(keyServices);
        final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
        final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
        final byte[] cipherText = fileCipher.encrypt(plainText, encryptAttributes);
        final byte[] plainTextRecover = fileCipher.decrypt(cipherText, decryptAttributes);
        Assert.assertArrayEquals(plainText, plainTextRecover);
        logger.info(String.format("CIPHERTEXT = %s", Transcoder.base64().encode(cipherText)));
    }

    /**
     * Verify simple file content cryptography symmetry
     * using {@link com.ionic.sdk.agent.cipher.file.OpenXmlFileCipher}.
     *
     * @throws IonicException on initialization failure, operation failure
     */
    @Test
    public final void testFileCipherOpenXml_EncryptDecryptFile() throws IonicException {
        final KeyServices keyServices = IonicTestEnvironment.getInstance().getKeyServices();
        final byte[] plainText = new FileCryptoCoverPageServicesDefault().getCoverPage(FileType.FILETYPE_DOCX);
        final File filePlainText = new File(folderOutput, "plainText.docx");
        final File fileCipherText = new File(folderOutput, "cipherText.docx");
        final File filePlainTextRecover = new File(folderOutput, "plainText.recover.docx");
        DeviceUtils.write(filePlainText, plainText);
        final FileCipherAbstract fileCipher = new OpenXmlFileCipher(keyServices);
        final FileCryptoEncryptAttributes encryptAttributes = new FileCryptoEncryptAttributes();
        final FileCryptoDecryptAttributes decryptAttributes = new FileCryptoDecryptAttributes();
        fileCipher.encrypt(filePlainText.getPath(), fileCipherText.getPath(), encryptAttributes);
        fileCipher.decrypt(fileCipherText.getPath(), filePlainTextRecover.getPath(), decryptAttributes);
        final String sha256PlainText = CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainText));
        final String sha256Recover = CryptoUtils.sha256ToHexString(DeviceUtils.read(filePlainTextRecover));
        Assert.assertEquals(sha256PlainText, sha256Recover);
    }
}
