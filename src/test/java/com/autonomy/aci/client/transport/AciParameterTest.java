/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport;

import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * JUnit test class for <tt>com.autonomy.aci.client.transport.AciParameter</tt>.
 */
public class AciParameterTest {

    @Test
    public void testDefaultConstructor() {
        // Create an AciParameter object...
        final AciParameter parameter = new AciParameter();

        // Chech it...
        assertThat("name parameter should be null", parameter.getName(), is(nullValue()));
        assertThat("value parameter should be null", parameter.getValue(), is(nullValue()));
    }

    @Test
    public void testConstructorWithString() {
        // Values to use...
        final String name = "name";
        final String value = "value";

        // Create an AciParameter object...
        final AciParameter parameter = new AciParameter(name, value);

        // Check it...
        assertThat("name parameter not as expected", parameter.getName(), is(equalTo(name)));
        assertThat("value parameter not as expected", parameter.getValue(), is(equalTo(value)));
    }

    @Test
    public void testConstructorWithInt() {
        // Values to use...
        final String name = "name";

        // Create an AciParameter object...
        final AciParameter parameter = new AciParameter(name, 12);

        // Check it...
        assertThat("name parameter not as expected", parameter.getName(), is(equalTo(name)));
        assertThat("value parameter not as expected", parameter.getValue(), is(equalTo("12")));
    }

    @Test
    public void testConstructorWithLong() {
        // Values to use...
        final String name = "name";

        // Create an AciParameter object...
        final AciParameter parameter = new AciParameter(name, 100000000L);

        // Check it...
        assertThat("name parameter not as expected", parameter.getName(), is(equalTo(name)));
        assertThat("value parameter not as expected", parameter.getValue(), is(equalTo("100000000")));
    }

    @Test
    public void testConstructorWithFloat() {
        // Values to use...
        final String name = "name";

        // Create an AciParameter object...
        final AciParameter parameter = new AciParameter(name, 1.2f);

        // Check it...
        assertThat("name parameter not as expected", parameter.getName(), is(equalTo(name)));
        assertThat("value parameter not as expected", parameter.getValue(), is(equalTo("1.2")));
    }

    @Test
    public void testConstructorWithDouble() {
        // Values to use...
        final String name = "name";

        // Create an AciParameter object...
        final AciParameter parameter = new AciParameter(name, 1.2222222222222222222222222d);

        // Check it...
        assertThat("name parameter not as expected", parameter.getName(), is(equalTo(name)));
        assertThat("value parameter not as expected", parameter.getValue(), is(equalTo("1.2222222222222223")));
    }

    @Test
    public void testConstructorWithBoolean() {
        // Values to use...
        final String name = "name";

        // Create an AciParameter object...
        AciParameter parameter = new AciParameter(name, false);

        // Check it...
        assertThat("name parameter not as expected", parameter.getName(), is(equalTo(name)));
        assertThat("value parameter not as expected", parameter.getValue(), is(equalTo("false")));

        // Create an AciParameter object...
        parameter = new AciParameter(name, true);

        // Check it...
        assertThat("name parameter not as expected", parameter.getName(), is(equalTo(name)));
        assertThat("value parameter not as expected", parameter.getValue(), is(equalTo("true")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNull() {
        // Values to use...
        final String name = "name";

        // Create an AciParameter object...
        final AciParameter parameter = new AciParameter(name, null);

        // Check it...
        assertThat("name parameter not as expected", parameter.getName(), is(equalTo(name)));
        assertThat("value parameter not as expected", parameter.getValue(), is(nullValue()));

        // Create an AciParameter object...
        new AciParameter(null, null);

        fail("Should have thrown an IllegalArgumentException");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameProperty() {
        // This is the valure ot set the name to...
        final String name = "name";

        // Create an AciParameter object...
        final AciParameter parameter = new AciParameter();
        assertThat("name parameter is not null", parameter.getName(), is(nullValue()));

        // Set the name nad check it...
        parameter.setName(name);
        assertThat("name parameter not as expected", parameter.getName(), is(equalTo(name)));

        // Set the name to null and check it throws a wobbler...
        parameter.setName(null);

        fail("Should have thrown an IllegalArgumentException");
    }

    @Test
    public void testValueProperty() {
        // This is the valure ot set the name to...
        final String value = "value";

        // Create an AciParameter object...
        final AciParameter parameter = new AciParameter();
        assertThat("value parameter is not null", parameter.getValue(), is(nullValue()));

        // Set the value nad check it...
        parameter.setValue(value);
        assertThat("value parameter is not as expected", parameter.getValue(), is(equalTo(value)));

        // Set the name to null and check it's set to null...
        parameter.setValue(null);
        assertThat("value parameter is not null", parameter.getValue(), is(nullValue()));
    }

    @Test
    public void testEqualsMethod() {
        // Construct two parameters, with different names and values...
        final AciParameter parameter1 = new AciParameter("ACTION", "query");
        final AciParameter parameter2 = new AciParameter("action", "getcontent");
        final AciParameter parameter3 = new AciParameter("print", "query");

        // Compare to itself and the other parameter...
        assertThat("Parameter should be equal to itself", parameter1.equals(parameter1), is(true));
        assertThat("Parameters should be equal", parameter2.equals(parameter1), is(true));
        assertThat("Parameters should not be equal", parameter1.equals(parameter3), is(false));

        // Compare it to some random object, it should be false...
        assertThat("Parameters should be equal", parameter1.equals(new Object()), is(false));

        // As case-insensitivity of the letter i behaves differently in a Turkish locale...
        final Locale defaultLocale = Locale.getDefault();
        final Locale turkish = new Locale("tr");
        Locale.setDefault(turkish);

        assertThat("Parameters should be equal in a Turkish locale", parameter1.equals(parameter2), is(true));
        assertThat("Parameters should not be equal in a Turkish locale", parameter1.equals(parameter3), is(false));

        Locale.setDefault(defaultLocale);

        // Check that changing the name does change equality
        parameter2.setName("PRINT");

        assertThat("Parameters should not be equal after name change", parameter2.equals(parameter1), is(false));
        assertThat("Parameters should be equal after name change", parameter2.equals(parameter3), is(true));
    }

    @Test
    public void testHashCodeMethod() {
        // The key thing is that hashcode must be case insensitive and based purely on the value
        final AciParameter parameter1 = new AciParameter("ACTION", "query");
        final AciParameter parameter2 = new AciParameter("action", "getcontent");
        final AciParameter parameter3 = new AciParameter("print", "query");

        assertThat("Parameters should have the same hashcode", parameter1.hashCode(), is(equalTo(parameter2.hashCode())));
        assertThat("Parameters should have different hashcodes", parameter1.hashCode(), is(not(equalTo(parameter3.hashCode()))));

        // As case-insensitivity of the letter i behaves differently in a Turkish locale...
        final Locale defaultLocale = Locale.getDefault();
        final Locale turkish = new Locale("tr");
        Locale.setDefault(turkish);

        assertThat("Parameters should have the same hashcode in a Turkish locale", parameter1.hashCode(), is(equalTo(parameter2.hashCode())));
        assertThat("Parameters should have different hashcodes in a Turkish locale", parameter1.hashCode(), is(not(equalTo(parameter3.hashCode()))));

        Locale.setDefault(defaultLocale);

        // Check that changing the name does change the hashcode correctly
        parameter2.setName("PRINT");

        assertThat("Parameters should not have the same hashcode after name change", parameter1.hashCode(), is(not(equalTo(parameter2.hashCode()))));
        assertThat("Parameters should have the same hashcode after name change", parameter2.hashCode(), is(equalTo(parameter3.hashCode())));
    }

    @Test
    public void testEqualsHashCodeConsistency() {
        // Note that all we test here is that equals() and hashCode() are consistent, we don't make any assertions about
        // the correctness of that equality. This is intentional as it means that this test should still pass no matter
        // what changes are made to AciParameter.

        final AciParameter[] parameters = {
                new AciParameter("ACTION", "query"),
                new AciParameter("action", "query"),

                // Turkish i
                new AciParameter("ACT\u0130ON", "query"),
                new AciParameter("act\u0131on", "query"),

                // German double S
                new AciParameter("\u00DF", "query"),
                new AciParameter("SS", "query"),
                new AciParameter("ss", "query")
        };

        for (final AciParameter parameter1 : parameters) {
            for (final AciParameter parameter2 : parameters) {
                assertThat(
                        "Equality and hashcode should be consistent for " + parameter1.getName() + " and " + parameter2.getName(),
                        parameter1.hashCode() == parameter2.hashCode(),
                        is(equalTo(parameter1.equals(parameter2)))
                );
            }
        }
    }

}
