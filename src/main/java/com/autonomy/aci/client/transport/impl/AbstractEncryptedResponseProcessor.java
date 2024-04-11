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

package com.autonomy.aci.client.transport.impl;

import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.EncryptionCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

abstract class AbstractEncryptedResponseProcessor<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEncryptedResponseProcessor.class);

    private final XMLInputFactory xmlInputFactory;

    protected final EncryptionCodec encryptionCodec;

    protected final String charsetName;

    public AbstractEncryptedResponseProcessor(final EncryptionCodec encryptionCodec, final String charsetName) {
        this.encryptionCodec = encryptionCodec;
        this.charsetName = charsetName;

        // Holds the factory for creating XMLStreamReader's...
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    }

    public T process(final InputStream aciResponse) {
        LOGGER.trace("process() called...");

        // Define this here so we can make sure it's closed when the processor is finished...
        XMLStreamReader xmlStreamReader = null;

        try {
            // Convert the input stream..
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(aciResponse);
            return process(xmlStreamReader);
        } catch (XMLStreamException xmlse) {
            throw new ProcessorException("Unable to convert the InputStream to a XMLStreamReader", xmlse);
        } finally {
            if (xmlStreamReader != null) {
                try {
                    // This does NOT close the underlying InputStream
                    xmlStreamReader.close();
                } catch (XMLStreamException xmlse) {
                    LOGGER.error("Unable to close the XMLStreamReader.", xmlse);
                }
            }
        }
    }

    abstract T process(final XMLStreamReader aciResponse) throws XMLStreamException;

}
