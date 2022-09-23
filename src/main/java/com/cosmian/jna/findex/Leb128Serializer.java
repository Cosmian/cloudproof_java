package com.cosmian.jna.findex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.android.dex.Leb128;
import com.android.dex.util.ByteArrayByteInput;
import com.android.dx.util.ByteArrayAnnotatedOutput;

public class Leb128Serializer {

    public static List<byte[]> deserializeList(byte[] serializedUids) {
        List<byte[]> result = new ArrayList<byte[]>();
        ByteArrayByteInput in = new ByteArrayByteInput(serializedUids);
        int len = 0;
        while ((len = Leb128.readUnsignedLeb128(in)) >= 0) {
            if (len == 0) {
                break;
            }
            byte[] element = new byte[len];
            for (int i = 0; i < len; i++) {
                element[i] = in.readByte();
            }
            result.add(element);
        }
        return result;
    }

    public static HashMap<byte[], byte[]> deserializeHashmap(byte[] serializedUids) {
        HashMap<byte[], byte[]> result = new HashMap<byte[], byte[]>();
        ByteArrayByteInput in = new ByteArrayByteInput(serializedUids);
        int len = 0;
        while ((len = Leb128.readUnsignedLeb128(in)) >= 0) {
            if (len == 0) {
                break;
            }
            byte[] key = new byte[len];
            for (int i = 0; i < len; i++) {
                key[i] = in.readByte();
            }

            len = Leb128.readUnsignedLeb128(in);
            if (len == 0) {
                throw new IllegalArgumentException("HashMap `value` should be serialized after `key`");
            }
            byte[] value = new byte[len];
            for (int i = 0; i < len; i++) {
                value[i] = in.readByte();
            }
            result.put(key, value);
        }
        return result;
    }

    public static byte[] serializeHashMap(HashMap<byte[], byte[]> uidsAndValues) {
        return serializeEntrySet(uidsAndValues.entrySet());
    }

    public static byte[] serializeEntrySet(Set<Entry<byte[], byte[]>> uidsAndValues) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();
        for (Entry<byte[], byte[]> entry : uidsAndValues) {
            byte[] uid = entry.getKey();
            Leb128.writeUnsignedLeb128(out, uid.length);
            out.write(uid);
            byte[] value = entry.getValue();
            Leb128.writeUnsignedLeb128(out, value.length);
            out.write(value);
        }
        // empty array marks the end
        out.write(new byte[] {0});

        byte[] uidsAndValuesBytes = out.toByteArray();
        return uidsAndValuesBytes;
    }

    public static byte[] serializeList(List<byte[]> values) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();
        for (byte[] bs : values) {
            Leb128.writeUnsignedLeb128(out, bs.length);
            out.write(bs);
        }
        // empty array marks the end
        out.write(new byte[] {0});

        return out.toByteArray();
    }
}
