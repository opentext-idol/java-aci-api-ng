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

package com.autonomy.aci.client.transport.impl;

import com.autonomy.aci.client.services.AciConstants;
import com.autonomy.aci.client.transport.*;
import com.autonomy.aci.client.util.ActionParameters;
import com.autonomy.aci.client.util.EncryptionCodecUtils;
import org.apache.commons.lang3.Validate;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Implementation of the {@link com.autonomy.aci.client.transport.AciHttpClient} interface that provides the actual HTTP
 * communication mechanism. This implementation uses the HttpClient provided by the <a href="http://hc.apache.org/">
 * Apache HttpComponents</a> project. It defaults to using the HTTP <tt>GET</tt> method, if you wish to send ACI actions
 * with the HTTP <tt>POST</tt> method, then call the {@link #setUsePostMethod(boolean)} method with {@code true}.
 * <p>
 * This implementation of the {@link com.autonomy.aci.client.transport.AciHttpClient} interface does no configuration of
 * the {@code HttpClient} that it uses. It expects all the configuration to have been done by the user before passing it
 * to this object. This configuration can be done in normal code, via the
 * {@link com.autonomy.aci.client.transport.impl.HttpClientFactory}, or via an IoC container like
 * <a href="http://www.springsource.org/">Spring</a>.
 * @see <a href="http://hc.apache.org/">Apache HttpComponents</a>
 */
public class AciHttpClientImpl implements AciHttpClient {

    /**
     * Class logger...
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AciHttpClientImpl.class);

    /**
     * Holds the {@code HttpClient} that will do the work. By allowing it to be passed in as a parameter, it means it
     * can be configured in an IoC container like {@code Spring} before being injected.
     */
    private HttpClient httpClient5;

    /**
     * Holds the {@code HttpClient} that will do the work. By allowing it to be passed in as a parameter, it means it
     * can be configured in an IoC container like {@code Spring} before being injected.
     */
    private org.apache.http.client.HttpClient httpClient;

    /**
     * Holds value of property usePostMethod.
     */
    private boolean usePostMethod;

    /**
     * Creates a new instance of AciHttpClientImpl. The {@code setHttpClient} method <strong>must</strong> must be
     * called before tyring to use this object to execute  an ACI action, otherwise a {@code NullPointerException} will
     * be generated.
     */
    public AciHttpClientImpl() {
        // Empty...
    }

    /**
     * Creates a new instance of AciHttpClientImpl.
     * @param httpClient The {@code HttpClient} to use
     */
    public AciHttpClientImpl(final HttpClient httpClient) {
        httpClient5 = httpClient;
    }

    /**
     * Creates a new instance of AciHttpClientImpl.
     * @param httpClient The {@code HttpClient} to use
     */
    public AciHttpClientImpl(final org.apache.http.client.HttpClient httpClient) {
        // Save the httpClient...
        this.httpClient = httpClient;
    }

    /**
     * Turns the {@code parameters} and {@code serverDetails} into either an HTTP GET or POST request.
     * @param serverDetails The details of the ACI server the request will be sent to
     * @param parameters    The parameters to send with the ACI action.
     * @return A HTTP GET or POST request that can be used to execute the ACI action
     * @throws EncryptionCodecException     If something went wrong encrypting the parameters
     * @throws URISyntaxException           If something went wrong creating the URI to send the action to
     * @throws UnsupportedEncodingException If there was a problem working with the parameters in the specified
     *                                      character encoding
     */
    private HttpUriRequest constructHttp5Request(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws EncryptionCodecException, URISyntaxException, UnsupportedEncodingException {
        LOGGER.trace("constructHttpMethod() called...");

        // Copy the parameters...
        final Set<? extends ActionParameter<?>> params = (serverDetails.getEncryptionCodec() == null)
                ? parameters
                : createEncryptedParameters(serverDetails, parameters);

        final boolean hasPostParameter = parameters.stream()
                .anyMatch(ActionParameter::requiresPostRequest);

        // If an InputStream parameter has been provided, use a post request regardless
        if (usePostMethod || hasPostParameter) {
            return createPost5Method(serverDetails, params);
        } else {
            return createGet5Method(serverDetails, params);
        }
    }

    @Deprecated
    private org.apache.http.client.methods.HttpUriRequest constructHttpRequest(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws EncryptionCodecException, URISyntaxException, UnsupportedEncodingException {
        LOGGER.trace("constructHttpMethod() called...");

        // Copy the parameters...
        final Set<? extends ActionParameter<?>> params = (serverDetails.getEncryptionCodec() == null)
                ? parameters
                : createEncryptedParameters(serverDetails, parameters);

        final boolean hasPostParameter = parameters.stream()
                .anyMatch(ActionParameter::requiresPostRequest);

        // If an InputStream parameter has been provided, use a post request regardless
        if (usePostMethod || hasPostParameter) {
            return createPostMethod(serverDetails, params);
        } else {
            return createGetMethod(serverDetails, params);
        }
    }

    /**
     * Takes the passed in set of parameters and encrypts them.
     * @param serverDetails The details of the ACI server the request will be sent to
     * @param parameters    The parameters to send with the ACI action.
     * @return A set of encrypted parameters
     * @throws EncryptionCodecException if something went wrong encrypting the parameters
     */
    private Set<? extends ActionParameter<?>> createEncryptedParameters(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws EncryptionCodecException {
        LOGGER.trace("createEncryptedParameters() called...");

        // Generate the query String and put it through the codec...
        final String data = EncryptionCodecUtils.getInstance().encrypt(
                serverDetails.getEncryptionCodec(),
                convertParameters(parameters, serverDetails.getCharsetName()),
                serverDetails.getCharsetName()
        );

        // Create the parameters for an encrypted action...
        return new ActionParameters(
                new AciParameter(AciConstants.PARAM_ACTION, AciConstants.ACTION_ENCRYPTED),
                new AciParameter(AciConstants.PARAM_DATA, data)
        );
    }

    /**
     * Create a {@code GetMethod} and adds the ACI parameters to the query string.
     * @param serverDetails The details of the ACI server the request will be sent to
     * @param parameters    The parameters to send with the ACI action.
     * @return a {@code HttpGet} that is ready to execute the ACI action.
     * @throws URISyntaxException If there was a problem construction the request URI from the <tt>serverDetails</tt>
     *                            and <tt>parameters</tt>
     */
    private HttpUriRequest createGet5Method(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws URISyntaxException {
        LOGGER.trace("createGetMethod() called...");

        // Create the URI to use...
        final URI uri = new URIBuilder()
                .setScheme(serverDetails.getProtocol().toString().toLowerCase(Locale.ENGLISH))
                .setHost(serverDetails.getHost())
                .setPort(serverDetails.getPort())
                .setPath("/")
                .setCustomQuery(convertParameters(parameters, serverDetails.getCharsetName()))
                .build();

        // Return the constructed get method...
        return new HttpGet(uri);
    }

    @Deprecated
    private org.apache.http.client.methods.HttpUriRequest createGetMethod(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws URISyntaxException {
        LOGGER.trace("createGetMethod() called...");

        // Create the URI to use...
        final URI uri = new URIBuilder()
                .setScheme(serverDetails.getProtocol().toString().toLowerCase(Locale.ENGLISH))
                .setHost(serverDetails.getHost())
                .setPort(serverDetails.getPort())
                .setPath("/")
                .setCustomQuery(convertParameters(parameters, serverDetails.getCharsetName()))
                .build();

        // Return the constructed get method...
        return new org.apache.http.client.methods.HttpGet(uri);
    }

    /**
     * Create a {@code PostMethod} and adds the ACI parameters to the request body.
     * @param serverDetails The details of the ACI server the request will be sent to
     * @param parameters    The parameters to send with the ACI action.
     * @return An {@code HttpPost} that is ready to execute the ACI action.
     * @throws UnsupportedEncodingException Will be thrown if <tt>serverDetails.getCharsetName()</tt> returns a
     *                                      charset that is not supported by the JVM
     * @throws URISyntaxException           If there was a problem construction the request URI from the
     *                                      <tt>serverDetails</tt> and <tt>parameters</tt>
     */
    private HttpUriRequest createPost5Method(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws URISyntaxException, UnsupportedEncodingException {
        LOGGER.trace("createPostMethod() called...");

        // Create the URI to use...
        final URI uri = new URIBuilder()
                .setScheme(serverDetails.getProtocol().toString().toLowerCase(Locale.ENGLISH))
                .setHost(serverDetails.getHost())
                .setPort(serverDetails.getPort())
                .setPath("/")
                .build();

        // Create the method...
        final HttpPost method = new HttpPost(uri);

        final Charset charset = Charset.forName(serverDetails.getCharsetName());

        final boolean requiresMultipart = parameters.stream()
                .anyMatch(ActionParameter::requiresPostRequest);

        if (requiresMultipart) {
            final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.setCharset(charset);

            parameters.forEach(parameter -> parameter.addToEntity(multipartEntityBuilder, charset));

            // Convert the parameters into an entity...
            method.setEntity(multipartEntityBuilder.build());
        }
        else {
            method.setEntity(new StringEntity(convertParameters(parameters, serverDetails.getCharsetName()), charset));
        }

        // Return the method...
        return method;
    }

    private org.apache.http.client.methods.HttpUriRequest createPostMethod(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws URISyntaxException, UnsupportedEncodingException {
        LOGGER.trace("createPostMethod() called...");

        // Create the URI to use...
        final URI uri = new URIBuilder()
                .setScheme(serverDetails.getProtocol().toString().toLowerCase(Locale.ENGLISH))
                .setHost(serverDetails.getHost())
                .setPort(serverDetails.getPort())
                .setPath("/")
                .build();

        // Create the method...
        final org.apache.http.client.methods.HttpPost method = new org.apache.http.client.methods.HttpPost(uri);

        final Charset charset = Charset.forName(serverDetails.getCharsetName());

        final boolean requiresMultipart = parameters.stream()
                .anyMatch(ActionParameter::requiresPostRequest);

        if (requiresMultipart) {
            final org.apache.http.entity.mime.MultipartEntityBuilder multipartEntityBuilder = org.apache.http.entity.mime.MultipartEntityBuilder.create();
            multipartEntityBuilder.setCharset(charset);

            parameters.forEach(parameter -> parameter.addToEntity(multipartEntityBuilder, charset));

            // Convert the parameters into an entity...
            method.setEntity(multipartEntityBuilder.build());
        }
        else {
            method.setEntity(new org.apache.http.entity.StringEntity(convertParameters(parameters, serverDetails.getCharsetName()), charset));
        }

        // Return the method...
        return method;
    }

    /**
     * Converts a list of {@code AciParameter} objects into an array of {@code NameValuePair} objects suitable for use
     * in both POST and GET methods.
     * @param parameters  The set of parameters to convert.
     * @param charsetName The name of the charset to use when encoding the parameters
     * @return an <tt>String</tt> representing the query string portion of a URI
     */
    private String convertParameters(final Set<? extends ActionParameter<?>> parameters, final String charsetName) {
        LOGGER.trace("convertParameters() called...");

        // Just incase, remove the allowed null entry...
        parameters.remove(null);

        final List<NameValuePair> pairs = new ArrayList<>(parameters.size());

        LOGGER.debug("Converting {} parameters...", parameters.size());

        NameValuePair actionPair = null;

        for (final ActionParameter<?> parameter : parameters) {
            final Object value = parameter.getValue();

            if (value instanceof String) {
                final String stringValue = (String) value;

                if (AciConstants.PARAM_ACTION.equalsIgnoreCase(parameter.getName())) {
                    actionPair = new BasicNameValuePair(parameter.getName(), stringValue);
                } else {
                    pairs.add(new BasicNameValuePair(parameter.getName(), stringValue));
                }
            }
        }

        // Ensure that the action=XXX parameter is the first thing in the list...
        Validate.isTrue(actionPair != null, "No action parameter found in parameter set, please set one before trying to execute an ACI request.");
        pairs.add(0, actionPair);

        // Convert to a string and return...
        return URLEncodedUtils.format(pairs, Charset.forName(charsetName));
    }

    /**
     * Execute an ACI action on the specific ACI server.
     * @param serverDetails Details of the ACI server to send the action to
     * @param parameters    The parameters to send with the ACI action
     * @return An <tt>AciResponseInputStream</tt> containing the ACI response
     * @throws IOException              If an I/O (transport) error occurs. Some transport exceptions can be recovered from
     * @throws AciHttpException         If a protocol exception occurs. Usually protocol exceptions cannot be recovered from
     * @throws IllegalArgumentException if the <tt>httpClient</tt> property is <tt>null</tt> or <tt>parameters</tt> is <tt>null</tt>
     */
    @Override
    public AciResponseInputStream executeAction(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws IOException, AciHttpException {
        LOGGER.trace("executeAction() called...");

        Validate.notEmpty(parameters, "The parameter set must not be null or empty.");

        LOGGER.debug("Executing action on {}:{}...", serverDetails.getHost(), serverDetails.getPort());
        try {
            if (httpClient5 != null) {

                final HttpUriRequest request = constructHttp5Request(serverDetails, parameters);
                return httpClient5.execute(request, response -> {
                    final int statusCode = response.getCode();
                    LOGGER.debug("Executed method and got status code - {}...", statusCode);

                    // Treat anything other than a 2xx status code as an error...
                    if ((statusCode < 200) || (statusCode >= 300)) {
                        // close the connection so it can be reused
                        EntityUtils.consume(response.getEntity());

                        throw new HttpException(
                                "The server returned a status code, " + statusCode +
                                        ", that wasn't in the 2xx Success range.");
                    }

                    // Decorate the InputStream so we can release the HTTP connection once the stream's been read...
                    return decryptResponse(serverDetails.getEncryptionCodec(), response)
                            ? new DecryptingAciResponseInputStreamImpl(serverDetails, response)
                            : new AciResponseInputStreamImpl(response);
                });

            } else {
                Validate.notNull(httpClient, "You must set the HttpClient instance to use before using this class.");
                final org.apache.http.client.methods.HttpUriRequest request =
                        constructHttpRequest(serverDetails, parameters);

                return httpClient.execute(request, response -> {
                    final int statusCode = response.getStatusLine().getStatusCode();
                    LOGGER.debug("Executed method and got status code - {}...", statusCode);

                    // Treat anything other than a 2xx status code as an error...
                    if ((statusCode < 200) || (statusCode >= 300)) {
                        // close the connection so it can be reused
                        org.apache.http.util.EntityUtils.consume(response.getEntity());

                        throw new org.apache.http.client.ClientProtocolException(
                                "The server returned a status code, " + statusCode +
                                        ", that wasn't in the 2xx Success range.");
                    }

                    // Decorate the InputStream so we can release the HTTP connection once the stream's been read...
                    return decryptResponse(serverDetails.getEncryptionCodec(), response)
                            ? new DecryptingAciResponseInputStreamImpl(serverDetails, response)
                            : new AciResponseInputStreamImpl(response);
                });

            }
        } catch (final ClientProtocolException | org.apache.http.client.ClientProtocolException cpe) {
            throw new AciHttpException("A HTTP protocol Exception has been caught while trying to execute the ACI request.", cpe);
        } catch (final EncryptionCodecException ece) {
            throw new AciHttpException("Unable to send the ACI request due to an encryption failure.", ece);
        } catch (final URISyntaxException urise) {
            throw new AciHttpException("Unable to construct the URI required to send the ACI request.", urise);
        }
    }

    private boolean decryptResponse(final EncryptionCodec encryptionCodec, final HttpResponse response) {
        LOGGER.trace("decryptResponse() called...");

        // If there is no encryptionCodec then we don't need to check the headers...
        boolean decryptResponse = (encryptionCodec != null);

        LOGGER.debug("Using an EncryptionCodec - {}...", decryptResponse);

        if (decryptResponse) {
            LOGGER.debug("Checking AUTN-Content-Type response header...");

            // This response header is only supplied with encrypted responses, so if it's not there we don't decrypt,
            // i.e. it's either an OEM IDOL, or they did encryptResponse=false...
            final Header header = response.getFirstHeader("AUTN-Content-Type");
            if (header == null) {
                LOGGER.debug("No AUTN-Content-Type response header, so don't auto decrypt response...");
                decryptResponse = false;
            }
        }

        // Send back the flag...
        return decryptResponse;
    }

    @Deprecated
    private boolean decryptResponse(final EncryptionCodec encryptionCodec, final org.apache.http.HttpResponse response) {
        LOGGER.trace("decryptResponse() called...");

        // If there is no encryptionCodec then we don't need to check the headers...
        boolean decryptResponse = (encryptionCodec != null);

        LOGGER.debug("Using an EncryptionCodec - {}...", decryptResponse);

        if (decryptResponse) {
            LOGGER.debug("Checking AUTN-Content-Type response header...");

            // This response header is only supplied with encrypted responses, so if it's not there we don't decrypt,
            // i.e. it's either an OEM IDOL, or they did encryptResponse=false...
            final org.apache.http.Header header = response.getFirstHeader("AUTN-Content-Type");
            if (header == null) {
                LOGGER.debug("No AUTN-Content-Type response header, so don't auto decrypt response...");
                decryptResponse = false;
            }
        }

        // Send back the flag...
        return decryptResponse;
    }

    /**
     * Getter for property httpClient.
     * @return Value of property httpClient
     */
    @Deprecated
    public org.apache.http.client.HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Setter for property httpClient.
     * @param httpClient New value of property httpClient
     */
    @Deprecated
    public void setHttpClient(final org.apache.http.client.HttpClient httpClient) {
        httpClient5 = null;
        this.httpClient = httpClient;
    }

    /**
     * Getter for property usePostMethod.
     * @return Value of property usePostMethod
     */
    public boolean isUsePostMethod() {
        return this.usePostMethod;
    }

    /**
     * Setter for property usePostMethod.
     * @param usePostMethod New value of property usePostMethod
     */
    public void setUsePostMethod(final boolean usePostMethod) {
        this.usePostMethod = usePostMethod;
    }

}
