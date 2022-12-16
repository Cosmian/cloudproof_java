package com.cosmian.findex;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.TestUtils;
import com.cosmian.jna.findex.Findex;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;

public class TestSqlite {

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
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
        byte[] key = IndexUtils.loadKey();
        assertEquals(32, key.length);
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
        // Prepare Sqlite tables and users
        //
        try (Sqlite db = new Sqlite()) {
            db.insertUsers(testFindexDataset);

            //
            // Upsert
            //
            Findex.upsert(key, label, indexedValuesAndWords, db);
            System.out
                .println("After insertion: entry_table size: " + db.getAllKeyValueItems("entry_table").size());
            System.out
                .println("After insertion: chain_table size: " + db.getAllKeyValueItems("chain_table").size());

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
                int[] dbLocations = IndexUtils.indexedValuesBytesListToArray(indexedValuesList);
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
                int[] dbUids = IndexUtils.indexedValuesBytesListToArray(indexedValuesList);
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
                int[] dbUids = IndexUtils.indexedValuesBytesListToArray(indexedValuesList);
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
                int[] dbUids = IndexUtils.indexedValuesBytesListToArray(indexedValuesList);
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

        byte[] key = IndexUtils.loadKey();
        byte[] label = IndexUtils.loadLabel();
        UsersDataset[] datasets = IndexUtils.loadDatasets();
        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(datasets);
        try (Sqlite db = new Sqlite()) {
            for (int i = 0; i < 100; i++) {
                Findex.upsert(key, label, indexedValuesAndWords, db);
            }
        }
        System.out.println("<== successfully performed 100 upserts");
    }

}
