package com.cosmian.jna.findex;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;

public class Uid {
    private final byte[] bytes;

    public Uid(byte[] bytes) {
        this.bytes = bytes;
    }

    public Uid(int len) {
        this.bytes = new byte[len];
        if (len > 0) {
            Random rd = new Random();
            rd.nextBytes(bytes);
        }
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        return Hex.encodeHexString(this.bytes);
    }
}
