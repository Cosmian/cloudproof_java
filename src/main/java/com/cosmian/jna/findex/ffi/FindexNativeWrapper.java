package com.cosmian.jna.findex.ffi;

import com.cosmian.utils.CloudproofException;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * This maps the Findex Secure Searchable Encryption written in Rust
 */
public interface FindexNativeWrapper extends Library {

    int set_error(String errorMsg);

    int get_last_error(byte[] output,
                       IntByReference outputSize);

    /* Internal callbacks FFI */
    interface FetchEntryCallback extends Callback {
        int apply(
                  Pointer output,
                  IntByReference outputSize,
                  Pointer uidsPointer,
                  int uidsLength)
            throws CloudproofException;
    }

    interface FetchAllEntryCallback extends Callback {
        int apply(Pointer output,
                  IntByReference outputSize,
                  int numberOfEntries)
            throws CloudproofException;
    }

    interface FetchChainCallback extends Callback {
        int apply(Pointer output,
                  IntByReference outputSize,
                  Pointer uidsPointer,
                  int uidsLength)
            throws CloudproofException;
    }

    interface UpsertEntryCallback extends Callback {
        int apply(
                  Pointer entries,
                  int entriesLength,
                  Pointer outputs,
                  IntByReference outputsLength)
            throws CloudproofException;
    }

    interface UpsertChainCallback extends Callback {
        int apply(Pointer chains,
                  int chainsLength)
            throws CloudproofException;
    }

    interface UpdateLinesCallback extends Callback {
        int apply(Pointer removedChains,
                  int removedChainsLength,
                  Pointer newEntries,
                  int newEntriesLength,
                  Pointer newChains,
                  int newChainsLength)
            throws CloudproofException;
    }

    interface ListRemovedLocationsCallback extends Callback {
        int apply(Pointer output,
                  IntByReference outputSize,
                  Pointer locations,
                  int locationsLength)
            throws CloudproofException;
    }

    interface ProgressCallback extends Callback {
        boolean apply(Pointer output,
                      int outputSize)
            throws CloudproofException;
    }

    int h_upsert(
                 Pointer masterKeyPointer,
                 int masterKeySize,
                 Pointer labelPointer,
                 int labelSize,
                 String dbUidsAndWordsJson,
                 FetchEntryCallback fetchEntry,
                 UpsertEntryCallback upsertEntry,
                 UpsertChainCallback upsertChain);

    int h_graph_upsert(String masterKeysJson,
                       Pointer labelPointer,
                       int labelSize,
                       String dbUidsAndWordsJson,
                       FetchEntryCallback fetchEntry,
                       UpsertEntryCallback upsertEntry,
                       UpsertChainCallback upsertChain);

    int h_compact(int numberOfReindexingPhasesBeforeFullSet,
                  String masterKeysJson,
                  Pointer labelPointer,
                  int labelSize,
                  FetchEntryCallback fetchEntry,
                  FetchChainCallback fetchChain,
                  FetchAllEntryCallback fetchAllEntry,
                  UpdateLinesCallback updateLines,
                  ListRemovedLocationsCallback listRemovedLocations);

    int h_search(byte[] dbUidsPtr,
                 IntByReference dbUidsSize,
                 Pointer keyKPointer,
                 int keyKLength,
                 Pointer labelPointer,
                 int labelSize,
                 String words,
                 int loopIterationLimit,
                 int maxDepth,
                 ProgressCallback progress,
                 FetchEntryCallback fetchEntry,
                 FetchChainCallback fetchChain);
}
