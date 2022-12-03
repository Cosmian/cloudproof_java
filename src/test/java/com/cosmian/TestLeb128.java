package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestLeb128 {

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    @Test
    public void testCanonicalU64() throws Exception {
        // see Wikipedia https://en.wikipedia.org/wiki/LEB128
        final long canonicalVal = Long.parseUnsignedLong("10011000011101100101", 2); // new Random().nextLong();
        final byte[] canonicalLeb128 = new byte[] {
                (byte) Integer.parseInt("11100101", 2),
                (byte) Integer.parseInt("10001110", 2),
                (byte) Integer.parseInt("00100110", 2) };
        // encode
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Leb128.writeU64(bos, canonicalVal);
        byte[] result = bos.toByteArray();
        assertArrayEquals(canonicalLeb128, result);
        // decode
        ByteArrayInputStream bis = new ByteArrayInputStream(canonicalLeb128);
        long val_ = Leb128.readU64(bis);
        assertEquals(canonicalVal, val_);
    }

    @Test
    public void testRandomU64() throws Exception {
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            long val = random.nextLong();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Leb128.writeU64(bos, val);
            byte[] leb128 = bos.toByteArray();
            //
            ByteArrayInputStream bis = new ByteArrayInputStream(leb128);
            long val_ = Leb128.readU64(bis);
            assertEquals(val, val_);
        }
    }

    @Test
    public void testArray() throws Exception {
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            int length = random.nextInt(1025);
            byte[] buffer = new byte[length];
            for (int j = 0; j < length; j++) {
                buffer[j] = (byte) random.nextInt();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Leb128.writeArray(bos, buffer);
            byte[] data = bos.toByteArray();
            //
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            byte[] buffer_ = Leb128.readByteArray(bis);
            assertArrayEquals(buffer, buffer_);
        }
    }

    @Test
    public void testZeroLengthArray() throws Exception {
        byte[] array = new byte[] {};
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Leb128.writeArray(bos, array);
        byte[] data = bos.toByteArray();
        assertEquals(1, data.length);
        //
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        byte[] array_ = Leb128.readByteArray(bis);
        assertArrayEquals(array, array_);

    }

}
