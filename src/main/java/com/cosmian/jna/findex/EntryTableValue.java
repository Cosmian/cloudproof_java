package com.cosmian.jna.findex;

import com.cosmian.jna.findex.serde.Leb128ByteArray;

/**
 * An entry table value holds the encrypted content of value in the Entry table
 */
public class EntryTableValue extends Leb128ByteArray {

    public EntryTableValue(byte[] bytes) {
        super(bytes);
    }

}
