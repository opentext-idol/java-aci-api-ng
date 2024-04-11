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

package com.autonomy.aci.client.transport.gss;

import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.ActionParameters;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class GssAciHttpClientImplTest {

    @Test
    public void testDefaultConstructor() {
        final GssAciHttpClientImpl gssAciHttpClient = new GssAciHttpClientImpl();

        assertThat("HttpClient 4 not null", gssAciHttpClient.getHttpClient(), is(nullValue()));
        assertThat("Use POST should be false", gssAciHttpClient.isUsePostMethod(), is(false));
    }

    @Test
    public void testHttpClientConstructor() {
        final HttpClient httpClient = HttpClients.createDefault();
        final GssAciHttpClientImpl gssAciHttpClient = new GssAciHttpClientImpl(httpClient);

        assertThat("HttpClient 4 not null", gssAciHttpClient.getHttpClient(), is(nullValue()));
        assertThat("Use POST should be false", gssAciHttpClient.isUsePostMethod(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteWrongClass() throws IOException, AciHttpException {
        new GssAciHttpClientImpl(HttpClients.createDefault()).executeAction(new AciServerDetails(), new ActionParameters());
        fail("Should've thrown an IllegalArgumentException...");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteNoServiceName() throws IOException, AciHttpException {
        new GssAciHttpClientImpl(HttpClients.createDefault()).executeAction(new GssAciServerDetails(), new ActionParameters());
    }

}
