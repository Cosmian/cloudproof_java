package com.cosmian.jna.findex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.ffi.SearchResults;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public final class FindexCloud extends FindexBase {

    public static String generateNewToken(
                              String indexId,
                              byte[] fetchEntriesSeed,
                              byte[] fetchChainsSeed,
                              byte[] upsertEntriesSeed,
                              byte[] insertChainsSeed)
        throws CloudproofException {

        try (
            final Memory fetchEntriesSeedPointer = new Memory(fetchEntriesSeed.length);
            final Memory fetchChainsSeedPointer = new Memory(fetchChainsSeed.length);
            final Memory upsertEntriesSeedPointer = new Memory(upsertEntriesSeed.length);
            final Memory insertChainsSeedPointer = new Memory(insertChainsSeed.length);) {
            fetchEntriesSeedPointer.write(0, fetchEntriesSeed, 0, fetchEntriesSeed.length);
            fetchChainsSeedPointer.write(0, fetchChainsSeed, 0, fetchChainsSeed.length);
            upsertEntriesSeedPointer.write(0, upsertEntriesSeed, 0, upsertEntriesSeed.length);
            insertChainsSeedPointer.write(0, insertChainsSeed, 0, insertChainsSeed.length);

            byte[] tokenBuffer = new byte[200];
            IntByReference tokenBufferSize = new IntByReference(tokenBuffer.length);

            unwrap(INSTANCE.h_generate_new_token(
                tokenBuffer,
                tokenBufferSize,
                indexId,
                fetchEntriesSeedPointer, fetchEntriesSeed.length,
                fetchChainsSeedPointer, fetchChainsSeed.length,
                upsertEntriesSeedPointer, upsertEntriesSeed.length,
                insertChainsSeedPointer, insertChainsSeed.length));

            byte[] tokenBytes = Arrays.copyOfRange(tokenBuffer, 0, tokenBufferSize.getValue());

            return new String(tokenBytes, StandardCharsets.UTF_8);
        }
    }

    public static void upsert(
                              String token,
                              byte[] label,
                              Map<IndexedValue, Set<Keyword>> additions,
                              Map<IndexedValue, Set<Keyword>> deletions,
                              String baseUrl)
        throws CloudproofException {

        try (
            final Memory labelPointer = new Memory(label.length)) {
            labelPointer.write(0, label, 0, label.length);

            // Indexes creation + insertion/update
            unwrap(INSTANCE.h_upsert_cloud(
                token,
                labelPointer, label.length,
                indexedValuesToJson(additions),
                indexedValuesToJson(deletions),
                baseUrl));
        }
    }

    public static void upsert(IndexRequest request)
        throws CloudproofException {
        upsert(request.token, request.label, request.additions, request.deletions, request.baseUrl);
    }

    public static void upsert(
                              String token,
                              byte[] label,
                              Map<IndexedValue, Set<Keyword>> additions,
                              Map<IndexedValue, Set<Keyword>> deletions)
        throws CloudproofException {
        upsert(token, label, additions, deletions, null);
    }

    public static SearchResults search(SearchRequest request)
        throws CloudproofException {
        return search(request.token, request.label, request.keywords, request.baseUrl);
    }

    public static SearchResults search(String token,
                                       byte[] label,
                                       Set<Keyword> keyWords)
        throws CloudproofException {
        return search(token, label, keyWords, null);
    }

    public static SearchResults search(String token,
                                       byte[] label,
                                       Set<Keyword> keyWords,
                                       String baseUrl)
        throws CloudproofException {
        //
        // Prepare outputs
        //
        // start with an arbitration buffer allocation size of 131072 (around 4096
        // indexedValues)
        byte[] indexedValuesBuffer = new byte[131072];
        IntByReference indexedValuesBufferSize = new IntByReference(indexedValuesBuffer.length);

        if (token == null) {
            throw new CloudproofException("Token cannot be null");
        }
        try (final Memory labelPointer = new Memory(label.length)) {
            labelPointer.write(0, label, 0, label.length);

            String wordsJson = keywordsToJson(keyWords);

            // Indexes creation + insertion/update
            int ffiCode = INSTANCE.h_search_cloud(
                indexedValuesBuffer, indexedValuesBufferSize,
                token,
                labelPointer, label.length,
                wordsJson,
                baseUrl);
            if (ffiCode != 0) {
                // Retry with correct allocated size
                indexedValuesBuffer = new byte[indexedValuesBufferSize.getValue()];
                ffiCode = INSTANCE.h_search_cloud(
                    indexedValuesBuffer, indexedValuesBufferSize,
                    token,
                    labelPointer, label.length,
                    wordsJson,
                    baseUrl);
                if (ffiCode != 0) {
                    throw new CloudproofException(get_last_error(4095));
                }
            }

            byte[] indexedValuesBytes = Arrays.copyOfRange(indexedValuesBuffer, 0, indexedValuesBufferSize.getValue());

            return new Leb128Reader(indexedValuesBytes).readObject(SearchResults.class);
        }
    }

    static public class SearchRequest extends FindexBase.SearchRequest<SearchRequest> {
        private String token;

        private String baseUrl = System.getenv("COSMIAN_FINDEX_CLOUD_BASE_URL");

        public SearchRequest(String token, byte[] label) {
            this.token = token;
            this.label = label;
        }

        public SearchRequest(String token, String label) {
            this.token = token;
            this.label = label.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        SearchRequest self() {
            return this;
        }

        public SearchRequest token(String token) {
            this.token = token;
            return this;
        }

        public SearchRequest baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
    }

    static public class IndexRequest extends FindexBase.IndexRequest<IndexRequest> {
        private String token;

        private String baseUrl = System.getenv("COSMIAN_FINDEX_CLOUD_BASE_URL");

        public IndexRequest(String token, byte[] label) {
            this.token = token;
            this.label = label;
        }

        public IndexRequest(String token, String label) {
            this.token = token;
            this.label = label.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        IndexRequest self() {
            return this;
        }

        public IndexRequest baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
    }
}
