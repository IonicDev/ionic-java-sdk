package com.ionic.sdk.agent.key;

/**
 * Common base interface for all Ionic Keys.
 */
 public interface KeyBase {

     /**
      * Get an identifier for the key.
      *
      * @return String keyId.
      */
     String getId();

     /**
      * Set the identifier of the key.
      *
      * @param keyId
      *      The key id String.
      */
     void setId(String keyId);

     /**
      * Get the key data.
      *
      * @return byte[] keyBytes.
      */
     byte[] getKey();

     /**
      * Set the key data.
      *
      * @param keyBytes
      *      The key bytes.
      */
     void setKey(final byte[] keyBytes);
 }
