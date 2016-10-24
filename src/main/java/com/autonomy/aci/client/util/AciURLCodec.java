/*
 * Copyright 2006-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 * Implements the 'www-form-urlencoded' encoding scheme, also misleadingly known as URL encoding.
 * <p/>
 * For more detailed information please refer to
 * <a href="http://www.w3.org/TR/html4/interact/forms.html#h-17.13.4.1">
 * Chapter 17.13.4 'Form content types'</a> of the
 * <a href="http://www.w3.org/TR/html4/">HTML 4.01 Specification<a>
 * <p/>
 * This codec differs from the spec as it encodes spaces as <tt>%20</tt> and not as <tt>+</tt>. This is required by
 * various IDOL Server components to stop references with spaces in them being treated as multiple references, as the
 * space character is a valid ACI parameter value separator.
 */
public final class AciURLCodec {

    /**
     * Thread safe singleton instance of our class.
     */
    private static final AciURLCodec INSTANCE = new AciURLCodec();

    /**
     * Seven-bit ASCII, also known as ISO646-US, also known as the Basic Latin block of the Unicode character set.
     */
    private static final String US_ASCII = "US-ASCII";

    /**
     * Eight-bit Unicode Transformation Format.
     */
    private static final String UTF8 = "UTF-8";

    /**
     * BitSet of www-form-url safe characters, missing ' ' as this should be convert to %20.
     */
    private final BitSet safeChars = new BitSet(256);

    /**
     * Creates a new instance of AciURLCodec.
     */
    private AciURLCodec() {
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++) {
            safeChars.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            safeChars.set(i);
        }

        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            safeChars.set(i);
        }

        // special chars
        safeChars.set('-');
        safeChars.set('_');
        safeChars.set('.');
        safeChars.set('*');
    }

    /**
     * Returns the thread safe singleton instance of this class.
     * @return The thread safe singleton instance of this class
     */
    public static AciURLCodec getInstance() {
        return INSTANCE;
    }

    /**
     * Encodes a string into its URL safe form using the UTF-8 charset. Unsafe characters are escaped and the resulting
     * string is returned in the US-ASCII charset.
     * @param string The string to convert to a URL safe form
     * @return URL safe string
     * @throws AciURLCodecException If there was a problem during the encoding
     */
    public String encode(final String string) {
        // This is what we'll return...
        String returnValue = null;

        try {
            if (string != null) {
                returnValue = new String(URLCodec.encodeUrl(safeChars, string.getBytes(UTF8)), US_ASCII);
            }
        } catch (final UnsupportedEncodingException uee) {
            // This should never ever happen as both charsets are required by the Java Spec.
            throw new AciURLCodecException(uee);
        }

        // Return the result...
        return returnValue;
    }

    /**
     * Decodes a URL safe string into its original form using the UTF-8 charset. Escaped characters are converted back
     * to their original representation.
     * @param string URL safe string to convert into its original form
     * @return original string
     * @throws AciURLCodecException Thrown if URL decoding is unsuccessful
     */
    public String decode(final String string) throws AciURLCodecException {
        // This is what we'll return...
        String returnValue = null;

        try {
            if (string != null) {
                returnValue = new String(URLCodec.decodeUrl(string.getBytes(US_ASCII)), UTF8);
            }
        } catch (final UnsupportedEncodingException uee) {
            // This should never ever happen as both charsets are required by the Java Spec.
            throw new AciURLCodecException(uee);
        } catch (final DecoderException de) {
            throw new AciURLCodecException(de);
        }

        // Return the result...
        return returnValue;
    }

}
