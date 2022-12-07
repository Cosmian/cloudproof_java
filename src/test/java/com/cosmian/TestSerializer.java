package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.cosmian.jna.findex.EntryTableValue;
import com.cosmian.jna.findex.Leb128Serializer;

public class TestSerializer {

    @Test
    public void testListSerializer() throws Exception {

        Random rand = new Random();
        int NUM_ELEMENTS = 1000;
        byte[] valueBuffer = new byte[64];
        // byte[] uidBuffer = new byte[32];

        List<EntryTableValue> list = new ArrayList<>(NUM_ELEMENTS);
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            rand.nextBytes(valueBuffer);
            EntryTableValue etv = new EntryTableValue(Arrays.copyOf(valueBuffer, valueBuffer.length));
            list.add(etv);
        }

        byte[] serialized = Leb128Serializer.serializeList(list);
        List<EntryTableValue> list_ = Leb128Serializer.deserializeList(serialized);

        assertEquals(list.size(), list_.size());
        for (EntryTableValue etv : list) {
            assertTrue(list_.contains(etv));
        }
    }

}
