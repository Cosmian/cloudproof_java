package com.cosmian.jna.findex.ffi;

import java.util.List;
import java.util.logging.Logger;

import com.cosmian.jna.findex.FindexCallbackException;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchEntry;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.serde.Tuple;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchEntry implements FetchEntryCallback {

    final Logger logger = Logger.getLogger(this.getClass().getName());

    private DBFetchEntry fetch;

    public FetchEntry(DBFetchEntry fetch) {
        this.fetch = fetch;
    }

    @Override
    public int apply(Pointer output,
                     IntByReference outputSize,
                     Pointer uidsPointer,
                     int uidsLength)
        throws CloudproofException {
        try {
            //
            // Read `uidsPointer` until `uidsLength`
            //
            byte[] uids = new byte[uidsLength];
            uidsPointer.read(0, uids, 0, uidsLength);

            //
            // Deserialize Entry Table uids
            //
            List<Uid32> entryTableUids = Leb128Reader.deserializeCollection(Uid32.class, uids);

            //
            // Select uids and values in EntryTable
            //
            List<Tuple<Uid32, EntryTableValue>> uidsAndValues = this.fetch.fetch(entryTableUids);

            //
            // Serialize results
            //
            return FFiUtils.listOfTuplesToOutputPointer(uidsAndValues, output, outputSize);
        } catch (CloudproofException e) {
            return FindexCallbackException.record(e);
        }
    }

}
