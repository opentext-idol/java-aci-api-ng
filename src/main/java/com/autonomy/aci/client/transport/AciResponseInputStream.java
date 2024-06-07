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

package com.autonomy.aci.client.transport;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * Provides a wrapper around the actual ACI response <code>InputStream</code> and propagates the content type of the HTTP
 * response. Also allows <code>InputStream</code> methods to be overridden, like {@link #close} so that HTTP resources can
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
     * Return the content type of the response. Most likely to be <code>text/xml</code>, but could be <code>image/jpeg</code>,
     * for example, if the response is from a <code>ClusterServe2DMap</code> action.
     * @return The content type of the response
     */
    public abstract String getContentType();

}
