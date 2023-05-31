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

package com.autonomy.aci.client.services;

import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * JUnit test class for <tt>com.autonomy.aci.client.services.AciErrorException</tt> class.
 */
public class AciErrorExceptionTest {

    /**
     * Common assert message...
     */
    private static final String PROPERTY_NOT_AS_EXPECTED = "The property wasn't as expected";

    @Test
    public void testDefaultConstructor() {
        // Create an empty one...
        final AciErrorException exception = new AciErrorException();

        // Check it's properties...
        assertNull("Exception message isn't null.", exception.getMessage());
        assertNull("Exception cause isn't null.", exception.getCause());
    }

    @Test
    public void testMessageConstructor() {
        // This is the message to use...
        final String message = "This is a message";

        // Create an exception with the message...
        final AciErrorException exception = new AciErrorException(message);

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
        final AciErrorException exception = new AciErrorException(cause);

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
        final AciErrorException exception = new AciErrorException(message, cause);

        // Check it's properties...
        assertEquals("Exception message doesn't match.", message, exception.getMessage());
        assertEquals("Exception cause doesn't match.", cause, exception.getCause());
    }

    @Test
    public void testErrorIdProperty() {
        // Create a blank exception...
        final AciErrorException exception = new AciErrorException();
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getErrorId());

        // Set it and check...
        final String errorId = "AutonomyIDOLServerWOBBLE1";
        exception.setErrorId(errorId);
        assertEquals(PROPERTY_NOT_AS_EXPECTED, errorId, exception.getErrorId());

        // Set it to null...
        exception.setErrorId(null);
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getErrorId());
    }

    @Test
    public void testRawErrorIdProperty() {
        // Create a blank exception...
        final AciErrorException exception = new AciErrorException();
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getRawErrorId());

        // Set it and check...
        final String rawErrorId = "0x20D";
        exception.setRawErrorId(rawErrorId);
        assertEquals(PROPERTY_NOT_AS_EXPECTED, rawErrorId, exception.getRawErrorId());

        // Set it to null...
        exception.setRawErrorId(null);
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getRawErrorId());
    }

    @Test
    public void testErrorStringProperty() {
        // Create a blank exception...
        final AciErrorException exception = new AciErrorException();
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getErrorString());

        // Set it and check...
        final String errorString = "ERROR";
        exception.setErrorString(errorString);
        assertEquals(PROPERTY_NOT_AS_EXPECTED, errorString, exception.getErrorString());

        // Set it to null...
        exception.setErrorString(null);
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getErrorString());
    }

    @Test
    public void testErrorDescriptionProperty() {
        // Create a blank exception...
        final AciErrorException exception = new AciErrorException();
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getErrorDescription());

        // Set it and check...
        final String errorDescription = "The requested action was not recognised";
        exception.setErrorDescription(errorDescription);
        assertEquals(PROPERTY_NOT_AS_EXPECTED, errorDescription, exception.getErrorDescription());

        // Set it to null...
        exception.setErrorDescription(null);
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getErrorDescription());
    }

    @Test
    public void testErrorCodeProperty() {
        // Create a blank exception...
        final AciErrorException exception = new AciErrorException();
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getErrorCode());

        // Set it and check....
        final String errorCode = "ERRORNOTIMPLEMENTED";
        exception.setErrorCode(errorCode);
        assertEquals(PROPERTY_NOT_AS_EXPECTED, errorCode, exception.getErrorCode());

        // Set it to null...
        exception.setErrorCode(null);
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getErrorCode());
    }

    @Test
    public void testErrorTimeProperty() {
        // Create a blank exception...
        final AciErrorException exception = new AciErrorException();
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getErrorTime());

        // Set it an check...
        final Date errorTime = new Date(System.currentTimeMillis());
        exception.setErrorTime(errorTime);
        assertEquals(PROPERTY_NOT_AS_EXPECTED, errorTime, exception.getErrorTime());

        // Set it to null...
        exception.setErrorTime(null);
        assertNull(PROPERTY_NOT_AS_EXPECTED, exception.getErrorTime());
    }

    @Test
    public void testGetMessageMethod() {
        // These are the properties we'll use...
        final String message = "This is a message...";
        final String errorString = "ERROR";
        final String errorDescription = "The requested action was not recognised";

        // Create an exception with the message and add both properties...
        AciErrorException exception = new AciErrorException(message);
        exception.setErrorString(errorString);
        exception.setErrorDescription(errorDescription);

        // Check it's getMessage response...
        assertEquals("getMessage() response not expected", message, exception.getMessage());

        // Create an exception with no message and add both properties...
        exception = new AciErrorException();
        exception.setErrorString(errorString);
        exception.setErrorDescription(errorDescription);

        // Check it's getMessage response...
        assertEquals("getMessage() response not expected", errorDescription, exception.getMessage());

        // Create an exception with no message and no errorDescription property...
        exception = new AciErrorException();
        exception.setErrorString(errorString);

        // Check it's getMessage response...
        assertEquals("getMessage() response not expected", errorString, exception.getMessage());
    }

}
