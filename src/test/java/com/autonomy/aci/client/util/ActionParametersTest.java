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

package com.autonomy.aci.client.util;

import com.autonomy.aci.client.services.AciConstants;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.ActionParameter;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Junit tests for <tt>com.autonomy.aci.client.util.AciParameters</tt>.
 */
@SuppressWarnings("deprecation")
public class ActionParametersTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultConstructor() {
        // Check it's empty...
        assertThat("Non-empty parameter set", new ActionParameters().isEmpty(), is(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAciParameterConstructor() {
        // This is the parameter to test with...
        final AciParameter parameter = new AciParameter("action", "test");

        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(parameter);

        // Check it...
        assertThat("Wrong size for parameter set", parameters.size(), is(1));
        assertThat("Wrong parameter stored", parameters.iterator().next(), is(sameInstance(parameter)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testActionConstructor() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters("test");

        // Check it...
        assertThat("Wrong size for parameter set", parameters.size(), is(1));

        final ActionParameter<?> parameter = parameters.iterator().next();
        assertThat("Wrong parameter stored", parameter.getName(), is(AciConstants.PARAM_ACTION));
        assertThat("Wrong parameter stored", parameter.getValue(), is("test"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCollectionConstructorNoDupes() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // Check it...
        assertThat("Wrong size for parameter set", parameters.size(), is(3));
        assertThat("Wrong parameters stored", parameters, hasItems(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCollectionConstructorDupes() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("action", "test"),
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // Check it...
        assertThat("Wrong size for parameter set", parameters.size(), is(3));
        assertThat("Wrong parameters stored", parameters, hasItems(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHashCode() {
        final ActionParameters parameters1 = ActionParameters.convert(Arrays.asList(
                new AciParameter("action", "query"),
                new AciParameter("text", "test")
        ));

        final ActionParameters parameters2 = ActionParameters.convert(Arrays.asList(
                new AciParameter("action", "suggest"),
                new AciParameter("reference", "test")
        ));

        assertThat("hascode", parameters1.hashCode(), is(parameters1.hashCode()));
        assertThat("hascode", parameters1.hashCode(), is(not(parameters2.hashCode())));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEquals() {
        final ActionParameters parameters1 = ActionParameters.convert(Arrays.asList(
                new AciParameter("action", "query"),
                new AciParameter("text", "test")
        ));

        final ActionParameters parameters2 = ActionParameters.convert(Arrays.asList(
                new AciParameter("action", "suggest"),
                new AciParameter("reference", "test")
        ));

        assertThat("hascode", parameters1.equals(parameters1), is(true));
        assertThat("hascode", parameters1.equals(parameters2), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unchecked")
    public void testAddMethod() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters();

        // Add a paramameter...
        boolean result = parameters.add("action", "test");
        assertThat("Wrong result", result, is(true));
        assertThat("Wrong size for parameter set", parameters.size(), is(1));

        // Try and add the same parameter again...
        result = parameters.add("action", "test");
        assertThat("Wrong result", result, is(false));
        assertThat("Wrong size for parameter set", parameters.size(), is(1));

        // Add a parameter with a null value...
        result = parameters.add("wibble", null);
        assertThat("Wrong result", result, is(true));
        assertThat("Wrong size for parameter set", parameters.size(), is(2));

        // Add a parameter with a null name...
        parameters.add(null, null);
        fail("Should have thrown an IllegalArgumentException");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveMethod() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));
        assertThat("Wrong size for parameter set", parameters.size(), is(3));

        // Remove a paramameter...
        boolean result = parameters.remove("combine");
        assertThat("Wrong result", result, is(true));
        assertThat("Wrong size for parameter set", parameters.size(), is(2));

        // Try and remove the same parameter again...
        result = parameters.remove("combine");
        assertThat("Wrong result", result, is(false));
        assertThat("Wrong size for parameter set", parameters.size(), is(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testContainsObjectMethod() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // Test a known good parameter...
        final AciParameter parameter = new AciParameter("Combine", "UUID");

        boolean result = parameters.contains(parameter);
        assertThat("Wrong result", result, is(true));

        // We've had problems with multiple contains methods, so ensure it also works when the parameter is an 'Object'
        final Object parameterAsObject = parameter;

        result = parameters.contains(parameterAsObject);
        assertThat("Wrong result", result, is(true));

        // Try a random name...
        result = parameters.contains(new AciParameter("aoljds", false));
        assertThat("Wrong result", result, is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testContainsStringMethod() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // Test a known good parameter...
        boolean result = parameters.contains("Combine");
        assertThat("Wrong result", result, is(true));

        // We've had problems with multiple contains methods, so ensure it also works when the parameter is an 'Object'.
        // Note that this should return false, as it should be calling the other contains method which is part of the
        // Set contract and as such obliged to return false.
        final Object parameterNameAsObject = "Combine";

        result = parameters.contains(parameterNameAsObject);
        assertThat("Wrong result", result, is(false));

        // Try a random name...
        result = parameters.contains(new AciParameter("aoljds", false));
        assertThat("Wrong result", result, is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testContainsStringMethodWithTurkishLocale() {
        // As case-insensitivity of the letter i behaves differently in a Turkish locale...
        final Locale defaultLocale = Locale.getDefault();
        final Locale turkish = new Locale("tr");
        Locale.setDefault(turkish);

        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("ACTION", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // Test case conversion both ways round...
        boolean result = parameters.contains("action");
        assertThat("Wrong result", result, is(true));

        result = parameters.contains("COMBINE");
        assertThat("Wrong result", result, is(true));

        Locale.setDefault(defaultLocale);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testContainsAll() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // These parameters are all contained
        boolean result = parameters.containsAll(Arrays.asList(
                new AciParameter("Action", "query"),
                new AciParameter("MaxResults", 6)
        ));

        assertThat("Wrong result", result, is(true));

        // Check that a special case isn't made for Strings: this would violate the contract of containsAll
        result = parameters.containsAll(Collections.singletonList("maxresults"));

        assertThat("Wrong result", result, is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetMethod() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // Test a known good parameter...
        String result = (String) parameters.get("combine");
        assertThat("Wrong result", result, is(equalTo("simple")));

        // Try a random name...
        result = (String) parameters.get("aoljds");
        assertThat("Wrong result", result, is(nullValue()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetMethodWithTurkishLocale() {
        // As case-insensitivity of the letter i behaves differently in a Turkish locale...
        final Locale defaultLocale = Locale.getDefault();
        final Locale turkish = new Locale("tr");
        Locale.setDefault(turkish);

        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("ACTION", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // Test case conversion both ways round...
        String result = (String) parameters.get("action");
        assertThat("Wrong result", result, is(equalTo("test")));

        result = (String) parameters.get("COMBINE");
        assertThat("Wrong result", result, is(equalTo("simple")));

        Locale.setDefault(defaultLocale);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPutMethod() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // Test a known new parameter...
        String result = (String) parameters.put("print", "all");
        assertThat("Wrong result", result, is(nullValue()));

        // Try putting an existing paramter...
        result = (String) parameters.put("print", "fields");
        assertThat("Wrong result", result, is(equalTo("all")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPutAciParameterMethod() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // Test a known new parameter...
        ActionParameter<?> result = parameters.put(new AciParameter("print", "all"));
        assertThat("Wrong result", result, is(nullValue()));

        // Try putting an existing paramter...
        result = parameters.put(new AciParameter("print", "fields"));
        assertThat("Wrong result", result, is(equalTo(new AciParameter("print", "all"))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPutAllMethod() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(Arrays.asList(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        ));

        // Put some more parameters into the set...
        parameters.putAll(Arrays.asList(
                new AciParameter("print", "all"),
                new AciParameter("combine", "FieldCheck")
        ));

        // Check the contents are correct...
        assertThat("Wrong size", parameters, hasItems(
                new AciParameter("action", "test"),
                new AciParameter("maxresults", 10),
                new AciParameter("print", "all"),
                new AciParameter("combine", "FieldCheck")
        ));

        // Check that nothing changes if we try and putAll on a null...
        parameters.putAll((Collection<ActionParameter<?>>) null);

        // Check the contents are still correct...
        assertThat("Wrong size", parameters, hasItems(
                new AciParameter("action", "test"),
                new AciParameter("maxresults", 10),
                new AciParameter("print", "all"),
                new AciParameter("combine", "FieldCheck")
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testOtherPutAllMethod() {
        // Create a new instance...
        final ActionParameters parameters = new ActionParameters(
                new AciParameter("action", "test"),
                new AciParameter("combine", "simple"),
                new AciParameter("maxresults", 10)
        );

        // Put some more parameters into the set...
        parameters.putAll(
                new AciParameter("print", "all"),
                new AciParameter("combine", "FieldCheck")
        );

        // Check the contents are correct...
        assertThat("Wrong size", parameters, hasItems(
                new AciParameter("action", "test"),
                new AciParameter("maxresults", 10),
                new AciParameter("print", "all"),
                new AciParameter("combine", "FieldCheck")
        ));

        // Check that nothing changes if we try and putAll with no varargs...
        final AciParameter[] array = null;
        parameters.putAll(array);

        // Check the contents are correct...
        assertThat("Wrong size", parameters, hasItems(
                new AciParameter("action", "test"),
                new AciParameter("maxresults", 10),
                new AciParameter("print", "all"),
                new AciParameter("combine", "FieldCheck")
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConvert() {
        final ActionParameters aciParameters = ActionParameters.convert(Arrays.asList(
                new AciParameter("action", "query"),
                new AciParameter("text", "*"),
                new AciParameter("print", "all")
        ));

        assertThat("Wrong size", aciParameters.size(), is(3));
        assertThat("Wrong size", aciParameters, hasItems(
                new AciParameter("action", "query"),
                new AciParameter("text", "*"),
                new AciParameter("print", "all")
        ));

        // Converting an AciParameters should just return it unchanged
        assertThat("Not the same", ActionParameters.convert(aciParameters), sameInstance(aciParameters));
        assertThat("Wrong size", aciParameters.size(), is(3));
    }

    @Test
    public void testToArray() {
        final ActionParameters parameters = new ActionParameters();
        parameters.add("action", "query");
        parameters.add("text", "*");
        parameters.add("combine", "simple");

        ActionParameter<?>[] parameterArray = parameters.toArray();
        assertThat(parameterArray.length, is(3));
        assertThat(parameterArray, is(arrayContaining(
                new AciParameter("action", "query"),
                new AciParameter("text", "*"),
                new AciParameter("combine", "simple")
        )));

        // Modify the parameter set...
        parameters.put(new AciParameter("combine", "reference"));
        parameters.add(new AciParameter("print", "all"));

        parameterArray = parameters.toArray(new AciParameter[parameters.size()]);
        assertThat(parameterArray.length, is(4));
        assertThat(parameterArray, is(arrayContaining(
                new AciParameter("action", "query"),
                new AciParameter("text", "*"),
                new AciParameter("combine", "reference"),
                new AciParameter("print", "all")
        )));
    }

    @Test
    public void testRetainAll() {
        final ActionParameters parameters = new ActionParameters(
                new AciParameter("action", "query"),
                new AciParameter("text", "*"),
                new AciParameter("combine", "reference"),
                new AciParameter("print", "all")
        );

        assertThat(parameters, contains(
                new AciParameter("action", "query"),
                new AciParameter("text", "*"),
                new AciParameter("combine", "reference"),
                new AciParameter("print", "all")
        ));

        parameters.retainAll(new AciParameters(
                new AciParameter("action", "query"),
                new AciParameter("text", "Doesn't matter")
        ));

        assertThat(parameters, contains(
                new AciParameter("action", "query"),
                new AciParameter("text", "*")
        ));
    }

    @Test
    public void testRemoveAll() {
        final ActionParameters parameters = new ActionParameters(
                new AciParameter("action", "query"),
                new AciParameter("text", "*"),
                new AciParameter("combine", "reference"),
                new AciParameter("print", "all")
        );

        assertThat(parameters, contains(
                new AciParameter("action", "query"),
                new AciParameter("text", "*"),
                new AciParameter("combine", "reference"),
                new AciParameter("print", "all")
        ));

        parameters.removeAll(new AciParameters(
                new AciParameter("action", "query"),
                new AciParameter("text", "Doesn't matter")
        ));

        assertThat(parameters, contains(
                new AciParameter("combine", "reference"),
                new AciParameter("print", "all")
        ));
    }

    @Test
    public void testClear() {
        final ActionParameters parameters = new ActionParameters();
        parameters.add("action", "query");
        parameters.add("text", "*");
        parameters.add("combine", "simple");

        assertThat(parameters.isEmpty(), is(false));

        parameters.clear();

        assertThat(parameters.isEmpty(), is(true));
    }

    @Test
    public void testInputStreamParametersNotStringified() {
        final ActionParameters parameters = new ActionParameters();
        parameters.add("image", new ByteArrayInputStream(new byte[3]));

        @SuppressWarnings("unchecked")
        final Collection<InputStream> images = (Collection<InputStream>) parameters.get("image");

        images.forEach(image -> assertThat(image, is(instanceOf(InputStream.class))));
    }

}
