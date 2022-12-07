package com.cosmian.jna.findex;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;

import com.cosmian.Leb128;
import com.cosmian.jna.findex.Leb128Serializer.Leb128Serializable;

/**
 * A byte array serializable using the {@link Leb128Serializer}
 */
public abstract class Leb128ByteArray implements Leb128Serializable {

    protected byte[] bytes;

    protected Leb128ByteArray() {
        this.bytes = new byte[] {};
    }

    public Leb128ByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Generate a random array of len bytes
     * 
     * @param len the length of the array
     */
    public Leb128ByteArray(int len) {
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
                throw new IOException("Invalid serialized " + this.getClass().getSimpleName());
            }
            this.bytes = buffer;
        }
    }

    @Override
    public boolean isEmpty() {
        return this.bytes.length == 0;
    }

}
