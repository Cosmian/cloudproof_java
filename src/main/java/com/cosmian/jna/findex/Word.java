package com.cosmian.jna.findex;

import java.nio.charset.Charset;

public class Word extends IndexedValue {
    private byte[] bytes;

    public Word(byte[] word) {
        super(new byte[word.length + 1]);
        bytes[0] = WORD_BYTE;
        System.arraycopy(word, 0, bytes, 1, word.length);
    }

    public Word(String word) {
        this(word.getBytes(Charset.defaultCharset()));
    }

}
