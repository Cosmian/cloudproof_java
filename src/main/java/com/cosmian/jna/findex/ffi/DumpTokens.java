package com.cosmian.jna.findex.ffi;

import java.util.Set;

import com.cosmian.jna.findex.FindexCallbackException;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.DumpTokensCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBDumpTokens;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class DumpTokens implements DumpTokensCallback {
    private DBDumpTokens dumpTokens;

    public DumpTokens(DBDumpTokens dumpTokens) {
        this.dumpTokens = dumpTokens;
    }

    @Override
    public int apply(Pointer uidsPointer,
                     IntByReference uidsLen)
        throws CloudproofException {
        try {
            //
            // Select uids and values in EntryTable
            //
            Set<Uid32> uidsAndValues = this.dumpTokens.fetchAll();

            //
            // Serialize results
            //
            return FFiUtils.setToOutputPointer(uidsAndValues, uidsPointer, uidsLen);
        } catch (CloudproofException e) {
            return FindexCallbackException.record(e);
        }
    }

}
