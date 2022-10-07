package com.cosmian.findex;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.cosmian.CosmianException;
import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.IndexedValue;
import com.cosmian.jna.findex.Location;
import com.cosmian.jna.findex.Callbacks.FetchAllEntry;
import com.cosmian.jna.findex.Callbacks.FetchChain;
import com.cosmian.jna.findex.Callbacks.FetchEntry;
import com.cosmian.jna.findex.Callbacks.ListRemovedLocations;
import com.cosmian.jna.findex.Callbacks.Progress;
import com.cosmian.jna.findex.Callbacks.UpdateLines;
import com.cosmian.jna.findex.Callbacks.UpsertChain;
import com.cosmian.jna.findex.Callbacks.UpsertEntry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class Redis {

    private static final Logger logger = Logger.getLogger(Redis.class.getName());

    public static final String PREFIX_STORAGE = "cosmian";

    public static final int INDEX_TABLE_DATA_STORAGE = 3;

    public static final int INDEX_TABLE_CHAIN_STORAGE = 2;

    public static final int INDEX_TABLE_ENTRY_STORAGE = 1;

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

    public static Redis create(String uri) {
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

    static String redisPassword() {
        String v = System.getenv("REDIS_PASSWORD");
        return v;
    }

    //
    // Very basic redis functions
    public byte[] get(byte[] key) throws CosmianException {
        return this.jedis.get(key);
    }

    public long del(byte[] key) throws CosmianException {
        return this.jedis.del(key);
    }

    public byte[] set(byte[] key, byte[] value) throws CosmianException {
        return this.jedis.getSet(key, value);
    }

    //
    // Declare all callbacks
    //
    public FetchEntry fetchEntry = new FetchEntry(new com.cosmian.jna.findex.FfiWrapper.FetchEntryInterface() {
        @Override
        public HashMap<byte[], byte[]> fetch(List<byte[]> uids) throws FfiException {
            try {
                return getEntries(uids, INDEX_TABLE_ENTRY_STORAGE);
            } catch (CosmianException e) {
                throw new FfiException("Failed fetch entry: " + e.toString());
            }
        }
    });

    public FetchChain fetchChain = new FetchChain(new com.cosmian.jna.findex.FfiWrapper.FetchChainInterface() {
        @Override
        public HashMap<byte[], byte[]> fetch(List<byte[]> uids) throws FfiException {
            try {
                return getEntries(uids, INDEX_TABLE_CHAIN_STORAGE);
            } catch (CosmianException e) {
                throw new FfiException("Failed chain upsert: " + e.toString());
            }
        }
    });

    public FetchAllEntry fetchAllEntry =
        new FetchAllEntry(new com.cosmian.jna.findex.FfiWrapper.FetchAllEntryInterface() {
            @Override
            public HashMap<byte[], byte[]> fetch() throws FfiException {
                try {
                    return getAllKeysAndValues(INDEX_TABLE_ENTRY_STORAGE);
                } catch (CosmianException e) {
                    throw new FfiException("Failed fetch all entry: " + e.toString());
                }
            }
        });

    public UpsertEntry upsertEntry = new UpsertEntry(new com.cosmian.jna.findex.FfiWrapper.UpsertEntryInterface() {
        @Override
        public void upsert(HashMap<byte[], byte[]> uidsAndValues) throws FfiException {
            try {
                setEntries(uidsAndValues);
            } catch (CosmianException e) {
                throw new FfiException("Failed entry upsert: " + e.toString());
            }
        }
    });

    public UpsertChain upsertChain = new UpsertChain(new com.cosmian.jna.findex.FfiWrapper.UpsertChainInterface() {
        @Override
        public void upsert(HashMap<byte[], byte[]> uidsAndValues) throws FfiException {
            try {
                setChains(uidsAndValues);
            } catch (CosmianException e) {
                throw new FfiException("Failed chain upsert: " + e.toString());
            }
        }
    });

    /// Update the database with the new values. This function should:
    /// - remove all the Index Entry Table
    /// - add `new_encrypted_entry_table_items` to the Index Entry Table
    /// - remove `removed_chain_table_uids` from the Index Chain Table
    /// - add `new_encrypted_chain_table_items` to the Index Chain Table
    ///
    /// The order of these operation is not important but have some implications:
    ///
    /// ### Option 1
    ///
    /// Keep the database small but prevent using the index during the `update_lines`.
    ///
    /// 1. remove all the Index Entry Table
    /// 2. add `new_encrypted_entry_table_items` to the Index Entry Table
    /// 3. remove `removed_chain_table_uids` from the Index Chain Table
    /// 4. add `new_encrypted_chain_table_items` to the Index Chain Table
    ///
    /// ### Option 2
    ///
    /// During a small duration, the index tables are much bigger but users can continue
    /// using the index during the `update_lines`.
    ///
    /// 1. save all UIDs from the current Index Entry Table
    /// 2. add `new_encrypted_entry_table_items` to the Index Entry Table
    /// 3. add `new_encrypted_chain_table_items` to the Index Chain Table
    /// 4. publish new label to users
    /// 5. remove old lines from the Index Entry Table (using the saved UIDs in 1.)
    /// 6. remove `removed_chain_table_uids` from the Index Chain Table
    public UpdateLines updateLines = new UpdateLines(new com.cosmian.jna.findex.FfiWrapper.UpdateLinesInterface() {
        @Override
        public void update(List<byte[]> removedChains, HashMap<byte[], byte[]> newEntries,
            HashMap<byte[], byte[]> newChains) throws FfiException {
            try {
                delAllEntries(INDEX_TABLE_ENTRY_STORAGE);
                setEntries(newEntries);
                setChains(newChains);
                delEntries(removedChains, INDEX_TABLE_CHAIN_STORAGE);
            } catch (CosmianException e) {
                throw new FfiException("Failed update lines: " + e.toString());
            }
        }
    });

    public ListRemovedLocations listRemovedLocations =
        new ListRemovedLocations(new com.cosmian.jna.findex.FfiWrapper.ListRemovedLocationsInterface() {
            @Override
            public List<Location> list(List<Location> locations) throws FfiException {
                List<Integer> ids =
                    locations.stream().map((Location location) -> ByteBuffer.wrap(location.getBytes()).getInt())
                        .collect(Collectors.toList());

                try {
                    return listRemovedIds(ids).stream()
                        .map((Integer id) -> new Location(ByteBuffer.allocate(32).putInt(id).array()))
                        .collect(Collectors.toList());
                } catch (CosmianException e) {
                    throw new FfiException("Failed update lines: " + e.toString());
                }

            }
        });

    public Progress progress = new Progress(new com.cosmian.jna.findex.FfiWrapper.ProgressInterface() {
        @Override
        public boolean list(List<byte[]> indexedValues) throws FfiException {

            try {
                //
                // Convert indexed values from bytes
                //
                List<IndexedValue> indexedValuesBytes = new ArrayList<>();
                for (byte[] iv : indexedValues) {
                    indexedValuesBytes.add(new IndexedValue(iv));
                }
                return true;
            } catch (CosmianException e) {
                throw new FfiException("Failed getting search results: " + e.toString());
            }

        }
    });

    public List<Integer> listRemovedIds(List<Integer> ids) throws CosmianException {
        HashSet<Integer> removedIds = new HashSet<>(ids);
        for (Integer id : ids) {
            byte[] keySuffix = ByteBuffer.allocate(4).putInt(id).array();
            byte[] value = getDataEntry(keySuffix);
            if (value != null) {
                removedIds.remove(id);
            }
        }
        return new LinkedList<>(removedIds);
    }

    /**
     * Format Redis key as NUMBER:HEX_UID
     *
     * @param prefix table prefix (can be 1, 2 or 3)
     * @param number the index of the table
     * @param uid which is the UID of
     * @return key as prefix|number on 4 bytes|uid
     */
    public static byte[] key(String prefix, int number, byte[] uid) {
        byte[] prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
        byte[] numberBytes = ByteBuffer.allocate(4).putInt(number).array();
        byte[] result = Arrays.copyOf(prefixBytes, prefixBytes.length + numberBytes.length + uid.length);
        System.arraycopy(numberBytes, 0, result, prefixBytes.length, numberBytes.length);
        System.arraycopy(uid, 0, result, prefixBytes.length + numberBytes.length, uid.length);
        return result;
    }

    public HashMap<byte[], byte[]> getEntries(List<byte[]> uids, int redisPrefix) throws CosmianException {
        Transaction tx = this.jedis.multi();
        List<byte[]> keys = new ArrayList<>();
        for (byte[] uid : uids) {
            byte[] key = key(PREFIX_STORAGE, redisPrefix, uid);
            keys.add(key);
        }
        byte[][] keysArray = keys.toArray(new byte[0][]);
        Response<List<byte[]>> mgetResults = tx.mget(keysArray);
        tx.exec();

        HashMap<byte[], byte[]> keysAndValues = new HashMap<>();
        for (int i = 0; i < mgetResults.get().size(); i++) {
            byte[] key = uids.get(i);
            byte[] value = mgetResults.get().get(i);
            if (value != null) {
                keysAndValues.put(key, value);
            }
        }
        return keysAndValues;
    }

    public void setEntries(HashMap<byte[], byte[]> uidsAndValues) throws CosmianException {
        Transaction tx = this.jedis.multi();
        for (Entry<byte[], byte[]> entry : uidsAndValues.entrySet()) {
            byte[] key = key(PREFIX_STORAGE, INDEX_TABLE_ENTRY_STORAGE, entry.getKey());
            tx.getSet(key, entry.getValue());
        }
        tx.exec();
    }

    public void setChains(HashMap<byte[], byte[]> uidsAndValues) throws CosmianException {
        Transaction tx = this.jedis.multi();
        for (Entry<byte[], byte[]> entry : uidsAndValues.entrySet()) {
            byte[] key = key(PREFIX_STORAGE, INDEX_TABLE_CHAIN_STORAGE, entry.getKey());
            tx.getSet(key, entry.getValue());
        }
        tx.exec();
    }

    public byte[] getDataEntry(byte[] uid) throws CosmianException {
        byte[] key = key(PREFIX_STORAGE, INDEX_TABLE_DATA_STORAGE, uid);
        return get(key);
    }

    public byte[] setDataEntry(byte[] uid, byte[] value) throws CosmianException {
        byte[] key = key(PREFIX_STORAGE, INDEX_TABLE_DATA_STORAGE, uid);
        return set(key, value);
    }

    public long delDataEntry(byte[] uid) throws CosmianException {
        byte[] key = key(PREFIX_STORAGE, INDEX_TABLE_DATA_STORAGE, uid);
        return del(key);
    }

    public List<byte[]> getAllKeys(int redisIndex) throws CosmianException {
        List<byte[]> keys = new ArrayList<>();
        Transaction tx = this.jedis.multi();
        byte[] key = key(PREFIX_STORAGE, redisIndex, "*".getBytes());
        Response<Set<byte[]>> responses = tx.keys(key);
        tx.exec();
        if (responses.get() == null) {
            throw new CosmianException("Failed to get Redis items");
        }
        for (byte[] keyIter : responses.get()) {
            keys.add(keyIter);
        }
        return keys;
    }

    public HashMap<byte[], byte[]> getAllKeysAndValues(int redisIndex) throws CosmianException {
        List<byte[]> keys = getAllKeys(redisIndex);

        // Get all values now
        byte[][] keysArray = keys.toArray(new byte[0][]);
        Transaction tx2 = this.jedis.multi();
        Response<List<byte[]>> mgetResults = tx2.mget(keysArray);
        tx2.exec();

        HashMap<byte[], byte[]> keysAndValues = new HashMap<>();
        for (int i = 0; i < mgetResults.get().size(); i++) {
            // byte[] key = uids.get(i);
            byte[] value = mgetResults.get().get(i);
            if (value != null) {
                keysAndValues.put(keys.get(i), value);
            }
        }
        return keysAndValues;
    }

    public void delEntries(List<byte[]> uids, int redisPrefix) throws CosmianException {
        for (byte[] uid : uids) {
            byte[] key = key(PREFIX_STORAGE, redisPrefix, uid);
            this.jedis.del(key);
        }
    }

    public void delAllEntries(int redisPrefix) throws CosmianException {
        List<byte[]> keys = getAllKeys(redisPrefix);
        for (byte[] key : keys) {
            this.jedis.del(key);
        }
    }

    public void insertUsers(UsersDataset[] testFindexDataset) throws CosmianException {
        for (UsersDataset user : testFindexDataset) {
            String json = user.toString();
            byte[] keySuffix = ByteBuffer.allocate(4).putInt(user.id).array();
            setDataEntry(keySuffix, json.getBytes());
        }
    }

    public long deleteUser(int uid) throws CosmianException {
        byte[] keySuffix = ByteBuffer.allocate(4).putInt(uid).array();
        byte[] key = key(PREFIX_STORAGE, INDEX_TABLE_DATA_STORAGE, keySuffix);
        return del(key);
    }

}