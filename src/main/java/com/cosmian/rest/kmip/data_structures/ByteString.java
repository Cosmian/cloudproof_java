package com.cosmian.rest.kmip.data_structures;

import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ByteString implements KmipStruct {

    @JsonProperty(value = "ByteString")
    private byte[] byteString;

    public ByteString() {
    }

    public ByteString(byte[] byteString) {
        this.byteString = byteString;
    }

    public byte[] getByteString() {
        return this.byteString;
    }

    public void setByteString(byte[] byteString) {
        this.byteString = byteString;
    }

    public ByteString key(byte[] byteString) {
        setByteString(byteString);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ByteString)) {
            return false;
        }
        ByteString ByteString = (ByteString) o;
        return Arrays.equals(byteString, ByteString.byteString);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(byteString);
    }

    @Override
    public String toString() {
        return "{" + " byteString='" + Hex.encodeHexString(getByteString()) + "'" + "}";
    }

}
