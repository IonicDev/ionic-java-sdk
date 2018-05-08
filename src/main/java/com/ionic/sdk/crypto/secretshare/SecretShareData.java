package com.ionic.sdk.crypto.secretshare;

import com.ionic.sdk.error.IonicException;

import java.util.Collection;
import java.util.Properties;

/**
 * Implementations of this interface are used to generate a cryptography key from data supplied by the
 * implementation.  The key is protected by splitting it to shares using the Shamir Secret Sharing algorithm.  The
 * buckets define groups of attributes which must remain the same across usages, up to the threshold defined in
 * each bucket.
 */
public interface SecretShareData {

    /**
     * Request the aggregate data that should be gathered from the implementation.
     *
     * @return a Properties object, containing key-value pairs specific to the implementation
     * @throws IonicException on errors gathering the data
     */
    Properties getData() throws IonicException;

    /**
     * Request the definition of how the gathered data should be used to generate / recover the cryptography secret.
     *
     * @return a collection of objects defining property keys to be used, and data recovery thresholds for each group
     */
    Collection<SecretShareBucket> getBuckets();
}
