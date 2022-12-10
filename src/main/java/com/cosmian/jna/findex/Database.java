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
