/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.services.impl;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.autonomy.aci.client.transport.impl.AciResponseInputStreamImpl;
import org.apache.http.HttpVersion;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test(expected = AciErrorException.class)
    public void testErrorResponse() throws IOException, ProcessorException, AciErrorException {
        // Create the "response" and give it a content type...
        final InputStreamEntity inputStreamEntity = new InputStreamEntity(getClass().getResourceAsStream("/AciException-1.xml"), -1);
        inputStreamEntity.setContentType("text/xml");

        // Create a response...
        final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        response.setEntity(inputStreamEntity);

        // Set the AciResponseInputStream...
        final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

        // Process...
        processor.process(stream);
        fail("Should have thrown a AciErrorException.");
    }

    @Test(expected = AciErrorException.class)
    public void testXmlResponse() throws IOException, ProcessorException, AciErrorException {
        // Create the "response" and give it a content type...
        final InputStreamEntity inputStreamEntity = new InputStreamEntity(getClass().getResourceAsStream("/GetVersion.xml"), -1);
        inputStreamEntity.setContentType("text/xml");

        // Create a response...
        final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        response.setEntity(inputStreamEntity);

        // Set the AciResponseInputStream...
        final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

        // Process...
        processor.process(stream);
        fail("Should have thrown a AciErrorException.");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNonXmlResponse() throws IOException, ProcessorException, AciErrorException {
        // This is the "known" response...
        final String string = "This is a test...";

        // Create the "response" and give it a content type...
        final StringEntity stringEntity = new StringEntity(string);
        stringEntity.setContentType("text/text");

        // Create a response...
        final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
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
