package com.ionic.sdk.core.validate;

import com.ionic.sdk.error.IonicException;

/**
 * Interface providing for the ability to validate JSON messages being exchanged with the Ionic.com
 * server infrastructure.
 */
public interface Validator {

    /**
     * Perform validation on a given JSON message.
     *
     * @param type    the type of message to validate
     * @param message the JSON message to validate
     * @throws IonicException on message validation failure
     */
    void validate(String type, String message) throws IonicException;
}
