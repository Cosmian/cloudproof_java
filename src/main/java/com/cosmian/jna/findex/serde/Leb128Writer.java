package com.cosmian.jna.findex.serde;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.cosmian.CloudproofException;

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

    private void writeCollectionEnd() throws CloudproofException {
        try {
            os.write(0);
            os.flush();
        } catch (IOException e) {
            throw new CloudproofException("Leb128 writer: failed writing the collection end mark: " + e.getMessage(),
                e);
        }
    }

    public <T extends Leb128Serializable> void writeCollection(Collection<T> elements) throws CloudproofException {
        for (T value : elements) {
            this.writeObject(value);
        }
        // mark the end
        this.writeCollectionEnd();
    }

    public <K extends Leb128Serializable, V extends Leb128Serializable> void writeMap(Map<K, V> map)
        throws CloudproofException {
        this.writeEntryCollection(map.entrySet());
    }

    public <K extends Leb128Serializable, V extends Leb128Serializable> void writeEntryCollection(Collection<Entry<K, V>> tupleCollection)
        throws CloudproofException {
        for (Entry<K, V> value : tupleCollection) {
            this.writeEntry(value);
        }
        // mark the end
        this.writeCollectionEnd();
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
