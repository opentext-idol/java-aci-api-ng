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
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.autonomy.aci.client.transport.impl.AciResponseInputStreamImpl;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * JUnit test for <tt>com.autonomy.aci.client.services.impl.DocumentProcessor</tt>.
 */
public class DocumentProcessorTest {

    /**
     * This is the thing we're testing...
     */
    private static DocumentProcessor processor;

    @BeforeClass
    public static void createDocumentProcessor() {
        // Create the processor...
        processor = new DocumentProcessor();
    }

    @Test
    public void testConvertACIResponseToDOMSAXException() throws AciErrorException, IOException {
        try {
            // Setup with a proper XML response file...
            final ClassicHttpResponse response = new BasicClassicHttpResponse(200);
            response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/MalformedAciException.xml"), ContentType.APPLICATION_XML));

            // Set the AciResponseInputStream...
            final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

            // Process...
            processor.process(stream);
            fail("Should have raised an ProcessorException.");
        } catch (final ProcessorException pe) {
            // Check for the correct causes...
            assertThat("Cause not correct.", pe.getCause(), is(instanceOf(SAXException.class)));
        }
    }

    @Test
    public void testConvertACIResponseToDOMIOException() throws IOException, AciErrorException {
        try {
            final AciResponseInputStream mockAciResponseInputStream = mock(AciResponseInputStream.class);
            doThrow(IOException.class).when(mockAciResponseInputStream);

            // Process...
            processor.process(mockAciResponseInputStream);
            fail("Should have raised an ProcessorException.");
        } catch (final ProcessorException pe) {
            // Check for the correct causes...
            assertThat("Cause not correct.", pe.getCause(), is(instanceOf(IOException.class)));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCheckACIResponseForErrorWithErrorResponse() throws IOException, ProcessorException {
        try {
            // Setup with a error response file...
            final ClassicHttpResponse response = new BasicClassicHttpResponse(200);
            response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/AciException-1.xml"), ContentType.APPLICATION_XML));

            // Set the AciResponseInputStream...
            final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

            // Process...
            processor.process(stream);
            fail("Should have raised an AciErrorException.");
        } catch (final AciErrorException aee) {
            // Check its properties...
            assertThat("errorId property not as expected.", aee.getErrorId(), is(equalTo("AutonomyIDOLServerWOBBLE1")));
            assertThat("errorString property not as expected.", aee.getErrorString(), is(equalTo("ERROR")));
            assertThat("errorDescription property not as expected.", aee.getErrorDescription(), is(equalTo("The requested action was not recognised")));
            assertThat("errorCode property not as expected.", aee.getErrorCode(), is(equalTo("ERRORNOTIMPLEMENTED")));
            assertThat("errorTime property not as expected.", DateFormatUtils.format(aee.getErrorTime(), "dd MMM yy HH:mm:ss"), is(equalTo("06 Feb 06 17:03:54")));
        }
    }

    @Test
    public void testCheckACIResponseForErrorWithBadDateErrorResponse() throws IOException, ProcessorException {
        try {
            // Setup with a error response file...
            final ClassicHttpResponse response = new BasicClassicHttpResponse(200);
            response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/AciException-2.xml"), ContentType.APPLICATION_XML));

            // Set the AciResponseInputStream...
            final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

            // Process...
            processor.process(stream);
            fail("Should have raised an AciErrorException.");
        } catch (final AciErrorException aee) {
            assertThat("errorTime property not as expected.", aee.getErrorTime(), is(nullValue()));
        }
    }

    @Test
    public void testProcessor() throws IOException, ProcessorException, AciErrorException {
        // Setup with a error response file...
        final ClassicHttpResponse response = new BasicClassicHttpResponse(200);
        response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/GetVersion.xml"), ContentType.APPLICATION_XML));

        // Set the AciResponseInputStream...
        final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

        // Process...
        final Document document = processor.process(stream);
        assertThat("Document is null", document, is(notNullValue()));
    }

}
