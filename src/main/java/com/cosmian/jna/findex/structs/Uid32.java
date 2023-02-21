package com.cosmian.jna.findex.structs;

import com.cosmian.jna.findex.serde.Leb128ByteArray;

public class Uid32 extends Leb128ByteArray {

    public Uid32(byte[] bytes) {
        super(bytes);
        if (bytes.length != fixedSize()) {
            throw new IllegalArgumentException(
                "A " + getClass().getSimpleName() + " must have " + fixedSize() + " bytes, not " + bytes.length);
        }
    }

    @Override
    public int fixedSize() {
        return 32;
    }

}
