/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.gss;

import com.autonomy.aci.client.transport.EncryptionCodecException;
import com.autonomy.aci.client.transport.impl.AbstractEncryptionCodec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This <tt>EncryptionCodec</tt> uses the GSS-API to do the encryption/decryption via the <tt>wrap</tt> and
 * <tt>unwrap</tt> methods. While this class is <tt>Serializable</tt> the <tt>GSSContext</tt> that is used, is marked
 * as transient, so upon deserialization, this codec will not be usable and a new context must be set.
 */
class GssEncryptionCodec extends AbstractEncryptionCodec {

    private static final long serialVersionUID = 492697591639093560L;

    private static final Logger LOGGER = LoggerFactory.getLogger(GssEncryptionCodec.class);

    private transient GSSContext context;

    public GssEncryptionCodec(final GSSContext context) {
        this.context = context;
    }

    /**
     * This method firstly www-form-urlencoded unescapes the input string, as the response from a GSS-API secured ACI
     * server www-form-urlencoded escapes the Base64 encoded content. Once that's done it passes the resulting string
     * onto the super class for the Base64 decoding and length prefix stripping.
     * @param bytes The Base64 encoded byte array to decode.
     * @return The decoded byte array.
     * @throws EncryptionCodecException If an error occurred during processing
     */
    @Override
    protected byte[] decodeInternal(final byte[] bytes) throws EncryptionCodecException {
        try {
            // We're not using AciURLCodec as it works on Strings, it uses URLCodec internally anyway, so it's always 
            // going to be shipped, thus we may as well use it's byte[] methods...
            return super.decodeInternal(URLCodec.decodeUrl(bytes));
        } catch (final DecoderException de) {
            throw new EncryptionCodecException("Unable to www-form-urlencoded unescape.", de);
        }
    }

    @Override
    protected byte[] encryptInternal(final byte[] bytes) throws EncryptionCodecException {
        LOGGER.trace("encryptInternal() called...");

        try {
            LOGGER.debug("Encrypting content with context.wrap()...");
            return context.wrap(bytes, 0, bytes.length, new MessageProp(0, true));
        } catch (final GSSException gsse) {
            throw new EncryptionCodecException("Unable to encrypt the outgoing ACI data.", gsse);
        }
    }

    @Override
    protected byte[] decryptInternal(final byte[] bytes) throws EncryptionCodecException {
        LOGGER.trace("decryptInternal() called...");

        try {
            LOGGER.debug("Decrypting content with context.unwrap()...");
            return context.unwrap(bytes, 0, bytes.length, new MessageProp(0, true));
        } catch (final GSSException gsse) {
            throw new EncryptionCodecException("Unable to decrypt the incoming ACI data.", gsse);
        }
    }

    public GSSContext getContext() {
        return context;
    }

    public void setContext(final GSSContext context) {
        this.context = context;
    }

}
