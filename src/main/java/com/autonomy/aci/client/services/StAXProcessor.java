/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.services;

import javax.xml.stream.XMLStreamReader;

/**
 * Defines methods that StAX based ACI response processors should implement.
 * @param <T> The type of object this processor will generate
 */
public interface StAXProcessor<T> extends Processor<T> {

    /**
     * Process the ACI response input into an object of type <tt>T</tt>.
     * @param aciResponse The ACI server response to process
     * @return An object of type <tt>T</tt>
     * @throws AciErrorException  If the ACI response was an error response
     * @throws ProcessorException If an error occurred during the processing of the ACI server response
     */
    T process(XMLStreamReader aciResponse) throws AciErrorException, ProcessorException;

}
