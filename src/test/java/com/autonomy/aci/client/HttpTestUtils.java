package com.autonomy.aci.client;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpTestUtils {

    public static HttpMocks mockHttpClient(final int statusCode, final HttpEntity responseEntity) throws IOException {
        final ClassicHttpResponse mockHttpResponse = mock(ClassicHttpResponse.class);
        when(mockHttpResponse.getCode()).thenReturn(statusCode);
        when(mockHttpResponse.getEntity()).thenReturn(responseEntity);

        final HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
                .thenAnswer(inv -> {
                    try {
                        return inv.getArgument(1, HttpClientResponseHandler.class).handleResponse(mockHttpResponse);
                    } catch (final HttpException e) {
                        throw new ClientProtocolException(e);
                    }
                });

        return new HttpMocks(mockHttpClient, mockHttpResponse, responseEntity);
    }

    public static HttpMocks mockHttpClient(final int statusCode, final String responseEntityResourcePath)
            throws IOException
    {
        final HttpEntity mockHttpEntity = mock(HttpEntity.class);
        when(mockHttpEntity.getContent())
                .thenReturn(HttpTestUtils.class.getResourceAsStream(responseEntityResourcePath));
        return mockHttpClient(statusCode, mockHttpEntity);
    }

    public static class HttpMocks {
        HttpClient client;
        ClassicHttpResponse response;
        HttpEntity entity;

        public HttpMocks(
                final HttpClient client,
                final ClassicHttpResponse response,
                final HttpEntity entity
        ) {
            this.client = client;
            this.response = response;
            this.entity = entity;
        }

        public HttpClient client() { return client; }
        public ClassicHttpResponse response() { return response; }
        public HttpEntity entity() { return entity; }

    }

}
