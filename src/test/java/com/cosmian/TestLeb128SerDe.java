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

import com.cosmian.jna.findex.ChainTableValue;
import com.cosmian.jna.findex.EntryTableValue;
import com.cosmian.jna.findex.EntryTableValues;
import com.cosmian.jna.findex.Uid;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.serde.Leb128Writer;

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
    public void testMapSerDe() throws Exception {

        Random rand = new Random();
        int NUM_ELEMENTS = 1000;
        byte[] uidBuffer = new byte[32];
        byte[] valueBuffer = new byte[64];

        Map<Uid, ChainTableValue> map = new HashMap<>(NUM_ELEMENTS);
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            rand.nextBytes(uidBuffer);
            rand.nextBytes(valueBuffer);
            Uid uid = new Uid(Arrays.copyOf(uidBuffer, uidBuffer.length));
            ChainTableValue ctv = new ChainTableValue(Arrays.copyOf(valueBuffer, valueBuffer.length));
            map.put(uid, ctv);
        }

        byte[] serialized = Leb128Writer.serializeMap(map);
        Map<Uid, ChainTableValue> map_ = Leb128Reader.deserializeMap(Uid.class, ChainTableValue.class, serialized);

        assertEquals(map.size(), map_.size());
        for (Entry<Uid, ChainTableValue> entry : map.entrySet()) {
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

        Map<Uid, EntryTableValues> map = new HashMap<>(NUM_ELEMENTS);
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            rand.nextBytes(uidBuffer);
            rand.nextBytes(previousValueBuffer);
            rand.nextBytes(newValueBuffer);
            Uid uid = new Uid(Arrays.copyOf(uidBuffer, uidBuffer.length));
            EntryTableValues ctv = new EntryTableValues(
                Arrays.copyOf(previousValueBuffer, previousValueBuffer.length),
                Arrays.copyOf(newValueBuffer, newValueBuffer.length));
            map.put(uid, ctv);
        }

        byte[] serialized = Leb128Writer.serializeMap(map);
        Map<Uid, EntryTableValues> map_ = Leb128Reader.deserializeMap(Uid.class, EntryTableValues.class, serialized);

        assertEquals(map.size(), map_.size());
        for (Entry<Uid, EntryTableValues> entry : map.entrySet()) {
            assertEquals(entry.getValue(), map_.get(entry.getKey()));
        }
    }

}
