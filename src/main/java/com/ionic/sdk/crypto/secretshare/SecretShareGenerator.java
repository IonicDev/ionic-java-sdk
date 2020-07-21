package com.ionic.sdk.crypto.secretshare;

import com.ionic.sdk.agent.service.IDC;
import com.ionic.sdk.cipher.aes.AesCipher;
import com.ionic.sdk.cipher.aes.AesGcmCipher;
import com.ionic.sdk.core.codec.Transcoder;
import com.ionic.sdk.core.rng.CryptoRng;
import com.ionic.sdk.core.value.Value;
import com.ionic.sdk.crypto.CryptoUtils;
import com.ionic.sdk.crypto.shamir.Scheme;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.error.SdkError;
import com.ionic.sdk.json.JsonIO;
import com.ionic.sdk.json.JsonSource;
import com.ionic.sdk.json.JsonTarget;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Generator for a cryptography secret, intended to be derived from (and protected by) externally-supplied data.
 */
public final class SecretShareGenerator {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The external data used to protect the key.
     */
    private final Properties properties;

    /**
     * Container for configuration instructing the generator how to fold together the external information
     * to derive the protection key / JSON.
     */
    private final Collection<SecretShareBucket> buckets;

    /**
     * Constructor.
     *
     * @param properties the external data used to protect the key
     */
    public SecretShareGenerator(final Properties properties) {
        this.properties = properties;
        this.buckets = new ArrayList<SecretShareBucket>();
    }

    /**
     * Add configuration on how to derive the protection metadata from the external data.
     *
     * @param bucket a container for configuration on a single logical properties grouping
     */
    public void addBucket(final SecretShareBucket bucket) {
        buckets.add(bucket);
    }

    /**
     * Use the seeded environment data of generate the secret to be protected, as well as the metadata used to
     * protect the secret.
     *
     * @return an object containing the secret, and its associated metadata
     * @throws IonicException on cryptography errors
     */
    public SecretShareKey generate() throws IonicException {
        final List<byte[]> shares = new ArrayList<byte[]>();
        final byte[] secret = new byte[AesCipher.KEY_BYTES];
        //logger.finest("GENERATE, BEFORE BUCKETS");
        for (final SecretShareBucket bucket : buckets) {
            final byte[] secretIt = new byte[AesCipher.KEY_BYTES];
            final List<String> labels = new ArrayList<String>(bucket.getKeys());
            final int threshold = bucket.getThreshold();
            if (threshold == labels.size()) {
                createKeyFromInputs(labels, secretIt);
            } else if (threshold < labels.size()) {
                createThresholdKeyFromInputs(labels, threshold, secretIt, shares);
            } else {
                throw new IonicException(SdkError.ISAGENT_INVALIDVALUE);
            }
            //logger.finest(String.format("BUCKET, SECRET_IT=[%s]", Transcoder.hex().encode(secretIt)));
            //logger.finest(String.format("BUCKET, SECRET BEFORE=[%s]", Transcoder.hex().encode(secret)));
            accumulateXOR(secret, secretIt);
            //logger.finest(String.format("BUCKET, SECRET AFTER=[%s]", Transcoder.hex().encode(secret)));
        }
        //logger.finest("GENERATE, AFTER BUCKETS");
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (final byte[] share : shares) {
            JsonTarget.addNotNull(jsonArrayBuilder, CryptoUtils.binToBase64(share));
        }
        final byte[] salt = new CryptoRng().rand(new byte[SALT_BITS / Byte.SIZE]);
        final byte[] secretFinal = CryptoUtils.pbkdf2ToBytes(secret, salt, PBKDF_ITERATIONS, secret.length);
        final JsonObject jsonPersist = Json.createObjectBuilder()
                .add(IDC.SSKP.SHARES, jsonArrayBuilder.build())
                .add(IDC.SSKP.SALT, CryptoUtils.binToBase64(salt))
                .build();
        return new SecretShareKey(CryptoUtils.binToHex(secretFinal), JsonIO.write(jsonPersist, true));
    }

    /**
     * Recover the original secret from its associated metadata.
     *
     * @param data the JSON-encoded metadata bound to the JVM environment which protects the secret
     * @return an object containing the secret, and its associated metadata
     * @throws IonicException on cryptography errors
     */
    public SecretShareKey recover(final String data) throws IonicException {
        final JsonObject jsonPersist = JsonIO.readObject(data, SdkError.ISAGENT_PARSEFAILED);
        final JsonArray jsonShares = JsonSource.getJsonArray(jsonPersist, IDC.SSKP.SHARES);
        final String jsonSalt = JsonSource.getString(jsonPersist, IDC.SSKP.SALT);

        final List<byte[]> shares = new ArrayList<byte[]>();
        for (final JsonValue jsonValue : jsonShares) {
            final String jsonShare = JsonSource.toString(jsonValue);
            shares.add(CryptoUtils.base64ToBin(jsonShare));
        }
        final byte[] salt = CryptoUtils.base64ToBin(jsonSalt);
        final byte[] secret = new byte[AesCipher.KEY_BYTES];
        //logger.finest(String.format("RECOVER, BEFORE BUCKETS, SALT=[%s]", Transcoder.hex().encode(salt)));
        for (final SecretShareBucket bucket : buckets) {
            //logger.finest(String.format("BUCKET, KEYS=[%s],THRESHOLD=[%d]", bucket.getKeys(), bucket.getThreshold()));
            final byte[] secretIt = new byte[AesCipher.KEY_BYTES];
            final List<String> labels = new ArrayList<String>(bucket.getKeys());
            final int threshold = bucket.getThreshold();
            if (threshold == labels.size()) {
                createKeyFromInputs(labels, secretIt);
            } else if (threshold < labels.size()) {
                restoreThresholdKeyFromInputs(labels, threshold, secretIt, shares);
            } else {
                throw new IonicException(SdkError.ISAGENT_INVALIDVALUE);
            }
            //logger.finest(String.format("BUCKET, SECRET_IT=[%s]", Transcoder.hex().encode(secretIt)));
            //logger.finest(String.format("BUCKET, SECRET BEFORE=[%s]", Transcoder.hex().encode(secret)));
            accumulateXOR(secret, secretIt);
            //logger.finest(String.format("BUCKET, SECRET AFTER=[%s]", Transcoder.hex().encode(secret)));
        }
        //logger.finest(String.format(
        //        "RECOVER, AFTER BUCKETS, SECRET=[%s], SALT=[%s], ITERATIONS=[%d], SECRET-LENGTH=[%d]",
        //        Transcoder.hex().encode(secret), Transcoder.hex().encode(salt), iterationsPBKDF, secret.length));
        final byte[] secretFinal = CryptoUtils.pbkdf2ToBytes(secret, salt, PBKDF_ITERATIONS, secret.length);
        //logger.finest(String.format("RECOVER, AFTER PBKDF, SECRET_FINAL=[%s]", Transcoder.hex().encode(secretFinal)));
        return new SecretShareKey(CryptoUtils.binToHex(secretFinal), data);
    }

    /**
     * Create a secret using data values derived from the JVM environment.
     *
     * @param keys   the names of the property data elements to associate with this bucket
     * @param secret the container for the created secret
     * @throws IonicException on cryptography errors
     */
    private void createKeyFromInputs(final Collection<String> keys, final byte[] secret) throws IonicException {
        // javax.crypto.spec.PBEKeySpec requires value, salt to be non-null and non-empty so we create
        // a random value to guarantee that API call will succeed, and that the field will not match
        final byte[] salt = Transcoder.utf8().decode(Value.joinCollection(IDC.Message.DELIMITER, keys));
        for (final String key : keys) {
            final byte[] value = Transcoder.utf8().decode(properties.getProperty(key));
            final byte[] valuePBKDF = (Value.isEmpty(value) ? new byte[SALT_BITS / Byte.SIZE] : value);
            final byte[] secretIt = CryptoUtils.pbkdf2ToBytes(valuePBKDF, salt, PBKDF_ITERATIONS, secret.length);
            //logger.finest(String.format("CREATE(INPUTS), SECRET_IT=[%s]", Transcoder.hex().encode(secret)));
            //logger.finest(String.format("CREATE(INPUTS), SECRET BEFORE=[%s]", Transcoder.hex().encode(secret)));
            accumulateXOR(secret, secretIt);
            //logger.finest(String.format("CREATE(INPUTS), SECRET AFTER=[%s]", Transcoder.hex().encode(secret)));
        }
    }

    /**
     * Create a secret using data values derived from the JVM environment.  The Shamir algorithm is used to split
     * the secret into parts, which are then bound to individual data values.  The original secret should be
     * recoverable using a subset of the original data values, as specified by the threshold parameter.
     *
     * @param keys      the names of the property data elements to associate with this bucket
     * @param threshold the number of data elements whose values must match in order to recover the secret
     * @param secret    the container for the created secret
     * @param shares    the parts of the original secret, protected by the corresponding data values
     * @throws IonicException on cryptography errors
     */
    private void createThresholdKeyFromInputs(
            final List<String> keys, final int threshold,
            final byte[] secret, final List<byte[]> shares) throws IonicException {
        final byte[] salt = Transcoder.utf8().decode(Value.joinCollection(IDC.Message.DELIMITER, keys));
        //logger.finest(String.format("CREATE(THRESHOLD), SALT=[%s]", Transcoder.utf8().encode(salt)));
        // create a secret
        new CryptoRng().rand(secret);
        //logger.finest(String.format("CREATE(THRESHOLD), KEY=[%s]", Transcoder.hex().encode(secret)));
        // split the secret
        final Scheme scheme = new Scheme(keys.size(), threshold);
        final List<byte[]> split = scheme.split(secret);
        final AesGcmCipher cipher = new AesGcmCipher();
        for (int i = 0; (i < split.size()); ++i) {
            final String key = keys.get(i);
            //logger.fine(String.format("CREATE(THRESHOLD), PROP_KEY=[%s]", key));
            final byte[] value = Transcoder.utf8().decode(properties.getProperty(key));
            //logger.fine(String.format("CREATE(THRESHOLD), PROP_VALUE=[%s]", Transcoder.hex().encode(value)));
            // javax.crypto.spec.PBEKeySpec requires value, salt to be non-null and non-empty so we create
            // a random value to guarantee that API call will succeed, and that the field will not match
            final byte[] valuePBKDF = (Value.isEmpty(value)
                    ? new CryptoRng().rand(new byte[SALT_BITS / Byte.SIZE]) : value);
            final byte[] secretIt = CryptoUtils.pbkdf2ToBytes(valuePBKDF, salt, PBKDF_ITERATIONS, secret.length);
            //logger.finest(String.format("CREATE(THRESHOLD), SECRET_IT=[%s]", Transcoder.hex().encode(secretIt)));
            cipher.setKey(secretIt);
            cipher.setAuthData(Transcoder.utf8().decode(key));
            final byte[] plainText = split.get(i);
            final byte[] cipherText = cipher.encrypt(plainText);
            //logger.finest(String.format("CREATE(THRESHOLD), CIPHERTEXT=[%s]", Transcoder.hex().encode(cipherText)));
            shares.add(cipherText);
        }
    }

    /**
     * Recover the original secret using data values derived from the JVM environment.  The Shamir algorithm is used
     * to reconstitute the original secret from the deserialized parts which are protected by individual data
     * values.
     *
     * @param keys      the names of the property data elements to associate with this bucket
     * @param threshold the number of data elements whose values must match in order to recover the secret
     * @param secret    the container for the created secret
     * @param shares    the parts of the original secret, protected by the corresponding data values
     * @throws IonicException on cryptography errors
     */
    private void restoreThresholdKeyFromInputs(
            final List<String> keys, final int threshold,
            final byte[] secret, final List<byte[]> shares) throws IonicException {
        final List<byte[]> sharesIt = new ArrayList<byte[]>(shares.subList(0, keys.size()));
        shares.removeAll(sharesIt);
        final byte[] salt = Transcoder.utf8().decode(Value.joinCollection(IDC.Message.DELIMITER, keys));
        //logger.finest(String.format("RECOVER(THRESHOLD), SALT=[%s]", Transcoder.hex().encode(salt)));
        final Collection<byte[]> parts = new ArrayList<byte[]>();
        final AesGcmCipher cipher = new AesGcmCipher();
        for (int i = 0; (i < keys.size()); ++i) {
            final String key = keys.get(i);
            final byte[] value = Transcoder.utf8().decode(properties.getProperty(key));
            //logger.finest(String.format("RECOVER(THRESHOLD), PROP_VALUE=[%s]", Transcoder.hex().encode(value)));
            // javax.crypto.spec.PBEKeySpec requires value, salt to be non-null and non-empty so we create
            // a random value to guarantee that API call will succeed, and that the field will not match
            final byte[] valuePBKDF = (Value.isEmpty(value)
                    ? new CryptoRng().rand(new byte[SALT_BITS / Byte.SIZE]) : value);
            final byte[] secretIt = CryptoUtils.pbkdf2ToBytes(valuePBKDF, salt, PBKDF_ITERATIONS, secret.length);
            //logger.finest(String.format("RECOVER(THRESHOLD), SECRET_IT=[%s]", Transcoder.hex().encode(secretIt)));
            cipher.setKey(secretIt);
            cipher.setAuthData(Transcoder.utf8().decode(key));
            final byte[] cipherText = sharesIt.get(i);
            //logger.finest(String.format("RECOVER(THRESHOLD), CIPHERTEXT=[%s]", Transcoder.hex().encode(cipherText)));
            try {
                final byte[] decrypt = cipher.decrypt(cipherText);
                //logger.finest(String.format("RECOVER(THRESHOLD), CLEARTEXT=[%s]", Transcoder.hex().encode(decrypt)));
                if (decrypt.length == (secret.length + CRYPTOPP_OFFSET)) {
                    parts.add(decrypt);
                    logger.fine(String.format("A piece of environmental data has been recovered [%s].", key));
                } else {
                    logger.fine(String.format("A piece of environmental data has been discarded [%s].", key));
                }
            } catch (IonicException e) {
                logger.warning(String.format("A piece of environmental data is missing or changed [%s].", key));
            }
        }
        if (parts.size() >= threshold) {
            final Scheme scheme = new Scheme(keys.size(), threshold);
            final byte[] secretPart = scheme.join(parts, secret.length);
            //logger.finest(String.format("RECOVER, FINAL, SECRET=[%s]", Transcoder.hex().encode(secretPart)));
            System.arraycopy(secretPart, 0, secret, 0, secretPart.length);
        } else {
            throw new IonicException(SdkError.ISCRYPTO_ERROR, new GeneralSecurityException(
                    String.format("Unable to recover key; %d shares decrypted, %d needed.", parts.size(), threshold)));
        }
    }

    /**
     * Use the XOR operator to fold additional input into the secret accumulator.
     *
     * @param accumulator container for the current state of the secret
     * @param input       additional data used to protect the secret
     */
    private void accumulateXOR(final byte[] accumulator, final byte[] input) {
        for (int i = 0; (i < accumulator.length); ++i) {
            accumulator[i] ^= input[i];
        }
    }

    /**
     * Length in bits of salt used in PBKDF calls.
     */
    private static final int SALT_BITS = 64;

    /**
     * Number of iterations to be used in PBKDF calls.  This number comes from the original SI implementation:
     * ISAgentDeviceProfilePersistorEnvironmental.cpp#45.
     */
    private static final int PBKDF_ITERATIONS = 10000;

    /**
     * The "cryptopp" implementation prepends the share data with a 4-byte block containing the channel number.
     */
    private static final int CRYPTOPP_OFFSET = 4;
}
