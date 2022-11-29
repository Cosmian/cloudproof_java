package com.cosmian.jna.findex;

import java.util.HashMap;
import java.util.List;

import com.cosmian.CloudproofException;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * This maps the Findex Secure Searchable Encryption written in Rust
 */
public interface FindexWrapper extends Library {

    int set_error(String errorMsg);

    int get_last_error(byte[] output, IntByReference outputSize);

    /* Internal callbacks FFI */
    interface FetchEntryCallback extends Callback {
        int apply(Pointer output, IntByReference outputSize, Pointer uidsPointer, int uidsLength)
            throws CloudproofException;
    }

    interface FetchAllEntryCallback extends Callback {
        int apply(Pointer output, IntByReference outputSize, int numberOfEntries) throws CloudproofException;
    }

    interface FetchChainCallback extends Callback {
        int apply(Pointer output, IntByReference outputSize, Pointer uidsPointer, int uidsLength)
            throws CloudproofException;
    }

    interface UpsertEntryCallback extends Callback {
        int apply(Pointer entries, int entriesLength) throws CloudproofException;
    }

    interface UpsertChainCallback extends Callback {
        int apply(Pointer chains, int chainsLength) throws CloudproofException;
    }

    interface UpdateLinesCallback extends Callback {
        int apply(Pointer removedChains, int removedChainsLength, Pointer newEntries, int newEntriesLength,
            Pointer newChains, int newChainsLength) throws CloudproofException;
    }

    interface ListRemovedLocationsCallback extends Callback {
        int apply(Pointer output, IntByReference outputSize, Pointer locations, int locationsLength)
            throws CloudproofException;
    }

    interface ProgressCallback extends Callback {
        boolean apply(Pointer output, int outputSize)
            throws CloudproofException;
    }

    /* Customer high-level callbacks */
    interface FetchEntryInterface {
        public HashMap<byte[], byte[]> fetch(List<byte[]> uids) throws CloudproofException;
    }

    interface FetchAllEntryInterface {
        public HashMap<byte[], byte[]> fetch() throws CloudproofException;
    }

    interface FetchChainInterface {
        public HashMap<byte[], byte[]> fetch(List<byte[]> uids) throws CloudproofException;
    }

    interface UpsertEntryInterface {
        public void upsert(HashMap<byte[], byte[]> uidsAndValues) throws CloudproofException;
    }

    interface UpsertChainInterface {
        public void upsert(HashMap<byte[], byte[]> uidsAndValues) throws CloudproofException;
    }

    interface UpdateLinesInterface {
        public void update(List<byte[]> removedChains, HashMap<byte[], byte[]> newEntries,
            HashMap<byte[], byte[]> newChains) throws CloudproofException;
    }

    interface ListRemovedLocationsInterface {
        public List<Location> list(List<Location> locations) throws CloudproofException;
    }

    interface ProgressInterface {
        public boolean list(List<byte[]> indexedValues) throws CloudproofException;
    }

    int h_upsert(String masterKeysJson, Pointer labelPointer, int labelSize, String dbUidsAndWordsJson,
        FetchEntryCallback fetchEntry, UpsertEntryCallback upsertEntry, UpsertChainCallback upsertChain);

    int h_graph_upsert(String masterKeysJson, Pointer labelPointer, int labelSize, String dbUidsAndWordsJson,
        FetchEntryCallback fetchEntry, UpsertEntryCallback upsertEntry, UpsertChainCallback upsertChain);

    int h_compact(int numberOfReindexingPhasesBeforeFullSet, String masterKeysJson, Pointer labelPointer, int labelSize,
        FetchEntryCallback fetchEntry, FetchChainCallback fetchChain, FetchAllEntryCallback fetchAllEntry,
        UpdateLinesCallback updateLines, ListRemovedLocationsCallback listRemovedLocations);

    int h_search(byte[] dbUidsPtr, IntByReference dbUidsSize, Pointer keyKPointer, int keyKLength, Pointer labelPointer,
        int labelSize, String words, int loopIterationLimit, int maxDepth, ProgressCallback progress,
        FetchEntryCallback fetchEntry,
        FetchChainCallback fetchChain);
}
