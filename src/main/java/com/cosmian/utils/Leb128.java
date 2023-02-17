package com.cosmian.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Leb128 {

    final static Long MASK = 127L;

    final static Long HIGH_ORDER_BIT = 128L;

    public static byte[] encode(long value) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Leb128.writeU64(output, value);

            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Cannot encode bytes", e);
        }
    }

    public static long decode(byte[] bytes) {
        try {
            return readU64(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new RuntimeException("Cannot decode bytes", e);
        }
    }

    /**
     * Write a u64 as an LEB128
     *
     * @param os the {@link OutputStream} to write to
     * @param value the value to write
     * @throws IOException if the stream is in error
     */
    public static void writeU64(OutputStream os,
                                long value)
        throws IOException {
        do {
            long b = value & MASK;
            value = value >>> 7;
            if (value != 0) {
                b = b | HIGH_ORDER_BIT;
            }
            os.write((int) b & 0xFF);
        } while (value != 0);
    }

    /**
     * Read an u64 encoded as an LEB 128 from a stream
     *
     * @param is the {@link InputStream} to read from
     * @return the long value
     * @throws IOException if the stream is in error
     */
    public static long readU64(InputStream is) throws IOException {
        int shift = 0;
        long result = 0;

        boolean last;
        do {
            long b = is.read();
            last = (b & HIGH_ORDER_BIT) != HIGH_ORDER_BIT;
            b = b & MASK;
            result |= (b << shift);
            shift += 7;
        } while (!last);
        return result;
    }

    /**
     * Read a byte array prepended with a LEB 128 u64 indicated its length Warning: the maximum array size is 2^31
     *
     * @param is the {@link InputStream} to read the array from
     * @return the byte array
     * @throws IOException if the stream is in error or the number of bytes read is not the expected value
     */
    public static byte[] readByteArray(InputStream is) throws IOException {
        long length = readU64(is);
        if (length == 0) {
            return new byte[] {};
        }
        byte[] buffer = new byte[(int) length];
        int actualLength = is.read(buffer);
        if (actualLength != length) {
            throw new IOException(
                "Error reading a byte array of " + length + " bytes: only " + actualLength + " bytes were read !");
        }
        return buffer;
    }

    /**
     * Write a byte array prepended with a LEB128 u64 indicating its length
     *
     * @param os the {@link OutputStream} to write to
     * @param array the array to write
     * @throws IOException is the stream is in error
     */
    public static void writeArray(OutputStream os,
                                  byte[] array)
        throws IOException {
        writeU64(os, (long) array.length);
        os.write(array);
    }

}
