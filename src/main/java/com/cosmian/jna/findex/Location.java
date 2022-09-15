package com.cosmian.jna.findex;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;

public class Location extends IndexedValue {
    private byte[] bytes;

    public Location(byte[] location) {
        this.bytes = location;
    }

    public Location(String location) {
        this.bytes = location.getBytes(Charset.defaultCharset());
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    @Override
    public String toString() {
        byte[] prefix = {(byte) 108};
        byte[] location = Arrays.copyOf(prefix, this.bytes.length + 1);
        System.arraycopy(this.bytes, 0, location, 1, this.bytes.length);
        return Base64.getEncoder().encodeToString(location);
    }
}
