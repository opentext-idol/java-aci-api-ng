/*
 * Copyright 2006-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.impl;

import com.autonomy.aci.client.TestEncryptionCodec;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.EncryptionCodec;
import com.autonomy.aci.client.transport.EncryptionCodecException;
import com.hp.autonomy.test.xml.XmlTestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class EncryptedTextContentProcessorTest {

    @Test
    public void testProcess() throws XMLStreamException, IOException {
        final ByteArrayInputStream byteArrayInputStream = new EncryptedTextContentProcessor(new TestEncryptionCodec(), "UTF-8")
                .process(XmlTestUtils.getResourceAsXMLStreamReader("/com/autonomy/aci/client/transport/impl/EncryptedResponse.xml"));

        assertThat(
                IOUtils.toString(byteArrayInputStream),
                is(equalTo(IOUtils.toString(getClass().getResourceAsStream("/com/autonomy/aci/client/transport/impl/UnencryptedResponse.xml"))))
        );
    }

    @Test(expected = ProcessorException.class)
    public void testProcessXMLStreamException() throws XMLStreamException, IOException {
        final XMLStreamReader mockXmlStreamReader = mock(XMLStreamReader.class);
        doThrow(XMLStreamException.class).when(mockXmlStreamReader).getVersion();

        new EncryptedTextContentProcessor(new TestEncryptionCodec(), "UTF-8").process(mockXmlStreamReader);
        fail("Should have thrown a ProcessorException...");
    }

    @Test(expected = ProcessorException.class)
    public void testProcessIOException() throws XMLStreamException, IOException {
        new EncryptedTextContentProcessor(new TestEncryptionCodec(), "WIBBLE").process(mock(XMLStreamReader.class));
        fail("Should have thrown a ProcessorException...");
    }

    @Test(expected = ProcessorException.class)
    @SuppressWarnings("unchecked")
    public void testProcessEncryptionCodecException() throws XMLStreamException, IOException, EncryptionCodecException {
        final EncryptionCodec mockEncryptionCodec = mock(EncryptionCodec.class);
        when(mockEncryptionCodec.decrypt((byte[]) any())).thenThrow(EncryptionCodecException.class);

        new EncryptedTextContentProcessor(mockEncryptionCodec, "UTF-8")
                .process(XmlTestUtils.getResourceAsXMLStreamReader("/com/autonomy/aci/client/transport/impl/EncryptedResponse.xml"));
        fail("Should have thrown a ProcessorException...");
    }

}
