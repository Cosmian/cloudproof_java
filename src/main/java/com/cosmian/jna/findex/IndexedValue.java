package com.cosmian.jna.findex;

import java.util.Arrays;

import com.cosmian.CosmianException;

public class IndexedValue {
    private byte[] bytes;

    public IndexedValue(byte[] bytes) throws CosmianException {
        this.bytes = bytes;
        if (bytes[0] != 108 && bytes[0] != 119) {
            throw new CosmianException("Indexed value must be prefixed by 'l' or 'w' in byte");
        }
    }

    public IndexedValue() {
    }

    public IndexedValue(Location location) throws CosmianException {
        if (location == null) {
            throw new CosmianException("location is null");
        }
        byte[] locationBytes = location.getBytes();
        byte[] prefix = {(byte) 108};
        byte[] indexedValueBytes = Arrays.copyOf(prefix, locationBytes.length + 1);
        System.arraycopy(locationBytes, 0, indexedValueBytes, 1, locationBytes.length);
        this.bytes = indexedValueBytes;
    }

    public IndexedValue(Word word) throws CosmianException {
        if (word == null) {
            throw new CosmianException("word is null");
        }
        byte[] wordBytes = word.getBytes();
        byte[] prefix = {(byte) 119};
        byte[] indexedValueBytes = Arrays.copyOf(prefix, wordBytes.length + 1);
        System.arraycopy(wordBytes, 0, indexedValueBytes, 1, wordBytes.length);
        this.bytes = wordBytes;
    }

    public boolean isLocation() {
        return this.bytes[0] == 108; // char 'l' is 108 is ascii
    }

    public boolean isWord() {
        return this.bytes[0] == 119; // char 'w' is 119 is ascii
    }

    public Location getLocation() throws CosmianException {
        if (isLocation()) {
            // remove 'l' char
            byte[] location = new byte[this.bytes.length - 1];
            System.arraycopy(this.bytes, 1, location, 0, location.length);
            return new Location(location);
        }
        throw new CosmianException("IndexValue is not a location");
    }

    public Word getWord() throws CosmianException {
        if (isWord()) {
            // remove 'w' char
            byte[] word = new byte[this.bytes.length - 1];
            System.arraycopy(this.bytes, 1, word, 0, word.length);
            return new Word(this.bytes);
        }
        throw new CosmianException("IndexValue is not a word");
    }
}
