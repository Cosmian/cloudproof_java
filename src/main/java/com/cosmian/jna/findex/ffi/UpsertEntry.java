package com.cosmian.jna.findex.ffi;

import java.util.Map;

import com.cosmian.jna.findex.Findex;
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
    public int apply(Pointer entries,
                     int entriesLength,
                     Pointer outputs,
                     IntByReference outputsLength)
        throws CloudproofException {
        //
        // Read `entries` until `itemsLength`
        //
        byte[] entriesBytes = new byte[entriesLength];
        entries.read(0, entriesBytes, 0, entriesLength);

        Map<Uid32, EntryTableValues> map =
            Leb128Reader.deserializeMap(Uid32.class, EntryTableValues.class, entriesBytes);

        Map<Uid32, EntryTableValue> failedEntries = upsert.upsert(map);
        return Findex.mapToOutputPointer(failedEntries, outputs, outputsLength);

    }

}
