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

package com.autonomy.aci.client.transport.gss;

import com.autonomy.aci.client.transport.AciServerDetails;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Holds connection details about an ACI Server that is secured using GSS-API. It is a subclass of
 * <tt>AciServerDetails</tt> that has the addition of the service name of the ACI server to be contacted.
 */
public class GssAciServerDetails extends AciServerDetails {

    private static final long serialVersionUID = -5740681849846014916L;

    private String serviceName;

    /**
     * Default constructor
     */
    public GssAciServerDetails() {
        super();
    }

    /**
     * Creates connection details for an ACI server, with the specified GSS-API {@code serviceName}, {@code host} and
     * {@code port} details and with the default {@code protocol} and {@code charsetName} values.
     * @param serviceName The service name of the GSS-API secured ACI Server
     * @param host        The host of the ACI server
     * @param port        The port of the ACI server
     * @since 4.1.3
     */
    public GssAciServerDetails(final String serviceName, final String host, final int port) {
        super(host, port);
        this.serviceName = serviceName;
    }

    /**
     * Creates connection details for an ACI server, with the specified GSS-API {@code serviceName}, {@code protocol},
     * {@code host} and {@code port} details and with the default {@code protocol} and {@code charsetName} values.
     * @param serviceName The service name of the GSS-API secured ACI Server
     * @param protocol    The protocol to use when communicating with the ACI server
     * @param host        The host of the ACI server
     * @param port        The port of the ACI server
     * @since 4.1.3
     */
    public GssAciServerDetails(final String serviceName, final TransportProtocol protocol, final String host, final int port) {
        super(protocol, host, port);
        this.serviceName = serviceName;
    }

    /**
     * Copy constructor.
     * @param that The <tt>GssAciServerDetails</tt> to copy details from.
     */
    public GssAciServerDetails(final GssAciServerDetails that) {
        super(that);
        this.serviceName = that.serviceName;
    }

    /**
     * Indicates whether some other object is <em>equal to</em> this one.
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        boolean returnValue = false;

        if (this == obj) {
            returnValue = true;
        } else if (obj instanceof GssAciServerDetails) {
            final GssAciServerDetails that = (GssAciServerDetails) obj;

            returnValue = new EqualsBuilder()
                    .appendSuper(super.equals(that))
                    .append(this.serviceName, that.serviceName)
                    .isEquals();
        }

        return returnValue;
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(9, 17)
                .appendSuper(super.hashCode())
                .append(serviceName)
                .toHashCode();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

}
