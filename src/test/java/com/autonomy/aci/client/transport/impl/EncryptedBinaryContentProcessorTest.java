/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.impl;

import com.autonomy.aci.client.TestEncryptionCodec;
import com.autonomy.aci.client.ReflectionTestUtils;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.EncryptionCodec;
import com.autonomy.aci.client.transport.EncryptionCodecException;
import com.autonomy.aci.client.util.IOUtils;
import com.hp.autonomy.test.xml.XmlTestUtils;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class EncryptedBinaryContentProcessorTest {

    @Test
    public void testProcess() throws XMLStreamException, IOException, NoSuchAlgorithmException {
        final ByteArrayInputStream byteArrayInputStream = new EncryptedBinaryContentProcessor(new TestEncryptionCodec(), "UTF-8")
                .process(XmlTestUtils.getResourceAsXMLStreamReader("/com/autonomy/aci/client/transport/impl/EncryptedBinaryContent.xml"));

        // Create a hash of the binary content so we don't have to check byte for byte...
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.getInstance().copy(byteArrayInputStream, byteArrayOutputStream);
        final MessageDigest md = MessageDigest.getInstance("MD5");
        assertThat(new BigInteger(md.digest(byteArrayOutputStream.toByteArray())).toString(16), is(equalTo("-11dd06c110a78fb8c3d2c72ff0289bd3")));
    }

    @Test(expected = ProcessorException.class)
    @SuppressWarnings("unchecked")
    public void testProcessXMLStreamException() throws XMLStreamException, IOException {
        final XMLStreamReader mockXmlStreamReader = when(mock(XMLStreamReader.class).hasNext()).thenThrow(XMLStreamException.class).getMock();
        new EncryptedBinaryContentProcessor(new TestEncryptionCodec(), "UTF-8").process(mockXmlStreamReader);
        fail("Should have thrown a ProcessorException...");
    }

    @Test(expected = ProcessorException.class)
    @SuppressWarnings("unchecked")
    public void testProcessEncryptionCodecException() throws XMLStreamException, IOException, EncryptionCodecException {
        final EncryptionCodec mockEncryptionCodec = when(mock(EncryptionCodec.class).decrypt((byte[]) any())).thenThrow(EncryptionCodecException.class).getMock();
        new EncryptedBinaryContentProcessor(mockEncryptionCodec, "UTF-8")
                .process(XmlTestUtils.getResourceAsXMLStreamReader("/com/autonomy/aci/client/transport/impl/EncryptedBinaryContent.xml"));
        fail("Should have thrown a ProcessorException...");
    }

    @Test(expected = ProcessorException.class)
    @SuppressWarnings("unchecked")
    public void testProcessIOException() throws NoSuchFieldException, IllegalAccessException, IOException, XMLStreamException {
        final EncryptedBinaryContentProcessor processor = new EncryptedBinaryContentProcessor(new TestEncryptionCodec(), "UTF-8");

        final ByteArrayOutputStream mockByteArrayOutputStream = mock(ByteArrayOutputStream.class);
        doThrow(IOException.class).when(mockByteArrayOutputStream).write((byte[]) any());

        final Field field = ReflectionTestUtils.getAccessibleField(EncryptedBinaryContentProcessor.class, "buffer");
        field.set(processor, mockByteArrayOutputStream);

        processor.process(XmlTestUtils.getResourceAsXMLStreamReader("/com/autonomy/aci/client/transport/impl/EncryptedBinaryContent.xml"));
        fail("Should have thrown a ProcessorException...");
    }

}
