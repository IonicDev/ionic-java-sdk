package com.ionic.sdk.agent.key.merge;

import com.ionic.sdk.agent.key.KeyAttributesMap;

/**
 * Interface used during protection key update operations to merge key attributes when needed.
 */
public interface KeyAttributesMapMerger {

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
    int mergeKeyAttributeMaps(boolean attributesAreMutable,
                              KeyAttributesMap originalServerAttributes,
                              KeyAttributesMap currentServerAttributes,
                              KeyAttributesMap clientAttributes,
                              KeyAttributesMap mergedAttributes);
}
