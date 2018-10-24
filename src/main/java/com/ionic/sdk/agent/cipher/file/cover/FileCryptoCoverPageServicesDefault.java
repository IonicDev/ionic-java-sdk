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
            default:
                throw new IonicException(SdkError.ISFILECRYPTO_NO_COVERPAGE);
        }
    }

    @Override
    public byte[] getAccessDeniedPage(final FileType fileType) throws IonicException {
        switch (fileType) {
            case FILETYPE_CSV:
                return EOL.normalizeToWindows(DeviceUtils.read(Resource.resolve(CSV_ACCESS_DENIED_STRING)));
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
}
