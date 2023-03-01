package com.cosmian.jna.findex.structs;

import java.nio.charset.StandardCharsets;

import com.cosmian.jna.findex.serde.Leb128ByteArray;

public class NextKeyword extends Leb128ByteArray implements ToIndexedValue {

    public NextKeyword() {
        super();
    }

    public NextKeyword(byte[] keyword) {
        super(keyword);
    }

    public NextKeyword(String keyword) {
        this(keyword.getBytes(StandardCharsets.UTF_8));
    }

    public IndexedValue toIndexedValue() {
        return new IndexedValue(this);
    }
}
