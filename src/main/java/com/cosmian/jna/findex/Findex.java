package com.cosmian.jna.findex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.ffi.FindexUserCallbacks.SearchInterrupt;
import com.cosmian.jna.findex.ffi.Interrupt;
import com.cosmian.jna.findex.ffi.ProgressResults;
import com.cosmian.jna.findex.ffi.SearchResults;
import com.cosmian.jna.findex.ffi.UpsertResults;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.serde.Leb128Writer;
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

            byte[] additionsBytes = Leb128Writer.serializeMapOfSet(additions);
            final Memory additionsPointer = new Memory(additionsBytes.length);
            additionsPointer.write(0, additionsBytes, 0, additionsBytes.length);

            byte[] deletionsBytes = Leb128Writer.serializeMapOfSet(deletions);
            Memory deletionsPointer = null;
            if (deletionsBytes.length > 0) {
                deletionsPointer = new Memory(deletionsBytes.length);
                deletionsPointer.write(0, deletionsBytes, 0, deletionsBytes.length);
            }

            // Do not allocate memory. The Rust FFI function will directly
            // return after setting newKeywordsBufferSize to an upper bound on
            // the amount of memory to allocate.
            byte[] newKeywordsBuffer = new byte[0];
            IntByReference newKeywordsBufferSize = new IntByReference();

            long start = System.currentTimeMillis();
            int ffiCode = INSTANCE.h_upsert(newKeywordsBuffer, newKeywordsBufferSize,
                keyPointer, key.length,
                labelPointer, label.length,
                additionsPointer, additionsBytes.length,
                deletionsPointer, deletionsBytes.length,
                entryTableNumber,
                db.fetchEntryCallback(),
                db.upsertEntryCallback(),
                db.insertChainCallback());

            FindexCallbackException.rethrowOnErrorCode(ffiCode, start, System.currentTimeMillis());

            if (ffiCode == 1) {
                newKeywordsBuffer = new byte[newKeywordsBufferSize.getValue()];
                unwrap(INSTANCE.h_upsert(newKeywordsBuffer, newKeywordsBufferSize,
                    keyPointer, key.length,
                    labelPointer, label.length,
                    additionsPointer, additionsBytes.length,
                    deletionsPointer, deletionsBytes.length,
                    entryTableNumber,
                    db.fetchEntryCallback(),
                    db.upsertEntryCallback(),
                    db.insertChainCallback()));
            } else if (ffiCode != 0) {
                unwrap(ffiCode);
            }

            byte[] newKeywordsBytes = Arrays.copyOfRange(newKeywordsBuffer, 0, newKeywordsBufferSize.getValue());
            return new Leb128Reader(newKeywordsBytes).readObject(UpsertResults.class);
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

    //----------
    //--- Search
    //----------
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
        return search(new SearchRequest(key, label, db).keywords(keywords).setEntryTableNumber(entryTableNumber));
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
                                       Set<Keyword> keywords,
                                       int entryTableNumber,
                                       Database db,
                                       SearchInterrupt interruptCallback)
        throws CloudproofException {
        //
        // Prepare outputs
        //
        // start with an arbitration buffer allocation size of 131072 (around 4096
        // indexedValues)
        byte[] searchResultsBuffer = new byte[131072];
        IntByReference searchResultsBufferSize = new IntByReference(searchResultsBuffer.length);

        // Findex master keys
        if (key == null) {
            throw new CloudproofException("Key cannot be null");
        }

        // wrap Interrupt callback
        Interrupt wrappedInterrupt = new Interrupt(interruptCallback);

        byte[] serializedKeywords = Leb128Writer.serializeCollection(keywords);

        try (final Memory keyPointer = new Memory(key.length);
            final Memory labelPointer = new Memory(label.length);
            final Memory keywordsPointer = new Memory(serializedKeywords.length)) {

            keyPointer.write(0, key, 0, key.length);
            labelPointer.write(0, label, 0, label.length);
            keywordsPointer.write(0, serializedKeywords, 0, serializedKeywords.length);

            // Indexes creation + insertion/update
            long start = System.currentTimeMillis();
            int ffiCode = INSTANCE.h_search(
                searchResultsBuffer, searchResultsBufferSize,
                keyPointer, key.length,
                labelPointer, label.length,
                keywordsPointer, serializedKeywords.length,
                entryTableNumber,
                wrappedInterrupt,
                db.fetchEntryCallback(),
                db.fetchChainCallback());

            FindexCallbackException.rethrowOnErrorCode(ffiCode, start, System.currentTimeMillis());

            if (ffiCode != 0) {
                // Retry with correct allocated size
                searchResultsBuffer = new byte[searchResultsBufferSize.getValue()];
                long startRetry = System.currentTimeMillis();
                unwrap(INSTANCE.h_search(
                    searchResultsBuffer, searchResultsBufferSize,
                    keyPointer, key.length,
                    labelPointer, label.length,
                    keywordsPointer, serializedKeywords.length,
                    entryTableNumber,
                    wrappedInterrupt,
                    db.fetchEntryCallback(),
                    db.fetchChainCallback()), startRetry);
            }

            byte[] indexedValuesBytes = Arrays.copyOfRange(searchResultsBuffer, 0, searchResultsBufferSize.getValue());

            return new Leb128Reader(indexedValuesBytes).readObject(SearchResults.class);
        }
    }

    /// `nCompactToFull`: if you compact the indexes every night
    /// this is the number of days to wait before be sure that a big portion of the
    /// indexes were checked.
    /// (see the coupon problem to understand why it's not 100% sure)
    public static void compact(byte[] oldKey,
                               byte[] newKey,
                               byte[] oldLabel,
                               byte[] newLabel,
                               int nCompactToFull,
                               int entryTableNumber, //TODO(ecse): remove param
                               Database database)
        throws CloudproofException {

        try (final Memory oldKeyPtr = new Memory(oldKey.length);
            final Memory newKeyPtr = new Memory(newKey.length);
            final Memory newLabelPtr = new Memory(newLabel.length);
            final Memory oldLabelPtr = new Memory(oldLabel.length)) {

            oldKeyPtr.write(0, oldKey, 0, oldKey.length);
            newKeyPtr.write(0, newKey, 0, newKey.length);
            newLabelPtr.write(0, newLabel, 0, newLabel.length);

            long start = System.currentTimeMillis();
            // Indexes creation + insertion/update
            unwrap(INSTANCE.h_compact(
                oldKeyPtr, oldKey.length,
                newKeyPtr, newKey.length,
                oldLabelPtr, oldLabel.length,
                newLabelPtr, newLabel.length,
                nCompactToFull,
                entryTableNumber,
                database.fetchEntryCallback(),
                database.fetchChainCallback(),
                database.upsertEntryCallback(),
                database.insertChainCallback(),
                database.deleteEntryCallback(),
                database.deleteChainCallback(),
                database.dumpTokensCallback(),
                database.filterObsoleteLocationsCallback()), // TODO(ecse)
                start);
        }
    }

    public static void compact(byte[] oldKey,
                               byte[] newKey,
                               byte[] oldLabel,
                               byte[] newLabel,
                               int nCompactToFull,
                               Database database)
        throws CloudproofException {
        // Make entryTableNumber equals to 1 by default
        compact(oldKey, newKey, oldLabel, newLabel, nCompactToFull, 1, database);
    }

    static public class SearchRequest extends FindexBase.SearchRequest<SearchRequest> {
        protected byte[] key;

        protected Database database;

        protected SearchInterrupt searchProgress = new SearchInterrupt() {
            @Override
            public boolean notify(ProgressResults results) throws CloudproofException {
                return false;
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

        public SearchRequest searchProgress(SearchInterrupt searchProgress) {
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
