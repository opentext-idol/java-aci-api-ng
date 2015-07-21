/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.util;

/**
 * Thrown when the <tt>AciURLCodec</tt> has encountered a failure condition during an encode or decode.
 */
public class AciURLCodecException extends RuntimeException {

    private static final long serialVersionUID = 4326437867829713287L;

    /**
     * Constructs a new {@code AciURLCodecException} without a specified detail message.
     */
    public AciURLCodecException() {
        super();
    }

    /**
     * Constructs a new {@code AciURLCodecException} with the specified detail message.
     * @param msg The error message
     */
    public AciURLCodecException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a new {@code AciURLCodecException} with the specified nested {@code Throwable}.
     * @param cause The exception or error that caused this exception to be thrown
     */
    public AciURLCodecException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code AciURLCodecException} with the specified detail message and nested {@code Throwable}.
     * @param msg   The error message
     * @param cause The exception or error that caused this exception to be thrown
     */
    public AciURLCodecException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
