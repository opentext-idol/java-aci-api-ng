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
import com.autonomy.aci.client.transport.impl.AciResponseInputStreamImpl;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for <tt>com.autonomy.aci.client.services.impl.BinaryResponseProcessor</tt>.
 */
public class BinaryResponseProcessorTest {

    /**
     * This is the thing we're testing...
     */
    private static BinaryResponseProcessor processor;

    @BeforeClass
    public static void createBinaryResponseProcessor() {
        // Create the processor...
        processor = new BinaryResponseProcessor();
    }

    @Test
    public void testErrorResponse() throws IOException, ProcessorException, AciErrorException {
        // Create the "response" and give it a content type...
        final InputStreamEntity inputStreamEntity = new InputStreamEntity(getClass().getResourceAsStream("/AciException-1.xml"), ContentType.TEXT_XML);

        // Create a response...
        final ClassicHttpResponse response = new BasicClassicHttpResponse(200);
        response.setEntity(inputStreamEntity);

        // Set the AciResponseInputStream...
        final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

        // Process...
        try {
            processor.process(stream);
        } catch (final AciErrorException e) {
            Assert.assertEquals("Should have correct error ID",
                "AutonomyIDOLServerWOBBLE1", e.getErrorId());
            return;
        }
        fail("Should have thrown a AciErrorException.");
    }

    @Test
    public void testXmlResponse() throws IOException, ProcessorException, AciErrorException {
        // Create the "response" and give it a content type...
        final InputStreamEntity inputStreamEntity = new InputStreamEntity(getClass().getResourceAsStream("/GetVersion.xml"), ContentType.TEXT_XML);

        // Create a response...
        final ClassicHttpResponse response = new BasicClassicHttpResponse(200);
        response.setEntity(inputStreamEntity);

        // Set the AciResponseInputStream...
        final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

        // Process...
        try {
            processor.process(stream);
        } catch (final AciErrorException e) {
            Assert.assertNotNull("Should have an error message", e.getMessage());
            return;
        }
        fail("Should have thrown a AciErrorException.");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNonXmlResponse() throws IOException, ProcessorException, AciErrorException {
        // This is the "known" response...
        final String string = "This is a test...";

        // Create the "response" and give it a content type...
        final StringEntity stringEntity = new StringEntity(string, ContentType.TEXT_PLAIN);

        // Create a response...
        final ClassicHttpResponse response = new BasicClassicHttpResponse(200);
        response.setEntity(stringEntity);

        // Set the AciResponseInputStream...
        final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

        // Process...
        final byte[] result = processor.process(stream);
        assertThat("result is wrong", new String(result, "UTF-8"), is(equalTo(string)));
    }

    @Test(expected = ProcessorException.class)
    @SuppressWarnings("unchecked")
    public void testProcessorBadInputStream() throws IOException, ProcessorException, AciErrorException {
        final AciResponseInputStream mockAciResponseInputStream = mock(AciResponseInputStream.class);
        when(mockAciResponseInputStream.getContentType()).thenReturn("image/jpeg");
        when(mockAciResponseInputStream.read((byte[]) any())).thenThrow(IOException.class);

        processor.process(mockAciResponseInputStream);
        fail("Should have thrown a ProcessorException.");
    }

    @Test
    public void testErrorProcessorProperty() {
        // Check on unmodified object...
        assertThat("Should not be null", processor.getErrorProcessor(), is(notNullValue()));

        // Set it to null...
        processor.setErrorProcessor(null);
        assertThat("Should not be null", processor.getErrorProcessor(), is(nullValue()));

        // Set it to this ErrorProcessor and check again...
        final Processor<AciErrorException> errorProcessor = new ErrorProcessor();
        processor.setErrorProcessor(errorProcessor);
        assertThat("Should not be null", processor.getErrorProcessor(), is(sameInstance(errorProcessor)));
    }

    @Test
    public void testByteArrayProcessorProperty() {
        // Check on unmodified object...
        assertThat("Should not be null", processor.getByteArrayProcessor(), is(notNullValue()));

        // Set it to null...
        processor.setByteArrayProcessor(null);
        assertThat("Should not be null", processor.getByteArrayProcessor(), is(nullValue()));

        // Set it to this ByteArrayProcessor and check again...
        final Processor<byte[]> byteArrayProcessor = new ByteArrayProcessor();
        processor.setByteArrayProcessor(byteArrayProcessor);
        assertThat("Should not be null", processor.getByteArrayProcessor(), is(sameInstance(byteArrayProcessor)));
    }

}
