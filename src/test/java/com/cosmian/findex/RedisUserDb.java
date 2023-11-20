package com.cosmian.findex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.cosmian.jna.findex.FilterLocations;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.utils.CloudproofException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisUserDb extends RedisConnection implements FilterLocations {

    static final int PREFIX = 3;

    /**
     * Internal constructor instantiating from an existing pool and loading the Conditional Upsert Lua script
     *
     * @param pool the existing {@link JedisPool}
     * @param redisPassword the password to use to authenticate
     */
    RedisUserDb(JedisPool pool, String redisPassword) {
        this.pool = pool;
        this.redisPassword = redisPassword;
    }

    /**
     * Instantiate a Redis instance from an URI. Read the password from environment variables.
     *
     * @param uri the URI to the Redis server
     */
    public RedisUserDb(String uri) {
        this(new JedisPool(uri), redisPassword());
    }

    /**
     * Instantiate a Redis instance using the hostname, port and password passed as environment variables
     */
    public RedisUserDb() {
        this(new JedisPool(redisHostname(), redisPort()), redisPassword());
    }

    /**
     * Instantiate a new Redis Client
     *
     * @param hostname the REST Server URL e.g. localhost
     * @param port Sets a specified port value e.g 6379
     * @param password the authentication password or token
     */
    public RedisUserDb(String hostname, int port, String password) {
        this(new JedisPool(hostname, port), password);
    }

    /**
     * Insert all the users in the data "table"
     *
     * @param testFindexDataset the dataset containing the user records
     * @throws CloudproofException
     */
    public void insertUsers(UsersDataset[] testFindexDataset) throws CloudproofException {
        try (Jedis jedis = connect()) {
            for (UsersDataset user : testFindexDataset) {
                byte[] keySuffix = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN).putLong(user.id).array();
                byte[] key = RedisConnection.getKey(PREFIX, keySuffix);
                byte[] value = user.toString().getBytes(StandardCharsets.UTF_8);
                jedis.set(key, value);
            }
        }
    }

    /**
     * Return all the keys in the raw format
     */
    public Set<byte[]> getAllKeys() {
        Jedis jedis = connect();
        byte[] pattern = RedisConnection.getKey(PREFIX, "*".getBytes());
        return jedis.keys(pattern);
    }

    /**
     * Delete all users.
     */
    public void flush() {
        Set<byte[]> keys = getAllKeys();
        if (0 < keys.size()) {
            byte[][] keysToDelete = keys.toArray(new byte[keys.size()][]);
            Jedis jedis = connect();
            jedis.del(keysToDelete);
        }
    }

    /**
     * Delete a user from the data "table"
     *
     * @param userId the id of the user to delete
     * @return
     * @throws CloudproofException
     */
    public long deleteUser(long userId) throws CloudproofException {
        byte[] keySuffix = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN).putLong(userId).array();
        byte[] key = RedisConnection.getKey(PREFIX, keySuffix);
        try (Jedis jedis = connect()) {
            return jedis.del(key);
        }
    }

    @Override
    public List<Location> filter(List<Location> locations) throws CloudproofException {
        try (Jedis jedis = connect()) {
            java.util.Iterator<Location> it = locations.iterator();
            while (it.hasNext()) {
                Location location = it.next();
                byte[] key =
                    getKey(PREFIX, Arrays.copyOfRange(location.getBytes(), 0, location.getBytes().length));
                byte[] value = jedis.get(key);
                if (value == null) {
                    it.remove();
                }
            }
            return locations;
        }
    }
}
