package com.cosmian.jna.findex;

import com.cosmian.jna.findex.serde.Leb128ByteArray;

/**
 * An chain table value holds the encrypted content of an {@link IndexedValue} in the Chain table
 */
public class ChainTableValue extends Leb128ByteArray {
    static final long serialVersionUID = 1L;

    public ChainTableValue(byte[] bytes) {
        super(bytes);
    }

}
