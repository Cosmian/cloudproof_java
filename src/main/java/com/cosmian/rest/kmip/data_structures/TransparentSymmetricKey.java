package com.cosmian.rest.kmip.data_structures;

import java.util.Arrays;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.codec.binary.Hex;

public class TransparentSymmetricKey implements KmipStruct {

    @JsonProperty(value = "Key")
    private byte[] key;

    public TransparentSymmetricKey() {
    }

    public TransparentSymmetricKey(byte[] key) {
        this.key = key;
    }

    public byte[] getKey() {
        return this.key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public TransparentSymmetricKey key(byte[] key) {
        setKey(key);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TransparentSymmetricKey)) {
            return false;
        }
        TransparentSymmetricKey transparentSymmetricKey = (TransparentSymmetricKey) o;
        return Arrays.equals(key, transparentSymmetricKey.key);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(key);
    }

    @Override
    public String toString() {
        return "{" + " key='" + Hex.encodeHexString(getKey()) + "'" + "}";
    }

}
