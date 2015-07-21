package com.autonomy.aci.client;

import com.autonomy.aci.client.transport.EncryptionCodec;
import com.autonomy.aci.client.transport.EncryptionCodecException;
import org.apache.commons.codec.binary.Base64;

/**
 * EncryptionCodec for use in unit tests. Fakes encryption by base 64 encoding the input bytes.
 */
public class TestEncryptionCodec implements EncryptionCodec {
    private static final long serialVersionUID = -6973661597503333561L;

    public byte[] encrypt(final byte[] bytes) throws EncryptionCodecException {
        return Base64.encodeBase64(bytes);
    }

    public byte[] decrypt(final byte[] bytes) throws EncryptionCodecException {
        return Base64.decodeBase64(bytes);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof TestEncryptionCodec;
    }

    @Override
    public int hashCode() {
        return 123;
    }
}
