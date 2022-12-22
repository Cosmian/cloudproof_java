package com.cosmian.jna.findex.serde;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Leb128;

public class Leb128Writer {

    final OutputStream os;

    public Leb128Writer(OutputStream os) {
        this.os = os;
    }

    public <T extends Leb128Serializable> void writeObject(T obj) throws CloudproofException {
        obj.writeObject(os);
    }

    private <K extends Leb128Serializable, V extends Leb128Serializable> void writeEntry(Entry<K, V> tuple)
        throws CloudproofException {
        this.writeObject(tuple.getKey());
        this.writeObject(tuple.getValue());
    }

    public <LEFT extends Leb128Serializable, RIGHT extends Leb128Serializable> void writeTuple(Tuple<LEFT, RIGHT> tuple)
        throws CloudproofException {
        this.writeEntry(tuple);
    }

    public <T extends Leb128Serializable> void writeCollection(Collection<T> elements) throws CloudproofException {
        try {
            Leb128.writeU64(this.os, elements.size());
        } catch (IOException e) {
            throw new CloudproofException("failed writing the collection to the output: " + e.getMessage(), e);
        }
        for (T value : elements) {
            this.writeObject(value);
        }
    }

    public <K extends Leb128Serializable, V extends Leb128Serializable> void writeMap(Map<K, V> map)
        throws CloudproofException {
        this.writeEntryCollection(map.entrySet());
    }

    public <K extends Leb128Serializable, V extends Leb128Serializable> void writeEntryCollection(Collection<Entry<K, V>> entryCollection)
        throws CloudproofException {
        try {
            Leb128.writeU64(this.os, entryCollection.size());
        } catch (IOException e) {
            throw new CloudproofException("failed writing the entry collection to the output: " + e.getMessage(), e);
        }
        for (Entry<K, V> value : entryCollection) {
            this.writeEntry(value);
        }
    }

    // ------------------------------------------------------
    // Static implementations
    // ------------------------------------------------------

    public static <T extends Leb128Serializable> byte[] serializeCollection(Collection<T> elements)
        throws CloudproofException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new Leb128Writer(bos).writeCollection(elements);
        return bos.toByteArray();
    }

    public static <K extends Leb128Serializable, V extends Leb128Serializable> byte[] serializeMap(Map<K, V> map)
        throws CloudproofException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new Leb128Writer(bos).writeMap(map);
        return bos.toByteArray();
    }

    public static <LEFT extends Leb128Serializable, RIGHT extends Leb128Serializable> byte[] serializeTuple(Tuple<LEFT, RIGHT> tuple)
        throws CloudproofException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new Leb128Writer(bos).writeTuple(tuple);
        return bos.toByteArray();
    }

    public static <K extends Leb128Serializable, V extends Leb128Serializable> byte[] serializeEntryCollection(Collection<Entry<K, V>> entryCollection)
        throws CloudproofException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new Leb128Writer(bos).writeEntryCollection(entryCollection);
        return bos.toByteArray();
    }
}
