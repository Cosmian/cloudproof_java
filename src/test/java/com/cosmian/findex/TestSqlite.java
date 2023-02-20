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
import com.cosmian.jna.findex.ffi.SearchResults;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.utils.Resources;

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
        // Generate key and label
        //
        byte[] key = IndexUtils.loadKey();
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
        // Prepare Sqlite tables and users
        //
        try (Sqlite db = new Sqlite()) {
            db.insertUsers(testFindexDataset);

            //
            // Upsert
            //
            Map<Location, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);
            Findex.upsert(new Findex.IndexRequest(key, label, db).add(indexedValuesAndWords));
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
        Map<Location, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(datasets);
        try (Sqlite db = new Sqlite()) {
            for (int i = 0; i < 100; i++) {
                Findex.upsert(new Findex.IndexRequest(key, label, db).add(indexedValuesAndWords));
            }
        }
        System.out.println("<== successfully performed 100 upserts");
    }

    void verify(byte[] key,
                byte[] label,
                HashMap<Location, Set<Keyword>> indexedValuesAndWords,
                String dbPath,
                Set<Long> expectedDbLocations)
        throws Exception {
        Sqlite db = new Sqlite(dbPath);
        int initialEntryTableSize = db.getAllKeyValueItems("entry_table").size();
        int initialChainTableSize = db.getAllKeyValueItems("chain_table").size();
        System.out
            .println("Before insertion: entry_table size: " + initialEntryTableSize);
        System.out
            .println("Before insertion: chain_table size: " + initialChainTableSize);

        //
        // Search
        //
        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Search Sqlite in " + dbPath);
        System.out.println("---------------------------------------");
        System.out.println("");

        {
            SearchResults searchResults =
                Findex.search(
                    key,
                    label,
                    new HashSet<>(Arrays.asList(new Keyword("France"))),
                    -1, -1, db);
            assertEquals(expectedDbLocations, searchResults.getNumbers());
            System.out.println("<== successfully found all original French locations");
        }

        //
        // Upsert
        //
        UsersDataset[] users = UsersDataset.fromJson(Resources.load_resource("findex/single_user.json"));
        Map<Location, Set<Keyword>> singleUserIndexedValuesAndWords = IndexUtils.index(users);
        Findex.upsert(new Findex.IndexRequest(key, label, db).add(singleUserIndexedValuesAndWords));

        Set<Long> newExpectedDbLocations = new HashSet<>(expectedDbLocations);
        for (UsersDataset user : users) {
            newExpectedDbLocations.add(user.id);
        }

        int currentEntryTableSize = db.getAllKeyValueItems("entry_table").size();
        int currentChainTableSize = db.getAllKeyValueItems("chain_table").size();
        System.out
            .println("After insertion: entry_table size: " + currentEntryTableSize);
        System.out
            .println("After insertion: chain_table size: " + currentChainTableSize);
        assertEquals(initialEntryTableSize + 6, currentEntryTableSize);
        assertEquals(initialChainTableSize + 8, currentChainTableSize);

        //
        // Search
        //
        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Search Sqlite");
        System.out.println("---------------------------------------");
        System.out.println("");

        {
            SearchResults searchResults =
                Findex.search(
                    key,
                    label,
                    new HashSet<>(Arrays.asList(new Keyword("France"))),
                    -1, -1, db);
            assertEquals(newExpectedDbLocations, searchResults.getNumbers());
        }
    }

    @Test
    public void test_non_regression_vectors() throws Exception {
        //
        // Recover key and label
        //
        byte[] key = IndexUtils.loadKey();
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
        HashMap<Location, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);

        //
        // Browse all sqlite.db and check them
        //
        String testFolder = "src/test/resources/findex/non_regression";
        for (String file : TestUtils.listFiles(testFolder)) {
            String fullPath = testFolder + "/" + file;
            String newPath = System.getProperty("java.io.tmpdir") + "/" + file;
            java.nio.file.Files.copy(
                new java.io.File(fullPath).toPath(),
                new java.io.File(newPath).toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
                java.nio.file.LinkOption.NOFOLLOW_LINKS);
            System.out.println("Non-regression test file: " + newPath);
            verify(key, label, indexedValuesAndWords, newPath, expectedDbLocations);
        }
    }

    @Test
    public void test_generate_non_regression_vectors() throws Exception {
        //
        // Recover key and label
        //
        byte[] key = IndexUtils.loadKey();
        assertEquals(16, key.length);
        byte[] label = IndexUtils.loadLabel();

        //
        // Build dataset with DB uids and words
        //
        UsersDataset[] testFindexDataset = IndexUtils.loadDatasets();
        Map<Location, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);

        //
        // Generate non regression sqlite - uncomment if needed
        //
        //
        // Upsert
        //
        Findex.upsert(new Findex.IndexRequest(key, label, new Sqlite("./target/sqlite.db")).add(indexedValuesAndWords));
    }
}
