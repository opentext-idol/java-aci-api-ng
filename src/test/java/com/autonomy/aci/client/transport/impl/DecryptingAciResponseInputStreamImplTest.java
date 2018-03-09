/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.impl;

import com.autonomy.aci.client.TestEncryptionCodec;
import com.autonomy.aci.client.ReflectionTestUtils;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class DecryptingAciResponseInputStreamImplTest {

    private static TestEncryptionCodec encryptionCodec;

    private static String encryptedTextResponse;

    private static String unencryptedTextResponse;

    private static String encryptedBinaryResponse;

    private BasicHttpResponse httpResponse;

    private AciServerDetails serverDetails;

    @BeforeClass
    public static void createCommonTestStuff() throws IOException {
        encryptionCodec = new TestEncryptionCodec();

        // Get the string based responses...
        encryptedTextResponse = toString(DecryptingAciResponseInputStreamImplTest.class.getResourceAsStream("/com/autonomy/aci/client/transport/impl/EncryptedResponse.xml"));
        unencryptedTextResponse = toString(DecryptingAciResponseInputStreamImplTest.class.getResourceAsStream("/com/autonomy/aci/client/transport/impl/UnencryptedResponse.xml"));
        encryptedBinaryResponse = toString(DecryptingAciResponseInputStreamImplTest.class.getResourceAsStream("/com/autonomy/aci/client/transport/impl/EncryptedBinaryContent.xml"));
    }

    @Before
    public void createHttpResponse() {
        httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
    }

    @Before
    public void createAciServerDetails() {
        serverDetails = new AciServerDetails();
        serverDetails.setHost("localhost");
        serverDetails.setPort(9000);
        serverDetails.setEncryptionCodec(encryptionCodec);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConstructorNoAutnContentType() throws IOException {
        // Load the HttpMethod with a response...
        httpResponse.setEntity(new StringEntity(encryptedTextResponse));
        httpResponse.setHeader(new BasicHeader("Content-Type", "text/xml"));

        // There is no AUTN-Content-Type so the stream should just contain the raw encrypted response... 
        final DecryptingAciResponseInputStreamImpl stream = new DecryptingAciResponseInputStreamImpl(serverDetails, httpResponse);
        assertThat("Decrypted stream not correct", toString(stream), is(equalTo(encryptedTextResponse)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConstructorTextContent() throws IOException {
        // Load the HttpMethod with a response...
        final StringEntity stringEntity = new StringEntity(encryptedTextResponse);
        stringEntity.setContentType("text/xml");

        httpResponse.setEntity(stringEntity);
        httpResponse.setHeaders(new Header[]{new BasicHeader("AUTN-Content-Type", "text/xml"), new BasicHeader("Content-Type", "text/xml")});

        // This should decrypt the response...
        final DecryptingAciResponseInputStreamImpl stream = new DecryptingAciResponseInputStreamImpl(serverDetails, httpResponse);
        assertThat(toString(stream), is(equalTo(unencryptedTextResponse)));

        // Check that wqe have the right content type...
        assertThat(stream.getContentType(), is(equalTo("text/xml")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConstructorBinaryContent() throws IOException, NoSuchAlgorithmException {
        // Load the HttpMethod with a response...
        final StringEntity stringEntity = new StringEntity(encryptedBinaryResponse);
        stringEntity.setContentType("text/xml");

        httpResponse.setEntity(stringEntity);
        httpResponse.setHeaders(new Header[]{new BasicHeader("AUTN-Content-Type", "image/jpeg"), new BasicHeader("Content-Type", "text/xml")});

        // This should decrypt the response...
        final DecryptingAciResponseInputStreamImpl stream = new DecryptingAciResponseInputStreamImpl(serverDetails, httpResponse);

        // Create a hash of the binary content so we don't have to check byte for byte...
        final MessageDigest md = MessageDigest.getInstance("MD5");
        final String hash = new BigInteger(md.digest(toBytes(stream))).toString(16);

        assertThat(hash, is(equalTo("-11dd06c110a78fb8c3d2c72ff0289bd3")));

        // Check that wqe have the right content type...
        assertThat(stream.getContentType(), is(equalTo("image/jpeg")));
    }

    @Test(expected = IOException.class)
    @SuppressWarnings("unchecked")
    public void testConstructorProcessorException() throws IOException {
        final InputStream mockInputStream = when(mock(InputStream.class).read()).thenThrow(IOException.class).getMock();
        final HttpEntity mockHttpEntity = when(mock(HttpEntity.class).getContent()).thenReturn(mockInputStream).getMock();

        httpResponse.setEntity(mockHttpEntity);
        httpResponse.setHeaders(new Header[]{new BasicHeader("AUTN-Content-Type", "text/xml"), new BasicHeader("Content-Type", "text/xml")});

        // There is no AUTN-Content-Type so the stream should just contain the raw encr    @Test
        new DecryptingAciResponseInputStreamImpl(serverDetails, httpResponse);
        fail("Should've thrown a IOException...");
    }

    @Test
    public void testVerifyDelegation() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Load the HttpMethod with a response...
        httpResponse.setEntity(new StringEntity(encryptedTextResponse));
        httpResponse.setHeader(new BasicHeader("Content-Type", "text/xml"));

        final DecryptingAciResponseInputStreamImpl decryptingAciResponseInputStream = new DecryptingAciResponseInputStreamImpl(serverDetails, httpResponse);
        final Field field = ReflectionTestUtils.getAccessibleField(DecryptingAciResponseInputStreamImpl.class, "decryptedResponse");

        final ByteArrayInputStream mockByteArrayInputStream = mock(ByteArrayInputStream.class);
        field.set(decryptingAciResponseInputStream, mockByteArrayInputStream);

        decryptingAciResponseInputStream.read();
        verify(mockByteArrayInputStream).read();

        decryptingAciResponseInputStream.skip(10);
        verify(mockByteArrayInputStream).skip(10);

        decryptingAciResponseInputStream.available();
        verify(mockByteArrayInputStream).available();

        decryptingAciResponseInputStream.mark(10);
        verify(mockByteArrayInputStream).mark(10);

        decryptingAciResponseInputStream.reset();
        verify(mockByteArrayInputStream).reset();

        decryptingAciResponseInputStream.markSupported();
        verify(mockByteArrayInputStream).markSupported();
    }

    private static String toString(final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IOUtils.getInstance().copy(inputStream, buffer);
        return buffer.toString("UTF-8");
    }

    private static byte[] toBytes(final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IOUtils.getInstance().copy(inputStream, buffer);
        return buffer.toByteArray();
    }

}
