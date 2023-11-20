package com.cosmian.findex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.cosmian.jna.findex.ChainTableDatabase;
import com.cosmian.jna.findex.serde.Tuple;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

public class RedisChainTable extends RedisConnection implements ChainTableDatabase {

    static final int PREFIX = 2;

    /**
     * Internal constructor instantiating from an existing pool and loading the Conditional Upsert Lua script
     *
     * @param pool the existing {@link JedisPool}
     * @param redisPassword the password to use to authenticate
     */
    RedisChainTable(JedisPool pool, String redisPassword) {
        this.pool = pool;
        this.redisPassword = redisPassword;
    }

    /**
     * Instantiate a Redis instance from a URI
     *
     * @param uri the URI to the Redis server
     */
    public RedisChainTable(String uri) {
        this(new JedisPool(uri), redisPassword());
    }

    /**
     * Instantiate a Redis instance from a server hostname and port passed as environment variables
     */
    public RedisChainTable() {
        this(new JedisPool(redisHostname(), redisPort()), redisPassword());
    }

    /**
     * Instantiate a new Redis Client
     *
     * @param hostname the REST Server URL e.g. localhost
     * @param port Sets a specified port value e.g 6379
     * @param password the authentication password or token
     */
    public RedisChainTable(String hostname, int port, String password) {
        this(new JedisPool(hostname, port), password);
    }

    /**
     * Retrieve multiple raw values from a list of Uids in a particular "table"
     *
     * @param uids the {@link Uid32} to retrieve
     * @param redisPrefix the "table"prefix
     * @return the list of raw values
     */
    protected List<byte[]> get(List<Uid32> uids) {
        try (Jedis jedis = connect()) {
            List<byte[]> keys = new ArrayList<>();
            for (Uid32 uid : uids) {
                byte[] key = getKey(uid);
                keys.add(key);
            }
            byte[][] keysArray = keys.toArray(new byte[0][]);
            List<byte[]> mgetResults = jedis.mget(keysArray);
            return mgetResults;
        }
    }

    /**
     * Return all the keys in the raw format
     */
    public Set<byte[]> getAllKeys() throws CloudproofException {
        byte[] pattern = RedisConnection.getKey(PREFIX, "*".getBytes());
        try (Jedis jedis = connect()) {
            return jedis.keys(pattern);
        }
    }

    /**
     * Delete all entries in the given "table"
     *
     * @param redisTableIndex the "table" index
     * @throws CloudproofException
     */
    public void flush() throws CloudproofException {
        Set<byte[]> keys = getAllKeys();
        if (0 < keys.size()) {
            byte[][] keysToDelete = keys.toArray(new byte[keys.size()][]);
            Jedis jedis = connect();
            jedis.del(keysToDelete);
        }
    }

    public static byte[] getKey(Uid32 uid) {
        return RedisConnection.getKey(PREFIX, uid.getBytes());
    }

    //
    // Implement all callbacks
    //

    @Override
    public List<Tuple<Uid32, ChainTableValue>> fetch(List<Uid32> uids) throws CloudproofException {
        List<byte[]> keys = uids.stream().map((Uid32 uid) -> getKey(uid)).collect(Collectors.toList());
        byte[][] keysArray = keys.toArray(new byte[0][]);

        ArrayList<Tuple<Uid32, ChainTableValue>> keysAndValues = new ArrayList<>();

        if (0 == keys.size()) {
            return keysAndValues;
        }

        Jedis jedis = connect();
        List<byte[]> values = jedis.mget(keysArray);
        jedis.close();

        for (int i = 0; i < values.size(); i++) {
            Uid32 key = uids.get(i);
            byte[] value = values.get(i);
            if (value != null) {
                keysAndValues.add(new Tuple<>(key, new ChainTableValue(value)));
            }
        }
        return keysAndValues;
    }

    @Override
    public void insert(Map<Uid32, ChainTableValue> uidsAndValues) throws CloudproofException {
        Jedis jedis = connect();
        Transaction tx = jedis.multi();
        for (Entry<Uid32, ChainTableValue> entry : uidsAndValues.entrySet()) {
            byte[] key = RedisConnection.getKey(PREFIX, entry.getKey().getBytes());
            tx.getSet(key, entry.getValue().getBytes());
        }
        tx.exec();
        jedis.close();
    }

    @Override
    public void delete(List<Uid32> uids) throws CloudproofException {
        Jedis jedis = connect();
        for (Uid32 uid : uids) {
            jedis.del(getKey(uid));
        }
        jedis.close();
    }
}
