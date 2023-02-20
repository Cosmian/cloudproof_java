package com.cosmian.findex;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Resources;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IndexUtils {

    /**
     * Index the given Datasets
     *
     * @param testFindexDataset the list of {@link UsersDataset}
     * @return the clear text index
     */
    public static HashMap<Location, Set<Keyword>> index(UsersDataset[] testFindexDataset) {

        HashMap<Location, Set<Keyword>> indexedValuesAndWords = new HashMap<>();
        Set<Keyword> keywords = new HashSet<>();
        for (UsersDataset user : testFindexDataset) {
            indexedValuesAndWords.put(new Location(user.id), user.values());
            keywords.addAll(user.values());
        }

        // stats
        System.out.println("Num keywords: " + keywords.size() + ", indexed Values: " + indexedValuesAndWords.size());
        return indexedValuesAndWords;
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

    public static byte[] generateKey() throws IOException {
        // return Base64.getDecoder().decode(Resources.load_resource("findex/key.b64"));
        byte[] key = new byte[16];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(key);
        return key;
    }

    public static byte[] loadKey() throws IOException {
        return Base64.getDecoder().decode(Resources.load_resource("findex/key.b64"));
    }

    public static byte[] loadLabel() throws IOException {
        return Resources.load_resource_as_bytes("findex/label");
    }

    public static UsersDataset[] loadDatasets() throws IOException, CloudproofException {
        String dataJson = Resources.load_resource("findex/data.json");
        return UsersDataset.fromJson(dataJson);
    }

    public static Set<Long> loadExpectedDBLocations() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String expectedSearchResultsInt = Resources.load_resource("findex/expected_db_uids.json");
        return mapper.readValue(expectedSearchResultsInt, new TypeReference<Set<Long>>() {
        });
    }
}
