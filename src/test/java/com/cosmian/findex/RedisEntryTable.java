package com.cosmian.findex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.cosmian.jna.findex.EntryTableDatabase;
import com.cosmian.jna.findex.serde.Tuple;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

public class RedisEntryTable extends RedisConnection implements EntryTableDatabase {

    static final int PREFIX = 1;

    final static String CONDITIONAL_UPSERT_SCRIPT =
        "local value=redis.call('GET',KEYS[1])\n" +
            "if((value==false) or (not(value == false) and (ARGV[1] == value))) then \n" +
            "  redis.call('SET', KEYS[1], ARGV[2])\n" +
            "  return {} \n" +
            "else \n" +
            "  return {value} \n" +
            "end";

    final byte[] conditionalUpsertSha;

    boolean shouldThrowInsideFetchEntries = false;

    /**
     * Internal constructor instantiating from an existing pool and loading the Conditional Upsert Lua script
     *
     * @param pool the existing {@link JedisPool}
     * @param redisPassword the password to use to authenticate
     */
    RedisEntryTable(JedisPool pool, String redisPassword) {
        this.pool = pool;
        this.redisPassword = redisPassword;
        try (final Jedis jedis = connect()) {
            this.conditionalUpsertSha = jedis.scriptLoad(CONDITIONAL_UPSERT_SCRIPT.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Instantiate a Redis instance from a URI
     *
     * @param uri the URI to the Redis server
     */
    public RedisEntryTable(String uri) {
        this(new JedisPool(uri), redisPassword());
    }

    /**
     * Instantiate a Redis instance from a server hostname and port passed as environment variables
     */
    public RedisEntryTable() {
        this(new JedisPool(redisHostname(), redisPort()), redisPassword());
    }

    /**
     * Instantiate a new Redis Client
     *
     * @param hostname the REST Server URL e.g. localhost
     * @param port Sets a specified port value e.g 6379
     * @param password the authentication password or token
     */
    public RedisEntryTable(String hostname, int port, String password) {
        this(new JedisPool(hostname, port), password);
    }

    /**
     * Return all the keys in the raw format
     */
    protected Set<byte[]> getAllKeys() {
        Jedis jedis = connect();
        byte[] pattern = RedisConnection.getKey(PREFIX, "*".getBytes());
        return jedis.keys(pattern);
    }

    /**
     * Delete all users from the table.
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
     * Format Redis key as 1:HEX_UID.
     *
     * @param uid the {@link Uid32} value of the key
     * @return key as prefix|uid on 4 bytes
     */
    public static byte[] getKey(Uid32 uid) {
        return RedisConnection.getKey(PREFIX, uid.getBytes());
    }

    //
    // Implement all callbacks
    //

    @Override
    public Set<Uid32> fetchAllUids() throws CloudproofException {
        return getAllKeys().stream().map((byte[] b) -> uid(b)).collect(Collectors.toSet());
    }

    @Override
    public List<Tuple<Uid32, EntryTableValue>> fetch(List<Uid32> uids) throws CloudproofException {
        if (shouldThrowInsideFetchEntries) {
            throw new CloudproofException("Should throw inside fetch entries");
        }

        List<byte[]> keys = uids.stream().map((Uid32 uid) -> getKey(uid)).collect(Collectors.toList());
        byte[][] keysArray = keys.toArray(new byte[0][]);

        ArrayList<Tuple<Uid32, EntryTableValue>> keysAndValues = new ArrayList<>();

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
                keysAndValues.add(new Tuple<>(key, new EntryTableValue(value)));
            }
        }
        return keysAndValues;
    }

    @Override
    public Map<Uid32, EntryTableValue> upsert(Map<Uid32, EntryTableValues> uidsAndValues) throws CloudproofException {

        final Map<Uid32, EntryTableValue> rejected = new HashMap<>();
        Jedis jedis = connect();
        for (final Entry<Uid32, EntryTableValues> entry : uidsAndValues.entrySet()) {
            List<byte[]> keys =
                Arrays.asList(RedisConnection.getKey(PREFIX, entry.getKey().getBytes()));
            List<byte[]> args =
                Arrays.asList(entry.getValue().getPrevious().getBytes(),
                    entry.getValue().getNew().getBytes());
            @SuppressWarnings("unchecked")
            List<byte[]> response =
                (List<byte[]>) jedis.evalsha(RedisEntryTable.this.conditionalUpsertSha, keys, args);
            if (response.size() > 0) {
                rejected.put(entry.getKey(), new EntryTableValue(response.get(0)));
            }
        }
        jedis.close();
        return rejected;
    }

    @Override
    public void insert(Map<Uid32, EntryTableValue> uidsAndValues) throws CloudproofException {
        Jedis jedis = connect();
        Transaction tx = jedis.multi();
        for (Entry<Uid32, EntryTableValue> entry : uidsAndValues.entrySet()) {
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
