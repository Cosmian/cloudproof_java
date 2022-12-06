package com.cosmian.jna.findex;

import java.nio.charset.Charset;

public class Location extends IndexedValue {
    private byte[] bytes;

    public Location(byte[] location) {
        super(new byte[location.length + 1]);
        bytes[0] = LOCATION_BYTE;
        System.arraycopy(location, 0, bytes, 1, location.length);
    }

    public Location(String location) {
        this(location.getBytes(Charset.defaultCharset()));
    }

}
