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

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Exception that holds information about why an ACI command failed.
 */
public class AciErrorException extends AciServiceException {

    private static final long serialVersionUID = -5947776449281420291L;

    /**
     * Holds value of property errorId.
     */
    private String errorId;

    /**
     * Holds value of property rawErrorId.
     */
    private String rawErrorId;

    /**
     * Holds value of property errorString.
     */
    private String errorString;

    /**
     * Holds value of property errorDescription.
     */
    private String errorDescription;

    /**
     * Holds value of property errorCode.
     */
    private String errorCode;

    /**
     * Holds value of property errorTime.
     */
    private Date errorTime;

    /**
     * Constructs a new {@code AciErrorException} without specified detail
     * message.
     */
    public AciErrorException() {
        super();
    }

    /**
     * Constructs a new {@code AciErrorException} with specified detail message.
     * @param msg The error message.
     */
    public AciErrorException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a new {@code AciErrorException} with specified nested {@code Throwable}.
     * @param cause The exception or error that caused this exception to be thrown
     */
    public AciErrorException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code AciErrorException} with specified detail message and nested {@code Throwable}.
     * @param msg   The error message
     * @param cause The exception or error that caused this exception to be thrown
     */
    public AciErrorException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    @Override
    public String getMessage() {
        return StringUtils.isBlank(super.getMessage()) ? (StringUtils.isBlank(errorDescription) ? errorString : errorDescription) : super.getMessage();
    }

    public String getErrorId() {
        return this.errorId;
    }

    public void setErrorId(final String errorId) {
        this.errorId = errorId;
    }

    public String getErrorString() {
        return this.errorString;
    }

    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

    public void setErrorDescription(final String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }

    public Date getErrorTime() {
        return (this.errorTime == null) ? null : (Date) this.errorTime.clone();
    }

    public void setErrorTime(final Date errorTime) {
        this.errorTime = (errorTime == null) ? null : (Date) errorTime.clone();
    }

    public String getRawErrorId() {
        return rawErrorId;
    }

    public void setRawErrorId(final String rawErrorId) {
        this.rawErrorId = rawErrorId;
    }

}
