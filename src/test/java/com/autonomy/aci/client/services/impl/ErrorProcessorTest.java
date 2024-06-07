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
import com.hp.autonomy.test.xml.XmlTestUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * JUnit tests for <code>com.autonomy.aci.client.services.impl.ErrorProcessor</code>.
 */
public class ErrorProcessorTest {

    /**
     * This is the thing we're testing...
     */
    private final ErrorProcessor processor = new ErrorProcessor();

    @Test
    public void testFullErrorResponse() throws XMLStreamException, AciErrorException, ProcessorException {
        try {
            // Execute the processor...
            processor.process(XmlTestUtils.getResourceAsXMLStreamReader("/com/autonomy/aci/client/services/processor/errorProcessorTestFullErrorResponse.xml"));
            fail("Should have thrown an AciErrorException");
        } catch (final AciErrorException exception) {
            // Check it...
            assertThat("errorId property not as expected.", exception.getErrorId(), is(equalTo("AutonomyIDOLServerWOBBLE1")));
            assertThat("rawErrorId property not as expected.", exception.getRawErrorId(), is(nullValue()));
            assertThat("errorString property not as expected.", exception.getErrorString(), is(equalTo("ERROR")));
            assertThat("errorDescription property not as expected.", exception.getErrorDescription(), is(equalTo("The requested action was not recognised")));
            assertThat("errorCode property not as expected.", exception.getErrorCode(), is(equalTo("ERRORNOTIMPLEMENTED")));
            assertThat("errorTime property not as expected.", DateFormatUtils.format(exception.getErrorTime(), "dd MMM yy HH:mm:ss"), is(equalTo("06 Feb 06 17:03:54")));
        }
    }

    @Test
    public void testFullErrorResponseRawErrorId() throws XMLStreamException, AciErrorException, ProcessorException {
        try {
            // Execute the processor...
            processor.process(XmlTestUtils.getResourceAsXMLStreamReader("/com/autonomy/aci/client/services/processor/errorProcessorTestFullErrorResponseRawErrorId.xml"));
            fail("Should have thrown an AciErrorException");
        } catch (final AciErrorException exception) {
            // Check it...
            assertThat("errorId property not as expected.", exception.getErrorId(), is(equalTo("DAHGETQUERYTAGVALUES525")));
            assertThat("rawErrorId property not as expected.", exception.getRawErrorId(), is(equalTo("0x20D")));
            assertThat("errorString property not as expected.", exception.getErrorString(), is(equalTo("No valid parametric fields")));
            assertThat("errorDescription property not as expected.", exception.getErrorDescription(), is(equalTo("The fieldname parameter contained no valid parametric fields")));
            assertThat("errorCode property not as expected.", exception.getErrorCode(), is(equalTo("ERRORPARAMINVALID")));
            assertThat("errorTime property not as expected.", DateFormatUtils.format(exception.getErrorTime(), "dd MMM yy HH:mm:ss"), is(equalTo("09 Jul 08 15:48:22")));
        }
    }

    @Test
    public void testBadErrorTimeResponse() throws XMLStreamException, AciErrorException, ProcessorException {
        try {
            // Execute the processor...
            processor.process(XmlTestUtils.getResourceAsXMLStreamReader("/com/autonomy/aci/client/services/processor/errorProcessorTestBadErrorTimeResponse.xml"));
            fail("Should have thrown an AciErrorException");
        } catch (final AciErrorException exception) {
            // Check it...
            assertThat("errorId property not as expected.", exception.getErrorId(), is(equalTo("AutonomyIDOLServerWOBBLE1")));
            assertThat("rawErrorId property not as expected.", exception.getRawErrorId(), is(nullValue()));
            assertThat("errorString property not as expected.", exception.getErrorString(), is(equalTo("ERROR")));
            assertThat("errorDescription property not as expected.", exception.getErrorDescription(), is(equalTo("The requested action was not recognised")));
            assertThat("errorCode property not as expected.", exception.getErrorCode(), is(equalTo("ERRORNOTIMPLEMENTED")));
            assertThat("errorTime property not as expected.", exception.getErrorTime(), is(nullValue()));
        }
    }

    @Test
    public void testPartialErrorResponse() throws XMLStreamException, AciErrorException, ProcessorException {
        try {
            // Execute the processor...
            processor.process(XmlTestUtils.getResourceAsXMLStreamReader("/com/autonomy/aci/client/services/processor/errorProcessorTestPartialErrorResponse.xml"));
            fail("Should have thrown an AciErrorException");
        } catch (final AciErrorException exception) {
            // Check it...
            assertThat("errorId property not as expected.", exception.getErrorId(), is(equalTo("AutonomyIDOLServerWOBBLE1")));
            assertThat("rawErrorId property not as expected.", exception.getRawErrorId(), is(nullValue()));
            assertThat("errorString property not as expected.", exception.getErrorString(), is(equalTo("ERROR")));
            assertThat("errorDescription property not as expected.", exception.getErrorDescription(), is(equalTo("The requested action was not recognised")));
            assertThat("errorCode property not as expected.", exception.getErrorCode(), is(equalTo("ERRORNOTIMPLEMENTED")));
            assertThat("errorTime property not as expected.", DateFormatUtils.format(exception.getErrorTime(), "dd MMM yy HH:mm:ss"), is(equalTo("06 Feb 06 17:03:54")));
        }
    }

    @Test(expected = ProcessorException.class)
    public void testXMLStreamException() throws XMLStreamException, AciErrorException, ProcessorException {
        final XMLStreamReader mockXmlStreamReader = mock(XMLStreamReader.class);
        when(mockXmlStreamReader.hasNext()).thenThrow(new XMLStreamException("JUnit test exception"));

        processor.process(mockXmlStreamReader);

        fail("Should have raised a ProcessorException.");
    }

}
