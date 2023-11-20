package com.cosmian.findex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.cosmian.TestUtils;
import com.cosmian.jna.findex.Findex;
import com.cosmian.jna.findex.RestToken;
import com.cosmian.jna.findex.ffi.KeywordSet;
import com.cosmian.jna.findex.ffi.SearchResults;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.utils.RestClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestFindexCloud {
    @Test
    public void testFindexCloud() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String baseUrl = System.getenv("COSMIAN_FINDEX_CLOUD_BASE_URL");

        if (baseUrl == null) {
            System.out.println("No COSMIAN_FINDEX_CLOUD_BASE_URL: ignoring");
            return;
        }

        if (!TestUtils.serverAvailable(baseUrl)) {
            throw new RuntimeException("Findex cloud is down");
        }

        String label = "Hello World!";

        RestClient client = new RestClient(baseUrl, Optional.empty());
        String response = client.json_post("/indexes", "{ \"name\": \"Test\" }");

        Index index = mapper.readValue(response, Index.class);

        String token = RestToken.generateNewToken(index.publicId, index.fetchEntriesKey, index.fetchChainsKey,
            index.upsertEntriesKey, index.insertChainsKey);

        Findex findex = new Findex();
        findex.instantiateRestBackend(label.getBytes(), token, baseUrl);

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Rest upsert");
        System.out.println("---------------------------------------");
        System.out.println("");

        HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords = new HashMap<>();

        indexedValuesAndWords.put(new Location(1337).toIndexedValue(),
            new HashSet<>(Arrays.asList(new Keyword("John"), new Keyword("Doe"))));

        indexedValuesAndWords.put(new Location(42).toIndexedValue(),
            new HashSet<>(Arrays.asList(new Keyword("Jane"), new Keyword("Doe"))));

        KeywordSet res = findex.add(indexedValuesAndWords);
        assertEquals(3, res.getResults().size(), "wrong number of new keywords returned");

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println("Findex Rest search");
        System.out.println("---------------------------------------");
        System.out.println("");

        SearchResults searchResults = findex.search(new String[] {"Doe"});
        assertEquals(new HashSet<>(Arrays.asList(new Long(1337), new Long(42))), searchResults.getNumbers());
    }

    public static class Index {
        @JsonProperty(value = "public_id")
        String publicId;

        @JsonProperty(value = "fetch_entries_key")
        byte[] fetchEntriesKey;

        @JsonProperty(value = "fetch_chains_key")
        byte[] fetchChainsKey;

        @JsonProperty(value = "upsert_entries_key")
        byte[] upsertEntriesKey;

        @JsonProperty(value = "insert_chains_key")
        byte[] insertChainsKey;

        Index() {
        }
    }
}
