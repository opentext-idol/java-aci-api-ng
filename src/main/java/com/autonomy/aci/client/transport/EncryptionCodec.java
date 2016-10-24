/*
 * Copyright 2006-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.aci.client.transport;

import java.io.Serializable;

/**
 * IDOL Server supports a number of different methods for encrypting ACI actions between itself and the client. This
 * interface defines the methods that the {@code AciHttpClient} will call to encrypt and decrypt an ACI action.
 * <p/>
 * Classes that implement this interface should be aware of just how IDOL expects the incoming ACI action to be
 * formatted. If the user is sending a query, for example, the action parameters sent may look something like:
 * <pre>
 *   action=query&text=tour%20de%20france&maxresults=10&combine=simple&print=all
 * </pre>
 * When the {@code AciHttpClient} detects that an {@code EncryptionCodec} should be used, it creates a new set of ACI
 * parameters and uses the output of the codec as the value to one of those parameters.
 * <p/>
 * When using a {@code Cipher} to encrypt text, the result is a {@code byte[]} not a string. HTTP is a text based
 * protocol, so the {@code EncryptionCodec} needs to convert the {@code byte[]} to a {@code String} by Base64 encoding
 * it. Furthermore, as query strings can be quite long, even before encrypting. Thus the query string that is sent to
 * IDOL will look it:
 * <pre>
 *   action=encrypted&data=&lt;base64(encrypt(deflate(original query string)))&gt;
 * </pre>
 * When IDOL sends the response back, the {@code AciHttpClient} takes the contents of the
 * <tt>/autnresponse/responsedata/autn:encrypted</tt> element and applies the steps to encrypt in reverse, i.e. base64
 * decode, decrypt and inflate. This will then result in a {@code byte[]} that contains the actual ACI response to be
 * sent back via the {@code AciService}.
 * <p/>
 * The reason the codec works with byte arrays and not Strings, is due to IDOL Server having a number of actions that
 * return binary data, i.e. <tt>action=ClusterServe2DMap</tt>. There is a utility class {@link
 * com.autonomy.aci.client.util.EncryptionCodecUtils} that has methods to make it easier to encrypt/decrypt <tt>String</tt>
 * objects.
 */
public interface EncryptionCodec extends Serializable {

    /**
     * This method should firstly deflate {@code bytes}, then encrypt it and finally Base64 encode it.
     * @param bytes An array of bytes to encrypt.
     * @return A Base64 encoded, encrypted, deflated copy of the input array.
     * @throws EncryptionCodecException if there was a problem during any of the three stages of processing.
     */
    byte[] encrypt(byte[] bytes) throws EncryptionCodecException;

    /**
     * This method should firstly Base64 decode {@code bytes}, then decrypt it and finally inflate it.
     * @param bytes A Base64 encoded, encrypted and deflated array of bytes.
     * @return The original unencrypted content as a byte array.
     * @throws EncryptionCodecException if there was a problem during any of the three stages of processing.
     */
    byte[] decrypt(byte[] bytes) throws EncryptionCodecException;

}
