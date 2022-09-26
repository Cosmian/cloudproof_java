package com.cosmian.jna.findex.Callbacks;

import java.util.HashMap;
import java.util.List;

import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.Ffi;
import com.cosmian.jna.findex.FfiWrapper.FetchChainCallback;
import com.cosmian.jna.findex.FfiWrapper.FetchChainInterface;
import com.cosmian.jna.findex.Leb128Serializer;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchChain implements FetchChainCallback {

    private FetchChainInterface fetch;

    public FetchChain(FetchChainInterface fetch) {
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
        // Deserialize Chain Table uids
        //
        List<byte[]> chainTableUids = Leb128Serializer.deserializeList(uids);

        //
        // Select uid and value in ChainTable
        //
        HashMap<byte[], byte[]> uidsAndValues = this.fetch.fetch(chainTableUids);

        //
        // Serialize results
        //
        return Ffi.writeOutputPointerAndSize(uidsAndValues, output, outputSize);
    }

}
