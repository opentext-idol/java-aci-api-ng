/*
 * Copyright 2006-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport.gss;

import com.autonomy.aci.client.transport.EncryptionCodecException;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.net.URLCodec;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class GssEncryptionCodecTest {

    @Test
    public void testConstructorAndPropertyAcessors() {
        final GSSContext gssContext = spy(GSSContext.class);
        final GssEncryptionCodec codec = new GssEncryptionCodec(gssContext);

        assertThat(codec.getContext(), is(sameInstance(gssContext)));

        codec.setContext(null);
        assertThat(codec.getContext(), is(nullValue()));
    }

    @Test(expected = EncryptionCodecException.class)
    public void testDecodeInternalDecoderException() throws UnsupportedEncodingException, EncryptionCodecException {
        final byte[] encoded = new URLCodec().encode("This is a $tring that n33ds €% ", CharEncoding.UTF_8).getBytes(CharEncoding.UTF_8);

        // Partially copy the encoded byte array, so it should fail on decoding...
        final byte[] bad = new byte[encoded.length - 2];
        System.arraycopy(encoded, 0, bad, 0, (encoded.length - 2));

        new GssEncryptionCodec(spy(GSSContext.class)).decodeInternal(bad);
        fail("Should've thrown a DecoderException...");
    }


    @Test
    public void testEncryptInternal() throws GSSException, EncryptionCodecException {
        final GSSContext gssContext = when(spy(GSSContext.class).wrap((byte[]) any(), anyInt(), anyInt(), any(MessageProp.class))).thenReturn("This is a test result...".getBytes()).getMock();
        final GssEncryptionCodec codec = new GssEncryptionCodec(gssContext);

        final byte[] result = codec.encryptInternal("This is a test...".getBytes());
        assertThat(new String(result), is(equalTo("This is a test result...")));

        verify(gssContext).wrap((byte[]) any(), anyInt(), anyInt(), any(MessageProp.class));
        verifyNoMoreInteractions(gssContext);
    }

    @Test(expected = EncryptionCodecException.class)
    @SuppressWarnings("unchecked")
    public void testEncryptInternalException() throws GSSException, EncryptionCodecException {
        final GssEncryptionCodec codec = new GssEncryptionCodec(
                when(spy(GSSContext.class).wrap((byte[]) any(), anyInt(), anyInt(), any(MessageProp.class))).thenThrow(GSSException.class).<GSSContext>getMock()
        );

        codec.encryptInternal("This is a test...".getBytes());
        fail("Should've thrown a GSSException...");
    }

    @Test
    public void testDecryptInternal() throws GSSException, EncryptionCodecException {
        final GSSContext gssContext = when(spy(GSSContext.class).unwrap((byte[]) any(), anyInt(), anyInt(), any(MessageProp.class))).thenReturn("This is a test...".getBytes()).getMock();
        final GssEncryptionCodec codec = new GssEncryptionCodec(gssContext);

        final byte[] result = codec.decryptInternal("This is a test result...".getBytes());
        assertThat(new String(result), is(equalTo("This is a test...")));

        verify(gssContext).unwrap((byte[]) any(), anyInt(), anyInt(), any(MessageProp.class));
        verifyNoMoreInteractions(gssContext);
    }

    @Test(expected = EncryptionCodecException.class)
    @SuppressWarnings("unchecked")
    public void testDecryptInternalException() throws GSSException, EncryptionCodecException {
        final GssEncryptionCodec codec = new GssEncryptionCodec(
                when(spy(GSSContext.class).unwrap((byte[]) any(), anyInt(), anyInt(), any(MessageProp.class))).thenThrow(GSSException.class).<GSSContext>getMock()
        );

        codec.decryptInternal("This is a test result...".getBytes());
        fail("Should've thrown a GSSException...");
    }

}
