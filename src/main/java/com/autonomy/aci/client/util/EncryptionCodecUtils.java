/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.util;

import com.autonomy.aci.client.transport.EncryptionCodec;
import com.autonomy.aci.client.transport.EncryptionCodecException;

import java.io.UnsupportedEncodingException;

/**
 * This is a utility class that encrypts and decrypts <tt>String</tt> objects, it main function is to hide the required
 * conversions between strings and byte arrays.
 */
public class EncryptionCodecUtils {

    // Thread safe singleton instance of our class.
    private static final EncryptionCodecUtils INSTANCE = new EncryptionCodecUtils();

    /**
     * Returns the thread safe singleton instance of this class.
     * @return The thread safe singleton instance of this class
     */
    public static EncryptionCodecUtils getInstance() {
        return INSTANCE;
    }

    /**
     * Encrypt the given string with the supplied codec and use the supplied charset name for any string/byte
     * conversions.
     * @param codec       The <tt>EncryptionCodec</tt> to use
     * @param string      The input to encrypt
     * @param charsetName The name of charset to use for string and byte array conversion
     * @return The encrypted input
     * @throws EncryptionCodecException if something went wrong during either the conversion of encrypting
     */
    public String encrypt(final EncryptionCodec codec, final String string, final String charsetName) throws EncryptionCodecException {
        final byte[] bytes = toBytes(string, charsetName);
        final byte[] encrypted = codec.encrypt(bytes);
        return toString(encrypted, charsetName);
    }

    /**
     * Decrypt the given string with the supplied codec and use the supplied charset name for any string/byte conversions.
     * @param codec       The <tt>EncryptionCodec</tt> to use
     * @param string      The input to decrypt
     * @param charsetName The name of charset to use for string and byte array conversion
     * @return The decrypted input
     * @throws EncryptionCodecException if something went wrong during either the conversion of decrypting.
     */
    public String decrypt(final EncryptionCodec codec, final String string, final String charsetName) throws EncryptionCodecException {
        final byte[] bytes = toBytes(string, charsetName);
        final byte[] decrypted = codec.decrypt(bytes);
        return toString(decrypted, charsetName);
    }

    /**
     * Constructs a new <tt>String</tt> by decoding the specified array of bytes using the specified charset. The length
     * of the new <tt>String</tt> is a function of the charset, and hence may not be equal to the length of the byte
     * array.
     * <p>
     * The behavior of this method when the given bytes are not valid in the given charset is unspecified. The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control over the decoding process is required.
     * @param bytes       the bytes to be decoded into characters
     * @param charsetName the name of a supported {@link java.nio.charset.Charset </code>charset<code>}
     * @return A string representation of the <tt>bytes</tt> argument
     * @throws EncryptionCodecException If the named charset is not supported
     * @throws NullPointerException     If either <tt>bytes</tt> or <tt>charsetName</tt> is <tt>null</tt>
     */
    public String toString(final byte[] bytes, final String charsetName) throws EncryptionCodecException {
        try {
            return new String(bytes, charsetName);
        } catch (final UnsupportedEncodingException uee) {
            throw new EncryptionCodecException("Unable to convert the byte array into a String.", uee);
        }
    }

    /**
     * Encodes the <tt>string</tt> into a sequence of bytes using the named charset, storing the result into a new byte
     * array.
     * <p>
     * The behavior of this method when this string cannot be encoded in the given charset is unspecified. The {@link
     * java.nio.charset.CharsetEncoder} class should be used when more control over the encoding process is required.
     * @param string      The <tt>String</tt> to convert to a byte array.
     * @param charsetName The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
     * @return The resultant byte array
     * @throws EncryptionCodecException If the named charset is not supported
     * @throws NullPointerException     if either <tt>string</tt> or <tt>charsetName</tt> is <tt>null</tt>
     */
    public byte[] toBytes(final String string, final String charsetName) throws EncryptionCodecException {
        try {
            return string.getBytes(charsetName);
        } catch (final UnsupportedEncodingException uee) {
            throw new EncryptionCodecException("Unable to convert the String into a byte array.", uee);
        }
    }

}
