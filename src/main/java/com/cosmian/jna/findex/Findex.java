package com.cosmian.jna.findex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.ffi.FindexUserCallbacks.SearchProgress;
import com.cosmian.jna.findex.ffi.Progress;
import com.cosmian.jna.findex.ffi.ProgressResults;
import com.cosmian.jna.findex.ffi.SearchResults;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public final class Findex extends FindexBase {

    public static void upsert(
                              byte[] key,
                              byte[] label,
                              Map<IndexedValue, Set<Keyword>> indexedValuesAndWords,
                              Database db)
        throws CloudproofException {

        try (
            final Memory keyPointer = new Memory(key.length);
            final Memory labelPointer = new Memory(label.length)) {
            keyPointer.write(0, key, 0, key.length);
            labelPointer.write(0, label, 0, label.length);

            long start = System.currentTimeMillis();
            // Indexes creation + insertion/update
            unwrap(INSTANCE.h_upsert(
                keyPointer, key.length,
                labelPointer, label.length,
                indexedValuesToJson(indexedValuesAndWords),
                db.fetchEntryCallback(),
                db.upsertEntryCallback(),
                db.upsertChainCallback()), start);
        }
    }

    public static void upsert(IndexRequest request)
        throws CloudproofException {
        upsert(request.key, request.label, request.indexedValuesAndWords, request.database);
    }

    public static SearchResults search(SearchRequest request)
        throws CloudproofException {
        return search(request.key, request.label, request.keywords, request.maxResultsPerKeyword, request.maxDepth,
            request.maxDepth, request.database, request.searchProgress);
    }

    public static SearchResults search(byte[] key,
                                       byte[] label,
                                       Set<Keyword> keywords,
                                       Database db)
        throws CloudproofException {
        return search(new SearchRequest(key, label, db).keywords(keywords));
    }

    public static SearchResults search(byte[] key,
                                       byte[] label,
                                       Set<Keyword> keyWords,
                                       int maxResultsPerKeyword,
                                       int maxDepth,
                                       int insecureFetchChainsBatchSize,
                                       Database db,
                                       SearchProgress progressCallback)
        throws CloudproofException {
        //
        // Prepare outputs
        //
        // start with an arbitration buffer allocation size of 131072 (around 4096
        // indexedValues)
        byte[] indexedValuesBuffer = new byte[131072];
        IntByReference indexedValuesBufferSize = new IntByReference(indexedValuesBuffer.length);

        // Findex master keys
        if (key == null) {
            throw new CloudproofException("Key cannot be null");
        }

        // wrap progress callback
        Progress wrappedProgress = new Progress(progressCallback);

        try (final Memory keyPointer = new Memory(key.length);
            final Memory labelPointer = new Memory(label.length)) {

            keyPointer.write(0, key, 0, key.length);
            labelPointer.write(0, label, 0, label.length);

            String wordsJson = keywordsToJson(keyWords);

            // Indexes creation + insertion/update
            long start = System.currentTimeMillis();
            int ffiCode = INSTANCE.h_search(
                indexedValuesBuffer, indexedValuesBufferSize,
                keyPointer, key.length,
                labelPointer, label.length,
                wordsJson,
                maxResultsPerKeyword,
                maxDepth,
                insecureFetchChainsBatchSize,
                wrappedProgress,
                db.fetchEntryCallback(),
                db.fetchChainCallback());

            FindexCallbackException.rethrowOnErrorCode(ffiCode, start, System.currentTimeMillis());

            if (ffiCode != 0) {
                // Retry with correct allocated size
                indexedValuesBuffer = new byte[indexedValuesBufferSize.getValue()];
                long startRetry = System.currentTimeMillis();
                unwrap(INSTANCE.h_search(
                    indexedValuesBuffer, indexedValuesBufferSize,
                    keyPointer, key.length,
                    labelPointer, label.length,
                    wordsJson,
                    maxResultsPerKeyword,
                    maxDepth,
                    insecureFetchChainsBatchSize,
                    wrappedProgress,
                    db.fetchEntryCallback(),
                    db.fetchChainCallback()), startRetry);
            }

            byte[] indexedValuesBytes = Arrays.copyOfRange(indexedValuesBuffer, 0, indexedValuesBufferSize.getValue());

            return new Leb128Reader(indexedValuesBytes).readObject(SearchResults.class);
        }
    }

    /// `number_of_reindexing_phases_before_full_set`: if you compact the indexes
    /// every night
    /// this is the number of days to wait before be sure that a big portion of the
    /// indexes were checked
    /// (see the coupon problem to understand why it's not 100% sure)
    public static void compact(int numberOfReindexingPhasesBeforeFullSet,
                               byte[] existingKey,
                               byte[] newKey,
                               byte[] label,
                               Database database)
        throws CloudproofException {

        try (final Memory existingKeyPointer = new Memory(existingKey.length);
            final Memory newKeyPointer = new Memory(newKey.length);
            final Memory labelPointer = new Memory(label.length)) {

            existingKeyPointer.write(0, existingKey, 0, existingKey.length);
            newKeyPointer.write(0, newKey, 0, newKey.length);
            labelPointer.write(0, label, 0, label.length);

            long start = System.currentTimeMillis();
            // Indexes creation + insertion/update
            unwrap(INSTANCE.h_compact(
                numberOfReindexingPhasesBeforeFullSet,
                existingKeyPointer, existingKey.length,
                newKeyPointer, newKey.length,
                labelPointer, label.length,
                database.fetchAllEntryTableUidsCallback(),
                database.fetchEntryCallback(),
                database.fetchChainCallback(),
                database.updateLinesCallback(),
                database.listRemoveLocationsCallback()), start);
        }
    }

    static public class SearchRequest extends FindexBase.SearchRequest<SearchRequest> {
        protected byte[] key;

        protected Database database;

        protected SearchProgress searchProgress = new SearchProgress() {
            @Override
            public boolean notify(ProgressResults results) throws CloudproofException {
                return true;
            }
        };

        public SearchRequest(byte[] key, byte[] label, Database database) {
            this.key = key;
            this.label = label;
            this.database = database;
        }

        public SearchRequest(byte[] key, String label, Database database) {
            this.key = key;
            this.label = label.getBytes(StandardCharsets.UTF_8);
            this.database = database;
        }

        @Override
        SearchRequest self() {
            return this;
        }

        public SearchRequest searchProgress(SearchProgress searchProgress) {
            this.searchProgress = searchProgress;
            return this;
        }
    }

    static public class IndexRequest extends FindexBase.IndexRequest<IndexRequest> {
        protected byte[] key;

        protected Database database;

        public IndexRequest(byte[] key, byte[] label, Database database) {
            this.key = key;
            this.label = label;
            this.database = database;
        }

        public IndexRequest(byte[] key, String label, Database database) {
            this.key = key;
            this.label = label.getBytes(StandardCharsets.UTF_8);
            this.database = database;
        }

        @Override
        IndexRequest self() {
            return this;
        }
    }
}
