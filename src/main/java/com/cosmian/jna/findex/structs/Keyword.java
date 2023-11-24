package com.cosmian.jna.findex.structs;

import java.nio.charset.StandardCharsets;

import com.cosmian.jna.findex.serde.Leb128ByteArray;

/**
 * A Keyword indexed by Findex
 */
public class Keyword extends Leb128ByteArray {

    public Keyword() {
        super();
    }

    public Keyword(byte[] bytes) {
        super(bytes);
    }

    public Keyword(String string) {
        super(string.getBytes(StandardCharsets.UTF_8));
    }

    public IndexedValue toIndexedValue() {
	    return new IndexedValue(this);
    }

}
