/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.services;

/**
 * This exception is thrown if an error occurred during processing by an ACI response <tt>Processor</tt>.
 */
public class ProcessorException extends RuntimeException {

    private static final long serialVersionUID = -1460955256529068945L;

    /**
     * Constructs a new {@code ProcessorException} without specified detail message.
     */
    public ProcessorException() {
        super();
    }

    /**
     * Constructs a new {@code ProcessorException} with specified detail message.
     * @param msg The error message.
     */
    public ProcessorException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a new {@code ProcessorException} with specified nested {@code Throwable}.
     * @param cause The exception or error that caused this exception to be thrown
     */
    public ProcessorException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code ProcessorException} with specified detail message and nested {@code Throwable}.
     * @param msg   The error message
     * @param cause The exception or error that caused this exception to be thrown
     */
    public ProcessorException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
