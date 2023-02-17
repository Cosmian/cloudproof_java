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

    interface FetchAllEntryTableUidsCallback extends Callback {
        int apply(Pointer uidsPointer,
                  IntByReference uidsSize)
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
        int apply(Pointer outputs,
                  IntByReference outputsLength,
                  Pointer entries,
                  int entriesLength)
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

    int h_compact(int numberOfReindexingPhasesBeforeFullSet,
                  Pointer existingKeyPointer,
                  int existingKeySize,
                  Pointer newKeyPointer,
                  int newKeySize,
                  Pointer labelPointer,
                  int labelSize,
                  FetchAllEntryTableUidsCallback fetchAllEntryTableUids,
                  FetchEntryCallback fetchEntry,
                  FetchChainCallback fetchChain,
                  UpdateLinesCallback updateLines,
                  ListRemovedLocationsCallback listRemovedLocations);

    int h_search(byte[] dbUidsPtr,
                 IntByReference dbUidsSize,
                 Pointer keyKPointer,
                 int keyKLength,
                 Pointer labelPointer,
                 int labelSize,
                 String words,
                 int maxResultsPerKeyword,
                 int maxDepth,
                 int insecureFetchChainsBatchSize,
                 ProgressCallback progress,
                 FetchEntryCallback fetchEntry,
                 FetchChainCallback fetchChain);

    int h_search_cloud(byte[] dbUidsPtr,
                       IntByReference dbUidsSize,
                       String token,
                       Pointer labelPointer,
                       int labelSize,
                       String words,
                       int maxResultsPerKeyword,
                       int maxDepth,
                       int insecureFetchChainsBatchSize,
                       String baseUrl);

    int h_upsert_cloud(String token,
                       Pointer labelPointer,
                       int labelSize,
                       String dbUidsAndWordsJson,
                       String baseUrl);

}
