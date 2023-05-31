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
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.HttpVersion;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
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

    @Test(expected = FactoryConfigurationError.class)
    public void testConvertACIResponseToDOMInvalidDocumentBuilderFactory() throws AciErrorException, IOException, ProcessorException {
        // Set a duff property for the DocumentBuilderFactory...
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.autonomy.DuffDocumentBuilderFactory");

        try {
            // Setup with a proper XML response file...
            final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
            response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/GetVersion.xml"), -1));

            // Set the AciResponseInputStream...
            final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

            // Process...
            processor.process(stream);
            fail("Should have raised an ProcessorException.");
        } finally {
            // Remove the duff system property...
            System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
        }
    }

    @Test
    public void testConvertACIResponseToDOMParserConfigurationException() throws AciErrorException, IOException {
        // Set the property to be our mock implementation that will throw a ParserConfigurationException...
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.autonomy.aci.client.mock.MockDocumentBuilderFactory");

        try {
            // Setup with a proper XML response file...
            final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
            response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/GetVersion.xml"), -1));

            // Set the AciResponseInputStream...
            final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

            // Process...
            processor.process(stream);
            fail("Should have raised an ProcessorException.");
        } catch (final ProcessorException pe) {
            // Check for the correct causes...
            assertThat("Cause not correct.", pe.getCause(), is(instanceOf(ParserConfigurationException.class)));
        } finally {
            // Remove the duff system property...
            System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
        }
    }

    @Test
    public void testConvertACIResponseToDOMSAXException() throws AciErrorException, IOException {
        try {
            // Setup with a proper XML response file...
            final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
            response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/MalformedAciException.xml"), -1));

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
            final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
            response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/AciException-1.xml"), -1));

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
            final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
            response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/AciException-2.xml"), -1));

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
        final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        response.setEntity(new InputStreamEntity(getClass().getResourceAsStream("/GetVersion.xml"), -1));

        // Set the AciResponseInputStream...
        final AciResponseInputStream stream = new AciResponseInputStreamImpl(response);

        // Process...
        final Document document = processor.process(stream);
        assertThat("Document is null", document, is(notNullValue()));
    }

}
