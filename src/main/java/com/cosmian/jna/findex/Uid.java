package com.cosmian.jna.findex;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;

import com.cosmian.Leb128;
import com.cosmian.jna.findex.Leb128Serializer.Leb128Serializable;

public class Uid implements Leb128Serializable {
    static final long serialVersionUID = 1L;

    private byte[] bytes;

    protected Uid() {
        this.bytes = new byte[] {};
    }

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

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        Leb128.writeU64(out, this.bytes.length);
        if (this.bytes.length > 0) {
            out.write(this.bytes);
        }
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        int length = (int) Leb128.readU64(in);
        if (length == 0) {
            this.bytes = new byte[] {};
        } else {
            byte[] buffer = new byte[length];
            int actual = in.read(buffer);
            if (actual != length) {
                throw new IOException("Invalid serialized UID");
            }
            this.bytes = buffer;
        }
    }

    @Override
    public boolean isEmpty() {
        return this.bytes.length == 0;
    }

}
