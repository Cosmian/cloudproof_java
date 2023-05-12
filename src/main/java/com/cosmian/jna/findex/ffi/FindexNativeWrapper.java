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
                  IntByReference outputLen,
                  Pointer uidsPtr,
                  int uidsLength)
            throws CloudproofException;
    }

    interface FetchAllEntryTableUidsCallback extends Callback {
        int apply(Pointer uidsPtr,
                  IntByReference uidsSize)
            throws CloudproofException;
    }

    interface FetchChainCallback extends Callback {
        int apply(Pointer output,
                  IntByReference outputLen,
                  Pointer uidsPtr,
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
                  IntByReference outputLen,
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
                 Pointer masterKeyPtr,
                 int masterKeyLen,
                 Pointer labelPtr,
                 int labelLen,
                 String additions,
                 String deletions,
                 FetchEntryCallback fetchEntry,
                 UpsertEntryCallback upsertEntry,
                 UpsertChainCallback upsertChain);

    int h_compact(Pointer oldMasterKeyPtr,
                  int oldMasterKeyLen,
                  Pointer newMasterKeyPtr,
                  int newMasterKeyLen,
                  Pointer newLabelPtr,
                  int newLabelLen,
                  int numReindexingBeforeFullSet,
                  FetchAllEntryTableUidsCallback fetchAllEntryTableUids,
                  FetchEntryCallback fetchEntry,
                  FetchChainCallback fetchChain,
                  UpdateLinesCallback updateLines,
                  ListRemovedLocationsCallback listRemovedLocations);

    int h_search(byte[] searchresultsPtr,
                 IntByReference searchresultsLen,
                 Pointer masterKeyPtr,
                 int masterKeyLength,
                 Pointer labelPtr,
                 int labelLen,
                 String keywords,
                 ProgressCallback progress,
                 FetchEntryCallback fetchEntry,
                 FetchChainCallback fetchChain);

    int h_search_cloud(byte[] dbUidsPtr,
                       IntByReference dbUidsLen,
                       String token,
                       Pointer labelPtr,
                       int labelLen,
                       String keywords,
                       String baseUrl);

    int h_upsert_cloud(String token,
                       Pointer labelPtr,
                       int labelLen,
                       String additions,
                       String deletions,
                       String baseUrl);


    int h_generate_new_token(byte[] tokenPtr,
        IntByReference tokenLen,
        String indexIdPtr,
        Pointer fetchEntriesSeedPtr,
        int fetchEntriesSeedLen,
        Pointer fetchChainsSeedPtr,
        int fetchChainsSeedLen,
        Pointer upsertEntriesSeedPtr,
        int upsertEntriesSeedLen,
        Pointer insertChainsSeedPtr,
        int insertChainsSeedSize);

}
