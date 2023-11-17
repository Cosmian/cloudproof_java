package com.cosmian.jna.findex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.ffi.FFiUtils;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpsertCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.DeleteCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.DumpTokensCallback;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.serde.Tuple;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface EntryTableDatabase {

    /**
     * Fetch all the Entry Table Uids.
     * <p>
     * Implementation of this method is only required for the compact
     * operation.
     *
     * @return the {@link Set} of all {@link Uid32}
     * @throws {@link CloudproofException} if anything goes wrong
     */
    public Set<Uid32> fetchAllUids() throws CloudproofException;

    /**
     * Fetch the Entry Table lines for the list of given {@link Uid32}. If a line does not exist, there should be not
     * entry in the returned map.
     * <p>
     * Implementation of this method is always required.
     *
     * @param uids the unique {@link Uid32}s used as line id
     * @return a {@link List} of {@link Tuple} of {@link Uid32} and {@link EntryTableValue}
     * @throws {@link CloudproofException} if anything goes wrong
     */
    public List<Tuple<Uid32, EntryTableValue>> fetch(List<Uid32> uids) throws CloudproofException;

    /**
     * Upsert the given lines into the Entry Table.
     * <p>
     * The {@link EntryTableValues} structure contains both the new value to be upserted and the previous value known at
     * the time of fetch. To avoid concurrency issues, the new value of an existing {@link Uid32} must <b>not</b> be
     * updated if the current value in the database does not match the previous value of the structure. In such a case,
     * the {@link Uid32} and the <b>current</b> database value must be returned as part of the returned {@link Map}. *
     * <p>
     * Implementation of this method is only required to update or compact the index
     * <p>
     * See the Redis and Sqlite implementations for implementation examples
     * <p>
     *
     * @param uidsAndValues a {@link Map} of {@link Uid32} to {@link EntryTableValues}
     * @return a map of the {@link Uid32} that could not be updated and the current database value for the entry.
     * @throws {@link CloudproofException} if anything goes wrong
     */
    public Map<Uid32, EntryTableValue> upsert(Map<Uid32, EntryTableValues> uidsAndValues)
	throws CloudproofException;

    /**
     * Delete the lines with the given UIDs.
     *
     * @param uids a {@link List} of {@link Uid32}
     * @throws {@link CloudproofException} if anything goes wrong
     */
    public void delete(List<Uid32> uids) throws CloudproofException;

    /**
     * Return the appropriate fetch callback (with input/output serialization).
     */
    default FetchCallback fetchCallback() {
        return new FetchCallback() {
            @Override
            public int callback(Pointer output,
                    IntByReference outputLen,
                    Pointer uidsPtr,
                    int uidsLength)
	    {

                try {
                    byte[] uids = new byte[uidsLength];
                    uidsPtr.read(0, uids, 0, uidsLength);

                    List<Uid32> entryTableUids = Leb128Reader.deserializeCollection(Uid32.class, uids);

                    List<Tuple<Uid32, EntryTableValue>> uidsAndValues = fetch(entryTableUids);

                    return FFiUtils.listOfTuplesToOutputPointer(uidsAndValues, output, outputLen);
                } catch (CloudproofException e) {
                    return FindexCallbackException.record(e);
                }
            }
        };
    }


    /**
     * Return the appropriate upsert callback (with input/output serialization).
     */
    default UpsertCallback upsertCallback() {
        return new UpsertCallback() {
            @Override
            public int callback(Pointer outputs,
                    IntByReference outputsLength,
                    Pointer oldValues,
                    int oldValuesLength,
                    Pointer newValues,
                    int newValuesLength)
	    {
                try {
                    //
                    // Read `oldValues` until `oldValuesLength`
                    //
                    byte[] oldValuesBytes = new byte[oldValuesLength];
                    oldValues.read(0, oldValuesBytes, 0, oldValuesLength);
                    Map<Uid32, EntryTableValue> oldValuesMap =
                        Leb128Reader.deserializeMap(Uid32.class, EntryTableValue.class, oldValuesBytes);

                    //
                    // Read `newValues` until `newValuesLength`
                    //
                    byte[] newValuesBytes = new byte[newValuesLength];
                    newValues.read(0, newValuesBytes, 0, newValuesLength);
                    Map<Uid32, EntryTableValue> newValuesMap =
                        Leb128Reader.deserializeMap(Uid32.class, EntryTableValue.class, newValuesBytes);

                    //
                    // merge both table values
                    //
                    Map<Uid32, EntryTableValues> map = new HashMap<Uid32, EntryTableValues>();
                    for (Map.Entry<Uid32, EntryTableValue> newValuesIter : newValuesMap.entrySet()) {
                        boolean optionalValueFound = false;
                        for (Map.Entry<Uid32, EntryTableValue> oldValuesIter : oldValuesMap.entrySet()) {
                            if (newValuesIter.getKey().equals( oldValuesIter.getKey())) {
                                EntryTableValues e = new EntryTableValues(oldValuesIter.getValue(), newValuesIter.getValue());
                                map.put(newValuesIter.getKey(), e);
                                optionalValueFound = true;
                            }
                        }
                        if (!optionalValueFound) {
                            EntryTableValues e = new EntryTableValues(new EntryTableValue(), newValuesIter.getValue());
                            map.put(newValuesIter.getKey(), e);
                        }
                    }

                    Map<Uid32, EntryTableValue> failedEntries = upsert(map);
                    return FFiUtils.mapToOutputPointer(failedEntries, outputs, outputsLength);
                } catch (CloudproofException e) {
                    return FindexCallbackException.record(e);
                }
            }
        };
    }

    /**
     * Return the appropriate upsert callback (with input/output serialization).
     */
    default DeleteCallback deleteCallback() {
        return new DeleteCallback() {
            @Override
            public int callback(Pointer items, int itemsLength) {
                try {
                    //
                    // Read `items` until `itemsLength`
                    //
                    byte[] itemsBytes = new byte[itemsLength];
                    items.read(0, itemsBytes, 0, itemsLength);

                    //
                    // Deserialize the chain table items
                    //
                    List<Uid32> uids =
                        Leb128Reader.deserializeCollection(Uid32.class, itemsBytes);

                    //
                    // Insert in database
                    //
                    delete(uids);

                    return 0;
                } catch (CloudproofException e) {
                    return FindexCallbackException.record(e);
                }
            }
        };
    }

    default DumpTokensCallback dumpTokenCallback() {
        return new DumpTokensCallback() {
            @Override
            public int callback(Pointer uidsPointer, IntByReference uidsLen) {
                try {
                    //
                    // Select uids and values in EntryTable
                    //
                    Set<Uid32> uidsAndValues = fetchAllUids();

                    //
                    // Serialize results
                    //
                    return FFiUtils.setToOutputPointer(uidsAndValues, uidsPointer, uidsLen);
                } catch (CloudproofException e) {
                    return FindexCallbackException.record(e);
                }
            }

        };
    }
}
