package com.cosmian.jna.findex.serde;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cosmian.CloudproofException;

public class Leb128Reader {

    final PeekInputStream in;

    public Leb128Reader(byte[] bytes) {
        this.in = new PeekInputStream(new ByteArrayInputStream(bytes));
    }

    public Leb128Reader(InputStream in) {
        this.in = new PeekInputStream(in);
    }

    public <T extends Leb128Serializable> T readObject(T newInstance) throws CloudproofException {
        newInstance.readObject(this.in);
        return newInstance;
    }

    public <T extends Leb128Serializable> T readObject(Class<? extends Leb128Serializable> clazzOfT)
        throws CloudproofException {
        T element;
        try {
            @SuppressWarnings("unchecked")
            final T el = (T) clazzOfT.newInstance();
            element = el;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CloudproofException(
                "Leb128 reader: failed instantiating a " + clazzOfT.getSimpleName() + ": " + e.getMessage(), e);
        }
        this.readObject(element);
        return element;
    }

    public boolean reachedCollectionEnd() throws CloudproofException {
        try {
            return this.in.peek() == 0;
        } catch (IOException e) {
            throw new CloudproofException("Leb128 reader: failed reading the collection end mark: " + e.getMessage(),
                e);
        }
    }

    public <T extends Leb128Serializable> List<T> readCollection(Class<? extends Leb128Serializable> clazzOfT)
        throws CloudproofException {
        List<T> result = new ArrayList<T>();
        while (!this.reachedCollectionEnd()) {
            result.add(this.readObject(clazzOfT));
        }
        return result;
    }

    public <LEFT extends Leb128Serializable, RIGHT extends Leb128Serializable> Tuple<LEFT, RIGHT> readTuple(Class<? extends Leb128Serializable> clazzOfK,
                                                                                                            Class<? extends Leb128Serializable> clazzOfV)
        throws CloudproofException {
        LEFT key = this.readObject(clazzOfK);
        RIGHT value = this.readObject(clazzOfV);
        return new Tuple<>(key, value);
    }

    private <K extends Leb128Serializable, V extends Leb128Serializable> Entry<K, V> readEntry(Class<? extends Leb128Serializable> clazzOfK,
                                                                                               Class<? extends Leb128Serializable> clazzOfV)
        throws CloudproofException {
        return this.readTuple(clazzOfK, clazzOfV);
    }

    public <K extends Leb128Serializable, V extends Leb128Serializable> Map<K, V> readMap(Class<? extends Leb128Serializable> clazzOfK,
                                                                                          Class<? extends Leb128Serializable> clazzOfV)
        throws CloudproofException {
        Map<K, V> map = new HashMap<>();
        while (!this.reachedCollectionEnd()) {
            Entry<K, V> entry = this.readEntry(clazzOfK, clazzOfV);
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    // ------------------------------------------------------
    // Static implementations
    // ------------------------------------------------------

    public static <T extends Leb128Serializable> List<T> deserializeCollection(Class<? extends Leb128Serializable> clazzOfT,
                                                                               byte[] bytes)
        throws CloudproofException {
        return new Leb128Reader(new ByteArrayInputStream(bytes)).readCollection(clazzOfT);
    }

    public static <K extends Leb128Serializable, V extends Leb128Serializable> Map<K, V> deserializeMap(Class<? extends Leb128Serializable> clazzOfK,
                                                                                                        Class<? extends Leb128Serializable> clazzOfV,
                                                                                                        byte[] bytes)
        throws CloudproofException {
        return new Leb128Reader(new ByteArrayInputStream(bytes)).readMap(clazzOfK, clazzOfV);
    }

    public static <LEFT extends Leb128Serializable, RIGHT extends Leb128Serializable> Tuple<LEFT, RIGHT> deserializeTuple(Class<? extends Leb128Serializable> clazzOfLeft,
                                                                                                                          Class<? extends Leb128Serializable> clazzOfRight,
                                                                                                                          byte[] bytes)
        throws CloudproofException {
        return new Leb128Reader(new ByteArrayInputStream(bytes)).readTuple(clazzOfLeft, clazzOfRight);
    }

}
