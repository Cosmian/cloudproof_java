package com.cosmian.rest.kmip.data_structures;

import com.cosmian.rest.kmip.json.KmipChoice6;

// TODO Implement missing TransparentDSAPrivateKey, TransparentDSAPublicKey,
public class KeyMaterial extends
    KmipChoice6<ByteString, TransparentSymmetricKey, TransparentDHPrivateKey, TransparentDHPublicKey, TransparentECPrivateKey, TransparentECPublicKey> {

    public KeyMaterial(Object value) {
        super(value);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
