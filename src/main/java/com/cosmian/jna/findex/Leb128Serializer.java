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
import com.cosmian.Leb128;

public class Leb128Serializer {

    public interface Leb128Serializable extends Serializable {

        /**
         * An empty object is used as a marker to end list
         */
        public boolean isEmpty();
    }

    public static <T extends Leb128Serializable> List<T> deserializeList(byte[] serializedUids)
        throws CloudproofException {
        List<T> result = new ArrayList<T>();
        try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(serializedUids))) {
            while (true) {
                @SuppressWarnings("unchecked")
                final T element = (T) is.readObject();
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

    public static HashMap<byte[], byte[]> deserializeHashmap(byte[] serializedUids) throws CloudproofException {
        HashMap<byte[], byte[]> result = new HashMap<byte[], byte[]>();
        ByteArrayInputStream in = new ByteArrayInputStream(serializedUids);
        long len = 0;
        try {
            while ((len = Leb128.readU64(in)) >= 0) {
                if (len == 0) {
                    break;
                }
                byte[] key = new byte[(int) len];
                in.read(key);

                len = Leb128.readU64(in);
                if (len == 0) {
                    throw new IllegalArgumentException("HashMap `value` should be serialized after `key`");
                }
                byte[] value = new byte[(int) len];
                in.read(value);
                result.put(key, value);
            }
        } catch (IOException e) {
            throw new CloudproofException("failed deserializing the map", e);
        }
        return result;
    }

    public static byte[] serializeHashMap(Map<Uid, byte[]> uidsAndValues) throws CloudproofException {
        return serializeEntrySet(uidsAndValues.entrySet());
    }

    public static byte[] serializeEntrySet(Set<Entry<Uid, byte[]>> uidsAndValues) throws CloudproofException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (Entry<Uid, byte[]> entry : uidsAndValues) {
                byte[] uid = entry.getKey().getBytes();
                Leb128.writeArray(out, uid);
                byte[] value = entry.getValue();
                Leb128.writeArray(out, value);
            }
            // empty array marks the end
            out.write(new byte[] {0});
        } catch (IOException e) {
            throw new CloudproofException("failed serializing the set", e);
        }

        byte[] uidsAndValuesBytes = out.toByteArray();
        return uidsAndValuesBytes;
    }

    public static <T extends Leb128Serializable> byte[] serializeList(List<T> values) throws CloudproofException {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out)) {
            for (T value : values) {
                oos.writeObject(value);
            }
            // empty object marks the end
            Leb128.writeU64(out, 0);
            return out.toByteArray();
        } catch (IOException e) {
            throw new CloudproofException("failed serializing the list", e);
        }

    }
}
