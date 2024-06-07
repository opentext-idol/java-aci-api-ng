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

package com.autonomy.aci.client.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for <code>com.autonomy.aci.client.util.IOUtils</code>.
 */
public class IOUtilsTest {

    @Test
    public void testCopy() throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream("This is a test".getBytes("UTF-8"));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        IOUtils.getInstance().copy(inputStream, outputStream);
        assertThat(outputStream.toString("UTF-8"), is(equalTo("This is a test")));
    }

    @Test
    public void testCloseQuietlyNullInputStream() {
        // Nothing should happen...
        IOUtils.getInstance().closeQuietly(null);
    }

    @Test
    public void testCloseQuietlyInputStream() throws IOException {
        final InputStream mockInputStream = mock(InputStream.class);
        IOUtils.getInstance().closeQuietly(mockInputStream);
        verify(mockInputStream).close();
    }

    @Test
    public void testCloseQuietlyInputStreamExceptionOnClose() throws IOException {
        final InputStream mockInputStream = mock(InputStream.class);
        doThrow(IOException.class).when(mockInputStream).close();
        IOUtils.getInstance().closeQuietly(mockInputStream);
        verify(mockInputStream).close();
    }

}
