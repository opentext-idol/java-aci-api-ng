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

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * JUnit test class for the <code>com.autonomy.aci.client.transport.impl.AciResponseInputStreamTest</code> class.
 */
public class AciResponseInputStreamTest {

    /**
     * The mock <code>HttpMethod</code> to use for testing.
     */
    private ClassicHttpResponse httpResponse;

    @Before
    public void setupHttpResponse() {
        // Create it...
        httpResponse = new BasicClassicHttpResponse(200);
    }

    @Test
    public void testConstructor() throws IOException {
        // Mock an entity and create the ACIResponseInputStream object...
        httpResponse.setEntity(mock(HttpEntity.class));
        new AciResponseInputStreamImpl(httpResponse);
    }

    @Test(expected = IOException.class)
    public void testConstructorIOException() throws IOException {
        final ClassicHttpResponse mockHttpResponse = mock(ClassicHttpResponse.class);
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
        final StringEntity stringEntity = new StringEntity("This is a test...", ContentType.TEXT_PLAIN, "gzip", false);
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
        final StringEntity stringEntity = new StringEntity("This is a test...", ContentType.TEXT_XML);
        httpResponse.setEntity(stringEntity);

        // Create the AciResponseInputStreamImpl object...
        final AciResponseInputStreamImpl stream = new AciResponseInputStreamImpl(httpResponse);
        assertThat("Content-Type header wrong", stream.getContentType(), is(startsWith("text/xml")));
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
