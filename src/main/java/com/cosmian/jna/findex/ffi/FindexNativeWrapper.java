package com.cosmian.jna.findex.ffi;

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
    interface FetchCallback extends Callback {
        int callback(Pointer output,
                  IntByReference outputLen,
                  Pointer uidsPtr,
                  int uidsLength);
    }

    interface UpsertCallback extends Callback {
        int callback(Pointer outputs,
                  IntByReference outputsLength,
                  Pointer oldValues,
                  int oldValuesLength,
                  Pointer newValues,
                  int newValuesLength);
    }

    interface InsertCallback extends Callback {
        int callback(Pointer chains, int chainsLength);
    }

    interface DeleteCallback extends Callback {
        int callback(Pointer entriesPtr, int entriesLen);
    }

    interface DumpTokensCallback extends Callback {
        int callback(Pointer outputPtr, IntByReference outputLen);
    }

    interface InterruptCallback extends Callback {
        boolean callback(Pointer outputPtr, int outputLen);
    }

    interface FilterLocationsCallback extends Callback {
        int callback(Pointer outputLocationsPtr,
                  IntByReference outputLocationsLen,
                  Pointer locationsPtr,
                  int locationsLen);
    }

    int h_instantiate_with_ffi_backend(IntByReference handle,
                                       Pointer keyPtr,
                                       int keyLen,
                                       Pointer labelPtr,
                                       int labelLen,
                                       int entryTableNumber,
                                       FetchCallback fetchentry,
                                       FetchCallback fetchChain,
                                       UpsertCallback upsertEntry,
                                       InsertCallback insertChain,
                                       DeleteCallback deleteEntry,
                                       DeleteCallback deleteChain,
                                       DumpTokensCallback dumpTokens);

    int h_instantiate_with_rest_backend(IntByReference handle,
                                        Pointer labelPtr,
                                        int labelLen,
					String token,
					String url);

    int h_add(byte[] newKeywordsBufferPtr,
                 IntByReference newKeywordsBufferLen,
                 int handle,
                 Pointer additionsPtr,
                 int additionsLen);

    int h_delete(byte[] newKeywordsBufferPtr,
                 IntByReference newKeywordsBufferLen,
                 int handle,
                 Pointer deletionPtr,
                 int deletionLen);

    int h_compact(int handle,
                  Pointer newKeyPtr,
                  int newKeyLen,
                  Pointer newLabelPtr,
                  int newLabelLen,
                  int numCompactToFull,
                  FilterLocationsCallback filterObsoleteData);

    int h_search(byte[] searchResultsPtr,
                 IntByReference searchResultsLen,
                 int handle,
                 Pointer keywordsPtr,
                 int keywordsLen,
                 InterruptCallback interrupt);

    int h_generate_new_token(byte[] tokenPtr,
			     IntByReference tokenLen,
                             String indexId,
                             Pointer fetchEntriesSeedPtr,
                             int fetchEntriesSeedLen,
                             Pointer fetchChainsSeedPtr,
                             int fetchChainsSeedLen,
                             Pointer upsertEntriesSeedPtr,
                             int upsertEntriesSeedLen,
                             Pointer insertChainsSeedPtr,
                             int insertChainsSeedSize);
}
