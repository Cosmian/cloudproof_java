package com.cosmian.jna.findex;

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

    public static void upsert(
                              String token,
                              byte[] label,
                              Map<IndexedValue, Set<Keyword>> indexedValuesAndWords,
                              String baseUrl)
        throws CloudproofException {

        try (
            final Memory labelPointer = new Memory(label.length)) {
            labelPointer.write(0, label, 0, label.length);

            // Indexes creation + insertion/update
            unwrap(INSTANCE.h_upsert_cloud(
                token,
                labelPointer, label.length,
                indexedValuesToJson(indexedValuesAndWords),
                baseUrl));
        }
    }

    public static void upsert(
                              String token,
                              byte[] label,
                              Map<IndexedValue, Set<Keyword>> indexedValuesAndWords)
        throws CloudproofException {
        upsert(token, label, indexedValuesAndWords, null);
    }

    public static SearchResults search(SearchParams params)
        throws CloudproofException {
        return search(params.token, params.label, params.keywords, params.maxResultsPerKeyword, params.maxDepth,
            params.maxDepth, params.baseUrl);
    }

    public static SearchResults search(String token,
                                       byte[] label,
                                       Set<Keyword> keyWords)
        throws CloudproofException {
        return search(token, label, keyWords, 0, -1, 0, null);
    }

    public static SearchResults search(String token,
                                       byte[] label,
                                       Set<Keyword> keyWords,
                                       int maxResultsPerKeyword,
                                       int maxDepth)
        throws CloudproofException {
        return search(token, label, keyWords, maxResultsPerKeyword, maxDepth, 0, null);
    }

    public static SearchResults search(String token,
                                       byte[] label,
                                       Set<Keyword> keyWords,
                                       int maxResultsPerKeyword,
                                       int maxDepth,
                                       int insecureFetchChainsBatchSize,
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
                maxResultsPerKeyword,
                maxDepth,
                insecureFetchChainsBatchSize,
                baseUrl);
            if (ffiCode != 0) {
                // Retry with correct allocated size
                indexedValuesBuffer = new byte[indexedValuesBufferSize.getValue()];
                ffiCode = INSTANCE.h_search_cloud(
                    indexedValuesBuffer, indexedValuesBufferSize,
                    token,
                    labelPointer, label.length,
                    wordsJson,
                    maxResultsPerKeyword,
                    maxDepth,
                    insecureFetchChainsBatchSize,
                    baseUrl);
                if (ffiCode != 0) {
                    throw new CloudproofException(get_last_error(4095));
                }
            }

            byte[] indexedValuesBytes = Arrays.copyOfRange(indexedValuesBuffer, 0, indexedValuesBufferSize.getValue());

            return new Leb128Reader(indexedValuesBytes).readObject(SearchResults.class);
        }
    }

    static public class SearchParams extends FindexBase.SearchParams<SearchParams> {
        private String token;

        private String baseUrl;

        @Override
        SearchParams self() {
            return this;
        }

        public SearchParams token(String token) {
            this.token = token;
            return this;
        }

        public SearchParams baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
    }
}
