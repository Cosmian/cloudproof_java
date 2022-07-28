package com.cosmian.jna.findex.Callbacks;

import java.util.HashMap;
import java.util.List;

import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.FfiWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.FfiWrapper.FetchEntryInterface;
import com.cosmian.jna.findex.Leb128Serializer;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchEntry implements FetchEntryCallback {
    private FetchEntryInterface fetch;

    public FetchEntry(FetchEntryInterface fetch) {
        this.fetch = fetch;
    }

    @Override
    public int apply(Pointer output, IntByReference outputSize, Pointer uidsPointer, int uidsLength)
        throws FfiException {
        //
        // Read `uidsPointer` until `uidsLength`
        //
        byte[] uids = new byte[uidsLength];
        uidsPointer.read(0, uids, 0, uidsLength);

        //
        // Deserialize Entry Table uids
        //
        List<byte[]> entryTableUids = Leb128Serializer.deserializeList(uids);

        //
        // Select uids and values in EntryTable
        //
        HashMap<byte[], byte[]> uidsAndValues = this.fetch.fetch(entryTableUids);

        //
        // Serialize results
        //
        if (uidsAndValues.size() > 0) {
            byte[] uidsAndValuesBytes = Leb128Serializer.serializeHashMap(uidsAndValues);
            output.write(0, uidsAndValuesBytes, 0, uidsAndValuesBytes.length);
            outputSize.setValue(uidsAndValuesBytes.length);
        } else {
            outputSize.setValue(0);
        }

        return 0;
    }

}
