package com.cosmian.jna.findex;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.ffi.FetchAllEntryTableUids;
import com.cosmian.jna.findex.ffi.FetchChain;
import com.cosmian.jna.findex.ffi.FetchEntry;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchAllEntryTableUidsCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchChainCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.ListRemovedLocationsCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.ProgressCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpdateLinesCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpsertChainCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpsertEntryCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchAllEntryTableUids;
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
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;

public abstract class Database {

    /**
     * Fetch all Uids of the Entry Table
     * <p>
     * Implementation of this method is only required to compact the index
     * 
     * @return the {@link Set} of all {@link Uid32}
     * @throws CloudproofException if anything goes wrong
     */
    protected abstract Set<Uid32> fetchAllEntryTableUids() throws CloudproofException;

    /**
     * Fetch the Entry Table lines for the list of given {@link Uid32}. If a line does not exist, there should be not
     * entry in the returned map.
     * <p>
     * Implementation of this only method is required to search, update or compact the index
     * 
     * @param uids the unique {@link Uid32}s used as line id
     * @return a {@link Map} of {@link Uid32} to {@link EntryTableValue}
     * @throws CloudproofException if anything goes wrong
     */
    protected abstract Map<Uid32, EntryTableValue> fetchEntries(List<Uid32> uids) throws CloudproofException;

    /**
     * Fetch the Chain Table lines for the list of given {@link Uid32}. If a line does not exist, there should be not
     * entry in the returned map.
     * <p>
     * Implementation of this only method is required to search or compact the index
     * 
     * @param uids the unique {@link Uid32}s used as line id
     * @return a {@link Map} of {@link Uid32} to {@link ChainTableValue}
     * @throws CloudproofException if anything goes wrong
     */
    protected abstract Map<Uid32, ChainTableValue> fetchChains(List<Uid32> uids) throws CloudproofException;

    /**
     * Upsert the given lines into the Entry Table.
     * <p>
     * The {@link EntryTableValues} structure contains both the new value to be upserted and the previous value known at
     * the time of fetch. To avoid concurrency issues, the new value of an existing {@link Uid32} must <b>not</b> be
     * updated if the current value in the database does not match the previous value of the structure. In such a case,
     * the {@link Uid32} and the <b>current</b> database value must be returned as part of the returned {@link Map}. *
     * <p>
     * Implementation of this only method is required to update or compact the index
     * <p>
     * See the Redis and Sqlite implementations for implementation examples
     * <p>
     * 
     * @param uidsAndValues a {@link Map} of {@link Uid32} to {@link EntryTableValues}
     * @return a map of the {@link Uid32} that could not be updated and the current database value for the entry.
     * @throws CloudproofException if anything goes wrong
     */
    protected abstract Map<Uid32, EntryTableValue> upsertEntries(Map<Uid32, EntryTableValues> uidsAndValues)
        throws CloudproofException;

    /**
     * Upsert the given lines into the Chain Table *
     * <p>
     * Implementation of this only method is required to update or compact the index
     * 
     * @param uidsAndValues a {@link Map} of {@link Uid32} to {@link ChainTableValue}
     * @throws CloudproofException if anything goes wrong
     */
    protected abstract void upsertChains(Map<Uid32, ChainTableValue> uidsAndValues) throws CloudproofException;

    /**
     * Update the database tables with the new values. This function should:
     * <ul>
     * <li>remove all the Index Entry Table</li>
     * <li>add _new_encrypted_entry_table_items_ to the Index Entry Table</li>
     * <li>remove _removed_chain_table_uids_ from the Index Chain Table</li>
     * <li>add _new_encrypted_chain_table_items_ to the Index Chain Table</li>
     * </ul>
     * The order of these operations is not important but have some implications:
     * <p>
     * <b>Option 1</b>
     * </P>
     * Keep the database small but prevent using the index during the _update_lines_.
     * <ul>
     * <li>remove all the Index Entry Table</li>
     * <li>add _new_encrypted_entry_table_items_ to the Index Entry Table</li>
     * <li>remove _removed_chain_table_uids_ from the Index Chain Table</li>
     * <li>add _new_encrypted_chain_table_items_ to the Index Chain Table</li>
     * </ul>
     * <br>
     * <br>
     * <b>Option 2</b>
     * <p>
     * During a small duration, the index tables are much bigger but users can continue using the index during the
     * _update_lines`.
     * <ul>
     * <li>save all UIDs from the current Index Entry Table</li>
     * <li>add _new_encrypted_entry_table_items_ to the Index Entry Table</li>
     * <li>add _new_encrypted_chain_table_items_ to the Index Chain Table</li>
     * <li>publish new label to users</li>
     * <li>remove old lines from the Index Entry Table (using the saved UIDs in 1.)</li>
     * <li>remove _removed_chain_table_uids_ from the Index Chain Table</li>
     * </ul>
     * <p>
     * *
     * <p>
     * Implementation of this only method is required to compact the index
     * 
     * @param removedChains a list of lines to remove from the Chain Table
     * @param newEntries a list of lines to add to the Entry Table (after it has been dropped)
     * @param newChains a list of lines to add to the Chain Table
     * @throws CloudproofException if anything goes wrong
     */
    protected abstract void updateTables(List<Uid32> removedChains,
                                         Map<Uid32, EntryTableValue> newEntries,
                                         Map<Uid32, ChainTableValue> newChains)
        throws CloudproofException;

    /**
     * Determine which of the passed {@link Location} no longer exist in the main database/storage and return them. *
     * <p>
     * Implementation of this only method is required to compact the index
     * 
     * @param locations the list to check for existence
     * @return the list of locations that no longer exist in the main database/storage
     * @throws CloudproofException if anything goes wrong
     */
    protected abstract List<Location> listRemovedLocations(List<Location> locations) throws CloudproofException;

    /**
     * The Findex search mechanism will call this method as the search for keywords progresses through the search graph.
     * <p>
     * The user should return <i>false</i> to immediately have the search return and stop further progressing down the
     * graph. *
     * <p>
     * Implementation of this only method is required to search the index
     * 
     * @param indexedValues A list of {@link IndexedValue} already found by the search
     * @return false to stop the graph from progressing
     * @throws CloudproofException if anything goes wrong
     */
    protected abstract boolean searchProgress(List<IndexedValue> indexedValues) throws CloudproofException;

    public FetchAllEntryTableUidsCallback fetchAllEntryTableUidsCallback() {
        return new FetchAllEntryTableUids(new DBFetchAllEntryTableUids() {

            @Override
            public Set<Uid32> fetchAll() throws CloudproofException {
                return Database.this.fetchAllEntryTableUids();
            }
        });
    }

    public FetchEntryCallback fetchEntryCallback() {
        return new FetchEntry(new DBFetchEntry() {

            @Override
            public Map<Uid32, EntryTableValue> fetch(List<Uid32> uids) throws CloudproofException {
                return Database.this.fetchEntries(uids);
            }

        });
    }

    public FetchChainCallback fetchChainCallback() {
        return new FetchChain(new DBFetchChain() {
            @Override
            public Map<Uid32, ChainTableValue> fetch(List<Uid32> uids) throws CloudproofException {
                return Database.this.fetchChains(uids);
            }
        });
    }

    public UpsertEntryCallback upsertEntryCallback() {
        return new UpsertEntry(new DBUpsertEntry() {
            @Override
            public Map<Uid32, EntryTableValue> upsert(Map<Uid32, EntryTableValues> uidsAndValues)
                throws CloudproofException {
                return Database.this.upsertEntries(uidsAndValues);
            }
        });
    }

    public UpsertChainCallback upsertChainCallback() {
        return new UpsertChain(new DBUpsertChain() {
            @Override
            public void upsert(Map<Uid32, ChainTableValue> uidsAndValues) throws CloudproofException {
                Database.this.upsertChains(uidsAndValues);
            }
        });
    }

    public UpdateLinesCallback updateLinesCallback() {
        return new UpdateLines(new DBUpdateLines() {

            @Override
            public void update(List<Uid32> removedChains,
                               Map<Uid32, EntryTableValue> newEntries,
                               Map<Uid32, ChainTableValue> newChains)
                throws CloudproofException {
                Database.this.updateTables(removedChains, newEntries, newChains);
            }

        });
    }

    public ListRemovedLocationsCallback listRemoveLocationsCallback() {
        return new ListRemovedLocations(new DBListRemovedLocations() {
            @Override
            public List<Location> list(List<Location> locations) throws CloudproofException {
                return Database.this.listRemovedLocations(locations);
            }
        });
    }

    public ProgressCallback progressCallback() {
        return new Progress(new SearchProgress() {
            @Override
            public boolean notify(List<IndexedValue> indexedValues) throws CloudproofException {
                return Database.this.searchProgress(indexedValues);
            }
        });
    }

}
