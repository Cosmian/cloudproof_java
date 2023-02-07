package com.cosmian.findex;

import java.io.Closeable;
import java.io.IOException;
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

import com.cosmian.jna.findex.Database;
import com.cosmian.jna.findex.serde.Leb128ByteArray;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.jna.findex.ffi.ProgressResults;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

public class Sqlite extends Database implements Closeable {

    private final Connection connection;

    public Sqlite() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        this.createTables();
    }

    public Sqlite(String url) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + url);
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

    public Map<Uid32, ChainTableValue> fetchChainTableItems(List<Uid32> uids) throws SQLException {
        PreparedStatement pstmt = this.connection
            .prepareStatement(
                "SELECT uid, value FROM chain_table WHERE uid IN (" + questionMarks(uids.size()) + ")");

        int count = 1;
        for (Uid32 uid : uids) {
            pstmt.setBytes(count, uid.getBytes());
            count += 1;
        }
        ResultSet rs = pstmt.executeQuery();

        //
        // Recover all results
        //
        HashMap<Uid32, ChainTableValue> uidsAndValues = new HashMap<>();
        while (rs.next()) {
            uidsAndValues.put(
                new Uid32(rs.getBytes("uid")),
                new ChainTableValue(rs.getBytes("value")));
        }
        return uidsAndValues;
    }

    public Set<Uid32> fetchAllEntryUids() throws SQLException {
        PreparedStatement pstmt = this.connection.prepareStatement("SELECT uid FROM entry_table");
        ResultSet rs = pstmt.executeQuery();

        //
        // Recover all results
        //
        Set<Uid32> uids = new HashSet<>();
        while (rs.next()) {
            uids.add(new Uid32(rs.getBytes("uid")));
        }
        return uids;
    }

    public Map<Uid32, EntryTableValue> fetchEntryTableItems(List<Uid32> uids) throws SQLException {
        PreparedStatement pstmt = this.connection
            .prepareStatement(
                "SELECT uid, value FROM entry_table WHERE uid IN (" + questionMarks(uids.size()) + ")");

        int count = 1;
        for (Uid32 uid : uids) {
            pstmt.setBytes(count, uid.getBytes());
            count += 1;
        }
        ResultSet rs = pstmt.executeQuery();

        //
        // Recover all results
        //
        HashMap<Uid32, EntryTableValue> uidsAndValues = new HashMap<>(uids.size(), 1);
        while (rs.next()) {
            uidsAndValues.put(
                new Uid32(rs.getBytes("uid")),
                new EntryTableValue(rs.getBytes("value")));
        }
        return uidsAndValues;
    }

    public <V extends Leb128ByteArray> void upsert(Map<Uid32, V> uidsAndValues,
                                                   String tableName)
        throws SQLException {
        PreparedStatement pstmt = connection
            .prepareStatement("INSERT OR REPLACE INTO " + tableName + "(uid, value) VALUES (?,?)");
        // this.connection.setAutoCommit(false);
        for (Entry<Uid32, V> entry : uidsAndValues.entrySet()) {
            pstmt.setBytes(1, entry.getKey().getBytes());
            pstmt.setBytes(2, entry.getValue().getBytes());
            pstmt.addBatch();
        }
        /* int[] result = */ pstmt.executeBatch();
        // this.connection.commit();
        // System.out.println("The number of rows in " + tableName + " inserted: " +
        // result.length);
    }

    public Map<Uid32, EntryTableValue> conditionalUpsert(Map<Uid32, EntryTableValues> uidsAndValues,
                                                         String tableName)
        throws SQLException {
        if (uidsAndValues.size() == 0) {
            return new HashMap<>();
        }
        PreparedStatement updatePreparedStatement = connection
            .prepareStatement("INSERT INTO " + tableName
                + "(uid, value) VALUES(?,?) ON CONFLICT(uid) DO UPDATE SET value=? WHERE value=?;");
        // this.connection.setAutoCommit(false);
        ArrayList<Uid32> uids = new ArrayList<>(uidsAndValues.size());
        for (Entry<Uid32, EntryTableValues> entry : uidsAndValues.entrySet()) {
            Uid32 uid = entry.getKey();
            uids.add(uid);
            updatePreparedStatement.setBytes(1, uid.getBytes());
            updatePreparedStatement.setBytes(2, entry.getValue().getNew().getBytes());
            updatePreparedStatement.setBytes(3, entry.getValue().getNew().getBytes());
            updatePreparedStatement.setBytes(4, entry.getValue().getPrevious().getBytes());
            updatePreparedStatement.addBatch();
        }
        // this.connection.commit();
        int[] updateResults = updatePreparedStatement.executeBatch();
        HashSet<Uid32> failedUids = new HashSet<>();
        for (int i = 0; i < updateResults.length; i++) {
            if (updateResults[i] == 0) {
                failedUids.add(uids.get(i));
            }
        }
        if (failedUids.size() == 0) {
            return new HashMap<>();
        }

        // Select all the failed uids and their corresponding
        HashMap<Uid32, EntryTableValue> failed = new HashMap<>(failedUids.size(), 1);
        PreparedStatement selectPreparedStatement = connection
            .prepareStatement("SELECT uid, value FROM " + tableName
                + " WHERE uid IN (" + questionMarks(failedUids.size()) + ")");

        // setArray does not work on Linux (works on MacOS)
        // selectPreparedStatement.setArray(1, connection.createArrayOf("BLOB", failedUidBytes.toArray()));
        int count = 1;
        for (Uid32 failedUid : failedUids) {
            selectPreparedStatement.setBytes(count, failedUid.getBytes());
            count += 1;
        }
        ResultSet selectResults = selectPreparedStatement.executeQuery();
        while (selectResults.next()) {
            Uid32 uid = new Uid32(selectResults.getBytes("uid"));
            failed.put(uid, new EntryTableValue(selectResults.getBytes("value")));
        }
        return failed;
    }

    public void remove(List<Uid32> uids,
                       String tableName)
        throws SQLException {
        PreparedStatement pstmt = this.connection
            .prepareStatement("DELETE FROM " + tableName + " WHERE uid IN (" + questionMarks(uids.size()) + ")");

        int count = 1;
        for (Uid32 uid : uids) {
            pstmt.setBytes(count, uid.getBytes());
            count += 1;
        }
        pstmt.execute();
    }

    public void truncate(String tableName) throws SQLException {
        connection.createStatement().execute("DELETE FROM " + tableName);
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

    @Override
    protected Set<Uid32> fetchAllEntryTableUids() throws CloudproofException {
        try {
            return this.fetchAllEntryUids();
        } catch (Exception e) {
            throw new CloudproofException("Failed fetching all entry UIDs: " + e.toString());
        }
    }

    @Override
    protected Map<Uid32, EntryTableValue> fetchEntries(List<Uid32> uids) throws CloudproofException {
        try {
            return fetchEntryTableItems(uids);
        } catch (SQLException e) {
            throw new CloudproofException("Failed fetch entry: " + e.toString());
        }
    }

    @Override
    protected Map<Uid32, ChainTableValue> fetchChains(List<Uid32> uids) throws CloudproofException {
        try {
            return fetchChainTableItems(uids);
        } catch (SQLException e) {
            throw new CloudproofException("Failed fetch chain: " + e.toString());
        }
    }

    @Override
    protected Map<Uid32, EntryTableValue> upsertEntries(Map<Uid32, EntryTableValues> uidsAndValues)
        throws CloudproofException {
        try {
            return Sqlite.this.conditionalUpsert(uidsAndValues, "entry_table");
        } catch (SQLException e) {
            throw new CloudproofException("Failed entry upsert: " + e.toString());
        }
    }

    @Override
    protected void upsertChains(Map<Uid32, ChainTableValue> uidsAndValues) throws CloudproofException {
        try {
            Sqlite.this.upsert(uidsAndValues, "chain_table");
        } catch (SQLException e) {
            throw new CloudproofException("Failed chain upsert: " + e.toString());
        }
    }

    @Override
    protected void updateTables(List<Uid32> removedChains,
                                Map<Uid32, EntryTableValue> newEntries,
                                Map<Uid32, ChainTableValue> newChains)
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

    @Override
    protected List<Location> listRemovedLocations(List<Location> locations) throws CloudproofException {
        List<Integer> ids = locations.stream()
            .map((Location location) -> IndexUtils.locationToUserId(location))
            .collect(Collectors.toList());
        try {
            List<Integer> removedIds = listRemovedIds("users", ids);
            return removedIds.stream()
                .map((Integer id) -> IndexUtils.userIdToLocation(id))
                .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new CloudproofException("Failed list removed locations: " + e.toString());
        }
    }

    @Override
    protected boolean searchProgress(ProgressResults indexedValues) throws CloudproofException {
        // let search progress
        return true;
    }

}
