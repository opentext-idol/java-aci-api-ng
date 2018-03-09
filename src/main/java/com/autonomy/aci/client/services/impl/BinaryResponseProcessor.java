/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.services.impl;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes an ACI response and returns it as a byte array. This processor should only be used for those actions that
 * <strong>don't return XML</strong> by default, for example, cluster actions that return an image. This processor will
 * correctly handle an XML error response by checking to see what the content type of the returned response is before
 * processing it.
 * <p>
 * By default this processor uses {@link ErrorProcessor} and {@link ByteArrayProcessor} to do it's work. The
 * implementations to be used can be changed by using the appropriate accessor methods.
 * <p>
 * <strong>Note:</strong> If the content type is <tt>text/xml</tt> then an
 * {@link com.autonomy.aci.client.services.AciErrorException} will be thrown regardless of the contents of the actual
 * response. If the response didn't contain an error, then the resulting exception's error properties will all be
 * <tt>null</tt>. Any other content type will result in the response being returned in a <tt>byte[]</tt>.
 */
public class BinaryResponseProcessor implements Processor<byte[]> {

    private static final long serialVersionUID = 1262978871829721008L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ByteArrayProcessor.class);

    /**
     * This holds the error processor to use if the content type of the response is <tt>text/xml</tt>.
     */
    private Processor<AciErrorException> errorProcessor = new ErrorProcessor();

    /**
     * This holds the processor to use if the content type of the response isn't <tt>text/xml</tt>.
     */
    private Processor<byte[]> byteArrayProcessor = new ByteArrayProcessor();

    public byte[] process(final AciResponseInputStream inputStream) {
        LOGGER.trace("process() called...");

        // Check the content type to determine what we do next...
        final String contentType = inputStream.getContentType();

        LOGGER.debug("Content-type is {}...", contentType);

        if ("text/xml".equalsIgnoreCase(contentType)) {
            // Process the error response...
            throw errorProcessor.process(inputStream);
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
