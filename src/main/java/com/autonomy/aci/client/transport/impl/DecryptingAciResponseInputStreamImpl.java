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

import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * This <tt>AciResponseInputStream</tt> implementation decrypts ACI responses from actions that have been sent with the
 * {@code EncryptResponse} parameter set to {@code true}.
 */
public class DecryptingAciResponseInputStreamImpl extends AciResponseInputStreamImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecryptingAciResponseInputStreamImpl.class);

    /**
     * This is what we'll decrypt the input into...
     */
    private ByteArrayInputStream decryptedResponse;

    /**
     * Holds the content type of the encrypted data.
     */
    private String contentType;

    /**
     * Creates a new instance of DecryptingAciResponseInputStreamImpl.
     * @param serverDetails The <tt>AciServerDetails</tt> that contains the <tt>EncryptionCodec</tt> that is being used
     *                      to decrypt the ACI response and the character encoding being used on this ACI request/response
     * @param response      An {@code HttpResponse} that contains the ACI response as an {@code InputStream}
     * @throws IOException If an I/O error occurs
     */
    public DecryptingAciResponseInputStreamImpl(
            final AciServerDetails serverDetails,
            final ClassicHttpResponse response
    ) throws IOException {
        super(response);
        setup(serverDetails, () -> new AciResponseInputStreamImpl(response));
    }

    /**
     * Creates a new instance of DecryptingAciResponseInputStreamImpl.
     * @param serverDetails The <tt>AciServerDetails</tt> that contains the <tt>EncryptionCodec</tt> that is being used
     *                      to decrypt the ACI response and the character encoding being used on this ACI request/response
     * @param response      An {@code HttpResponse} that contains the ACI response as an {@code InputStream}
     * @throws IOException If an I/O error occurs
     * @deprecated Use {@link #DecryptingAciResponseInputStreamImpl(AciServerDetails, ClassicHttpResponse)}
     */
    @Deprecated
    public DecryptingAciResponseInputStreamImpl(
            final AciServerDetails serverDetails,
            final HttpResponse response
    ) throws IOException {
        super(response);
        setup(serverDetails, () -> new AciResponseInputStreamImpl(response));
    }

    private void setup(final AciServerDetails serverDetails, GetInputStream getStream) throws IOException {
        try {
            LOGGER.debug("Checking AUTN-Content-Type header...");

            // Get the autonomy content type header...
            final String autnContentType = getHeader("AUTN-Content-Type");

            // This shouldn't be necessary, but just in case someone is using this outwith the API...
            if (StringUtils.isBlank(autnContentType)) {
                LOGGER.debug("AUTN-Content-Type header doesn't exist, will return response unprocessed...");

                // Copy the response to the decryptedResponse property unmolested... 
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                IOUtils.getInstance().copy(getStream.get(), buffer);
                decryptedResponse = new ByteArrayInputStream(buffer.toByteArray());

                // Set the content type...
                contentType = super.getContentType();
            } else {
                LOGGER.debug("AUTN-Content-Type header is {}...", autnContentType);

                // Set the content type...
                contentType = autnContentType;

                // Get the response and decrypt it into the internal buffer... We have to create a new
                // AciResponseInputStreamImpl rather than giving this, otherwise the overridden methods we be called
                // resulting in bad things happening...
                decryptedResponse = "text/xml".equals(autnContentType)
                        ? new EncryptedTextContentProcessor(serverDetails.getEncryptionCodec(), serverDetails.getCharsetName()).process(in)
                        : new EncryptedBinaryContentProcessor(serverDetails.getEncryptionCodec(), serverDetails.getCharsetName()).process(in);
            }
        } catch (final ProcessorException pe) {
            LOGGER.error("ProcessorException caught while trying to decrypt the ACI response", pe);
            throw new IOException(pe.getMessage());
        }
    }

    /**
     * Return the content type of the response. Most likely to be <tt>text/xml</tt>, but could be <tt>image/jpeg</tt> if
     * the response if from a <tt>ClusterServe2DMap</tt> action, for example.
     * @return The content type of the response, if no Content-Type header was found, then <tt>null</tt> is returned
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public int read() throws IOException {
        return decryptedResponse.read();
    }

    @Override
    public int read(final byte[] bytes, final int off, final int len) throws IOException {
        return decryptedResponse.read(bytes, off, len);
    }

    @Override
    public long skip(final long num) throws IOException {
        return decryptedResponse.skip(num);
    }

    @Override
    public int available() throws IOException {
        return decryptedResponse.available();
    }

    @Override
    public synchronized void mark(final int readlimit) {
        decryptedResponse.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        decryptedResponse.reset();
    }

    @Override
    public boolean markSupported() {
        return decryptedResponse.markSupported();
    }

    private interface GetInputStream {
        InputStream get() throws IOException;
    }

}
