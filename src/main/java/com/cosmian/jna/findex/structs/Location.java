package com.cosmian.jna.findex.structs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.cosmian.jna.findex.serde.Leb128ByteArray;

public class Location extends Leb128ByteArray implements ToIndexedValue {

    public Location() {
        super();
    }

    public Location(byte[] value) {
        super(value);
    }

    public Location(String location) {
        this(location.getBytes(StandardCharsets.UTF_8));
    }

    public Location(int location) {
        this(ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.BIG_ENDIAN).putInt(location).array());
    }

    public Location(long location) {
        this(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN).putLong(location).array());
    }

    public Location(UUID location) {
        this(uuidToBytes(location));
    }

    public String toString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public long toLong() {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    public int toInt() {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
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
