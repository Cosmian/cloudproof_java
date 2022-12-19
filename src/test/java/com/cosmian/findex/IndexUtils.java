package com.cosmian.findex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Resources;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IndexUtils {

    /**
     * Index the given Datasets
     * 
     * @param testFindexDataset the list of {@link UsersDataset}
     * @return the clear text index
     */
    public static HashMap<IndexedValue, Set<Keyword>> index(UsersDataset[] testFindexDataset) {

        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = new HashMap<>();
        Set<Keyword> keywords = new HashSet<>();
        for (UsersDataset user : testFindexDataset) {
            indexedValuesAndWords.put(userIdToLocation(user.id).toIndexedValue(), user.values());
            keywords.addAll(user.values());
        }

        // stats
        System.out.println("Num keywords: " + keywords.size() + ", indexed Values: " + indexedValuesAndWords.size());
        return indexedValuesAndWords;
    }

    /**
     * Transform the user id, which is the database unique key for the users, to a location which is the object used to
     * hold it in the index
     */
    public static Location userIdToLocation(int userId) {
        return new Location(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(userId).array());
    }

    /**
     * Extract the user Id from the location returned by the index
     * 
     * @param location
     */
    public static int locationToUserId(Location location) {
        return ByteBuffer.wrap(location.getBytes()).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    /**
     * A Sha 256 hash
     * 
     * @param data the data to hash
     * @return the 32 byte hash value
     * @throws NoSuchAlgorithmException
     */
    public byte[] hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] passHash = sha256.digest(data);
        return passHash;
    }

    public static byte[] loadKey() throws IOException {
        // return Base64.getDecoder().decode(Resources.load_resource("findex/key.b64"));
        byte[] key = new byte[16];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(key);
        return key;
    }

    public static byte[] loadLabel() throws IOException {
        return Resources.load_resource_as_bytes("findex/label");
    }

    public static UsersDataset[] loadDatasets() throws IOException, CloudproofException {
        String dataJson = Resources.load_resource("findex/data.json");
        return UsersDataset.fromJson(dataJson);
    }

    public static int[] loadExpectedDBLocations() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String expectedSearchResultsInt = Resources.load_resource("findex/expected_db_uids.json");
        int[] expectedDbLocations = mapper.readValue(expectedSearchResultsInt, int[].class);
        Arrays.sort(expectedDbLocations);

        return expectedDbLocations;
    }

    /*
     * Helper function to transform the results returned by the FFI to a sorted array of int (representing the DB id of
     * users).
     */
    public static int[] searchResultsToDbUids(Map<Keyword, Set<Location>> searchResults) throws Exception {
        HashSet<Integer> dbLocations = new HashSet<>();
        for (Set<Location> locations : searchResults.values()) {
            for (Location location : locations) {
                int dbLocation = locationToUserId(location);
                dbLocations.add(dbLocation);
            }
        }
        int[] dbUids = dbLocations.stream().mapToInt(Integer::intValue).toArray();
        Arrays.sort(dbUids);
        return dbUids;
    }

}
