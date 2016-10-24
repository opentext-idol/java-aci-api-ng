/*
 * Copyright 2006-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * JUnit test class for the <tt>com.autonomy.aci.client.transport.impl.AciResponseInputStreamTest</tt> class.
 */
public class AciResponseInputStreamTest {

    /**
     * The mock <tt>HttpMethod</tt> to use for testing.
     */
    private BasicHttpResponse httpResponse;

    @Before
    public void setupHttpResponse() {
        // Create it...
        httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
    }

    @Test
    public void testConstructor() throws IOException {
        // Mock an entity and create the ACIResponseInputStream object...
        httpResponse.setEntity(mock(HttpEntity.class));
        final AciResponseInputStreamImpl stream = new AciResponseInputStreamImpl(httpResponse);

        // Should have stored the response for future use...
        assertThat(httpResponse, is(sameInstance(stream.getMethod())));
    }

    @Test(expected = IOException.class)
    public void testConstructorIOException() throws IOException {
        final HttpResponse mockHttpResponse = mock(HttpResponse.class);
        final HttpEntity mockHttpEntity = mock(HttpEntity.class);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
        doThrow(IOException.class).when(mockHttpEntity).getContent();

        new AciResponseInputStreamImpl(mockHttpResponse);
        fail("Should have thrown an IOException.");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetStatusCode() throws IOException {
        httpResponse.setEntity(new StringEntity("This is a test..."));
        final AciResponseInputStreamImpl stream = new AciResponseInputStreamImpl(httpResponse);
        assertThat(stream.getStatusCode(), is(200));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetContentEncoding() throws IOException {
        final StringEntity stringEntity = new StringEntity("This is a test...");
        stringEntity.setContentEncoding("gzip");
        httpResponse.setEntity(stringEntity);

        final AciResponseInputStreamImpl stream = new AciResponseInputStreamImpl(httpResponse);
        assertThat(stream.getContentEncoding(), is(equalTo("gzip")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetContentLength() throws IOException {
        httpResponse.setEntity(new StringEntity("This is a test..."));
        final AciResponseInputStreamImpl stream = new AciResponseInputStreamImpl(httpResponse);
        assertThat(stream.getContentLength(), is(equalTo((long) "This is a test...".length())));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetContentType() throws IOException {
        final StringEntity stringEntity = new StringEntity("This is a test...");
        stringEntity.setContentType("text/xml");
        httpResponse.setEntity(stringEntity);

        // Create the AciResponseInputStreamImpl object...
        final AciResponseInputStreamImpl stream = new AciResponseInputStreamImpl(httpResponse);
        assertThat("Content-Type header wrong", "text/xml", is(equalTo(stream.getContentType())));
    }

    @Test
    public void testMethodProperty() throws IOException {
        // Create the AciResponseInputStreamImpl object...
        httpResponse.setEntity(new StringEntity("This is a test..."));
        final AciResponseInputStreamImpl stream = new AciResponseInputStreamImpl(httpResponse);
        assertThat("HttpResponse objects are different", httpResponse, is(sameInstance(stream.getMethod())));

        // Set it to null and check...
        stream.setMethod(null);
        assertThat("method property not as expected", stream.getMethod(), is(nullValue()));

        // Set the mock method agian and check...
        stream.setMethod(httpResponse);
        assertThat("method property not as expected", httpResponse, is(sameInstance(stream.getMethod())));
    }

    @Test
    public void testCloseMethod() throws IOException {
        // Mock the http entity and its input stream...
        final HttpEntity mockHttpEntity = mock(HttpEntity.class);
        final InputStream mockInputStream = mock(InputStream.class);
        when(mockHttpEntity.getContent()).thenReturn(mockInputStream);
        httpResponse.setEntity(mockHttpEntity);

        // Create the AciResponseInputStreamImpl object and close it...
        final AciResponseInputStreamImpl stream = new AciResponseInputStreamImpl(httpResponse);
        stream.close();

        // Verify that the correct things get called...
        verify(mockHttpEntity).isStreaming();
        verify(mockInputStream).close();
    }

    @Test(expected = IOException.class)
    public void testCloseMethodIOException() throws IOException {
        // Mock the http entity and its input stream...
        final HttpEntity mockHttpEntity = mock(HttpEntity.class);
        final InputStream mockInputStream = mock(InputStream.class);
        when(mockHttpEntity.getContent()).thenReturn(mockInputStream);
        doThrow(new IOException("JUnit test exception")).when(mockInputStream).close();
        httpResponse.setEntity(mockHttpEntity);

        // Create the AciResponseInputStreamImpl object and close it...
        final AciResponseInputStreamImpl stream = new AciResponseInputStreamImpl(httpResponse);
        stream.close();
    }

}
