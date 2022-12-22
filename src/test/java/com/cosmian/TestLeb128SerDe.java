package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.serde.Leb128Writer;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.Uid32;

public class TestLeb128SerDe {

    @Test
    public void testCollectionSerDe() throws Exception {

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

        byte[] serialized = Leb128Writer.serializeCollection(list);
        List<EntryTableValue> list_ = Leb128Reader.deserializeCollection(EntryTableValue.class, serialized);

        assertEquals(list.size(), list_.size());
        for (EntryTableValue etv : list) {
            assertTrue(list_.contains(etv));
        }
    }

    @Test
    public void testFixedCollectionSerDe() throws Exception {

        Random rand = new Random();
        int NUM_ELEMENTS = 1000;
        byte[] uidBuffer = new byte[new Uid32().fixedSize()];

        List<Uid32> list = new ArrayList<>(NUM_ELEMENTS);
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            rand.nextBytes(uidBuffer);
            Uid32 etv = new Uid32(Arrays.copyOf(uidBuffer, uidBuffer.length));
            list.add(etv);
        }

        byte[] serialized = Leb128Writer.serializeCollection(list);
        List<Uid32> list_ = Leb128Reader.deserializeCollection(Uid32.class, serialized);

        assertEquals(list.size(), list_.size());
        for (Uid32 etv : list) {
            assertTrue(list_.contains(etv));
        }
    }

    @Test
    public void testMapSerDe() throws Exception {

        Random rand = new Random();
        int NUM_ELEMENTS = 1000;
        byte[] uidBuffer = new byte[32];
        byte[] valueBuffer = new byte[64];

        Map<Uid32, ChainTableValue> map = new HashMap<>(NUM_ELEMENTS);
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            rand.nextBytes(uidBuffer);
            rand.nextBytes(valueBuffer);
            Uid32 uid = new Uid32(Arrays.copyOf(uidBuffer, uidBuffer.length));
            ChainTableValue ctv = new ChainTableValue(Arrays.copyOf(valueBuffer, valueBuffer.length));
            map.put(uid, ctv);
        }

        byte[] serialized = Leb128Writer.serializeMap(map);
        Map<Uid32, ChainTableValue> map_ = Leb128Reader.deserializeMap(Uid32.class, ChainTableValue.class, serialized);

        assertEquals(map.size(), map_.size());
        for (Entry<Uid32, ChainTableValue> entry : map.entrySet()) {
            assertEquals(entry.getValue(), map_.get(entry.getKey()));
        }
    }

    @Test
    public void testMapEntryTableValuesSerDe() throws Exception {

        Random rand = new Random();
        int NUM_ELEMENTS = 1000;
        byte[] uidBuffer = new byte[32];
        byte[] previousValueBuffer = new byte[64];
        byte[] newValueBuffer = new byte[64];

        Map<Uid32, EntryTableValues> map = new HashMap<>(NUM_ELEMENTS);
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            rand.nextBytes(uidBuffer);
            rand.nextBytes(previousValueBuffer);
            rand.nextBytes(newValueBuffer);
            Uid32 uid = new Uid32(Arrays.copyOf(uidBuffer, uidBuffer.length));
            EntryTableValues ctv = new EntryTableValues(
                Arrays.copyOf(previousValueBuffer, previousValueBuffer.length),
                Arrays.copyOf(newValueBuffer, newValueBuffer.length));
            map.put(uid, ctv);
        }

        byte[] serialized = Leb128Writer.serializeMap(map);
        Map<Uid32, EntryTableValues> map_ =
            Leb128Reader.deserializeMap(Uid32.class, EntryTableValues.class, serialized);

        assertEquals(map.size(), map_.size());
        for (Entry<Uid32, EntryTableValues> entry : map.entrySet()) {
            assertEquals(entry.getValue(), map_.get(entry.getKey()));
        }
    }

}
