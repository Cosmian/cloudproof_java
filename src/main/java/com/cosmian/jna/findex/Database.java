package com.cosmian.jna.findex;

import com.cosmian.jna.findex.ffi.FetchAllEntry;
import com.cosmian.jna.findex.ffi.FetchChain;
import com.cosmian.jna.findex.ffi.FetchEntry;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchAllEntriesCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchChainCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.ListRemovedLocationsCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.ProgressCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpdateLinesCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpsertChainCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpsertEntryCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchAllEntries;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchChain;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchEntry;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBListRemovedLocations;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBUpdateLines;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBUpsertChain;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBUpsertEntry;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.SearchProgress;
import com.cosmian.jna.findex.ffi.ListRemovedLocations;
import com.cosmian.jna.findex.ffi.Progress;
import com.cosmian.jna.findex.ffi.UpdateLines;
import com.cosmian.jna.findex.ffi.UpsertChain;
import com.cosmian.jna.findex.ffi.UpsertEntry;

public abstract class Database {

    protected abstract DBFetchEntry fetchEntry();

    protected abstract DBFetchAllEntries fetchAllEntries();

    protected abstract DBFetchChain fetchChain();

    protected abstract DBUpsertEntry upsertEntry();

    protected abstract DBUpsertChain upsertChain();

    /**
     * <pre>
     *  Update the database with the new values. This function should:
     *  - remove all the Index Entry Table
     *  - add `new_encrypted_entry_table_items` to the Index Entry Table
     *  - remove `removed_chain_table_uids` from the Index Chain Table
     *  - add `new_encrypted_chain_table_items` to the Index Chain Table
     * 
     *  The order of these operation is not important but have some implications:
     * 
     *  ### Option 1
     * 
     *  Keep the database small but prevent using the index during the
     *  `update_lines`.
     * 
     *  1. remove all the Index Entry Table
     *  2. add `new_encrypted_entry_table_items` to the Index Entry Table
     *  3. remove `removed_chain_table_uids` from the Index Chain Table
     *  4. add `new_encrypted_chain_table_items` to the Index Chain Table
     * 
     *  ### Option 2
     * 
     *  During a small duration, the index tables are much bigger but users can
     *  continue
     *  using the index during the `update_lines`.
     * 
     *  1. save all UIDs from the current Index Entry Table
     *  2. add `new_encrypted_entry_table_items` to the Index Entry Table
     *  3. add `new_encrypted_chain_table_items` to the Index Chain Table
     *  4. publish new label to users
     *  5. remove old lines from the Index Entry Table (using the saved UIDs in 1.)
     *  6. remove `removed_chain_table_uids` from the Index Chain Table
     * </pre>
     */
    protected abstract DBUpdateLines updateLines();

    protected abstract DBListRemovedLocations listRemovedLocations();

    protected abstract SearchProgress progress();

    public FetchEntryCallback fetchEntryCallback() {
        return new FetchEntry(fetchEntry());
    }

    public FetchAllEntriesCallback fetchAllEntriesCallback() {
        return new FetchAllEntry(fetchAllEntries());
    }

    public FetchChainCallback fetchChainCallback() {
        return new FetchChain(fetchChain());
    }

    public UpsertEntryCallback upsertEntryCallback() {
        return new UpsertEntry(upsertEntry());
    }

    public UpsertChainCallback upsertChainCallback() {
        return new UpsertChain(upsertChain());
    }

    public UpdateLinesCallback updateLinesCallback() {
        return new UpdateLines(updateLines());
    }

    public ListRemovedLocationsCallback listRemoveLocationsCallback() {
        return new ListRemovedLocations(listRemovedLocations());
    }

    public ProgressCallback progressCallback() {
        return new Progress(progress());
    }

}
