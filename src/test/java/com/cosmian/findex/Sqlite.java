package com.cosmian.findex;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        stat.execute("CREATE TABLE IF NOT EXISTS entry_table (uid text PRIMARY KEY,value text NOT NULL)");
        stat.execute("CREATE TABLE IF NOT EXISTS chain_table (uid text PRIMARY KEY,value text NOT NULL)");
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

    public String[] fetchChainTableItems(String[] uids) throws SQLException {
        Statement stat = this.connection.createStatement();
        String quotedUids = "";
        for (int i = 0; i < uids.length; i++) {
            quotedUids += "\"" + uids[i] + "\"";
            if (i != uids.length - 1) {
                quotedUids += ",";
            }
        }
        quotedUids += "";

        String sql = "SELECT uid, value FROM chain_table WHERE uid IN (" + quotedUids + ")";

        ResultSet rs = stat.executeQuery(sql);

        //
        // Recover all results
        //
        List<String> where = new ArrayList<String>();
        while (rs.next()) {
            where.add(rs.getString("value"));
        }
        String[] uidsAndValues = new String[where.size()];
        where.toArray(uidsAndValues);

        return uidsAndValues;
    }

    public HashMap<String, String> fetchEntryTableItems(String[] uids) throws SQLException {
        Statement stat = this.connection.createStatement();
        String quotedUids = "";
        for (int i = 0; i < uids.length; i++) {
            quotedUids += "\"" + uids[i] + "\"";
            if (i != uids.length - 1) {
                quotedUids += ",";
            }
        }
        quotedUids += "";

        String sql = "SELECT uid, value FROM entry_table WHERE uid IN (" + quotedUids + ")";

        ResultSet rs = stat.executeQuery(sql);

        //
        // Recover all results
        //
        HashMap<String, String> uidsAndValues = new HashMap<String, String>();
        while (rs.next()) {
            uidsAndValues.put(rs.getString("uid"), rs.getString("value"));
        }
        return uidsAndValues;
    }

    public void databaseUpsert(HashMap<String, String> uidsAndValues, String tableName) throws SQLException {
        Statement stat = this.connection.createStatement();
        for (Map.Entry<String, String> set : uidsAndValues.entrySet()) {
            stat.executeUpdate("INSERT OR REPLACE INTO " + tableName + "(uid, value) VALUES ('" + set.getKey() + "', '"
                + set.getValue() + "')");
        }
    }

    public HashMap<String, String> getAllKeyValueItems(String tableName) throws SQLException {
        Statement stat = this.connection.createStatement();
        String sql = "SELECT uid, value FROM " + tableName;
        ResultSet rs = stat.executeQuery(sql);
        HashMap<String, String> uidsAndValues = new HashMap<String, String>();
        while (rs.next()) {
            uidsAndValues.put(rs.getString("uid"), rs.getString("value"));
        }
        return uidsAndValues;
    }
}
