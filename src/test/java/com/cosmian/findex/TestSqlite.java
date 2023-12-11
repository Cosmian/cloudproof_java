package com.cosmian.findex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.TestUtils;
import com.cosmian.jna.findex.DataFilter;
import com.cosmian.jna.findex.Findex;
import com.cosmian.jna.findex.ffi.SearchResults;
import com.cosmian.jna.findex.ffi.KeywordSet;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Resources;

public class TestSqlite {

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    public static HashMap<IndexedValue, Set<Keyword>> mapToIndex(String word, int userId) {
        Set<Keyword> keywords = new HashSet<>(Arrays.asList(new Keyword(word)));

        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = new HashMap<>();
        indexedValuesAndWords.put(new Location(userId).toIndexedValue(), keywords);
        return indexedValuesAndWords;
    }

    public static void printMap(String tableName,
                                Map<byte[], byte[]> map) {
        System.out.println(tableName + " size: " + map.size());
        for (Map.Entry<byte[], byte[]> entry : map.entrySet()) {
            System.out.println(tableName + ": uid: " + Base64.getEncoder().encodeToString(entry.getKey()) + " value: "
                + Base64.getEncoder().encodeToString(entry.getValue()));
        }
    }

    @Test
    public void testMultiFetchEntryValues() throws Exception {
        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Multi Fetch Entries");
        System.out.println("---------------------------------------");
        System.out.println("");

        //
        // Generate key and label
        //
        SqliteEntryTable entryTable1 = new SqliteEntryTable("target/entry_table_1");
        SqliteEntryTable entryTable2 = new SqliteEntryTable("target/entry_table_2");
        SqliteChainTable chainTable = new SqliteChainTable("target/chain_table");

        entryTable1.flush();
        entryTable2.flush();
        chainTable.flush();

        MultiSqlite compositeEntryTable = new MultiSqlite(Arrays.asList( entryTable1, entryTable2 ));

        byte[] key = IndexUtils.loadKey();
        String label = IndexUtils.loadLabel();

        Findex findex = new Findex(key, label, 2, compositeEntryTable, chainTable);

        compositeEntryTable.selectTable(0);
        findex.add(mapToIndex("John", 1));

        compositeEntryTable.selectTable(1);
        findex.add(mapToIndex("John", 2));

        long entryTable1Length = entryTable1.fetchAllUids().size();
        long entryTable2Length = entryTable2.fetchAllUids().size();
        long chainTableLength = chainTable.fetchAllUids().size();
        System.out.println("Entry Table 1 length: " + entryTable1Length);
        System.out.println("Entry Table 2 length: " + entryTable2Length);
        System.out.println("Chain Table length: " + chainTableLength);
        assertEquals(1, entryTable1Length);
        assertEquals(1, entryTable2Length);
        assertEquals(2, chainTableLength);

        //
        // Search
        //
        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Search Sqlite through multi entry tables");
        System.out.println("---------------------------------------");
        System.out.println("");

        System.out.println("Searching with multiple entries values");
        Set<Keyword> keywords = new HashSet<>(Arrays.asList(new Keyword("John")));
        SearchResults searchResults = findex.search(keywords);
        assertEquals(searchResults.getNumbers(), new HashSet<>(Arrays.asList(1L, 2L)));
        System.out.println("<== successfully found all original French locations");
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
        String label = IndexUtils.loadLabel();

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
        try (SqliteEntryTable entryTable = new SqliteEntryTable();
             SqliteChainTable chainTable = new SqliteChainTable();) {

            entryTable.flush();
            chainTable.flush();

            Findex findex = new Findex(key, label, entryTable, chainTable);

            //
            // Upsert
            //
            Map<IndexedValue, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);
            KeywordSet res = findex.add(indexedValuesAndWords);
            int entryTableSize = entryTable.fetchAllUids().size();
            int chainTableSize = chainTable.fetchAllUids().size();
            assertEquals(583, res.getResults().size(), "wrong number of new upserted keywords");
            assertEquals(583, entryTableSize, "invalid entry table items number");
            assertEquals(618, chainTableSize, "invalid chain table items number");
            System.out.println("Upserted " + res.getResults().size() + " new keywords.");
            System.out.println("After insertion: entry_table size: " + entryTableSize);
            System.out.println("After insertion: chain_table size: " + chainTableSize);

            //
            // Upsert a new keyword
            //
            HashMap<IndexedValue, Set<Keyword>> newIndexedKeyword = new HashMap<>();
            Set<Keyword> expectedKeywords = new HashSet<>();
            expectedKeywords.add(new Keyword("test"));
            newIndexedKeyword.put(new IndexedValue(new Location(1)), expectedKeywords);
            // It is returned the first time it is added.
            Set<Keyword> newKeywords = findex.add(newIndexedKeyword).getResults();
            assertEquals(expectedKeywords, newKeywords, "new keyword is not returned");
            // It is *not* returned the second time it is added.
            newKeywords = findex.add(newIndexedKeyword).getResults();
            assert (newKeywords.isEmpty());

            System.out.println("");
            System.out.println("---------------------------------------");
            System.out.println("Findex Search Sqlite");
            System.out.println("---------------------------------------");
            System.out.println("");

            {
                SearchResults searchResults = findex.search(new String[] { "France"});
                assertEquals(expectedDbLocations, searchResults.getNumbers());
                System.out.println("<== successfully found all original French locations");
            }

            System.out.println("");
            System.out.println("---------------------------------------");
            System.out.println("Findex Compact Sqlite");
            System.out.println("---------------------------------------");
            System.out.println("");

            // This compact should do nothing except changing the label since the users
            // table didn't change.
            entryTableSize = entryTable.fetchAllUids().size();
            chainTableSize = chainTable.fetchAllUids().size();
            System.out.println("Before first compact: entry_table size: " + entryTableSize);
            System.out.println("Before first compact: chain_table size: " + chainTableSize);

            findex.compact(key, "NewLabel");

            entryTableSize = entryTable.fetchAllUids().size();
            chainTableSize = chainTable.fetchAllUids().size();
            System.out.println("After first compact: entry_table size: " + entryTableSize);
            System.out.println("After first compact: chain_table size: " + chainTableSize);
            assertEquals(584, entryTableSize, "invalid entry table items number");
            assertEquals(619, chainTableSize, "invalid chain table items number");

            {
                SearchResults searchResults = findex.search(new String[] { "France"});
                assertEquals(expectedDbLocations, searchResults.getNumbers());
                System.out.println("<== successfully found all French locations with the new label");
            }

            System.out.println("");
            System.out.println("---------------------------------------");
            System.out.println("Findex Re-Compact Sqlite");
            System.out.println("---------------------------------------");
            System.out.println("");

            // Delete the user n°17 to test the compact indexedValuesAndWords
            Set<Location> filteredLocations = new HashSet<Location>(Arrays.asList( new Location(17)));
            expectedDbLocations.remove(17l);

            entryTableSize = entryTable.fetchAllUids().size();
            chainTableSize = chainTable.fetchAllUids().size();
            System.out.println("Before 2nd compact: entry_table size: " + entryTableSize);
            System.out.println("Before 2nd compact: chain_table size: " + chainTableSize);


            findex.compact(key, "NewLabel2", new DataFilter() {
                    @Override
                    public List<Location> filter(List<Location> locations)
                    throws CloudproofException {
                        return locations.stream().filter((Location location) -> !filteredLocations.contains(location))
                                .collect(Collectors.toList());
                    }
            });

            {
                // Search should return everyone but n°17
                SearchResults searchResults = findex.search(new String[] { "France"});
                assertEquals(expectedDbLocations, searchResults.getNumbers());
                System.out.println("<== successfully found all French locations after removing one and compacting");
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
        String label = IndexUtils.loadLabel();
        UsersDataset[] datasets = IndexUtils.loadDatasets();
        Map<IndexedValue, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(datasets);
        try (SqliteUserDb db = new SqliteUserDb();
                SqliteEntryTable entryTable = new SqliteEntryTable();
                SqliteChainTable chainTable = new SqliteChainTable();) {
            Findex findex = new Findex(key, label, entryTable, chainTable);
            for (int i = 0; i < 100; i++) {
                findex.add(indexedValuesAndWords);
            }
        }
        System.out.println("<== successfully performed 100 upserts");
    }

    void verify(byte[] key,
                String label,
                HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords,
                String dbPath,
                Set<Long> expectedDbLocations)
        throws Exception {

        try (SqliteUserDb db = new SqliteUserDb(dbPath);
                SqliteEntryTable entryTable = new SqliteEntryTable(dbPath);
                SqliteChainTable chainTable = new SqliteChainTable(dbPath);)
        {

            int initialEntryTableSize = entryTable.fetchAllUids().size();
            int initialChainTableSize = chainTable.fetchAllUids().size();
            System.out
                .println("Before insertion: entry_table size: " + initialEntryTableSize);
            System.out
                .println("Before insertion: chain_table size: " + initialChainTableSize);

            Findex findex = new Findex(key, label, entryTable, chainTable);

            //
            // Search
            //
            System.out.println("");
            System.out.println("---------------------------------------");
            System.out.println("Verify: Findex Search Sqlite in " + dbPath);
            System.out.println("---------------------------------------");
            System.out.println("");

            {
                SearchResults searchResults = findex.search(new String[] { "France"});
                assertEquals(expectedDbLocations, searchResults.getNumbers());
                System.out.println("<== successfully found all original French locations");
            }

            //
            // Upsert
            //
            UsersDataset[] users = UsersDataset.fromJson(Resources.load_resource("findex/single_user.json"));
            Map<IndexedValue, Set<Keyword>> singleUserIndexedValuesAndWords = IndexUtils.index(users);
            findex.add(singleUserIndexedValuesAndWords);

            Set<Long> newExpectedDbLocations = new HashSet<>(expectedDbLocations);
            for (UsersDataset user : users) {
                newExpectedDbLocations.add(user.id);
            }

            int currentEntryTableSize = entryTable.fetchAllUids().size();
            int currentChainTableSize = chainTable.fetchAllUids().size();
            System.out.println("After insertion: entry_table size: " + currentEntryTableSize);
            System.out.println("After insertion: chain_table size: " + currentChainTableSize);
            assertEquals(initialEntryTableSize + 6, currentEntryTableSize);
            assertEquals(initialChainTableSize + 8, currentChainTableSize);

            //
            // Search
            //
            System.out.println("");
            System.out.println("---------------------------------------");
            System.out.println("Verify: Findex Search Sqlite");
            System.out.println("---------------------------------------");
            System.out.println("");

            {
                SearchResults searchResults = findex.search(new String[] { "France"});
                assertEquals(newExpectedDbLocations, searchResults.getNumbers());
            }

        }
    }

    @Test
    public void test_non_regression_vectors() throws Exception {
        //
        // Recover key and label
        //
        byte[] key = IndexUtils.loadKey();
        assertEquals(16, key.length);
        String label = IndexUtils.loadLabel();

        //
        // Recover test vectors
        //
        Set<Long> expectedDbLocations = IndexUtils.loadExpectedDBLocations();

        //
        // Build dataset with DB uids and words
        //
        UsersDataset[] testFindexDataset = IndexUtils.loadDatasets();
        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);

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
            System.out.println("... OK: Non-regression test file: " + fullPath);
        }
    }

    @Test
    public void test_generate_non_regression_vectors() throws Exception {
        new java.io.File("./target/sqlite.db").delete();

        //
        // Recover key and label
        //
        byte[] key = IndexUtils.loadKey();
        assertEquals(16, key.length);
        String label = IndexUtils.loadLabel();

        //
        // Build dataset with DB uids and words
        //
        UsersDataset[] testFindexDataset = IndexUtils.loadDatasets();
        Map<IndexedValue, Set<Keyword>> indexedValuesAndWords = IndexUtils.index(testFindexDataset);

        //
        // Generate non regression sqlite
        //
        //
        // Upsert
        //
        Findex findex = new Findex(key,
                                   label,
                                   new SqliteEntryTable("./target/sqlite.db"),
                                   new SqliteChainTable("./target/sqlite.db"));
        findex.add(indexedValuesAndWords);
    }
}
