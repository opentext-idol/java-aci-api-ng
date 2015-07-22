/*
 * Copyright 2006-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.services;

/**
 * General exception that is thrown when something goes wrong during an {@code AciService} method call.
 */
public class AciServiceException extends RuntimeException {

    private static final long serialVersionUID = 3283015337774458145L;

    /**
     * Constructs a new {@code AciServiceException} without specified detail message.
     */
    public AciServiceException() {
        super();
    }

    /**
     * Constructs a new {@code AciServiceException} with specified detail message.
     * @param msg The error message.
     */
    public AciServiceException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a new {@code AciServiceException} with specified nested {@code Throwable}.
     * @param cause The exception or error that caused this exception to be thrown
     */
    public AciServiceException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code AciServiceException} with specified detail message and nested {@code Throwable}.
     * @param msg   The error message
     * @param cause The exception or error that caused this exception to be thrown
     */
    public AciServiceException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
