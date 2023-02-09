package com.cosmian.jna.findex;

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
import com.cosmian.jna.findex.structs.Location;
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

            // Indexes creation + insertion/update
            unwrap(INSTANCE.h_upsert(
                keyPointer, key.length,
                labelPointer, label.length,
                indexedValuesToJson(indexedValuesAndWords),
                db.fetchEntryCallback(),
                db.upsertEntryCallback(),
                db.upsertChainCallback()));
        }
    }

    public static Map<Keyword, Set<Location>> search(byte[] key,
                                                     byte[] label,
                                                     Set<Keyword> keyWords,
                                                     Database db)
        throws CloudproofException {
        return search(key, label, keyWords, 0, -1, 0, db);
    }

    public static Map<Keyword, Set<Location>> search(byte[] key,
                                                     byte[] label,
                                                     Set<Keyword> keyWords,
                                                     int maxResultsPerKeyword,
                                                     int maxDepth,
                                                     Database db)
        throws CloudproofException {
        return search(key, label, keyWords, maxResultsPerKeyword, maxDepth, 0, db);
    }

    public static Map<Keyword, Set<Location>> search(byte[] key,
                                                     byte[] label,
                                                     Set<Keyword> keyWords,
                                                     int maxResultsPerKeyword,
                                                     int maxDepth,
                                                     int insecureFetchChainsBatchSize,
                                                     Database db)
        throws CloudproofException {
        return search(key, label, keyWords, maxResultsPerKeyword, maxDepth, insecureFetchChainsBatchSize, db,
            new SearchProgress() {
                @Override
                public boolean notify(ProgressResults results) throws CloudproofException {
                    // default progress callback
                    return true;
                }
            });
    }

    public static Map<Keyword, Set<Location>> search(byte[] key,
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
            if (ffiCode != 0) {
                // Retry with correct allocated size
                indexedValuesBuffer = new byte[indexedValuesBufferSize.getValue()];
                ffiCode = INSTANCE.h_search(
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
                if (ffiCode != 0) {
                    throw new CloudproofException(get_last_error(4095));
                }
            }

            byte[] indexedValuesBytes = Arrays.copyOfRange(indexedValuesBuffer, 0, indexedValuesBufferSize.getValue());

            SearchResults searchResults = new Leb128Reader(indexedValuesBytes).readObject(SearchResults.class);
            return searchResults.getResults();
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
                database.listRemoveLocationsCallback()));
        }
    }
}
