package com.cosmian.findex;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;

import com.cosmian.CloudproofException;
import com.cosmian.jna.findex.FindexWrapper.EntryTableValue;
import com.cosmian.jna.findex.IndexedValue;
import com.cosmian.jna.findex.Location;
import com.cosmian.jna.findex.Uid;
import com.cosmian.jna.findex.Callbacks.FetchAllEntry;
import com.cosmian.jna.findex.Callbacks.FetchChain;
import com.cosmian.jna.findex.Callbacks.FetchEntry;
import com.cosmian.jna.findex.Callbacks.ListRemovedLocations;
import com.cosmian.jna.findex.Callbacks.Progress;
import com.cosmian.jna.findex.Callbacks.UpdateLines;
import com.cosmian.jna.findex.Callbacks.UpsertChain;
import com.cosmian.jna.findex.Callbacks.UpsertEntry;

public class Sqlite implements Closeable {

    private final Connection connection;

    //
    // Declare all Findex callbacks
    //
    public FetchEntry fetchEntry = new FetchEntry(new com.cosmian.jna.findex.FindexWrapper.FetchEntryInterface() {
        @Override
        public HashMap<byte[], byte[]> fetch(List<byte[]> uids) throws CloudproofException {
            try {
                return fetchEntryTableItems(uids);
            } catch (SQLException e) {
                throw new CloudproofException("Failed fetch entry: " + e.toString());
            }
        }
    });

    public FetchAllEntry fetchAllEntry = new FetchAllEntry(
            new com.cosmian.jna.findex.FindexWrapper.FetchAllEntryInterface() {
                @Override
                public HashMap<byte[], byte[]> fetch() throws CloudproofException {
                    try {
                        return fetchAllEntryTableItems();
                    } catch (SQLException e) {
                        throw new CloudproofException("Failed fetch all entry: " + e.toString());
                    }
                }
            });

    public FetchChain fetchChain = new FetchChain(new com.cosmian.jna.findex.FindexWrapper.FetchChainInterface() {
        @Override
        public HashMap<byte[], byte[]> fetch(List<byte[]> uids) throws CloudproofException {
            try {
                return fetchChainTableItems(uids);
            } catch (SQLException e) {
                throw new CloudproofException("Failed fetch chain: " + e.toString());
            }
        }
    });

    public UpsertEntry upsertEntry = new UpsertEntry(new com.cosmian.jna.findex.FindexWrapper.UpsertEntryInterface() {
        // @Override
        // public void upsert(HashMap<byte[], byte[]> uidsAndValues) throws
        // CloudproofException {
        // try {
        // databaseUpsert(uidsAndValues, "entry_table");
        // } catch (SQLException e) {
        // throw new CloudproofException("Failed entry upsert: " + e.toString());
        // }
        // }

        @Override
        public HashMap<byte[], byte[]> upsert(HashMap<byte[], EntryTableValue> uidsAndValues)
                throws CloudproofException {
            // TODO Auto-generated method stub
            return null;
        }
    });

    public UpsertChain upsertChain = new UpsertChain(new com.cosmian.jna.findex.FindexWrapper.UpsertChainInterface() {
        @Override
        public void upsert(HashMap<byte[], byte[]> uidsAndValues) throws CloudproofException {
            try {
                databaseUpsert(uidsAndValues, "chain_table");
            } catch (SQLException e) {
                throw new CloudproofException("Failed chain upsert: " + e.toString());
            }
        }
    });

    public UpdateLines updateLines = new UpdateLines(new com.cosmian.jna.findex.FindexWrapper.UpdateLinesInterface() {
        @Override
        public void update(List<byte[]> removedChains, HashMap<byte[], byte[]> newEntries,
                HashMap<byte[], byte[]> newChains) throws CloudproofException {
            try {
                databaseTruncate("entry_table");
                databaseUpsert(newEntries, "entry_table");
                databaseUpsert(newChains, "chain_table");
                databaseRemove(removedChains, "chain_table");
            } catch (SQLException e) {
                throw new CloudproofException("Failed update lines: " + e.toString());
            }
        }
    });

    public ListRemovedLocations listRemovedLocations = new ListRemovedLocations(
            new com.cosmian.jna.findex.FindexWrapper.ListRemovedLocationsInterface() {
                @Override
                public List<Location> list(List<Location> locations) throws CloudproofException {
                    List<Integer> ids = locations.stream()
                            .map((Location location) -> ByteBuffer.wrap(location.getBytes()).getInt())
                            .collect(Collectors.toList());

                    try {
                        return listRemovedIds("users", ids).stream()
                                .map((Integer id) -> new Location(ByteBuffer.allocate(32).putInt(id).array()))
                                .collect(Collectors.toList());
                    } catch (SQLException e) {
                        throw new CloudproofException("Failed update lines: " + e.toString());
                    }

                }
            });

    public Progress progress = new Progress(new com.cosmian.jna.findex.FindexWrapper.ProgressInterface() {
        @Override
        public boolean list(List<byte[]> indexedValues) throws CloudproofException {

            try {
                //
                // Convert indexed values from bytes
                //
                List<IndexedValue> indexedValuesBytes = new ArrayList<>();
                for (byte[] iv : indexedValues) {
                    indexedValuesBytes.add(new IndexedValue(iv));
                }
                return true;
            } catch (CloudproofException e) {
                throw new CloudproofException("Failed getting search results: " + e.toString());
            }

        }
    });

    public Sqlite() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        this.createTables();
    }

    public Connection getConnection() {
        return connection;
    }

    void createTables() throws SQLException {
        Statement stat = this.connection.createStatement();
        stat.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (id integer PRIMARY KEY, firstName text NOT NULL, lastName text NOT NULL, email text NOT NULL, phone text NOT NULL, country text NOT NULL, region text NOT NULL, employeeNumber text NOT NULL, security text NOT NULL)");
        stat.execute("CREATE TABLE IF NOT EXISTS entry_table (uid BLOB PRIMARY KEY,value BLOB NOT NULL)");
        stat.execute("CREATE TABLE IF NOT EXISTS chain_table (uid BLOB PRIMARY KEY,value BLOB NOT NULL)");
    }

    public void insertUsers(UsersDataset[] testFindexDataset) throws SQLException {
        Statement stat = this.connection.createStatement();
        for (UsersDataset user : testFindexDataset) {
            stat.executeUpdate(
                    "INSERT INTO users (id, firstName,lastName,phone,email,country,region,employeeNumber,security) VALUES ("
                            + user.id + ", '" + user.firstName + "','" + user.lastName + "','" + user.phone + "','"
                            + user.email
                            + "','" + user.country + "','" + user.region + "','" + user.employeeNumber + "','"
                            + user.security
                            + "')");
        }
    }

    public void deleteUser(int userId) throws SQLException {
        this.connection.createStatement().execute("DELETE FROM users WHERE id = " + userId);
    }

    public HashMap<byte[], byte[]> fetchChainTableItems(List<byte[]> uids) throws SQLException {
        PreparedStatement pstmt = this.connection
                .prepareStatement(
                        "SELECT uid, value FROM chain_table WHERE uid IN (" + questionMarks(uids.size()) + ")");

        int count = 1;
        for (byte[] bs : uids) {
            pstmt.setBytes(count, bs);
            count += 1;
        }
        ResultSet rs = pstmt.executeQuery();

        //
        // Recover all results
        //
        HashMap<byte[], byte[]> uidsAndValues = new HashMap<byte[], byte[]>();
        while (rs.next()) {
            uidsAndValues.put(rs.getBytes("uid"), rs.getBytes("value"));
        }
        return uidsAndValues;
    }

    public HashMap<byte[], byte[]> fetchAllEntryTableItems() throws SQLException {
        PreparedStatement pstmt = this.connection.prepareStatement("SELECT uid, value FROM entry_table");
        ResultSet rs = pstmt.executeQuery();

        //
        // Recover all results
        //
        HashMap<byte[], byte[]> uidsAndValues = new HashMap<byte[], byte[]>();
        while (rs.next()) {
            uidsAndValues.put(rs.getBytes("uid"), rs.getBytes("value"));
        }

        return uidsAndValues;
    }

    public HashMap<byte[], byte[]> fetchEntryTableItems(List<byte[]> uids) throws SQLException {
        PreparedStatement pstmt = this.connection
                .prepareStatement(
                        "SELECT uid, value FROM entry_table WHERE uid IN (" + questionMarks(uids.size()) + ")");

        int count = 1;
        for (byte[] bs : uids) {
            pstmt.setBytes(count, bs);
            count += 1;
        }
        ResultSet rs = pstmt.executeQuery();

        //
        // Recover all results
        //
        HashMap<byte[], byte[]> uidsAndValues = new HashMap<byte[], byte[]>();
        while (rs.next()) {
            uidsAndValues.put(rs.getBytes("uid"), rs.getBytes("value"));
        }
        return uidsAndValues;
    }

    public void databaseUpsert(Map<byte[], byte[]> uidsAndValues, String tableName) throws SQLException {
        PreparedStatement pstmt = connection
                .prepareStatement("INSERT OR REPLACE INTO " + tableName + "(uid, value) VALUES (?,?)");
        // this.connection.setAutoCommit(false);
        for (Entry<byte[], byte[]> entry : uidsAndValues.entrySet()) {
            pstmt.setBytes(1, entry.getKey());
            pstmt.setBytes(2, entry.getValue());
            pstmt.addBatch();
        }
        /* int[] result = */ pstmt.executeBatch();
        // this.connection.commit();
        // System.out.println("The number of rows in " + tableName + " inserted: " +
        // result.length);
    }

    public Set<byte[]> databaseConditionalUpsert(Map<Uid, EntryTableValue> uidsAndValues, String tableName)
            throws SQLException {
        if (uidsAndValues.size() == 0) {
            return new HashSet<>();
        }
        PreparedStatement pstmt = connection
                .prepareStatement("INSERT INTO " + tableName
                        + "(uid, value) VALUES(?,?) ON CONFLICT(uid) DO UPDATE SET value=? WHERE value=?;");
        // this.connection.setAutoCommit(false);
        ArrayList<byte[]> uids = new ArrayList<>(uidsAndValues.size());
        for (Entry<Uid, EntryTableValue> entry : uidsAndValues.entrySet()) {
            uids.add(entry.getKey().getBytes());
            pstmt.setBytes(1, entry.getKey().getBytes());
            pstmt.setBytes(2, entry.getValue().newValue);
            pstmt.setBytes(3, entry.getValue().newValue);
            pstmt.setBytes(4, entry.getValue().previousValue);
            pstmt.addBatch();
        }
        // this.connection.commit();
        int[] results = pstmt.executeBatch();
        HashSet<byte[]> failed = new HashSet<>();
        for (int result : results) {
            if (result == 0) {
                failed.add(uids.get(result));
            }
            System.out.println(result + " -> " + Hex.encodeHexString(uids.get(result)));
        }
        return failed;
    }

    public void databaseRemove(List<byte[]> uids, String tableName) throws SQLException {
        PreparedStatement pstmt = this.connection
                .prepareStatement("DELETE FROM " + tableName + " WHERE uid IN (" + questionMarks(uids.size()) + ")");

        int count = 1;
        for (byte[] bs : uids) {
            pstmt.setBytes(count, bs);
            count += 1;
        }
        pstmt.execute();
    }

    public void databaseTruncate(String tableName) throws SQLException {
        connection.createStatement().execute("DELETE FROM " + tableName);
        System.out.println("Table " + tableName + " has been truncated");
    }

    public HashMap<byte[], byte[]> getAllKeyValueItems(String tableName) throws SQLException {
        Statement stat = this.connection.createStatement();
        String sql = "SELECT uid, value FROM " + tableName;
        ResultSet rs = stat.executeQuery(sql);
        HashMap<byte[], byte[]> uidsAndValues = new HashMap<byte[], byte[]>();
        while (rs.next()) {
            uidsAndValues.put(rs.getBytes("uid"), rs.getBytes("value"));
        }
        return uidsAndValues;
    }

    public List<Integer> listRemovedIds(String string, List<Integer> ids) throws SQLException {
        PreparedStatement pstmt = this.connection
                .prepareStatement("SELECT id FROM users WHERE id IN (" + questionMarks(ids.size()) + ")");

        int count = 1;
        for (Integer bs : ids) {
            pstmt.setInt(count, bs);
            count += 1;
        }
        ResultSet rs = pstmt.executeQuery();

        HashSet<Integer> removedIds = new HashSet<>(ids);
        while (rs.next()) {
            removedIds.remove(rs.getInt("id"));
        }

        return new LinkedList<>(removedIds);
    }

    private String questionMarks(int count) {
        String lotsOfQuestions = "";
        for (int i = 0; i < count; i++) {
            lotsOfQuestions += "?";
            if (i != count - 1) {
                lotsOfQuestions += ",";
            }
        }
        return lotsOfQuestions;
    }

    @Override
    public void close() throws IOException {
        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new IOException("failed closing the Sqlite connection: " + e.getMessage(), e);
        }

    }
}
