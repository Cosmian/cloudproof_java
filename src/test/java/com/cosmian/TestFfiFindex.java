package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.findex.Sqlite;
import com.cosmian.findex.UsersDataset;
import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.Ffi;
import com.cosmian.jna.findex.IndexedValue;
import com.cosmian.jna.findex.Location;
import com.cosmian.jna.findex.MasterKeys;
import com.cosmian.jna.findex.Word;
import com.cosmian.jna.findex.Callbacks.FetchChain;
import com.cosmian.jna.findex.Callbacks.FetchEntry;
import com.cosmian.jna.findex.Callbacks.UpsertChain;
import com.cosmian.jna.findex.Callbacks.UpsertEntry;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestFfiFindex {

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    public byte[] hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] passHash = sha256.digest(data);
        return passHash;
    }

    @Test
    public void testUpsertAndSearch() throws Exception {
        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Upsert");
        System.out.println("---------------------------------------");
        System.out.println("");

        //
        // Recover master keys (k and k*)
        //
        String masterKeysJson = Resources.load_resource("findex/keys.json");
        MasterKeys masterKeys = MasterKeys.fromJson(masterKeysJson);

        byte[] publicLabelT = Resources.load_resource_as_bytes("findex/public_label_t");

        //
        // Recover test vectors
        //
        ObjectMapper mapper = new ObjectMapper();
        String expectedSearchResultsString = Resources.load_resource("findex/expected_db_uids.json");
        String[] expectedDbUids = mapper.readValue(expectedSearchResultsString, String[].class);
        Arrays.sort(expectedDbUids);

        //
        // Build dataset with DB uids and words
        //
        String dataJson = Resources.load_resource("findex/data.json");
        UsersDataset[] testFindexDataset = UsersDataset.fromJson(dataJson);
        HashMap<IndexedValue, Word[]> indexedValuesAndWords = new HashMap<>();
        int count = 1;
        for (UsersDataset user : testFindexDataset) {
            byte[] dbUid = new byte[32];
            byte[] pattern =
                String.format("%02X", count).replace(' ', '0').toLowerCase().getBytes(Charset.defaultCharset());
            for (int i = 0; i < 32; i = i + 2) {
                dbUid[i] = pattern[0];
                dbUid[i + 1] = pattern[1];
            }
            indexedValuesAndWords.put(new Location(dbUid), user.values());
            count++;
        }

        //
        // Prepare Sqlite tables and users
        //
        Sqlite db = new Sqlite();
        db.insertUsers(testFindexDataset);

        //
        // Declare all callbacks
        //
        FetchEntry fetchEntry = new FetchEntry(new com.cosmian.jna.findex.FfiWrapper.FetchEntryInterface() {
            @Override
            public HashMap<byte[], byte[]> fetch(List<byte[]> uids) throws FfiException {
                try {
                    return db.fetchEntryTableItems(uids);
                } catch (SQLException e) {
                    throw new FfiException("Failed fetch entry: " + e.toString());
                }
            }
        });

        FetchChain fetchChain = new FetchChain(new com.cosmian.jna.findex.FfiWrapper.FetchChainInterface() {
            @Override
            public HashMap<byte[], byte[]> fetch(List<byte[]> uids) throws FfiException {
                try {
                    return db.fetchChainTableItems(uids);
                } catch (SQLException e) {
                    throw new FfiException("Failed fetch chain: " + e.toString());
                }
            }
        });

        UpsertEntry upsertEntry = new UpsertEntry(new com.cosmian.jna.findex.FfiWrapper.UpsertEntryInterface() {
            @Override
            public void upsert(HashMap<byte[], byte[]> uidsAndValues) throws FfiException {
                try {
                    db.databaseUpsert(uidsAndValues, "entry_table");
                } catch (SQLException e) {
                    throw new FfiException("Failed entry upsert: " + e.toString());
                }
            }
        });
        UpsertChain upsertChain = new UpsertChain(new com.cosmian.jna.findex.FfiWrapper.UpsertChainInterface() {
            @Override
            public void upsert(HashMap<byte[], byte[]> uidsAndValues) throws FfiException {
                try {
                    db.databaseUpsert(uidsAndValues, "chain_table");
                } catch (SQLException e) {
                    throw new FfiException("Failed chain upsert: " + e.toString());
                }
            }
        });
        //
        // Upsert
        //
        Ffi.upsert(masterKeys, publicLabelT, indexedValuesAndWords, fetchEntry, upsertEntry, upsertChain);
        System.out.println("After insertion: entry_table: nb indexes: " + db.getAllKeyValueItems("entry_table").size());
        System.out.println("After insertion: chain_table: nb indexes: " + db.getAllKeyValueItems("chain_table").size());

        //
        // Search
        //
        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Search");
        System.out.println("---------------------------------------");
        System.out.println("");

        List<byte[]> indexedValuesList =
            Ffi.search(masterKeys.getK(), publicLabelT, new Word[] {new Word("France")}, 0, fetchEntry, fetchChain);

        // Get DbUids from IndexedValues
        List<String> dbUidsStringList = new ArrayList<String>();
        for (byte[] dbUidBytes : indexedValuesList) {
            byte[] location = new IndexedValue(dbUidBytes).getLocation().getBytes();
            String dbUid = new String(location, StandardCharsets.UTF_8);
            dbUidsStringList.add(dbUid);
        }
        String[] dbUids = new String[dbUidsStringList.size()];
        dbUidsStringList.toArray(dbUids);
        Arrays.sort(dbUids);
        System.out.println("DB UIDS found: " + Arrays.toString(dbUids));

        assertArrayEquals(dbUids, expectedDbUids);
    }

}
