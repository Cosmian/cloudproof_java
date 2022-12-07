package com.cosmian.jna.findex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cosmian.CloudproofException;

public class Leb128Serializer {

    /**
     * An Object is a serializable using the {@link Leb128Serializer} if implements
     * {@link Serializable} and can be empty (a null byte). Empty objects are used
     * to mark the end of streams.
     */
    public interface Leb128Serializable extends Serializable {

        /**
         * An empty object is used as a marker to end list
         */
        public boolean isEmpty();
    }

    public static <T extends Leb128Serializable> List<T> deserializeList(byte[] serializedList)
            throws CloudproofException {
        List<T> result = new ArrayList<T>();
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedList))) {
            while (true) {
                @SuppressWarnings("unchecked")
                final T element = (T) in.readObject();
                if (element.isEmpty()) {
                    break;
                }
                result.add(element);
            }
        } catch (IOException e) {
            throw new CloudproofException("failed deserializing the list", e);
        } catch (ClassNotFoundException e) {
            throw new CloudproofException("failed deserializing the list", e);
        }
        return result;
    }

    public static <K extends Leb128Serializable, V extends Leb128Serializable> Map<K, V> deserializeMap(
            byte[] serializedMap) throws CloudproofException {
        HashMap<K, V> result = new HashMap<>();
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedMap))) {
            while (true) {
                @SuppressWarnings("unchecked")
                final K key = (K) in.readObject();
                if (key.isEmpty()) {
                    break;
                }
                @SuppressWarnings("unchecked")
                final V value = (V) in.readObject();
                result.put(key, value);
            }
        } catch (IOException e) {
            throw new CloudproofException("failed deserializing the map", e);
        } catch (ClassNotFoundException e) {
            throw new CloudproofException("failed deserializing the map", e);
        }
        return result;
    }

    public static <K extends Leb128Serializable, V extends Leb128Serializable> byte[] serializeMap(Map<K, V> map)
            throws CloudproofException {
        return serializeEntrySet(map.entrySet());
    }

    public static <K extends Leb128Serializable, V extends Leb128Serializable> byte[] serializeEntrySet(
            Set<Entry<K, V>> entrySet) throws CloudproofException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(out)) {
            for (Entry<K, V> entry : entrySet) {
                oos.writeObject(entry.getKey());
                oos.writeObject(entry.getValue());
            }
            // empty object marks the end
            out.write(0);
            return out.toByteArray();
        } catch (IOException e) {
            throw new CloudproofException("failed serializing the set", e);
        }
    }

    public static <T extends Leb128Serializable> byte[] serializeList(List<T> elements) throws CloudproofException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(out)) {
            for (T value : elements) {
                oos.writeObject(value);
            }
            // empty object marks the end
            out.write(0);
            return out.toByteArray();
        } catch (IOException e) {
            throw new CloudproofException("failed serializing the list", e);
        }

    }
}
