package com.cosmian.jna.findex.structs;

import java.nio.charset.Charset;

public class NextKeyword extends IndexedValue {

    public NextKeyword(byte[] keyword) {
        super(new byte[keyword.length + 1]);
        this.bytes[0] = WORD_BYTE;
        System.arraycopy(keyword, 0, this.bytes, 1, keyword.length);
    }

    public NextKeyword(String keyword) {
        this(keyword.getBytes(Charset.defaultCharset()));
    }

}
