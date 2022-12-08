package com.cosmian.jna.findex.structs;

import java.nio.charset.Charset;

public class Location extends IndexedValue {

    public Location(byte[] location) {
        super(new byte[location.length + 1]);
        this.bytes[0] = LOCATION_BYTE;
        System.arraycopy(location, 0, this.bytes, 1, location.length);
    }

    public Location(String location) {
        this(location.getBytes(Charset.defaultCharset()));
    }

}
