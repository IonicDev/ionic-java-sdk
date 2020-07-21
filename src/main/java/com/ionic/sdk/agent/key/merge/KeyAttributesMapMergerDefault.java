package com.ionic.sdk.agent.key.merge;

import com.ionic.sdk.agent.key.KeyAttributesMap;
import com.ionic.sdk.error.SdkError;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

/**
 * This is the default SDK-provided implementation of key attribute merging logic that can be used
 * in the event that a merge is needed during a protection key update operation
 * ({@link com.ionic.sdk.agent.Agent#updateKeys(com.ionic.sdk.agent.request.updatekey.UpdateKeysRequest)}).
 */
public class KeyAttributesMapMergerDefault implements KeyAttributesMapMerger {

    /**
     * Class scoped logger.
     */
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Perform key attribute merge operation.
     *
     * @param attributesAreMutable     Specifies if the attributes being merged are mutable or not.
     * @param originalServerAttributes The attributes originally received from the server during a past
     *                                 key create, fetch, or update operation.
     * @param currentServerAttributes  The attributes as they are currently known to the server.
     * @param clientAttributes         The attributes known to the client.
     * @return The output attributes resulting from the merge operation.
     */
    public final KeyAttributesMap mergeKeyAttributeMaps(final boolean attributesAreMutable,
                                                        final KeyAttributesMap originalServerAttributes,
                                                        final KeyAttributesMap currentServerAttributes,
                                                        final KeyAttributesMap clientAttributes) {
        final KeyAttributesMap mergedAttributes = new KeyAttributesMap(currentServerAttributes);
        mergeKeyAttributeMapsInternal(attributesAreMutable, originalServerAttributes,
                currentServerAttributes, clientAttributes, mergedAttributes);
        return mergedAttributes;
    }

    /**
     * Perform key attribute merge operation.
     *
     * @param attributesAreMutable     Specifies if the attributes being merged are mutable or not.
     * @param originalServerAttributes The attributes originally received from the server during a past
     *                                 key create, fetch, or update operation.
     * @param currentServerAttributes  The attributes as they are currently known to the server.
     * @param clientAttributes         The attributes known to the client.
     * @param mergedAttributes         The output attributes resulting from the merge operation.
     * @return An error code indicating the status of the operation.
     */
    @Override
    public final int mergeKeyAttributeMaps(final boolean attributesAreMutable,
                                           final KeyAttributesMap originalServerAttributes,
                                           final KeyAttributesMap currentServerAttributes,
                                           final KeyAttributesMap clientAttributes,
                                           final KeyAttributesMap mergedAttributes) {
        return mergeKeyAttributeMapsInternal(attributesAreMutable,
                originalServerAttributes, currentServerAttributes, clientAttributes, mergedAttributes);
    }

    /**
     * Perform key attribute merge operation.
     *
     * @param attributesAreMutable     Specifies if the attributes being merged are mutable or not.
     * @param originalServerAttributes The attributes originally received from the server during a past
     *                                 key create, fetch, or update operation.
     * @param currentServerAttributes  The attributes as they are currently known to the server.
     * @param clientAttributes         The attributes known to the client.
     * @param mergedAttributes         The output attributes resulting from the merge operation.
     * @return An error code indicating the status of the operation.
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")  // preserve the existing API signature
    private int mergeKeyAttributeMapsInternal(final boolean attributesAreMutable,
                                              final KeyAttributesMap originalServerAttributes,
                                              final KeyAttributesMap currentServerAttributes,
                                              final KeyAttributesMap clientAttributes,
                                              final KeyAttributesMap mergedAttributes) {
        // Trying to model as closely as possible the existing C++ implementation.  One gotcha was that the C++
        // algorithm depended on the ability to examine the collection element at the iterator cursor.  Using
        // ListIterator and adjusting the logic as needed.  Also changed KeyAttributesMap to derive from TreeMap, so
        // we get the same ordering implied by std::map<string, ...>.
        final KeyAttributesMap finalAttributes = new KeyAttributesMap(currentServerAttributes);
        final ListIterator<String> origIter = new ArrayList<String>(originalServerAttributes.keySet()).listIterator();
        final ListIterator<String> clientIter = new ArrayList<String>(clientAttributes.keySet()).listIterator();
        while (origIter.hasNext() || clientIter.hasNext()) {
            if (!origIter.hasNext()) {
                // there are no more items in the original server attributes, so we are iterating
                // on client attributes. this means we need to merge our client attribute values
                // with the current server attribute values (which FYI are available in mapFinalAttributes).
                final String clientKey = clientIter.next();
                final List<String> clientValues = clientAttributes.get(clientKey);
                final List<String> mergedValues = new ArrayList<String>();
                mergeKeyAttributeValues(new ArrayList<String>(),
                        finalAttributes.get(clientKey), clientValues, mergedValues);
                finalAttributes.put(clientKey, mergedValues);
            } else if (!clientIter.hasNext()) {
                // there are no more items in the client attributes, so we are iterating on
                // original server attributes. this means we need to remove the original server
                // attribute altogether since our client has intentionally deleted it.
                final String origKey = origIter.next();
                finalAttributes.remove(origKey);
            } else {
                // Java does not implement ability to "peek" through the Iterator interface, but ListIterator works
                final String origKey = origIter.next();
                final String clientKey = clientIter.next();
                origIter.previous();
                clientIter.previous();
                if (clientKey.compareTo(origKey) < 0) {
                    // we found a client attribute that does not exist in the original server attributes.
                    // this means we need to merge our client attribute values with the current server
                    // attribute values (which FYI are available in mapFinalAttributes).
                    final List<String> clientValues = clientAttributes.get(clientKey);
                    final List<String> mergedValues = new ArrayList<String>();
                    mergeKeyAttributeValues(new ArrayList<String>(),
                            finalAttributes.get(clientKey), clientValues, mergedValues);
                    finalAttributes.put(clientKey, mergedValues);
                    clientIter.next();
                } else if (origKey.compareTo(clientKey) < 0) {
                    // we found an original server attribute that does not exist in the client attributes.
                    // this means we need to remove the original server attribute altogether since our
                    // client has intentionally deleted it.
                    finalAttributes.remove(origKey);
                    origIter.next();
                } else {
                    // we found an original server attribute that also exists in the client attributes.
                    // this means we need to merge both of their value vectors together into the current
                    // server attribute values (which FYI are available in mapFinalAttributes).
                    if (finalAttributes.containsKey(clientKey)) {
                        final List<String> clientValues = clientAttributes.get(clientKey);
                        final List<String> mergedValues = new ArrayList<String>();
                        mergeKeyAttributeValues(originalServerAttributes.get(origKey),
                                finalAttributes.get(clientKey), clientValues, mergedValues);
                        finalAttributes.put(clientKey, mergedValues);
                    }
                    origIter.next();
                    clientIter.next();
                }
            }
        }
        mergedAttributes.clear();
        mergedAttributes.putAll(finalAttributes);
        logger.finest(String.format("mergedAttributes = %s", mergedAttributes.toString()));
        return SdkError.ISAGENT_OK;
    }

    /**
     * Merge sets of values for a (mutable) attribute key.
     *
     * @param originalServerValues The attribute values originally received from the server during a past
     *                             key create, fetch, or update operation.
     * @param currentServerValues  The attribute values as they are currently known to the server.
     * @param currentClientValues  The attribute values known to the client.
     * @param mergedValues         The output attribute values resulting from the merge operation.
     */
    private void mergeKeyAttributeValues(final List<String> originalServerValues,
                                         final List<String> currentServerValues,
                                         final List<String> currentClientValues,
                                         final List<String> mergedValues) {
        final List<String> currentServerValuesNN = (currentServerValues == null)
                ? new ArrayList<String>() : currentServerValues;
        final List<String> finalValues = new ArrayList<String>(currentServerValuesNN);
        // detect which values were removed by the client and remove them from the final
        // values vector. this is done by seeing which values inside of vecOriginalServerValuesIn
        // do not exist inside of vecClientValuesInOut
        for (String originalServerValue : originalServerValues) {
            if (!currentClientValues.contains(originalServerValue)) {
                finalValues.remove(originalServerValue);
            }
        }
        // detect which values were added by the client and add them to the final
        // values vector. this is done by seeing which values inside of vecClientValuesInOut
        // do not exist inside of vecOriginalServerValuesIn
        for (String currentClientValue : currentClientValues) {
            if (!originalServerValues.contains(currentClientValue)
                    && !finalValues.contains(currentClientValue)) {
                finalValues.add(currentClientValue);
            }
        }
        mergedValues.clear();
        mergedValues.addAll(finalValues);
        logger.finest(String.format("mergedValues = %s", mergedValues.toString()));
    }
}
