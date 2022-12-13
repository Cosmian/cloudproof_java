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
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchAllEntries;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchChain;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchEntry;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBListRemovedLocations;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBUpdateLines;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBUpsertChain;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBUpsertEntry;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.SearchProgress;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class Redis extends Database implements Closeable {

    private final static byte[] CONDITIONAL_UPSERT_SCRIPT =
        ("local value=redis.call('GET',KEYS[1])\n" +
            "if((value==false) or (not(value == false) and (ARGV[1] == value))) then \n" +
            "  redis.call('SET', KEYS[1], ARGV[2])\n" +
            "  return {} \n" +
            "else \n" +
            "  return {value} \n" +
            "end").getBytes(StandardCharsets.UTF_8);

    private static final Logger logger = Logger.getLogger(Redis.class.getName());

    public static final String PREFIX_STORAGE = "cosmian";

    public static final int DATA_TABLE_INDEX = 3;

    public static final int CHAIN_TABLE_INDEX = 2;

    public static final int ENTRY_TABLE_INDEX = 1;

    private final JedisPool pool;

    public final Jedis jedis;

    //
    // Constructors
    //
    public Redis(String uri) {
        this.pool = new JedisPool(uri);
        this.jedis = this.pool.getResource();

    }

    public Redis() {
        this.pool = new JedisPool(redisHostname(), redisPort());
        this.jedis = this.pool.getResource();
        if (redisPassword() != null)
            jedis.auth(redisPassword());
    }

    /**
     * Instantiate a new Redis Client
     *
     * @param hostname the REST Server URL e.g. localhost
     * @param port Sets a specified port value e.g 6379
     * @param password the authentication password or token
     */
    public Redis(String hostname, int port, String password) {
        this.pool = new JedisPool(hostname, port);
        this.jedis = this.pool.getResource();
        if (!password.isEmpty())
            jedis.auth(password);
    }

    static Redis create(String uri) {
        return new Redis(uri);
    }

    static String redisHostname() {
        String v = System.getenv("REDIS_HOSTNAME");
        if (v == null) {
            return "localhost";
        }
        return v;
    }

    static int redisPort() {
        String v = System.getenv("REDIS_PORT");
        if (v == null) {
            return 6379;
        }
        return Integer.parseInt(v);
    }

    public static String redisPassword() {
        String v = System.getenv("REDIS_PASSWORD");
        return v;
    }

    //
    // Declare all callbacks
    //

    /**
     * Format Redis key as NUMBER:HEX_UID
     *
     * @param prefix table prefix (can be 1, 2 or 3)
     * @param number the index of the table
     * @param uid which is the UID of
     * @return key as prefix|number on 4 bytes|uid
     */
    public static byte[] key(String prefix,
                             int number,
                             byte[] uid) {
        byte[] prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
        byte[] numberBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(number).array();
        byte[] result = Arrays.copyOf(prefixBytes, prefixBytes.length + numberBytes.length + uid.length);
        System.arraycopy(numberBytes, 0, result, prefixBytes.length, numberBytes.length);
        System.arraycopy(uid, 0, result, prefixBytes.length + numberBytes.length, uid.length);
        return result;
    }

    public Response<List<byte[]>> getEntries(List<Uid32> uids,
                                             int redisPrefix)
        throws CloudproofException {
        Transaction tx = this.jedis.multi();
        List<byte[]> keys = new ArrayList<>();
        for (Uid32 uid : uids) {
            byte[] key = key(PREFIX_STORAGE, redisPrefix, uid.getBytes());
            keys.add(key);
        }
        byte[][] keysArray = keys.toArray(new byte[0][]);
        Response<List<byte[]>> mgetResults = tx.mget(keysArray);
        tx.exec();
        return mgetResults;
    }

    public void setEntries(HashMap<byte[], byte[]> uidsAndValues) throws CloudproofException {
        Transaction tx = this.jedis.multi();
        for (Entry<byte[], byte[]> entry : uidsAndValues.entrySet()) {
            byte[] key = key(PREFIX_STORAGE, ENTRY_TABLE_INDEX, entry.getKey());
            tx.getSet(key, entry.getValue());
        }
        tx.exec();
    }

    public List<byte[]> getAllKeys(int redisTableIndex) throws CloudproofException {
        List<byte[]> keys = new ArrayList<>();
        Transaction tx = this.jedis.multi();
        byte[] key = key(PREFIX_STORAGE, redisTableIndex, "*".getBytes());
        Response<Set<byte[]>> responses = tx.keys(key);
        tx.exec();
        if (responses.get() == null) {
            throw new CloudproofException("Failed to get all Redis items from table " + redisTableIndex);
        }
        for (byte[] keyIter : responses.get()) {
            keys.add(keyIter);
        }
        return keys;
    }

    public void delEntries(List<byte[]> uids,
                           int redisPrefix)
        throws CloudproofException {
        for (byte[] uid : uids) {
            byte[] key = key(PREFIX_STORAGE, redisPrefix, uid);
            this.jedis.del(key);
        }
    }

    public void delAllEntries(int redisPrefix) throws CloudproofException {
        List<byte[]> keys = getAllKeys(redisPrefix);
        for (byte[] key : keys) {
            this.jedis.del(key);
        }
    }

    public void insertUsers(UsersDataset[] testFindexDataset) throws CloudproofException {
        for (UsersDataset user : testFindexDataset) {
            byte[] keySuffix = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(user.id).array();
            byte[] key = key(PREFIX_STORAGE, DATA_TABLE_INDEX, keySuffix);
            byte[] value = user.toString().getBytes(StandardCharsets.UTF_8);
            this.jedis.set(key, value);
        }
    }

    public long deleteUser(int uid) throws CloudproofException {
        byte[] keySuffix = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(uid).array();
        byte[] key = key(PREFIX_STORAGE, DATA_TABLE_INDEX, keySuffix);
        return this.jedis.del(key);
    }

    @Override
    protected DBFetchAllEntries fetchAllEntries() {
        return new DBFetchAllEntries() {

            @Override
            public Map<Uid32, EntryTableValue> fetch() throws CloudproofException {
                List<byte[]> keys = getAllKeys(ENTRY_TABLE_INDEX);

                // Get all values now
                byte[][] keysArray = keys.toArray(new byte[0][]);
                Transaction tx2 = Redis.this.jedis.multi();
                Response<List<byte[]>> mgetResults = tx2.mget(keysArray);
                tx2.exec();

                HashMap<Uid32, EntryTableValue> keysAndValues = new HashMap<>();
                for (int i = 0; i < mgetResults.get().size(); i++) {
                    byte[] value = mgetResults.get().get(i);
                    if (value != null) {
                        keysAndValues.put(new Uid32(keys.get(i)), new EntryTableValue(value));
                    }
                }
                return keysAndValues;
            }

        };
    }

    @Override
    protected DBFetchChain fetchChain() {
        return new DBFetchChain() {

            @Override
            public Map<Uid32, ChainTableValue> fetch(List<Uid32> uids) throws CloudproofException {
                Response<List<byte[]>> response = getEntries(uids, CHAIN_TABLE_INDEX);
                HashMap<Uid32, ChainTableValue> keysAndValues = new HashMap<>();
                for (int i = 0; i < response.get().size(); i++) {
                    Uid32 key = uids.get(i);
                    byte[] value = response.get().get(i);
                    if (value != null) {
                        keysAndValues.put(key, new ChainTableValue(value));
                    }
                }
                return keysAndValues;
            }

        };
    }

    @Override
    protected DBFetchEntry fetchEntry() {
        return new DBFetchEntry() {

            @Override
            public Map<Uid32, EntryTableValue> fetch(List<Uid32> uids) throws CloudproofException {
                Response<List<byte[]>> response = getEntries(uids, ENTRY_TABLE_INDEX);
                HashMap<Uid32, EntryTableValue> keysAndValues = new HashMap<>();
                for (int i = 0; i < response.get().size(); i++) {
                    Uid32 key = uids.get(i);
                    byte[] value = response.get().get(i);
                    if (value != null) {
                        keysAndValues.put(key, new EntryTableValue(value));
                    }
                }
                return keysAndValues;
            }

        };
    }

    @Override
    protected DBListRemovedLocations listRemovedLocations() {
        return new DBListRemovedLocations() {

            @Override
            public List<Location> list(List<Location> locations) throws CloudproofException {
                Iterator<Location> it = locations.iterator();
                while (it.hasNext()) {
                    Location location = it.next();
                    byte[] key = key(PREFIX_STORAGE, DATA_TABLE_INDEX, location.getBytes());
                    byte[] value = Redis.this.jedis.get(key);
                    if (value != null) {
                        it.remove();
                    }
                }
                return locations;
            }
        };
    }

    @Override
    protected SearchProgress progress() {
        return new SearchProgress() {

            @Override
            public boolean notify(List<IndexedValue> indexedValues) throws CloudproofException {
                // You may want to do something here such as User feedback
                List<Integer> ids =
                    indexedValues.stream().map((IndexedValue iv) -> {
                        try {
                            return ByteBuffer.wrap(iv.getLocation().getBytes())
                                .order(ByteOrder.BIG_ENDIAN).getInt();
                        } catch (CloudproofException e) {
                            return -1;
                        }
                    }).collect(Collectors.toList());
                logger.info("Progress: " + ids.toString());
                return true;
            }

        };
    }

    @Override
    protected DBUpdateLines updateLines() {
        return new DBUpdateLines() {

            @Override
            public void update(List<Uid32> removedChains,
                               Map<Uid32, EntryTableValue> newEntries,
                               Map<Uid32, ChainTableValue> newChains)
                throws CloudproofException {

                // truncate the EN
                delAllEntries(ENTRY_TABLE_INDEX);
                // set the new Entries
                for (Entry<Uid32, EntryTableValue> newEntry : newEntries.entrySet()) {
                    byte[] key = key(PREFIX_STORAGE, ENTRY_TABLE_INDEX, newEntry.getKey().getBytes());
                    Redis.this.jedis.set(key, newEntry.getValue().getBytes());
                }
                // set (upsert) the new chains
                for (Entry<Uid32, ChainTableValue> newChain : newChains.entrySet()) {
                    byte[] key = key(PREFIX_STORAGE, CHAIN_TABLE_INDEX, newChain.getKey().getBytes());
                    Redis.this.jedis.set(key, newChain.getValue().getBytes());
                }
                // clean up the Chain Table
                for (Uid32 uid : removedChains) {
                    byte[] key = key(PREFIX_STORAGE, CHAIN_TABLE_INDEX, uid.getBytes());
                    Redis.this.jedis.del(key);
                }

            }

        };
    }

    @Override
    protected DBUpsertChain upsertChain() {
        return new DBUpsertChain() {

            @Override
            public void upsert(Map<Uid32, ChainTableValue> uidsAndValues) throws CloudproofException {
                try (Transaction tx = Redis.this.jedis.multi();) {
                    for (Entry<Uid32, ChainTableValue> entry : uidsAndValues.entrySet()) {
                        byte[] key = key(PREFIX_STORAGE, CHAIN_TABLE_INDEX, entry.getKey().getBytes());
                        tx.getSet(key, entry.getValue().getBytes());
                    }
                    tx.exec();
                }
            }
        };
    }

    @Override
    protected DBUpsertEntry upsertEntry() {
        return new DBUpsertEntry() {

            @Override
            public Map<Uid32, EntryTableValue> upsert(Map<Uid32, EntryTableValues> uidsAndValues)
                throws CloudproofException {
                final Map<Uid32, EntryTableValue> failed = new HashMap<>();
                for (final Entry<Uid32, EntryTableValues> entry : uidsAndValues.entrySet()) {
                    List<byte[]> keys =
                        Arrays.asList(key(PREFIX_STORAGE, CHAIN_TABLE_INDEX, entry.getKey().getBytes()));
                    List<byte[]> args =
                        Arrays.asList(entry.getValue().getPrevious().getBytes(), entry.getValue().getNew().getBytes());
                    @SuppressWarnings("unchecked")
                    List<byte[]> response =
                        (List<byte[]>) Redis.this.jedis.eval(CONDITIONAL_UPSERT_SCRIPT, keys, args);
                    if (response.size() > 0) {
                        failed.put(entry.getKey(), new EntryTableValue(response.get(0)));
                    }
                }
                return failed;
            }
        };
    }

    @Override
    public void close() throws IOException {
        this.jedis.close();
    }

}
