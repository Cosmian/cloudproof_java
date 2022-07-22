package com.cosmian.findex;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
                "INSERT INTO users (firstName,lastName,phone,email,country,region,employeeNumber,security) VALUES ('"
                    + user.firstName + "','" + user.lastName + "','" + user.phone + "','" + user.email + "','"
                    + user.country + "','" + user.region + "','" + user.employeeNumber + "','" + user.security + "')");
        }
    }

    public List<byte[]> fetchChainTableItems(List<byte[]> uids) throws SQLException {
        String lotsOfQuestions = "";
        for (int i = 0; i < uids.size(); i++) {
            lotsOfQuestions += "?";
            if (i != uids.size() - 1) {
                lotsOfQuestions += ",";
            }
        }

        PreparedStatement pstmt =
            this.connection.prepareStatement("SELECT value FROM chain_table WHERE uid IN (" + lotsOfQuestions + ")");

        int count = 1;
        for (byte[] bs : uids) {
            pstmt.setBytes(count, bs);
            count += 1;
        }
        ResultSet rs = pstmt.executeQuery();

        //
        // Recover all results
        //
        List<byte[]> values = new ArrayList<byte[]>();
        while (rs.next()) {
            values.add(rs.getBytes("value"));
        }
        return values;
    }

    public HashMap<byte[], byte[]> fetchEntryTableItems(List<byte[]> uids) throws SQLException {
        String lotsOfQuestions = "";
        for (int i = 0; i < uids.size(); i++) {
            lotsOfQuestions += "?";
            if (i != uids.size() - 1) {
                lotsOfQuestions += ",";
            }
        }

        PreparedStatement pstmt = this.connection
            .prepareStatement("SELECT uid, value FROM entry_table WHERE uid IN (" + lotsOfQuestions + ")");

        int count = 1;
        for (byte[] bs : uids) {
            System.out.println("count=" + count);
            System.out.println("bs=" + Arrays.toString(bs));
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

    public void databaseUpsert(byte[] uid, byte[] value, String tableName) throws SQLException {
        PreparedStatement pstmt =
            this.connection.prepareStatement("INSERT OR REPLACE INTO " + tableName + "(uid, value) VALUES (?,?)");
        pstmt.setBytes(1, uid);
        pstmt.setBytes(2, value);
        pstmt.executeUpdate();
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
}
