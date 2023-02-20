package com.cosmian.findex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.TestUtils;
import com.cosmian.jna.findex.Findex;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.SearchProgress;
import com.cosmian.jna.findex.ffi.ProgressResults;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.ffi.SearchResults;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.jna.findex.structs.NextKeyword;
import com.cosmian.utils.CloudproofException;

import redis.clients.jedis.Jedis;

public class TestRedis {

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    @Test
    public void testUpsertAndSearchRedis() throws Exception {
        if (TestUtils.portAvailable(Redis.redisHostname(), 6379)) {
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
        Set<Long> expectedDbLocations = IndexUtils.loadExpectedDBLocations();

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
            System.out
                .println("After insertion: data_table size: " + db.getAllKeys(Redis.DATA_TABLE_INDEX).size());

            //
            // Upsert
            //
            Map<Location, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);
            Findex.upsert(new Findex.IndexRequest(key, label, db).add(indexedValuesAndWords));
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
                SearchResults searchResults =
                    Findex.search(
                        key,
                        label,
                        new HashSet<>(Arrays.asList(new Keyword("France"))),
                        db);
                assertEquals(expectedDbLocations, searchResults.getNumbers());
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
                SearchResults searchResults =
                    Findex.search(
                        key,
                        label,
                        new HashSet<>(Arrays.asList(new Keyword("France"))),
                        db);
                assertEquals(0, searchResults.size());
                System.out.println("<== successfully compacted and changed the label");
            }

            {
                // Search with new label and without user changes
                SearchResults searchResults = Findex.search(
                    key,
                    "NewLabel".getBytes(),
                    new HashSet<>(Arrays.asList(new Keyword("France"))),
                    db);
                assertEquals(expectedDbLocations, searchResults.getNumbers());
                System.out.println("<== successfully found all French locations with the new label");
            }

            // Delete the user n°17 to test the compact indexes
            db.deleteUser(17);
            expectedDbLocations.remove(new Long(17));
            Findex.compact(1, key, key, "NewLabel".getBytes(), db);
            {
                // Search should return everyone but n°17
                SearchResults searchResults = Findex.search(
                    key,
                    "NewLabel".getBytes(),
                    new HashSet<>(Arrays.asList(new Keyword("France"))),
                    db);
                assertEquals(expectedDbLocations, searchResults.getNumbers());
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
    public void testGraphUpsertAndSearchRedis() throws Exception {
        if (TestUtils.portAvailable(Redis.redisHostname(), 6379)) {
            System.out.println("Ignore test since Redis is down");
            return;
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
        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);
        // add auto-completion for keywords 'Martin', 'Martena'
        indexedValuesAndWords.put(new IndexedValue(new NextKeyword("Mart")), new HashSet<>(
            Arrays.asList(new Keyword("Mar"))));
        indexedValuesAndWords.put(new IndexedValue(new NextKeyword("Marti")), new HashSet<>(
            Arrays.asList(new Keyword("Mart"))));
        indexedValuesAndWords.put(new IndexedValue(new NextKeyword("Marte")), new HashSet<>(
            Arrays.asList(new Keyword("Mart"))));
        indexedValuesAndWords.put(new IndexedValue(new NextKeyword("Martin")), new HashSet<>(
            Arrays.asList(new Keyword("Marti"))));
        indexedValuesAndWords.put(new IndexedValue(new NextKeyword("Marten")), new HashSet<>(
            Arrays.asList(new Keyword("Marte"))));
        indexedValuesAndWords.put(new IndexedValue(new NextKeyword("Martena")), new HashSet<>(
            Arrays.asList(new Keyword("Marten"))));

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
            System.out.println("Findex Graph Search Redis");
            System.out.println("---------------------------------------");
            System.out.println("");

            {
                Map<Keyword, Set<Location>> searchResults =
                    Findex.search(
                        key,
                        label,
                        new HashSet<>(Arrays.asList(new Keyword("Mar"))),
                        0, -1, 0,
                        db,
                        new SearchProgress() {
                            @Override
                            public boolean notify(ProgressResults results) throws CloudproofException {
                                Map<Keyword, Set<IndexedValue>> indexedValuesByKeywords = results.getResults();
                                Keyword key_marti = new Keyword("Marti");
                                Keyword key_marte = new Keyword("Marte");
                                if (indexedValuesByKeywords.containsKey(key_marti)) {
                                    IndexedValue iv = indexedValuesByKeywords.get(key_marti).iterator().next();
                                    assertEquals(new Keyword("Martin"), iv.getWord());
                                }
                                if (indexedValuesByKeywords.containsKey(key_marte)) {
                                    IndexedValue iv = indexedValuesByKeywords.get(key_marte).iterator().next();
                                    assertEquals(new Keyword("Marten"), iv.getWord());
                                }
                                return true;
                            }
                        });
                System.out.println(
                    "Search results: " + searchResults.size());
                int[] dbLocations = IndexUtils.searchResultsToDbUids(searchResults);
                assertEquals(3, dbLocations.length);
                System.out.println("<== successfully found all original French locations");
            }

            // delete all items
            try (Jedis jedis = db.getJedis()) {
                jedis.flushAll();
            }

        }
    }

}
