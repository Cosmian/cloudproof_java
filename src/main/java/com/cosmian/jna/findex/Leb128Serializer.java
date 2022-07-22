package com.cosmian.jna.findex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.android.dex.Leb128;
import com.android.dex.util.ByteArrayByteInput;
import com.android.dx.util.ByteArrayAnnotatedOutput;

public class Leb128Serializer {

    public static List<byte[]> deserialize(byte[] serializedUids) {
        List<byte[]> uids = new ArrayList<byte[]>();
        ByteArrayByteInput in = new ByteArrayByteInput(serializedUids);
        int len = 0;
        while ((len = Leb128.readUnsignedLeb128(in)) >= 0) {
            if (len == 0) {
                break;
            }
            byte[] uid = new byte[len];
            for (int i = 0; i < len; i++) {
                uid[i] = in.readByte();
            }
            uids.add(uid);
        }
        return uids;
    }

    public static byte[] serialize(HashMap<byte[], byte[]> uidsAndValues) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();
        for (Entry<byte[], byte[]> entry : uidsAndValues.entrySet()) {
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

    public static byte[] serialize(List<byte[]> values) {
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
