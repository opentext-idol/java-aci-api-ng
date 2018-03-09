/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.services;

import com.autonomy.aci.client.transport.AciResponseInputStream;

import java.io.Serializable;

/**
 * Defines methods that ACI response processors should implement.
 * @param <T> The type of object this processor will generate
 */
public interface Processor<T> extends Serializable {

    /**
     * Process the ACI response input into an object of type <tt>T</tt>.
     * @param aciResponse The ACI response to process
     * @return An object of type <tt>T</tt>
     * @throws AciErrorException  If the ACI response was an error response
     * @throws ProcessorException If an error occurred during the processing of the ACI response
     */
    T process(AciResponseInputStream aciResponse);

}
