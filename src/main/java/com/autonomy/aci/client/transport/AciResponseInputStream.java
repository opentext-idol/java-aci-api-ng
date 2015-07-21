/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * Provides a wrapper around the actual ACI response <tt>InputStream</tt> and propagates the content type of the HTTP
 * response. Also allows <tt>InputStream</tt> methods to be overridden, like {@link #close} so that HTTP resources can
 * be released when the stream is closed.
 */
public abstract class AciResponseInputStream extends FilterInputStream {

    /**
     * Creates a new instance of AciResponseInputStream.
     * @param inputStream the ACI response
     */
    public AciResponseInputStream(final InputStream inputStream) {
        // Give the filter the InputStream to use...
        super(inputStream);
    }

    /**
     * Return the status code of the HTTP request. This is useful if you are, for example, proxying View server requests,
     * as by default it can send 404 and 500 error codes if it can't find, or can't render documents.
     * @return Returns the response status code
     */
    public abstract int getStatusCode();

    public abstract String getHeader(String name);

    public abstract String getContentEncoding();

    public abstract long getContentLength();

    /**
     * Return the content type of the response. Most likely to be <tt>text/xml</tt>, but could be <tt>image/jpeg</tt>,
     * for example, if the response is from a <tt>ClusterServe2DMap</tt> action.
     * @return The content type of the response
     */
    public abstract String getContentType();

}
