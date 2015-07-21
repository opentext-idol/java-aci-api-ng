/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.impl;

import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class HttpClientFactoryTest {

    @Test
    public void testAccessorMethods() {
        final HttpClientFactory httpClientFactory = new HttpClientFactory();

        httpClientFactory.setMaxTotalConnections(71);
        assertThat(httpClientFactory.getMaxTotalConnections(), is(71));

        httpClientFactory.setMaxConnectionsPerRoute(42);
        assertThat(httpClientFactory.getMaxConnectionsPerRoute(), is(42));

        httpClientFactory.setConnectionTimeout(12345);
        assertThat(httpClientFactory.getConnectionTimeout(), is(12345));

        httpClientFactory.setLinger(19);
        assertThat(httpClientFactory.getLinger(), is(19));

        httpClientFactory.setSocketBufferSize(99);
        assertThat(httpClientFactory.getSocketBufferSize(), is(99));

        httpClientFactory.setSoKeepAlive(false);
        assertThat(httpClientFactory.isSoKeepAlive(), is(false));

        httpClientFactory.setSoReuseAddr(false);
        assertThat(httpClientFactory.isSoReuseAddr(), is(false));

        httpClientFactory.setSoTimeout(13579);
        assertThat(httpClientFactory.getSoTimeout(), is(13579));

        httpClientFactory.setStaleCheckingEnabled(true);
        assertThat(httpClientFactory.isStaleCheckingEnabled(), is(true));

        httpClientFactory.setTcpNoDelay(false);
        assertThat(httpClientFactory.isTcpNoDelay(), is(false));

        httpClientFactory.setUseCompression(false);
        assertThat(httpClientFactory.isUseCompression(), is(false));
    }

    @Test
    public void testCreateInstance() {
        final HttpClientFactory httpClientFactory = new HttpClientFactory();
        httpClientFactory.setMaxTotalConnections(71);
        httpClientFactory.setMaxConnectionsPerRoute(42);
        httpClientFactory.setConnectionTimeout(12345);
        httpClientFactory.setLinger(19);
        httpClientFactory.setSocketBufferSize(99);
        httpClientFactory.setSoKeepAlive(false);
        httpClientFactory.setSoReuseAddr(false);
        httpClientFactory.setSoTimeout(13579);
        httpClientFactory.setStaleCheckingEnabled(true);
        httpClientFactory.setTcpNoDelay(false);
        httpClientFactory.setUseCompression(false);

        DefaultHttpClient httpClient = (DefaultHttpClient) httpClientFactory.createInstance();
        final BasicHttpParams httpParams = (BasicHttpParams) httpClient.getParams();

        assertThat((Boolean) httpParams.getParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK), is(true));
        assertThat((Integer) httpParams.getParameter(CoreConnectionPNames.CONNECTION_TIMEOUT), is(12345));
        assertThat((Integer) httpParams.getParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE), is(99));
        assertThat((Boolean) httpParams.getParameter(CoreConnectionPNames.SO_KEEPALIVE), is(false));
        assertThat((Integer) httpParams.getParameter(CoreConnectionPNames.SO_LINGER), is(19));
        assertThat((Boolean) httpParams.getParameter(CoreConnectionPNames.SO_REUSEADDR), is(false));
        assertThat((Integer) httpParams.getParameter(CoreConnectionPNames.SO_TIMEOUT), is(13579));
        assertThat((Boolean) httpParams.getParameter(CoreConnectionPNames.TCP_NODELAY), is(false));

        final PoolingClientConnectionManager poolingClientConnectionManager = (PoolingClientConnectionManager) httpClient.getConnectionManager();
        assertThat(poolingClientConnectionManager.getDefaultMaxPerRoute(), is(42));
        assertThat(poolingClientConnectionManager.getMaxTotal(), is(71));

        assertThat(getRequestInterceptors(httpClient), not(hasItem("org.apache.http.client.protocol.RequestAcceptEncoding")));
        assertThat(getResponseInterceptors(httpClient), not(hasItem("com.autonomy.aci.client.transport.impl.DeflateContentEncoding")));

        httpClientFactory.setUseCompression(true);
        httpClient = (DefaultHttpClient) httpClientFactory.createInstance();

        assertThat(getRequestInterceptors(httpClient), hasItem("org.apache.http.client.protocol.RequestAcceptEncoding"));
        assertThat(getResponseInterceptors(httpClient), hasItem("com.autonomy.aci.client.transport.impl.DeflateContentEncoding"));
    }

    private Set<String> getRequestInterceptors(final AbstractHttpClient abstractHttpClient) {
        final LinkedHashSet<String> requestInterceptors = new LinkedHashSet<String>();
        for (int ii = 0; ii < abstractHttpClient.getRequestInterceptorCount(); ii++) {
            requestInterceptors.add(abstractHttpClient.getRequestInterceptor(ii).getClass().getName());
        }
        return requestInterceptors;
    }

    private Set<String> getResponseInterceptors(final AbstractHttpClient abstractHttpClient) {
        final LinkedHashSet<String> responseInterceptors = new LinkedHashSet<String>();
        for (int ii = 0; ii < abstractHttpClient.getResponseInterceptorCount(); ii++) {
            responseInterceptors.add(abstractHttpClient.getResponseInterceptor(ii).getClass().getName());
        }
        return responseInterceptors;
    }

}
