package com.cosmian.findex;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class Sqlite {

    private Connection connection;

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
                    + user.id + ", '" + user.firstName + "','" + user.lastName + "','" + user.phone + "','" + user.email
                    + "','" + user.country + "','" + user.region + "','" + user.employeeNumber + "','" + user.security
                    + "')");
        }
    }

    public void deleteUser(int userId) throws SQLException {
        this.connection.createStatement().execute("DELETE FROM users WHERE id = " + userId);
    }

    public HashMap<byte[], byte[]> fetchChainTableItems(List<byte[]> uids) throws SQLException {
        PreparedStatement pstmt = this.connection
            .prepareStatement("SELECT uid, value FROM chain_table WHERE uid IN (" + questionMarks(uids.size()) + ")");

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
            .prepareStatement("SELECT uid, value FROM entry_table WHERE uid IN (" + questionMarks(uids.size()) + ")");

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

    public void databaseUpsert(HashMap<byte[], byte[]> uidsAndValues, String tableName) throws SQLException {
        PreparedStatement pstmt =
            connection.prepareStatement("INSERT OR REPLACE INTO " + tableName + "(uid, value) VALUES (?,?)");
        this.connection.setAutoCommit(false);
        for (Entry<byte[], byte[]> entry : uidsAndValues.entrySet()) {
            pstmt.setBytes(1, entry.getKey());
            pstmt.setBytes(2, entry.getValue());
            pstmt.addBatch();
        }
        int[] result = pstmt.executeBatch();
        this.connection.commit();
        System.out.println("The number of rows in " + tableName + " inserted: " + result.length);
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
        PreparedStatement pstmt =
            this.connection.prepareStatement("SELECT id FROM users WHERE id IN (" + questionMarks(ids.size()) + ")");

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
}
