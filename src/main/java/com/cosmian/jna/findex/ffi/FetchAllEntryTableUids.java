package com.cosmian.jna.findex.ffi;

import java.util.Set;
import java.util.logging.Logger;

import com.cosmian.jna.findex.FindexCallbackException;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchAllEntryTableUidsCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchAllEntryTableUids;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchAllEntryTableUids implements FetchAllEntryTableUidsCallback {

    final Logger logger = Logger.getLogger(this.getClass().getName());

    private DBFetchAllEntryTableUids fetch;

    public FetchAllEntryTableUids(DBFetchAllEntryTableUids fetch) {
        this.fetch = fetch;
    }

    @Override
    public int apply(Pointer uidsPointer,
                     IntByReference uidsSize)
        throws CloudproofException {
        try {
            //
            // Select uids and values in EntryTable
            //
            Set<Uid32> uidsAndValues = this.fetch.fetchAll();
    
            //
            // Serialize results
            //
            return FFiUtils.setToOutputPointer(uidsAndValues, uidsPointer, uidsSize);
        } catch (CloudproofException e) {
            return FindexCallbackException.record(e);
        }
    }

}
