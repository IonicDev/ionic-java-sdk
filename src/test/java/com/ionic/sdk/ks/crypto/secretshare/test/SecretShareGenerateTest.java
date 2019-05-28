package com.ionic.sdk.ks.crypto.secretshare.test;

import com.ionic.sdk.crypto.secretshare.SecretShareBucket;
import com.ionic.sdk.crypto.secretshare.SecretShareGenerator;
import com.ionic.sdk.crypto.secretshare.SecretShareKey;
import com.ionic.sdk.error.IonicException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Test ability to generate and recover secret share keys.
 */
public class SecretShareGenerateTest {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Test the ability to generate a secret (inputs=2, threshold=1).
     *
     * @throws IonicException on failure to collect / recover secret
     */
    @Test
    public final void testSecretShare_GenerateKeyI2T1_Success() throws IonicException {
        final Properties environment = getEnvironment();
        final Collection<String> keysGroup0 = Arrays.asList("key1", "key2");

        final SecretShareGenerator generator = new SecretShareGenerator(environment);
        generator.addBucket(new SecretShareBucket(keysGroup0, keysGroup0.size() - 1));
        final SecretShareKey secretShareKey = generator.generate();
        logger.info(secretShareKey.getKey());
        logger.info(secretShareKey.getShares());

        final SecretShareGenerator generatorRecover = new SecretShareGenerator(environment);
        generatorRecover.addBucket(new SecretShareBucket(keysGroup0, keysGroup0.size() - 1));
        final SecretShareKey secretShareKeyRecover = generatorRecover.recover(secretShareKey.getShares());
        logger.info(secretShareKeyRecover.getKey());
        logger.info(secretShareKeyRecover.getShares());
        Assert.assertEquals(secretShareKey.getKey(), secretShareKeyRecover.getKey());
    }

    /**
     * Test the ability to generate a secret (inputs=3, threshold=2).
     *
     * @throws IonicException on failure to collect / recover secret
     */
    @Test
    public final void testSecretShare_GenerateKeyI3T2_Success() throws IonicException {
        final Properties environment = getEnvironment();
        final Collection<String> keysGroup0 = Arrays.asList("key1", "key2", "key3");

        final SecretShareGenerator generator = new SecretShareGenerator(environment);
        generator.addBucket(new SecretShareBucket(keysGroup0, keysGroup0.size() - 1));
        final SecretShareKey secretShareKey = generator.generate();
        logger.info(secretShareKey.getKey());
        logger.info(secretShareKey.getShares());

        final SecretShareGenerator generatorRecover = new SecretShareGenerator(environment);
        generatorRecover.addBucket(new SecretShareBucket(keysGroup0, keysGroup0.size() - 1));
        final SecretShareKey secretShareKeyRecover = generatorRecover.recover(secretShareKey.getShares());
        logger.info(secretShareKeyRecover.getKey());
        logger.info(secretShareKeyRecover.getShares());
        Assert.assertEquals(secretShareKey.getKey(), secretShareKeyRecover.getKey());
    }

    /**
     * @return a test environment set, used for secret generation and recovery
     */
    private static Properties getEnvironment() {
        final Properties environment = new Properties();
        environment.setProperty("key1", "value1");
        environment.setProperty("key2", "value2");
        environment.setProperty("key3", "value3");
        environment.setProperty("key4", "value4");
        environment.setProperty("key5", "value5");
        environment.setProperty("key6", "value6");
        return environment;
    }
}
