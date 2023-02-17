package com.cosmian.jna.findex.structs;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.cosmian.jna.findex.serde.Leb128ByteArray;
import com.cosmian.utils.Leb128;

public class Location extends Leb128ByteArray {

    public Location() {
        super();
    }

    public Location(byte[] value) {
        super(value);
    }

    public Location(String location) {
        this(location.getBytes(StandardCharsets.UTF_8));
    }

    public Location(long location) {
        this(Leb128.encode(location));
    }

    public Location(UUID location) {
        this(uuidToBytes(location));
    }

    public String toString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public long toLong() {
        return Leb128.decode(bytes);
    }

    public UUID toUuid() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

    public IndexedValue toIndexedValue() {
        return new IndexedValue(this);
    }

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

}
