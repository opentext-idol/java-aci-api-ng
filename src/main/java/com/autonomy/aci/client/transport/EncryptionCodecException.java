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

/**
 * Signals that an exception has occurred while trying to encrypt or decrypt the ACI request or response.
 */
public class EncryptionCodecException extends Exception {

    private static final long serialVersionUID = -7359860563640646911L;

    /**
     * Constructs a new {@code EncryptionCodecException} without a specified detail message.
     */
    public EncryptionCodecException() {
        super();
    }

    /**
     * Constructs a new {@code EncryptionCodecException} with the specified detail message.
     * @param msg The error message.
     */
    public EncryptionCodecException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a new {@code EncryptionCodecException} with the specified nested {@code Throwable}.
     * @param cause the exception or error that caused this exception to be thrown
     */
    public EncryptionCodecException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code EncryptionCodecException} with the specified detail message and nested {@code Throwable}.
     * @param msg   the error message
     * @param cause the exception or error that caused this exception to be thrown
     */
    public EncryptionCodecException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
