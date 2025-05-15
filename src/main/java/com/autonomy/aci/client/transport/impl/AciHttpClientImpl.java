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
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.net.WWWFormCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the {@link com.autonomy.aci.client.transport.AciHttpClient} interface that provides the actual HTTP
 * communication mechanism. This implementation uses the HttpClient provided by the <a href="http://hc.apache.org/">
 * Apache HttpComponents</a> project. It defaults to using the HTTP <code>GET</code> method, if you wish to send ACI actions
 * with the HTTP <code>POST</code> method, then call the {@link #setUsePostMethod(boolean)} method with {@code true}.
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

        final boolean hasPostParameter = parameters.stream()
                .anyMatch(ActionParameter::requiresPostRequest);
        final boolean encrypt = serverDetails.getEncryptionCodec() != null;

        if (hasPostParameter) {
            // OEM multipart is done by encrypting the querystring with at least 1 param (in this case the action),
            // and not encrypting the body
            final List<ActionParameter<?>> orderedParams = orderParams(parameters);
            final List<ActionParameter<?>> urlParams = orderedParams.subList(0, 1);
            final Collection<? extends ActionParameter<?>> encryptedUrlParams = encrypt ?
                    createEncryptedParameters(serverDetails, urlParams) : urlParams;
            final List<ActionParameter<?>> bodyParams = orderedParams.stream().skip(1).collect(Collectors.toList());
            return createMultipart5Method(serverDetails, encryptedUrlParams, bodyParams);
        } else {
            final Set<? extends ActionParameter<?>> encryptedParams = encrypt ?
                    createEncryptedParameters(serverDetails, parameters) : parameters;
            if (usePostMethod) {
                return createPost5Method(serverDetails, encryptedParams);
            } else {
                return createGet5Method(serverDetails, encryptedParams);
            }
        }
    }

    @Deprecated
    private org.apache.http.client.methods.HttpUriRequest constructHttpRequest(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws EncryptionCodecException, URISyntaxException, UnsupportedEncodingException {
        LOGGER.trace("constructHttpMethod() called...");

        final boolean hasPostParameter = parameters.stream()
                .anyMatch(ActionParameter::requiresPostRequest);
        final boolean encrypt = serverDetails.getEncryptionCodec() != null;

        if (hasPostParameter) {
            final List<ActionParameter<?>> orderedParams = orderParams(parameters);
            final List<ActionParameter<?>> urlParams = orderedParams.subList(0, 1);
            final Collection<? extends ActionParameter<?>> encryptedUrlParams = encrypt ?
                    createEncryptedParameters(serverDetails, urlParams) : urlParams;
            final List<ActionParameter<?>> bodyParams = orderedParams.stream().skip(1).collect(Collectors.toList());
            return createMultipartMethod(serverDetails, encryptedUrlParams, bodyParams);
        } else {
            final Set<? extends ActionParameter<?>> encryptedParams = encrypt ?
                    createEncryptedParameters(serverDetails, parameters) : parameters;
            if (usePostMethod) {
                return createPostMethod(serverDetails, encryptedParams);
            } else {
                return createGetMethod(serverDetails, encryptedParams);
            }
        }
    }

    /**
     * Takes the passed in set of parameters and encrypts them.
     * @param serverDetails The details of the ACI server the request will be sent to
     * @param parameters    The parameters to send with the ACI action.
     * @return A set of encrypted parameters
     * @throws EncryptionCodecException if something went wrong encrypting the parameters
     */
    private Set<? extends ActionParameter<?>> createEncryptedParameters(final AciServerDetails serverDetails, final Collection<? extends ActionParameter<?>> parameters) throws EncryptionCodecException {
        LOGGER.trace("createEncryptedParameters() called...");

        // Generate the query String and put it through the codec...
        final String data = EncryptionCodecUtils.getInstance().encrypt(
                serverDetails.getEncryptionCodec(),
                wwwFormEncodeParams(orderParams(parameters), serverDetails.getCharsetName()),
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
     * @throws URISyntaxException If there was a problem construction the request URI from the <code>serverDetails</code>
     *                            and <code>parameters</code>
     */
    private HttpUriRequest createGet5Method(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws URISyntaxException {
        LOGGER.trace("createGetMethod() called...");

        // Create the URI to use...
        final URI uri = new URIBuilder()
                .setScheme(serverDetails.getProtocol().toString().toLowerCase(Locale.ENGLISH))
                .setHost(serverDetails.getHost())
                .setPort(serverDetails.getPort())
                .setPath(serverDetails.getPath())
                .setParameters(paramsToNVP(orderParams(parameters)))
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
                .setPath(serverDetails.getPath())
                .setParameters(paramsToNVP(orderParams(parameters)))
                .build();

        // Return the constructed get method...
        return new org.apache.http.client.methods.HttpGet(uri);
    }

    /**
     * Create multipart POST request.
     *
     * @param serverDetails
     * @param urlParams Parameters to send in the URL querystring
     * @param bodyParams Parameters to send in multipart body
     * @return Built request
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private HttpUriRequest createMultipart5Method(
            final AciServerDetails serverDetails,
            final Collection<? extends ActionParameter<?>> urlParams,
            final Collection<? extends ActionParameter<?>> bodyParams
    ) throws URISyntaxException, UnsupportedEncodingException {
        LOGGER.trace("createMultipartMethod() called...");

        final URI uri = new URIBuilder()
                .setScheme(serverDetails.getProtocol().toString().toLowerCase(Locale.ENGLISH))
                .setHost(serverDetails.getHost())
                .setPort(serverDetails.getPort())
                .setPath(serverDetails.getPath())
                .setParameters(paramsToNVP(urlParams))
                .build();

        final HttpPost method = new HttpPost(uri);

        final Charset charset = Charset.forName(serverDetails.getCharsetName());
        final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setCharset(charset);
        bodyParams.forEach(parameter -> parameter.addToEntity(multipartEntityBuilder, charset));
        method.setEntity(multipartEntityBuilder.build());

        return method;
    }

    private org.apache.http.client.methods.HttpUriRequest createMultipartMethod(
            final AciServerDetails serverDetails,
            final Collection<? extends ActionParameter<?>> urlParams,
            final Collection<? extends ActionParameter<?>> bodyParams
    ) throws URISyntaxException, UnsupportedEncodingException {
        LOGGER.trace("createMultipartMethod() called...");

        final URI uri = new URIBuilder()
                .setScheme(serverDetails.getProtocol().toString().toLowerCase(Locale.ENGLISH))
                .setHost(serverDetails.getHost())
                .setPort(serverDetails.getPort())
                .setPath(serverDetails.getPath())
                .setParameters(paramsToNVP(urlParams))
                .build();

        final org.apache.http.client.methods.HttpPost method = new org.apache.http.client.methods.HttpPost(uri);

        final Charset charset = Charset.forName(serverDetails.getCharsetName());
        final org.apache.http.entity.mime.MultipartEntityBuilder multipartEntityBuilder = org.apache.http.entity.mime.MultipartEntityBuilder.create();
        multipartEntityBuilder.setCharset(charset);
        bodyParams.forEach(parameter -> parameter.addToEntity(multipartEntityBuilder, charset));
        method.setEntity(multipartEntityBuilder.build());

        return method;
    }

    /**
     * Create form-urlencoded POST request.
     *
     * @param serverDetails
     * @param parameters Parameters to send in form-urlencoded body
     * @return Built request
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private HttpUriRequest createPost5Method(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws URISyntaxException, UnsupportedEncodingException {
        LOGGER.trace("createPostMethod() called...");

        final URI uri = new URIBuilder()
                .setScheme(serverDetails.getProtocol().toString().toLowerCase(Locale.ENGLISH))
                .setHost(serverDetails.getHost())
                .setPort(serverDetails.getPort())
                .setPath(serverDetails.getPath())
                .build();

        final HttpPost method = new HttpPost(uri);

        method.setEntity(new StringEntity(
                    wwwFormEncodeParams(orderParams(parameters), serverDetails.getCharsetName()),
                    ContentType.TEXT_PLAIN));

        return method;
    }

    private org.apache.http.client.methods.HttpUriRequest createPostMethod(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws URISyntaxException, UnsupportedEncodingException {
        LOGGER.trace("createPostMethod() called...");

        final URI uri = new URIBuilder()
                .setScheme(serverDetails.getProtocol().toString().toLowerCase(Locale.ENGLISH))
                .setHost(serverDetails.getHost())
                .setPort(serverDetails.getPort())
                .setPath(serverDetails.getPath())
                .build();

        final org.apache.http.client.methods.HttpPost method = new org.apache.http.client.methods.HttpPost(uri);

        method.setEntity(new org.apache.http.entity.StringEntity(
                wwwFormEncodeParams(orderParams(parameters), serverDetails.getCharsetName()),
                org.apache.http.entity.ContentType.TEXT_PLAIN));

        return method;
    }

    /**
     * @param parameters ACI request parameters
     * @return Parameters ordered so that the action comes first
     * @throws IllegalArgumentException When there is no action
     */
    private List<ActionParameter<?>> orderParams(final Collection<? extends ActionParameter<?>> parameters) {
        LOGGER.trace("orderParams() called...");

        // Just incase, remove the allowed null entry...
        parameters.remove(null);

        final List<ActionParameter<?>> ordered = new ArrayList<>(parameters.size());

        ActionParameter<?> actionParam = null;

        for (final ActionParameter<?> parameter : parameters) {
            if (AciConstants.PARAM_ACTION.equalsIgnoreCase(parameter.getName())) {
                actionParam = parameter;
            } else {
                ordered.add(parameter);
            }
        }

        // Ensure that the action=XXX parameter is the first thing in the list...
        Validate.isTrue(actionParam != null, "No 'action' parameter found in parameter set, please set one before trying to execute an ACI request.");
        ordered.add(0, actionParam);

        return ordered;
    }

    /**
     * Converts a sequence of string parameters into a a sequence of {@code NameValuePair} objects suitable for use
     * in both POST and GET requests.  Order is preserved.
     *
     * @param parameters
     * @return Pairs
     */
    private List<NameValuePair> paramsToNVP(final Collection<? extends ActionParameter<?>> parameters) {
        LOGGER.trace("paramsToNameValuePair() called...");
        return parameters.stream().flatMap(parameter -> {
            final Object value = parameter.getValue();
            if (value instanceof String) {
                return Stream.of(new BasicNameValuePair(parameter.getName(), (String) value));
            } else {
                return Stream.of();
            }
        }).collect(Collectors.toList());
    }

    /**
     * Encodes a sequence of parameters to form-urlencoded request body, preserving order.
     *
     * @param parameters
     * @param charsetName
     * @return Request body
     */
    private String wwwFormEncodeParams(final List<? extends ActionParameter<?>> parameters, final String charsetName) {
        return WWWFormCodec.format(paramsToNVP(parameters), Charset.forName(charsetName));
    }

    /**
     * Execute an ACI action on the specific ACI server.
     * @param serverDetails Details of the ACI server to send the action to
     * @param parameters    The parameters to send with the ACI action
     * @return An <code>AciResponseInputStream</code> containing the ACI response
     * @throws IOException              If an I/O (transport) error occurs. Some transport exceptions can be recovered from
     * @throws AciHttpException         If a protocol exception occurs. Usually protocol exceptions cannot be recovered from
     * @throws IllegalArgumentException if the <code>httpClient</code> property is <code>null</code> or <code>parameters</code> is <code>null</code>
     */
    @Override
    public AciResponseInputStream executeAction(final AciServerDetails serverDetails, final Set<? extends ActionParameter<?>> parameters) throws IOException, AciHttpException {
        LOGGER.trace("executeAction() called...");

        Validate.notEmpty(parameters, "The parameter set must not be null or empty.");

        LOGGER.debug("Executing action on {}:{}/{}...", serverDetails.getHost(), serverDetails.getPort(), serverDetails.getPath());
        try {
            if (httpClient5 != null) {

                final HttpUriRequest request = constructHttp5Request(serverDetails, parameters);
                final ClassicHttpResponse response = httpClient5.executeOpen(null, request, null);
                final int statusCode = response.getCode();
                LOGGER.debug("Executed method and got status code - {}...", statusCode);

                // Treat anything other than a 2xx status code as an error...
                if ((statusCode < 200) || (statusCode >= 300)) {
                    // close the connection so it can be reused
                    EntityUtils.consume(response.getEntity());

                    throw new AciHttpException(
                            "The server returned a status code, " + statusCode +
                                    ", that wasn't in the 2xx Success range.");
                }

                // Decorate the InputStream so we can release the HTTP connection once the stream's been read...
                return decryptResponse(serverDetails.getEncryptionCodec(), response)
                        ? new DecryptingAciResponseInputStreamImpl(serverDetails, response)
                        : new AciResponseInputStreamImpl(response);

            } else {
                Validate.notNull(httpClient, "You must set the HttpClient instance to use before using this class.");
                final org.apache.http.client.methods.HttpUriRequest request =
                        constructHttpRequest(serverDetails, parameters);

                final org.apache.http.HttpResponse response = httpClient.execute(request);
                final int statusCode = response.getStatusLine().getStatusCode();
                LOGGER.debug("Executed method and got status code - {}...", statusCode);

                // Treat anything other than a 2xx status code as an error...
                if ((statusCode < 200) || (statusCode >= 300)) {
                    // close the connection so it can be reused
                    org.apache.http.util.EntityUtils.consume(response.getEntity());

                    throw new AciHttpException(
                            "The server returned a status code, " + statusCode +
                                    ", that wasn't in the 2xx Success range.");
                }

                // Decorate the InputStream so we can release the HTTP connection once the stream's been read...
                return decryptResponse(serverDetails.getEncryptionCodec(), response)
                        ? new DecryptingAciResponseInputStreamImpl(serverDetails, response)
                        : new AciResponseInputStreamImpl(response);

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
