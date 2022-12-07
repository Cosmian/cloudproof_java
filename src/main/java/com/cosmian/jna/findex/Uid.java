package com.cosmian.jna.findex;

import com.cosmian.jna.findex.serde.Leb128ByteArray;

public class Uid extends Leb128ByteArray {
    static final long serialVersionUID = 1L;

    public Uid(byte[] bytes) {
        super(bytes);
    }

}
