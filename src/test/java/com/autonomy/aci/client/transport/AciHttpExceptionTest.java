/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * JUnit test class for <tt>com.autonomy.aci.client.transport.AciHttpException</tt> class.
 */
public class AciHttpExceptionTest {

    @Test
    public void testDefaultConstructor() {
        // Create an empty one...
        final AciHttpException exception = new AciHttpException();

        // Check it's properties...
        assertNull("Exception message isn't null.", exception.getMessage());
        assertNull("Exception cause isn't null.", exception.getCause());
    }

    @Test
    public void testMessageConstructor() {
        // This is the message to use...
        final String message = "This is a message";

        // Create an exception with the message...
        final AciHttpException exception = new AciHttpException(message);

        // Check it's properties...
        assertEquals("Exception message doesn't match.", message, exception.getMessage());
        assertNull("Exception cause isn't null.", exception.getCause());
    }

    @Test
    public void testCauseConstructor() {
        // This is the cause to use...
        final String message = "This is the cause...";
        final IOException cause = new IOException(message);

        // Create an exception with the message...
        final AciHttpException exception = new AciHttpException(cause);

        // Check it's properties...
        assertEquals("Exception message doesn't match.", cause.getClass().getName() + ": " + message, exception.getMessage());
        assertEquals("Exception cause doesn't match.", cause, exception.getCause());
    }

    @Test
    public void testMessageAndCauseConstructor() {
        // This is the cause to use...
        final String message = "This is the message...";
        final IOException cause = new IOException("This is the cause...");

        // Create an exception with the message...
        final AciHttpException exception = new AciHttpException(message, cause);

        // Check it's properties...
        assertEquals("Exception message doesn't match.", message, exception.getMessage());
        assertEquals("Exception cause doesn't match.", cause, exception.getCause());
    }

}
