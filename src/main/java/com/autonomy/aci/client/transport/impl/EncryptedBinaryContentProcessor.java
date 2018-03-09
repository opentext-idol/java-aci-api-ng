/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.impl;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.EncryptionCodec;
import com.autonomy.aci.client.transport.EncryptionCodecException;
import com.autonomy.aci.client.util.EncryptionCodecUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EncryptedBinaryContentProcessor extends AbstractEncryptedResponseProcessor<ByteArrayInputStream> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedBinaryContentProcessor.class);

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public EncryptedBinaryContentProcessor(final EncryptionCodec encryptionCodec, final String charsetName) {
        super(encryptionCodec, charsetName);
    }

    @Override
    public ByteArrayInputStream process(final XMLStreamReader aciResponse) throws AciErrorException, ProcessorException {
        try {
            while (aciResponse.hasNext()) {
                // Get the event type...
                final int eventType = aciResponse.next();

                if ((eventType == XMLEvent.START_ELEMENT) && "autn:encrypteddata".equals(aciResponse.getLocalName())) {
                    LOGGER.debug("Found an encrypted data block, decrypting and appending to buffer...");
                    buffer.write(encryptionCodec.decrypt(EncryptionCodecUtils.getInstance().toBytes(aciResponse.getElementText(), charsetName)));
                }
            }

            // Return the decrypted response...
            return new ByteArrayInputStream(buffer.toByteArray());
        } catch (final XMLStreamException xmlse) {
            throw new ProcessorException("Unable to decrypt the ACI response due to a problem with the input stream.", xmlse);
        } catch (final IOException ioe) {
            throw new ProcessorException("Unable to decrypt the ACI response due to an IOException.", ioe);
        } catch (final EncryptionCodecException ece) {
            throw new ProcessorException("Unable to decrypt the ACI response.", ece);
        }
    }

}
