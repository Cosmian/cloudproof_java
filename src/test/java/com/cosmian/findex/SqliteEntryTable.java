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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cosmian.jna.findex.EntryTableDatabase;
import com.cosmian.jna.findex.serde.Tuple;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

public class SqliteEntryTable implements EntryTableDatabase, Closeable {

    final Connection connection;

    public SqliteEntryTable() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        this.createTable();
    }

    public SqliteEntryTable(String url) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + url);
        this.createTable();
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws IOException {
        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new IOException("failed closing the Sqlite connection: " + e.getMessage(), e);
        }
    }

    void createTable() throws SQLException {
        Statement stat = this.connection.createStatement();
        stat.execute("CREATE TABLE IF NOT EXISTS entry_table (uid BLOB PRIMARY KEY,value BLOB NOT NULL)");
    }

    public void flush() throws SQLException {
        connection.createStatement().execute("DELETE FROM entry_table");
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

    public List<Tuple<Uid32, EntryTableValue>> fetchUids(List<Uid32> uids) throws SQLException {
        PreparedStatement pstmt = this.connection.prepareStatement(
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
        ArrayList<Tuple<Uid32, EntryTableValue>> uidsAndValues = new ArrayList<>();
        while (rs.next()) {
            uidsAndValues.add(new Tuple<>(new Uid32(rs.getBytes("uid")), new EntryTableValue(rs.getBytes("value"))));
        }
        return uidsAndValues;
    }

    public Map<Uid32, EntryTableValue> conditionalUpsert(Map<Uid32, EntryTableValues> uidsAndValues)
        throws SQLException {
        PreparedStatement updatePreparedStatement = connection
            .prepareStatement(
                "INSERT INTO entry_table (uid, value) VALUES(?,?) ON CONFLICT(uid) DO UPDATE SET value=? WHERE value=?;");
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
            .prepareStatement(
                "SELECT uid, value FROM entry_table WHERE uid IN (" + questionMarks(failedUids.size()) + ")");

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

    void insertEntry(Map<Uid32, EntryTableValue> uidsAndValues) throws SQLException {
        PreparedStatement pstmt = connection
            .prepareStatement("INSERT INTO entry_table (uid, value) VALUES (?,?)");

        for (Entry<Uid32, EntryTableValue> entry : uidsAndValues.entrySet()) {
            pstmt.setBytes(1, entry.getKey().getBytes());
            pstmt.setBytes(2, entry.getValue().getBytes());
            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }

    public void deleteUids(List<Uid32> uids) throws SQLException {
        PreparedStatement pstmt = this.connection
            .prepareStatement(
                "DELETE FROM entry_table WHERE uid IN (" + questionMarks(uids.size()) + ")");
        int count = 1;
        for (Uid32 uid : uids) {
            pstmt.setBytes(count, uid.getBytes());
            count += 1;
        }
        pstmt.execute();
    }

    @Override
    public Set<Uid32> fetchAllUids() throws CloudproofException {
        try {
            return fetchAllEntryUids();
        } catch (SQLException e) {
            throw new CloudproofException("error in Entry Table UID dump: ", e);
        }
    }

    @Override
    public List<Tuple<Uid32, EntryTableValue>> fetch(List<Uid32> uids) throws CloudproofException {
        try {
            return fetchUids(uids);
        } catch (SQLException e) {
            throw new CloudproofException("error in Entry Table fetch: ", e);
        }
    }

    @Override
    public Map<Uid32, EntryTableValue> upsert(Map<Uid32, EntryTableValues> modifications) throws CloudproofException {
        try {
            return conditionalUpsert(modifications);
        } catch (SQLException e) {
            throw new CloudproofException("error in Entry Table upsert: ", e);
        }
    }

    @Override
    public void insert(Map<Uid32, EntryTableValue> uidsAndValues) throws CloudproofException {
        try {
            insertEntry(uidsAndValues);
        } catch (SQLException e) {
            throw new CloudproofException("error in Entry Table insert: ", e);
        }
    }

    @Override
    public void delete(List<Uid32> uids) throws CloudproofException {
        try {
            deleteUids(uids);
        } catch (SQLException e) {
            throw new CloudproofException("error in Entry Table delete: ", e);
        }
    }
}
