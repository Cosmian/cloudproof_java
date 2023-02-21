package com.cosmian.jna.findex.structs;

import com.cosmian.jna.findex.serde.Leb128ByteArray;

/**
 * An chain table value holds the encrypted content of an {@link IndexedValue} in the Chain table
 */
public class ChainTableValue extends Leb128ByteArray {

    public ChainTableValue(byte[] bytes) {
        super(bytes);
    }

}
