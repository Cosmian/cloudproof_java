package com.cosmian.jna.findex.ffi;

import java.util.List;
import java.util.Map;

import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchChainCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchChain;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchChain implements FetchChainCallback {

    private DBFetchChain fetch;

    public FetchChain(DBFetchChain fetch) {
        this.fetch = fetch;
    }

    @Override
    public int apply(Pointer output,
                     IntByReference outputSize,
                     Pointer uidsPointer,
                     int uidsLength)
        throws CloudproofException {
        //
        // Read `uidsPointer` until `uidsLength`
        //
        byte[] uids = new byte[uidsLength];
        uidsPointer.read(0, uids, 0, uidsLength);

        //
        // Deserialize Chain Table uids
        //
        List<Uid32> chainTableUids = Leb128Reader.deserializeCollection(Uid32.class, uids);

        //
        // Select uid and value in ChainTable
        //
        Map<Uid32, ChainTableValue> uidsAndValues = this.fetch.fetch(chainTableUids);

        //
        // Serialize results
        //
        return FFiUtils.mapToOutputPointer(uidsAndValues, output, outputSize);
    }

}
