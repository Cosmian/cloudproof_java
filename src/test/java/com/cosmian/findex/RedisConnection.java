package com.cosmian.findex;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.cosmian.jna.findex.structs.Uid32;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public abstract class RedisConnection implements Closeable {

    static final byte[] STORAGE_PREFIX = "cosmian".getBytes(StandardCharsets.UTF_8);

    JedisPool pool;

    String redisPassword;

    /**
     * The Redis server hostname from the REDIS_HOSTNAME environment variable. Defaults to localhost if not found.
     *
     * @return the hostname
     */
    public static String redisHostname() {
        String v = System.getenv("REDIS_HOSTNAME");
        if (v == null) {
            return "localhost";
        }
        return v;
    }

    /**
     * The Redis server port from the REDIS_PORT environment variable. Defaults to 6379 if not found.
     *
     * @return the port
     */
    public static int redisPort() {
        String v = System.getenv("REDIS_PORT");
        if (v == null) {
            return 6379;
        }
        return Integer.parseInt(v);
    }

    /**
     * The Redis server password from the REDIS_PASSWORD environment variable. Defaults to null if not found.
     *
     * @return the password
     */
    public static String redisPassword() {
        String v = System.getenv("REDIS_PASSWORD");
        return v;
    }

    /**
     * Establish a connection, authenticating it if the instance was instantiated with a password.
     *
     * @return the {@link Jedis} connection
     */
    public Jedis connect() {
        Jedis jedis = pool.getResource();
        if (redisPassword != null) {
            jedis.auth(redisPassword);
        }
        return jedis;
    }

    /**
     * Format Redis key as NUMBER:HEX_UID.
     *
     * @param number the index of the table
     * @param uid the {@link Uid32} value of the key
     * @return key as prefix|number on 4 bytes|uid
     */
    public static byte[] getKey(int number,
                                byte[] uid) {
        byte[] numberBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(number).array();
        byte[] result = Arrays.copyOf(STORAGE_PREFIX, STORAGE_PREFIX.length + numberBytes.length + uid.length);
        System.arraycopy(numberBytes, 0, result, STORAGE_PREFIX.length, numberBytes.length);
        System.arraycopy(uid, 0, result, STORAGE_PREFIX.length + numberBytes.length, uid.length);
        return result;
    }

    /**
     * Convert a Redis key back to an {@link Uid32}.
     *
     * @param key the Redis key
     * @return the {@link Uid32}
     */
    public static Uid32 uid(byte[] key) {
        return new Uid32(Arrays.copyOfRange(key, STORAGE_PREFIX.length + 4, key.length));
    }

    @Override
    public void close() throws IOException {
        pool.close();
    }

}
