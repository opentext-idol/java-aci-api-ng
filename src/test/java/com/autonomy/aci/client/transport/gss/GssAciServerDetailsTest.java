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

import com.autonomy.aci.client.transport.AciServerDetails.TransportProtocol;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * JUnit test class for <tt>com.autonomy.aci.client.util.transport.gss.GssAciServerDetails</tt> class.
 */
public class GssAciServerDetailsTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultConstructor() {
        // Create an instance...
        final GssAciServerDetails details = new GssAciServerDetails();

        // Check everything is null...
        assertThat(details.getServiceName(), is(nullValue()));
        assertThat(details.getProtocol(), is(equalTo(TransportProtocol.HTTP)));
        assertThat(details.getHost(), is(nullValue()));
        assertThat(details.getPort(), is(0));
        assertThat(details.getCharsetName(), is("UTF-8"));
        assertThat(details.getEncryptionCodec(), is(nullValue()));
    }

    @Test
    public void testThreeParamConstructor() {
        try {
            new GssAciServerDetails("SERVICE/host.example.com@EXAMPLE.COM", null, 10);
            fail("Should have thrown an NullPointerException as host is null.");
        } catch (final NullPointerException npe) { /* ignore */ }

        try {
            new GssAciServerDetails("SERVICE/host.example.com@EXAMPLE.COM", "localhost", -10);
            fail("Should have thrown an IllegalArgumentException as the port is out of range.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        try {
            new GssAciServerDetails("SERVICE/host.example.com@EXAMPLE.COM", "localhost", 123456789);
            fail("Should have thrown an IllegalArgumentException as the port is out of range.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        // Create one for real...
        final GssAciServerDetails details = new GssAciServerDetails("SERVICE/host.example.com@EXAMPLE.COM", "localhost", 10);

        assertThat(details.getServiceName(), is(equalTo("SERVICE/host.example.com@EXAMPLE.COM")));
        assertThat(details.getProtocol(), is(equalTo(TransportProtocol.HTTP)));
        assertThat(details.getHost(), is(equalTo("localhost")));
        assertThat(details.getPort(), is(10));
        assertThat(details.getCharsetName(), is("UTF-8"));
        assertThat(details.getEncryptionCodec(), is(nullValue()));
    }

    @Test
    public void testFourParamConstructor() {
        try {
            new GssAciServerDetails("SERVICE/host.example.com@EXAMPLE.COM", null, null, 10);
            fail("Should have thrown an NullPointerException as the protocol was set to null.");
        } catch (final NullPointerException npe) { /* ignore */ }

        try {
            new GssAciServerDetails("SERVICE/host.example.com@EXAMPLE.COM", TransportProtocol.HTTPS, null, 10);
            fail("Should have thrown an NullPointerException as host is null.");
        } catch (final NullPointerException npe) { /* ignore */ }

        try {
            new GssAciServerDetails("SERVICE/host.example.com@EXAMPLE.COM", TransportProtocol.HTTPS, "localhost", -10);
            fail("Should have thrown an IllegalArgumentException as the port is out of range.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        try {
            new GssAciServerDetails("SERVICE/host.example.com@EXAMPLE.COM", TransportProtocol.HTTPS, "localhost", 123456789);
            fail("Should have thrown an IllegalArgumentException as the port is out of range.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        // Create one for real...
        final GssAciServerDetails details = new GssAciServerDetails("SERVICE/host.example.com@EXAMPLE.COM", TransportProtocol.HTTPS, "localhost", 10);

        assertThat(details.getServiceName(), is(equalTo("SERVICE/host.example.com@EXAMPLE.COM")));
        assertThat(details.getProtocol(), is(equalTo(TransportProtocol.HTTPS)));
        assertThat(details.getHost(), is(equalTo("localhost")));
        assertThat(details.getPort(), is(10));
        assertThat(details.getCharsetName(), is("UTF-8"));
        assertThat(details.getEncryptionCodec(), is(nullValue()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCopyConstructor() {
        // Create an instance...
        final GssAciServerDetails details = new GssAciServerDetails();
        details.setServiceName("SERVICE/host.example.com@EXAMPLE.COM");
        details.setHost("localhost");
        details.setPort(12345);
        details.setEncryptionCodec(null);

        // Copy...
        final GssAciServerDetails newDetails = new GssAciServerDetails(details);

        // Check everything was copied across...
        assertThat("serviceName", newDetails.getServiceName(), is(equalTo(details.getServiceName())));
        assertThat("protocol", newDetails.getProtocol(), is(equalTo(details.getProtocol())));
        assertThat("host", newDetails.getHost(), is(equalTo(details.getHost())));
        assertThat("port", newDetails.getPort(), is(equalTo(details.getPort())));
        assertThat("charsetName", newDetails.getCharsetName(), is(equalTo(details.getCharsetName())));
        assertThat("encryptionCodec", newDetails.getEncryptionCodec(), is(nullValue()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testServiceNameProperty() {
        // Create an instance...
        final GssAciServerDetails details = new GssAciServerDetails();
        assertThat("serviceName property is not null", details.getServiceName(), is(nullValue()));

        // Set the host to something...
        final String serviceName = "SERVICE/host.example.com@EXAMPLE.COM";
        details.setServiceName(serviceName);
        assertThat("serviceName property is not as expected", details.getServiceName(), is(equalTo(serviceName)));

        // Set the host to null and check....
        details.setServiceName(null);
        assertThat("serviceName property is not null", details.getServiceName(), is(nullValue()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEqualsMethod() {
        // Create an instance...
        final GssAciServerDetails details1 = new GssAciServerDetails();
        details1.setServiceName("SERVICE1/host.example.com@EXAMPLE.COM");
        details1.setHost("host1.example.com");
        details1.setPort(5790);
        details1.setCharsetName("UTF-8");

        // Create another instance...
        final GssAciServerDetails details2 = new GssAciServerDetails();
        details2.setServiceName("SERVICE2/host.example.com@EXAMPLE.COM");
        details2.setHost("host1.example.com");
        details2.setPort(5790);
        details2.setCharsetName("UTF-8");

        // Compare to itself and the other details...
        assertThat("Details should be equal to itself", details1.equals(details1), is(true));
        assertThat("Details should not be equal", details1.equals(details2), is(false));

        // Change details2 to have the same name so technically they'll return the same...
        details2.setServiceName("SERVICE1/host.example.com@EXAMPLE.COM");
        assertThat("Details should be equal", details1.equals(details2), is(true));

        // Compare it to some random object, it should be false...
        assertThat("Shouldn't be equal", details1.equals(new Object()), is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHashCodeMethod() {
        // Create an instance...
        final GssAciServerDetails details1 = new GssAciServerDetails();
        details1.setServiceName("SERVICE1/host.example.com@EXAMPLE.COM");
        details1.setHost("host1.example.com");
        details1.setPort(5790);

        // Create an instance...
        final GssAciServerDetails details2 = new GssAciServerDetails();
        details2.setServiceName("SERVICE1/host.example.com@EXAMPLE.COM");
        details2.setHost("host1.example.com");
        details2.setPort(5790);

        // Create yet another instance...
        final GssAciServerDetails details3 = new GssAciServerDetails();
        details3.setServiceName("SERVICE2/host.example.com@EXAMPLE.COM");
        details3.setHost("host1.example.com");
        details3.setPort(5790);

        // Assert that 1 & 2 are the same and 1 & 3 and 2 & 3 are different...
        assertThat("Hash codes should be equal", details1.hashCode() == details2.hashCode(), is(true));
        assertThat("Hash codes should not be equal", details1.hashCode() == details3.hashCode(), is(false));
        assertThat("Hash codes should not be equal", details2.hashCode() == details3.hashCode(), is(false));
    }

}
