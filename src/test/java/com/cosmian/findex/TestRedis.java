package com.cosmian.findex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.TestUtils;
import com.cosmian.jna.findex.Findex;
import com.cosmian.jna.findex.Interrupt;
import com.cosmian.jna.findex.ffi.KeywordSet;
import com.cosmian.jna.findex.ffi.SearchResults;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.utils.CloudproofException;

import redis.clients.jedis.Jedis;

public class TestRedis {

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    @Test
    public void testUpsertAndSearchRedis() throws Exception {
        if (TestUtils.portAvailable(RedisEntryTable.redisHostname(), 6379)) {
            throw new RuntimeException("Redis is down");
        }

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Upsert Redis");
        System.out.println("---------------------------------------");
        System.out.println("");

        //
        // Recover key and label
        //
        byte[] key = IndexUtils.generateKey();
        assertEquals(16, key.length);
        byte[] label = IndexUtils.loadLabel();

        //
        // Recover test vectors
        //
        Set<Long> expectedDbLocations = IndexUtils.loadExpectedDBLocations();

        //
        // Build dataset with DB uids and words
        //
        UsersDataset[] testFindexDataset = IndexUtils.loadDatasets();

        //
        // Prepare Redis tables and users
        //
        try (RedisUserDb db = new RedisUserDb();
            RedisEntryTable entryTable = new RedisEntryTable();
            RedisChainTable chainTable = new RedisChainTable();) {
            db.flush();
            entryTable.flush();
            chainTable.flush();

            db.insertUsers(testFindexDataset);
            System.out.println("After insertion: data_table size: " + chainTable.getAllKeys().size());

            Findex findex = new Findex(key, label, 1, entryTable, chainTable);

            //
            // Upsert
            //
            Map<IndexedValue, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);
            KeywordSet res = findex.add(indexedValuesAndWords);
            int entryTableLength = entryTable.getAllKeys().size();
            int chainTableLength = chainTable.getAllKeys().size();
            assertEquals(583, res.getResults().size(), "wrong number of new upserted keywords");
            assertEquals(583, entryTableLength, "wrong Entry Table length");
            assertEquals(618, chainTableLength, "wrong Entry Table length");
            System.out.println("After insertion: entry_table size: " + entryTableLength);
            System.out.println("After insertion: chain_table size: " + chainTableLength);

            //
            // Upsert a new keyword
            //
            HashMap<IndexedValue, Set<Keyword>> newIndexedKeyword = new HashMap<>();
            Set<Keyword> expectedKeywords = new HashSet<>();
            expectedKeywords.add(new Keyword("test"));
            newIndexedKeyword.put(new IndexedValue(new Location("ici")), expectedKeywords);
            // It is returned the first time it is added.
            Set<Keyword> newKeywords = findex.add(newIndexedKeyword).getResults();
            assertEquals(expectedKeywords, newKeywords, "new keyword is not returned");
            // It is *not* returned the second time it is added.
            newKeywords = findex.add(newIndexedKeyword).getResults();
            assert (newKeywords.isEmpty());

            //
            // Search
            //
            System.out.println("");
            System.out.println("---------------------------------------");
            System.out.println("Findex Search Redis");
            System.out.println("---------------------------------------");
            System.out.println("");

            {
                SearchResults searchResults = findex.search(new String[] {"France"});
                assertEquals(expectedDbLocations, searchResults.getNumbers());
                System.out.println("<== successfully found all original French locations");
            }

            // This compact should do nothing except changing the label since the users
            // table didn't change.
            findex.compact(key, "NewLabel".getBytes(), 1);
            System.out
                .println("After first compact: entry_table size: " + entryTable.getAllKeys().size());
            System.out
                .println("After first compact: chain_table size: " + chainTable.getAllKeys().size());

            {
                // Search with new label and without user changes
                SearchResults searchResults = findex.search(new String[] {"France"});
                assertEquals(expectedDbLocations, searchResults.getNumbers());
                System.out.println("<== successfully found all French locations with the new label");
            }

            //
            // Compact
            //
            System.out.println("");
            System.out.println("---------------------------------------");
            System.out.println("Findex Re-Compact Sqlite");
            System.out.println("---------------------------------------");
            System.out.println("");

            // Delete the user n°17 to test the compact indexes
            db.deleteUser(17);
            expectedDbLocations.remove(new Long(17));
            findex.compact(key, "NewLabel2".getBytes(), 1, db);
            {
                // Search should return everyone but n°17
                SearchResults searchResults = findex.search(new String[] {"France"});
                assertEquals(expectedDbLocations, searchResults.getNumbers());
                System.out
                    .println("<== successfully found all French locations after removing one and compacting");
            }
        }
    }

    @Test
    public void testExceptions() throws Exception {
        if (TestUtils.portAvailable(RedisEntryTable.redisHostname(), 6379)) {
            throw new RuntimeException("Redis is down");
        }

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Exceptions in Redis");
        System.out.println("---------------------------------------");
        System.out.println("");

        //
        // Recover key and label
        //
        byte[] key = IndexUtils.generateKey();
        assertEquals(16, key.length);
        byte[] label = IndexUtils.loadLabel();

        //
        // Build dataset with DB uids and words
        //
        UsersDataset[] testFindexDataset = IndexUtils.loadDatasets();

        //
        // Prepare Redis tables and users
        //
        try (RedisUserDb db = new RedisUserDb();
            RedisEntryTable entryTable = new RedisEntryTable();
            RedisChainTable chainTable = new RedisChainTable();) {

            db.flush();
            entryTable.flush();
            chainTable.flush();

            db.insertUsers(testFindexDataset);

            Findex findex = new Findex(key, label, 1, entryTable, chainTable);

            Map<IndexedValue, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);
            findex.add(indexedValuesAndWords);

            entryTable.shouldThrowInsideFetchEntries = true;

            try {
                findex.search(new String[] {"John"});
            } catch (CloudproofException e) {
                assertEquals("Should throw inside fetch entries", e.getMessage());
                return;
            }

            throw new Exception("Should have throw");
        }
    }

    @Test
    public void testGraphUpsertAndSearchRedis() throws Exception {
        if (TestUtils.portAvailable(RedisEntryTable.redisHostname(), 6379)) {
            throw new RuntimeException("Redis is down");
        }

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Graph Upsert Redis");
        System.out.println("---------------------------------------");
        System.out.println("");

        //
        // Recover key and label
        //
        byte[] key = IndexUtils.generateKey();
        assertEquals(16, key.length);
        byte[] label = IndexUtils.loadLabel();

        //
        // Build dataset with DB uids and words
        //
        UsersDataset[] testFindexDataset = IndexUtils.loadDatasets();
        Map<IndexedValue, Set<Keyword>> additions = IndexUtils.index(testFindexDataset);
        // add auto-completion for keywords 'Martin', 'Martena'
        additions.put(new Keyword("Mart").toIndexedValue(), new HashSet<>(
            Arrays.asList(new Keyword("Mar"))));
        additions.put(new Keyword("Marti").toIndexedValue(), new HashSet<>(
            Arrays.asList(new Keyword("Mart"))));
        additions.put(new Keyword("Marte").toIndexedValue(), new HashSet<>(
            Arrays.asList(new Keyword("Mart"))));
        additions.put(new Keyword("Martin").toIndexedValue(), new HashSet<>(
            Arrays.asList(new Keyword("Marti"))));
        additions.put(new Keyword("Marten").toIndexedValue(), new HashSet<>(
            Arrays.asList(new Keyword("Marte"))));
        additions.put(new Keyword("Martena").toIndexedValue(), new HashSet<>(
            Arrays.asList(new Keyword("Marten"))));

        //
        // Prepare Redis tables and users
        //
        try (RedisUserDb db = new RedisUserDb();
            RedisEntryTable entryTable = new RedisEntryTable();
            RedisChainTable chainTable = new RedisChainTable();) {

            db.flush();
            entryTable.flush();
            chainTable.flush();

            db.insertUsers(testFindexDataset);
            System.out.println("After insertion: data_table size: " + db.getAllKeys().size());

            Findex findex = new Findex(key, label, 1, entryTable, chainTable);

            //
            // Upsert
            //
            findex.add(additions);
            System.out.println("After insertion: entry_table size: " + entryTable.getAllKeys().size());
            System.out.println("After insertion: chain_table size: " + chainTable.getAllKeys().size());

            //
            // Search
            //
            System.out.println("");
            System.out.println("---------------------------------------");
            System.out.println("Findex Graph Search Redis");
            System.out.println("---------------------------------------");
            System.out.println("");

            {
                SearchResults searchResults = findex.search(new String[] {"Mar"}, new Interrupt() {
                    @Override
                    public boolean interrupt(Map<Keyword, Set<IndexedValue>> results) throws CloudproofException {
                        Keyword key_marti = new Keyword("Marti");
                        Keyword key_marte = new Keyword("Marte");
                        if (results.containsKey(key_marti)) {
                            IndexedValue iv = results.get(key_marti).iterator().next();
                            assertEquals(new Keyword("Martin"), iv.getKeyword());
                        }
                        if (results.containsKey(key_marte)) {
                            IndexedValue iv = results.get(key_marte).iterator().next();
                            assertEquals(new Keyword("Marten"), iv.getKeyword());
                        }
                        return true;
                    }
                });
                assertEquals(3, searchResults.numberOfUniqueLocations());
                System.out.println("<== successfully found all original French locations");
            }

            // delete all items
            try (Jedis jedis = db.connect()) {
                jedis.flushAll();
            }

        }
    }

}
