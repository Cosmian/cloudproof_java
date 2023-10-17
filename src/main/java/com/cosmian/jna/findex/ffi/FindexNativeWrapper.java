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
        int apply(Pointer output,
                  IntByReference outputLen,
                  Pointer uidsPtr,
                  int uidsLength)
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
                  Pointer oldValues,
                  int oldValuesLength,
                  Pointer newValues,
                  int newValuesLength)
            throws CloudproofException;
    }

    interface InsertChainCallback extends Callback {
        int apply(Pointer chains,
                  int chainsLength)
            throws CloudproofException;
    }

    interface DeleteEntryCallback extends Callback {
        int apply(Pointer entriesPtr,
                  int entriesLen)
            throws CloudproofException;
    }

    interface DeleteChainCallback extends Callback {
        int apply(Pointer chainsPtr,
                  int chainsLen)
            throws CloudproofException;
    }

    interface DumpTokensCallback extends Callback {
        int apply(Pointer outputPtr,
                  IntByReference outputLen)
            throws CloudproofException;
    }

    interface InterruptCallback extends Callback {
        boolean apply(Pointer outputPtr,
                      int outputLen)
            throws CloudproofException;
    }

    interface FilterObsoleteLocationsCallback extends Callback {
        int apply(Pointer outputLocationsPtr,
                  IntByReference outputLocationsLen,
                  Pointer locationsPtr,
                  int locationsLen)
            throws CloudproofException;
    }

    int h_upsert(byte[] newKeywordsBufferPtr,
                 IntByReference newKeywordsBufferLen,
                 Pointer KeyPtr,
                 int KeyLen,
                 Pointer labelPtr,
                 int labelLen,
                 Pointer additionsPtr,
                 int additionsLen,
                 Pointer deletionsPtr,
                 int deletionsLen,
                 int entryTableNumber,
                 FetchEntryCallback fetchEntry,
                 UpsertEntryCallback upsertEntry,
                 InsertChainCallback insertChain);

    int h_compact(Pointer oldKeyPtr,
                  int oldKeyLen,
                  Pointer newKeyPtr,
                  int newKeyLen,
                  Pointer oldLabelPtr,
                  int oldLabelLen,
                  Pointer newLabelPtr,
                  int newLabelLen,
                  int nCompactToFull,
                  int entryTableNumber,
                  FetchEntryCallback fetchEntry,
                  FetchChainCallback fetchChain,
                  UpsertEntryCallback upsertEntry,
                  InsertChainCallback insertChain,
                  DeleteEntryCallback deleteEntry,
                  DeleteChainCallback deleteChain,
                  DumpTokensCallback dumpTokens,
                  FilterObsoleteLocationsCallback filterObsoleteData);

    int h_search(byte[] searchResultsPtr,
                 IntByReference searchResultsLen,
                 Pointer keyPtr,
                 int keyLength,
                 Pointer labelPtr,
                 int labelLen,
                 Pointer keywordsPtr,
                 int keywordsLen,
                 int entryTableNumber,
                 InterruptCallback interrupt,
                 FetchEntryCallback fetchEntry,
                 FetchChainCallback fetchChain);

    int h_search_cloud(byte[] searchResultsPtr,
                       IntByReference searchResultsLen,
                       String token,
                       Pointer labelPtr,
                       int labelLen,
                       Pointer keywordsPtr,
                       int keywordsLen,
                       String baseUrl,
                       InterruptCallback interrupt);

    int h_upsert_cloud(byte[] newKeywordsBufferPtr,
                       IntByReference newKeywordsBufferLen,
                       String token,
                       Pointer labelPtr,
                       int labelLen,
                       Pointer additionsPtr,
                       int additionsLen,
                       Pointer deletionsPtr,
                       int deletionsLen,
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
