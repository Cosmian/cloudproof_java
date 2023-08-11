package com.cosmian.jna.findex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.ffi.FindexUserCallbacks.SearchProgress;
import com.cosmian.jna.findex.ffi.Progress;
import com.cosmian.jna.findex.ffi.ProgressResults;
import com.cosmian.jna.findex.ffi.SearchResults;
import com.cosmian.jna.findex.ffi.UpsertResults;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public final class Findex extends FindexBase {

    public static UpsertResults upsert(byte[] key,
                                       byte[] label,
                                       Map<IndexedValue, Set<Keyword>> additions,
                                       Map<IndexedValue, Set<Keyword>> deletions,
                                       int entryTableNumber,
                                       Database db)
        throws CloudproofException {

        try (
            final Memory keyPointer = new Memory(key.length);
            final Memory labelPointer = new Memory(label.length)) {
            keyPointer.write(0, key, 0, key.length);
            labelPointer.write(0, label, 0, label.length);

            //
            // Prepare outputs
            //
            // Allocate the amount of memory needed to store all upserted keywords.
            byte[] newKeywordsBuffer = new byte[0];
            IntByReference newKeywordsBufferSize = new IntByReference(newKeywordsBuffer.length);
            long start = System.currentTimeMillis();
            int ffiCode = INSTANCE.h_upsert(newKeywordsBuffer, newKeywordsBufferSize,
                                     keyPointer, key.length,
                                     labelPointer, label.length,
                                     indexedValuesToJson(additions),
                                     indexedValuesToJson(deletions),
                                     entryTableNumber,
                                     db.fetchEntryCallback(),
                                     db.upsertEntryCallback(),
                                     db.upsertChainCallback());
            FindexCallbackException.rethrowOnErrorCode(ffiCode, start, System.currentTimeMillis());

            if (ffiCode != 0) {
                // Retry with correct allocated size
                newKeywordsBuffer = new byte[newKeywordsBufferSize.getValue()];
                long startRetry = System.currentTimeMillis();
                unwrap(INSTANCE.h_upsert(newKeywordsBuffer, newKeywordsBufferSize,
                                         keyPointer, key.length,
                                         labelPointer, label.length,
                                         indexedValuesToJson(additions),
                                         indexedValuesToJson(deletions),
                                         entryTableNumber,
                                         db.fetchEntryCallback(),
                                         db.upsertEntryCallback(),
                                         db.upsertChainCallback())
                        , startRetry);
            }

            byte[] newKeywordsBytes = Arrays.copyOfRange(newKeywordsBuffer, 0, newKeywordsBufferSize.getValue());

            return  new Leb128Reader(newKeywordsBytes).readObject(UpsertResults.class);
        }
    }

    public static UpsertResults upsert(byte[] key,
                                       byte[] label,
                                       Map<IndexedValue, Set<Keyword>> additions,
                                       Map<IndexedValue, Set<Keyword>> deletions,
                                       Database db)
        throws CloudproofException {
        // Make entryTableNumber equals to 1 by default
        return upsert(key, label, additions, deletions, 1, db);
    }

    public static UpsertResults upsert(IndexRequest request)
        throws CloudproofException {
        return upsert(request.key, request.label, request.additions, request.deletions, request.entryTableNumber,
                      request.database);
    }

    public static SearchResults search(SearchRequest request)
        throws CloudproofException {
        return search(request.key, request.label, request.keywords, request.entryTableNumber, request.database,
            request.searchProgress);
    }

    public static SearchResults search(byte[] key,
                                       byte[] label,
                                       Set<Keyword> keywords,
                                       int entryTableNumber,
                                       Database db)
        throws CloudproofException {
        return search(new SearchRequest(key, label, db).keywords(keywords));
    }

    public static SearchResults search(byte[] key,
                                       byte[] label,
                                       Set<Keyword> keywords,
                                       Database db)
        throws CloudproofException {
        // Make entryTableNumber equals to 1 by default
        return search(new SearchRequest(key, label, db).keywords(keywords).setEntryTableNumber(1));
    }

    public static SearchResults search(byte[] key,
                                       byte[] label,
                                       Set<Keyword> keyWords,
                                       int entryTableNumber,
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
                entryTableNumber,
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
                    entryTableNumber,
                    wrappedProgress,
                    db.fetchEntryCallback(),
                    db.fetchChainCallback()), startRetry);
            }

            byte[] indexedValuesBytes = Arrays.copyOfRange(indexedValuesBuffer, 0, indexedValuesBufferSize.getValue());

            return new Leb128Reader(indexedValuesBytes).readObject(SearchResults.class);
        }
    }

    /// `numReindexingBeforeFullSet`: if you compact the indexes every night
    /// this is the number of days to wait before be sure that a big portion of the
    /// indexes were checked.
    /// (see the coupon problem to understand why it's not 100% sure)
    public static void compact(byte[] oldMasterKey,
                               byte[] newMasterKey,
                               byte[] newLabel,
                               int numReindexingBeforeFullSet,
                               int entryTableNumber,
                               Database database)
        throws CloudproofException {

        try (final Memory oldMasterKeyPtr = new Memory(oldMasterKey.length);
            final Memory newMasterKeyPtr = new Memory(newMasterKey.length);
            final Memory newLabelPtr = new Memory(newLabel.length)) {

            oldMasterKeyPtr.write(0, oldMasterKey, 0, oldMasterKey.length);
            newMasterKeyPtr.write(0, newMasterKey, 0, newMasterKey.length);
            newLabelPtr.write(0, newLabel, 0, newLabel.length);

            long start = System.currentTimeMillis();
            // Indexes creation + insertion/update
            unwrap(INSTANCE.h_compact(
                oldMasterKeyPtr, oldMasterKey.length,
                newMasterKeyPtr, newMasterKey.length,
                newLabelPtr, newLabel.length,
                numReindexingBeforeFullSet,
                entryTableNumber,
                database.fetchAllEntryTableUidsCallback(),
                database.fetchEntryCallback(),
                database.fetchChainCallback(),
                database.updateLinesCallback(),
                database.listRemoveLocationsCallback()), start);
        }
    }

    public static void compact(byte[] oldMasterKey,
                               byte[] newMasterKey,
                               byte[] newLabel,
                               int numReindexingBeforeFullSet,
                               Database database)
        throws CloudproofException {
        // Make entryTableNumber equals to 1 by default
        compact(oldMasterKey, newMasterKey, newLabel, numReindexingBeforeFullSet, 1, database);
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
