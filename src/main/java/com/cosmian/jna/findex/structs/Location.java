package com.cosmian.jna.findex.structs;

import java.nio.charset.Charset;

import com.cosmian.jna.findex.serde.Leb128ByteArray;

public class Location extends Leb128ByteArray {

    public Location() {
        super();
    }

    public Location(byte[] value) {
        super(value);
    }

    public Location(String location) {
        this(location.getBytes(Charset.defaultCharset()));
    }

    public IndexedValue toIndexedValue() {
        return new IndexedValue(this);
    }
}
