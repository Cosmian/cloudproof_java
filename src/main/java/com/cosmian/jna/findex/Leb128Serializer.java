package com.cosmian.jna.findex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.cosmian.CloudproofException;
import com.cosmian.Leb128;

public class Leb128Serializer {

    public static List<byte[]> deserializeList(byte[] serializedUids) throws CloudproofException {
        List<byte[]> result = new ArrayList<byte[]>();
        ByteArrayInputStream in = new ByteArrayInputStream(serializedUids);
        long len = 0;
        try {
            while ((len = Leb128.readU64(in)) >= 0) {
                if (len == 0) {
                    break;
                }
                byte[] element = new byte[(int) len];
                in.read(element);
                result.add(element);
            }
        } catch (IOException e) {
            throw new CloudproofException("failed serializing the list", e);
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

    public static byte[] serializeHashMap(HashMap<byte[], byte[]> uidsAndValues) throws CloudproofException {
        return serializeEntrySet(uidsAndValues.entrySet());
    }

    public static byte[] serializeEntrySet(Set<Entry<byte[], byte[]>> uidsAndValues) throws CloudproofException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (Entry<byte[], byte[]> entry : uidsAndValues) {
                byte[] uid = entry.getKey();
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

    public static byte[] serializeList(List<byte[]> values) throws CloudproofException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (byte[] bs : values) {
                Leb128.writeArray(out, bs);
            }
            // empty array marks the end
            out.write(new byte[] {0});
        } catch (IOException e) {
            throw new CloudproofException("failed serializing the list", e);
        }

        return out.toByteArray();
    }
}
