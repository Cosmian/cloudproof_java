package com.cosmian.jna.findex;

import java.util.HashMap;
import java.util.List;

import com.cosmian.jna.FfiException;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * This maps the Findex Secure Searchable Encryption written in Rust
 */
public interface FfiWrapper extends Library {

    int set_error(String errorMsg);

    int get_last_error(byte[] output, IntByReference outputSize);

    /* Internal callbacks FFI */
    interface FetchEntryCallback extends Callback {
        int apply(Pointer output, IntByReference outputSize, Pointer uidsPointer, int uidsLength) throws FfiException;
    }
    interface FetchChainCallback extends Callback {
        int apply(Pointer output, IntByReference outputSize, Pointer uidsPointer, int uidsLength) throws FfiException;
    }
    interface UpsertEntryCallback extends Callback {
        int apply(Pointer entries, int entriesLength) throws FfiException;
    }
    interface UpsertChainCallback extends Callback {
        int apply(Pointer chains, int chainsLength) throws FfiException;
    }

    /* Customer high-level callbacks */
    interface FetchEntryInterface {
        public HashMap<byte[], byte[]> fetch(List<byte[]> uids) throws FfiException;
    }
    interface FetchChainInterface {
        public List<byte[]> fetch(List<byte[]> uids) throws FfiException;
    }
    interface UpsertEntryInterface {
        public void upsert(HashMap<byte[], byte[]> uidsAndValues) throws FfiException;
    }
    interface UpsertChainInterface {
        public void upsert(HashMap<byte[], byte[]> uidsAndValues) throws FfiException;
    }

    int h_upsert(String masterKeysJson, String dbUidsAndWordsJson, FetchEntryCallback fetchEntry,
        UpsertEntryCallback upsertEntry, UpsertChainCallback upsertChain);

    int h_search(byte[] dbUidsPtr, IntByReference dbUidsSize, Pointer keyKPointer,
        int keyKLength, String words,
        int loopIterationLimit, FetchEntryCallback fetchEntry, FetchChainCallback fetchChain);
}
