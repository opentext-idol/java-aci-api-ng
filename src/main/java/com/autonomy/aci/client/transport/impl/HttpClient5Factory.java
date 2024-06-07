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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility class to help create and configure an instance of Apache {@link HttpClient} as it appears almost
 * impossible to do so via an IoC container like Spring. This factory assumes that you'll be running in a multi-threaded
 * environment so creates a {@link DefaultHttpClient} with an instance of {@link PoolingClientConnectionManager} and
 * sets the necessary configuration parameters to the values that have been set via the setter methods.
 * <p>
 * Defaults for all the properties that can be configured are:
 * <table border="0" style="margin-left: 2em;" summary="">
 * <tr><td><code>maxTotalConnections</code></td><td>&#160;</td><td>20</td></tr>
 * <tr><td><code>maxConnectionsPerRoute</code></td><td>&#160;</td><td>4</tr>
 * <tr><td><code>connectionTimeout</code></td><td>&#160;</td><td>7000</tr>
 * <tr><td><code>linger</code></td><td>&#160;</td><td>-1</tr>
 * <tr><td><code>socketBufferSize</code></td><td>&#160;</td><td>8192</tr>
 * <tr><td><code>soKeepAlive</code></td><td>&#160;</td><td>true</tr>
 * <tr><td><code>soReuseAddr</code></td><td>&#160;</td><td>true</tr>
 * <tr><td><code>soTimeout</code></td><td>&#160;</td><td>10000</tr>
 * <tr><td><code>staleCheckingEnabled</code></td><td>&#160;</td><td>false</tr>
 * <tr><td><code>tcpNoDelay</code></td><td>&#160;</td><td>true</tr>
 * <tr><td><code>useCompression</code></td><td>&#160;</td><td>true</tr>
 * </table>
 * <p>
 * If you are using something like Spring's IoC container, you can use this class to configure your
 * {@link com.autonomy.aci.client.services.impl.AciServiceImpl} like so:
 * <pre>
 *   &lt;bean id="httpClientFactory" class="com.autonomy.aci.client.transport.impl.HttpClientFactory"
 *       p:maxConnectionsPerRoute="20"
 *       p:maxTotalConnections="120" /&gt;
 *
 *   &lt;!-- Defining this as a stand-alone bean allows it to be shared with the Non ACI API... --&gt;
 *   &lt;bean id="httpClient" factory-bean="httpClientFactory" factory-method="createInstance" /&gt;
 *
 *   &lt;bean id="aciService" class="com.autonomy.aci.client.services.impl.AciServiceImpl"&gt;
 *     &lt;constructor-arg&gt;
 *       &lt;bean class="com.autonomy.aci.client.transport.impl.AciHttpClientImpl"&gt;
 *         &lt;constructor-arg ref="httpClient" /&gt;
 *       &lt;/bean&gt;
 *     &lt;/constructor-arg&gt;
 *   &lt;/bean&gt;
 * </pre>
 */
public class HttpClient5Factory {

    public static final Logger LOGGER = LoggerFactory.getLogger(HttpClient5Factory.class);

    private int maxTotalConnections = 20;

    private int maxConnectionsPerRoute = 4;

    private int connectionTimeout = 7000;

    private int linger = -1;

    private int socketBufferSize = 8192;

    private boolean soKeepAlive = true;

    private boolean soReuseAddr = true;

    private int soTimeout = 10000;

    private boolean staleCheckingEnabled;

    private boolean tcpNoDelay = true;

    private boolean useCompression = true;

    /**
     * Creates an instance of <code>DefaultHttpClient</code> with a <code>ThreadSafeClientConnManager</code>.
     * @return an implementation of the <code>HttpClient</code> interface.
     */
    public HttpClient createInstance() {
        LOGGER.debug("Creating a new instance of DefaultHttpClient with configuration -> {}", toString());

        // Create the connection manager which will be default create the necessary schema registry stuff...
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectionTimeout))
                .build());

        connectionManager.setDefaultSocketConfig(SocketConfig.custom()
                .setSoLinger(TimeValue.ofSeconds(linger))
                .setSndBufSize(socketBufferSize).setRcvBufSize(socketBufferSize)
                .setSoKeepAlive(soKeepAlive)
                .setSoReuseAddress(soReuseAddr)
                .setSoTimeout(Timeout.ofMilliseconds(soTimeout))
                .setTcpNoDelay(tcpNoDelay)
                .build());

        // Create the HttpClient and configure the compression interceptors if required...
        final HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager);
        if (staleCheckingEnabled) {
            httpClientBuilder.evictExpiredConnections();
        }
        if (!useCompression) {
            httpClientBuilder.disableContentCompression();
        }

        return httpClientBuilder.build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("maxTotalConnections", maxTotalConnections)
                .append("maxConnectionsPerRoute", maxConnectionsPerRoute)
                .append("connectionTimeout", connectionTimeout)
                .append("linger", linger)
                .append("socketBufferSize", socketBufferSize)
                .append("soKeepAlive", soKeepAlive)
                .append("soReuseAddr", soReuseAddr)
                .append("soTimeout", soTimeout)
                .append("staleCheckingEnabled", staleCheckingEnabled)
                .append("tcpNoDelay", tcpNoDelay)
                .append("useCompression", useCompression)
                .toString();
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    /**
     * Defines the maximum number of connections in total. This limit is interpreted by client connection managers and
     * applies to individual manager instances. The default is <code>10</code>.
     * @param maxTotalConnections The maximum number of connections to set
     */
    public void setMaxTotalConnections(final int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    /**
     * Defines the maximum number of connections per route. This limit is interpreted by client connection managers and
     * applies to individual manager instances. The default is <code>4</code>.
     * @param maxConnectionsPerRoute The maximum number of connections per route to set
     */
    public void setMaxConnectionsPerRoute(final int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Determines the timeout in milliseconds until a connection is established. A timeout value of zero is interpreted
     * as an infinite timeout. The default is <code>7000</code>ms (7s).
     * @param connectionTimeout The timeout in milliseconds to set
     */
    public void setConnectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getLinger() {
        return linger;
    }

    /**
     * Sets <code>SO_LINGER</code> with the specified linger time in seconds. The maximum timeout value is platform
     * specific. Value <code>0</code> implies that the option is disabled. Value <code>-1</code> implies that the JRE default
     * is used. The setting only affects the socket close operation. The default is <code>-1</code>.
     * @param linger The specified linger time in seconds to set
     */
    public void setLinger(final int linger) {
        this.linger = linger;
    }

    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    /**
     * Determines the size of the internal socket buffer used to buffer data while receiving / transmitting HTTP
     * messages. The default is <code>8192</code> (8 * 1024).
     * @param socketBufferSize The size of the internal socket buffer to use
     */
    public void setSocketBufferSize(final int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
    }

    public boolean isSoKeepAlive() {
        return soKeepAlive;
    }

    /**
     * Defines whether or not TCP is to send automatically a keepalive probe to the peer after an interval of inactivity
     * (no data exchanged in either direction) between this host and the peer. The purpose of this option is to detect
     * if the peer host crashes.
     * @param soKeepAlive {@code true} to set {@code SO_KEEPALIVE}, {@code false} otherwise
     * @since 4.1.2
     */
    public void setSoKeepAlive(final boolean soKeepAlive) {
        this.soKeepAlive = soKeepAlive;
    }

    public boolean isSoReuseAddr() {
        return soReuseAddr;
    }

    /**
     * Defines whether the socket can be bound even though a previous connection is still in a timeout state.
     * @param soReuseAddr {@code true} to set {@code SO_REUSEADDR}, {@code false} otherwise.
     * @since 4.1.2
     */
    public void setSoReuseAddr(final boolean soReuseAddr) {
        this.soReuseAddr = soReuseAddr;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    /**
     * Defines the socket timeout (<code>SO_TIMEOUT</code>) in milliseconds, which is the timeout for waiting for data or,
     * put differently, a maximum period inactivity between two consecutive data packets). A timeout value of zero is
     * interpreted as an infinite timeout. The default is <code>10000</code> (10s).
     * @param soTimeout The socket timeout (<code>SO_TIMEOUT</code>) in milliseconds to set
     */
    public void setSoTimeout(final int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public boolean isStaleCheckingEnabled() {
        return staleCheckingEnabled;
    }

    /**
     * Determines whether stale connection check is to be used. The stale connection check can cause up to 30
     * millisecond overhead per request and should be used only when appropriate. For performance critical operations
     * this check should be disabled. The default is <code>false</code>.
     * @param staleCheckingEnabled <code>true</code> if stale connection checking is to be used, <code>false</code> otherwise
     */
    public void setStaleCheckingEnabled(final boolean staleCheckingEnabled) {
        this.staleCheckingEnabled = staleCheckingEnabled;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Determines whether Nagle's algorithm is to be used. The Nagle's algorithm tries to conserve bandwidth by
     * minimizing the number of segments that are sent. When applications wish to decrease network latency and increase
     * performance, they can disable Nagle's algorithm (that is enable <code>TCP_NODELAY</code>). Data will be sent earlier,
     * at the cost of an increase in bandwidth consumption. The default is <code>true</code>.
     * @param tcpNoDelay <code>false</code> to use Nagle's algorithm, <code>true</code> to enable <code>TCP_NODELAY</code>
     */
    public void setTcpNoDelay(final boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isUseCompression() {
        return useCompression;
    }

    /**
     * Configures the {@link org.apache.http.client.HttpClient} to send the <code>Accept-Encoding: gzip,deflate</code>
     * header and thus handle compressed responses from the ACI server.
     * @param useCompression <code>true</code> to set the <code>Accept-Encoding</code> header, <code>false</code> to not set it
     * @since 4.1
     */
    public void setUseCompression(final boolean useCompression) {
        this.useCompression = useCompression;
    }

}
