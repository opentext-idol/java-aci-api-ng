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

import com.autonomy.aci.client.transport.AciServerDetails;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * This class has to be used when compression is enabled while communicating with an ACI server. The use of the default
 * HttpClient class {@link org.apache.http.client.protocol.ResponseContentEncoding} doesn't work as the ACI server and
 * that class have their <tt>gzip</tt> <tt>deflate</tt> handlers transposed and thus aren't compatible.
 */
@Deprecated
class DeflateContentEncoding extends ResponseContentEncoding {

    private InputStream getTranslatedInputStream(final HttpEntity entity) throws IOException {
        final InputStream content = entity.getContent();

        final byte[] peeked = new byte[6];

        final PushbackInputStream pushback = new PushbackInputStream(content, peeked.length);

        final int headerLength = pushback.read(peeked);

        if (headerLength == -1) {
            throw new IOException("Unable to read the response (header length)");
        }

        final byte[] dummy = new byte[1];
        final Inflater inf = new Inflater();

        try {
            int n;
            while ((n = inf.inflate(dummy)) == 0) {
                if (inf.finished()) {
                    throw new IOException("");
                }

                if (inf.needsDictionary()) {
                    break;
                }

                if (inf.needsInput()) {
                    inf.setInput(peeked);
                }
            }

            if (n == -1) {
                throw new IOException("Unable to read the response (insufficient bytes)");
            }

            pushback.unread(peeked, 0, headerLength);

            return new InflaterInputStream(pushback);
        } catch (final DataFormatException e) {
            pushback.unread(peeked, 0, headerLength);
            return new InflaterInputStream(pushback, new Inflater(false));
        }
    }

    @Override
    public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        final HttpEntity entity = response.getEntity();
        if (entity == null) {
            return; // no work to do
        }

        final Header ceheader = entity.getContentEncoding();
        if (ceheader != null) {
            final HeaderElement[] codecs = ceheader.getElements();
            if (codecs.length == 0) {
                return;
            }

            final HeaderElement codec = codecs[0];
            if ("gzip".equalsIgnoreCase(codec.getName())) {
                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
            } else if ("deflate".equalsIgnoreCase(codec.getName())) {
                final InputStreamEntity isEntity = new InputStreamEntity(getTranslatedInputStream(response.getEntity()), -1);
                isEntity.setContentType(response.getEntity().getContentType());
                response.setEntity(isEntity);
            } else if ("identity".equalsIgnoreCase(codec.getName())) {
                /* Don't need to transform the content - no-op */
            } else {
                throw new HttpException("Unsupported Content-Coding: " + codec.getName());
            }
        }
    }

}
