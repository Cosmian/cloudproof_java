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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.cosmian.jna.findex.ChainTableDatabase;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.cosmian.jna.findex.serde.Tuple;

public class SqliteChainTable implements ChainTableDatabase, Closeable {

    final Connection connection;

    public SqliteChainTable() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        this.createTable();
    }

    public SqliteChainTable(String url) throws SQLException {
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
        stat.execute("CREATE TABLE IF NOT EXISTS chain_table (uid BLOB PRIMARY KEY,value BLOB NOT NULL)");
    }

    public void flush() throws SQLException {
        connection.createStatement().execute("DELETE FROM chain_table");
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

    public Set<Uid32> fetchAllUids() throws SQLException {
        PreparedStatement pstmt = this.connection.prepareStatement("SELECT uid FROM chain_table");
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

    List<Tuple<Uid32, ChainTableValue>> fetchUids(List<Uid32> uids) throws SQLException {
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
        ArrayList<Tuple<Uid32, ChainTableValue>> uidsAndValues = new ArrayList<>();
        while (rs.next()) {
            uidsAndValues.add(new Tuple<>(
                        new Uid32(rs.getBytes("uid")),
                        new ChainTableValue(rs.getBytes("value"))));
        }
        return uidsAndValues;
    }

    void insertChain(Map<Uid32, ChainTableValue> uidsAndValues) throws SQLException {
        PreparedStatement pstmt = connection
            .prepareStatement("INSERT INTO chain_table (uid, value) VALUES (?,?)");

        for (Entry<Uid32, ChainTableValue> entry : uidsAndValues.entrySet()) {
            pstmt.setBytes(1, entry.getKey().getBytes());
            pstmt.setBytes(2, entry.getValue().getBytes());
            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }

    void deleteUids(List<Uid32> uids) throws SQLException {
        PreparedStatement pstmt = this.connection
            .prepareStatement(
                    "DELETE FROM chain_table WHERE uid IN (" + questionMarks(uids.size()) + ")");

        int count = 1;
        for (Uid32 uid : uids) {
            pstmt.setBytes(count, uid.getBytes());
            count += 1;
        }

	pstmt.execute();
    }

    @Override
    public List<Tuple<Uid32, ChainTableValue>> fetch(List<Uid32> uids) throws CloudproofException {
        try {
            return fetchUids(uids);
        } catch (SQLException e) {
            throw new CloudproofException("error in Chain Table fetch: ", e);
        }
    }

    @Override
    public void insert(Map<Uid32, ChainTableValue> uidsAndValues) throws CloudproofException {
        try {
            insertChain(uidsAndValues);
        } catch (SQLException e) {
            throw new CloudproofException("error in Chain Table fetch: ", e);
        }
    }

    @Override
    public void delete(List<Uid32> uids) throws CloudproofException {
        try {
            deleteUids(uids);
        } catch (SQLException e) {
            throw new CloudproofException("error in Chain Table upsert: ", e);
        }
    }
}
