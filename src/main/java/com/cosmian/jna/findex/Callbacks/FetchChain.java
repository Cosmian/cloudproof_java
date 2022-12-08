package com.cosmian.jna.findex.Callbacks;

import java.util.List;
import java.util.Map;

import com.cosmian.CloudproofException;
import com.cosmian.jna.findex.ChainTableValue;
import com.cosmian.jna.findex.Findex;
import com.cosmian.jna.findex.FindexWrapper.FetchChainCallback;
import com.cosmian.jna.findex.FindexWrapper.FetchChainInterface;
import com.cosmian.jna.findex.Uid;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchChain implements FetchChainCallback {

    private FetchChainInterface fetch;

    public FetchChain(FetchChainInterface fetch) {
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
        List<Uid> chainTableUids = Leb128Reader.deserializeCollection(Uid.class, uids);

        //
        // Select uid and value in ChainTable
        //
        Map<Uid, ChainTableValue> uidsAndValues = this.fetch.fetch(chainTableUids);

        //
        // Serialize results
        //
        return Findex.mapToOutputPointer(uidsAndValues, output, outputSize);
    }

}
