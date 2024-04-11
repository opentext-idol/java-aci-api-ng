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

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.EncryptionCodec;
import com.autonomy.aci.client.transport.EncryptionCodecException;
import com.autonomy.aci.client.util.EncryptionCodecUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

class EncryptedTextContentProcessor extends AbstractEncryptedResponseProcessor<ByteArrayInputStream> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTextContentProcessor.class);

    private static final String TEMPLATE_1 = "<?xml version=''{0}'' encoding=''{1}'' ?><autnresponse xmlns:autn=''http://schemas.autonomy.com/aci/''>";
    private static final String TEMPLATE_2 = "<responsedata>";
    private static final String TEMPLATE_3 = "</responsedata></autnresponse>";

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public EncryptedTextContentProcessor(final EncryptionCodec encryptionCodec, final String charsetName) {
        super(encryptionCodec, charsetName);
    }

    @Override
    public ByteArrayInputStream process(final XMLStreamReader aciResponse) throws AciErrorException, ProcessorException {
        try {
            // We should already be on the START_DOCUMENT event...
            LOGGER.debug("Writing initial template to buffer...");
            final String template = MessageFormat.format(TEMPLATE_1, aciResponse.getVersion(), aciResponse.getEncoding());
            buffer.write(template.getBytes(charsetName));

            // As the content type was text/xml, we can use string to string methods...
            final EncryptionCodecUtils codecUtils = EncryptionCodecUtils.getInstance();

            while (aciResponse.hasNext()) {
                // Get the event type...
                final int eventType = aciResponse.next();

                if (eventType == XMLEvent.START_ELEMENT) {
                    if ("autn:encryptedheader".equals(aciResponse.getLocalName())) {
                        LOGGER.debug("Found the encrypted header, decrypting and appending to buffer...");
                        buffer.write(encryptionCodec.decrypt(codecUtils.toBytes(aciResponse.getElementText(), charsetName)));
                        buffer.write(TEMPLATE_2.getBytes(charsetName));
                    } else if ("autn:encrypteddata".equals(aciResponse.getLocalName())) {
                        LOGGER.debug("Found an encrypted data block, decrypting and appending to buffer...");
                        buffer.write(encryptionCodec.decrypt(codecUtils.toBytes(aciResponse.getElementText(), charsetName)));
                    }
                } else if (eventType == XMLEvent.END_DOCUMENT) {
                    LOGGER.debug("Found the end of the response, appending the footer to the buffer...");
                    buffer.write(TEMPLATE_3.getBytes(charsetName));
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
