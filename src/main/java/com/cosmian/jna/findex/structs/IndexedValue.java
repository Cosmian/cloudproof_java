package com.cosmian.jna.findex.structs;

import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

import com.cosmian.jna.findex.serde.Leb128ByteArray;
import com.cosmian.utils.CloudproofException;

/**
 * An Indexed Value is either a Location or a (next) word It is the plaintext entry of the Chain table
 */
public class IndexedValue extends Leb128ByteArray {

    protected final byte LOCATION_BYTE = 'l';

    protected final byte WORD_BYTE = 'w';

    public IndexedValue(byte[] bytes) {
        super(bytes);
    }

    public boolean isLocation() {
        return this.bytes[0] == LOCATION_BYTE; // char 'l' is LOCATION_BYTE is ascii
    }

    public boolean isWord() {
        return this.bytes[0] == WORD_BYTE; // char 'w' is WORD_BYTE is ascii
    }

    public Location getLocation() throws CloudproofException {
        if (isLocation()) {
            return (Location) this;
        }
        throw new CloudproofException("IndexValue is not a location");
    }

    public NextKeyword getWord() throws CloudproofException {
        if (isWord()) {
            return (NextKeyword) this;
        }
        throw new CloudproofException("IndexValue is not a word");
    }

    @Override
    public String toString() {
        return this.bytes[0] + ":"
            + Hex.encodeHexString(Arrays.copyOfRange(this.bytes, 1, this.bytes.length));
    }

}
