package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.findex.Redis;
import com.cosmian.findex.Sqlite;
import com.cosmian.findex.UsersDataset;
import com.cosmian.jna.findex.Findex;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Resources;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestNativeFindex {

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    public byte[] hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] passHash = sha256.digest(data);
        return passHash;
    }

    /**
     * Index the given Datasets
     * 
     * @param testFindexDataset the list of {@link UsersDataset}
     * @return the clear text index
     */
    private HashMap<IndexedValue, Set<Keyword>> index(UsersDataset[] testFindexDataset) {

        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = new HashMap<>();
        Set<Keyword> keywords = new HashSet<>();
        List<Integer> originalLocationIds = new ArrayList<>();
        for (UsersDataset user : testFindexDataset) {
            originalLocationIds.add(user.id);
            ByteBuffer buf = ByteBuffer.allocate(32);
            buf.order(ByteOrder.BIG_ENDIAN);
            buf.putInt(user.id);
            byte[] dbUid = buf.array();
            indexedValuesAndWords.put(new Location(dbUid).toIndexedValue(), user.values());
            keywords.addAll(user.values());
        }

        // stats
        System.out.println("Num keywords: " + keywords.size() + ", indexed Values: " + indexedValuesAndWords.size());
        return indexedValuesAndWords;
    }

    private byte[] loadKey() throws IOException {
        return Base64.getDecoder().decode(Resources.load_resource("findex/key.b64"));
    }

    private byte[] loadLabel() throws IOException {
        return Resources.load_resource_as_bytes("findex/label");
    }

    private UsersDataset[] loadDatasets() throws IOException, CloudproofException {
        String dataJson = Resources.load_resource("findex/data.json");
        return UsersDataset.fromJson(dataJson);
    }

    private int[] loadExpectedDBLocations() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String expectedSearchResultsInt = Resources.load_resource("findex/expected_db_uids.json");
        int[] expectedDbLocations = mapper.readValue(expectedSearchResultsInt, int[].class);
        Arrays.sort(expectedDbLocations);

        return expectedDbLocations;
    }

    @Test
    public void testUpsertAndSearchSqlite() throws Exception {
        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Upsert Sqlite");
        System.out.println("---------------------------------------");
        System.out.println("");

        //
        // Recover key and label
        //
        byte[] key = loadKey();
        assertEquals(32, key.length);
        byte[] label = loadLabel();

        //
        // Recover test vectors
        //
        int[] expectedDbLocations = loadExpectedDBLocations();

        //
        // Build dataset with DB uids and words
        //
        UsersDataset[] testFindexDataset = loadDatasets();
        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = index(testFindexDataset);

        //
        // Prepare Sqlite tables and users
        //
        try (Sqlite db = new Sqlite()) {
            db.insertUsers(testFindexDataset);

            //
            // Upsert
            //
            Findex.upsert(key, label, indexedValuesAndWords, db);
            System.out
                .println("After insertion: entry_table: nb indexes: " + db.getAllKeyValueItems("entry_table").size());
            System.out
                .println("After insertion: chain_table: nb indexes: " + db.getAllKeyValueItems("chain_table").size());

            //
            // Search
            //
            System.out.println("");
            System.out.println("---------------------------------------");
            System.out.println("Findex Search Sqlite");
            System.out.println("---------------------------------------");
            System.out.println("");

            {
                List<IndexedValue> indexedValuesList =
                    Findex.search(
                        key,
                        label,
                        new HashSet<>(Arrays.asList(new Keyword("France"))),
                        0, -1, db);
                int[] dbLocations = indexedValuesBytesListToArray(indexedValuesList);
                assertEquals(expectedDbLocations.length, dbLocations.length);
                assertArrayEquals(expectedDbLocations, dbLocations);
                System.out.println("<== successfully found all original French locations");
            }

            // This compact should do nothing except changing the label since the users
            // table didn't change.
            Findex.compact(1, key, key, "NewLabel".getBytes(), db);
            {
                // Search with old label
                List<IndexedValue> indexedValuesList =
                    Findex.search(
                        key,
                        label,
                        new HashSet<>(Arrays.asList(new Keyword("France"))),
                        0, -1, db);
                int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);
                assertEquals(0, dbUids.length);
                System.out.println("<== successfully compacted and changed the label");
            }

            {
                // Search with new label and without user changes
                List<IndexedValue> indexedValuesList = Findex.search(
                    key,
                    "NewLabel".getBytes(),
                    new HashSet<>(Arrays.asList(new Keyword("France"))),
                    0, -1, db);
                int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);
                assertArrayEquals(expectedDbLocations, dbUids);
                System.out.println("<== successfully found all French locations with the new label");
            }

            // Delete the user n°17 to test the compact indexes
            db.deleteUser(17);
            int[] newExpectedDbUids = ArrayUtils.removeElement(expectedDbLocations, 17);
            Findex.compact(1, key, key, "NewLabel".getBytes(), db);
            {
                // Search should return everyone but n°17
                List<IndexedValue> indexedValuesList = Findex.search(
                    key,
                    "NewLabel".getBytes(),
                    new HashSet<>(Arrays.asList(new Keyword("France"))),
                    0, -1, db);
                int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);
                assertArrayEquals(newExpectedDbUids, dbUids);
                System.out
                    .println("<== successfully found all French locations after removing one and compacting");
            }
        }
    }

    /**
     * Check allocation problem during insertions. Allocation problem could occur when fetching entry table /* values
     * whose sizes depend on words being indexed: the Entry Table Encrypted value is: `EncSym(𝐾value, (ict_uid𝑥𝑤𝑖,
     * 𝐾𝑤𝑖 , 𝑤𝑖))`
     */
    @Test
    public void testCheckAllocations() throws Exception {

        byte[] key = loadKey();
        byte[] label = loadLabel();
        UsersDataset[] datasets = loadDatasets();
        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = index(datasets);
        try (Sqlite db = new Sqlite()) {
            for (int i = 0; i < 100; i++) {
                Findex.upsert(key, label, indexedValuesAndWords, db);
            }
        }
        System.out.println("<== successfully performed 100 upserts");
    }

    // private void verifyFindexSearch(MasterKeys masterKeys,
    // byte[] label,
    // String word,
    // Redis db,
    // int expectedResultsNumber)
    // throws Exception {
    // List<byte[]> indexedValuesList = Findex.search(masterKeys.getK(), label, new Word[] {new Word(word)}, 0, -1,
    // db.progress, db.fetchEntry, db.fetchChain);
    // int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);

    // assertEquals(expectedResultsNumber, dbUids.length);
    // }

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
        byte[] key = loadKey();
        assertEquals(32, key.length);
        byte[] label = loadLabel();

        //
        // Recover test vectors
        //
        int[] expectedDbLocations = loadExpectedDBLocations();

        //
        // Build dataset with DB uids and words
        //
        UsersDataset[] testFindexDataset = loadDatasets();
        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = index(testFindexDataset);

        //
        // Prepare Redis tables and users
        //
        try (Redis db = new Redis()) {
            db.jedis.flushAll();
            db.insertUsers(testFindexDataset);

            //
            // Upsert
            //
            Findex.upsert(key, label, indexedValuesAndWords, db);
            System.out
                .println("After insertion: entry_table: nb indexes: " + db.getAllKeys(Redis.ENTRY_TABLE_INDEX).size());
            System.out
                .println("After insertion: chain_table: nb indexes: " + db.getAllKeys(Redis.CHAIN_TABLE_INDEX).size());

            // //
            // // Upsert
            // //
            // Findex.graph_upsert(masterKeys, label, indexedValuesAndWords, db.fetchEntry,
            // db.upsertEntry, db.upsertChain);
            // System.out.println("After insertion: entry_table: nb indexes: "
            // + db.getAllKeysAndValues(Redis.INDEX_TABLE_ENTRY_STORAGE).size());
            // System.out.println("After insertion: chain_table: nb indexes: "
            // + db.getAllKeysAndValues(Redis.INDEX_TABLE_CHAIN_STORAGE).size());

            // //
            // // Search
            // //
            // System.out.println("");
            // System.out.println("---------------------------------------");
            // System.out.println("Findex Search Redis");
            // System.out.println("---------------------------------------");
            // System.out.println("");

            // {
            // List<byte[]> indexedValuesList = Findex.search(masterKeys.getK(), label, new
            // Word[] { new Word("France") },
            // 0,
            // -1, db.progress, db.fetchEntry, db.fetchChain);
            // int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);

            // assertArrayEquals(expectedDbUids, dbUids);
            // }

            // // With graph upsertions, we can search from 3-letters words
            // verifyFindexSearch(masterKeys, label, "fel", db, 2);
            // verifyFindexSearch(masterKeys, label, "Fel", db, 3);
            // verifyFindexSearch(masterKeys, label, "Fra", db, 30);
            // verifyFindexSearch(masterKeys, label, "Kia", db, 1);
            // verifyFindexSearch(masterKeys, label, "vit", db, 2); // 2 emails starting
            // with `vit`

            // // This compact should do nothing except changing the label since the users
            // // table didn't change.
            // Findex.compact(1, masterKeys, "NewLabel".getBytes(), db.fetchEntry,
            // db.fetchChain, db.fetchAllEntry,
            // db.updateLines, db.listRemovedLocations);

            // verifyFindexSearch(masterKeys, label, "France", db, 0);

            // {
            // // Search with new label and without user changes

            // List<byte[]> indexedValuesList = Findex.search(masterKeys.getK(),
            // "NewLabel".getBytes(),
            // new Word[] { new Word("France") }, 0, -1, db.progress, db.fetchEntry,
            // db.fetchChain);
            // int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);

            // assertArrayEquals(expectedDbUids, dbUids);
            // }

            // // Delete the user n°17 to test the compact indexes
            // db.deleteUser(17);
            // int[] newExpectedDbUids = ArrayUtils.removeElement(expectedDbUids, 17);

            // Findex.compact(1, masterKeys, "NewLabel".getBytes(), db.fetchEntry,
            // db.fetchChain, db.fetchAllEntry,
            // db.updateLines, db.listRemovedLocations);

            // {
            // // Search should return everyone instead of n°17

            // List<byte[]> indexedValuesList = Findex.search(masterKeys.getK(),
            // "NewLabel".getBytes(),
            // new Word[] { new Word("France") }, 0, -1, db.progress, db.fetchEntry,
            // db.fetchChain);
            // int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);

            // assertArrayEquals(newExpectedDbUids, dbUids);
            // }

            // // Check allocation problem during insertions. Allocation problem could occur
            // // when fetching entry table
            // // values
            // // whose sizes depend on words being indexed: the Entry Table Encrypted value
            // // is:
            // // `EncSym(𝐾value, (ict_uid𝑥𝑤𝑖, 𝐾𝑤𝑖 , 𝑤𝑖))`
            // for (int i = 0; i < 100; i++) {
            // Findex.upsert(masterKeys, label, indexedValuesAndWords, db.fetchEntry,
            // db.upsertEntry, db.upsertChain);
            // List<byte[]> indexedValuesList = Findex.search(masterKeys.getK(), label, new
            // Word[] { new Word("France") },
            // 0,
            // -1, db.progress, db.fetchEntry, db.fetchChain);
            // int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);
            // assertArrayEquals(expectedDbUids, dbUids);
            // }
        }
    }

    /*
     * Helper function to transform the list of bytes returned by the FFI (representing `IndexedValue`) to a sorted
     * array of int (representing the DB id of users).
     */
    private int[] indexedValuesBytesListToArray(List<IndexedValue> indexedValuesList) throws Exception {
        int[] dbLocations = new int[indexedValuesList.size()];
        int count = 0;
        for (IndexedValue iv : indexedValuesList) {
            byte[] location = iv.getLocation().getBytes();
            ByteBuffer buf = ByteBuffer.wrap(location);
            buf.order(ByteOrder.BIG_ENDIAN);
            int dbLocation = buf.getInt();
            dbLocations[count] = dbLocation;
            count++;
        }
        Arrays.sort(dbLocations);
        return dbLocations;
    }

}
