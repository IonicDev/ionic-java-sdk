package com.ionic.sdk.crypto.secretshare;

import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.device.DeviceUtils;
import com.ionic.sdk.error.IonicException;

import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Interface for working with secret share implementations.
 */
public class SecretSharePersistor {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The path of a file containing the JSON encoded share data, used to reconstitute the secret.
     */
    private final String path;

    /**
     * Data used to generate a cryptography secret.
     */
    private final SecretShareData shareData;

    /**
     * Constructor.
     * <p>
     * A path is needed in order to specify a filesystem location which should be used for persistence of the
     * secret share data.  The {@link SecretShareData} object specifies how to extract the external data from the
     * environment in order to generate / recover the cryptography secret.
     *
     * @param path      the filesystem persistence location for the share data
     * @param shareData the instructions on how to gather the external data, and how to transform the data into a secret
     */
    public SecretSharePersistor(final String path, final SecretShareData shareData) {
        this.path = path;
        this.shareData = shareData;
    }

    /**
     * Generate the secret share key from the input data.
     *
     * @return the hex encoded representation of the cryptography secret
     * @throws IonicException on errors gathering or persisting the external data
     */
    public final String generateKey() throws IonicException {
        final SecretShareGenerator generator = new SecretShareGenerator(shareData.getData());
        final Collection<SecretShareBucket> buckets = shareData.getBuckets();
        for (SecretShareBucket bucket : buckets) {
            generator.addBucket(bucket);
        }
        final File file = new File(path);
        final SecretShareKey secretShareKey;
        if (file.exists()) {
            final byte[] bytes = DeviceUtils.read(file);
            logger.info(String.format("SecretSharePersistor, resource=[%s], hash=[%s], size=[%d]",
                    path, CryptoUtils.sha256ToHexString(bytes), bytes.length));
            secretShareKey = generator.recover(Transcoder.utf8().encode(bytes));
        } else {
            secretShareKey = generator.generate();
            DeviceUtils.write(file, Transcoder.utf8().decode(secretShareKey.getShares()));
        }
        return secretShareKey.getKey();
    }
}
