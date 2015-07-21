/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.util;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

/**
 * JUnit test class for <tt>com.autonomy.aci.client.util.AciURLCodec</tt> class.
 */
public class AciURLCodecTest {

    private static final int[] SWISS_GERMAN_STUFF_UNICODE = {
            0x47, 0x72, 0xFC, 0x65, 0x7A, 0x69, 0x5F, 0x7A, 0xE4, 0x6D, 0xE4
    };

    private static final int[] RUSSIAN_STUFF_UNICODE = {
            0x412, 0x441, 0x435, 0x43C, 0x5F, 0x43F, 0x440, 0x438, 0x432, 0x435, 0x442
    };

    private String constructString(final int[] unicodeChars) {
        final StringBuilder buffer = new StringBuilder();
        if (unicodeChars != null) {
            for (final int unicodeChar : unicodeChars) {
                buffer.append((char) unicodeChar);
            }
        }
        return buffer.toString();
    }

    @Test
    public void testNullEmptyAndBlankInput() {
        // Get the codec...
        final AciURLCodec codec = AciURLCodec.getInstance();

        // Test null...
        assertThat("Null encode", codec.encode(null), is(nullValue()));
        assertThat("Null decode", codec.decode(null), is(nullValue()));

        // Test empty...
        assertThat("Empty encode", codec.encode(""), is(""));
        assertThat("Empty decode", codec.decode(""), is(""));

        // Test blank...
        assertThat("Blank encode", codec.encode("   "), is("%20%20%20"));
        assertThat("Blank decode", codec.decode("   "), is("   "));
    }

    @Test
    public void testUTF8RoundTrip() {
        // Get the codec...
        final AciURLCodec codec = AciURLCodec.getInstance();

        final String ru_msg = constructString(RUSSIAN_STUFF_UNICODE);
        final String ch_msg = constructString(SWISS_GERMAN_STUFF_UNICODE);

        assertEquals("%D0%92%D1%81%D0%B5%D0%BC_%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82", codec.encode(ru_msg));
        assertEquals("Gr%C3%BCezi_z%C3%A4m%C3%A4", codec.encode(ch_msg));

        assertEquals(ru_msg, codec.decode(codec.encode(ru_msg)));
        assertEquals(ch_msg, codec.decode(codec.encode(ch_msg)));
    }

    @Test
    public void testBasicEncodeDecode() throws Exception {
        final AciURLCodec codec = AciURLCodec.getInstance();

        final String plain = "Hello there!";
        final String encoded = codec.encode(plain);

        assertEquals("Basic URL encoding test", "Hello%20there%21", encoded);
        assertEquals("Basic URL decoding test", plain, codec.decode(encoded));
    }

    @Test
    public void testSafeCharEncodeDecode() throws Exception {
        final AciURLCodec codec = AciURLCodec.getInstance();

        final String plain = "abc123_-.*";
        final String encoded = codec.encode(plain);

        assertEquals("Safe chars URL encoding test", plain, encoded);
        assertEquals("Safe chars URL decoding test", plain, codec.decode(encoded));
    }

    @Test
    public void testUnsafeEncodeDecode() throws Exception {
        final AciURLCodec codec = AciURLCodec.getInstance();

        final String plain = "~!@#$%^&()+{}\"\\;:`,/[]";
        final String encoded = codec.encode(plain);

        assertEquals("Unsafe chars URL encoding test", "%7E%21%40%23%24%25%5E%26%28%29%2B%7B%7D%22%5C%3B%3A%60%2C%2F%5B%5D", encoded);
        assertEquals("Unsafe chars URL decoding test", plain, codec.decode(encoded));
    }

    @Test
    public void testEncodeDecodeNull() throws Exception {
        final AciURLCodec codec = AciURLCodec.getInstance();

        assertNull("Null string URL encoding test", codec.encode(null));
        assertNull("Null string URL decoding test", codec.decode(null));
    }

    @Test
    public void testDecodeInvalid() throws Exception {
        // Create the codec...
        final AciURLCodec codec = AciURLCodec.getInstance();

        try {
            codec.decode("%");
            fail("AciURLCodecException should have been thrown");
        } catch (final AciURLCodecException aurlce) {
            // Expected...
        }

        try {
            codec.decode("%A");
            fail("AciURLCodecException should have been thrown");
        } catch (final AciURLCodecException aurlce) {
            // Expected...
        }

        try {
            codec.decode("%WW");
            fail("AciURLCodecException should have been thrown");
        } catch (final AciURLCodecException aurlce) {
            // Expected...
        }
    }

}
