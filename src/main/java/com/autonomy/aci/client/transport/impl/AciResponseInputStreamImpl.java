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

import com.autonomy.aci.client.transport.AciResponseInputStream;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This {@code AciResponseInputStream} implementation provides the ability to release a {@code HttpClient} connection
 * when the stream has been read, even if the stream is being read by code that doesn't know where it's come from. It
 * accomplishes this by using a {@code FilterInputStream} to pass all method calls to the ACI response {@code
 * InputStream} and overrides the {@code close()} method, so that it can release the HTTP connection at the same time.
 * <p>
 * This class is required to decorate the returned {@code InputStream} if the {@code HttpClient} has been setup to use
 * the {@code MultiThreadedHttpConnectionManager}.
 */
public class AciResponseInputStreamImpl extends AciResponseInputStream {

    /**
     * Class logger...
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AciResponseInputStreamImpl.class);

    private ClassicHttpResponse response5;

    /**
     * Holds value of property method.
     */
    private HttpResponse response;

    /**
     * Creates a new instance of AciResponseInputStreamImpl.
     * @param response An {@code HttpResponse} that contains the ACI response as an {@code InputStream}
     * @throws IOException If an I/O error occurs
     */
    public AciResponseInputStreamImpl(final ClassicHttpResponse response) throws IOException {
        // Give the filter the InputStream to use...
        super(response.getEntity().getContent());

        // Store the method...
        response5 = response;
    }

    /**
     * Creates a new instance of AciResponseInputStreamImpl.
     * @param response An {@code HttpResponse} that contains the ACI response as an {@code InputStream}
     * @throws IOException If an I/O error occurs
     * @deprecated Use {@link #AciResponseInputStreamImpl(ClassicHttpResponse)}
     */
    @Deprecated
    public AciResponseInputStreamImpl(final HttpResponse response) throws IOException {
        // Give the filter the InputStream to use...
        super(response.getEntity().getContent());

        // Store the method...
        this.response = response;
        response5 = null;
    }

    /**
     * Return the status code of the HTTP request. This is useful if you are, for example, proxying View server requests,
     * as by default it can send 404 and 500 error codes if it can't find, or can't render documents.
     * @return Returns the response status code.
     */
    @Override
    public int getStatusCode() {
        return response5 != null ? response5.getCode() : response.getStatusLine().getStatusCode();
    }

    private String getHeaderValue(final Header header) {
        return header == null ? null : header.getValue();
    }

    private String getHeaderValue(final org.apache.http.Header header) {
        return header == null ? null : header.getValue();
    }

    @Override
    public String getHeader(final String name) {
        return response5 != null ?
                getHeaderValue(response5.getFirstHeader(name)) :
                getHeaderValue(response.getFirstHeader(name));
    }

    @Override
    public String getContentEncoding() {
        return response5 != null ?
                response5.getEntity().getContentEncoding() :
                getHeaderValue(response.getEntity().getContentEncoding());
    }

    @Override
    public long getContentLength() {
        return response5 != null ?
                response5.getEntity().getContentLength() :
                response.getEntity().getContentLength();
    }

    /**
     * Return the content type of the response. Most likely to be <code>text/xml</code>, but could be <code>image/jpeg</code> if
     * the response if from a <code>ClusterServe2DMap</code>, for example.
     * @return The content type of the response, if no Content-Type header was found, then <code>null</code> is returned
     */
    @Override
    public String getContentType() {
        return response5 != null ?
                response5.getEntity().getContentType() :
                getHeaderValue(response.getEntity().getContentType());
    }

    @Override
    public void close() throws IOException {
        LOGGER.trace("close() called...");

        try {
            // Close the actual InputStream...
            super.close();
        } finally {
            LOGGER.debug("Releasing the HTTP Connection...");
            if (response5 != null) {
                EntityUtils.consume(response5.getEntity());
            } else {
                org.apache.http.util.EntityUtils.consume(response.getEntity());
            }
        }
    }

    /**
     * Getter for property method.
     * @return Value of property method
     */
    @Deprecated
    public HttpResponse getMethod() {
        return response;
    }

    /**
     * Setter for property method.
     * @param response New value of property response
     */
    @Deprecated
    public void setMethod(final HttpResponse response) {
        response5 = null;
        this.response = response;
    }

}
