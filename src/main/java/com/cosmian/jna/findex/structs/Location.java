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

    /**
     * Numbers are encoded in big-endian 8 bytes.
     *
     * @param location long representation
     */
    public Location(long location) {
        this(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN).putLong(location).array());
    }

    /**
     * Convert UUIDv4 only because they are more common.

     * @param uuidV4 uuid representation
     */
    public Location(UUID uuidV4) {
        this(uuidToBytes(uuidV4));
    }

    public String toString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public long toNumber() {
        if (bytes.length != Long.BYTES) {
            throw new RuntimeException(
                "The location is of length " + bytes.length + ", " + Long.BYTES + " bytes expected for a number.");
        }

        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    public UUID toUuid() {
        if (bytes.length != Long.BYTES * 2) {
            throw new RuntimeException(
                "The location is of length " + bytes.length + ", " + Long.BYTES * 2 + " bytes expected for a UUID.");
        }

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
