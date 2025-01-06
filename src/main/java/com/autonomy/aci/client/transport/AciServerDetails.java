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

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Holds connection details about an ACI Server. Other properties, like timeouts etc, should be available on the
 * implementation of the {@code AciHttpClient} interface.
 */
public class AciServerDetails implements Serializable {

    private static final long serialVersionUID = -7383607154729383419L;

    /**
     * Holds the different types of transport protocol that can be used communicate with ACI servers.
     */
    public enum TransportProtocol {
        HTTP, HTTPS
    }

    /**
     * Holds value of property protocol. Defaults to <code>TransportProtocol.HTTP</code>
     */
    private TransportProtocol protocol = TransportProtocol.HTTP;

    /**
     * Holds value of property host.
     */
    private String host;

    /**
     * Holds value of property port.
     */
    private int port;

    /**
     * Holds value of property charsetName. Defaults to <code>UTF-8</code>.
     */
    private String charsetName = "UTF-8";

    /**
     * If this is non-null, then it will be used to encrypt communication with the ACI Server.
     * <p>
     * Note that streamed parameters (see {@link com.autonomy.aci.client.transport.ActionParameter#requiresPostRequest})
     * cannot be encrypted.  Using ACI encryption for security (rather than for OEM licensing purposes) is deprecated in
     * favour of TLS, but if you must use it, do not use streamed parameters.
     */
    private EncryptionCodec encryptionCodec;

    /**
     * Default constructor
     */
    public AciServerDetails() {
        super();
    }

    /**
     * Creates connection details for an ACI server, with the specified <code>host</code> and <code>port</code> details and
     * with the default <code>protocol</code> and <code>charsetName</code> values.
     * @param host The host of the ACI server
     * @param port The port of the ACI server
     */
    public AciServerDetails(final String host, final int port) {
        Validate.notNull(host, "host must not be null, it must be set to a value");
        Validate.isTrue((port >= 0) && (port <= 65536), "port is out of range, it should be between 0 and 65536.");

        this.host = host;
        this.port = port;
    }

    /**
     * Creates connection details for an ACI server, with the specified <code>protocol</code>, <code>host</code> and <code>port
     * </code> details and with the default <code>charsetName</code> value.
     * @param protocol The protocol to use when communicating with the ACI server
     * @param host     The host of the ACI server
     * @param port     The port of the ACI server
     */
    public AciServerDetails(final TransportProtocol protocol, final String host, final int port) {
        Validate.notNull(protocol, "protocol must not be null, it must be set to a value");
        Validate.notNull(host, "host must not be null, it must be set to a value");
        Validate.isTrue((port >= 0) && (port <= 65536), "port is out of range, it should be between 0 and 65536.");

        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    /**
     * Copy constructor.
     * @param that The <code>AciServerDetails</code> to copy details from.
     */
    public AciServerDetails(final AciServerDetails that) {
        this.protocol = that.protocol;
        this.host = that.host;
        this.port = that.port;
        this.charsetName = that.charsetName;
        this.encryptionCodec = that.encryptionCodec;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(final Object obj) {
        boolean returnValue = false;

        if (this == obj) {
            returnValue = true;
        } else if (obj instanceof AciServerDetails) {
            final AciServerDetails that = (AciServerDetails) obj;

            returnValue = new EqualsBuilder()
                    .append(this.protocol, that.protocol)
                    .append(this.host, that.host)
                    .append(this.port, that.port)
                    .append(this.charsetName, that.charsetName)
                    .append(this.encryptionCodec, that.encryptionCodec)
                    .isEquals();
        }

        return returnValue;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 11)
                .append(protocol)
                .append(host)
                .append(port)
                .append(charsetName)
                .append(encryptionCodec)
                .toHashCode();
    }

    /**
     * Getter for property protocol.
     * @return Value of property protocol
     */
    public TransportProtocol getProtocol() {
        return protocol;
    }

    /**
     * Setter for property protocol.
     * @param protocol New value of property protocol
     * @throws java.lang.IllegalArgumentException if <code>protocol</code> is null
     */
    public void setProtocol(final TransportProtocol protocol) {
        Validate.notNull(protocol, "protocol must not be null, it must be set to a value");
        this.protocol = protocol;
    }

    /**
     * Getter for property host.
     * @return Value of property host
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Setter for property host.
     * @param host New value of property host
     */
    public void setHost(final String host) {
        Validate.notNull(host, "host must not be null, it must be set to a value");
        this.host = host;
    }

    /**
     * Getter for property port.
     * @return Value of property port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Setter for property port. The port number must be in the range <code>0 &lt;= port &lt;= 65536</code> or an
     * <code>IllegalArgumentException</code> will be thrown.
     * @param port New value of property port
     * @throws IllegalArgumentException if the <code>port</code> is outside the range <code>0 &lt;= port &lt;= 65536</code>
     */
    public void setPort(final int port) {
        Validate.isTrue((port >= 0) && (port <= 65536), "port is out of range, it should be between 0 and 65536.");
        this.port = port;
    }

    /**
     * Getter for property charsetName.
     * @return Value of property charsetName
     */
    public String getCharsetName() {
        return this.charsetName;
    }

    /**
     * Setter for property charsetName.
     * @param charsetName The name of the requested charset; may be either a canonical name or an alias
     * @throws IllegalArgumentException                     If <code>charsetName</code> is null
     * @throws java.nio.charset.IllegalCharsetNameException If the given charset name is illegal
     * @throws java.nio.charset.UnsupportedCharsetException If no support for the named charset is available in this
     *                                                      instance of the Java virtual machine
     */
    public void setCharsetName(final String charsetName) {
        if (Charset.isSupported(charsetName)) {
            this.charsetName = charsetName;
        } else {
            throw new UnsupportedCharsetException("No support for, " + charsetName + ", is available in this instance of the JVM");
        }
    }

    public EncryptionCodec getEncryptionCodec() {
        return encryptionCodec;
    }

    public void setEncryptionCodec(final EncryptionCodec encryptionCodec) {
        this.encryptionCodec = encryptionCodec;
    }

}
