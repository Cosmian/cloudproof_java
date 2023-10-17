package com.cosmian.jna.findex.ffi;

import java.util.HashMap;
import java.util.Map;

import com.cosmian.jna.findex.FindexCallbackException;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpsertEntryCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBUpsertEntry;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.EntryTableValues;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class UpsertEntry implements UpsertEntryCallback {

    private DBUpsertEntry upsert;

    public UpsertEntry(DBUpsertEntry upsert) {
        this.upsert = upsert;
    }

    @Override
    public int apply(Pointer outputs,
                     IntByReference outputsLength,
                     Pointer oldValues,
                     int oldValuesLength,
                     Pointer newValues,
                     int newValuesLength)
        throws CloudproofException {
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

            Map<Uid32, EntryTableValue> failedEntries = upsert.upsert(map);
            return FFiUtils.mapToOutputPointer(failedEntries, outputs, outputsLength);
        } catch (CloudproofException e) {
            return FindexCallbackException.record(e);
        }
    }

}
