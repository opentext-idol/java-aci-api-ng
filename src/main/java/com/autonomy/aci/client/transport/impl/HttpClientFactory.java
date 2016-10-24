/*
 * Copyright 2006-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.impl;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility class to help create and configure an instance of Apache {@link HttpClient} as it appears almost
 * impossible to do so via an IoC container like Spring. This factory assumes that you'll be running in a multi-threaded
 * environment so creates a {@link DefaultHttpClient} with an instance of {@link PoolingClientConnectionManager} and
 * sets the necessary configuration parameters to the values that have been set via the setter methods.
 * <p/>
 * Defaults for all the properties that can be configured are:
 * <table border="0" style="margin-left: 2em;">
 * <tr><td><tt>maxTotalConnections</tt></td><td>&#160;</td><td>20</td></tr>
 * <tr><td><tt>maxConnectionsPerRoute</tt></tt></td><td>&#160;</td><td>4</tr>
 * <tr><td><tt>connectionTimeout</tt></tt></td><td>&#160;</td><td>7000</tr>
 * <tr><td><tt>linger</tt></tt></td><td>&#160;</td><td>-1</tr>
 * <tr><td><tt>socketBufferSize</tt></tt></td><td>&#160;</td><td>8192</tr>
 * <tr><td><tt>soKeepAlive</tt></tt></td><td>&#160;</td><td>true</tr>
 * <tr><td><tt>soReuseAddr</tt></tt></td><td>&#160;</td><td>true</tr>
 * <tr><td><tt>soTimeout</tt></tt></td><td>&#160;</td><td>10000</tr>
 * <tr><td><tt>staleCheckingEnabled</tt></tt></td><td>&#160;</td><td>false</tr>
 * <tr><td><tt>tcpNoDelay</tt></tt></td><td>&#160;</td><td>true</tr>
 * <tr><td><tt>useCompression</tt></tt></td><td>&#160;</td><td>true</tr>
 * </table>
 * <p/>
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
public class HttpClientFactory {

    public static final Logger LOGGER = LoggerFactory.getLogger(HttpClientFactory.class);

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
     * Creates an instance of <tt>DefaultHttpClient</tt> with a <tt>ThreadSafeClientConnManager</tt>.
     * @return an implementation of the <tt>HttpClient</tt> interface.
     */
    public HttpClient createInstance() {
        LOGGER.debug("Creating a new instance of DefaultHttpClient with configuration -> {}", toString());

        // Create the connection manager which will be default create the necessary schema registry stuff...
        final PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        // Set the HTTP connection parameters (These are in the HttpCore JavaDocs, NOT the HttpClient ones)...
        final HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setLinger(params, linger);
        HttpConnectionParams.setSocketBufferSize(params, socketBufferSize);
        HttpConnectionParams.setSoKeepalive(params, soKeepAlive);
        HttpConnectionParams.setSoReuseaddr(params, soReuseAddr);
        HttpConnectionParams.setSoTimeout(params, soTimeout);
        HttpConnectionParams.setStaleCheckingEnabled(params, staleCheckingEnabled);
        HttpConnectionParams.setTcpNoDelay(params, tcpNoDelay);

        // Create the HttpClient and configure the compression interceptors if required...
        final DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, params);

        if (useCompression) {
            httpClient.addRequestInterceptor(new RequestAcceptEncoding());
            httpClient.addResponseInterceptor(new DeflateContentEncoding());
        }

        return httpClient;
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
     * applies to individual manager instances. The default is <tt>10</tt>.
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
     * applies to individual manager instances. The default is <tt>4</tt>.
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
     * as an infinite timeout. The default is <tt>7000</tt>ms (7s).
     * @param connectionTimeout The timeout in milliseconds to set
     */
    public void setConnectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getLinger() {
        return linger;
    }

    /**
     * Sets <tt>SO_LINGER</tt> with the specified linger time in seconds. The maximum timeout value is platform
     * specific. Value <tt>0</tt> implies that the option is disabled. Value <tt>-1</tt> implies that the JRE default
     * is used. The setting only affects the socket close operation. The default is <tt>-1</tt>.
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
     * messages. The default is <tt>8192</tt> (8 * 1024).
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
     * Defines the socket timeout (<tt>SO_TIMEOUT</tt>) in milliseconds, which is the timeout for waiting for data or,
     * put differently, a maximum period inactivity between two consecutive data packets). A timeout value of zero is
     * interpreted as an infinite timeout. The default is <tt>10000</tt> (10s).
     * @param soTimeout The socket timeout (<tt>SO_TIMEOUT</tt>) in milliseconds to set
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
     * this check should be disabled. The default is <tt>false</tt>.
     * @param staleCheckingEnabled <tt>true</tt> if stale connection checking is to be used, <tt>false</tt> otherwise
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
     * performance, they can disable Nagle's algorithm (that is enable <tt>TCP_NODELAY</tt>). Data will be sent earlier,
     * at the cost of an increase in bandwidth consumption. The default is <tt>true</tt>.
     * @param tcpNoDelay <tt>false</tt> to use Nagle's algorithm, <tt>true</tt> to enable <tt>TCP_NODELAY</tt>
     */
    public void setTcpNoDelay(final boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isUseCompression() {
        return useCompression;
    }

    /**
     * Configures the {@link org.apache.http.client.HttpClient} to send the <tt>Accept-Encoding: gzip,deflate</tt>
     * header and thus handle compressed responses from the ACI server.
     * @param useCompression <tt>true</tt> to set the <tt>Accept-Encoding</tt> header, <tt>false</tt> to not set it
     * @since 4.1
     */
    public void setUseCompression(final boolean useCompression) {
        this.useCompression = useCompression;
    }

}
