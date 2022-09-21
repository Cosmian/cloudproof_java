package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
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
import com.cosmian.jna.findex.Callbacks.FetchAllEntry;
import com.cosmian.jna.findex.Callbacks.FetchChain;
import com.cosmian.jna.findex.Callbacks.FetchEntry;
import com.cosmian.jna.findex.Callbacks.ListRemovedLocations;
import com.cosmian.jna.findex.Callbacks.UpdateLines;
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
        String expectedSearchResultsInt = Resources.load_resource("findex/expected_db_uids.json");
        int[] expectedDbUids = mapper.readValue(expectedSearchResultsInt, int[].class);
        Arrays.sort(expectedDbUids);

        //
        // Build dataset with DB uids and words
        //
        String dataJson = Resources.load_resource("findex/data.json");
        UsersDataset[] testFindexDataset = UsersDataset.fromJson(dataJson);
        HashMap<IndexedValue, Word[]> indexedValuesAndWords = new HashMap<>();
        for (UsersDataset user : testFindexDataset) {
            ByteBuffer dbuf = ByteBuffer.allocate(32);
            dbuf.putInt(user.id);
            byte[] dbUid = dbuf.array();

            indexedValuesAndWords.put(new Location(dbUid), user.values());
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

        FetchAllEntry fetchAllEntry = new FetchAllEntry(new com.cosmian.jna.findex.FfiWrapper.FetchAllEntryInterface() {
            @Override
            public HashMap<byte[], byte[]> fetch() throws FfiException {
                try {
                    return db.fetchAllEntryTableItems();
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
        UpdateLines updateLines = new UpdateLines(new com.cosmian.jna.findex.FfiWrapper.UpdateLinesInterface() {
            @Override
            public void update(List<byte[]> removedChains, HashMap<byte[], byte[]> newEntries,
                HashMap<byte[], byte[]> newChains) throws FfiException {
                try {
                    db.databaseTruncate("entry_table");
                    db.databaseUpsert(newEntries, "entry_table");
                    db.databaseUpsert(newChains, "chain_table");
                    db.databaseRemove(removedChains, "chain_table");
                } catch (SQLException e) {
                    throw new FfiException("Failed update lines: " + e.toString());
                }
            }
        });
        ListRemovedLocations listRemovedLocations =
            new ListRemovedLocations(new com.cosmian.jna.findex.FfiWrapper.ListRemovedLocationsInterface() {
                @Override
                public List<Location> list(List<Location> locations) throws FfiException {
                    List<Integer> ids =
                        locations.stream().map((Location location) -> ByteBuffer.wrap(location.getBytes()).getInt())
                            .collect(Collectors.toList());

                    try {
                        return db.listRemovedIds("users", ids).stream()
                            .map((Integer id) -> new Location(ByteBuffer.allocate(32).putInt(id).array()))
                            .collect(Collectors.toList());
                    } catch (SQLException e) {
                        throw new FfiException("Failed update lines: " + e.toString());
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

        {
            List<byte[]> indexedValuesList =
                Ffi.search(masterKeys.getK(), publicLabelT, new Word[] {new Word("France")}, 0, fetchEntry, fetchChain);
            int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);

            assertArrayEquals(expectedDbUids, dbUids);
        }

        Ffi.compact(1, masterKeys, "NewPublicLabelT".getBytes(), fetchEntry, fetchChain, fetchAllEntry, updateLines,
            listRemovedLocations);

        {
            List<byte[]> indexedValuesList =
                Ffi.search(masterKeys.getK(), publicLabelT, new Word[] {new Word("France")}, 0, fetchEntry, fetchChain);
            int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);

            assertEquals(0, dbUids.length);
        }

        {
            List<byte[]> indexedValuesList = Ffi.search(masterKeys.getK(), "NewPublicLabelT".getBytes(),
                new Word[] {new Word("France")}, 0, fetchEntry, fetchChain);
            int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);

            assertArrayEquals(expectedDbUids, dbUids);
        }

        db.deleteUser(17);
        int[] newExpectedDbUids = ArrayUtils.removeElement(expectedDbUids, 17);

        Ffi.compact(1, masterKeys, "NewPublicLabelT".getBytes(), fetchEntry, fetchChain, fetchAllEntry, updateLines,
            listRemovedLocations);

        {
            List<byte[]> indexedValuesList = Ffi.search(masterKeys.getK(), "NewPublicLabelT".getBytes(),
                new Word[] {new Word("France")}, 0, fetchEntry, fetchChain);
            int[] dbUids = indexedValuesBytesListToArray(indexedValuesList);

            assertArrayEquals(newExpectedDbUids, dbUids);
        }
    }

    int[] indexedValuesBytesListToArray(List<byte[]> indexedValuesList) throws Exception {
        int[] dbUids = new int[indexedValuesList.size()];
        int count = 0;
        for (byte[] dbUidBytes : indexedValuesList) {
            byte[] location = new IndexedValue(dbUidBytes).getLocation().getBytes();

            dbUids[count] = ByteBuffer.wrap(location).getInt();
            count++;
        }

        Arrays.sort(dbUids);
        return dbUids;
    }

}
