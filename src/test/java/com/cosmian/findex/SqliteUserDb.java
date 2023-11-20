package com.cosmian.findex;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.cosmian.jna.findex.FilterLocations;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

public class SqliteUserDb implements FilterLocations, Closeable {

    final Connection connection;

    public SqliteUserDb() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        this.createTable();
    }

    public SqliteUserDb(String url) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + url);
        this.createTable();
    }

    public Connection getConnection() {
        return connection;
    }

    void createTable() throws SQLException {
        Statement stat = this.connection.createStatement();
        stat.executeUpdate(
            "CREATE TABLE IF NOT EXISTS users (id integer PRIMARY KEY, firstName text NOT NULL, lastName text NOT NULL, email text NOT NULL, phone text NOT NULL, country text NOT NULL, region text NOT NULL, employeeNumber text NOT NULL, security text NOT NULL)");
    }

    String questionMarks(int count) {
        String lotsOfQuestions = "";
        for (int i = 0; i < count; i++) {
            lotsOfQuestions += "?";
            if (i != count - 1) {
                lotsOfQuestions += ",";
            }
        }
        return lotsOfQuestions;
    }

    public void flush(String tableName) throws SQLException {
        connection.createStatement().execute("DELETE FROM users");
    }

    public void insert(UsersDataset[] testFindexDataset) throws SQLException {
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

    public void deleteUsers(List<Uid32> uids) throws SQLException {
        PreparedStatement pstmt = this.connection
            .prepareStatement("DELETE FROM users WHERE uid IN (" + questionMarks(uids.size()) + ")");

        int count = 1;
        for (Uid32 uid : uids) {
            pstmt.setBytes(count, uid.getBytes());
            count += 1;
        }
        pstmt.execute();
    }

    public Map<byte[], byte[]> getAllKeyValueItems() throws SQLException {
        Statement stat = this.connection.createStatement();
        String sql = "SELECT uid, value FROM users";
        ResultSet rs = stat.executeQuery(sql);
        HashMap<byte[], byte[]> uidsAndValues = new HashMap<byte[], byte[]>();
        while (rs.next()) {
            uidsAndValues.put(rs.getBytes("uid"), rs.getBytes("value"));
        }
        return uidsAndValues;
    }

    public Set<Integer> fetchUsersById(List<Integer> ids)
        throws SQLException {
        PreparedStatement pstmt = this.connection
            .prepareStatement("SELECT id FROM users WHERE id IN (" + questionMarks(ids.size()) + ")");
        int count = 1;
        for (Integer bs : ids) {
            pstmt.setInt(count, bs);
            count += 1;
        }
        ResultSet rs = pstmt.executeQuery();

        Set<Integer> removedIds = new HashSet<Integer>();
        while (rs.next()) {
            removedIds.add(rs.getInt("id"));
        }
        return removedIds;
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
    public List<Location> filter(List<Location> locations) throws CloudproofException {
        System.out.println("Filtering " + locations.size() + " locations");
        try {
            List<Integer> ids = locations.stream()
                .map((Location location) -> (int) location.toNumber())
                .collect(Collectors.toList());
            Set<Integer> knownIds = fetchUsersById(ids);
            List<Location> res = ids.stream()
                .filter((Integer id) -> knownIds.contains(id))
                .map((Integer id) -> new Location(id))
                .collect(Collectors.toList());
            System.out.println("Remaining " + res.size() + " locations");
            return res;
        } catch (SQLException e) {
            throw new CloudproofException("Failed list removed locations: " + e.toString());
        }
    }

}
