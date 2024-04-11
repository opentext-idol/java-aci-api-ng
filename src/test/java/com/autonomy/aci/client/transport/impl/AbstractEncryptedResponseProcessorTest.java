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

package com.autonomy.aci.client.transport.impl;

import com.autonomy.aci.client.ReflectionTestUtils;
import com.autonomy.aci.client.TestEncryptionCodec;
import com.autonomy.aci.client.services.ProcessorException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * The majority of the {@code AbstractEncryptedResponseProcessor} class is tested via it's implementations, we're just
 * testing the error cases.
 */
public class AbstractEncryptedResponseProcessorTest {

    @Rule
    public final TemporaryFolder tempDirs = new TemporaryFolder();

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

    @Test(expected = ProcessorException.class)
    public void testXMLStreamReaderErrorsOnXXE() throws IOException {
        final XXEResponseProcessorImpl processor = new XXEResponseProcessorImpl();

        String osfile = tempDirs.newFile().toURI().toString();
        String tmpl = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY xxe SYSTEM \"%s\" >]><foo>&xxe;</foo>";
        String xml = String.format(tmpl, StringEscapeUtils.escapeXml(osfile));
        String content = processor.process(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        fail("Should have thrown a ProcessorException. Read: " + content);
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

    /**
     * Implementation of a response processor to test secure XML parsing. It must parse far
     *  enough into the document to encounter an external entity which will result in the
     *  underlying parser throwing an exception.
     */
    private static class XXEResponseProcessorImpl extends AbstractEncryptedResponseProcessor<String> {

        public XXEResponseProcessorImpl() {
            super(new TestEncryptionCodec(), "UTF-8");
        }

        @Override
        String process(final XMLStreamReader xmlStreamReader) throws XMLStreamException {
            StringBuilder bldr = new StringBuilder();
            while(xmlStreamReader.hasNext()) {
                int eventType = xmlStreamReader.next();
                if (eventType == XMLEvent.START_ELEMENT && xmlStreamReader.getLocalName().equals("foo")) {
                    bldr.append(xmlStreamReader.getElementText());
                }
            }
            return bldr.toString();
        }

    }
}
