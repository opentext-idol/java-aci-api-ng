/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.impl;

import com.autonomy.aci.client.TestEncryptionCodec;
import com.autonomy.aci.client.ReflectionTestUtils;
import com.autonomy.aci.client.services.ProcessorException;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.lang.reflect.Field;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * The majority of the {@code AbstractEncryptedResponseProcessor} class is tested via it's implementations, we're just
 * testing the error cases.
 */
public class AbstractEncryptedResponseProcessorTest {

    @Test(expected = ProcessorException.class)
    @SuppressWarnings("unchecked")
    public void testFailToCreateXMLStreamReader() throws NoSuchFieldException, XMLStreamException, IllegalAccessException {
        final AbstractEncryptedResponseProcessorImpl processor = new AbstractEncryptedResponseProcessorImpl();

        final Field field = ReflectionTestUtils.getAccessibleField(AbstractEncryptedResponseProcessor.class, "xmlInputFactory");
        field.set(processor, when(mock(XMLInputFactory.class).createXMLStreamReader(any(InputStream.class))).thenThrow(XMLStreamException.class).getMock());

        processor.process(mock(InputStream.class));
        fail("Should have thrown a ProcessorException...");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testXMLStreamReaderClosed() throws NoSuchFieldException, XMLStreamException, IllegalAccessException {
        final AbstractEncryptedResponseProcessorImpl processor = new AbstractEncryptedResponseProcessorImpl();

        final Field field = ReflectionTestUtils.getAccessibleField(AbstractEncryptedResponseProcessor.class, "xmlInputFactory");
        final XMLStreamReader mockXmlStreamReader = when(mock(XMLStreamReader.class).hasNext()).thenThrow(ProcessorException.class).getMock();
        field.set(processor, when(mock(XMLInputFactory.class).createXMLStreamReader(any(InputStream.class))).thenReturn(mockXmlStreamReader).getMock());

        try {
            processor.process(mock(InputStream.class));
        } catch (final ProcessorException e) {
            verify(mockXmlStreamReader).close();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testXMLStreamReaderErrorOnClose() throws NoSuchFieldException, XMLStreamException, IllegalAccessException {
        final AbstractEncryptedResponseProcessorImpl processor = new AbstractEncryptedResponseProcessorImpl();

        final Field field = ReflectionTestUtils.getAccessibleField(AbstractEncryptedResponseProcessor.class, "xmlInputFactory");
        final XMLStreamReader mockXmlStreamReader = when(mock(XMLStreamReader.class).hasNext()).thenThrow(ProcessorException.class).getMock();
        doThrow(XMLStreamException.class).when(mockXmlStreamReader).close();
        field.set(processor, when(mock(XMLInputFactory.class).createXMLStreamReader(any(InputStream.class))).thenReturn(mockXmlStreamReader).getMock());

        try {
            processor.process(mock(InputStream.class));
        } catch (final ProcessorException e) {
            verify(mockXmlStreamReader).close();
        }
    }

    private class AbstractEncryptedResponseProcessorImpl extends AbstractEncryptedResponseProcessor<Boolean> {

        public AbstractEncryptedResponseProcessorImpl() {
            super(new TestEncryptionCodec(), "UTF-8");
        }

        @Override
        Boolean process(final XMLStreamReader xmlStreamReader) throws XMLStreamException {
            return xmlStreamReader.hasNext();
        }

    }

}
