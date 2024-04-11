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
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.autonomy.aci.client.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Takes an ACI response and returns it as a byte array. Unlike {@link BinaryResponseProcessor}, this processor doesn't
 * check the content type and wont throw a {@link AciErrorException} if the response contains an error.
 */
public class ByteArrayProcessor implements Processor<byte[]> {

    private static final long serialVersionUID = -2108328281761910807L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ByteArrayProcessor.class);

    public byte[] process(final AciResponseInputStream inputStream) {
        LOGGER.trace("process() called...");

        try {
            // Got an image of some sort so read it into a byte array...
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Copy the input stream to the byte array...
            IOUtils.getInstance().copy(inputStream, outputStream);

            // Return the bytes that make up the response...
            return outputStream.toByteArray();
        } catch (final IOException ioe) {
            throw new ProcessorException("Unable to copy ACI response to a byte array.", ioe);
        }
    }

}
