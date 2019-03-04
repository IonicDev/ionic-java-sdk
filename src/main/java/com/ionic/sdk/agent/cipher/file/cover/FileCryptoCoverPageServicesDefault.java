package com.ionic.sdk.agent.cipher.file.cover;

import com.ionic.sdk.agent.cipher.file.data.FileType;
import com.ionic.sdk.core.res.Resource;
import com.ionic.sdk.core.vm.EOL;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;

/**
 * Ionic SDK default Cover Page Service provider.
 */
public final class FileCryptoCoverPageServicesDefault extends FileCryptoCoverPageServicesInterface {

    @Override
    public byte[] getCoverPage(final FileType fileType) throws IonicException {
        switch (fileType) {
            case FILETYPE_CSV:
                return EOL.normalizeToWindows(DeviceUtils.read(Resource.resolve(CSV_HEADER_STRING)));
            case FILETYPE_PDF:
                return DeviceUtils.read(Resource.resolve(PDF_COVER_PAGE_FILE));
            case FILETYPE_DOCX:
                return DeviceUtils.read(Resource.resolve(DOCX_COVER_PAGE_FILE));
            case FILETYPE_DOCM:
                return DeviceUtils.read(Resource.resolve(DOCM_COVER_PAGE_FILE));
            case FILETYPE_PPTX:
                return DeviceUtils.read(Resource.resolve(PPTX_COVER_PAGE_FILE));
            case FILETYPE_PPTM:
                return DeviceUtils.read(Resource.resolve(PPTM_COVER_PAGE_FILE));
            case FILETYPE_XLSX:
                return DeviceUtils.read(Resource.resolve(XLSX_COVER_PAGE_FILE));
            case FILETYPE_XLSM:
                return DeviceUtils.read(Resource.resolve(XLSM_COVER_PAGE_FILE));

            default:
                throw new IonicException(SdkError.ISFILECRYPTO_NO_COVERPAGE);
        }
    }

    @Override
    public byte[] getAccessDeniedPage(final FileType fileType) throws IonicException {
        switch (fileType) {
            case FILETYPE_CSV:
                return EOL.normalizeToWindows(DeviceUtils.read(Resource.resolve(CSV_ACCESS_DENIED_STRING)));
            case FILETYPE_PDF:
                return DeviceUtils.read(Resource.resolve(PDF_ACCESS_DENIED_FILE));
            case FILETYPE_DOCX:
                return DeviceUtils.read(Resource.resolve(DOCX_ACCESS_DENIED_FILE));
            case FILETYPE_DOCM:
                return DeviceUtils.read(Resource.resolve(DOCM_ACCESS_DENIED_FILE));
            case FILETYPE_PPTX:
                return DeviceUtils.read(Resource.resolve(PPTX_ACCESS_DENIED_FILE));
            case FILETYPE_PPTM:
                return DeviceUtils.read(Resource.resolve(PPTM_ACCESS_DENIED_FILE));
            case FILETYPE_XLSX:
                return DeviceUtils.read(Resource.resolve(XLSX_ACCESS_DENIED_FILE));
            case FILETYPE_XLSM:
                return DeviceUtils.read(Resource.resolve(XLSM_ACCESS_DENIED_FILE));
            default:
                throw new IonicException(SdkError.ISFILECRYPTO_NO_COVERPAGE);
        }
    }

    /**
     * The classpath location of the resource containing the Ionic cover page text for the CSV file type.
     */
    private static final String CSV_HEADER_STRING =
            "com/ionic/sdk/agent/cipher/file/cover/csv.header.string.csv";

    /**
     * The classpath location of the resource containing the Ionic access denied text for the CSV file type.
     */
    private static final String CSV_ACCESS_DENIED_STRING =
            "com/ionic/sdk/agent/cipher/file/cover/csv.access.denied.string.csv";

    /**
     * The classpath location of the resource containing the Ionic cover page text for the PDF file type.
     */
    private static final String PDF_COVER_PAGE_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/ionic_cover_page.pdf";

    /**
     * The classpath location of the resource containing the Ionic access denied text for the PDF file type.
     */
    private static final String PDF_ACCESS_DENIED_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/ionic_access_denied_page.pdf";

    /**
     * The classpath location of the resource containing the Ionic cover page for the DOCX file type.
     */
    private static final String DOCX_COVER_PAGE_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/coverpage.docx";
    /**
     * The classpath location of the resource containing the Ionic access denied for the DOCX file type.
     */
    private static final String DOCX_ACCESS_DENIED_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/accessdenied.docx";
    /**
     * The classpath location of the resource containing the Ionic cover page for the DOCM file type.
     */
    private static final String DOCM_COVER_PAGE_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/coverpage.docm";
    /**
     * The classpath location of the resource containing the Ionic access denied for the DOCM file type.
     */
    private static final String DOCM_ACCESS_DENIED_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/accessdenied.docm";
    /**
     * The classpath location of the resource containing the Ionic cover page for the PPTX file type.
     */
    private static final String PPTX_COVER_PAGE_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/coverpage.pptx";
    /**
     * The classpath location of the resource containing the Ionic access denied for the PPTX file type.
     */
    private static final String PPTX_ACCESS_DENIED_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/accessdenied.pptx";
    /**
     * The classpath location of the resource containing the Ionic cover page for the PPTM file type.
     */
    private static final String PPTM_COVER_PAGE_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/coverpage.pptm";
    /**
     * The classpath location of the resource containing the Ionic access denied for the PPTM file type.
     */
    private static final String PPTM_ACCESS_DENIED_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/accessdenied.pptm";
    /**
     * The classpath location of the resource containing the Ionic cover page for the XLSX file type.
     */
    private static final String XLSX_COVER_PAGE_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/coverpage.xlsx";
    /**
     * The classpath location of the resource containing the Ionic access denied for the XLSX file type.
     */
    private static final String XLSX_ACCESS_DENIED_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/accessdenied.xlsx";
    /**
     * The classpath location of the resource containing the Ionic cover page for the XLSM file type.
     */
    private static final String XLSM_COVER_PAGE_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/coverpage.xlsm";
    /**
     * The classpath location of the resource containing the Ionic access denied for the XLSM file type.
     */
    private static final String XLSM_ACCESS_DENIED_FILE =
            "com/ionic/sdk/agent/cipher/file/cover/accessdenied.xlsm";
}
