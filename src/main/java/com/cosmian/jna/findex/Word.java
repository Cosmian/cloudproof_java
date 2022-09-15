package com.cosmian.jna.findex;

import java.nio.charset.Charset;
import java.util.Base64;

public class Word extends IndexedValue {
    private byte[] bytes;

    public Word(byte[] word) {
        this.bytes = word;
    }

    public Word(String word) {
        this.bytes = word.getBytes(Charset.defaultCharset());
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(this.bytes);
    }
}
