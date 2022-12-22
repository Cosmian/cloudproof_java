package com.cosmian.jna.findex.ffi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchChainCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchChain;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchChain implements FetchChainCallback {

    final Logger logger = Logger.getLogger(this.getClass().getName());

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

        if (uidsLength == 0 && uidsPointer == null) {
            logger.fine("callback called with 0 Uids");
            return FFiUtils.mapToOutputPointer(new HashMap<>(), output, outputSize);
        }

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
