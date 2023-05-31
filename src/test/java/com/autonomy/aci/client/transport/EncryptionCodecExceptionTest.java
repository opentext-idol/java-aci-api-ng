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

package com.autonomy.aci.client.transport;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * JUnit tests for the <tt>com.autonomy.aci.client.transport.EncryptionCodecException</tt> class...
 */
public class EncryptionCodecExceptionTest {

    @Test
    public void testDefaultConstructor() {
        // Create an empty one...
        final EncryptionCodecException exception = new EncryptionCodecException();

        // Check it's properties...
        assertThat("Exception message isn't null.", exception.getMessage(), is(nullValue()));
        assertThat("Exception cause isn't null.", exception.getCause(), is(nullValue()));
    }

    @Test
    public void testMessageConstructor() {
        // This is the message to use...
        final String message = "This is a message";

        // Create an exception with the message...
        final EncryptionCodecException exception = new EncryptionCodecException(message);

        // Check it's properties...
        assertThat("Exception message doesn't match.", exception.getMessage(), is(equalTo(message)));
        assertThat("Exception cause isn't null.", exception.getCause(), is(nullValue()));
    }

    @Test
    public void testCauseConstructor() {
        // This is the cause to use...
        final String message = "This is the cause...";
        final IOException cause = new IOException(message);

        // Create an exception with the message...
        final EncryptionCodecException exception = new EncryptionCodecException(cause);

        // Check it's properties...
        assertThat("Exception message doesn't match.", cause.getClass().getName() + ": " + message, is(equalTo(exception.getMessage())));
        assertThat("Exception cause doesn't match.", cause, is(sameInstance(exception.getCause())));
    }

    @Test
    public void testMessageAndCauseConstructor() {
        // This is the cause to use...
        final String message = "This is the message...";
        final IOException cause = new IOException("This is the cause...");

        // Create an exception with the message...
        final EncryptionCodecException exception = new EncryptionCodecException(message, cause);

        // Check it's properties...
        assertThat("Exception message doesn't match.", exception.getMessage(), is(equalTo(message)));
        assertThat("Exception cause doesn't match.", cause, is(sameInstance(exception.getCause())));
    }

}
