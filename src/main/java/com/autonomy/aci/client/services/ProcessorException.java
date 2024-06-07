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

/**
 * This exception is thrown if an error occurred during processing by an ACI response <code>Processor</code>.
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
