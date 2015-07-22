/*
 * Copyright 2006-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.impl;

import com.autonomy.aci.client.transport.EncryptionCodecException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * JUnit test class for <tt>com.autonomy.aci.client.util.transport.AbstractEncryptionCodec</tt> class.
 */
public class AbstractEncryptionCodecTest extends AbstractEncryptionCodec {

    private static final long serialVersionUID = 5112056667065156616L;

    private final String string = "This is a test string...";

    private static byte[] deflatedWithPrefix;

    private static String encodedWithPrefix;

    @BeforeClass
    public static void createObjects() throws UnsupportedEncodingException {
        deflatedWithPrefix = new byte[]{65, 85, 84, 78, 58, 120, -100, 11, -55, -56, 44, 86, 0, -94, 68, -123, -110,
                -44, -30, 18, -123, -30, -110, -94, -52, -68, 116, 61, 61, 61, 0, 105, 100, 8, 55};

        // Has to be this and not generated on the fly, as extra stuff is added to the front of deflatedWithPrefix
        // before it is encoded...
        encodedWithPrefix = "MzV8QVVUTjp4nAvJyCxWAKJEhZLU4hKF4pKizLx0PT09AGlkCDc=";
    }

    @Test
    public void testDeflate() throws EncryptionCodecException, UnsupportedEncodingException {
        // Deflate and check...
        assertThat("Incorrect deflation", deflateInternal(string.getBytes("UTF-8")), is(equalTo(deflatedWithPrefix)));
    }

    @Test
    public void testEncode() throws EncryptionCodecException, UnsupportedEncodingException {
        // Encode and check...
        assertThat("Incorrect encoding", encodedWithPrefix, is(equalTo(new String(encodeInternal(deflatedWithPrefix), "UTF-8"))));
    }

    @Test
    public void testDecode() throws EncryptionCodecException, UnsupportedEncodingException {
        // Decode and check...
//        assertThat("Incorrect decoding", deflatedWithPrefix, is(equalTo(decodeInternal(encodedWithPrefix.getBytes("UTF-8")))));

        // Hamcrest 1.2 has borked asserting byte[]'s via is(equalTo(...))...
        final byte[] decoded = decodeInternal(encodedWithPrefix.getBytes("UTF-8"));
        for (int ii = 0; ii < decoded.length; ii++) {
            assertThat("Incorrect deflation", decoded[ii], is(deflatedWithPrefix[ii]));
        }
    }

    @Test(expected = EncryptionCodecException.class)
    public void testNoLengthAndSeperator() throws UnsupportedEncodingException, EncryptionCodecException {
        // Base64 encode a string with out the length and seperator on the front...
        final byte[] encoded = Base64.encodeBase64("This is a random string that means nothing...".getBytes("UTF-8"));

        // Decode...
        decodeInternal(encoded);
        fail("Should have raised an EncryptionCodecException");
    }

    @Test
    public void testInflate() throws EncryptionCodecException, UnsupportedEncodingException {
        // Strip the AUTN: bit off the front of the deflated byte array...
        assertThat("Incorrect inflating", new String(inflateInternal(deflatedWithPrefix), "UTF-8"), is(equalTo(string)));
    }

    @Test
    public void testEncrypt() throws EncryptionCodecException, UnsupportedEncodingException {
        // Encrypt and check...
        assertThat("Incorrect encryption", encodedWithPrefix, is(equalTo(new String(encrypt(string.getBytes("UTF-8")), "UTF-8"))));
    }

    @Test
    public void testEncryptNullOrEmpty() throws EncryptionCodecException, UnsupportedEncodingException {
        try {
            // Encrypt and check...
            encrypt(null);
            fail("Should have thrown an IllegalArgumentException...");
        } catch (final IllegalArgumentException iae) {
            // Expected...
        }

        try {
            // Encrypt and check...
            encrypt(new byte[0]);
            fail("Should have thrown an IllegalArgumentException...");
        } catch (final IllegalArgumentException iae) {
            // Expected...
        }
    }

    @Test
    public void testDecrypt() throws EncryptionCodecException, UnsupportedEncodingException {
        // Decrypt and check...
        assertThat("Incorrect decryption", string, is(equalTo(new String(decrypt(encodedWithPrefix.getBytes("UTF-8")), "UTF-8"))));
    }

    @Test
    public void testDecryptNullOrEmpty() throws EncryptionCodecException, UnsupportedEncodingException {
        try {
            // Decrypt and check...
            decrypt(null);
            fail("Should have thrown an IllegalArgumentException...");
        } catch (final IllegalArgumentException iae) {
            // Expected...
        }

        try {
            // Decrypt and check...
            decrypt(new byte[0]);
            fail("Should have thrown an IllegalArgumentException...");
        } catch (final IllegalArgumentException iae) {
            // Expected...
        }
    }


    @Override
    public byte[] encryptInternal(final byte[] bytes) throws EncryptionCodecException {
        return bytes;
    }

    @Override
    public byte[] decryptInternal(final byte[] bytes) throws EncryptionCodecException {
        return bytes;
    }

}
