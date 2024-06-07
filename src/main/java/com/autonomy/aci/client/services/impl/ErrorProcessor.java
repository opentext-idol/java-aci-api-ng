/*
 * Copyright 2006-2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.aci.client.services.impl;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;

/**
 * Processes an ACI Server error response into an <code>AciErrorException</code>.
 */
public class ErrorProcessor extends AbstractStAXProcessor<AciErrorException> {

    private static final long serialVersionUID = -5828188492102112726L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorProcessor.class);

    /**
     * Process the ACI error response into an <code>AciErrorException</code>.
     * @param aciResponse The ACI response to process
     * @return Does not actually return anything as it throws the exception when it's finished parsing the response.
     * @throws AciErrorException  Unless there was an error
     * @throws ProcessorException If an error occurred during the processing of the ACI response
     */
    @Override
    public AciErrorException process(final XMLStreamReader aciResponse) {
        LOGGER.trace("process() called...");

        try {
            // Create the exception that we will throw...
            final AciErrorException exception = new AciErrorException();

            // We need to be able to handle both being given the full response and a partial response...
            while (aciResponse.hasNext()) {
                // Get the event type...
                final int eventType = aciResponse.next();

                // Get the errorid element and then process from there...
                if (XMLEvent.START_ELEMENT == eventType) {
                    if ("errorid".equalsIgnoreCase(aciResponse.getLocalName())) {
                        exception.setErrorId(aciResponse.getElementText());
                    } else if ("rawerrorid".equalsIgnoreCase(aciResponse.getLocalName())) {
                        exception.setRawErrorId(aciResponse.getElementText());
                    } else if ("errorstring".equalsIgnoreCase(aciResponse.getLocalName())) {
                        exception.setErrorString(aciResponse.getElementText());
                    } else if ("errordescription".equalsIgnoreCase(aciResponse.getLocalName())) {
                        exception.setErrorDescription(aciResponse.getElementText());
                    } else if ("errorcode".equalsIgnoreCase(aciResponse.getLocalName())) {
                        exception.setErrorCode(aciResponse.getElementText());
                    } else if ("errortime".equalsIgnoreCase(aciResponse.getLocalName())) {
                        try {
                            exception.setErrorTime(DateTimeUtils.getInstance().parseDate(aciResponse.getElementText(), "dd MMM yy HH:mm:ss"));
                        } catch (final ParseException pe) {
                            LOGGER.error("ParseException caught while trying to convert the errortime element into a java.util.Date.", pe);
                        }
                    }
                }
            }

            // Throw the generated exception...
            throw exception;
        } catch (final XMLStreamException xmlse) {
            throw new ProcessorException("Unable to create an AciErrorException from the ACI response.", xmlse);
        }
    }

}
