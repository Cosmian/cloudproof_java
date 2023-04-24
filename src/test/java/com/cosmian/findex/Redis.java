package com.cosmian.findex;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.cosmian.jna.findex.Database;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

public class Redis extends Database implements Closeable {

    private final static String CONDITIONAL_UPSERT_SCRIPT =
        "local value=redis.call('GET',KEYS[1])\n" +
            "if((value==false) or (not(value == false) and (ARGV[1] == value))) then \n" +
            "  redis.call('SET', KEYS[1], ARGV[2])\n" +
            "  return {} \n" +
            "else \n" +
            "  return {value} \n" +
            "end";

    private static final Logger logger = Logger.getLogger(Redis.class.getName());

    public static final byte[] STORAGE_PREFIX = "cosmian".getBytes(StandardCharsets.UTF_8);

    public static final int DATA_TABLE_INDEX = 3;

    public static final int CHAIN_TABLE_INDEX = 2;

    public static final int ENTRY_TABLE_INDEX = 1;

    private final JedisPool pool;

    private final byte[] conditionalUpsertSha;

    private final String redisPassword;

    public boolean shouldThrowInsideFetchEntries = false;

    // public final Jedis jedis;

    /**
     * Internal constructor instantiating from an existing pool and loading the Conditional Upsert Lua script
     * 
     * @param pool the existing {@link JedisPool}
     * @param redisPassword the password to use to authenticate
     */
    protected Redis(JedisPool pool, String redisPassword) {
        this.pool = pool;
        this.redisPassword = redisPassword;
        try (final Jedis jedis = getJedis()) {
            this.conditionalUpsertSha = jedis.scriptLoad(CONDITIONAL_UPSERT_SCRIPT.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Instantiate a Redis instance from a URI
     * 
     * @param uri the URI to the Redis server
     */
    public Redis(String uri) {
        this(new JedisPool(uri), redisPassword());
    }

    /**
     * Instantiate a Redis instance from a server hostname and port passed as environment variables
     */
    public Redis() {
        this(new JedisPool(redisHostname(), redisPort()), redisPassword());
    }

    /**
     * Instantiate a new Redis Client
     *
     * @param hostname the REST Server URL e.g. localhost
     * @param port Sets a specified port value e.g 6379
     * @param password the authentication password or token
     */
    public Redis(String hostname, int port, String password) {
        this(new JedisPool(hostname, port), password);
    }

    /**
     * Get a Jedis connection from the pool, authenticating it if needed
     * 
     * @return the {@link Jedis} connection
     */
    protected Jedis getJedis() {
        Jedis jedis = pool.getResource();
        if (redisPassword() != null) {
            jedis.auth(this.redisPassword);
        }
        return jedis;
    }

    /**
     * The Redis server hostname from the REDIS_HOSTNAME environment variable. Defaults to localhost if not found.
     * 
     * @return the hostname
     */
    static String redisHostname() {
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
    static int redisPort() {
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
     * Format Redis key as NUMBER:HEX_UID
     *
     * @param number the index of the table
     * @param uid the {@link Uid32} value of the key
     * @return key as prefix|number on 4 bytes|uid
     */
    public static byte[] key(int number,
                             byte[] uid) {
        byte[] numberBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(number).array();
        byte[] result = Arrays.copyOf(STORAGE_PREFIX, STORAGE_PREFIX.length + numberBytes.length + uid.length);
        System.arraycopy(numberBytes, 0, result, STORAGE_PREFIX.length, numberBytes.length);
        System.arraycopy(uid, 0, result, STORAGE_PREFIX.length + numberBytes.length, uid.length);
        return result;
    }

    /**
     * Convert a Redis key back to an {@link Uid32}
     * 
     * @param key the Redis key
     * @return the {@link Uid32}
     */
    public static Uid32 uid(byte[] key) {
        return new Uid32(Arrays.copyOfRange(key, STORAGE_PREFIX.length + 4, key.length));
    }

    /**
     * Retrieve multiple raw values from a list of Uids in a particular "table"
     * 
     * @param uids the {@link Uid32} to retrieve
     * @param redisPrefix the "table"prefix
     * @return the list of raw values
     */
    protected List<byte[]> getEntries(List<Uid32> uids,
                                      int redisPrefix) {

        try (Jedis jedis = getJedis()) {
            List<byte[]> keys = new ArrayList<>();
            for (Uid32 uid : uids) {
                byte[] key = key(redisPrefix, uid.getBytes());
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
    protected Set<byte[]> getAllKeys(int redisTableIndex) throws CloudproofException {
        byte[] pattern = key(redisTableIndex, "*".getBytes());
        try (Jedis jedis = getJedis()) {
            return jedis.keys(pattern);
        }
    }

    /**
     * Delete all entries in the given "table"
     * 
     * @param redisTableIndex the "table" index
     * @throws CloudproofException
     */
    public void delAllEntries(int redisTableIndex) throws CloudproofException {
        Set<byte[]> keys = getAllKeys(redisTableIndex);
        try (Jedis jedis = getJedis()) {
            jedis.del(keys.toArray(new byte[keys.size()][]));
        }
    }

    /**
     * Insert all the users in the data "table"
     * 
     * @param testFindexDataset the dataset containing the user records
     * @throws CloudproofException
     */
    public void insertUsers(UsersDataset[] testFindexDataset) throws CloudproofException {
        try (Jedis jedis = getJedis()) {
            for (UsersDataset user : testFindexDataset) {
                byte[] keySuffix = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(user.id).array();
                byte[] key = key(DATA_TABLE_INDEX, keySuffix);
                byte[] value = user.toString().getBytes(StandardCharsets.UTF_8);
                jedis.set(key, value);
            }
        }
    }

    /**
     * Delete a user from the data "table"
     * 
     * @param userId the id of the user to delete
     * @return
     * @throws CloudproofException
     */
    public long deleteUser(int userId) throws CloudproofException {
        byte[] keySuffix = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(userId).array();
        byte[] key = key(DATA_TABLE_INDEX, keySuffix);
        try (Jedis jedis = getJedis()) {
            return jedis.del(key);
        }
    }

    //
    // Implement all callbacks
    //

    @Override
    public void close() throws IOException {
        this.pool.close();
    }

    @Override
    protected Set<Uid32> fetchAllEntryTableUids() throws CloudproofException {
        return getAllKeys(ENTRY_TABLE_INDEX).stream().map((byte[] b) -> uid(b))
            .collect(Collectors.toSet());
    }

    @Override
    protected Map<Uid32, EntryTableValue> fetchEntries(List<Uid32> uids) throws CloudproofException {
        if (shouldThrowInsideFetchEntries) {
            throw new CloudproofException("Should throw inside fetch entries");
        }

        List<byte[]> values = getEntries(uids, ENTRY_TABLE_INDEX);
        // post process
        HashMap<Uid32, EntryTableValue> keysAndValues = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            Uid32 key = uids.get(i);
            byte[] value = values.get(i);
            if (value != null) {
                keysAndValues.put(key, new EntryTableValue(value));
            }
        }
        return keysAndValues;
    }

    @Override
    protected Map<Uid32, ChainTableValue> fetchChains(List<Uid32> uids) throws CloudproofException {
        List<byte[]> response = getEntries(uids, CHAIN_TABLE_INDEX);

        HashMap<Uid32, ChainTableValue> keysAndValues = new HashMap<>();
        for (int i = 0; i < response.size(); i++) {
            Uid32 key = uids.get(i);
            byte[] value = response.get(i);
            if (value != null) {
                keysAndValues.put(key, new ChainTableValue(value));
            }
        }
        return keysAndValues;
    }

    @Override
    protected Map<Uid32, EntryTableValue> upsertEntries(Map<Uid32, EntryTableValues> uidsAndValues)
        throws CloudproofException {
        final Map<Uid32, EntryTableValue> failed = new HashMap<>();
        try (Jedis jedis = getJedis()) {
            for (final Entry<Uid32, EntryTableValues> entry : uidsAndValues.entrySet()) {
                List<byte[]> keys =
                    Arrays.asList(key(ENTRY_TABLE_INDEX, entry.getKey().getBytes()));
                List<byte[]> args =
                    Arrays.asList(entry.getValue().getPrevious().getBytes(),
                        entry.getValue().getNew().getBytes());
                @SuppressWarnings("unchecked")
                List<byte[]> response =
                    (List<byte[]>) jedis.evalsha(Redis.this.conditionalUpsertSha, keys, args);
                if (response.size() > 0) {
                    failed.put(entry.getKey(), new EntryTableValue(response.get(0)));
                }
            }
        }
        return failed;
    }

    @Override
    protected void upsertChains(Map<Uid32, ChainTableValue> uidsAndValues) throws CloudproofException {
        try (Jedis jedis = getJedis(); Transaction tx = jedis.multi();) {
            for (Entry<Uid32, ChainTableValue> entry : uidsAndValues.entrySet()) {
                byte[] key = key(CHAIN_TABLE_INDEX, entry.getKey().getBytes());
                tx.getSet(key, entry.getValue().getBytes());
            }
            tx.exec();
        }
    }

    @Override
    protected void updateTables(List<Uid32> removedChains,
                                Map<Uid32, EntryTableValue> newEntries,
                                Map<Uid32, ChainTableValue> newChains)
        throws CloudproofException {
        // truncate the EN
        delAllEntries(ENTRY_TABLE_INDEX);

        try (Jedis jedis = getJedis()) {
            // set the new Entries
            for (Entry<Uid32, EntryTableValue> newEntry : newEntries.entrySet()) {
                byte[] key = key(ENTRY_TABLE_INDEX, newEntry.getKey().getBytes());
                jedis.set(key, newEntry.getValue().getBytes());
            }
            // set (upsert) the new chains
            for (Entry<Uid32, ChainTableValue> newChain : newChains.entrySet()) {
                byte[] key = key(CHAIN_TABLE_INDEX, newChain.getKey().getBytes());
                jedis.set(key, newChain.getValue().getBytes());
            }
            // clean up the Chain Table
            for (Uid32 uid : removedChains) {
                byte[] key = key(CHAIN_TABLE_INDEX, uid.getBytes());
                jedis.del(key);
            }
        }
    }

    @Override
    protected List<Location> listRemovedLocations(List<Location> locations) throws CloudproofException {
        try (Jedis jedis = getJedis()) {
            Iterator<Location> it = locations.iterator();
            while (it.hasNext()) {
                Location location = it.next();
                byte[] key = key(DATA_TABLE_INDEX, Arrays.copyOfRange(location.getBytes(), 0, 4));
                byte[] value = jedis.get(key);
                if (value != null) {
                    it.remove();

                }
            }
            return locations;
        }
    }

    @Override
    protected boolean searchProgress(List<IndexedValue> indexedValues) throws CloudproofException {
        // let the search progress
        logger.fine("progress called with " + indexedValues.size() + " values");
        return true;
    }

}
