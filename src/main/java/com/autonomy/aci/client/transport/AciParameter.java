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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.ContentType;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Holds the name and value of an ACI parameter to be sent as part of an ACI request. The value of the parameter should
 * automatically be encoded by the implementation of {@code AciHttpClient}, so you only need to encode values where
 * double encoding is required, for example {@code MatchReference} parameters etc.
 */
public class AciParameter implements Serializable, ActionParameter<String> {

    private static final long serialVersionUID = -5984519978673149176L;

    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Holds value of property value.
     */
    private String value;

    /**
     * Creates a new instance of AciParameter.
     */
    public AciParameter() {
        super();
    }

    /**
     * Creates a new instance of AciParameter
     * @param name  The name of the ACI parameter
     * @param value The value of the ACI parameter. If the <code>value</code> is <code>null</code>, then the value is left
     *              blank, i.e. <code>&amp;parameter=</code>; otherwise, the value of <code>value.toString()</code> is stored
     * @throws IllegalArgumentException if <code>name</code> is <code>null</code>
     */
    public AciParameter(final String name, final Object value) {
        super();

        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Parameter names cannot be null.");
        }

        // Set the name...
        this.name = name;

        // Yes String.valueOf(null) has a null check that returns "null", but that's not what we want, we want the 
        // eventual parameter to be 'name='...
        if (value != null) {
            this.value = String.valueOf(value);
        }
    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     * An {@code AciParameter} is deemed to be equal to another if the name of the parameter is the same. This means
     * that you will be unable to add another parameter with the same name to the {@code Set} to be sent when an action
     * is executed, if one of the same name already exists in the {@code Set}.
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        boolean returnValue = false;

        if (this == obj) {
            returnValue = true;
        } else if (obj instanceof AciParameter) {
            final AciParameter that = (AciParameter) obj;

            // Check the name of this parameter with the passed in parameter...
            returnValue = this.name.toLowerCase(Locale.ENGLISH).equals(that.name.toLowerCase(Locale.ENGLISH));
        }

        return returnValue;
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ENGLISH).hashCode();
    }

    /**
     * Getter for property name.
     * @return Value of property name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for property name.
     * @param name New value of property name
     * @throws java.lang.IllegalArgumentException if <code>name</code> is <code>null</code>
     */
    public void setName(final String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Parameter names cannot be null.");
        }
        this.name = name;
    }

    /**
     * Getter for property value.
     * @return Value of property value
     */
    public String getValue() {
        return this.value;
    }

    @Override
    public void addToEntity(final MultipartEntityBuilder builder, final Charset charset) {
        builder.addPart(name, new StringBody(value, ContentType.create("text/plain", charset)));
    }

    @Override
    @Deprecated
    public void addToEntity(final org.apache.http.entity.mime.MultipartEntityBuilder builder, final Charset charset) {
        builder.addPart(name, new org.apache.http.entity.mime.content.StringBody(value,
                org.apache.http.entity.ContentType.create("text/plain", charset)));
    }

    @Override
    public boolean requiresPostRequest() {
        return false;
    }

    /**
     * Setter for property value.
     * @param value New value of property value
     */
    public void setValue(final String value) {
        this.value = value;
    }

}
