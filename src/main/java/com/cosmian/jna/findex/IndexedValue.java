package com.cosmian.jna.findex;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

import com.cosmian.CloudproofException;
import com.cosmian.Leb128;
import com.cosmian.jna.findex.Leb128Serializer.Leb128Serializable;

public class IndexedValue implements Leb128Serializable {

    protected final byte LOCATION_BYTE = 'l';

    protected final byte WORD_BYTE = 'w';

    private byte[] bytes;

    protected IndexedValue() {
        this.bytes = new byte[] {};
    }

    protected IndexedValue(byte[] bytes) {
        this.bytes = bytes;
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

    public Word getWord() throws CloudproofException {
        if (isWord()) {
            return (Word) this;
        }
        throw new CloudproofException("IndexValue is not a word");
    }

    @Override
    public String toString() {
        return this.bytes[0] + ":"
            + Base64.getEncoder().encodeToString(Arrays.copyOfRange(this.bytes, 1, this.bytes.length));
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        Leb128.writeU64(out, this.bytes.length);
        if (this.bytes.length > 0) {
            out.write(this.bytes);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int length = (int) Leb128.readU64(in);
        if (length == 0) {
            this.bytes = new byte[] {};
        } else {
            byte[] buffer = new byte[length];
            int actual = in.read(buffer);
            if (actual != length) {
                throw new IOException("Invalid serialized Indexed Value");
            }
            this.bytes = buffer;
        }
    }

    @Override
    public boolean isEmpty() {
        return this.bytes.length == 0;
    }
}
