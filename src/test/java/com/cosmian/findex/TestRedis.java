package com.cosmian.findex;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.TestUtils;
import com.cosmian.jna.findex.Findex;
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
        if (TestUtils.portAvailable(6379)) {
            System.out.println("Ignore test since Redis is down");
            return;
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
        int[] expectedDbLocations = IndexUtils.loadExpectedDBLocations();

        //
        // Build dataset with DB uids and words
        //
        UsersDataset[] testFindexDataset = IndexUtils.loadDatasets();
        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);

        //
        // Prepare Redis tables and users
        //
        try (Redis db = new Redis()) {
            // delete all items
            try (Jedis jedis = db.getJedis()) {
                jedis.flushAll();
            }
            db.insertUsers(testFindexDataset);
            System.out
                .println("After insertion: data_table size: " + db.getAllKeys(Redis.DATA_TABLE_INDEX).size());

            //
            // Upsert
            //
            Findex.upsert(key, label, indexedValuesAndWords, db);
            System.out
                .println("After insertion: entry_table size: " + db.getAllKeys(Redis.ENTRY_TABLE_INDEX).size());
            System.out
                .println("After insertion: chain_table size: " + db.getAllKeys(Redis.CHAIN_TABLE_INDEX).size());

            //
            // Search
            //
            System.out.println("");
            System.out.println("---------------------------------------");
            System.out.println("Findex Search Redis");
            System.out.println("---------------------------------------");
            System.out.println("");

            {
                Map<Keyword, Set<Location>> searchResults =
                    Findex.search(
                        key,
                        label,
                        new HashSet<>(Arrays.asList(new Keyword("France"))),
                        db);
                System.out.println(
                    "Search results: " + searchResults.size() + " " + searchResults.values().iterator().next().size());
                int[] dbLocations = IndexUtils.searchResultsToDbUids(searchResults);
                assertEquals(expectedDbLocations.length, dbLocations.length);
                assertArrayEquals(expectedDbLocations, dbLocations);
                System.out.println("<== successfully found all original French locations");
            }

            // This compact should do nothing except changing the label since the users
            // table didn't change.
            Findex.compact(1, key, key, "NewLabel".getBytes(), db);
            System.out
                .println("After first compact: entry_table size: " + db.getAllKeys(Redis.ENTRY_TABLE_INDEX).size());
            System.out
                .println("After first compact: chain_table size: " + db.getAllKeys(Redis.CHAIN_TABLE_INDEX).size());

            {
                // Search with old label
                Map<Keyword, Set<Location>> searchResults =
                    Findex.search(
                        key,
                        label,
                        new HashSet<>(Arrays.asList(new Keyword("France"))),
                        db);
                int[] dbUids = IndexUtils.searchResultsToDbUids(searchResults);
                assertEquals(0, dbUids.length);
                System.out.println("<== successfully compacted and changed the label");
            }

            {
                // Search with new label and without user changes
                Map<Keyword, Set<Location>> searchResults = Findex.search(
                    key,
                    "NewLabel".getBytes(),
                    new HashSet<>(Arrays.asList(new Keyword("France"))),
                    db);
                int[] dbUids = IndexUtils.searchResultsToDbUids(searchResults);
                assertArrayEquals(expectedDbLocations, dbUids);
                System.out.println("<== successfully found all French locations with the new label");
            }

            // Delete the user n°17 to test the compact indexes
            db.deleteUser(17);
            int[] newExpectedDbUids = ArrayUtils.removeElement(expectedDbLocations, 17);
            Findex.compact(1, key, key, "NewLabel".getBytes(), db);
            {
                // Search should return everyone but n°17
                Map<Keyword, Set<Location>> searchResults = Findex.search(
                    key,
                    "NewLabel".getBytes(),
                    new HashSet<>(Arrays.asList(new Keyword("France"))),
                    db);
                int[] dbUids = IndexUtils.searchResultsToDbUids(searchResults);
                assertArrayEquals(newExpectedDbUids, dbUids);
                System.out
                    .println("<== successfully found all French locations after removing one and compacting");
            }

            // delete all items
            try (Jedis jedis = db.getJedis()) {
                jedis.flushAll();
            }

        }
    }

    @Test
    public void testExceptions() throws Exception {
        if (TestUtils.portAvailable(6379)) {
            System.out.println("Ignore test since Redis is down");
            return;
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
        try (Redis db = new Redis()) {
            // delete all items
            try (Jedis jedis = db.getJedis()) {
                jedis.flushAll();
            }
            db.insertUsers(testFindexDataset);

            Map<IndexedValue, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);
            Findex.upsert(key, label, indexedValuesAndWords, db);

            db.shouldThrowInsideFetchEntries = true;

            try {
                Findex.search(
                    key,
                    "NewLabel".getBytes(),
                    new HashSet<>(Arrays.asList(new Keyword("France"))),
                    db);
            } catch (CloudproofException e) {
                assertEquals("Should throw inside fetch entries", e.getMessage());
                return;
            }

            throw new Exception("Should have throw");
        }
    }

}
