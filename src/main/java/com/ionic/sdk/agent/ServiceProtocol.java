package com.ionic.sdk.agent;

import com.ionic.sdk.agent.config.AgentConfig;
import com.ionic.sdk.agent.data.MetadataMap;
import com.ionic.sdk.agent.hfp.Fingerprint;
import com.ionic.sdk.error.IonicException;
import com.ionic.sdk.httpclient.HttpHeaders;

import java.net.URL;
import java.util.Properties;

/**
 * Data and logic associated with use of a {@link com.ionic.sdk.key.KeyServices} implementation.  ServiceProtocol
 * brokers access to the underlying KeyServices, allowing for its logic to be used in a common way.
 */
public interface ServiceProtocol {

    /**
     * Determine if the {@link com.ionic.sdk.key.KeyServices} is initialized and ready for use.
     *
     * @return True if the {@link com.ionic.sdk.key.KeyServices} is initialized; false otherwise.
     */
    boolean isInitialized();

    /**
     * Determine if the {@link com.ionic.sdk.key.KeyServices} has an identity to make service requests.
     *
     * @return True if the {@link com.ionic.sdk.key.KeyServices} has an identity; false otherwise.
     */
    boolean hasIdentity();

    /**
     * Determine if the {@link com.ionic.sdk.key.KeyServices} identity is valid to make service requests.
     *
     * @return True if the {@link com.ionic.sdk.key.KeyServices} identity is valid; false otherwise.
     * @throws IonicException on invalid identity data
     */
    boolean isValidIdentity() throws IonicException;

    /**
     * @return the Machina identity associated with the underlying {@link com.ionic.sdk.key.KeyServices} instance
     */
    String getIdentity();

    /**
     * @return the configuration of the underlying {@link com.ionic.sdk.key.KeyServices} instance
     */
    AgentConfig getConfig();

    /**
     * Callback allowing Machina credentials to be added to {@link com.ionic.sdk.key.KeyServices} service requests
     * based on the authentication type.
     *
     * @param httpHeaders the http headers associated with the service request
     */
    void addHeader(HttpHeaders httpHeaders);

    /**
     * @return the fingerprint associated with this {@link com.ionic.sdk.key.KeyServices} instance
     */
    Fingerprint getFingerprint();

    /**
     * Generate a unique id to be used to identify a particular server transaction, and to help secure its content
     * during transit.
     *
     * @return a string to be used in context of a service request
     * @throws IonicException on {@link com.ionic.sdk.key.KeyServices} misconfiguration; random number
     *                        generation failure
     */
    String generateCid() throws IonicException;

    /**
     * @return the metadata attributes associated with the underlying {@link com.ionic.sdk.key.KeyServices} instance
     */
    MetadataMap getMetadata();

    /**
     * Perform a data protect transform on an attribute set, depending on the underlying
     * {@link com.ionic.sdk.key.KeyServices} implementation.
     *
     * @param authData  additional data used in the protect transform
     * @param plainText the data to be protected
     * @return the protected representation of the data
     * @throws IonicException on failure to protect the data
     */
    String protectAttributes(String authData, String plainText) throws IonicException;

    /**
     * Perform a data unprotect transform on an attribute set, depending on the underlying
     * {@link com.ionic.sdk.key.KeyServices} implementation.
     *
     * @param authData   additional data used in the unprotect transform
     * @param cipherText the data to be unprotected
     * @param key        the cryptography key to use in the unprotect transform
     * @return the unprotected representation of the data
     * @throws IonicException on failure to unprotect the data
     */
    String unprotectAttributes(String authData, String cipherText, byte[] key) throws IonicException;

    /**
     * Perform a protection transform to a service request payload, depending on the underlying
     * {@link com.ionic.sdk.key.KeyServices} implementation.
     *
     * @param payload the request payload to be submitted to the service
     * @param cid     the unique id used to identify a particular service transaction
     * @return the transformed payload
     * @throws IonicException on failure to transform the service request payload
     */
    byte[] transformRequestPayload(byte[] payload, String cid) throws IonicException;

    /**
     * Perform a protection transform to a service response payload, depending on the underlying
     * {@link com.ionic.sdk.key.KeyServices} implementation.
     *
     * @param payload the response payload received from the service
     * @param cid     the unique id used to identify a particular service transaction
     * @return the transformed payload
     * @throws IonicException on failure to transform the service response payload
     */
    byte[] transformResponsePayload(byte[] payload, String cid) throws IonicException;

    /**
     * @return the service endpoint associated with the underlying {@link com.ionic.sdk.key.KeyServices} implementation
     * @throws IonicException on {@link com.ionic.sdk.key.KeyServices} misconfiguration
     */
    URL getUrl() throws IonicException;

    /**
     * Calculate the resource portion of the service URL.
     *
     * @param version   the service API version to call
     * @param operation the service operation to perform
     * @return the URL resource associated with the service endpoint for the requested transaction type
     * @throws IonicException on {@link com.ionic.sdk.key.KeyServices} misconfiguration
     */
    String getResource(String version, String operation) throws IonicException;

    /**
     * Transform the data for a {@link com.ionic.sdk.agent.key.KeyBase} contained in a service response.
     *
     * @param keyHex   the key data for a given service response {@link com.ionic.sdk.agent.key.KeyBase}
     * @param authData the key authentication data used when performing the decrypt transform on the key data
     * @return the raw bytes associated with the {@link java.security.Key}
     * @throws IonicException on failure to recover the raw key bytes
     */
    byte[] getKeyBytes(String keyHex, String authData) throws IonicException;

    /**
     * Calculate the signature associated with the request attributes, and add to the service request payload.
     * <p>
     * a2\IonicAgents\SDK\ISAgentSDK\ISAgentLib\ISAgentTransactionUtil.cpp:buildSignedAttributes()
     *
     * @param cid        the unique id used to identify a particular service transaction
     * @param refId      the reference used in the signature algorithm
     * @param extra      extra data  used in the signature algorithm
     * @param sigs       service-transaction-scoped container of client-generated signatures
     * @param attrs      the data to be signed
     * @param areMutable a flag indicating whether the specified attributes may be updated after creation
     * @return the text encoded signature
     * @throws IonicException on cryptography errors
     */
    String signAttributes(String cid, String refId, String extra,
                          Properties sigs, String attrs, boolean areMutable) throws IonicException;

    /**
     * Verify the signature provided in the service response against a locally generated signature.
     *
     * @param name        the attribute whose value should be checked
     * @param sigExpected the (server-provided) value
     * @param attrs       the source material for the signature
     * @param key         the key to use in calculating the signature
     * @throws IonicException on cryptography errors
     */
    void verifySignature(String name, String sigExpected, String attrs, byte[] key) throws IonicException;
}
