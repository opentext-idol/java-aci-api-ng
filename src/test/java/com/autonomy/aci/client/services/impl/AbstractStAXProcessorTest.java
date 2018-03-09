/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.services.impl;

import com.autonomy.aci.client.ReflectionTestUtils;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.hp.autonomy.test.xml.XmlTestUtils;
import org.apache.commons.lang.SerializationUtils;
import org.junit.After;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for <tt>com.autonomy.aci.client.services.impl.AbstractStAXProcessor</tt>.
 */
public class AbstractStAXProcessorTest {

    @After
    public void tearDown() {
        System.clearProperty(XMLInputFactory.IS_NAMESPACE_AWARE);
        System.clearProperty(XMLInputFactory.IS_VALIDATING);
        System.clearProperty(XMLInputFactory.IS_COALESCING);
        System.clearProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES);
        System.clearProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES);
        System.clearProperty(XMLInputFactory.SUPPORT_DTD);
    }

    @Test
    public void testReadObject() throws NoSuchFieldException, IllegalAccessException {
        AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);

        final Field field = ReflectionTestUtils.getAccessibleField(AbstractStAXProcessor.class, "xmlInputFactory");
        assertThat(field.get(abstractStAXProcessor), is(notNullValue()));

        // Serialise then deserialise the spy and check the XMLInputFactory has been recreated...
        final byte[] serialisedSpy = SerializationUtils.serialize(abstractStAXProcessor);
        abstractStAXProcessor = (AbstractStAXProcessor<?>) SerializationUtils.deserialize(serialisedSpy);
        assertThat(field.get(abstractStAXProcessor), is(notNullValue()));
    }

    @Test(expected = ProcessorException.class)
    public void testProcessBadContentType() {
        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        abstractStAXProcessor.process(when(mock(AciResponseInputStream.class).getContentType()).thenReturn("image/jpeg").<AciResponseInputStream>getMock());

        fail("Should have thrown a ProcessorException.");
    }

    @Test
    public void testProcessBadInputStream() throws IOException {
        final AciResponseInputStream mockAciResponseInputStream = when(mock(AciResponseInputStream.class).getContentType()).thenReturn("text/xml").getMock();

        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        abstractStAXProcessor.process(mockAciResponseInputStream);

        verify(mockAciResponseInputStream).getContentType();
        verify(mockAciResponseInputStream, times(2)).read((byte[]) any(), anyInt(), anyInt());
        verifyNoMoreInteractions(mockAciResponseInputStream);
    }

    @Test(expected = ProcessorException.class)
    @SuppressWarnings("unchecked")
    public void testCantCreateXMLStreamReader() throws IOException, NoSuchFieldException, XMLStreamException, IllegalAccessException {
        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);

        final Field field = ReflectionTestUtils.getAccessibleField(AbstractStAXProcessor.class, "xmlInputFactory");
        field.set(
                abstractStAXProcessor,
                when(mock(XMLInputFactory.class).createXMLStreamReader(any(AciResponseInputStream.class))).thenThrow(XMLStreamException.class).getMock()
        );

        abstractStAXProcessor.process(when(mock(AciResponseInputStream.class).getContentType()).thenReturn("text/xml").<AciResponseInputStream>getMock());
        fail("Should've thrown a ProcessorException...");
    }

    @Test(expected = ProcessorException.class)
    public void testCloseCalledAfterException() throws NoSuchFieldException, XMLStreamException, IllegalAccessException {
        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        doThrow(XMLStreamException.class).when(abstractStAXProcessor).process(any(XMLStreamReader.class));

        final XMLStreamReader mockXmlStreamReader = mock(XMLStreamReader.class);

        final Field field = ReflectionTestUtils.getAccessibleField(AbstractStAXProcessor.class, "xmlInputFactory");
        field.set(
                abstractStAXProcessor,
                when(mock(XMLInputFactory.class).createXMLStreamReader(any(AciResponseInputStream.class))).thenReturn(mockXmlStreamReader).getMock()
        );

        abstractStAXProcessor.process(when(mock(AciResponseInputStream.class).getContentType()).thenReturn("text/xml").<AciResponseInputStream>getMock());
        verify(mockXmlStreamReader).close();
    }

    @Test
    public void testExceptionClosingXMLStreamReader() throws NoSuchFieldException, XMLStreamException, IllegalAccessException {
        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);

        final XMLStreamReader mockXmlStreamReader = mock(XMLStreamReader.class);
        doThrow(XMLStreamException.class).when(mockXmlStreamReader).close();

        final Field field = ReflectionTestUtils.getAccessibleField(AbstractStAXProcessor.class, "xmlInputFactory");
        field.set(
                abstractStAXProcessor,
                when(mock(XMLInputFactory.class).createXMLStreamReader(any(AciResponseInputStream.class))).thenReturn(mockXmlStreamReader).getMock()
        );

        abstractStAXProcessor.process(when(mock(AciResponseInputStream.class).getContentType()).thenReturn("text/xml").<AciResponseInputStream>getMock());
    }

    @Test
    public void testNullXMLStreamReader() throws NoSuchFieldException, XMLStreamException, IllegalAccessException {
        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);

        final Field field = ReflectionTestUtils.getAccessibleField(AbstractStAXProcessor.class, "xmlInputFactory");
        field.set(
                abstractStAXProcessor,
                when(mock(XMLInputFactory.class).createXMLStreamReader(any(AciResponseInputStream.class))).thenReturn(null).getMock()
        );

        abstractStAXProcessor.process(when(mock(AciResponseInputStream.class).getContentType()).thenReturn("text/xml").<AciResponseInputStream>getMock());
    }

    @Test
    public void testIsErrorResponseTrue() throws XMLStreamException {
        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        assertThat(abstractStAXProcessor.isErrorResponse(XmlTestUtils.getResourceAsXMLStreamReader("/AciException-1.xml")), is(true));
    }

    @Test
    public void testIsErrorResponseFalse() throws XMLStreamException {
        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        assertThat(abstractStAXProcessor.isErrorResponse(XmlTestUtils.getResourceAsXMLStreamReader("/GetVersion.xml")), is(false));
    }

    @Test(expected = XMLStreamException.class)
    public void testIsErrorResponseNoResponseElement() throws XMLStreamException {
        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        assertThat(abstractStAXProcessor.isErrorResponse(XmlTestUtils.getResourceAsXMLStreamReader("/logback-test.xml")), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessErrorResponseNoProcessorSet() {
        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        assertThat(abstractStAXProcessor.getErrorProcessor(), is(nullValue()));
        abstractStAXProcessor.processErrorResponse(mock(XMLStreamReader.class));
    }

    @Test
    public void testProcessErrorResponse() {
        final ErrorProcessor mockErrorProcessor = mock(ErrorProcessor.class);

        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        abstractStAXProcessor.setErrorProcessor(mockErrorProcessor);
        abstractStAXProcessor.processErrorResponse(mock(XMLStreamReader.class));

        verify(mockErrorProcessor).process(any(XMLStreamReader.class));
    }

    @Test(expected = XMLStreamException.class)
    public void testForwardToNextStartElement() throws XMLStreamException {
        final XMLStreamReader mockXmlStreamReader = mock(XMLStreamReader.class);
        when(mockXmlStreamReader.hasNext()).thenReturn(true, true, true, true, false);
        when(mockXmlStreamReader.next()).thenReturn(XMLEvent.START_DOCUMENT, XMLEvent.START_ELEMENT, XMLEvent.END_ELEMENT, XMLEvent.END_DOCUMENT);

        try {
            final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
            abstractStAXProcessor.forwardToNextStartElement(mockXmlStreamReader); // Should work...
            abstractStAXProcessor.forwardToNextStartElement(mockXmlStreamReader); // Should iterate out and thrown exception...
        } finally {
            verify(mockXmlStreamReader, times(5)).hasNext();
            verify(mockXmlStreamReader, times(4)).next();
            verifyNoMoreInteractions(mockXmlStreamReader);
        }
    }

    @Test(expected = XMLStreamException.class)
    public void testForwardToNextStartOrEndElement() throws XMLStreamException {
        final XMLStreamReader mockXmlStreamReader = mock(XMLStreamReader.class);
        when(mockXmlStreamReader.hasNext()).thenReturn(true, true, true, true, false);
        when(mockXmlStreamReader.next()).thenReturn(XMLEvent.START_DOCUMENT, XMLEvent.START_ELEMENT, XMLEvent.END_ELEMENT, XMLEvent.END_DOCUMENT);

        try {
            final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
            abstractStAXProcessor.forwardToNextStartOrEndElement(mockXmlStreamReader); // Should work, start element...
            abstractStAXProcessor.forwardToNextStartOrEndElement(mockXmlStreamReader); // Should work, end element...
            abstractStAXProcessor.forwardToNextStartOrEndElement(mockXmlStreamReader); // Should iterate out and thrown exception...
        } finally {
            verify(mockXmlStreamReader, times(5)).hasNext();
            verify(mockXmlStreamReader, times(4)).next();
            verifyNoMoreInteractions(mockXmlStreamReader);
        }
    }

    @Test(expected = XMLStreamException.class)
    public void testForwardToNamedStartElement() throws XMLStreamException {
        final XMLStreamReader mockXmlStreamReader = mock(XMLStreamReader.class);
        when(mockXmlStreamReader.hasNext()).thenReturn(true, true, true, true, true, true, false);
        when(mockXmlStreamReader.next()).thenReturn(XMLEvent.START_DOCUMENT, XMLEvent.START_ELEMENT, XMLEvent.END_ELEMENT, XMLEvent.START_ELEMENT, XMLEvent.END_ELEMENT, XMLEvent.END_DOCUMENT);
        when(mockXmlStreamReader.getLocalName()).thenReturn("wibble", "wobble");

        try {
            final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
            abstractStAXProcessor.forwardToNamedStartElement("wibble", mockXmlStreamReader); // Should work
            abstractStAXProcessor.forwardToNamedStartElement("wibble", mockXmlStreamReader); // Should iterate out and thrown exception...
        } finally {
            verify(mockXmlStreamReader, times(7)).hasNext();
            verify(mockXmlStreamReader, times(6)).next();
            verify(mockXmlStreamReader, times(2)).getLocalName();
            verifyNoMoreInteractions(mockXmlStreamReader);
        }
    }

    @Test
    public void testXMLInputFactorySystemProperties() throws NoSuchFieldException, IllegalAccessException {
        AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        final XMLInputFactory mockXmlInputFactory = mock(XMLInputFactory.class);

        final Field field = ReflectionTestUtils.getAccessibleField(AbstractStAXProcessor.class, "xmlInputFactory");
        field.set(abstractStAXProcessor, mockXmlInputFactory);

        // Check the defaults...
        abstractStAXProcessor.process(when(mock(AciResponseInputStream.class).getContentType()).thenReturn("text/xml").<AciResponseInputStream>getMock());
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_VALIDATING, false);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_COALESCING, false);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.SUPPORT_DTD, true);

        // Set different values via system properties...
        System.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, "true");
        System.setProperty(XMLInputFactory.IS_VALIDATING, "true");
        System.setProperty(XMLInputFactory.IS_COALESCING, "true");
        System.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, "false");
        System.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, "true");
        System.setProperty(XMLInputFactory.SUPPORT_DTD, "false");

        // Create a new spy...
        abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        field.set(abstractStAXProcessor, mockXmlInputFactory);

        // Check the values have changed when set...
        abstractStAXProcessor.process(when(mock(AciResponseInputStream.class).getContentType()).thenReturn("text/xml").<AciResponseInputStream>getMock());
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_VALIDATING, true);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_COALESCING, true);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    @Test
    public void testXMLInputFactoryPropertyAccessors() throws NoSuchFieldException, IllegalAccessException {
        // Check for the default values...
        final AbstractStAXProcessor<?> abstractStAXProcessor = spy(AbstractStAXProcessor.class);
        assertThat(abstractStAXProcessor.isNamespaceAware(), is(false));
        assertThat(abstractStAXProcessor.isValidating(), is(false));
        assertThat(abstractStAXProcessor.isCoalescing(), is(false));
        assertThat(abstractStAXProcessor.isReplacingEntityReferences(), is(true));
        assertThat(abstractStAXProcessor.isSupportingExternalEntities(), is(false));
        assertThat(abstractStAXProcessor.isSupportDtd(), is(true));

        // Set new values via the property accessors...
        abstractStAXProcessor.setNamespaceAware(true);
        abstractStAXProcessor.setValidating(true);
        abstractStAXProcessor.setCoalescing(true);
        abstractStAXProcessor.setReplacingEntityReferences(false);
        abstractStAXProcessor.setSupportingExternalEntities(true);
        abstractStAXProcessor.setSupportDtd(false);

        final XMLInputFactory mockXmlInputFactory = mock(XMLInputFactory.class);
        final Field field = ReflectionTestUtils.getAccessibleField(AbstractStAXProcessor.class, "xmlInputFactory");
        field.set(abstractStAXProcessor, mockXmlInputFactory);

        // Check the values have changed...
        abstractStAXProcessor.process(when(mock(AciResponseInputStream.class).getContentType()).thenReturn("text/xml").<AciResponseInputStream>getMock());
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_VALIDATING, true);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_COALESCING, true);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
        verify(mockXmlInputFactory).setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

}
