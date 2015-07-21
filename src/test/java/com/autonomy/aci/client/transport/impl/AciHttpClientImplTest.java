/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.impl;

import com.autonomy.aci.client.TestEncryptionCodec;
import com.autonomy.aci.client.ReflectionTestUtils;
import com.autonomy.aci.client.services.AciConstants;
import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.EncryptionCodec;
import com.autonomy.aci.client.transport.EncryptionCodecException;
import com.autonomy.aci.client.util.AciParameters;
import com.autonomy.aci.client.util.EncryptionCodecUtils;
import com.autonomy.aci.client.util.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

/**
 * JUnit test class for the <tt>com.autonomy.aci.client.transport.impl.AciHttpClientImpl</tt> class.
 */
public class AciHttpClientImplTest {

    private AciServerDetails serverDetails;

    @Before
    public void createConnectionDetails() {
        serverDetails = new AciServerDetails();
        serverDetails.setHost("localhost");
        serverDetails.setPort(9000);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultConstructor() {
        // Create an instance...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();

        // Check it...
        assertThat("HttpClient not null", aciHttpClient.getHttpClient(), is(nullValue()));
        assertThat("Use POST should be false", aciHttpClient.isUsePostMethod(), is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHttpClientConstructor() {
        // Create an instance...
        final HttpClient httpClient = new DefaultHttpClient();
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl(httpClient);

        // Check it...
        assertThat("HttpClient not as expected", aciHttpClient.getHttpClient(), is(sameInstance(httpClient)));
        assertThat("Use POST should be false", aciHttpClient.isUsePostMethod(), is(false));
    }

    @Test
    public void testHttpClientProperty() {
        // Create an instance of HttpClient...
        final HttpClient httpClient = new DefaultHttpClient();

        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();
        assertThat("HttpClient not null", aciHttpClient.getHttpClient(), is(nullValue()));

        // Set our instance on the client...
        aciHttpClient.setHttpClient(httpClient);
        assertThat("HttpClient not as expected", aciHttpClient.getHttpClient(), is(sameInstance(httpClient)));

        // Set it back to null...
        aciHttpClient.setHttpClient(null);
        assertThat("HttpClient not null", aciHttpClient.getHttpClient(), is(nullValue()));
    }

    /**
     * This is for complete code coverage, rather than being necessary... I.e. we're effectively testing
     * <tt>true == true</tt> and <tt>false == false</tt>.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testUsePostMethodProperty() {
        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();
        assertThat("usePostMethod not as expected", aciHttpClient.isUsePostMethod(), is(false));

        // Set it to true...
        aciHttpClient.setUsePostMethod(true);
        assertThat("usePostMethod not as expected", aciHttpClient.isUsePostMethod(), is(true));

        // Set it to false...
        aciHttpClient.setUsePostMethod(false);
        assertThat("usePostMethod not as expected", aciHttpClient.isUsePostMethod(), is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConvertParametersMethod() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();

        // Get our method...
        final Method method = ReflectionTestUtils.getAccessibleMethod(AciHttpClientImpl.class, "convertParameters", Set.class, String.class);

        // Here's the parameter set...
        final Set<AciParameter> parameters = new LinkedHashSet<AciParameter>();
        parameters.add(new AciParameter(AciConstants.PARAM_ACTION, "query"));
        parameters.add(new AciParameter("Text", "This is some text..."));

        // Invoke the method with the parameters...
        final String converted = (String) method.invoke(aciHttpClient, parameters, "UTF-8");

        // Check the converted parameters...
        assertThat("Converted parameters shouldn't be null", converted, is(notNullValue()));
        assertThat("Incorrect converted parameters", converted, is(equalTo("Action=query&Text=This+is+some+text...")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConvertParametersMethodActionNotFirstItem() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();

        // Get our method...
        final Method method = ReflectionTestUtils.getAccessibleMethod(AciHttpClientImpl.class, "convertParameters", Set.class, String.class);

        // Here's the parameter set...
        final Set<AciParameter> parameters = new LinkedHashSet<AciParameter>();
        parameters.add(new AciParameter("Text", "This is some text..."));
        parameters.add(new AciParameter("Combine", "Simple"));
        parameters.add(new AciParameter("Predict", false));
        parameters.add(new AciParameter(AciConstants.PARAM_ACTION, "query"));
        parameters.add(new AciParameter("MaxResults", 10));

        // Invoke the method with the parameters...
        final String converted = (String) method.invoke(aciHttpClient, parameters, "UTF-8");

        // Check the converted parameters...
        assertThat("Converted parameters shouldn't be null", converted, is(notNullValue()));
        assertThat("Incorrect converted parameters", converted, is(equalTo("Action=query&Text=This+is+some+text...&Combine=Simple&Predict=false&MaxResults=10")));
    }

    /*
     * Was throwing:
     * 
     * <pre>
     *     Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException
     *         at java.lang.System.arraycopy(Native Method)
     *         at com.autonomy.aci.client.transport.impl.AciHttpClientImpl.convertParameters(AciHttpClientImpl.java:220)
     *         ....
     * </pre>
     * 
     * When "action=xxx" instead of "Action=xxx" was found...
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testConvertParametersMethodLowerCaseAction() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();

        // Get our method...
        final Method method = ReflectionTestUtils.getAccessibleMethod(AciHttpClientImpl.class, "convertParameters", Set.class, String.class);

        // Here's the parameter set...
        final Set<AciParameter> parameters = new LinkedHashSet<AciParameter>();
        parameters.add(new AciParameter("Text", "This is some text..."));
        parameters.add(new AciParameter("Combine", "Simple"));
        parameters.add(new AciParameter("Predict", false));
        parameters.add(new AciParameter("action", "query"));
        parameters.add(new AciParameter("MaxResults", 10));

        // Invoke the method with the parameters...
        final String converted = (String) method.invoke(aciHttpClient, parameters, "UTF-8");

        // Check the converted parameters...
        assertThat("Converted parameters shouldn't be null", converted, is(notNullValue()));
        assertThat("Incorrect converted parameters", converted, is(equalTo("action=query&Text=This+is+some+text...&Combine=Simple&Predict=false&MaxResults=10")));
    }

    /**
     * This should never be possible in the real world, just just to be sure...
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testConvertParametersMethodNoAction() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();

        // Get our method...
        final Method method = ReflectionTestUtils.getAccessibleMethod(AciHttpClientImpl.class, "convertParameters", Set.class, String.class);

        // Here's the parameter set...
        final AciParameters parameters = new AciParameters(
                new AciParameter("Text", "This is some text..."),
                new AciParameter("MaxResults", true),
                new AciParameter("Print", "All")
        );

        try {
            // Invoke the method with the parameters...
            method.invoke(aciHttpClient, parameters, "UTF-8");
            fail("Should have thrown an InvocationTargetException");
        } catch (final InvocationTargetException ite) {
            assertThat(ite.getCause(), is(instanceOf(IllegalArgumentException.class)));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateGetMethod() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();

        // Get our method...
        final Method method = ReflectionTestUtils.getAccessibleMethod(AciHttpClientImpl.class, "createGetMethod", AciServerDetails.class, Set.class);

        // Here's the parameter set...
        final Set<AciParameter> parameters = new LinkedHashSet<AciParameter>();
        parameters.add(new AciParameter(AciConstants.PARAM_ACTION, "query"));
        parameters.add(new AciParameter("Text", "This is some text..."));

        // Invoke the method with the parameters...
        final HttpUriRequest request = (HttpUriRequest) method.invoke(aciHttpClient, serverDetails, parameters);

        // Check the query string...
        assertThat("Incorrect query string", request.getURI().getQuery(), is(equalTo("Action=query&Text=This+is+some+text...")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreatePostMethod() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();

        // Get our method...
        final Method method = ReflectionTestUtils.getAccessibleMethod(AciHttpClientImpl.class, "createPostMethod", AciServerDetails.class, Set.class);

        // Here's the parameter set...
        final Set<AciParameter> parameters = new LinkedHashSet<AciParameter>();
        parameters.add(new AciParameter(AciConstants.PARAM_ACTION, "query"));
        parameters.add(new AciParameter("Text", "This is some text..."));

        // Invoke the method with the parameters...
        final HttpUriRequest request = (HttpUriRequest) method.invoke(aciHttpClient, serverDetails, parameters);

        // Check the query string...
        assertThat("Incorrect query string", request.getURI().getQuery(), is(nullValue()));

        // The response should be a HttpPost, so cast it and get the entity that conbtains the query string...
        final HttpEntity entity = ((HttpPost) request).getEntity();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.getInstance().copy(entity.getContent(), baos);

        // Check the request body...
        assertThat("Incorrect query string", baos.toString("UTF-8"), is(equalTo("Action=query&Text=This+is+some+text...")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateEncryptedParameters() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, EncryptionCodecException {
        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();

        // Get our method...
        final Method method = ReflectionTestUtils.getAccessibleMethod(AciHttpClientImpl.class, "createEncryptedParameters", AciServerDetails.class, Set.class);

        // Add an encryption codec to the server details... 
        serverDetails.setEncryptionCodec(new TestEncryptionCodec());

        // Here's the parameter set...
        final Set<AciParameter> parameters = new LinkedHashSet<AciParameter>();
        parameters.add(new AciParameter(AciConstants.PARAM_ACTION, "query"));
        parameters.add(new AciParameter("Text", "This is some text..."));

        // Invoke the method with the parameters...
        final Set<AciParameter> encrypted = (Set<AciParameter>) method.invoke(aciHttpClient, serverDetails, parameters);

        // Correctly encode the query string before encrypting it...
        final String queryString = URLEncodedUtils.format(Arrays.asList(new BasicNameValuePair(AciConstants.PARAM_ACTION, "query"), new BasicNameValuePair("Text", "This is some text...")), "UTF-8");
        final String encryptedQueryString = EncryptionCodecUtils.getInstance().encrypt(serverDetails.getEncryptionCodec(), queryString, serverDetails.getCharsetName());

        // Check the encrypted parameters...
        assertThat("Incorrect query string", encrypted, hasItems(
                new AciParameter(AciConstants.PARAM_ACTION, AciConstants.ACTION_ENCRYPTED),
                new AciParameter(AciConstants.PARAM_DATA, encryptedQueryString)
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConstructHttpRequest() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();

        // Get our method...
        final Method method = ReflectionTestUtils.getAccessibleMethod(AciHttpClientImpl.class, "constructHttpRequest", AciServerDetails.class, Set.class);

        // Here's the parameter set...
        final Set<AciParameter> parameters = new LinkedHashSet<AciParameter>();
        parameters.add(new AciParameter(AciConstants.PARAM_ACTION, "query"));
        parameters.add(new AciParameter("Text", "This is some text..."));

        // Invoke and we should get a GetMethod back...
        HttpUriRequest request = (HttpUriRequest) method.invoke(aciHttpClient, serverDetails, parameters);
        assertThat("Incorrect HTTP method", request.getMethod(), is(equalTo("GET")));
        assertThat("Incorrect query string", request.getURI().getQuery(), is(equalTo("Action=query&Text=This+is+some+text...")));

        // Set it to use POST and try again...
        aciHttpClient.setUsePostMethod(true);
        request = (HttpUriRequest) method.invoke(aciHttpClient, serverDetails, parameters);
        assertThat("Incorrect HTTP method", request.getMethod(), is(equalTo("POST")));
        HttpEntity entity = ((HttpPost) request).getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.getInstance().copy(entity.getContent(), baos);
        assertThat("Incorrect query string", baos.toString("UTF-8"), is(equalTo("Action=query&Text=This+is+some+text...")));

        // Set an encryption codec...
        serverDetails.setEncryptionCodec(new TestEncryptionCodec());
        request = (HttpUriRequest) method.invoke(aciHttpClient, serverDetails, parameters);
        assertThat("Incorrect HTTP method", request.getMethod(), is(equalTo("POST")));
        entity = ((HttpPost) request).getEntity();
        baos = new ByteArrayOutputStream();
        IOUtils.getInstance().copy(entity.getContent(), baos);
        assertThat("Incorrect query string", baos.toString("UTF-8"), startsWith("Action=Encrypted&Data="));

        // Get rid of the POST, but keep the encryption codec...
        aciHttpClient.setUsePostMethod(false);
        request = (HttpUriRequest) method.invoke(aciHttpClient, serverDetails, parameters);
        assertThat("Incorrect HTTP method", request.getMethod(), is(equalTo("GET")));
        assertThat("Incorrect query string", request.getURI().getQuery(), startsWith("Action=Encrypted&Data="));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteActionNoHttpClient() throws IOException, AciHttpException {
        // Create our client and execute...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();
        aciHttpClient.executeAction(serverDetails, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteActionNoParameters() throws IOException, AciHttpException {
        // Create our client and execute...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl(new DefaultHttpClient());
        aciHttpClient.executeAction(serverDetails, null);
    }

    @Test
    public void testExecuteNon200StatusCode() throws IOException {
        // Create a response that has a non-200 status code
        final HttpResponse mockHttpResponse = mock(HttpResponse.class);
        final StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);

        // Create our mock HttpClient object...
        final HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(mockHttpResponse);

        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl(mockHttpClient);

        try {
            // Ensure it works with status codes less than 200...
            when(mockStatusLine.getStatusCode()).thenReturn(196);

            // Execute...
            aciHttpClient.executeAction(
                    new AciServerDetails("localhost", 9000),
                    new AciParameters(
                            new AciParameter(AciConstants.PARAM_ACTION, "query"),
                            new AciParameter("Text", "This is some text...")
                    )
            );
            fail("should have thrown an AciHttpException.");
        } catch (final AciHttpException e) {
        }

        try {
            // Ensure it works with status codes greater or equal to 300...
            when(mockStatusLine.getStatusCode()).thenReturn(300);

            // Execute...
            aciHttpClient.executeAction(
                    new AciServerDetails("localhost", 9000),
                    new AciParameters(
                            new AciParameter(AciConstants.PARAM_ACTION, "query"),
                            new AciParameter("Text", "This is some text...")
                    )
            );
            fail("should have thrown an AciHttpException.");
        } catch (final AciHttpException e) {
        }
    }

    @Test(expected = IOException.class)
    public void testExecuteActionIOException() throws IOException, AciHttpException {
        // Create our mock HttpClient object...
        final HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenThrow(new IOException("JUnit test exception."));

        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl(mockHttpClient);

        // Execute...
        aciHttpClient.executeAction(
                new AciServerDetails("localhost", 9000),
                new AciParameters(
                        new AciParameter(AciConstants.PARAM_ACTION, "query"),
                        new AciParameter("Text", "This is some text...")
                )
        );
        fail("should have thrown an IOException.");
    }

    @Test(expected = AciHttpException.class)
    public void testExecuteActionClientProtocolException() throws IOException, AciHttpException {
        // Create our mock HttpClient object...
        final HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenThrow(new ClientProtocolException("JUnit test exception."));

        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl(mockHttpClient);

        // Execute...
        aciHttpClient.executeAction(
                new AciServerDetails("localhost", 9000),
                new AciParameters(
                        new AciParameter(AciConstants.PARAM_ACTION, "query"),
                        new AciParameter("Text", "This is some text...")
                )
        );
        fail("should have thrown an AciHttpException.");
    }

    @Test(expected = AciHttpException.class)
    public void testExecuteActionEncryptionCodecException() throws IOException, AciHttpException, EncryptionCodecException {
        // Create our mock EncryptionCode...
        final EncryptionCodec mockEncryptionCodec = mock(EncryptionCodec.class);
        when(mockEncryptionCodec.encrypt((byte[]) any())).thenThrow(new EncryptionCodecException("JUnit test exception."));

        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl(mock(HttpClient.class));

        // Create some server details that will return the nobbled encryption codec...
        final AciServerDetails aciServerDetails = new AciServerDetails("localhost", 9000);
        aciServerDetails.setEncryptionCodec(mockEncryptionCodec);

        // Execute...
        aciHttpClient.executeAction(
                aciServerDetails,
                new AciParameters(
                        new AciParameter(AciConstants.PARAM_ACTION, "query"),
                        new AciParameter("Text", "This is some text...")
                )
        );
        fail("should have thrown an AciHttpException.");
    }

    @Test(expected = AciHttpException.class)
    public void testExecuteActionURISyntaxException() throws IOException, AciHttpException {
        // Execute...
        new AciHttpClientImpl(mock(HttpClient.class)).executeAction(
                new AciServerDetails("localh%weost", 9000),
                new AciParameters(
                        new AciParameter(AciConstants.PARAM_ACTION, "query"),
                        new AciParameter("Text", "This should cause an exception...")
                )
        );
        fail("should have thrown an AciHttpException.");
    }

    @Test
    public void testExecuteActionAciResponseInputStreamImpl() throws IOException, AciHttpException {
        // Create a response that has a 200 status code...
        final StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(200);

        final HttpEntity mockHttpEntity = mock(HttpEntity.class);
        when(mockHttpEntity.getContent()).thenReturn(getClass().getResourceAsStream("/com/autonomy/aci/client/transport/impl/UnencryptedResponse.xml"));

        final HttpResponse mockHttpResponse = mock(HttpResponse.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        // Create our mock HttpClient object...
        final HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(mockHttpResponse);

        // Execute...
        final AciResponseInputStream response = new AciHttpClientImpl(mockHttpClient).executeAction(
                new AciServerDetails("localhost", 9000),
                new AciParameters(
                        new AciParameter(AciConstants.PARAM_ACTION, "query"),
                        new AciParameter("Text", "This is some text...")
                )
        );

        // Check we get the right response implementation...
        assertThat(response, is(instanceOf(AciResponseInputStreamImpl.class)));
    }

    @Test
    public void testExecuteActionDecryptingAciResponseInputStreamImpl() throws IOException, AciHttpException {
        // Create a response that has a 200 status code...
        final StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(200);

        final HttpEntity mockHttpEntity = mock(HttpEntity.class);
        when(mockHttpEntity.getContent()).thenReturn(getClass().getResourceAsStream("/com/autonomy/aci/client/transport/impl/EncryptedResponse.xml"));

        final HttpResponse mockHttpResponse = mock(HttpResponse.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
        when(mockHttpResponse.getFirstHeader("AUTN-Content-Type")).thenReturn(new BasicHeader("AUTN-Content-Type", "text/xml"));

        // Create our mock HttpClient object...
        final HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(mockHttpResponse);

        // Create some server details that will return an encryption codec...
        final AciServerDetails aciServerDetails = new AciServerDetails("localhost", 9000);
        aciServerDetails.setEncryptionCodec(new TestEncryptionCodec());

        // Execute...
        final AciResponseInputStream response = new AciHttpClientImpl(mockHttpClient).executeAction(
                aciServerDetails,
                new AciParameters(
                        new AciParameter(AciConstants.PARAM_ACTION, "query"),
                        new AciParameter("Text", "This is some text...")
                )
        );

        // Check we get the right response implementation...
        assertThat(response, is(instanceOf(DecryptingAciResponseInputStreamImpl.class)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecryptResponse() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Get the method to test...
        final Method method = ReflectionTestUtils.getAccessibleMethod(AciHttpClientImpl.class, "decryptResponse", EncryptionCodec.class, HttpResponse.class);

        // This is mock method to use for response headers...
        final BasicHttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");

        // Create our client...
        final AciHttpClientImpl aciHttpClient = new AciHttpClientImpl();

        // This should return false...
        boolean result = (Boolean) method.invoke(aciHttpClient, null, httpResponse);
        assertThat("Incorrect result", result, is(false));

        // This should also return false...
        result = (Boolean) method.invoke(aciHttpClient, new TestEncryptionCodec(), httpResponse);
        assertThat("Incorrect result", result, is(false));

        // This should also return true...
        httpResponse.setHeader(new BasicHeader("AUTN-Content-Type", "text/xml"));
        result = (Boolean) method.invoke(aciHttpClient, new TestEncryptionCodec(), httpResponse);
        assertThat("Incorrect result", result, is(true));
    }

}
