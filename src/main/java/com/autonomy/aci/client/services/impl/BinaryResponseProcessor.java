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
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Takes an ACI response and returns it as a byte array. This processor should only be used for those actions that
 * <strong>don't return XML</strong> by default, for example, cluster actions that return an image. This processor will
 * correctly handle an XML error response by checking to see what the content type of the returned response is before
 * processing it.
 * <p>
 * By default this processor uses {@link ErrorProcessor} and {@link ByteArrayProcessor} to do it's work. The
 * implementations to be used can be changed by using the appropriate accessor methods.
 * <p>
 * <strong>Note:</strong> If the content type is <code>text/xml</code> then an
 * {@link com.autonomy.aci.client.services.AciErrorException} will be thrown regardless of the contents of the actual
 * response. If the response didn't contain an error, then the resulting exception's error properties will all be
 * <code>null</code>. Any other content type will result in the response being returned in a <code>byte[]</code>.
 */
public class BinaryResponseProcessor implements Processor<byte[]> {

    private static final long serialVersionUID = 1262978871829721008L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ByteArrayProcessor.class);

    /**
     * This holds the error processor to use if the content type of the response is <code>text/xml</code>.
     */
    private Processor<AciErrorException> errorProcessor = new ErrorProcessor();

    /**
     * This holds the processor to use if the content type of the response isn't <code>text/xml</code>.
     */
    private Processor<byte[]> byteArrayProcessor = new ByteArrayProcessor();

    public byte[] process(final AciResponseInputStream inputStream) {
        LOGGER.trace("process() called...");

        // Check the content type to determine what we do next...
        final String contentType = inputStream.getContentType();

        LOGGER.debug("Content-type is {}...", contentType);

        if (contentType.toLowerCase(Locale.ROOT).startsWith("text/xml")) {
            // Process the error response...
            try {
                throw errorProcessor.process(inputStream);
            } catch (final AciErrorException e) {
                if (e.getErrorId() == null) {
                    throw new AciErrorException(
                        "BinaryResponseProcessor should not be used for XML responses");
                } else {
                    throw e;
                }
            }
        } else {
            // Return the raw bytes...
            return byteArrayProcessor.process(inputStream);
        }
    }

    public Processor<AciErrorException> getErrorProcessor() {
        return errorProcessor;
    }

    public void setErrorProcessor(final Processor<AciErrorException> errorProcessor) {
        this.errorProcessor = errorProcessor;
    }

    public Processor<byte[]> getByteArrayProcessor() {
        return byteArrayProcessor;
    }

    public void setByteArrayProcessor(final Processor<byte[]> byteArrayProcessor) {
        this.byteArrayProcessor = byteArrayProcessor;
    }

}
