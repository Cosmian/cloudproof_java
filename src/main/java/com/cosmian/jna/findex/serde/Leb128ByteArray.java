package com.cosmian.jna.findex.serde;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;

import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Leb128;

/**
 * A serializable byte array to use with the native interface
 */
public abstract class Leb128ByteArray implements Leb128Serializable {

    protected final static int FIXED_SIZE = -1;

    protected byte[] bytes;

    protected Leb128ByteArray() {
        this.bytes = new byte[] {};
    }

    public Leb128ByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * If -1, the array is of variable size and it must be read as an LEB128 first; if not use the given value
     * 
     * @return the array fixed size or -1 for variable size (default)
     */
    public int fixedSize() {
        return -1;
    }

    /**
     * Generate a random array of len bytes
     * 
     * @param len the length of the array
     */
    public Leb128ByteArray(int len) {
        this.bytes = new byte[len];
        if (len > 0) {
            SecureRandom rd = new SecureRandom();
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

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Leb128ByteArray)) {
            return false;
        }
        Leb128ByteArray leb128ByteArray = (Leb128ByteArray) o;
        return Arrays.equals(bytes, leb128ByteArray.bytes);
    }

    @Override
    public void writeObject(OutputStream out) throws CloudproofException {
        try {
            if (this.fixedSize() < 0) {
                Leb128.writeU64(out, this.bytes.length);
            } else {
                if (this.bytes.length != this.fixedSize()) {
                    throw new IOException(
                        "failed serializing invalid " + this.getClass().getSimpleName() + ": expected size="
                            + this.fixedSize() + ", actual=" + this.bytes.length);
                }
            }
            if (this.bytes.length > 0) {
                out.write(this.bytes);
            }
        } catch (IOException e) {
            throw new CloudproofException(
                "failed serializing " + this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void readObject(InputStream in) throws CloudproofException {
        try {
            int length = this.fixedSize() >= 0 ? this.fixedSize() : (int) Leb128.readU64(in);
            if (length == 0) {
                this.bytes = new byte[] {};
            } else {
                byte[] buffer = new byte[length];
                int actual = in.read(buffer);
                if (actual != length) {
                    throw new IOException("Invalid serialized " + this.getClass().getSimpleName() + ": expected size="
                        + length + ", actual=" + actual);
                }
                this.bytes = buffer;
            }
        } catch (IOException e) {
            throw new CloudproofException(
                "failed deserializing " + this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

}
