/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport;

import com.autonomy.aci.client.TestEncryptionCodec;
import org.junit.Test;

import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * JUnit test class for <tt>com.autonomy.aci.client.util.transport.AciServerDetails</tt> class.
 */
public class AciServerDetailsTest {

    @Test
    public void testDefaultConstructor() {
        // Create an instance...
        final AciServerDetails details = new AciServerDetails();

        // Check everything is null...
        assertThat("protocol property is not HTTP", details.getProtocol(), is(equalTo(AciServerDetails.TransportProtocol.HTTP)));
        assertThat("host property is not null", details.getHost(), is(nullValue()));
        assertThat("port property is not 0", details.getPort(), is(0));
        assertThat("charsetName property is incorrect", details.getCharsetName(), is("UTF-8"));
        assertThat("encryptionCodec property is not null", details.getEncryptionCodec(), is(nullValue()));
    }

    @Test
    public void testTwoParamConstructor() {
        try {
            new AciServerDetails(null, 10);
            fail("Should have thrown an IllegalArgumentException as host is null.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        try {
            new AciServerDetails("localhost", -10);
            fail("Should have thrown an IllegalArgumentException as the port is out of range.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        try {
            new AciServerDetails("localhost", 123456789);
            fail("Should have thrown an IllegalArgumentException as the port is out of range.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        // Create one for real...
        final AciServerDetails details = new AciServerDetails("localhost", 10);

        // Check everything is null...
        assertThat(details.getProtocol(), is(equalTo(AciServerDetails.TransportProtocol.HTTP)));
        assertThat(details.getHost(), is(equalTo("localhost")));
        assertThat(details.getPort(), is(10));
        assertThat(details.getCharsetName(), is("UTF-8"));
        assertThat(details.getEncryptionCodec(), is(nullValue()));
    }

    @Test
    public void testThreeParamConstructor() {
        try {
            new AciServerDetails(null, null, 10);
            fail("Should have thrown an IllegalArgumentException as the protocol was set to null.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        try {
            new AciServerDetails(AciServerDetails.TransportProtocol.HTTPS, null, 10);
            fail("Should have thrown an IllegalArgumentException as host is null.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        try {
            new AciServerDetails(AciServerDetails.TransportProtocol.HTTPS, "localhost", -10);
            fail("Should have thrown an IllegalArgumentException as the port is out of range.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        try {
            new AciServerDetails(AciServerDetails.TransportProtocol.HTTPS, "localhost", 123456789);
            fail("Should have thrown an IllegalArgumentException as the port is out of range.");
        } catch (final IllegalArgumentException iae) { /* ignore */ }

        // Create one for real...
        final AciServerDetails details = new AciServerDetails(AciServerDetails.TransportProtocol.HTTPS, "localhost", 10);

        // Check everything is null...
        assertThat(details.getProtocol(), is(equalTo(AciServerDetails.TransportProtocol.HTTPS)));
        assertThat(details.getHost(), is(equalTo("localhost")));
        assertThat(details.getPort(), is(10));
        assertThat(details.getCharsetName(), is("UTF-8"));
        assertThat(details.getEncryptionCodec(), is(nullValue()));
    }

    @Test
    public void testCopyConstructor() {
        // The codec to use...
        final TestEncryptionCodec encryptionCodec = new TestEncryptionCodec();

        // Create an instance...
        final AciServerDetails details = new AciServerDetails();
        details.setHost("localhost");
        details.setPort(12345);
        details.setEncryptionCodec(encryptionCodec);

        // Copy...
        final AciServerDetails newDetails = new AciServerDetails(details);

        // Check everything was coppied across...
        assertThat("protocol", newDetails.getProtocol(), is(equalTo(details.getProtocol())));
        assertThat("host", newDetails.getHost(), is(equalTo(details.getHost())));
        assertThat("port", newDetails.getPort(), is(equalTo(details.getPort())));
        assertThat("charsetName", newDetails.getCharsetName(), is(equalTo(details.getCharsetName())));
        assertThat("encryptionCodec", newDetails.getEncryptionCodec(), is(sameInstance(details.getEncryptionCodec())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProtocolProperty() {
        // Create an instance...
        final AciServerDetails details = new AciServerDetails();
        assertThat("protocol property is not HTTP", details.getProtocol(), is(AciServerDetails.TransportProtocol.HTTP));

        // Set the protocol to something else...
        details.setProtocol(AciServerDetails.TransportProtocol.HTTPS);
        assertThat("protocol property is not HTTPS", details.getProtocol(), is(AciServerDetails.TransportProtocol.HTTPS));

        // Set the protocol to null and check....
        details.setProtocol(null);
        fail("Should have thrown an IllegalArgumentException as the protocol has been set to null.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHostProperty() {
        // Create an instance...
        final AciServerDetails details = new AciServerDetails();
        assertThat("host property is not null", details.getHost(), is(nullValue()));

        // Set the host to something...
        final String host = "host.example.com";
        details.setHost(host);
        assertThat("host property is not as expected", details.getHost(), is(equalTo(host)));

        // Set the host to null and check....
        details.setHost(null);
        fail("Should have thrown an IllegalArgumentException as the host has been set to null.");
    }

    @Test
    public void testPortProperty() {
        // Create an instance...
        final AciServerDetails details = new AciServerDetails();
        assertThat("port property is not 0", details.getPort(), is(0));

        // Set the port to minus out of range...
        try {
            details.setPort(-10);
            fail("Should have thrown an IllegalArgumentException as port is out of range.");
        } catch (final IllegalArgumentException iae) {
            // Expected due to out of range port number...
        }

        // Set port to positive out of range...
        try {
            details.setPort(90000);
            fail("Should have thrown an IllegalArgumentException as port is out of range.");
        } catch (final IllegalArgumentException iae) {
            // Expected due to out of range port number...
        }

        // Set the port to a number in range...
        final int port = 12000;
        details.setPort(port);
        assertThat("port property is not as expected", details.getPort(), is(equalTo(port)));
    }

    @Test
    public void testCharsetNameProperty() {
        // Create an instance...
        final AciServerDetails details = new AciServerDetails();
        assertThat("charsetName property is not null", details.getCharsetName(), is("UTF-8"));

        // Set the charsetName to something...
        final String charsetName = "ISO-8859-1";
        details.setCharsetName(charsetName);
        assertThat("charsetName property is not as expected", details.getCharsetName(), is(equalTo(charsetName)));

        // Set the charsetName to null and check....
        try {
            details.setCharsetName(null);
            fail("Should have thrown an IllegalArgumentException as charsetName is null.");
        } catch (final IllegalArgumentException iae) {
            // Expected...
        }

        // Set the charsetName to an illegal charset name and check....
        try {
            details.setCharsetName("_~@£$");
            fail("Should have thrown an IllegalCharsetNameException as charsetName is illegal.");
        } catch (final IllegalCharsetNameException icne) {
            // Expected...
        }

        // Set the charsetName to an unsupported charset and check....
        try {
            details.setCharsetName("wibble");
            fail("Should have thrown an UnsupportedCharsetException as charsetName is unsupported.");
        } catch (final UnsupportedCharsetException uce) {
            // Expected...
        }
    }

    @Test
    public void testEncryptionCodecProperty() {
        // Create an instance...
        final AciServerDetails details = new AciServerDetails();
        assertThat("encryptionCodec property is not null", details.getEncryptionCodec(), is(nullValue()));

        // Set the encryptionCodec to something...
        final EncryptionCodec encryptionCodec = new TestEncryptionCodec();
        details.setEncryptionCodec(encryptionCodec);
        assertThat("encryptionCodec property is not as expected", details.getEncryptionCodec(), is(sameInstance(encryptionCodec)));

        // Set the encryptionCodec to null and check....
        details.setEncryptionCodec(null);
        assertThat("encryptionCodec property is not null", details.getEncryptionCodec(), is(nullValue()));
    }

    @Test
    public void testEqualsMethod() {
        // Create an instance...
        final AciServerDetails details1 = new AciServerDetails();
        details1.setHost("host1.example.com");
        details1.setPort(5790);
        details1.setCharsetName("UTF-8");

        // Create another instance...
        final AciServerDetails details2 = new AciServerDetails();
        details2.setHost("host2.example.com");
        details2.setPort(5790);
        details2.setCharsetName("UTF-8");

        // Compare to itself and the other details...
        assertThat("Details should be equal to itself", details1.equals(details1), is(true));
        assertThat("Details should not be equal", details1.equals(details2), is(false));

        // Change details2 to have the same name so tecxhnically they'return the same...
        details2.setHost("host1.example.com");
        assertThat("Details should be equal", details1.equals(details2), is(true));

        // Compare it to some random object, it should be false...
        assertThat("Parameters should be equal", details1.equals(new Object()), is(false));

        // Create yet another instance...
        final AciServerDetails details3 = new AciServerDetails();
        details3.setHost("host1.example.com");
        details3.setPort(5790);
        details3.setCharsetName("UTF-8");
        details3.setEncryptionCodec(new TestEncryptionCodec());

        // Check against details1 which is the same apart from the codec...
        assertThat("Details should not be equal", details3.equals(details1), is(false));

        // Set the same codec on details1, it should now be the same as details3...
        details1.setEncryptionCodec(new TestEncryptionCodec());
        assertThat(details3.equals(details1), is(true));
    }

    @Test
    public void testHashCodeMethod() {
        // Create an instance...
        final AciServerDetails details1 = new AciServerDetails();
        details1.setHost("host1.example.com");
        details1.setPort(5790);
        details1.setCharsetName("UTF-8");

        // Create an instance...
        final AciServerDetails details2 = new AciServerDetails();
        details2.setHost("host1.example.com");
        details2.setPort(5790);
        details2.setCharsetName("UTF-8");

        // Create yet another instance...
        final AciServerDetails details3 = new AciServerDetails();
        details3.setHost("host2.example.com");
        details3.setPort(5790);
        details3.setCharsetName("UTF-8");

        // Assert that 1 & 2 are the same and 1 & 3 and 2 & 3 are different...
        assertThat("Hash codes should be equal", details1.hashCode() == details2.hashCode(), is(true));
        assertThat("Hash codes should not be equal", details1.hashCode() == details3.hashCode(), is(false));
        assertThat("Hash codes should not be equal", details2.hashCode() == details3.hashCode(), is(false));

        details1.setEncryptionCodec(new TestEncryptionCodec());
        assertThat(details1.hashCode() == details2.hashCode(), is(false));

        details2.setEncryptionCodec(new TestEncryptionCodec());
        assertThat(details1.hashCode() == details2.hashCode(), is(true));
    }

}
