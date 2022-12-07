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
import java.util.stream.Collectors;

import com.cosmian.CloudproofException;
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

import redis.clients.jedis.resps.Tuple;

public class Sqlite implements Closeable {

    private final Connection connection;

    //
    // Declare all Findex callbacks
    //
    public FetchEntry fetchEntry = new FetchEntry(new com.cosmian.jna.findex.FindexWrapper.FetchEntryInterface() {
        @Override
        public Map<Uid, byte[]> fetch(List<Uid> uids) throws CloudproofException {
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
                public Map<Uid, byte[]> fetch() throws CloudproofException {
                    try {
                        return fetchAllEntryTableItems();
                    } catch (SQLException e) {
                        throw new CloudproofException("Failed fetch all entry: " + e.toString());
                    }
                }
            });

    public FetchChain fetchChain = new FetchChain(new com.cosmian.jna.findex.FindexWrapper.FetchChainInterface() {
        @Override
        public Map<Uid, byte[]> fetch(List<Uid> uids) throws CloudproofException {
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
        public Map<Uid, byte[]> upsert(Map<Uid, Tuple> uidsAndValues)
                throws CloudproofException {
            // TODO Auto-generated method stub
            return null;
        }
    });

    public UpsertChain upsertChain = new UpsertChain(new com.cosmian.jna.findex.FindexWrapper.UpsertChainInterface() {
        @Override
        public void upsert(Map<Uid, byte[]> uidsAndValues) throws CloudproofException {
            try {
                Sqlite.this.upsert(uidsAndValues, "chain_table");
            } catch (SQLException e) {
                throw new CloudproofException("Failed chain upsert: " + e.toString());
            }
        }
    });

    public UpdateLines updateLines = new UpdateLines(new com.cosmian.jna.findex.FindexWrapper.UpdateLinesInterface() {
        @Override
        public void update(List<Uid> removedChains,
                Map<Uid, byte[]> newEntries,
                Map<Uid, byte[]> newChains)
                throws CloudproofException {
            try {
                truncate("entry_table");
                upsert(newEntries, "entry_table");
                upsert(newChains, "chain_table");
                remove(removedChains, "chain_table");
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
        public boolean list(List<IndexedValue> indexedValues) throws CloudproofException {
            return true;

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

    public Map<byte[], byte[]> fetchChainTableItems(List<byte[]> uids) throws SQLException {
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

    public Map<Uid, byte[]> fetchAllEntryTableItems() throws SQLException {
        PreparedStatement pstmt = this.connection.prepareStatement("SELECT uid, value FROM entry_table");
        ResultSet rs = pstmt.executeQuery();

        //
        // Recover all results
        //
        HashMap<Uid, byte[]> uidsAndValues = new HashMap<>();
        while (rs.next()) {
            uidsAndValues.put(new Uid(rs.getBytes("uid")), rs.getBytes("value"));
        }

        return uidsAndValues;
    }

    public Map<Uid, byte[]> fetchEntryTableItems(List<byte[]> uids) throws SQLException {
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
        HashMap<Uid, byte[]> uidsAndValues = new HashMap<>(uids.size(), 1);
        while (rs.next()) {
            uidsAndValues.put(new Uid(rs.getBytes("uid")), rs.getBytes("value"));
        }
        return uidsAndValues;
    }

    public void upsert(Map<byte[], byte[]> uidsAndValues,
            String tableName)
            throws SQLException {
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

    public Map<Uid, byte[]> conditionalUpsert(Map<Uid, Tuple> uidsAndValues,
            String tableName)
            throws SQLException {
        if (uidsAndValues.size() == 0) {
            return new HashMap<>();
        }
        PreparedStatement updatePreparedStatement = connection
                .prepareStatement("INSERT INTO " + tableName
                        + "(uid, value) VALUES(?,?) ON CONFLICT(uid) DO UPDATE SET value=? WHERE value=?;");
        // this.connection.setAutoCommit(false);
        ArrayList<Uid> uids = new ArrayList<>(uidsAndValues.size());
        for (Entry<Uid, Tuple> entry : uidsAndValues.entrySet()) {
            Uid uid = entry.getKey();
            uids.add(uid);
            updatePreparedStatement.setBytes(1, uid.getBytes());
            updatePreparedStatement.setBytes(2, entry.getValue().newValue);
            updatePreparedStatement.setBytes(3, entry.getValue().newValue);
            updatePreparedStatement.setBytes(4, entry.getValue().previousValue);
            updatePreparedStatement.addBatch();
        }
        // this.connection.commit();
        int[] updateResults = updatePreparedStatement.executeBatch();
        HashSet<byte[]> failedUidBytes = new HashSet<>();
        for (int i = 0; i < updateResults.length; i++) {
            if (updateResults[i] == 0) {
                failedUidBytes.add(uids.get(i).getBytes());
            }
        }
        if (failedUidBytes.size() == 0) {
            return new HashMap<>();
        }

        // Select all the failed uids and their corresponding
        HashMap<Uid, byte[]> failed = new HashMap<>(failedUidBytes.size(), 1);
        PreparedStatement selectPreparedStatement = connection
                .prepareStatement("SELECT uid, value FROM " + tableName
                        + " WHERE uid IN (?)");
        selectPreparedStatement.setArray(1, connection.createArrayOf("BLOB", failedUidBytes.toArray()));
        ResultSet selectResults = selectPreparedStatement.executeQuery();
        while (selectResults.next()) {
            Uid uid = new Uid(selectResults.getBytes("uid"));
            failed.put(uid, selectResults.getBytes("value"));
        }
        return failed;
    }

    public void remove(List<byte[]> uids,
            String tableName)
            throws SQLException {
        PreparedStatement pstmt = this.connection
                .prepareStatement("DELETE FROM " + tableName + " WHERE uid IN (" + questionMarks(uids.size()) + ")");

        int count = 1;
        for (byte[] bs : uids) {
            pstmt.setBytes(count, bs);
            count += 1;
        }
        pstmt.execute();
    }

    public void truncate(String tableName) throws SQLException {
        connection.createStatement().execute("DELETE FROM " + tableName);
        System.out.println("Table " + tableName + " has been truncated");
    }

    public Map<byte[], byte[]> getAllKeyValueItems(String tableName) throws SQLException {
        Statement stat = this.connection.createStatement();
        String sql = "SELECT uid, value FROM " + tableName;
        ResultSet rs = stat.executeQuery(sql);
        HashMap<byte[], byte[]> uidsAndValues = new HashMap<byte[], byte[]>();
        while (rs.next()) {
            uidsAndValues.put(rs.getBytes("uid"), rs.getBytes("value"));
        }
        return uidsAndValues;
    }

    public List<Integer> listRemovedIds(String string,
            List<Integer> ids)
            throws SQLException {
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
