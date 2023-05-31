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

import com.autonomy.aci.client.transport.EncryptionCodec;
import com.autonomy.aci.client.transport.EncryptionCodecException;
import com.autonomy.aci.client.util.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Abstract base class that contains everything an <tt>EncryptionCodec</tt> needs to do apart from the actual encrypt
 * and decrypt routines, which are cipher specific.
 */
public abstract class AbstractEncryptionCodec implements EncryptionCodec {

    private static final long serialVersionUID = -261634652396305630L;

    /**
     * Class logger...
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEncryptionCodec.class);

    /**
     * This method should firstly deflate {@code bytes}, then encrypt it and finally Base64 encode it.
     * @param bytes An array of bytes to encrypt
     * @return A Base64 encoded, encrypted, deflated copy of the input array
     * @throws EncryptionCodecException if there was a problem during any of the three stages of processing
     */
    public byte[] encrypt(final byte[] bytes) throws EncryptionCodecException {
        LOGGER.trace("encrypt() called...");

        if (ArrayUtils.isEmpty(bytes)) {
            throw new IllegalArgumentException("The byte array to encrypt must not be null or empty.");
        }

        // Do all the work...
        return encodeInternal(encryptInternal(deflateInternal(bytes)));
    }

    /**
     * This method should firstly Base64 decode {@code bytes}, then decrypt it and finally inflate it.
     * @param bytes A Base64 encoded, encrypted and deflated array of bytes
     * @return The original unencrypted content as a byte array
     * @throws EncryptionCodecException if there was a problem during any of the three stages of processing
     */
    public byte[] decrypt(final byte[] bytes) throws EncryptionCodecException {
        LOGGER.trace("decrypt() called...");

        if (ArrayUtils.isEmpty(bytes)) {
            throw new IllegalArgumentException("The byte array to decrypt must not be null or empty.");
        }

        // Do all the work...
        return inflateInternal(decryptInternal(decodeInternal(bytes)));
    }

    /**
     * Deflates the passed in <tt>String</tt> and prefixes the result with <tt>AUTN:</tt> before returning.
     * @param bytes The byte array to deflate
     * @return The deflated string prefixed with <tt>AUTN:</tt> as a byte array
     * @throws EncryptionCodecException If an error occurred during processing
     */
    protected byte[] deflateInternal(final byte[] bytes) throws EncryptionCodecException {
        LOGGER.trace("deflateInternal() called...");

        // This is what will deflate for us...
        DeflaterOutputStream deflater = null;

        try {
            // Create the output container...
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Create the deflater...
            deflater = new DeflaterOutputStream(baos);

            LOGGER.debug("Deflating content...");

            // Deflate the input string...
            deflater.write(bytes);
            deflater.finish();

            // Get the deflated bytes...
            final byte[] deflated = baos.toByteArray();

            LOGGER.debug("Adding prefix to deflated content...");

            // Get The deflated array prefix of AUTN: in bytes...
            final byte[] prefix = "AUTN:".getBytes("UTF-8");

            // Copy both the prefix and the deflated query string into a new array...
            final byte[] toEncrypt = new byte[prefix.length + deflated.length];
            System.arraycopy(prefix, 0, toEncrypt, 0, prefix.length);
            System.arraycopy(deflated, 0, toEncrypt, prefix.length, deflated.length);

            LOGGER.debug("Returning deflated and prefixed string...");

            // Return the deflated query string...
            return toEncrypt;
        } catch (final IOException ioe) {
            throw new EncryptionCodecException("Unable to deflate the input.", ioe);
        } finally {
            IOUtils.getInstance().closeQuietly(deflater);
        }
    }

    /**
     * Encrypt the given byte array.
     * @param bytes The <tt>byte[]</tt> to encrypt
     * @return The encrypted byte array
     * @throws EncryptionCodecException If an error occurred during processing
     */
    protected abstract byte[] encryptInternal(byte[] bytes) throws EncryptionCodecException;

    /**
     * Base64 encodes the supplied byte array, firstly prefixing the length of the byte array and a separator character.
     * @param encrypted The encrypted byte array to Base64 encode
     * @return The Base64 encoded byte array
     * @throws EncryptionCodecException If an error occurred during processing
     */
    protected byte[] encodeInternal(final byte[] encrypted) throws EncryptionCodecException {
        LOGGER.trace("encodeInternal() called...");

        try {
            LOGGER.debug("Prefixing length of encrypted section...");

            // This is the prefix to the encrypted byte array...
            final byte[] prefix = (String.valueOf(encrypted.length) + '|').getBytes("UTF-8");

            // Create a byte array to hold all the bits...
            final byte[] toEncode = new byte[prefix.length + encrypted.length];

            // Append the two arrays...
            System.arraycopy(prefix, 0, toEncode, 0, prefix.length);
            System.arraycopy(encrypted, 0, toEncode, prefix.length, encrypted.length);

            LOGGER.debug("Base64 encoding the deflated and encrypted input...");

            // Base64 encode the array...
            final byte[] encoded = Base64.encodeBase64(toEncode);

            LOGGER.debug("Returning deflated, encrypted and encoded input...");

            // Return the encoded byte array as a String...
            return encoded;
        } catch (final UnsupportedEncodingException uee) {
            throw new EncryptionCodecException("Unable to encode the input.", uee);
        }
    }

    /**
     * Base64 decodes the supplied string and strips the encrypted length and separator, for example <tt>.52|</tt>, from
     * the front of the byte array.
     * @param bytes The Base64 encoded byte array to decode
     * @return The decoded byte array
     * @throws EncryptionCodecException If an error occurred during processing
     */
    protected byte[] decodeInternal(final byte[] bytes) throws EncryptionCodecException {
        LOGGER.trace("decodeInternal() called...");
        LOGGER.debug("Decoding Base64 encoded input...");

        // Decode the input string in preparation for stripping encrypted length and separator...
        final byte[] decoded = Base64.decodeBase64(bytes);

        // We now need to strip off the length of the encrypted section and it's separator...
        final int index = ArrayUtils.indexOf(decoded, (byte) 0x7C);

        if (index == -1) {
            throw new EncryptionCodecException("Incorrect decoded input, no length and separator found.");
        }

        // This is the length of the encrypted portion...
        final int encryptedLength = decoded.length - (index + 1);

        // This will hold the encrypted bytes, minus the length and separator...
        final byte[] stripped = new byte[encryptedLength];

        LOGGER.debug("Stripping length and separator from decoded input...");

        // Strip the length and separator...
        System.arraycopy(decoded, (index + 1), stripped, 0, encryptedLength);

        LOGGER.debug("Returning encrypted portion of the input...");

        // Return the decoded content...
        return stripped;
    }

    /**
     * Decrypt the given byte array.
     * @param bytes the byte array to decrypt
     * @return The decrypted byte array
     * @throws EncryptionCodecException If an error occurred during processing
     */
    protected abstract byte[] decryptInternal(byte[] bytes) throws EncryptionCodecException;

    /**
     * Strip the <tt>AUTN:</tt> prefix and inflate the given <tt>byte[]</tt> to it's original form.
     * @param bytes The bytes to inflate
     * @return The inflated byte array
     * @throws EncryptionCodecException If an error occurred during processing
     */
    protected byte[] inflateInternal(final byte[] bytes) throws EncryptionCodecException {
        LOGGER.trace("inflateInternal() called...");

        // This is the input stream...
        InflaterInputStream inflater = null;

        try {
            LOGGER.debug("Stripping AUTN: prefix...");

            // This is the prefix in bytes...
            final byte[] prefix = "AUTN:".getBytes("UTF-8");

            // This will hold the content once it's has the prefix stripped... 
            final byte[] stripped = new byte[bytes.length - prefix.length];

            // Strip the prefix...
            System.arraycopy(bytes, prefix.length, stripped, 0, stripped.length);

            LOGGER.debug("Inflating decrypted input...");

            // Create the input stream...
            inflater = new InflaterInputStream(new ByteArrayInputStream(stripped));

            // Create the output buffer...
            final ByteArrayOutputStream inflated = new ByteArrayOutputStream();

            // Copy from one stream to the other...
            IOUtils.getInstance().copy(inflater, inflated);

            LOGGER.debug("Returning decoded, decrypted and inflated input...");

            // Convert to a string and return...
            return inflated.toByteArray();
        } catch (final IOException ioe) {
            throw new EncryptionCodecException("Unable to inflate decrypted content.", ioe);
        } finally {
            // Close the stream...
            IOUtils.getInstance().closeQuietly(inflater);
        }
    }

}
