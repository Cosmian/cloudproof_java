package com.cosmian.jna.findex.ffi;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

public interface FindexUserCallbacks {
    /* Customer high-level callbacks */

    interface DBFetchAllEntryTableUids {
        public Set<Uid32> fetchAll() throws CloudproofException;
    }

    interface DBFetchEntry {
        public Map<Uid32, EntryTableValue> fetch(List<Uid32> uids) throws CloudproofException;
    }

    interface DBFetchChain {
        public Map<Uid32, ChainTableValue> fetch(List<Uid32> uids) throws CloudproofException;
    }

    interface DBUpsertEntry {
        public Map<Uid32, EntryTableValue> upsert(Map<Uid32, EntryTableValues> uidsAndValues)
            throws CloudproofException;
    }

    interface DBUpsertChain {
        public void upsert(Map<Uid32, ChainTableValue> uidsAndValues) throws CloudproofException;
    }

    interface DBUpdateLines {
        public void update(List<Uid32> removedChains,
                           Map<Uid32, EntryTableValue> newEntries,
                           Map<Uid32, ChainTableValue> newChains)
            throws CloudproofException;
    }

    interface DBListRemovedLocations {
        public List<Location> list(List<Location> locations) throws CloudproofException;
    }

    interface SearchProgress {
        public boolean notify(ProgressResults results) throws CloudproofException;
    }
}
