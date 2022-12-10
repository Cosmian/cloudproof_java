package com.cosmian.jna.findex.structs;

import java.nio.charset.Charset;

import com.cosmian.jna.findex.serde.Leb128ByteArray;

public class NextKeyword extends Leb128ByteArray {

    public NextKeyword() {
        super();
    }

    public NextKeyword(byte[] keyword) {
        super(keyword);
    }

    public NextKeyword(String keyword) {
        this(keyword.getBytes(Charset.defaultCharset()));
    }

    public IndexedValue toIndexedValue() {
        return new IndexedValue(this);
    }

}
