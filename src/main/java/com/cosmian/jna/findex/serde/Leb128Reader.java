package com.cosmian.jna.findex.serde;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Leb128;

public class Leb128Reader {

    final InputStream in;

    public Leb128Reader(byte[] bytes) {
        this.in = new ByteArrayInputStream(bytes);
    }

    public Leb128Reader(InputStream in) {
        this.in = in;
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

    public <T extends Leb128Serializable> List<T> readCollection(Class<? extends Leb128Serializable> clazzOfT)
        throws CloudproofException {
        int size;
        try {
            size = (int) Leb128.readU64(this.in);
        } catch (IOException e) {
            throw new CloudproofException("failed reading the collection size: " + e.getMessage(), e);
        }
        List<T> result = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
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
        int size;
        try {
            size = (int) Leb128.readU64(this.in);
        } catch (IOException e) {
            throw new CloudproofException("failed reading the map size: " + e.getMessage(), e);
        }
        Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
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
